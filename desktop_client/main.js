const { app, BrowserWindow, Menu, shell, ipcMain } = require('electron');
const path = require('path');

let mainWindow;

// Enforce single instance
const gotTheLock = app.requestSingleInstanceLock();
if (!gotTheLock) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.focus();
    }
  });
}

function createWindow() {
  const iconPath = path.join(__dirname, 'nexagram.ico');

  mainWindow = new BrowserWindow({
    width: 1240,
    height: 820,
    minWidth: 420,
    minHeight: 520,
    title: 'Nexagram Desktop',
    icon: iconPath,
    backgroundColor: '#0c131d',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: false,
      spellcheck: true
    }
  });

  // Explicitly set window icon
  mainWindow.setIcon(iconPath);

  // Set desktop user agent
  const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Nexagram/2.3.1';
  mainWindow.webContents.setUserAgent(userAgent);

  // Load Telegram Web A (Responsive modern UI)
  mainWindow.loadURL('https://web.telegram.org/a/');

  // Force clean title
  mainWindow.webContents.on('page-title-updated', (event, title) => {
    event.preventDefault();
    const cleanTitle = (title || 'Nexagram').replace(/Telegram/gi, 'Nexagram');
    mainWindow.setTitle(cleanTitle || 'Nexagram Desktop');
  });

  // Open external links safely in system browser
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith('https://web.telegram.org') || url.startsWith('https://t.me')) {
      return { action: 'allow' };
    }
    shell.openExternal(url);
    return { action: 'deny' };
  });

  // IPC Handlers
  ipcMain.handle('open-external', (e, url) => {
    if (url) shell.openExternal(url);
  });

  // Application Menu
  const menuTemplate = [
    {
      label: 'Nexagram',
      submenu: [
        {
          label: 'N-Config (Preferences)',
          accelerator: 'CmdOrCtrl+,',
          click: () => {
            mainWindow.webContents.send('open-nconfig');
          }
        },
        {
          label: 'Check for Updates...',
          click: () => {
            mainWindow.webContents.send('open-updates');
          }
        },
        { type: 'separator' },
        {
          label: 'About Nexagram Desktop v2.3.1',
          click: () => {
            const { dialog } = require('electron');
            dialog.showMessageBox(mainWindow, {
              type: 'info',
              title: 'About Nexagram Desktop',
              message: 'Nexagram Desktop v2.3.1\nOfficial Windows 64-bit Edition',
              detail: 'Crafted with Liquid Glass Optics & Full Android Feature Parity.\nIncludes N-Config, Force Copy, Privacy & Custom Chat Settings.\nDeveloper: Zero by Arafath (SmartWorld).\nRepository: https://github.com/smartworldarafath/Nexagram',
              buttons: ['OK']
            });
          }
        },
        { type: 'separator' },
        { label: 'Reload', accelerator: 'CmdOrCtrl+R', click: () => mainWindow.reload() },
        { label: 'Force Reload', accelerator: 'CmdOrCtrl+Shift+R', click: () => mainWindow.webContents.reloadIgnoringCache() },
        { type: 'separator' },
        { label: 'Quit Nexagram', accelerator: 'CmdOrCtrl+Q', click: () => app.quit() }
      ]
    },
    {
      label: 'Edit',
      submenu: [
        { label: 'Undo', role: 'undo' },
        { label: 'Redo', role: 'redo' },
        { type: 'separator' },
        { label: 'Cut', role: 'cut' },
        { label: 'Copy', role: 'copy' },
        { label: 'Paste', role: 'paste' },
        { label: 'Select All', role: 'selectAll' }
      ]
    },
    {
      label: 'View',
      submenu: [
        { label: 'Zoom In', role: 'zoomIn' },
        { label: 'Zoom Out', role: 'zoomOut' },
        { label: 'Reset Zoom', role: 'resetZoom' },
        { type: 'separator' },
        { label: 'Toggle Full Screen', role: 'togglefullscreen' },
        { label: 'Toggle Developer Tools', accelerator: 'F12', click: () => mainWindow.webContents.toggleDevTools() }
      ]
    },
    {
      label: 'Help',
      submenu: [
        { label: 'GitHub Repository', click: () => shell.openExternal('https://github.com/smartworldarafath/Nexagram') },
        { label: 'Release Notes (v2.3.1)', click: () => shell.openExternal('https://github.com/smartworldarafath/Nexagram/releases/tag/v2.3.1') },
        { label: 'Report Issue', click: () => shell.openExternal('https://github.com/smartworldarafath/Nexagram/issues') }
      ]
    }
  ];

  const menu = Menu.buildFromTemplate(menuTemplate);
  Menu.setApplicationMenu(menu);

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

app.whenReady().then(() => {
  app.setAppUserModelId('com.smartworld.nexagram');
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
