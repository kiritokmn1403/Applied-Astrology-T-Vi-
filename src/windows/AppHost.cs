// Applied Astrology v0.0.3 - Windows host (Windows 7 SP1 trở lên; x86 / x64 / ARM64)
// Ưu tiên Microsoft Edge WebView2 (Windows 10/11) và tự chuyển sang IE11/MSHTML (Windows 7/8.1)
// khi máy không có WebView2 Runtime. Giao diện nằm trong thư mục "web" cạnh tệp exe.
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Net;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using Microsoft.Win32;

[assembly: AssemblyTitle("Applied Astrology")]
[assembly: AssemblyProduct("Applied Astrology (Tử Vi Ứng Dụng)")]
[assembly: AssemblyDescription("Applied Astrology - Tử Vi Ứng Dụng (Bửu Đình)")]
[assembly: AssemblyCompany("Applied Astrology")]
[assembly: AssemblyCopyright("Applied Astrology contributors")]
[assembly: AssemblyVersion("0.0.3.0")]
[assembly: AssemblyFileVersion("0.0.3.0")]
[assembly: ComVisible(false)]

namespace AppliedAstrology
{
    static class Program
    {
        internal static string BaseDir = AppDomain.CurrentDomain.BaseDirectory;
        internal static bool SelfTest;
        internal static string TestOut;
        internal static string ForceEngine;
        internal static string DataDir;
        internal const string Version = "0.0.3";

        [STAThread]
        static int Main(string[] args)
        {
            for (int i = 0; i < args.Length; i++)
            {
                string a = args[i].ToLowerInvariant();
                if (a == "--selftest") SelfTest = true;
                else if (a == "--out" && i + 1 < args.Length) TestOut = args[++i];
                else if (a == "--engine" && i + 1 < args.Length) ForceEngine = args[++i].ToLowerInvariant();
            }
            try { ServicePointManager.SecurityProtocol |= (SecurityProtocolType)3072; } catch { } // TLS 1.2 cho Windows 7
            bool portable = File.Exists(Path.Combine(BaseDir, "portable.ini"));
            DataDir = portable ? Path.Combine(BaseDir, "userdata")
                               : Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "AppliedAstrology");
            Directory.CreateDirectory(DataDir);
            // Gói MSIX/AppX không truyền được tham số dòng lệnh: dùng tệp cờ selftest.flag (nội dung = thư mục kết quả)
            string flag = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "AppliedAstrology", "selftest.flag");
            if (!SelfTest && File.Exists(flag))
            {
                SelfTest = true;
                try { TestOut = File.ReadAllText(flag).Trim(); File.Delete(flag); } catch { }
            }
            if (SelfTest)
            {
                if (string.IsNullOrEmpty(TestOut)) TestOut = Path.Combine(DataDir, "selftest");
                Directory.CreateDirectory(TestOut);
            }
            // v0.0.3: ghi báo cáo lỗi khi ứng dụng gặp ngoại lệ không xử lý
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);
            Application.ThreadException += delegate (object s, ThreadExceptionEventArgs e) { WriteCrash(e.Exception); };
            AppDomain.CurrentDomain.UnhandledException += delegate (object s, UnhandledExceptionEventArgs e) { WriteCrash(e.ExceptionObject as Exception); };
            Log("start v" + Version + " os=" + Environment.OSVersion.VersionString + " arch=" + ProcArch() + " install=" + InstallKind());
            try { SetProcessDPIAware(); } catch { }
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
            return 0;
        }

        [DllImport("user32.dll")] static extern bool SetProcessDPIAware();

        internal static string CrashDir { get { string d = Path.Combine(DataDir, "crash"); Directory.CreateDirectory(d); return d; } }
        internal static void WriteCrash(Exception ex)
        {
            try
            {
                string txt = "Applied Astrology " + Version + "\r\nOS: " + Environment.OSVersion.VersionString + (Environment.Is64BitOperatingSystem ? " 64-bit" : " 32-bit") +
                    "\r\nProcess: " + ProcArch() + "\r\nInstall: " + InstallKind() + "\r\nTime: " + DateTime.Now.ToString("s") + "\r\n\r\n" + (ex == null ? "(unknown)" : ex.ToString());
                File.WriteAllText(Path.Combine(CrashDir, "crash_" + DateTime.Now.ToString("yyyyMMdd_HHmmss") + ".txt"), txt, Encoding.UTF8);
                Log("CRASH " + (ex == null ? "" : ex.Message));
            }
            catch { }
        }
        /** Cách cài đặt: portable | appx | msi | setup */
        internal static string InstallKind()
        {
            string b = BaseDir.ToLowerInvariant();
            if (File.Exists(Path.Combine(BaseDir, "portable.ini"))) return "portable";
            if (b.Contains("\\windowsapps\\")) return "appx";
            if (File.Exists(Path.Combine(BaseDir, "uninstall.exe"))) return "setup";
            try { using (var k = Registry.CurrentUser.OpenSubKey(@"Software\AppliedAstrology\Msi")) if (k != null) return "msi"; } catch { }
            return "setup";
        }
        internal static bool LogEnabled = true;
        internal static void Log(string msg)
        {
            if (!LogEnabled && !SelfTest) return;
            try { string lf = Path.Combine(DataDir, "app.log"); if (File.Exists(lf) && new FileInfo(lf).Length > 512 * 1024) { File.Copy(lf, lf + ".1", true); File.Delete(lf); } } catch { }
            try { File.AppendAllText(Path.Combine(DataDir, "app.log"), DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss ") + msg + Environment.NewLine, Encoding.UTF8); } catch { }
            if (SelfTest) try { File.AppendAllText(Path.Combine(TestOut, "app.log"), DateTime.Now.ToString("HH:mm:ss ") + msg + Environment.NewLine, Encoding.UTF8); } catch { }
        }

        internal static string Arch()
        {
            string a = Environment.GetEnvironmentVariable("PROCESSOR_ARCHITEW6432");
            if (string.IsNullOrEmpty(a)) a = Environment.GetEnvironmentVariable("PROCESSOR_ARCHITECTURE");
            return (a ?? "unknown").ToUpperInvariant();
        }
        internal static string ProcArch()
        {
            string a = (Environment.GetEnvironmentVariable("PROCESSOR_ARCHITECTURE") ?? "").ToUpperInvariant();
            if (a == "ARM64") return "arm64";
            return IntPtr.Size == 8 ? "x64" : "x86";
        }
    }

    [ComVisible(true)]
    [ClassInterface(ClassInterfaceType.AutoDual)]
    public class HostBridge
    {
        private readonly MainForm form;
        private readonly Dictionary<string, string> store;
        private readonly string storeFile;
        private readonly JavaScriptSerializer json = new JavaScriptSerializer { MaxJsonLength = int.MaxValue };

        public HostBridge(MainForm f)
        {
            form = f;
            storeFile = Path.Combine(Program.DataDir, "store.json");
            store = new Dictionary<string, string>();
            try { if (File.Exists(storeFile)) store = json.Deserialize<Dictionary<string, string>>(File.ReadAllText(storeFile, Encoding.UTF8)) ?? store; } catch { }
            string st; if (store.TryGetValue("aa.settings", out st) && st != null && st.Contains("\"logs\":false")) Program.LogEnabled = false;
        }

        private static string ReadTail(string file, int max)
        {
            try
            {
                if (!File.Exists(file)) return "";
                using (var fs = new FileStream(file, FileMode.Open, FileAccess.Read, FileShare.ReadWrite))
                {
                    if (fs.Length > max) fs.Seek(-max, SeekOrigin.End);
                    using (var sr = new StreamReader(fs, Encoding.UTF8)) return sr.ReadToEnd();
                }
            }
            catch { return ""; }
        }

        private string CrashList()
        {
            var list = new List<Dictionary<string, object>>();
            try
            {
                string[] files = Directory.GetFiles(Program.CrashDir, "*.txt");
                Array.Sort(files); Array.Reverse(files);
                for (int i = 0; i < files.Length && i < 20; i++)
                    list.Add(new Dictionary<string, object> { { "name", Path.GetFileName(files[i]) }, { "text", ReadTail(files[i], 16384) } });
            }
            catch { }
            return json.Serialize(list);
        }

        public string HostCall(string cmd, string a, string b)
        {
            try
            {
                switch (cmd)
                {
                    case "storeGet": { string v; return store.TryGetValue(a, out v) ? v : null; }
                    case "storeSet": store[a] = b; Save(); if (a == "aa.settings") Program.LogEnabled = b == null || !b.Contains("\"logs\":false"); return "1";
                    case "info": return Info();
                    case "saveFile": return SaveFile(a, b);
                    case "share": Clipboard.SetText(a ?? ""); MessageBox.Show(form, "Đã sao chép vào bộ nhớ tạm / Copied to clipboard.", "Applied Astrology"); return "1";
                    case "openUrl": if (a != null && (a.StartsWith("https://") || a.StartsWith("http://"))) Process.Start(a); return "1";
                    case "httpGet": return HttpGet(a);
                    case "log": Program.Log("JS " + a); return "1";
                    case "testStep": return form.TestStep(a, b);
                    case "readLog": return ReadTail(Path.Combine(Program.DataDir, "app.log"), 131072);
                    case "clearLog": try { File.Delete(Path.Combine(Program.DataDir, "app.log")); File.Delete(Path.Combine(Program.DataDir, "app.log.1")); } catch { } return "1";
                    case "crashList": return CrashList();
                    case "clearCrash": try { foreach (string f in Directory.GetFiles(Program.CrashDir)) File.Delete(f); } catch { } return "1";
                    case "copy": Clipboard.SetText(string.IsNullOrEmpty(a) ? " " : a); return "1";
                    case "download": return Updater.Start(a, b) ? "1" : null;
                    case "downloadStatus": return Updater.Status(json);
                    case "installUpdate": return Updater.Install(a, form);
                    case "setBars": return "1";
                }
            }
            catch (Exception ex) { Program.Log("HostCall " + cmd + " error: " + ex.Message); }
            return null;
        }

        private void Save()
        {
            try { File.WriteAllText(storeFile, json.Serialize(store), new UTF8Encoding(false)); } catch (Exception ex) { Program.Log("store save " + ex.Message); }
        }

        private string Info()
        {
            var d = new Dictionary<string, object>();
            d["os"] = Environment.OSVersion.VersionString + (Environment.Is64BitOperatingSystem ? " 64-bit" : " 32-bit");
            d["abi"] = "OS " + Program.Arch() + " / process " + Program.ProcArch();
            d["engine"] = form.EngineName;
            d["portable"] = File.Exists(Path.Combine(Program.BaseDir, "portable.ini"));
            d["dataDir"] = Program.DataDir;
            d["install"] = Program.InstallKind();
            d["arch"] = Program.ProcArch();
            d["version"] = Program.Version;
            return json.Serialize(d);
        }

        private string SaveFile(string name, string data)
        {
            byte[] bytes;
            if (data != null && data.StartsWith("data:"))
                bytes = Convert.FromBase64String(data.Substring(data.IndexOf(',') + 1));
            else bytes = new UTF8Encoding(false).GetBytes(data ?? "");
            foreach (char c in Path.GetInvalidFileNameChars()) name = name.Replace(c, '_');
            string target;
            if (Program.SelfTest)
            {
                target = Path.Combine(Program.TestOut, "export_" + name);
            }
            else
            {
                using (var dlg = new SaveFileDialog())
                {
                    dlg.FileName = name;
                    dlg.InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
                    dlg.Filter = name.EndsWith(".png") ? "PNG (*.png)|*.png" : "JSON (*.json)|*.json";
                    if (dlg.ShowDialog(form) != DialogResult.OK) return null;
                    target = dlg.FileName;
                }
            }
            File.WriteAllBytes(target, bytes);
            Program.Log("saved " + target + " (" + bytes.Length + " bytes)");
            return target;
        }

        private string HttpGet(string url)
        {
            var sw = Stopwatch.StartNew();
            var r = new Dictionary<string, object>();
            try
            {
                var req = (HttpWebRequest)WebRequest.Create(url);
                req.Timeout = 6000; req.ReadWriteTimeout = 6000;
                req.UserAgent = "AppliedAstrology/" + Program.Version + " Windows";
                if (url.StartsWith("https://api.github.com/")) req.Accept = "application/vnd.github+json";
                using (var resp = (HttpWebResponse)req.GetResponse())
                using (var sr = new StreamReader(resp.GetResponseStream(), Encoding.UTF8))
                {
                    r["ok"] = true; r["status"] = (int)resp.StatusCode; r["body"] = sr.ReadToEnd();
                }
            }
            catch (Exception ex) { r["ok"] = false; r["status"] = 0; r["body"] = ex.Message; }
            r["ms"] = sw.ElapsedMilliseconds;
            return json.Serialize(r);
        }
    }

    /// <summary>v0.0.3: tải bản cập nhật từ GitHub Releases (chạy nền, có tiến độ) và cài đặt theo kiểu cài hiện tại.</summary>
    static class Updater
    {
        static readonly object Lock = new object();
        static string state = "idle", error = "", path = "";
        static long got, total;

        public static bool Start(string url, string name)
        {
            if (string.IsNullOrEmpty(url) || !url.StartsWith("https://")) return false;
            lock (Lock) { if (state == "running") return true; state = "running"; got = 0; total = 0; error = ""; path = ""; }
            var th = new Thread(delegate ()
            {
                try
                {
                    foreach (char c in Path.GetInvalidFileNameChars()) name = name.Replace(c, '_');
                    string dir = Path.Combine(Path.GetTempPath(), "AppliedAstrology-update");
                    Directory.CreateDirectory(dir);
                    string part = Path.Combine(dir, name + ".part"), dst = Path.Combine(dir, name);
                    var req = (HttpWebRequest)WebRequest.Create(url);
                    req.UserAgent = "AppliedAstrology/" + Program.Version + " Windows";
                    req.AllowAutoRedirect = true; req.Timeout = 30000; req.ReadWriteTimeout = 60000;
                    using (var resp = (HttpWebResponse)req.GetResponse())
                    using (var rs = resp.GetResponseStream())
                    using (var fs = new FileStream(part, FileMode.Create, FileAccess.Write))
                    {
                        total = resp.ContentLength;
                        byte[] buf = new byte[81920]; int n;
                        while ((n = rs.Read(buf, 0, buf.Length)) > 0) { fs.Write(buf, 0, n); got += n; }
                    }
                    if (total > 0 && got != total) throw new IOException("incomplete " + got + "/" + total);
                    if (File.Exists(dst)) File.Delete(dst);
                    File.Move(part, dst);
                    path = dst; state = "done";
                    Program.Log("update downloaded " + dst + " " + got);
                }
                catch (Exception ex) { error = ex.Message; state = "error"; Program.Log("update download error " + ex.Message); }
            });
            th.IsBackground = true; th.Start();
            return true;
        }

        public static string Status(JavaScriptSerializer json)
        {
            return json.Serialize(new Dictionary<string, object> { { "state", state }, { "got", got }, { "total", total }, { "path", path }, { "error", error } });
        }

        /// <summary>Setup.exe: cài im lặng vào đúng thư mục hiện tại rồi mở lại; MSI: msiexec /passive; portable: giải nén đè (giữ userdata); appx: App Installer.</summary>
        public static string Install(string file, Form form)
        {
            if (string.IsNullOrEmpty(file) || !File.Exists(file)) return null;
            string kind = Program.InstallKind(), exe = Path.Combine(Program.BaseDir, "AppliedAstrology.exe");
            string lower = file.ToLowerInvariant();
            Program.Log("update install " + file + " kind=" + kind);
            if (Program.SelfTest) return "selftest";
            string script = Path.Combine(Path.GetTempPath(), "AppliedAstrology-update", "apply_update.cmd");
            string pid = Process.GetCurrentProcess().Id.ToString();
            var sb = new StringBuilder();
            sb.AppendLine("@echo off");
            sb.AppendLine(":wait");
            sb.AppendLine("tasklist /FI \"PID eq " + pid + "\" 2>nul | find \"" + pid + "\" >nul && (ping -n 2 127.0.0.1 >nul & goto wait)");
            if (lower.EndsWith(".exe"))
                sb.AppendLine("\"" + file + "\" /S /D=\"" + Program.BaseDir.TrimEnd('\\') + "\"");
            else if (lower.EndsWith(".msi"))
                sb.AppendLine("msiexec /i \"" + file + "\" /passive /norestart");
            else if (lower.EndsWith(".zip"))
            {
                string tmp = Path.Combine(Path.GetTempPath(), "AppliedAstrology-update", "unzip");
                if (Directory.Exists(tmp)) Directory.Delete(tmp, true);
                System.IO.Compression.ZipFile.ExtractToDirectory(file, tmp);
                string[] found = Directory.GetFiles(tmp, "AppliedAstrology.exe", SearchOption.AllDirectories);
                if (found.Length == 0) return null;
                string src = Path.GetDirectoryName(found[0]);
                sb.AppendLine("robocopy \"" + src + "\" \"" + Program.BaseDir.TrimEnd('\\') + "\" /E /XD userdata /NFL /NDL /NJH /NJS >nul");
            }
            else { Process.Start(file); return "1"; }   // .appxbundle -> App Installer
            if (kind != "msi" || !lower.EndsWith(".msi")) sb.AppendLine("start \"\" \"" + exe + "\"");
            else sb.AppendLine("start \"\" \"%LOCALAPPDATA%\\Programs\\Applied Astrology\\AppliedAstrology.exe\"");
            File.WriteAllText(script, sb.ToString(), new UTF8Encoding(false));
            var psi = new ProcessStartInfo("cmd.exe", "/c \"" + script + "\"") { WindowStyle = ProcessWindowStyle.Hidden, CreateNoWindow = true, UseShellExecute = false };
            Process.Start(psi);
            form.BeginInvoke(new MethodInvoker(delegate { form.Close(); }));
            return "1";
        }
    }

    public class MainForm : Form
    {
        internal string EngineName = "";
        private Control browser;
        private readonly HostBridge bridge;
        private readonly string indexPath;
        private readonly List<string> stepLog = new List<string>();

        public MainForm()
        {
            Text = "Applied Astrology " + Program.Version + " – Tử Vi Ứng Dụng";
            Width = 1320; Height = 900;
            StartPosition = FormStartPosition.CenterScreen;
            BackColor = Color.FromArgb(8, 11, 22);
            try { Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath); } catch { }
            bridge = new HostBridge(this);
            indexPath = Path.Combine(Program.BaseDir, "web", "index.html");
            if (!File.Exists(indexPath))
            {
                MessageBox.Show("Không tìm thấy " + indexPath, "Applied Astrology", MessageBoxButtons.OK, MessageBoxIcon.Error);
                Environment.Exit(2);
            }
            Load += delegate { StartEngine(); };
        }

        private string StartUrl()
        {
            string url = new Uri(indexPath).AbsoluteUri;
            if (Program.SelfTest) url += "?selftest=1";
            return url;
        }

        private void StartEngine()
        {
            if (Program.ForceEngine != "ie" && TryWebView2()) return;
            StartIE();
        }

        // ---------- Edge WebView2 (nạp động bằng reflection để vẫn chạy được trên Windows 7) ----------
        private bool TryWebView2()
        {
            try
            {
                string core = Path.Combine(Program.BaseDir, "Microsoft.Web.WebView2.Core.dll");
                string wf = Path.Combine(Program.BaseDir, "Microsoft.Web.WebView2.WinForms.dll");
                if (!File.Exists(core) || !File.Exists(wf)) return false;
                Assembly coreAsm = Assembly.LoadFrom(core);
                Assembly wfAsm = Assembly.LoadFrom(wf);
                Type envType = coreAsm.GetType("Microsoft.Web.WebView2.Core.CoreWebView2Environment");
                string loaderDir = Path.Combine(Program.BaseDir, "runtimes", "win-" + Program.ProcArch(), "native");
                MethodInfo setLoader = envType.GetMethod("SetLoaderDllFolderPath");
                if (setLoader != null && Directory.Exists(loaderDir)) setLoader.Invoke(null, new object[] { loaderDir });
                string ver = (string)envType.GetMethod("GetAvailableBrowserVersionString", new Type[] { typeof(string) }).Invoke(null, new object[] { null });
                if (string.IsNullOrEmpty(ver)) return false;
                Type wvType = wfAsm.GetType("Microsoft.Web.WebView2.WinForms.WebView2");
                Control wv = (Control)Activator.CreateInstance(wvType);
                // Thư mục dữ liệu người dùng của WebView2
                Type cpType = wfAsm.GetType("Microsoft.Web.WebView2.WinForms.CoreWebView2CreationProperties");
                if (cpType != null)
                {
                    object cp = Activator.CreateInstance(cpType);
                    cpType.GetProperty("UserDataFolder").SetValue(cp, Path.Combine(Program.DataDir, "WebView2"), null);
                    wvType.GetProperty("CreationProperties").SetValue(wv, cp, null);
                }
                wv.Dock = DockStyle.Fill;
                Controls.Add(wv);
                browser = wv;
                EngineName = "Edge WebView2 " + ver;
                EventInfo ev = wvType.GetEvent("CoreWebView2InitializationCompleted");
                var handler = Delegate.CreateDelegate(ev.EventHandlerType, this, typeof(MainForm).GetMethod("OnWv2Ready", BindingFlags.NonPublic | BindingFlags.Instance));
                ev.AddEventHandler(wv, handler);
                MethodInfo ensure = null;
                foreach (MethodInfo mi in wvType.GetMethods())
                    if (mi.Name == "EnsureCoreWebView2Async" && mi.GetParameters().Length == 1) ensure = mi;
                ensure.Invoke(wv, new object[] { null });
                Program.Log("engine " + EngineName + " arch " + Program.ProcArch());
                return true;
            }
            catch (Exception ex)
            {
                Program.Log("WebView2 unavailable: " + (ex.InnerException ?? ex).Message);
                if (browser != null) { Controls.Remove(browser); browser = null; }
                return false;
            }
        }

        private void OnWv2Ready(object sender, EventArgs e)
        {
            try
            {
                object core = sender.GetType().GetProperty("CoreWebView2").GetValue(sender, null);
                if (core == null) { Program.Log("WebView2 init failed, falling back to IE"); Controls.Remove(browser); browser = null; StartIE(); return; }
                object settings = core.GetType().GetProperty("Settings").GetValue(core, null);
                settings.GetType().GetProperty("AreHostObjectsAllowed").SetValue(settings, true, null);
                settings.GetType().GetProperty("AreDevToolsEnabled").SetValue(settings, false, null);
                core.GetType().GetMethod("AddHostObjectToScript", new Type[] { typeof(string), typeof(object) }).Invoke(core, new object[] { "aahost", bridge });
                core.GetType().GetMethod("Navigate", new Type[] { typeof(string) }).Invoke(core, new object[] { StartUrl() });
            }
            catch (Exception ex) { Program.Log("WebView2 ready error: " + ex.Message); }
        }

        // ---------- IE11 / MSHTML (Windows 7, 8.1 hoặc khi thiếu WebView2) ----------
        private void StartIE()
        {
            try
            {
                string exe = Path.GetFileName(Application.ExecutablePath);
                using (var k = Registry.CurrentUser.CreateSubKey(@"Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_BROWSER_EMULATION"))
                    k.SetValue(exe, 11001, RegistryValueKind.DWord);
                // v0.0.3: tắt dựng hình GPU của MSHTML. Trên một số máy/driver (vd. Windows 11 build 26300 + NVIDIA),
                // WebBrowser control dựng bằng GPU chỉ hiện nền trắng dù DOM vẫn chạy; dựng hình phần mềm ổn định cho Windows 7/8.1.
                using (var k = Registry.CurrentUser.CreateSubKey(@"Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_GPU_RENDERING"))
                    k.SetValue(exe, Environment.GetEnvironmentVariable("AA_IE_GPU") == "1" ? 1 : 0, RegistryValueKind.DWord);
            }
            catch { }
            var wb = new WebBrowser();
            wb.Dock = DockStyle.Fill;
            wb.ScriptErrorsSuppressed = true;
            wb.IsWebBrowserContextMenuEnabled = false;
            wb.WebBrowserShortcutsEnabled = true;
            wb.AllowWebBrowserDrop = false;
            wb.ObjectForScripting = bridge;
            Controls.Add(wb);
            browser = wb;
            EngineName = "Internet Explorer 11 (MSHTML)";
            Program.Log("engine " + EngineName + " arch " + Program.ProcArch());
            wb.Navigate(StartUrl());
        }

        // ---------- Kiểm thử tự động: chụp cửa sổ sau từng bước ----------
        internal string TestStep(string name, string payload)
        {
            if (!Program.SelfTest) return null;
            try
            {
                Rectangle r = RectangleToScreen(ClientRectangle);
                using (var bmp = new Bitmap(Math.Max(1, r.Width), Math.Max(1, r.Height)))
                {
                    using (var g = Graphics.FromImage(bmp)) g.CopyFromScreen(r.Location, Point.Empty, r.Size);
                    bmp.Save(Path.Combine(Program.TestOut, name + ".png"), ImageFormat.Png);
                }
                File.AppendAllText(Path.Combine(Program.TestOut, "steps.jsonl"), "{\"step\":\"" + name + "\",\"data\":" + (string.IsNullOrEmpty(payload) ? "null" : payload) + "}\n", new UTF8Encoding(false));
                if (name == "99_done")
                {
                    File.WriteAllText(Path.Combine(Program.TestOut, "results.json"), payload, new UTF8Encoding(false));
                    Program.Log("SELFTEST_DONE " + payload);
                    var t = new System.Windows.Forms.Timer { Interval = 2500 };
                    t.Tick += delegate { t.Stop(); Close(); };
                    t.Start();
                }
                return "1";
            }
            catch (Exception ex) { Program.Log("testStep " + ex.Message); return null; }
        }
    }
}
