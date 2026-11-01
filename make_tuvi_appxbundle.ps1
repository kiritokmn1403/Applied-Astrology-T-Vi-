Add-Type -AssemblyName System.Drawing

$ver = "0.0.1"
$verQuad = "0.0.1.0"
$makeappx = "C:\Program Files (x86)\Windows Kits\10\bin\10.0.26100.0\x64\makeappx.exe"
$out = ".\output\0.0.1"
$layout = "$env:TEMP\AppliedAstrology_appx_layout"
$bundleDir = "$env:TEMP\AppliedAstrology_bundle_staging"
$rel = ".\build\windows-portable"

Write-Output "Building Windows .appxbundle for Applied Astrology v$ver"

if (Test-Path $layout) { Remove-Item $layout -Recurse -Force }
New-Item -ItemType Directory -Force -Path $layout | Out-Null

# 1) Copy application files
Copy-Item "$rel\*" $layout -Recurse -Force

# 2) Generate required AppX visual assets
$assetsDir = "$layout\Assets"
New-Item -ItemType Directory -Force -Path $assetsDir | Out-Null

$srcIconPath = ".\assets\logo_512.png"
$srcImg = [System.Drawing.Image]::FromFile($srcIconPath)

function Resize-Icon($img, $w, $h, $dest) {
    $bmp = New-Object System.Drawing.Bitmap($w, $h)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
    $g.DrawImage($img, 0, 0, $w, $h)
    $g.Dispose()
    $bmp.Save($dest, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

Resize-Icon $srcImg 50 50 "$assetsDir\StoreLogo.png"
Resize-Icon $srcImg 150 150 "$assetsDir\Square150x150Logo.png"
Resize-Icon $srcImg 44 44 "$assetsDir\Square44x44Logo.png"
Resize-Icon $srcImg 310 150 "$assetsDir\Wide310x150Logo.png"
Resize-Icon $srcImg 620 300 "$assetsDir\SplashScreen.png"
$srcImg.Dispose()

# 3) AppxManifest.xml
$manifestContent = @"
<?xml version="1.0" encoding="utf-8"?>
<Package
  xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
  xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10"
  xmlns:rescap="http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities">
  <Identity
    Name="AppliedAstrology"
    ProcessorArchitecture="x64"
    Publisher="CN=Visicons, O=Visicons, C=VN"
    Version="$verQuad" />
  <Properties>
    <DisplayName>Applied Astrology</DisplayName>
    <PublisherDisplayName>Visicons Astrology Team</PublisherDisplayName>
    <Logo>Assets\StoreLogo.png</Logo>
    <Description>Applied Astrology - Tử Vi Ứng Dụng Bửu Đình</Description>
  </Properties>
  <Resources>
    <Resource Language="vi-VN" />
    <Resource Language="en-US" />
  </Resources>
  <Dependencies>
    <TargetDeviceFamily Name="Windows.Desktop" MinVersion="10.0.14393.0" MaxVersionTested="10.0.26100.0" />
  </Dependencies>
  <Capabilities>
    <rescap:Capability Name="runFullTrust" />
  </Capabilities>
  <Applications>
    <Application Id="App"
      Executable="AppliedAstrology.exe"
      EntryPoint="Windows.FullTrustApplication">
      <uap:VisualElements
        DisplayName="Applied Astrology"
        Description="Tử Vi Ứng Dụng - Bửu Đình"
        BackgroundColor="#0a0518"
        Square150x150Logo="Assets\Square150x150Logo.png"
        Square44x44Logo="Assets\Square44x44Logo.png">
        <uap:DefaultTile Wide310x150Logo="Assets\Wide310x150Logo.png" />
        <uap:SplashScreen Image="Assets\SplashScreen.png" />
      </uap:VisualElements>
    </Application>
  </Applications>
</Package>
"@
Set-Content -Path "$layout\AppxManifest.xml" -Value $manifestContent -Encoding UTF8

# 4) Pack .appx into bundle staging
if (Test-Path $bundleDir) { Remove-Item $bundleDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $bundleDir | Out-Null

$appxFile = "$bundleDir\AppliedAstrology_${ver}_x64.appx"
& $makeappx pack /d $layout /p $appxFile /o

# 5) Build .appxbundle
$appxBundleFile = "$out\AppliedAstrology-$ver-windows-x64.appxbundle"
Remove-Item $appxBundleFile -ErrorAction SilentlyContinue
& $makeappx bundle /d $bundleDir /p $appxBundleFile /o

if (Test-Path $appxBundleFile) {
    Write-Output "AppxBundle created successfully: $appxBundleFile ($([math]::Round((Get-Item $appxBundleFile).Length / 1MB, 2)) MB)"
} else {
    throw "Failed to build .appxbundle!"
}
