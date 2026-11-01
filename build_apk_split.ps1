Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$ver = "0.0.1"
$env:JAVA_HOME = "D:\yh\tools\jdk"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$buildTools = "$sdk\build-tools\36.0.0"
$platformJar = "$sdk\platforms\android-37.0\android.jar"
$aapt2 = "$buildTools\aapt2.exe"
$d8 = "$buildTools\d8.bat"
$zipalign = "$buildTools\zipalign.exe"
$apksigner = "$buildTools\apksigner.bat"
$javac = "$env:JAVA_HOME\bin\javac.exe"
$outDir = ".\output\0.0.1"
$keystore = ".\appliedastrology.jks"

Write-Output "Building Android Split APKs for Applied Astrology v$ver (Enhanced UI)..."

$staging = "$env:TEMP\tuvi_apk_staging"
if (Test-Path $staging) { Remove-Item $staging -Recurse -Force }
New-Item -ItemType Directory -Force $staging | Out-Null

$resDir = "$staging\res"
$assetsDir = "$staging\assets"
$srcDir = "$staging\src"
$binDir = "$staging\bin"
$compiledRes = "$staging\compiled_res"

New-Item -ItemType Directory -Force "$resDir\values", $assetsDir, "$assetsDir\assets", "$srcDir\com\visicons\appliedastrology", $binDir, $compiledRes | Out-Null

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

# 1. AndroidManifest.xml
$manifestPath = "$staging\AndroidManifest.xml"
$manifest = @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.visicons.appliedastrology"
    android:versionCode="1"
    android:versionName="$ver">

    <uses-sdk android:minSdkVersion="23" android:targetSdkVersion="35" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:hardwareAccelerated="true"
        android:theme="@android:style/Theme.NoTitleBar.Fullscreen">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"@
[System.IO.File]::WriteAllText($manifestPath, $manifest, $utf8NoBom)

# 2. strings.xml
$stringsContent = @"
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Applied Astrology</string>
</resources>
"@
[System.IO.File]::WriteAllText("$resDir\values\strings.xml", $stringsContent, $utf8NoBom)

# 3. Generate Mipmap Icons
function Resize-Png($src, $w, $h, $dest) {
    $img = [System.Drawing.Image]::FromFile($src)
    $bmp = New-Object System.Drawing.Bitmap($w, $h)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
    $g.DrawImage($img, 0, 0, $w, $h)
    $g.Dispose()
    $img.Dispose()
    New-Item -ItemType Directory -Force (Split-Path -Parent $dest) | Out-Null
    $bmp.Save($dest, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

$logoPng = ".\assets\logo_512.png"
Resize-Png $logoPng 48 48 "$resDir\mipmap-mdpi\ic_launcher.png"
Resize-Png $logoPng 72 72 "$resDir\mipmap-hdpi\ic_launcher.png"
Resize-Png $logoPng 96 96 "$resDir\mipmap-xhdpi\ic_launcher.png"
Resize-Png $logoPng 144 144 "$resDir\mipmap-xxhdpi\ic_launcher.png"
Resize-Png $logoPng 192 192 "$resDir\mipmap-xxxhdpi\ic_launcher.png"

# 4. Compile Resources with aapt2
& $aapt2 compile --dir "$resDir" -o "$compiledRes\res.zip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed!" }

# 5. Link Resources
$baseApk = "$binDir\base.apk"
& $aapt2 link -o $baseApk -I $platformJar --manifest $manifestPath --min-sdk-version 23 --target-sdk-version 35 "$compiledRes\res.zip" --auto-add-overlay
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed!" }
Write-Output "aapt2 link succeeded."

# 6. MainActivity.java with Universal File Access & WebSettings
$javaCode = @"
package com.visicons.appliedastrology;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.res.Configuration;

public class MainActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mWebView = new WebView(this);
        setContentView(mWebView);

        WebSettings s = mWebView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mWebView != null) {
            mWebView.requestLayout();
        }
    }
}
"@
[System.IO.File]::WriteAllText("$srcDir\com\visicons\appliedastrology\MainActivity.java", $javaCode, $utf8NoBom)

# Compile Java with source/target 17
$classesDir = "$binDir\classes"
New-Item -ItemType Directory -Force $classesDir | Out-Null
& $javac -source 17 -target 17 -cp $platformJar -d $classesDir "$srcDir\com\visicons\appliedastrology\MainActivity.java"
if ($LASTEXITCODE -ne 0) { throw "javac failed!" }

# 7. Convert to Dalvik Bytecode with d8
$dexDir = "$binDir\dex"
New-Item -ItemType Directory -Force $dexDir | Out-Null
& $d8 "$classesDir\com\visicons\appliedastrology\MainActivity.class" --lib $platformJar --min-api 23 --output $dexDir
if ($LASTEXITCODE -ne 0) { throw "d8 failed!" }
Write-Output "d8 compiled classes.dex."

# 8. Assets Payload - Copy cleanly without nested assets/assets/
Copy-Item ".\index.html" "$assetsDir\index.html" -Force
Copy-Item ".\tuvi_engine.js" "$assetsDir\tuvi_engine.js" -Force
Copy-Item ".\i18n.js" "$assetsDir\i18n.js" -Force
Copy-Item ".\settings.js" "$assetsDir\settings.js" -Force
Copy-Item ".\settings.json" "$assetsDir\settings.json" -Force
Copy-Item ".\buudinh_tuvi_database.json" "$assetsDir\buudinh_tuvi_database.json" -Force

# Copy contents of assets/ into $assetsDir\assets\
Get-ChildItem -Path ".\assets" | ForEach-Object {
    Copy-Item $_.FullName "$assetsDir\assets\" -Recurse -Force
}

# 9. Build and Sign Split APKs
$abis = @('arm64-v8a', 'armeabi-v7a', 'x86_64')
$sourceLibBase = "D:\yh\build\mangayomi\build\app\intermediates\merged_native_libs\release\mergeReleaseNativeLibs\out\lib"

foreach ($abi in $abis) {
    $abiApkRaw = "$binDir\app-$abi-raw.apk"
    $abiApkAligned = "$binDir\app-$abi-aligned.apk"
    $finalApk = "$outDir\AppliedAstrology-$ver-$abi.apk"

    Copy-Item $baseApk $abiApkRaw -Force

    # Add classes.dex, assets, and native libraries
    $zip = [System.IO.Compression.ZipFile]::Open($abiApkRaw, [System.IO.Compression.ZipArchiveMode]::Update)
    
    # 9a. Add classes.dex
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, "$dexDir\classes.dex", "classes.dex") | Out-Null
    
    # 9b. Add assets
    Get-ChildItem -Path $assetsDir -Recurse -File | ForEach-Object {
        $relPath = $_.FullName.Substring($assetsDir.Length + 1).Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, "assets/$relPath") | Out-Null
    }

    # 9c. Add ABI native library (libapp.so)
    $libFile = "$sourceLibBase\$abi\libapp.so"
    if (Test-Path $libFile) {
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $libFile, "lib/$abi/libapp.so") | Out-Null
    }
    
    $zip.Dispose()

    # 10. Zipalign
    if (Test-Path $abiApkAligned) { Remove-Item $abiApkAligned -Force }
    & $zipalign -v -p 4 $abiApkRaw $abiApkAligned
    if ($LASTEXITCODE -ne 0) { throw "zipalign failed for $abi!" }

    # 11. Apksigner
    if (Test-Path $finalApk) { Remove-Item $finalApk -Force }
    & $apksigner sign --ks $keystore --ks-key-alias tuvi --ks-pass pass:tuvi1234 --key-pass pass:tuvi1234 --out $finalApk $abiApkAligned
    if ($LASTEXITCODE -ne 0) { throw "apksigner failed for $abi!" }

    Write-Output "Successfully built & signed: $finalApk ($((Get-Item $finalApk).Length / 1MB | ForEach-Object { '{0:N2} MB' -f $_ }))"
}

Write-Output "All Split APKs generated successfully!"
