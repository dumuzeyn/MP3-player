$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$SdkRoot = Join-Path $Root ".android-sdk"
$BuildDir = Join-Path $Root "build"
$OutputDir = Join-Path $Root "output"
$CmdlineZip = Join-Path $Root "commandlinetools.zip"
$CmdlineUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$Platform = "android-35"
$BuildTools = "35.0.0"

New-Item -ItemType Directory -Force -Path $SdkRoot, $BuildDir, $OutputDir | Out-Null

function Download-IfMissing($Url, $Path) {
    if (Test-Path -LiteralPath $Path) { return }
    Write-Host "Downloading $Url"
    Invoke-WebRequest -Uri $Url -OutFile $Path
}

function Ensure-AndroidSdk {
    $SdkManager = Join-Path $SdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"
    if (-not (Test-Path -LiteralPath $SdkManager)) {
        Download-IfMissing $CmdlineUrl $CmdlineZip
        $TempTools = Join-Path $Root "cmdline-temp"
        if (Test-Path -LiteralPath $TempTools) { Remove-Item -LiteralPath $TempTools -Recurse -Force }
        New-Item -ItemType Directory -Force -Path $TempTools | Out-Null
        tar -xf $CmdlineZip -C $TempTools
        New-Item -ItemType Directory -Force -Path (Join-Path $SdkRoot "cmdline-tools") | Out-Null
        $Latest = Join-Path $SdkRoot "cmdline-tools\latest"
        if (Test-Path -LiteralPath $Latest) { Remove-Item -LiteralPath $Latest -Recurse -Force }
        $ExtractedTools = Get-ChildItem -LiteralPath $TempTools -Recurse -Filter sdkmanager.bat |
            Select-Object -First 1 |
            ForEach-Object { Split-Path -Parent (Split-Path -Parent $_.FullName) }
        if (-not $ExtractedTools) {
            throw "sdkmanager.bat was not found after extracting Android command line tools."
        }
        Move-Item -LiteralPath $ExtractedTools -Destination $Latest
        Remove-Item -LiteralPath $TempTools -Recurse -Force
    }

    $env:ANDROID_HOME = $SdkRoot
    $env:ANDROID_SDK_ROOT = $SdkRoot
    $Accept = ("y`n" * 80)
    $Accept | & $SdkManager --sdk_root=$SdkRoot --licenses | Out-Host
    & $SdkManager --sdk_root=$SdkRoot "platform-tools" "platforms;$Platform" "build-tools;$BuildTools"
}

function Require-Tool($Path, $Name) {
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "$Name not found: $Path"
    }
}

function Invoke-Checked {
    param([scriptblock]$Command)
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code $LASTEXITCODE"
    }
}

Ensure-AndroidSdk

$AndroidJar = Join-Path $SdkRoot "platforms\$Platform\android.jar"
$Aapt2 = Join-Path $SdkRoot "build-tools\$BuildTools\aapt2.exe"
$D8 = Join-Path $SdkRoot "build-tools\$BuildTools\d8.bat"
$ZipAlign = Join-Path $SdkRoot "build-tools\$BuildTools\zipalign.exe"
$ApkSigner = Join-Path $SdkRoot "build-tools\$BuildTools\apksigner.bat"
$SigningMode = if ($env:MP3_SIGNING_MODE) { $env:MP3_SIGNING_MODE.ToLowerInvariant() } else { "debug" }

function Require-Env($Name) {
    $Value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "$Name is required for release signing."
    }
    return $Value
}

Require-Tool $AndroidJar "android.jar"
Require-Tool $Aapt2 "aapt2"
Require-Tool $D8 "d8"
Require-Tool $ZipAlign "zipalign"
Require-Tool $ApkSigner "apksigner"

if (Test-Path -LiteralPath $BuildDir) { Remove-Item -LiteralPath $BuildDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $BuildDir, $OutputDir | Out-Null

$FlatDir = Join-Path $BuildDir "flat"
$GenDir = Join-Path $BuildDir "gen"
$ClassDir = Join-Path $BuildDir "classes"
$DexDir = Join-Path $BuildDir "dex"
New-Item -ItemType Directory -Force -Path $FlatDir, $GenDir, $ClassDir, $DexDir | Out-Null

Invoke-Checked { & $Aapt2 compile --dir (Join-Path $Root "app\src\main\res") -o $FlatDir }

$UnsignedApk = Join-Path $BuildDir "mp3-player-unsigned.apk"
$FlatFiles = Get-ChildItem -LiteralPath $FlatDir -Filter *.flat -Recurse | ForEach-Object { $_.FullName }
$AaptLinkArgs = @(
    "link",
    "-o", $UnsignedApk,
    "-I", $AndroidJar,
    "--manifest", (Join-Path $Root "app\src\main\AndroidManifest.xml"),
    "--min-sdk-version", "23",
    "--target-sdk-version", "35",
    "--version-code", "1",
    "--version-name", "1.0",
    "--java", $GenDir
)
$AssetsDir = Join-Path $Root "app\src\main\assets"
if (Test-Path -LiteralPath $AssetsDir) {
    $AaptLinkArgs += @("-A", $AssetsDir)
}
$AaptLinkArgs += $FlatFiles
Invoke-Checked { & $Aapt2 @AaptLinkArgs }

$JavaFiles = Get-ChildItem -Path (Join-Path $Root "app\src\main\java") -Filter *.java -Recurse | ForEach-Object { $_.FullName }
Invoke-Checked { & javac --release 17 -encoding UTF-8 -classpath $AndroidJar -d $ClassDir $JavaFiles }

$ClassesJar = Join-Path $BuildDir "classes.jar"
Invoke-Checked { & jar cf $ClassesJar -C $ClassDir . }
Invoke-Checked { & $D8 --min-api 23 --classpath $AndroidJar --output $DexDir $ClassesJar }

Invoke-Checked { & jar uf $UnsignedApk -C $DexDir classes.dex }

if ($SigningMode -eq "release") {
    $KeyStore = Require-Env "MP3_RELEASE_KEYSTORE"
    $KeyAlias = Require-Env "MP3_RELEASE_KEY_ALIAS"
    $StorePass = Require-Env "MP3_RELEASE_STORE_PASS"
    $KeyPass = Require-Env "MP3_RELEASE_KEY_PASS"
    if (-not (Test-Path -LiteralPath $KeyStore)) {
        throw "Release keystore not found: $KeyStore"
    }
} elseif ($SigningMode -eq "debug") {
    $KeyStore = Join-Path $Root "mp3player.keystore"
    $KeyAlias = "mp3player"
    $StorePass = "android"
    $KeyPass = "android"
    if (-not (Test-Path -LiteralPath $KeyStore)) {
        & keytool -genkeypair `
            -keystore $KeyStore `
            -storepass $StorePass `
            -keypass $KeyPass `
            -alias $KeyAlias `
            -keyalg RSA `
            -keysize 2048 `
            -validity 10000 `
            -dname "CN=MP3 Player Debug, OU=Local, O=MP3 Player, L=Local, S=Local, C=RU"
    }
} else {
    throw "Unknown MP3_SIGNING_MODE '$SigningMode'. Use 'debug' or 'release'."
}

$AlignedApk = Join-Path $BuildDir "mp3-player-aligned.apk"
$SignedApk = Join-Path $OutputDir "MP3-Player.apk"

Invoke-Checked { & $ZipAlign -f -p 4 $UnsignedApk $AlignedApk }
Invoke-Checked { & $ApkSigner sign `
    --ks $KeyStore `
    --ks-key-alias $KeyAlias `
    --ks-pass "pass:$StorePass" `
    --key-pass "pass:$KeyPass" `
    --out $SignedApk `
    $AlignedApk }

Invoke-Checked { & $ApkSigner verify --verbose $SignedApk }

Write-Host ""
Write-Host "APK ready:"
Write-Host $SignedApk
