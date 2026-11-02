# =====================================================================================
#  Applied Astrology v0.0.2 - Build, test and release script (Windows PowerShell 5.1+)
#  - Android: arm64-v8a / armeabi-v7a / x86_64 APK (aapt2 + javac + d8 + NDK clang + apksigner)
#  - Windows: appxbundle (x86/x64/arm64), msi (x86/x64/arm64), Setup.exe, portable zip
#  - Tests: Android emulator + USB devices (adb), Windows (WebView2 + IE11 engines), screenshots + videos
#  - Publish: git commit/push + GitHub Release with all installers, then verify uploaded assets
#  Usage:  BUILD_v0.0.2.bat   (or)   powershell -ExecutionPolicy Bypass -File build_release_0.0.2.ps1 [-Phase all|build|test|publish]
#  All messages are ASCII on purpose (PowerShell 5.1 + Unicode paths). Log: build_logs\
# =====================================================================================
param(
    [string]$Phase = "all",
    [switch]$SkipEmulator,
    [switch]$Api23,
    [switch]$NoPublish
)
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$Ver = "0.0.2"
$Repo = Split-Path -Parent $MyInvocation.MyCommand.Path
$Stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$LogDir = Join-Path $Repo "build_logs"
New-Item -ItemType Directory -Force $LogDir | Out-Null
$Log = Join-Path $LogDir "build_${Ver}_$Stamp.log"
$StatusFile = Join-Path $LogDir "status.json"
$Work = "D:\yh\aa002"                       # ASCII staging folder (Android/NDK tools dislike Unicode paths)
$Out = Join-Path $Repo "output\$Ver"
$Reports = Join-Path $Repo "test_reports\$Ver"
$Keys = "D:\yh\aa002_keys"                  # signing keys (never committed)
New-Item -ItemType Directory -Force $Work, $Out, $Reports, $Keys | Out-Null
$Utf8 = New-Object System.Text.UTF8Encoding($false)
$Summary = [ordered]@{ version = $Ver; started = (Get-Date).ToString("s"); steps = @() }

function Say([string]$m, [string]$lvl = "INFO") {
    $line = "{0} [{1}] {2}" -f (Get-Date -Format "HH:mm:ss"), $lvl, $m
    Write-Host $line -ForegroundColor $(if ($lvl -eq "ERR") { "Red" } elseif ($lvl -eq "OK") { "Green" } elseif ($lvl -eq "STEP") { "Cyan" } else { "Gray" })
    Add-Content -Path $Log -Value $line -Encoding UTF8
}
function Status([string]$step, [string]$state, [string]$detail = "") {
    $Summary.steps += [ordered]@{ step = $step; state = $state; detail = $detail; time = (Get-Date).ToString("s") }
    [IO.File]::WriteAllText($StatusFile, ($Summary | ConvertTo-Json -Depth 6), $Utf8)
    Say ("{0}: {1} {2}" -f $step, $state, $detail) $(if ($state -eq "FAIL") { "ERR" } elseif ($state -eq "OK") { "OK" } else { "STEP" })
}
function Mask([string[]]$a) {
    $r = @(); $hide = $false
    foreach ($x in $a) { if ($hide) { $r += "***"; $hide = $false; continue }; if ($x -in @("-storepass", "-keypass", "/p")) { $hide = $true }; $r += ($x -replace 'pass:.*', 'pass:***') }
    return ,$r
}
function Run([string]$exe, [string[]]$argv, [string]$what, [switch]$IgnoreExit) {
    Say ("> {0} {1}" -f (Split-Path -Leaf $exe), ((Mask $argv) -join " "))
    $eap = $ErrorActionPreference; $ErrorActionPreference = "Continue"
    $out = & $exe @argv 2>&1 | ForEach-Object { "$_" }
    $code = $LASTEXITCODE
    $ErrorActionPreference = $eap
    foreach ($l in $out) { Add-Content -Path $Log -Value ("    " + $l) -Encoding UTF8 }
    if ($code -ne 0 -and -not $IgnoreExit) { throw "$what failed (exit $code): $(($out | Select-Object -Last 8) -join ' | ')" }
    return ,$out
}
function Step([string]$name, [scriptblock]$body) {
    Status $name "RUN"
    try { & $body; Status $name "OK" }
    catch { Status $name "FAIL" $_.Exception.Message; $script:Failed += $name }
}
$script:Failed = @()
function Find-First([string[]]$cands) { foreach ($c in $cands) { if ($c -and (Test-Path $c)) { return (Resolve-Path $c).Path } } return $null }
function Sha([string]$f) { (Get-FileHash -Algorithm SHA256 $f).Hash.ToLower() }

Say "=== Applied Astrology $Ver build started. Repo: $Repo ==="

# -------------------------------------------------------------------------------------
# 0. Tool discovery
# -------------------------------------------------------------------------------------
$T = @{}
Step "00_probe_tools" {
    $T.java = Find-First @("D:\yh\tools\jdk\bin\java.exe", "$env:JAVA_HOME\bin\java.exe", "C:\Program Files\Android\Android Studio\jbr\bin\java.exe", "D:\Programs\ads\jbr\bin\java.exe")
    $T.jdk = Split-Path (Split-Path $T.java)
    $T.sdk = Find-First @("D:\yh\tools\android-sdk", "$env:LOCALAPPDATA\Android\Sdk")
    $T.sdkEmu = Find-First @("$env:LOCALAPPDATA\Android\Sdk", "D:\yh\tools\android-sdk")
    $bt = Get-ChildItem "$($T.sdk)\build-tools" -Directory | Sort-Object { [version]($_.Name -replace '[^0-9.].*$', '') } -Descending | Select-Object -First 1
    $T.bt = $bt.FullName
    $plat = Find-First @("$($T.sdk)\platforms\android-35\android.jar", "$($T.sdk)\platforms\android-36\android.jar", "$($T.sdk)\platforms\android-37.0\android.jar")
    $T.androidJar = $plat
    $ndk = Get-ChildItem "$($T.sdk)\ndk" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
    if (-not $ndk) { $ndk = Get-ChildItem "$($T.sdkEmu)\ndk" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1 }
    $T.ndk = $ndk.FullName
    $T.clang = "$($T.ndk)\toolchains\llvm\prebuilt\windows-x86_64\bin\clang.exe"
    $T.strip = "$($T.ndk)\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-strip.exe"
    $T.adb = Find-First @("$($T.sdk)\platform-tools\adb.exe", "$($T.sdkEmu)\platform-tools\adb.exe")
    $T.emulator = Find-First @("$($T.sdkEmu)\emulator\emulator.exe")
    $T.avdmanager = Find-First @("$($T.sdkEmu)\cmdline-tools\latest\bin\avdmanager.bat", "$($T.sdk)\cmdline-tools\latest\bin\avdmanager.bat")
    $T.sdkmanager = Find-First @("$($T.sdkEmu)\cmdline-tools\latest\bin\sdkmanager.bat", "$($T.sdk)\cmdline-tools\latest\bin\sdkmanager.bat")
    $T.ffmpeg = (Get-ChildItem "D:\yh\tools" -Recurse -Filter ffmpeg.exe -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
    $T.git = (Get-Command git.exe -ErrorAction SilentlyContinue).Source
    if (-not $T.git) { $T.git = Find-First @("D:\yh\tools\git\cmd\git.exe", "C:\Program Files\Git\cmd\git.exe") }
    $T.node = (Get-Command node.exe -ErrorAction SilentlyContinue).Source
    $T.nuget = Find-First @("D:\yh\tools\nuget\nuget.exe")
    # C# compilers: Roslyn (VS 2022 - supports /platform:arm64) then .NET Framework csc
    $vswhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"
    if (Test-Path $vswhere) {
        $vs = & $vswhere -latest -products * -property installationPath 2>$null
        if ($vs) { $T.roslyn = Find-First @("$vs\MSBuild\Current\Bin\Roslyn\csc.exe") }
    }
    $T.csc4 = Find-First @("$env:WINDIR\Microsoft.NET\Framework64\v4.0.30319\csc.exe", "$env:WINDIR\Microsoft.NET\Framework\v4.0.30319\csc.exe")
    $T.csc = if ($T.roslyn) { $T.roslyn } else { $T.csc4 }
    $T.netref = Find-First @("${env:ProgramFiles(x86)}\Reference Assemblies\Microsoft\Framework\.NETFramework\v4.8", "${env:ProgramFiles(x86)}\Reference Assemblies\Microsoft\Framework\.NETFramework\v4.7.2", "${env:ProgramFiles(x86)}\Reference Assemblies\Microsoft\Framework\.NETFramework\v4.6.2", "$env:WINDIR\Microsoft.NET\Framework64\v4.0.30319")
    $kit = Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin" -Directory -ErrorAction SilentlyContinue | Where-Object { Test-Path "$($_.FullName)\x64\makeappx.exe" } | Sort-Object Name -Descending | Select-Object -First 1
    $T.makeappx = "$($kit.FullName)\x64\makeappx.exe"
    $T.signtool = "$($kit.FullName)\x64\signtool.exe"
    foreach ($k in ($T.Keys | Sort-Object)) { Say ("  {0,-11} = {1}" -f $k, $T[$k]) }
    foreach ($need in @("java", "sdk", "bt", "androidJar", "clang", "adb", "csc", "makeappx")) { if (-not $T[$need] -or -not (Test-Path $T[$need])) { throw "Missing tool: $need" } }
    $env:JAVA_HOME = $T.jdk
    $env:PATH = "$($T.jdk)\bin;$($T.bt);" + $env:PATH
    # Clear proxies that broke the emulator network in earlier projects
    foreach ($p in "HTTP_PROXY", "HTTPS_PROXY", "ALL_PROXY", "http_proxy", "https_proxy", "all_proxy") { Remove-Item "env:$p" -ErrorAction SilentlyContinue }
}

# -------------------------------------------------------------------------------------
# 1. Stage sources + unit tests
# -------------------------------------------------------------------------------------
$Stage = Join-Path $Work "stage"
Step "01_stage_and_unit_tests" {
    if (Test-Path $Stage) { Remove-Item $Stage -Recurse -Force }
    New-Item -ItemType Directory -Force $Stage | Out-Null
    Copy-Item (Join-Path $Repo "src") $Stage -Recurse
    Copy-Item (Join-Path $Repo "tools") $Stage -Recurse
    Copy-Item (Join-Path $Repo "assets") $Stage -Recurse
    Remove-Item "$Stage\src\web\qa_selftest.html" -ErrorAction SilentlyContinue
    Get-ChildItem "$Stage\src" -Recurse -Force | Where-Object { $_.Attributes -match "ReparsePoint" } | ForEach-Object { Say "  removing link $($_.FullName)"; Remove-Item $_.FullName -Force -Recurse }
    if ($T.node) {
        $r = Run $T.node @("$Stage\tools\tests\engine_test.js") "engine_test"
        Say ("  " + ($r | Select-Object -Last 2) -join " ")
        $r = Run $T.node @("$Stage\tools\tests\i18n_test.js") "i18n_test"
        Say ("  " + ($r | Select-Object -Last 1))
    } else { Say "node.exe not found - unit tests skipped" "ERR" }
}

# -------------------------------------------------------------------------------------
# 2. Android split APKs
# -------------------------------------------------------------------------------------
$Abis = [ordered]@{ "armeabi-v7a" = @{ code = 2001; target = "armv7a-linux-androideabi23"; extra = @("-mthumb") };
                    "x86_64"      = @{ code = 2002; target = "x86_64-linux-android23"; extra = @() };
                    "arm64-v8a"   = @{ code = 2003; target = "aarch64-linux-android23"; extra = @() } }
$Ks = Join-Path $Keys "appliedastrology-release.jks"
$KsPassFile = Join-Path $Keys "keystore-password.txt"
if ($Phase -in @("all", "build")) {
    Step "02_android_keystore" {
        if (-not (Test-Path $Ks)) {
            $pw = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 24 | ForEach-Object { [char]$_ })
            [IO.File]::WriteAllText($KsPassFile, $pw, $Utf8)
            Run "$($T.jdk)\bin\keytool.exe" @("-genkeypair", "-v", "-keystore", $Ks, "-alias", "appliedastrology", "-keyalg", "RSA", "-keysize", "3072", "-validity", "10000",
                "-storepass", $pw, "-keypass", $pw, "-dname", "CN=Applied Astrology, O=Applied Astrology, C=VN") "keytool" | Out-Null
        }
    }
    Step "03_android_build" {
        $A = Join-Path $Work "android"; if (Test-Path $A) { Remove-Item $A -Recurse -Force }
        New-Item -ItemType Directory -Force "$A\classes", "$A\dex", "$A\compiled", "$A\assets", "$A\lib" | Out-Null
        $src = "$Stage\src\android"
        # assets/web
        Copy-Item "$Stage\src\web" "$A\assets\web" -Recurse
        # resources
        Run "$($T.bt)\aapt2.exe" @("compile", "--dir", "$src\res", "-o", "$A\compiled\res.zip") "aapt2 compile" | Out-Null
        # java -> dex
        $javas = Get-ChildItem "$src\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
        Run "$($T.jdk)\bin\javac.exe" (@("-encoding", "UTF-8", "-source", "8", "-target", "8", "-Xlint:-options", "-bootclasspath", $T.androidJar, "-d", "$A\classes") + $javas) "javac" | Out-Null
        $classes = Get-ChildItem "$A\classes" -Recurse -Filter *.class | ForEach-Object { $_.FullName }
        Run "$($T.bt)\d8.bat" (@("--release", "--min-api", "23", "--lib", $T.androidJar, "--output", "$A\dex") + $classes) "d8" | Out-Null
        $pw = (Get-Content $KsPassFile -Raw).Trim()
        Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
        foreach ($abi in $Abis.Keys) {
            $cfg = $Abis[$abi]
            New-Item -ItemType Directory -Force "$A\lib\$abi" | Out-Null
            $so = "$A\lib\$abi\libaacore.so"
            Run $T.clang (@("--target=$($cfg.target)") + $cfg.extra + @("-shared", "-fPIC", "-O2", "-fvisibility=default", "-I$src\jni", "-o", $so, "$src\jni\aacore.c", "-landroid", "-llog", "-Wl,-z,max-page-size=16384", "-Wl,--build-id=none")) "clang $abi" | Out-Null
            Run $T.strip @("--strip-unneeded", $so) "strip $abi" | Out-Null
            $man = (Get-Content "$src\AndroidManifest.xml" -Raw -Encoding UTF8).Replace("@VERSION_CODE@", "$($cfg.code)")
            [IO.File]::WriteAllText("$A\AndroidManifest_$abi.xml", $man, $Utf8)
            $raw = "$A\app-$abi-unsigned.apk"
            Run "$($T.bt)\aapt2.exe" @("link", "-o", $raw, "-I", $T.androidJar, "--manifest", "$A\AndroidManifest_$abi.xml", "--min-sdk-version", "23", "--target-sdk-version", "35", "--version-code", "$($cfg.code)", "--version-name", $Ver, "$A\compiled\res.zip", "--auto-add-overlay") "aapt2 link $abi" | Out-Null
            $zip = [IO.Compression.ZipFile]::Open($raw, [IO.Compression.ZipArchiveMode]::Update)
            [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, "$A\dex\classes.dex", "classes.dex") | Out-Null
            [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $so, "lib/$abi/libaacore.so") | Out-Null
            # assets added here with forward slashes (aapt2 -A on Windows writes "assets/web\css\..." entries that Android cannot open)
            $assetRoot = "$A\assets"
            Get-ChildItem $assetRoot -Recurse -File | ForEach-Object {
                $rel = $_.FullName.Substring($assetRoot.Length + 1).Replace('\', '/')
                [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, "assets/$rel") | Out-Null
            }
            $zip.Dispose()
            $aligned = "$A\app-$abi-aligned.apk"
            Run "$($T.bt)\zipalign.exe" @("-f", "-P", "16", "4", $raw, $aligned) "zipalign $abi" -IgnoreExit | Out-Null
            if (-not (Test-Path $aligned)) { Run "$($T.bt)\zipalign.exe" @("-f", "4", $raw, $aligned) "zipalign $abi" | Out-Null }
            $signed = "$A\AppliedAstrology-$Ver-$abi.apk"   # ASCII path: apksigner.bat cannot handle the Unicode repo path
            $final = Join-Path $Out "AppliedAstrology-$Ver-$abi.apk"
            Run "$($T.bt)\apksigner.bat" @("sign", "--ks", $Ks, "--ks-key-alias", "appliedastrology", "--ks-pass", "pass:$pw", "--key-pass", "pass:$pw", "--v4-signing-enabled", "false", "--out", $signed, $aligned) "apksigner $abi" | Out-Null
            Run "$($T.bt)\apksigner.bat" @("verify", "--min-sdk-version", "23", $signed) "verify $abi" | Out-Null
            $badge = Run "$($T.bt)\aapt2.exe" @("dump", "badging", $signed) "badging" -IgnoreExit
            Copy-Item $signed $final -Force
            Say ("  {0}: {1:N2} MB  {2}" -f $abi, ((Get-Item $final).Length / 1MB), ($badge | Select-Object -First 1))
        }
    }
}

# -------------------------------------------------------------------------------------
# 3. Windows builds
# -------------------------------------------------------------------------------------
$W = Join-Path $Work "windows"
$WArchs = @("x86", "x64", "arm64")
if ($Phase -in @("all", "build")) {
    Step "04_windows_webview2_sdk" {
        New-Item -ItemType Directory -Force "$W\nuget" | Out-Null
        $pkg = Get-ChildItem "$W\nuget" -Directory -Filter "Microsoft.Web.WebView2*" -ErrorAction SilentlyContinue | Select-Object -First 1
        if (-not $pkg -and $T.nuget) {
            Run $T.nuget @("install", "Microsoft.Web.WebView2", "-Version", "1.0.2903.40", "-OutputDirectory", "$W\nuget", "-NonInteractive") "nuget WebView2" -IgnoreExit | Out-Null
            $pkg = Get-ChildItem "$W\nuget" -Directory -Filter "Microsoft.Web.WebView2*" -ErrorAction SilentlyContinue | Select-Object -First 1
        }
        if (-not $pkg) {
            Invoke-WebRequest "https://www.nuget.org/api/v2/package/Microsoft.Web.WebView2/1.0.2903.40" -OutFile "$W\nuget\wv2.zip" -UseBasicParsing
            Expand-Archive "$W\nuget\wv2.zip" "$W\nuget\Microsoft.Web.WebView2.1.0.2903.40" -Force
            $pkg = Get-Item "$W\nuget\Microsoft.Web.WebView2.1.0.2903.40"
        }
        $script:WV2 = $pkg.FullName
        Say "  WebView2 SDK: $script:WV2"
    }
    Step "05_windows_compile" {
        New-Item -ItemType Directory -Force "$W\bin" | Out-Null
        $src = "$Stage\src\windows"
        $refs = @("/r:System.dll", "/r:System.Drawing.dll", "/r:System.Windows.Forms.dll", "/r:System.Web.Extensions.dll", "/r:Microsoft.CSharp.dll")
        if ($T.roslyn -and $T.netref -and (Test-Path "$($T.netref)\mscorlib.dll")) { $refs = @("/nostdlib+", "/noconfig", "/r:$($T.netref)\mscorlib.dll") + ($refs | ForEach-Object { $_ -replace '/r:', "/r:$($T.netref)\" }) }
        $plats = @("anycpu") + $WArchs
        foreach ($p in $plats) {
            $platArg = if ($p -eq "anycpu") { "anycpu" } else { $p }
            if ($p -eq "arm64" -and -not $T.roslyn) { Say "  Roslyn csc not found - arm64 build uses AnyCPU (runs natively on ARM64 via .NET 4.8.1)" "ERR"; Copy-Item "$W\bin\AppliedAstrology-anycpu.exe" "$W\bin\AppliedAstrology-arm64.exe"; continue }
            Run $T.csc (@("/nologo", "/target:winexe", "/optimize+", "/platform:$platArg", "/codepage:65001", "/win32icon:$Stage\assets\app_icon.ico", "/win32manifest:$src\app.manifest", "/out:$W\bin\AppliedAstrology-$p.exe") + $refs + @("$src\AppHost.cs")) "csc AppHost $p" | Out-Null
        }
    }
    Step "06_windows_layouts" {
        $lib = Get-ChildItem "$script:WV2\lib" -Directory | Where-Object { $_.Name -like "net4*" } | Sort-Object Name | Select-Object -First 1
        foreach ($p in @("universal") + $WArchs) {
            $L = "$W\layout-$p"; if (Test-Path $L) { Remove-Item $L -Recurse -Force }
            New-Item -ItemType Directory -Force $L | Out-Null
            $exeSrc = if ($p -eq "universal") { "$W\bin\AppliedAstrology-anycpu.exe" } else { "$W\bin\AppliedAstrology-$p.exe" }
            Copy-Item $exeSrc "$L\AppliedAstrology.exe"
            Copy-Item "$Stage\src\windows\App.config" "$L\AppliedAstrology.exe.config"
            Copy-Item "$Stage\src\web" "$L\web" -Recurse
            Copy-Item "$($lib.FullName)\Microsoft.Web.WebView2.Core.dll", "$($lib.FullName)\Microsoft.Web.WebView2.WinForms.dll" $L
            $archs = if ($p -eq "universal") { $WArchs } else { @($p) }
            foreach ($a in $archs) {
                New-Item -ItemType Directory -Force "$L\runtimes\win-$a\native" | Out-Null
                Copy-Item "$script:WV2\runtimes\win-$a\native\WebView2Loader.dll" "$L\runtimes\win-$a\native\"
            }
            Copy-Item "$Stage\assets\app_icon.ico" $L
            [IO.File]::WriteAllText("$L\README.txt", "Applied Astrology $Ver (Tu Vi Ung Dung - Buu Dinh)`r`nRun AppliedAstrology.exe. Requires .NET Framework 4.5+ (Windows 7 SP1+). Uses Edge WebView2 when installed, otherwise IE11.`r`n", $Utf8)
        }
    }
    Step "07_windows_portable_zip" {
        $P = "$W\portable\AppliedAstrology-$Ver-portable"
        if (Test-Path "$W\portable") { Remove-Item "$W\portable" -Recurse -Force }
        New-Item -ItemType Directory -Force $P | Out-Null
        Copy-Item "$W\layout-universal\*" $P -Recurse
        [IO.File]::WriteAllText("$P\portable.ini", "[portable]`r`ndata=userdata`r`n", $Utf8)
        $zip = Join-Path $Out "AppliedAstrology-$Ver-windows-portable.zip"
        if (Test-Path $zip) { Remove-Item $zip }
        $script:PortableZip = $zip
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        try { [IO.Compression.ZipFile]::CreateFromDirectory("$W\portable", $zip, [IO.Compression.CompressionLevel]::Optimal, $false) }
        catch { Remove-Item $zip -ErrorAction SilentlyContinue; throw }
    }
    Step "08_windows_setup_exe" {
        $payload = "$W\payload.zip"; if (Test-Path $payload) { Remove-Item $payload }
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        [IO.Compression.ZipFile]::CreateFromDirectory("$W\layout-universal", $payload, [IO.Compression.CompressionLevel]::Optimal, $false)
        $exe = Join-Path $Out "AppliedAstrology-$Ver-windows-Setup.exe"
        $refs = @("/r:System.dll", "/r:System.Drawing.dll", "/r:System.Windows.Forms.dll", "/r:System.IO.Compression.dll", "/r:System.IO.Compression.FileSystem.dll")
        if ($T.roslyn -and $T.netref -and (Test-Path "$($T.netref)\mscorlib.dll")) { $refs = @("/nostdlib+", "/noconfig", "/r:$($T.netref)\mscorlib.dll") + ($refs | ForEach-Object { $_ -replace '/r:', "/r:$($T.netref)\" }) }
        Run $T.csc (@("/nologo", "/target:winexe", "/optimize+", "/platform:anycpu", "/codepage:65001", "/win32icon:$Stage\assets\app_icon.ico", "/win32manifest:$Stage\src\windows\app.manifest", "/resource:$payload,AppPayload", "/out:$exe") + $refs + @("$Stage\src\windows\Setup.cs")) "csc Setup" | Out-Null
    }
    Step "09_windows_codesign_cert" {
        $pfx = Join-Path $Keys "AppliedAstrology-codesign.pfx"; $cer = Join-Path $Out "AppliedAstrology-$Ver-signing-certificate.cer"
        $script:CertPass = "aa002-" + $env:COMPUTERNAME
        if (-not (Test-Path $pfx)) {
            $c = New-SelfSignedCertificate -Type CodeSigningCert -Subject "CN=Applied Astrology" -KeyUsage DigitalSignature -FriendlyName "Applied Astrology code signing" -CertStoreLocation "Cert:\CurrentUser\My" -NotAfter (Get-Date).AddYears(10) -TextExtension @("2.5.29.19={text}")
            Export-PfxCertificate -Cert $c -FilePath $pfx -Password (ConvertTo-SecureString $script:CertPass -AsPlainText -Force) | Out-Null
        }
        $x = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2($pfx, $script:CertPass)
        [IO.File]::WriteAllBytes($cer, $x.Export([System.Security.Cryptography.X509Certificates.X509ContentType]::Cert))
        $script:Pfx = $pfx
    }
    Step "10_windows_appxbundle" {
        $B = "$W\appx"; if (Test-Path $B) { Remove-Item $B -Recurse -Force }
        New-Item -ItemType Directory -Force "$B\bundle" | Out-Null
        foreach ($a in $WArchs) {
            $L = "$B\layout-$a"
            Copy-Item "$W\layout-$a" $L -Recurse
            New-Item -ItemType Directory -Force "$L\Assets" | Out-Null
            Copy-Item "$Stage\src\windows\appx_assets\*" "$L\Assets\"
            $m = (Get-Content "$Stage\src\windows\AppxManifest.template.xml" -Raw -Encoding UTF8).Replace("@ARCH@", $a).Replace("@PUBLISHER@", "CN=Applied Astrology")
            [IO.File]::WriteAllText("$L\AppxManifest.xml", $m, $Utf8)
            Run $T.makeappx @("pack", "/o", "/d", $L, "/p", "$B\bundle\AppliedAstrology_$($Ver)_$a.appx") "makeappx pack $a" | Out-Null
        }
        $bundle = Join-Path $Out "AppliedAstrology-$Ver-windows.appxbundle"
        Run $T.makeappx @("bundle", "/o", "/bv", "$Ver.0", "/d", "$B\bundle", "/p", $bundle) "makeappx bundle" | Out-Null
        Run $T.signtool @("sign", "/fd", "SHA256", "/f", $script:Pfx, "/p", $script:CertPass, $bundle) "signtool appxbundle" | Out-Null
    }
    Step "11_windows_msi" {
        $wixDir = "D:\yh\tools\wix314"
        if (-not (Test-Path "$wixDir\candle.exe")) {
            New-Item -ItemType Directory -Force $wixDir | Out-Null
            Invoke-WebRequest "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip" -OutFile "$wixDir\wix.zip" -UseBasicParsing
            Expand-Archive "$wixDir\wix.zip" $wixDir -Force
        }
        $upgrade = @{ x86 = "{6E1C2A51-8A7B-4E0B-9C2D-3F4A5B6C7D01}"; x64 = "{6E1C2A51-8A7B-4E0B-9C2D-3F4A5B6C7D02}"; arm64 = "{6E1C2A51-8A7B-4E0B-9C2D-3F4A5B6C7D03}" }
        $md5 = [Security.Cryptography.MD5]::Create()
        function Id([string]$prefix, [string]$s) { $prefix + (($md5.ComputeHash([Text.Encoding]::UTF8.GetBytes($s)) | ForEach-Object { $_.ToString("x2") }) -join "").Substring(0, 24) }
        foreach ($a in $WArchs) {
            $L = "$W\layout-$a"
            $sb = New-Object System.Text.StringBuilder
            $comps = New-Object System.Collections.ArrayList
            function EmitDir([string]$path, [string]$dirId) {
                $files = Get-ChildItem $path -File
                $cid = Id "c" ("dir:" + $path)
                [void]$comps.Add($cid)
                [void]$sb.AppendLine("<Component Id=""$cid"" Guid=""*""><RegistryValue Root=""HKCU"" Key=""Software\AppliedAstrology\Msi\$cid"" Name=""installed"" Type=""integer"" Value=""1"" KeyPath=""yes""/><RemoveFolder Id=""rm$cid"" On=""uninstall""/></Component>")
                foreach ($f in $files) {
                    $fc = Id "c" ("file:" + $f.FullName)
                    [void]$comps.Add($fc)
                    [void]$sb.AppendLine("<Component Id=""$fc"" Guid=""*""><File Id=""$(Id 'f' $f.FullName)"" Source=""$($f.FullName)"" Name=""$($f.Name)"" KeyPath=""yes""/></Component>")
                }
                foreach ($d in (Get-ChildItem $path -Directory)) {
                    $did = Id "d" $d.FullName
                    [void]$sb.AppendLine("<Directory Id=""$did"" Name=""$($d.Name)"">")
                    EmitDir $d.FullName $did
                    [void]$sb.AppendLine("</Directory>")
                }
            }
            EmitDir $L "INSTALLDIR"
            $refs = ($comps | ForEach-Object { "<ComponentRef Id=""$_""/>" }) -join ""
            $plat = $a
            $wxs = @"
<?xml version="1.0" encoding="utf-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
  <Product Id="*" Name="Applied Astrology" Language="1033" Version="$Ver.0" Manufacturer="Applied Astrology" UpgradeCode="$($upgrade[$a])">
    <Package InstallerVersion="500" Compressed="yes" InstallScope="perUser" InstallPrivileges="limited" Platform="$plat" Description="Applied Astrology $Ver (Tu Vi Ung Dung - Buu Dinh) $a" />
    <MajorUpgrade DowngradeErrorMessage="A newer version of Applied Astrology is already installed." />
    <MediaTemplate EmbedCab="yes" CompressionLevel="high" />
    <Icon Id="AppIcon" SourceFile="$L\app_icon.ico" />
    <Property Id="ARPPRODUCTICON" Value="AppIcon" />
    <Property Id="ALLUSERS" Secure="yes" />
    <Directory Id="TARGETDIR" Name="SourceDir">
      <Directory Id="LocalAppDataFolder"><Directory Id="ProgramsDir" Name="Programs"><Directory Id="INSTALLDIR" Name="Applied Astrology">
$($sb.ToString())
      </Directory></Directory></Directory>
      <Directory Id="ProgramMenuFolder" />
      <Directory Id="DesktopFolder" />
    </Directory>
    <DirectoryRef Id="ProgramMenuFolder"><Component Id="cShortcuts" Guid="*">
      <Shortcut Id="smShortcut" Name="Applied Astrology" Target="[INSTALLDIR]AppliedAstrology.exe" WorkingDirectory="INSTALLDIR" Icon="AppIcon" />
      <Shortcut Id="dtShortcut" Directory="DesktopFolder" Name="Applied Astrology" Target="[INSTALLDIR]AppliedAstrology.exe" WorkingDirectory="INSTALLDIR" Icon="AppIcon" />
      <RegistryValue Root="HKCU" Key="Software\AppliedAstrology\Msi" Name="shortcuts" Type="integer" Value="1" KeyPath="yes" />
    </Component></DirectoryRef>
    <Feature Id="Main" Title="Applied Astrology" Level="1">$refs<ComponentRef Id="cShortcuts" /></Feature>
  </Product>
</Wix>
"@
            $wxsFile = "$W\aa_$a.wxs"
            [IO.File]::WriteAllText($wxsFile, $wxs, $Utf8)
            Run "$wixDir\candle.exe" @("-nologo", "-arch", $a, "-out", "$W\aa_$a.wixobj", $wxsFile) "candle $a" | Out-Null
            $msi = Join-Path $Out "AppliedAstrology-$Ver-windows-$a.msi"
            Run "$wixDir\light.exe" @("-nologo", "-sval", "-spdb", "-out", $msi, "$W\aa_$a.wixobj") "light $a" | Out-Null
            Run $T.signtool @("sign", "/fd", "SHA256", "/f", $script:Pfx, "/p", $script:CertPass, $msi) "signtool msi $a" -IgnoreExit | Out-Null
        }
        Run $T.signtool @("sign", "/fd", "SHA256", "/f", $script:Pfx, "/p", $script:CertPass, (Join-Path $Out "AppliedAstrology-$Ver-windows-Setup.exe")) "signtool setup" -IgnoreExit | Out-Null
    }
    Step "12_checksums" {
        $lines = Get-ChildItem $Out -File | Where-Object { $_.Extension -in ".apk", ".zip", ".exe", ".msi", ".appxbundle", ".cer" } | Sort-Object Name | ForEach-Object { "{0} *{1}" -f (Sha $_.FullName), $_.Name }
        [IO.File]::WriteAllLines((Join-Path $Out "SHA256SUMS-$Ver.txt"), $lines, $Utf8)
        foreach ($l in $lines) { Say "  $l" }
    }
}

# -------------------------------------------------------------------------------------
# 4. Tests (Android emulator + USB devices, Windows engines + installers) with video
# -------------------------------------------------------------------------------------
function Adb([string[]]$a, [switch]$Quiet) { $eap = $ErrorActionPreference; $ErrorActionPreference = "Continue"; $o = & $T.adb @a 2>&1 | ForEach-Object { "$_" }; $ErrorActionPreference = $eap; if (-not $Quiet) { foreach ($l in $o) { Add-Content $Log ("    adb " + ($a -join ' ') + " | " + $l) -Encoding UTF8 } }; return ,$o }
function Shot([string]$serial, [string]$file) {
    Adb @("-s", $serial, "shell", "screencap", "-p", "/sdcard/aa_shot.png") -Quiet | Out-Null
    Adb @("-s", $serial, "pull", "/sdcard/aa_shot.png", $file) -Quiet | Out-Null
}
function Test-AndroidDevice([string]$serial, [string]$label) {
    $finalDir = Join-Path $Reports "android\$label"
    $dir = "$Work\reports\android\$label"      # adb needs ASCII paths; copied to test_reports at the end
    if (Test-Path $dir) { Remove-Item $dir -Recurse -Force }
    New-Item -ItemType Directory -Force $dir | Out-Null
    $abiList = (Adb @("-s", $serial, "shell", "getprop", "ro.product.cpu.abilist") -Quiet) -join ","
    $sdkV = ((Adb @("-s", $serial, "shell", "getprop", "ro.build.version.sdk") -Quiet) -join "").Trim()
    $model = ((Adb @("-s", $serial, "shell", "getprop", "ro.product.model") -Quiet) -join "").Trim()
    $pick = $null
    foreach ($ab in ($abiList -split ",")) { $ab = $ab.Trim(); if ($ab -in @("arm64-v8a", "x86_64", "armeabi-v7a")) { $pick = $ab; break } }   # primary ABI first
    if (-not $pick) { $pick = "armeabi-v7a" }
    Say "  device $serial ($model, API $sdkV, abis $abiList) -> $pick"
    $apk = "$Work\android\AppliedAstrology-$Ver-$pick.apk"   # ASCII path (adb cannot stat the Unicode repo path)
    Adb @("-s", $serial, "uninstall", "com.appliedastrology.tuvi") | Out-Null
    $inst = Adb @("-s", $serial, "install", "-r", "-g", $apk)
    if (-not ($inst -match "Success")) { throw "install failed on $label : $($inst -join ' ')" }
    Adb @("-s", $serial, "logcat", "-c") | Out-Null
    # wake the screen and dismiss a non-secure keyguard (WebView does not render behind the lock screen)
    Adb @("-s", $serial, "shell", "svc", "power", "stayon", "usb") -Quiet | Out-Null
    Adb @("-s", $serial, "shell", "input", "keyevent", "KEYCODE_WAKEUP") -Quiet | Out-Null
    Adb @("-s", $serial, "shell", "wm", "dismiss-keyguard") -Quiet | Out-Null
    Adb @("-s", $serial, "shell", "input", "keyevent", "82") -Quiet | Out-Null
    Adb @("-s", $serial, "shell", "rm", "-f", "/sdcard/aa_test.mp4") | Out-Null
    $rec = Start-Process -FilePath $T.adb -ArgumentList @("-s", $serial, "shell", "screenrecord", "--time-limit", "175", "--bit-rate", "2000000", "/sdcard/aa_test.mp4") -PassThru -WindowStyle Hidden
    Start-Sleep 2
    Adb @("-s", $serial, "shell", "am", "start", "-n", "com.appliedastrology.tuvi/.MainActivity", "--ez", "selftest", "true") | Out-Null
    $resPath = "/sdcard/Android/data/com.appliedastrology.tuvi/files/selftest/results.json"
    $t0 = Get-Date; $n = 0; $done = $false
    while (((Get-Date) - $t0).TotalSeconds -lt 420) {
        Start-Sleep 5; $n++
        Shot $serial "$dir\live_$('{0:D2}' -f $n).png"
        $chk = (Adb @("-s", $serial, "shell", "ls", $resPath) -Quiet) -join ""
        if ($chk -notmatch "No such file") { $done = $true; break }
    }
    Start-Sleep 3
    # rotate test (landscape) + back to portrait
    Adb @("-s", $serial, "shell", "settings", "put", "system", "accelerometer_rotation", "0") | Out-Null
    Adb @("-s", $serial, "shell", "settings", "put", "system", "user_rotation", "1") | Out-Null
    Start-Sleep 4; Shot $serial "$dir\rotate_landscape.png"
    Adb @("-s", $serial, "shell", "settings", "put", "system", "user_rotation", "0") | Out-Null
    Start-Sleep 3
    try { Stop-Process -Id $rec.Id -Force -ErrorAction SilentlyContinue } catch { }
    Adb @("-s", $serial, "shell", "pkill", "-INT", "screenrecord") | Out-Null
    Start-Sleep 3
    Adb @("-s", $serial, "pull", "/sdcard/Android/data/com.appliedastrology.tuvi/files/selftest", $dir) | Out-Null
    Adb @("-s", $serial, "pull", "/sdcard/aa_test.mp4", "$dir\android_test_video.mp4") | Out-Null
    (Adb @("-s", $serial, "logcat", "-d", "-s", "AppliedAstrology:*", "chromium:*") -Quiet) | Set-Content "$dir\logcat.txt" -Encoding UTF8
    New-Item -ItemType Directory -Force $finalDir | Out-Null
    Copy-Item "$dir\*" $finalDir -Recurse -Force
    $rf = Get-ChildItem $dir -Recurse -Filter results.json | Select-Object -First 1
    if (-not $rf) { throw "selftest did not finish on $label (see $finalDir)" }
    $r = Get-Content $rf.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
    Say ("  SELFTEST {0}: {1}/{2} passed" -f $label, $r.pass, $r.total) $(if ($r.pass -eq $r.total) { "OK" } else { "ERR" })
    foreach ($x in $r.results) { Say ("    {0} {1} {2}" -f $(if ($x.ok) { "PASS" } else { "FAIL" }), $x.step, $x.detail) }
    return $r
}

$AllResults = [ordered]@{}
if ($Phase -in @("all", "test")) {
    Step "20_android_emulator_and_devices" {
        Run $T.adb @("start-server") "adb" -IgnoreExit | Out-Null
        function DevList { (Adb @("devices")) | Where-Object { $_ -match "\tdevice$" } | ForEach-Object { ($_ -split "\t")[0] } }
        # 1) physical USB devices
        foreach ($s in (DevList | Where-Object { $_ -notlike "emulator-*" })) {
            $model = ((Adb @("-s", $s, "shell", "getprop", "ro.product.model") -Quiet) -join "").Trim() -replace "[^A-Za-z0-9_-]", "_"
            $label = "device_$model"
            try { $AllResults["android_$label"] = Test-AndroidDevice $s $label } catch { Say "  $label : $($_.Exception.Message)" "ERR"; $AllResults["android_$label"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
        }
        # 2) emulators: newest API and Android 6.0 (API 23) if available, one after another
        if (-not $SkipEmulator -and $T.emulator) {
            $eap = $ErrorActionPreference; $ErrorActionPreference = "Continue"
            $avdList = @(& $T.emulator -list-avds 2>$null | Where-Object { $_ -and $_ -notmatch "^INFO|^WARN" })
            $ErrorActionPreference = $eap
            Say "  AVDs: $($avdList -join ', ')"
            $targets = @()
            $a37 = $avdList | Where-Object { $_ -match "37|36|35" -and $_ -notmatch "TV" } | Select-Object -First 1
            $a23 = $avdList | Where-Object { $_ -match "23|Android6" } | Select-Object -First 1
            if ($a37) { $targets += $a37 }; if ($a23) { $targets += $a23 }
            # stop emulators left over from earlier runs
            foreach ($e in ((Adb @("devices") -Quiet) | Where-Object { $_ -match "^emulator-" } | ForEach-Object { ($_ -split "\t")[0] })) { Adb @("-s", $e, "emu", "kill") -Quiet | Out-Null }
            Start-Sleep 3
            Get-Process -Name "qemu-system-*", "emulator" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
            Start-Sleep 3
            $port = 5580
            foreach ($avd in $targets) {
                $serial = "emulator-$port"
                Say "  starting emulator $avd on $serial"
                Start-Process -FilePath $T.emulator -ArgumentList @("-avd", $avd, "-port", "$port", "-no-snapshot-load", "-no-boot-anim", "-no-audio", "-gpu", "swiftshader_indirect", "-feature", "-VirtioWifi", "-feature", "-NetsimCliUi", "-feature", "-NetsimWebUi") -WindowStyle Normal
                $t0 = Get-Date; $boot = ""
                do {
                    Start-Sleep 5
                    $boot = ((Adb @("-s", $serial, "shell", "getprop", "sys.boot_completed") -Quiet) -join "").Trim()
                } while ($boot -ne "1" -and ((Get-Date) - $t0).TotalSeconds -lt 480)
                if ($boot -ne "1") { Say "  $avd did not boot in 8 minutes" "ERR"; $AllResults["android_emulator_$avd"] = @{ pass = 0; total = 0; error = "boot timeout" } }
                else {
                    Start-Sleep 15
                    Adb @("-s", $serial, "shell", "input", "keyevent", "82") -Quiet | Out-Null
                    $label = "emulator_$avd" -replace "[^A-Za-z0-9_.-]", "_"
                    try { $AllResults["android_$label"] = Test-AndroidDevice $serial $label } catch { Say "  $label : $($_.Exception.Message)" "ERR"; $AllResults["android_$label"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
                }
                Adb @("-s", $serial, "emu", "kill") -Quiet | Out-Null
                Start-Sleep 8
                $port += 2
            }
        }
        if ($AllResults.Keys.Count -eq 0) { throw "no Android device or emulator available" }
    }

    $ff = $null
    function Start-DesktopVideo([string]$file) {
        if (-not $T.ffmpeg) { return $null }
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = $T.ffmpeg
        $psi.Arguments = "-y -f gdigrab -framerate 8 -draw_mouse 1 -i desktop -vf scale=trunc(iw/4)*2:trunc(ih/4)*2 -c:v libx264 -preset veryfast -crf 32 -pix_fmt yuv420p `"$file`""
        $psi.UseShellExecute = $false; $psi.RedirectStandardInput = $true; $psi.CreateNoWindow = $true
        return [System.Diagnostics.Process]::Start($psi)
    }
    function Stop-DesktopVideo($p) { if ($p) { try { $p.StandardInput.Write("q"); $p.WaitForExit(15000) | Out-Null } catch { } } }
    function Test-WindowsExe([string]$exe, [string]$label, [string[]]$extra = @()) {
        $dir = Join-Path $Reports "windows\$label"; if (Test-Path $dir) { Remove-Item $dir -Recurse -Force }
        New-Item -ItemType Directory -Force $dir | Out-Null
        $p = Start-Process -FilePath $exe -ArgumentList (@("--selftest", "--out", "`"$dir`"") + $extra) -PassThru
        if (-not $p.WaitForExit(480000)) { Stop-Process -Id $p.Id -Force; throw "$label timed out" }
        $rf = Join-Path $dir "results.json"
        if (-not (Test-Path $rf)) { throw "$label produced no results.json" }
        $r = Get-Content $rf -Raw -Encoding UTF8 | ConvertFrom-Json
        Say ("  SELFTEST {0}: {1}/{2} passed ({3})" -f $label, $r.pass, $r.total, $r.platform.engine) $(if ($r.pass -eq $r.total) { "OK" } else { "ERR" })
        foreach ($x in $r.results) { Say ("    {0} {1} {2}" -f $(if ($x.ok) { "PASS" } else { "FAIL" }), $x.step, $x.detail) }
        return $r
    }

    Step "21_windows_tests" {
        $vid = Join-Path $Reports "windows\windows_test_video.mp4"
        New-Item -ItemType Directory -Force (Split-Path $vid) | Out-Null
        $ff = Start-DesktopVideo $vid
        try {
            # a) portable (WebView2 engine) and the same build forced to IE11 (Windows 7 code path)
            Get-Process AppliedAstrology -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
            Start-Sleep 2
            $pt = "$Work\test_portable_$Stamp"
            Expand-Archive (Join-Path $Out "AppliedAstrology-$Ver-windows-portable.zip") $pt -Force
            $pexe = Get-ChildItem $pt -Recurse -Filter AppliedAstrology.exe | Select-Object -First 1
            try { $AllResults["windows_portable_webview2"] = Test-WindowsExe $pexe.FullName "portable_webview2" } catch { Say $_.Exception.Message "ERR"; $AllResults["windows_portable_webview2"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
            try { $AllResults["windows_portable_ie11"] = Test-WindowsExe $pexe.FullName "portable_ie11" @("--engine", "ie") } catch { Say $_.Exception.Message "ERR"; $AllResults["windows_portable_ie11"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
            # b) Setup.exe silent install -> test -> silent uninstall
            try {
            $setup = Join-Path $Out "AppliedAstrology-$Ver-windows-Setup.exe"
            $dst = "$env:LOCALAPPDATA\Programs\Applied Astrology"
            $p = Start-Process $setup -ArgumentList "/S" -PassThru -Wait
            Say "  Setup.exe /S exit $($p.ExitCode); installed: $(Test-Path "$dst\AppliedAstrology.exe")"
            try { $AllResults["windows_setup_exe"] = Test-WindowsExe "$dst\AppliedAstrology.exe" "setup_exe_installed" } catch { Say $_.Exception.Message "ERR"; $AllResults["windows_setup_exe"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
            Start-Process "$dst\uninstall.exe" -ArgumentList "/uninstall", "/S" -Wait; Start-Sleep 5
            Say "  uninstall: folder removed = $(-not (Test-Path "$dst\AppliedAstrology.exe")); ARP key removed = $(-not (Test-Path 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\AppliedAstrology'))"
            } catch { Say "  setup.exe test: $($_.Exception.Message)" "ERR"; $AllResults["windows_setup_exe"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
            # c) MSI (matching this PC architecture), per-user, silent
            try {
            $arch = if ($env:PROCESSOR_ARCHITECTURE -eq "ARM64") { "arm64" } elseif ([Environment]::Is64BitOperatingSystem) { "x64" } else { "x86" }
            $msi = Join-Path $Out "AppliedAstrology-$Ver-windows-$arch.msi"
            if (Test-Path $msi) {
                $p = Start-Process msiexec.exe -ArgumentList "/i", "`"$msi`"", "/qn", "/l*v", "`"$Reports\windows\msi_install.log`"" -PassThru -Wait
                Say "  msiexec /i exit $($p.ExitCode)"
                try { $AllResults["windows_msi_$arch"] = Test-WindowsExe "$dst\AppliedAstrology.exe" "msi_$arch" } catch { Say $_.Exception.Message "ERR"; $AllResults["windows_msi_$arch"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
                $p = Start-Process msiexec.exe -ArgumentList "/x", "`"$msi`"", "/qn" -PassThru -Wait
                Say "  msiexec /x exit $($p.ExitCode)"
            }
            } catch { Say "  msi test: $($_.Exception.Message)" "ERR" }
            # d) appxbundle: trust certificate for current user, install, run selftest through flag file, remove
            $bundle = Join-Path $Out "AppliedAstrology-$Ver-windows.appxbundle"
            try {
                $cer = Join-Path $Out "AppliedAstrology-$Ver-signing-certificate.cer"
                Import-Certificate -FilePath $cer -CertStoreLocation Cert:\CurrentUser\TrustedPeople | Out-Null
                try { Import-Certificate -FilePath $cer -CertStoreLocation Cert:\LocalMachine\TrustedPeople | Out-Null } catch { Say "  (LocalMachine\TrustedPeople needs admin - trying user install)" }
                $bundleA = "$env:TEMP\AppliedAstrology-$Ver-windows.appxbundle"   # real ASCII path: AppX deployment refuses junction paths such as D:\yh
                Copy-Item $bundle $bundleA -Force
                try { Add-AppxPackage -Path $bundleA -ForceApplicationShutdown; Say "  appxbundle installed from signed bundle" }
                catch {
                    Say "  signed bundle install failed ($($_.Exception.Message)); trying developer-mode registration of the x64 layout" "ERR"
                    $regDir = "$env:TEMP\AppliedAstrology_appx_layout"
                    if (Test-Path $regDir) { Remove-Item $regDir -Recurse -Force }
                    Copy-Item "$W\appx\layout-x64" $regDir -Recurse
                    Add-AppxPackage -Register "$regDir\AppxManifest.xml" -ForceApplicationShutdown
                }
                $pkg = Get-AppxPackage -Name "AppliedAstrology.TuViUngDung"
                Say "  appx installed: $($pkg.PackageFullName)"
                $dir = Join-Path $Reports "windows\appxbundle"; New-Item -ItemType Directory -Force $dir | Out-Null
                New-Item -ItemType Directory -Force "$env:APPDATA\AppliedAstrology" | Out-Null
                [IO.File]::WriteAllText("$env:APPDATA\AppliedAstrology\selftest.flag", $dir, $Utf8)
                Start-Process "shell:AppsFolder\$($pkg.PackageFamilyName)!AppliedAstrology"
                $t0 = Get-Date; while (-not (Test-Path "$dir\results.json") -and ((Get-Date) - $t0).TotalSeconds -lt 240) { Start-Sleep 3 }
                if (Test-Path "$dir\results.json") { $r = Get-Content "$dir\results.json" -Raw -Encoding UTF8 | ConvertFrom-Json; $AllResults["windows_appxbundle"] = $r; Say ("  SELFTEST appxbundle: {0}/{1}" -f $r.pass, $r.total) $(if ($r.pass -eq $r.total) { "OK" } else { "ERR" }) }
                else { throw "appx selftest produced no results" }
                Start-Sleep 4
                Remove-AppxPackage $pkg.PackageFullName
            } catch { Say "  appxbundle test: $($_.Exception.Message)" "ERR"; $AllResults["windows_appxbundle"] = @{ pass = 0; total = 0; error = $_.Exception.Message } }
        } finally { Stop-DesktopVideo $ff }
    }
    Step "22_test_report" {
        $md = New-Object System.Text.StringBuilder
        [void]$md.AppendLine("# Applied Astrology $Ver - Test report ($((Get-Date).ToString('yyyy-MM-dd HH:mm')))")
        [void]$md.AppendLine("")
        [void]$md.AppendLine("| Target | Engine / Device | Passed | Notes |")
        [void]$md.AppendLine("|---|---|---|---|")
        foreach ($k in $AllResults.Keys) {
            $r = $AllResults[$k]
            $eng = if ($r.platform) { "$($r.platform.engine) $($r.platform.os) $($r.platform.abi) $($r.platform.device)" } else { "" }
            $fails = if ($r.results) { ($r.results | Where-Object { -not $_.ok } | ForEach-Object { $_.step }) -join ", " } else { $r.error }
            [void]$md.AppendLine("| $k | $eng | $($r.pass)/$($r.total) | $fails |")
        }
        [IO.File]::WriteAllText((Join-Path $Reports "TEST_REPORT.md"), $md.ToString(), $Utf8)
        ($AllResults | ConvertTo-Json -Depth 8) | Set-Content (Join-Path $Reports "all_results.json") -Encoding UTF8
    }
}

# -------------------------------------------------------------------------------------
# 5. Publish: git commit + push, GitHub release with assets, verification
# -------------------------------------------------------------------------------------
if ($Phase -in @("all", "publish") -and -not $NoPublish) {
    $Owner = "kiritokmn1403"; $RepoName = "Applied-Astrology-T-Vi-"
    Step "30_git_commit_push" {
        Push-Location $Repo
        try {
            Run $T.git @("config", "core.quotepath", "false") "git" | Out-Null
            # remove obsolete v0.0.1 root files (moved to src/ in v0.0.2)
            foreach ($old in @("index.html", "i18n.js", "settings.js", "settings.json", "tuvi_engine.js", "AppliedAstrology.exe", "build_apk_split.ps1", "make_tuvi_appxbundle.ps1", "make_tuvi_installer.ps1", "assets/buudinh_tuvi_database.json")) {
                if (Test-Path $old) { Run $T.git @("rm", "-q", "--ignore-unmatch", $old) "git rm" -IgnoreExit | Out-Null }
            }
            Run $T.git @("add", "-A", "src", "tools", "assets", "docs", "output/$Ver", "test_reports/$Ver", "README.md", "TECHNICAL_UPDATE_NOTES.md", "CHANGELOG.md", "build_release_$Ver.ps1", "BUILD_v$Ver.bat", ".gitignore", ".gitattributes", "LICENSE") "git add" -IgnoreExit | Out-Null
            Run $T.git @("-c", "user.name=kiritokmn1403", "-c", "user.email=kietnguyenworking1403@gmail.com", "commit", "-m", "release: Applied Astrology v$Ver (Android arm64-v8a/armeabi-v7a/x86_64, Windows appxbundle/msi/exe/portable)") "git commit" -IgnoreExit | Out-Null
            Run $T.git @("push", "origin", "HEAD:main") "git push" | Out-Null
            Run $T.git @("tag", "-f", "v$Ver") "git tag" | Out-Null
            Run $T.git @("push", "-f", "origin", "v$Ver") "git push tag" | Out-Null
        } finally { Pop-Location }
    }
    Step "31_github_release" {
        # Token from the user's own Git Credential Manager (never printed or logged)
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = $T.git; $psi.Arguments = "credential fill"
        $psi.UseShellExecute = $false; $psi.RedirectStandardInput = $true; $psi.RedirectStandardOutput = $true; $psi.CreateNoWindow = $true
        $gp = [System.Diagnostics.Process]::Start($psi)
        $gp.StandardInput.NewLine = "`n"
        $gp.StandardInput.WriteLine("protocol=https"); $gp.StandardInput.WriteLine("host=github.com"); $gp.StandardInput.WriteLine("")
        $gp.StandardInput.Close()
        $cred = $gp.StandardOutput.ReadToEnd() -split "`n"
        $gp.WaitForExit(60000) | Out-Null
        $tok = (($cred | Where-Object { $_ -like "password=*" }) -replace "^password=", "").Trim()
        if (-not $tok) { throw "no GitHub credential in Git Credential Manager - sign in with GitHub Desktop/git first" }
        $h = @{ Authorization = "token $tok"; "User-Agent" = "AppliedAstrology-release"; Accept = "application/vnd.github+json" }
        $api = "https://api.github.com/repos/$Owner/$RepoName"
        $rel = $null
        try { $rel = Invoke-RestMethod -Headers $h -Uri "$api/releases/tags/v$Ver" -Method Get } catch { }
        $notesFile = Join-Path $Repo "docs\RELEASE_NOTES_$Ver.md"
        $notes = if (Test-Path $notesFile) { Get-Content $notesFile -Raw -Encoding UTF8 } else { "Applied Astrology $Ver" }
        if (-not $rel) {
            $body = @{ tag_name = "v$Ver"; name = "Applied Astrology v$Ver"; body = $notes; draft = $false; prerelease = $false } | ConvertTo-Json -Depth 4
            $rel = Invoke-RestMethod -Headers $h -Uri "$api/releases" -Method Post -Body ([Text.Encoding]::UTF8.GetBytes($body)) -ContentType "application/json; charset=utf-8"
        }
        $assets = @(Get-ChildItem $Out -File | Where-Object { $_.Extension -in ".apk", ".zip", ".exe", ".msi", ".appxbundle", ".cer", ".txt" })
        # test videos (kept out of git because of the 100 MB file limit) are attached to the release
        foreach ($v in (Get-ChildItem $Reports -Recurse -Filter *.mp4)) {
            $nm = "test-video-" + ($v.Directory.Name -replace "[^A-Za-z0-9_.-]", "_") + ".mp4"
            $dst = Join-Path $Work $nm; Copy-Item $v.FullName $dst -Force; $assets += Get-Item $dst
        }
        foreach ($f in $assets) {
            $old = $rel.assets | Where-Object { $_.name -eq $f.Name }
            if ($old) { Invoke-RestMethod -Headers $h -Uri "$api/releases/assets/$($old.id)" -Method Delete | Out-Null }
            $ct = switch ($f.Extension) { ".apk" { "application/vnd.android.package-archive" } ".zip" { "application/zip" } ".msi" { "application/x-msi" } ".txt" { "text/plain" } default { "application/octet-stream" } }
            $up = "https://uploads.github.com/repos/$Owner/$RepoName/releases/$($rel.id)/assets?name=$([Uri]::EscapeDataString($f.Name))"
            Say ("  uploading {0} ({1:N2} MB)" -f $f.Name, ($f.Length / 1MB))
            Invoke-RestMethod -Headers $h -Uri $up -Method Post -InFile $f.FullName -ContentType $ct -TimeoutSec 1800 | Out-Null
        }
        # Verify
        $rel = Invoke-RestMethod -Headers $h -Uri "$api/releases/$($rel.id)" -Method Get
        $missing = @()
        foreach ($f in $assets) {
            $a = $rel.assets | Where-Object { $_.name -eq $f.Name }
            if (-not $a -or $a.size -ne $f.Length) { $missing += $f.Name } else { Say ("  verified {0} {1} bytes state={2}" -f $a.name, $a.size, $a.state) "OK" }
        }
        "release_url=$($rel.html_url)" | Set-Content (Join-Path $LogDir "release_url.txt")
        if ($missing.Count) { throw "assets missing/mismatch: $($missing -join ', ')" }
        Say "  Release: $($rel.html_url)" "OK"
    }
}

$Summary.finished = (Get-Date).ToString("s")
$Summary.failed = $script:Failed
[IO.File]::WriteAllText($StatusFile, ($Summary | ConvertTo-Json -Depth 6), $Utf8)
if ($script:Failed.Count) { Say "=== Finished with failures: $($script:Failed -join ', ') ===" "ERR" } else { Say "=== ALL DONE ===" "OK" }
