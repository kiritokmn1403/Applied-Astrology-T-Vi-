// Applied Astrology v0.0.2 - Windows host (Windows 7 SP1 trở lên; x86 / x64 / ARM64)
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
[assembly: AssemblyVersion("0.0.2.0")]
[assembly: AssemblyFileVersion("0.0.2.0")]
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
            try { SetProcessDPIAware(); } catch { }
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
            return 0;
        }

        [DllImport("user32.dll")] static extern bool SetProcessDPIAware();

        internal static void Log(string msg)
        {
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
        }

        public string HostCall(string cmd, string a, string b)
        {
            try
            {
                switch (cmd)
                {
                    case "storeGet": { string v; return store.TryGetValue(a, out v) ? v : null; }
                    case "storeSet": store[a] = b; Save(); return "1";
                    case "info": return Info();
                    case "saveFile": return SaveFile(a, b);
                    case "share": Clipboard.SetText(a ?? ""); MessageBox.Show(form, "Đã sao chép vào bộ nhớ tạm / Copied to clipboard.", "Applied Astrology"); return "1";
                    case "openUrl": if (a != null && (a.StartsWith("https://") || a.StartsWith("http://"))) Process.Start(a); return "1";
                    case "httpGet": return HttpGet(a);
                    case "log": Program.Log("JS " + a); return "1";
                    case "testStep": return form.TestStep(a, b);
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
                req.UserAgent = "AppliedAstrology/0.0.2 Windows";
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

    public class MainForm : Form
    {
        internal string EngineName = "";
        private Control browser;
        private readonly HostBridge bridge;
        private readonly string indexPath;
        private readonly List<string> stepLog = new List<string>();

        public MainForm()
        {
            Text = "Applied Astrology 0.0.2 – Tử Vi Ứng Dụng";
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
                using (var k = Registry.CurrentUser.CreateSubKey(@"Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_GPU_RENDERING"))
                    k.SetValue(exe, 1, RegistryValueKind.DWord);
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
