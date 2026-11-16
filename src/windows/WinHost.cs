// Applied Astrology v0.0.4 – Windows NATIVE (C# WinForms, .NET Framework 4.x; Windows 7 SP1 → 11; x86 / x64 / ARM64).
// Không dùng Edge WebView2 / IE11 / MSHTML: toàn bộ giao diện vẽ bằng GDI+ → Task Manager chỉ hiện "Applied Astrology",
// không có tiến trình msedgewebview2.exe, SmartScreen/antivirus không coi là vỏ trình duyệt.
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using AppliedAstrology.Core;
using Microsoft.Win32;

[assembly: AssemblyTitle("Applied Astrology")]
[assembly: AssemblyProduct("Applied Astrology (Tử Vi Ứng Dụng)")]
[assembly: AssemblyDescription("Applied Astrology - Tử Vi Ứng Dụng (Bửu Đình) - native Windows app")]
[assembly: AssemblyCompany("Applied Astrology")]
[assembly: AssemblyCopyright("Applied Astrology contributors (GPL-3.0)")]
[assembly: AssemblyVersion("0.0.4.0")]
[assembly: AssemblyFileVersion("0.0.4.0")]
[assembly: ComVisible(false)]

namespace AppliedAstrology
{
    static class Program
    {
        internal const string Version = "0.0.4", BuildDate = "2026-11-16";
        internal static string BaseDir = AppDomain.CurrentDomain.BaseDirectory, DataDir, TestOut, Compat = "";
        internal static bool SelfTest;
        [STAThread]
        static int Main(string[] args)
        {
            for (int i = 0; i < args.Length; i++)
            {
                string a = args[i].ToLowerInvariant();
                if (a == "--selftest") SelfTest = true;
                else if (a == "--out" && i + 1 < args.Length) TestOut = args[++i];
                else if ((a == "--compat" || a == "--engine") && i + 1 < args.Length) Compat = args[++i].ToLowerInvariant();
            }
            try { ServicePointManager.SecurityProtocol |= (SecurityProtocolType)3072; } catch { } // TLS 1.2 trên Windows 7
            bool portable = File.Exists(Path.Combine(BaseDir, "portable.ini"));
            DataDir = portable ? Path.Combine(BaseDir, "userdata") : Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "AppliedAstrology");
            Directory.CreateDirectory(DataDir);
            string flag = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "AppliedAstrology", "selftest.flag");
            if (!SelfTest && File.Exists(flag)) { SelfTest = true; try { TestOut = File.ReadAllText(flag).Trim(); File.Delete(flag); } catch { } }
            if (SelfTest) { if (string.IsNullOrEmpty(TestOut)) TestOut = Path.Combine(DataDir, "selftest"); Directory.CreateDirectory(TestOut); }
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);
            Application.ThreadException += delegate (object s, ThreadExceptionEventArgs e) { Host.WriteCrash("ui", e.Exception); };
            AppDomain.CurrentDomain.UnhandledException += delegate (object s, UnhandledExceptionEventArgs e) { Host.WriteCrash("domain", e.ExceptionObject as Exception); };
            try { SetCurrentProcessExplicitAppUserModelID("AppliedAstrology.TuViUngDung"); } catch { }
            try { if (!SetProcessDpiAwarenessContext(new IntPtr(-4))) SetProcessDPIAware(); } catch { try { SetProcessDPIAware(); } catch { } }
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Host.Log("start v" + Version + " native os=" + Environment.OSVersion.VersionString + " arch=" + Host.ProcArch() + " install=" + Host.InstallKind() + " compat=" + Compat + " selftest=" + SelfTest);
            Application.Run(new MainForm());
            return 0;
        }
        [DllImport("user32.dll")] static extern bool SetProcessDPIAware();
        [DllImport("user32.dll")] static extern bool SetProcessDpiAwarenessContext(IntPtr v);
        [DllImport("shell32.dll", CharSet = CharSet.Unicode)] static extern int SetCurrentProcessExplicitAppUserModelID(string id);
    }

    /// <summary>Dịch vụ nền tảng Windows: dữ liệu đóng gói, lưu trữ (store.json – cùng định dạng 0.0.3), nhật ký, sự cố, mạng, tệp.</summary>
    static class Host
    {
        internal sealed class Src : ISource
        {
            public string Read(string path)
            {
                try { string f = Path.Combine(Program.BaseDir, path.Replace('/', Path.DirectorySeparatorChar)); return File.Exists(f) ? File.ReadAllText(f, Encoding.UTF8) : null; }
                catch (Exception e) { Log("read " + path + " " + e.Message); return null; }
            }
        }
        static Dictionary<string, object> store; static string StoreFile { get { return Path.Combine(Program.DataDir, "store.json"); } }
        static void LoadStore()
        {
            if (store != null) return; store = new Dictionary<string, object>();
            try { if (File.Exists(StoreFile)) store = Json.Obj(Json.Parse(File.ReadAllText(StoreFile, Encoding.UTF8))) ?? store; } catch { }
        }
        public static object Get(string key) { LoadStore(); object v; if (!store.TryGetValue(key, out v) || v == null) return null; return v is string ? Json.Parse((string)v) : v; }
        public static void Set(string key, object val)
        {
            LoadStore(); store[key] = Json.Stringify(val);
            try { string tmp = StoreFile + ".tmp"; File.WriteAllText(tmp, Json.Stringify(store), new UTF8Encoding(false)); if (File.Exists(StoreFile)) File.Delete(StoreFile); File.Move(tmp, StoreFile); } catch (Exception e) { Log("store " + e.Message); }
        }
        static readonly DateTime Epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
        /// <summary>Mốc thời gian kiểu JS Date.now() – tự tính để chạy được trên .NET 4.5 (Windows 7 chưa cập nhật)</summary>
        public static double NowMs() { return (DateTime.UtcNow - Epoch).TotalMilliseconds; }
        public static DateTime FromMs(long ms) { return Epoch.AddMilliseconds(ms).ToLocalTime(); }
        public static List<object> List(string key) { return Json.Arr(Get(key)) ?? new List<object>(); }

        internal static bool LogEnabled = true;
        static readonly object logLock = new object();
        public static void Log(string msg)
        {
            lock (logLock)
            {
                if (!LogEnabled && !Program.SelfTest) return;
                try { string lf = Path.Combine(Program.DataDir, "app.log"); if (File.Exists(lf) && new FileInfo(lf).Length > 512 * 1024) { File.Copy(lf, lf + ".1", true); File.Delete(lf); } File.AppendAllText(lf, DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss ") + msg + Environment.NewLine, Encoding.UTF8); } catch { }
                if (Program.SelfTest) try { File.AppendAllText(Path.Combine(Program.TestOut, "app.log"), DateTime.Now.ToString("HH:mm:ss ") + msg + Environment.NewLine, Encoding.UTF8); } catch { }
            }
        }
        public static string ReadLog() { try { string lf = Path.Combine(Program.DataDir, "app.log"); string t = (File.Exists(lf + ".1") ? File.ReadAllText(lf + ".1") : "") + (File.Exists(lf) ? File.ReadAllText(lf) : ""); return t.Length > 160000 ? t.Substring(t.Length - 160000) : t; } catch { return ""; } }
        public static void ClearLog() { try { File.Delete(Path.Combine(Program.DataDir, "app.log")); File.Delete(Path.Combine(Program.DataDir, "app.log.1")); } catch { } }
        static string CrashDir { get { string d = Path.Combine(Program.DataDir, "crash"); Directory.CreateDirectory(d); return d; } }
        public static void WriteCrash(string where, Exception ex)
        {
            try
            {
                string txt = "Applied Astrology " + Program.Version + " (native)\r\nOS: " + Environment.OSVersion.VersionString + (Environment.Is64BitOperatingSystem ? " 64-bit" : " 32-bit") + "\r\nProcess: " + ProcArch() + "\r\nInstall: " + InstallKind() + "\r\nWhere: " + where + "\r\nTime: " + DateTime.Now.ToString("s") + "\r\n\r\n" + (ex == null ? "(unknown)" : ex.ToString());
                File.WriteAllText(Path.Combine(CrashDir, "crash_" + DateTime.Now.ToString("yyyyMMdd_HHmmss_fff") + ".txt"), txt, Encoding.UTF8);
                Log("CRASH " + where + " " + (ex == null ? "" : ex.Message));
            }
            catch { }
        }
        public static List<string[]> CrashList() { try { return Directory.GetFiles(CrashDir, "*.txt").OrderByDescending(f => f).Take(20).Select(f => new[] { Path.GetFileName(f), File.ReadAllText(f) }).ToList(); } catch { return new List<string[]>(); } }
        public static void ClearCrash() { try { foreach (var f in Directory.GetFiles(CrashDir)) File.Delete(f); } catch { } }

        public static string InstallKind()
        {
            string b = Program.BaseDir.ToLowerInvariant();
            if (File.Exists(Path.Combine(Program.BaseDir, "portable.ini"))) return "portable";
            if (b.Contains("\\windowsapps\\")) return "appx";
            if (File.Exists(Path.Combine(Program.BaseDir, "uninstall.exe"))) return "setup";
            try { using (var k = Registry.CurrentUser.OpenSubKey(@"Software\AppliedAstrology\Msi")) if (k != null) return "msi"; } catch { }
            return "setup";
        }
        public static string ProcArch() { string a = (Environment.GetEnvironmentVariable("PROCESSOR_ARCHITECTURE") ?? "").ToUpperInvariant(); return a == "ARM64" ? "arm64" : IntPtr.Size == 8 ? "x64" : "x86"; }
        public static string OsName()
        {
            string n = Environment.OSVersion.VersionString;
            try { using (var k = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Windows NT\CurrentVersion")) if (k != null) { string p = Convert.ToString(k.GetValue("ProductName")), b = Convert.ToString(k.GetValue("CurrentBuild")); if (!string.IsNullOrEmpty(b) && int.Parse(b) >= 22000) p = p.Replace("Windows 10", "Windows 11"); n = p + " (build " + b + ")"; } } catch { }
            return n + (Environment.Is64BitOperatingSystem ? " 64-bit" : " 32-bit");
        }
        public static string Engine() { return "Native WinForms + GDI+ (no WebView2/IE)" + (Program.Compat == "win7" || Program.Compat == "ie" ? " [compat win7]" : ""); }

        public sealed class Resp { public bool Ok; public int Status; public string Body = ""; public long Ms; }
        public static Resp HttpGet(string url, int timeout)
        {
            var r = new Resp(); var sw = Stopwatch.StartNew();
            try
            {
                var req = (HttpWebRequest)WebRequest.Create(url); req.Timeout = timeout; req.ReadWriteTimeout = timeout;
                req.UserAgent = "AppliedAstrology/" + Program.Version + " (Windows; native)";
                if (url.StartsWith("https://api.github.com/")) req.Accept = "application/vnd.github+json";
                using (var resp = (HttpWebResponse)req.GetResponse()) using (var sr = new StreamReader(resp.GetResponseStream(), Encoding.UTF8)) { r.Status = (int)resp.StatusCode; r.Body = sr.ReadToEnd(); r.Ok = r.Status >= 200 && r.Status < 400; }
            }
            catch (WebException we) { var hr = we.Response as HttpWebResponse; r.Status = hr != null ? (int)hr.StatusCode : 0; r.Body = we.Message; }
            catch (Exception e) { r.Body = e.Message; }
            r.Ms = sw.ElapsedMilliseconds; return r;
        }
        public static string SaveFile(string name, byte[] data)
        {
            try
            {
                string dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "AppliedAstrology");
                if (Program.SelfTest) dir = Path.Combine(Program.TestOut, "exports");
                Directory.CreateDirectory(dir); string safe = string.Join("_", name.Split(Path.GetInvalidFileNameChars())); string f = Path.Combine(dir, safe);
                File.WriteAllBytes(f, data); Log("saved " + f); return f;
            }
            catch (Exception e) { Log("saveFile " + e.Message); return null; }
        }
        public static void OpenUrl(string url) { try { if (!Program.SelfTest) Process.Start(new ProcessStartInfo(url) { UseShellExecute = true }); else Log("openUrl (selftest) " + url); } catch (Exception e) { Log("openUrl " + e.Message); } }
        public static void Copy(string text) { try { Clipboard.SetText(text); } catch { } }
    }

    /// <summary>Tải & cài bản cập nhật (Setup.exe / MSI theo kiến trúc / portable zip / appxbundle)</summary>
    static class Updater
    {
        internal const string OWNER = "kiritokmn1403", REPO = "Applied-Astrology-T-Vi-";
        internal static readonly string REPO_URL = "https://github.com/" + OWNER + "/" + REPO, RELEASES_URL = REPO_URL + "/releases", API_LATEST = "https://api.github.com/repos/" + OWNER + "/" + REPO + "/releases/latest";
        internal static volatile string State = "idle", Error = "", PathDone = ""; internal static long Got, Total;
        internal static Dictionary<string, object> Release, Asset; internal static bool Newer; internal static string LastMsg = "", CheckState = "";
        public static int CmpVer(string a, string b)
        {
            Func<string, int[]> p = s => (s ?? "").TrimStart('v', 'V').Split('.', '-').Select(x => { int v; return int.TryParse(new string(x.TakeWhile(char.IsDigit).ToArray()), out v) ? v : 0; }).ToArray();
            int[] x1 = p(a), y1 = p(b);
            for (int i = 0; i < Math.Max(x1.Length, y1.Length); i++) { int u = i < x1.Length ? x1[i] : 0, v = i < y1.Length ? y1[i] : 0; if (u != v) return u < v ? -1 : 1; }
            return 0;
        }
        public static Dictionary<string, object> PickAsset(List<object> assets)
        {
            string kind = Host.InstallKind(), arch = Host.ProcArch();
            Func<Func<string, bool>, Dictionary<string, object>> find = f => assets.Select(Json.Obj).FirstOrDefault(m => f(Json.Str(m["name"], "").ToLowerInvariant()));
            if (kind == "portable") return find(n => n.EndsWith("windows-portable.zip"));
            if (kind == "appx") return find(n => n.EndsWith(".appxbundle"));
            if (kind == "msi") return find(n => n.EndsWith("windows-" + arch + ".msi")) ?? find(n => n.EndsWith("setup.exe"));
            return find(n => n.EndsWith("setup.exe"));
        }
        public static Dictionary<string, object> Check()
        {
            Host.Log("update check " + API_LATEST);
            var r = Host.HttpGet(API_LATEST, 15000); var rel = r.Ok ? Json.Obj(Json.Parse(r.Body)) : null;
            if (rel == null || !rel.ContainsKey("tag_name")) { Host.Log("update check failed status=" + r.Status); CheckState = "fail"; return Json.Map("ok", false, "status", (long)r.Status); }
            string latest = Json.Str(rel["tag_name"], "").TrimStart('v', 'V'); var assets = Json.Arr(rel.ContainsKey("assets") ? rel["assets"] : null) ?? new List<object>();
            Release = rel; Asset = PickAsset(assets); Newer = CmpVer(latest, Program.Version) > 0; CheckState = Newer ? "new" : "latest";
            Host.Set("aa.lastUpdateCheck", Json.Map("at", Host.NowMs(), "latest", latest));
            Host.Log("update latest=" + latest + " current=" + Program.Version + " newer=" + Newer + " asset=" + (Asset == null ? "-" : Asset["name"]));
            return Json.Map("ok", true, "latest", latest, "current", Program.Version, "newer", Newer, "asset", Asset == null ? null : Asset["name"], "assets", (long)assets.Count);
        }
        public static bool Download(string url, string name)
        {
            if (string.IsNullOrEmpty(url) || !url.StartsWith("https://")) return false;
            lock (typeof(Updater)) { if (State == "running") return true; State = "running"; Got = 0; Total = 0; Error = ""; PathDone = ""; }
            var th = new Thread(() =>
            {
                try
                {
                    string dir = Path.Combine(Path.GetTempPath(), "AppliedAstrology-update"); Directory.CreateDirectory(dir);
                    string dst = Path.Combine(dir, string.Join("_", name.Split(Path.GetInvalidFileNameChars()))), part = dst + ".part";
                    var req = (HttpWebRequest)WebRequest.Create(url); req.UserAgent = "AppliedAstrology/" + Program.Version + " (Windows; native)"; req.AllowAutoRedirect = true; req.Timeout = 30000;
                    using (var resp = (HttpWebResponse)req.GetResponse())
                    {
                        Total = resp.ContentLength;
                        using (var s = resp.GetResponseStream()) using (var f = File.Create(part)) { var buf = new byte[65536]; int n; while ((n = s.Read(buf, 0, buf.Length)) > 0) { f.Write(buf, 0, n); Got += n; } }
                    }
                    if (Total > 0 && Got != Total) throw new IOException("incomplete " + Got + "/" + Total);
                    if (File.Exists(dst)) File.Delete(dst); File.Move(part, dst); PathDone = dst; State = "done"; Host.Log("update downloaded " + dst + " " + Got);
                }
                catch (Exception ex) { Error = ex.Message; State = "error"; Host.Log("update download error " + ex.Message); }
            }) { IsBackground = true };
            th.Start(); return true;
        }
        /// <summary>Setup.exe cài im lặng đúng thư mục hiện tại rồi mở lại; MSI msiexec /passive; portable giải nén đè (giữ userdata); appx mở App Installer.</summary>
        public static string Install(string file, Form form)
        {
            if (string.IsNullOrEmpty(file) || !File.Exists(file)) return null;
            string kind = Host.InstallKind(), exe = Path.Combine(Program.BaseDir, "AppliedAstrology.exe"), lower = file.ToLowerInvariant();
            Host.Log("update install " + file + " kind=" + kind);
            if (Program.SelfTest) return "selftest";
            string script = Path.Combine(Path.GetTempPath(), "AppliedAstrology-update", "apply_update.cmd"), pid = Process.GetCurrentProcess().Id.ToString();
            var sb = new StringBuilder(); sb.AppendLine("@echo off"); sb.AppendLine(":wait");
            sb.AppendLine("tasklist /FI \"PID eq " + pid + "\" 2>nul | find \"" + pid + "\" >nul && (ping -n 2 127.0.0.1 >nul & goto wait)");
            if (lower.EndsWith(".exe")) sb.AppendLine("\"" + file + "\" /S /D=\"" + Program.BaseDir.TrimEnd('\\') + "\"");
            else if (lower.EndsWith(".msi")) sb.AppendLine("msiexec /i \"" + file + "\" /passive /norestart");
            else if (lower.EndsWith(".zip"))
            {
                string tmp = Path.Combine(Path.GetTempPath(), "AppliedAstrology-update", "unzip"); if (Directory.Exists(tmp)) Directory.Delete(tmp, true);
                System.IO.Compression.ZipFile.ExtractToDirectory(file, tmp);
                string[] found = Directory.GetFiles(tmp, "AppliedAstrology.exe", SearchOption.AllDirectories); if (found.Length == 0) return null;
                sb.AppendLine("robocopy \"" + Path.GetDirectoryName(found[0]) + "\" \"" + Program.BaseDir.TrimEnd('\\') + "\" /E /XD userdata /NFL /NDL /NJH /NJS >nul");
            }
            else { Process.Start(new ProcessStartInfo(file) { UseShellExecute = true }); return "1"; }
            if (kind != "msi" || !lower.EndsWith(".msi")) sb.AppendLine("start \"\" \"" + exe + "\""); else sb.AppendLine("start \"\" \"%LOCALAPPDATA%\\Programs\\Applied Astrology\\AppliedAstrology.exe\"");
            File.WriteAllText(script, sb.ToString(), new UTF8Encoding(false));
            Process.Start(new ProcessStartInfo("cmd.exe", "/c \"" + script + "\"") { WindowStyle = ProcessWindowStyle.Hidden, CreateNoWindow = true, UseShellExecute = false });
            form.BeginInvoke(new MethodInvoker(form.Close)); return "1";
        }
        internal static string DebugInfo(MainForm f)
        {
            var s = new StringBuilder();
            s.Append("App: Applied Astrology ").Append(Program.Version).Append(" (build ").Append(Program.BuildDate).Append(", native)\n");
            s.Append("Platform: windows / ").Append(Host.InstallKind()).Append("\nOS: ").Append(Host.OsName()).Append("\nProcess: ").Append(Host.ProcArch()).Append(" · .NET CLR ").Append(Environment.Version);
            s.Append("\nEngine: ").Append(Host.Engine()).Append("\nLanguage: ").Append(f.I18n.Current).Append(" · Screen: ").Append(Screen.PrimaryScreen.Bounds.Width).Append('x').Append(Screen.PrimaryScreen.Bounds.Height).Append(" @").Append(f.Dpi.ToString("0.##"));
            s.Append("\nLiquid Glass: ").Append(f.S("lgMode")).Append(" · Window: ").Append(f.ClientSize.Width).Append('x').Append(f.ClientSize.Height);
            return s.ToString();
        }
        internal static string ReportUrl(MainForm f)
        {
            var cr = Host.CrashList();
            string body = "### Mô tả lỗi / Description\n\n\n### Các bước tái hiện / Steps\n1. \n\n### Thông tin thiết bị / Device info\n```\n" + DebugInfo(f) + "\n```\n" + (cr.Count == 0 ? "" : "\n### Lỗi gần đây / Recent crash\n```\n" + cr[0][1].Substring(0, Math.Min(1500, cr[0][1].Length)) + "\n```\n");
            return REPO_URL + "/issues/new?title=" + Uri.EscapeDataString("[Bug] ") + "&body=" + Uri.EscapeDataString(body.Substring(0, Math.Min(6000, body.Length)));
        }
        internal static readonly string[][] LICENSES = {
            new[] { "Applied Astrology", "GPL-3.0", REPO_URL }, new[] { "Âm lịch Việt Nam – thuật toán Hồ Ngọc Đức", "Free for non-commercial use", "https://www.informatik.uni-leipzig.de/~duc/amlich/" },
            new[] { "hochanh/amlich, copthuy/amlich, wolfhong/LunarCalendar (giờ hoàng đạo, tiết khí, ngày lễ)", "MIT", "https://github.com/copthuy/amlich" },
            new[] { "QR Code generator – Project Nayuki", "MIT", "https://github.com/nayuki/QR-Code-generator" },
            new[] { "archisvaze/liquid-glass (hồ sơ khúc xạ, viền phản xạ)", "MIT", "https://github.com/archisvaze/liquid-glass" },
            new[] { "rdev/liquid-glass-react (tham số kính)", "MIT", "https://github.com/rdev/liquid-glass-react" },
            new[] { "dashersw/liquid-glass-js (kiểu thanh/nút/khối kính)", "MIT", "https://github.com/dashersw/liquid-glass-js" },
            new[] { "Aniyomi / Nekoyomi (bố cục Giới thiệu, danh sách ngôn ngữ)", "Apache-2.0", "https://github.com/aniyomiorg/aniyomi" },
            new[] { "Mangayomi (kiểm tra cập nhật, nhật ký, báo cáo lỗi)", "Apache-2.0", "https://github.com/kodjodevf/mangayomi" },
            new[] { "WiX Toolset 3.14 (chỉ dùng khi build MSI)", "MS-RL", "https://github.com/wixtoolset/wix3" },
            new[] { "Nội dung sách \"Tử Vi Ứng Dụng\" – Bửu Đình (Tử Vũ)", "© tác giả", "" } };
    }
}
