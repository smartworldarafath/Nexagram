#define MyAppName "Nexagram Desktop"
#define MyAppVersion "2.3.1"
#define MyAppPublisher "Zero by Arafath (SmartWorld)"
#define MyAppURL "https://github.com/smartworldarafath/Nexagram"
#define MyAppExeName "Nexagram.exe"
#define SourceDir "d:\Gemini Antigravity Project\Project 16 (Nexagram dot)\desktop_build\Nexagram-win32-x64"
#define IconFile "d:\Gemini Antigravity Project\Project 16 (Nexagram dot)\desktop_client\nexagram.ico"
#define OutputFolder "d:\Gemini Antigravity Project\Project 16 (Nexagram dot)\final_release"

[Setup]
AppId={{E5F90B20-B8D4-492F-C054-8F13C47E0D31}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} v{#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}/issues
AppUpdatesURL={#MyAppURL}/releases
DefaultDirName={localappdata}\Programs\Nexagram
DisableDirPage=no
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputDir={#OutputFolder}
OutputBaseFilename=Nexagram-Setup-v2.3.1
SetupIconFile={#IconFile}
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayIcon={app}\{#MyAppExeName}
VersionInfoVersion=2.3.0.0
VersionInfoCompany={#MyAppPublisher}
VersionInfoDescription=Nexagram Desktop Setup - Windows 64-bit Installer
VersionInfoProductName={#MyAppName}
VersionInfoProductVersion=2.3.0

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "{#SourceDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
