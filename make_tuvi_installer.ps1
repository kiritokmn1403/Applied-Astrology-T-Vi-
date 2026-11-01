$ver = "0.0.1"
$csc = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
$out = ".\output\0.0.1"
$zipPath = "$out\AppliedAstrology-$ver-windows-x64.zip"
$iconPath = ".\assets\app_icon.ico"
$installerExe = "$out\AppliedAstrology-$ver-windows-x64-Setup.exe"

Write-Output "Building Windows Installer: $installerExe"

$src = @"
using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Windows.Forms;

[assembly: AssemblyVersion("$ver.0")]
[assembly: AssemblyFileVersion("$ver.0")]
[assembly: AssemblyProduct("Applied Astrology Setup")]

namespace TuViInstaller
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new InstallerForm());
        }
    }

    public class InstallerForm : Form
    {
        private ProgressBar progressBar;
        private Label lblStatus;
        private TextBox txtPath;
        private CheckBox chkDesktop;
        private CheckBox chkStartMenu;
        private CheckBox chkLaunch;
        private Button btnInstall;
        private Button btnBrowse;

        public InstallerForm()
        {
            this.Text = "Applied Astrology $ver Setup - Cài Đặt Tử Vi Ứng Dụng";
            this.Size = new Size(540, 390);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.BackColor = Color.FromArgb(15, 10, 30);
            this.ForeColor = Color.White;

            var banner = new Panel
            {
                Dock = DockStyle.Top,
                Height = 70,
                BackColor = Color.FromArgb(35, 18, 65)
            };
            var lblTitle = new Label
            {
                Text = "Applied Astrology v$ver",
                Font = new Font("Segoe UI", 15, FontStyle.Bold),
                ForeColor = Color.FromArgb(253, 230, 138),
                Location = new Point(20, 12),
                AutoSize = true
            };
            var lblSub = new Label
            {
                Text = "Tử Vi Ứng Dụng - Bửu Đình (Bản cài đặt chính thức cho Windows)",
                Font = new Font("Segoe UI", 9),
                ForeColor = Color.FromArgb(200, 200, 230),
                Location = new Point(22, 42),
                AutoSize = true
            };
            banner.Controls.Add(lblTitle);
            banner.Controls.Add(lblSub);
            this.Controls.Add(banner);

            var lblDest = new Label
            {
                Text = "Thư mục cài đặt:",
                Location = new Point(24, 85),
                AutoSize = true,
                ForeColor = Color.FromArgb(253, 230, 138)
            };
            this.Controls.Add(lblDest);

            string defaultPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Programs", "AppliedAstrology");
            txtPath = new TextBox
            {
                Text = defaultPath,
                Location = new Point(24, 110),
                Width = 380,
                BackColor = Color.FromArgb(25, 15, 45),
                ForeColor = Color.White
            };
            this.Controls.Add(txtPath);

            btnBrowse = new Button
            {
                Text = "Chọn...",
                Location = new Point(415, 108),
                Width = 85,
                Height = 26,
                BackColor = Color.FromArgb(45, 25, 75),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat
            };
            btnBrowse.Click += (s, e) =>
            {
                using (var fbd = new FolderBrowserDialog())
                {
                    fbd.SelectedPath = txtPath.Text;
                    if (fbd.ShowDialog() == DialogResult.OK)
                    {
                        txtPath.Text = fbd.SelectedPath;
                    }
                }
            };
            this.Controls.Add(btnBrowse);

            chkDesktop = new CheckBox
            {
                Text = "Tạo lối tắt ngoài màn hình chính (Desktop Shortcut)",
                Checked = true,
                Location = new Point(24, 150),
                AutoSize = true
            };
            this.Controls.Add(chkDesktop);

            chkStartMenu = new CheckBox
            {
                Text = "Thêm vào Start Menu",
                Checked = true,
                Location = new Point(24, 175),
                AutoSize = true
            };
            this.Controls.Add(chkStartMenu);

            chkLaunch = new CheckBox
            {
                Text = "Khởi chạy Applied Astrology sau khi cài đặt xong",
                Checked = true,
                Location = new Point(24, 200),
                AutoSize = true
            };
            this.Controls.Add(chkLaunch);

            lblStatus = new Label
            {
                Text = "Sẵn sàng cài đặt.",
                Location = new Point(24, 235),
                Width = 475,
                ForeColor = Color.FromArgb(200, 200, 220)
            };
            this.Controls.Add(lblStatus);

            progressBar = new ProgressBar
            {
                Location = new Point(24, 255),
                Width = 475,
                Height = 20
            };
            this.Controls.Add(progressBar);

            btnInstall = new Button
            {
                Text = "Cài Đặt Ngay",
                Location = new Point(380, 295),
                Width = 120,
                Height = 34,
                BackColor = Color.FromArgb(245, 158, 11),
                ForeColor = Color.Black,
                Font = new Font("Segoe UI", 9, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat
            };
            btnInstall.Click += (s, e) => DoInstall();
            this.Controls.Add(btnInstall);
        }

        private void DoInstall()
        {
            btnInstall.Enabled = false;
            btnBrowse.Enabled = false;
            txtPath.Enabled = false;
            progressBar.Value = 10;
            lblStatus.Text = "Đang chuẩn bị cài đặt...";
            Application.DoEvents();

            try
            {
                string targetDir = txtPath.Text.Trim();
                if (!Directory.Exists(targetDir))
                {
                    Directory.CreateDirectory(targetDir);
                }

                var resStream = Assembly.GetExecutingAssembly().GetManifestResourceStream("AppZip");
                if (resStream != null)
                {
                    lblStatus.Text = "Đang giải nén gói ứng dụng...";
                    using (var archive = new ZipArchive(resStream))
                    {
                        int total = archive.Entries.Count;
                        int cur = 0;
                        foreach (var entry in archive.Entries)
                        {
                            cur++;
                            string cleanName = entry.FullName.Replace('/', Path.DirectorySeparatorChar);
                            string destFile = Path.Combine(targetDir, cleanName);
                            if (cleanName.EndsWith(Path.DirectorySeparatorChar.ToString()) || string.IsNullOrEmpty(entry.Name))
                            {
                                Directory.CreateDirectory(destFile);
                            }
                            else
                            {
                                string parent = Path.GetDirectoryName(destFile);
                                if (!string.IsNullOrEmpty(parent)) Directory.CreateDirectory(parent);
                                entry.ExtractToFile(destFile, true);
                            }
                            if (cur % 5 == 0 || cur == total)
                            {
                                progressBar.Value = 25 + (int)((50.0 * cur) / total);
                                Application.DoEvents();
                            }
                        }
                    }
                }

                progressBar.Value = 85;
                lblStatus.Text = "Đang tạo lối tắt...";

                string exePath = Path.Combine(targetDir, "AppliedAstrology.exe");

                if (chkDesktop.Checked)
                {
                    CreateShortcut(
                        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), "Applied Astrology.lnk"),
                        exePath,
                        "Applied Astrology - Tử Vi Ứng Dụng"
                    );
                }

                if (chkStartMenu.Checked)
                {
                    string startDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Programs), "Applied Astrology");
                    if (!Directory.Exists(startDir)) Directory.CreateDirectory(startDir);
                    CreateShortcut(
                        Path.Combine(startDir, "Applied Astrology.lnk"),
                        exePath,
                        "Applied Astrology - Tử Vi Ứng Dụng"
                    );
                }

                progressBar.Value = 100;
                lblStatus.Text = "Cài đặt hoàn tất thành công!";

                if (chkLaunch.Checked && File.Exists(exePath))
                {
                    Process.Start(new ProcessStartInfo
                    {
                        FileName = exePath,
                        WorkingDirectory = Path.GetDirectoryName(exePath)
                    });
                }

                MessageBox.Show("Cài đặt Applied Astrology v$ver thành công!", "Thông báo", MessageBoxButtons.OK, MessageBoxIcon.Information);
                this.Close();
            }
            catch (Exception ex)
            {
                lblStatus.Text = "Lỗi trong quá trình cài đặt!";
                MessageBox.Show("Lỗi: " + ex.Message, "Lỗi cài đặt", MessageBoxButtons.OK, MessageBoxIcon.Error);
                btnInstall.Enabled = true;
                btnBrowse.Enabled = true;
                txtPath.Enabled = true;
            }
        }

        private static void CreateShortcut(string shortcutPath, string targetPath, string description)
        {
            try
            {
                Type shellType = Type.GetTypeFromProgID("WScript.Shell");
                if (shellType == null) return;
                dynamic shell = Activator.CreateInstance(shellType);
                dynamic shortcut = shell.CreateShortcut(shortcutPath);
                shortcut.TargetPath = targetPath;
                shortcut.WorkingDirectory = Path.GetDirectoryName(targetPath);
                shortcut.Description = description;
                shortcut.Save();
            }
            catch { }
        }
    }
}
"@

$tmpCs = "$env:TEMP\TuViInstaller.cs"
Set-Content $tmpCs $src -Encoding UTF8

$resArg = "/resource:`"$zipPath`",AppZip"
$iconArg = if (Test-Path $iconPath) { "/win32icon:`"$iconPath`"" } else { "" }

& $csc /target:winexe /optimize+ /out:"$installerExe" $iconArg $resArg "/r:System.IO.Compression.dll" "/r:System.IO.Compression.FileSystem.dll" $tmpCs

if (Test-Path $installerExe) {
    Write-Output "Installer built successfully: $installerExe ($([math]::Round((Get-Item $installerExe).Length / 1MB, 2)) MB)"
} else {
    throw "Failed to build installer executable!"
}
