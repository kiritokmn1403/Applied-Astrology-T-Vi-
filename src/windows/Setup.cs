// Applied Astrology v0.0.3 - Bộ cài đặt .exe (một tệp cho x86 / x64 / ARM64, Windows 7 SP1+)
// Cài theo người dùng (không cần quyền Administrator) vào %LOCALAPPDATA%\Programs\Applied Astrology,
// tạo lối tắt, đăng ký "Apps & features" (HKCU) và tự làm trình gỡ cài đặt (uninstall.exe /uninstall).
// Dòng lệnh: /S (cài im lặng)  /D=<thư mục>  /uninstall  /uninstall /S
using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Windows.Forms;
using Microsoft.Win32;

[assembly: AssemblyTitle("Applied Astrology Setup")]
[assembly: AssemblyProduct("Applied Astrology (Tử Vi Ứng Dụng)")]
[assembly: AssemblyCompany("Applied Astrology")]
[assembly: AssemblyVersion("0.0.3.0")]
[assembly: AssemblyFileVersion("0.0.3.0")]

namespace AppliedAstrologySetup
{
    static class Program
    {
        internal const string AppName = "Applied Astrology";
        internal const string Version = "0.0.3";
        internal const string RegKey = @"Software\Microsoft\Windows\CurrentVersion\Uninstall\AppliedAstrology";
        internal static string DefaultDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Programs", AppName);

        [STAThread]
        static int Main(string[] args)
        {
            bool silent = false, uninstall = false; string dir = null;
            foreach (string a0 in args)
            {
                string a = a0.Trim();
                if (a.Equals("/S", StringComparison.OrdinalIgnoreCase)) silent = true;
                else if (a.Equals("/uninstall", StringComparison.OrdinalIgnoreCase)) uninstall = true;
                else if (a.StartsWith("/D=", StringComparison.OrdinalIgnoreCase)) dir = a.Substring(3).Trim('"');
            }
            Application.EnableVisualStyles();
            if (uninstall) return Uninstall(silent);
            if (silent)
            {
                try { Install(dir ?? DefaultDir, true, true, null); return 0; }
                catch (Exception ex) { File.WriteAllText(Path.Combine(Path.GetTempPath(), "AppliedAstrologySetup.log"), ex.ToString()); return 1; }
            }
            Application.Run(new SetupForm());
            return 0;
        }

        internal static void Install(string target, bool desktop, bool startMenu, Action<int, string> progress)
        {
            Directory.CreateDirectory(target);
            using (Stream s = Assembly.GetExecutingAssembly().GetManifestResourceStream("AppPayload"))
            using (var zip = new ZipArchive(s, ZipArchiveMode.Read))
            {
                int n = 0, total = zip.Entries.Count;
                foreach (ZipArchiveEntry e in zip.Entries)
                {
                    n++;
                    string dest = Path.Combine(target, e.FullName.Replace('/', Path.DirectorySeparatorChar));
                    if (string.IsNullOrEmpty(e.Name)) { Directory.CreateDirectory(dest); continue; }
                    Directory.CreateDirectory(Path.GetDirectoryName(dest));
                    e.ExtractToFile(dest, true);
                    if (progress != null && (n % 10 == 0 || n == total)) progress(5 + 85 * n / total, e.FullName);
                }
            }
            string exe = Path.Combine(target, "AppliedAstrology.exe");
            // Trình gỡ cài đặt = chính tệp setup
            File.Copy(Assembly.GetExecutingAssembly().Location, Path.Combine(target, "uninstall.exe"), true);
            if (desktop) Shortcut(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), AppName + ".lnk"), exe);
            if (startMenu)
            {
                string sm = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Programs), AppName);
                Directory.CreateDirectory(sm);
                Shortcut(Path.Combine(sm, AppName + ".lnk"), exe);
            }
            using (RegistryKey k = Registry.CurrentUser.CreateSubKey(RegKey))
            {
                k.SetValue("DisplayName", AppName + " (Tử Vi Ứng Dụng)");
                k.SetValue("DisplayVersion", Version);
                k.SetValue("Publisher", AppName);
                k.SetValue("DisplayIcon", exe);
                k.SetValue("InstallLocation", target);
                k.SetValue("UninstallString", "\"" + Path.Combine(target, "uninstall.exe") + "\" /uninstall");
                k.SetValue("QuietUninstallString", "\"" + Path.Combine(target, "uninstall.exe") + "\" /uninstall /S");
                k.SetValue("NoModify", 1, RegistryValueKind.DWord);
                k.SetValue("NoRepair", 1, RegistryValueKind.DWord);
                k.SetValue("EstimatedSize", 20000, RegistryValueKind.DWord);
            }
            if (progress != null) progress(100, "OK");
        }

        static void Shortcut(string lnk, string target)
        {
            try
            {
                Type t = Type.GetTypeFromProgID("WScript.Shell");
                object sh = Activator.CreateInstance(t);
                object sc = t.InvokeMember("CreateShortcut", BindingFlags.InvokeMethod, null, sh, new object[] { lnk });
                Type st = sc.GetType();
                st.InvokeMember("TargetPath", BindingFlags.SetProperty, null, sc, new object[] { target });
                st.InvokeMember("WorkingDirectory", BindingFlags.SetProperty, null, sc, new object[] { Path.GetDirectoryName(target) });
                st.InvokeMember("IconLocation", BindingFlags.SetProperty, null, sc, new object[] { target + ",0" });
                st.InvokeMember("Description", BindingFlags.SetProperty, null, sc, new object[] { "Applied Astrology – Tử Vi Ứng Dụng" });
                st.InvokeMember("Save", BindingFlags.InvokeMethod, null, sc, null);
            }
            catch { }
        }

        static int Uninstall(bool silent)
        {
            string dir = null;
            using (RegistryKey k = Registry.CurrentUser.OpenSubKey(RegKey))
                if (k != null) dir = k.GetValue("InstallLocation") as string;
            if (dir == null) dir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            if (!silent && MessageBox.Show("Gỡ cài đặt Applied Astrology " + Version + "?\nUninstall Applied Astrology?", AppName, MessageBoxButtons.YesNo, MessageBoxIcon.Question) != DialogResult.Yes) return 1;
            try { File.Delete(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), AppName + ".lnk")); } catch { }
            try { Directory.Delete(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Programs), AppName), true); } catch { }
            try { Registry.CurrentUser.DeleteSubKeyTree(RegKey); } catch { }
            // Xóa thư mục sau khi tiến trình này thoát
            var psi = new ProcessStartInfo("cmd.exe", "/c ping 127.0.0.1 -n 3 > nul & rmdir /s /q \"" + dir + "\"") { CreateNoWindow = true, UseShellExecute = false };
            Process.Start(psi);
            if (!silent) MessageBox.Show("Đã gỡ cài đặt. / Uninstalled.", AppName);
            return 0;
        }
    }

    class SetupForm : Form
    {
        TextBox path; CheckBox desk, start, launch; ProgressBar bar; Label status; Button go;
        public SetupForm()
        {
            Text = "Applied Astrology " + Program.Version + " – Cài đặt / Setup";
            ClientSize = new Size(560, 330); FormBorderStyle = FormBorderStyle.FixedDialog; MaximizeBox = false;
            StartPosition = FormStartPosition.CenterScreen; BackColor = Color.FromArgb(14, 20, 40); ForeColor = Color.White;
            try { Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath); } catch { }
            var banner = new Panel { Dock = DockStyle.Top, Height = 74, BackColor = Color.FromArgb(27, 36, 66) };
            banner.Controls.Add(new Label { Text = "Applied Astrology " + Program.Version, Font = new Font("Segoe UI", 16, FontStyle.Bold), ForeColor = Color.FromArgb(255, 231, 163), AutoSize = true, Location = new Point(18, 10) });
            banner.Controls.Add(new Label { Text = "Tử Vi Ứng Dụng – Bửu Đình · Windows 7/8.1/10/11 · x86 / x64 / ARM64", ForeColor = Color.FromArgb(154, 166, 196), AutoSize = true, Location = new Point(20, 46) });
            Controls.Add(banner);
            Controls.Add(new Label { Text = "Thư mục cài đặt / Install folder:", AutoSize = true, Location = new Point(20, 90) });
            path = new TextBox { Text = Program.DefaultDir, Location = new Point(20, 112), Width = 420 };
            Controls.Add(path);
            var browse = new Button { Text = "…", Location = new Point(450, 110), Width = 90, FlatStyle = FlatStyle.Flat };
            browse.Click += delegate { using (var f = new FolderBrowserDialog { SelectedPath = path.Text }) if (f.ShowDialog(this) == DialogResult.OK) path.Text = Path.Combine(f.SelectedPath, Program.AppName); };
            Controls.Add(browse);
            desk = new CheckBox { Text = "Lối tắt ngoài Desktop", Checked = true, AutoSize = true, Location = new Point(20, 146) };
            start = new CheckBox { Text = "Thêm vào Start Menu", Checked = true, AutoSize = true, Location = new Point(20, 170) };
            launch = new CheckBox { Text = "Mở ứng dụng sau khi cài", Checked = true, AutoSize = true, Location = new Point(20, 194) };
            Controls.Add(desk); Controls.Add(start); Controls.Add(launch);
            status = new Label { Text = "Sẵn sàng cài đặt (không cần quyền Administrator).", AutoSize = false, Width = 520, Location = new Point(20, 226), ForeColor = Color.FromArgb(154, 166, 196) };
            bar = new ProgressBar { Location = new Point(20, 250), Width = 520, Height = 18 };
            go = new Button { Text = "Cài đặt / Install", Location = new Point(400, 284), Width = 140, Height = 32, BackColor = Color.FromArgb(242, 184, 75), ForeColor = Color.Black, FlatStyle = FlatStyle.Flat };
            go.Click += delegate { DoInstall(); };
            Controls.Add(status); Controls.Add(bar); Controls.Add(go);
        }
        void DoInstall()
        {
            go.Enabled = false;
            try
            {
                Program.Install(path.Text.Trim(), desk.Checked, start.Checked, (p, f) => { bar.Value = Math.Min(100, p); status.Text = f; Application.DoEvents(); });
                status.Text = "Hoàn tất / Done.";
                if (launch.Checked) Process.Start(Path.Combine(path.Text.Trim(), "AppliedAstrology.exe"));
                MessageBox.Show(this, "Đã cài đặt Applied Astrology " + Program.Version + ".", Program.AppName);
                Close();
            }
            catch (Exception ex) { MessageBox.Show(this, ex.Message, Program.AppName, MessageBoxButtons.OK, MessageBoxIcon.Error); go.Enabled = true; }
        }
    }
}
