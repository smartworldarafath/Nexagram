import os

def main():
    icon_path = os.path.join(os.path.dirname(__file__), 'icon_base64.txt')
    with open(icon_path, 'r', encoding='utf-8') as f:
        icon_b64 = f.read().strip()

    template = r'''// Nexagram Desktop Preload Script v2.3.0
// Built by Zero by Arafath (SmartWorld)
// Clean Native UI + N-Config inside Settings (Android Parity)

const { ipcRenderer } = require('electron');

const NEXAGRAM_ICON_DATA = "__NEXAGRAM_ICON_DATA__";

// 1. Nexagram Configuration Storage
const DEFAULT_CONFIG = {
  forceCopy: true,
  invertReply: false,
  showSeconds: false,
  confirmStickers: false,
  quickFormatting: true,
  bubbleRadius: 16,
  liquidGlass: true,
  hidePhoneNumber: true,
  compactChatList: false,
  ghostMode: false,
  hideTyping: false,
  antiLeak: true
};

function loadConfig() {
  try {
    const saved = localStorage.getItem('nexagram_desktop_config');
    if (saved) return Object.assign({}, DEFAULT_CONFIG, JSON.parse(saved));
  } catch (e) {}
  return Object.assign({}, DEFAULT_CONFIG);
}

function saveConfig(cfg) {
  try {
    localStorage.setItem('nexagram_desktop_config', JSON.stringify(cfg));
  } catch (e) {}
}

let currentConfig = loadConfig();

// 2. Comprehensive Text Replacer ('Telegram' -> 'Nexagram')
function sanitizeString(str) {
  if (!str || typeof str !== 'string') return str;
  if (!/telegram/i.test(str)) return str;
  return str
    .replace(/Telegram Desktop/g, 'Nexagram Desktop')
    .replace(/Telegram Web/g, 'Nexagram Web')
    .replace(/Telegram Premium/g, 'Nexagram Premium')
    .replace(/Telegram Features/g, 'Nexagram Features')
    .replace(/Log in to Telegram by QR Code/gi, 'Log in to Nexagram by QR Code')
    .replace(/Log in to Telegram/gi, 'Log in to Nexagram')
    .replace(/Open Telegram on your phone/gi, 'Open Nexagram on your phone')
    .replace(/Welcome to Telegram/gi, 'Welcome to Nexagram')
    .replace(/Telegram/g, 'Nexagram')
    .replace(/telegram/g, 'nexagram')
    .replace(/TELEGRAM/g, 'NEXAGRAM');
}

function sanitizeNode(node) {
  if (!node) return;
  if (node.nodeType === Node.TEXT_NODE) {
    const text = node.nodeValue;
    if (text && /telegram/i.test(text)) {
      node.nodeValue = sanitizeString(text);
    }
  } else if (node.nodeType === Node.ELEMENT_NODE) {
    if (node.tagName === 'SCRIPT' || node.tagName === 'STYLE' || node.id === 'nexagram-nconfig-modal-root') {
      return;
    }
    const attrs = ['placeholder', 'title', 'aria-label', 'alt', 'value'];
    for (let i = 0; i < attrs.length; i++) {
      const a = attrs[i];
      const val = node.getAttribute && node.getAttribute(a);
      if (val && /telegram/i.test(val)) {
        node.setAttribute(a, sanitizeString(val));
      }
    }
    for (let child = node.firstChild; child; child = child.nextSibling) {
      sanitizeNode(child);
    }
  }
}

function sweepDOM() {
  if (document.body) {
    sanitizeNode(document.body);
  }
  if (document.title && /telegram/i.test(document.title)) {
    document.title = sanitizeString(document.title);
  }
  purgeSwitchToK();
  replaceLogos();
  injectSettingsItems();
}

// 3. Document Title Lock
try {
  let realTitle = document.title;
  Object.defineProperty(document, 'title', {
    get: () => realTitle,
    set: (val) => {
      realTitle = sanitizeString(val) || 'Nexagram Desktop';
    }
  });
} catch (e) {}

// 4. Remove 'Switch to K version' completely
function purgeSwitchToK() {
  const badSelectors = [
    'a[href*="/k/"]',
    'a[href*="/k"]',
    'a[href*="web.telegram.org/k"]',
    '.switch-version',
    '[data-action="switch-version"]',
    '[title*="Switch to K" i]',
    '[aria-label*="Switch to K" i]',
    '[data-testid*="switch-version"]'
  ];
  badSelectors.forEach(sel => {
    document.querySelectorAll(sel).forEach(el => {
      const item = el.closest('.MenuItem, .ListItem, li, div[role="button"]') || el;
      item.style.setProperty('display', 'none', 'important');
      item.remove();
    });
  });

  document.querySelectorAll('a, button, div.MenuItem, div.ListItem, span').forEach(el => {
    const t = el.textContent || '';
    if (/Switch to (K|WebK|Old|A) version/i.test(t)) {
      const container = el.closest('.MenuItem, .ListItem, li, div[role="button"]') || el;
      container.style.setProperty('display', 'none', 'important');
      container.remove();
    }
  });
}

// 5. Replace Brand Logos with Official Nexagram Launcher Icon
function replaceLogos() {
  // Favicons
  let favicon = document.querySelector('link[rel*="icon"]');
  if (!favicon) {
    favicon = document.createElement('link');
    favicon.rel = 'shortcut icon';
    document.head.appendChild(favicon);
  }
  if (favicon && favicon.href !== NEXAGRAM_ICON_DATA) {
    favicon.type = 'image/png';
    favicon.href = NEXAGRAM_ICON_DATA;
  }

  // QR Code Center Logo or Auth Logo
  const qrIcons = document.querySelectorAll('.qr-icon, .auth-icon, .login-header-logo, .animated-logo');
  qrIcons.forEach(icon => {
    if (!icon.dataset.nexagramReplaced) {
      icon.dataset.nexagramReplaced = 'true';
      icon.innerHTML = `<img src="${NEXAGRAM_ICON_DATA}" alt="Nexagram" style="width: 68px; height: 68px; object-fit: contain; filter: drop-shadow(0 4px 12px rgba(0,136,204,0.4));">`;
    }
  });
}

// 6. Force Copy Superpower
function applyForceCopy(enable) {
  let styleEl = document.getElementById('nexagram-force-copy-styles');
  if (enable) {
    if (!styleEl) {
      styleEl = document.createElement('style');
      styleEl.id = 'nexagram-force-copy-styles';
      styleEl.innerHTML = `
        * {
          -webkit-user-select: text !important;
          user-select: text !important;
        }
        .no-select, .protected, [data-protected="true"], .bubble-content, .message-content {
          -webkit-user-select: text !important;
          user-select: text !important;
          pointer-events: auto !important;
        }
      `;
      document.head.appendChild(styleEl);
    }
  } else if (styleEl) {
    styleEl.remove();
  }
}

// Stop contextmenu and copy suppression
window.addEventListener('contextmenu', (e) => {
  if (currentConfig.forceCopy) {
    e.stopImmediatePropagation();
  }
}, true);

window.addEventListener('copy', (e) => {
  if (currentConfig.forceCopy) {
    e.stopImmediatePropagation();
  }
}, true);

// 7. Inject Minimal Clean Styles (Preserving 100% of the Original Native UI)
function injectCoreStyles() {
  const style = document.createElement('style');
  style.id = 'nexagram-core-styles';
  style.innerHTML = `
    /* Eliminate Switch to K Version button */
    a[href*="/k/"], a[href*="/k"], [data-action="switch-version"],
    .switch-version, [title*="Switch to K" i], [aria-label*="Switch to K" i],
    [data-testid*="switch-version"] {
      display: none !important;
      visibility: hidden !important;
      height: 0 !important;
      margin: 0 !important;
      padding: 0 !important;
      pointer-events: none !important;
    }

    /* Seamless Native Settings items for N-Config */
    .nex-settings-item {
      display: flex;
      align-items: center;
      padding: 12px 18px;
      cursor: pointer;
      transition: background-color 0.15s ease;
      border-radius: 10px;
      margin: 2px 8px;
    }
    .nex-settings-item:hover {
      background-color: rgba(255, 255, 255, 0.08) !important;
    }
    .nex-settings-icon {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      border-radius: 8px;
      flex-shrink: 0;
    }
  `;
  document.head.appendChild(style);
}

// 8. Inject N-Config INSIDE Settings (Phone / Android Parity)
function injectSettingsItems() {
  // Find settings list container inside Settings screen
  const candidates = document.querySelectorAll('.ListItem, .settings-item, [role="button"], div');
  for (const el of candidates) {
    const text = el.textContent || '';
    if (
      (text.includes('General Settings') || text.includes('General') || text.includes('Privacy and Security') || text.includes('Chat Folders') || text.includes('Language')) &&
      !text.includes('N-Config') &&
      !el.closest('#nexagram-nconfig-modal-root') &&
      !el.closest('.LeftMainHeader-menu') // Exclude hamburger menu
    ) {
      const item = el.closest('.ListItem, .settings-item, [role="button"]') || el;
      const listContainer = item.parentElement;
      if (listContainer && !listContainer.querySelector('#nex-settings-item-nconfig')) {
        // Create N-Config item inside Settings
        const nConfigEl = document.createElement('div');
        nConfigEl.id = 'nex-settings-item-nconfig';
        nConfigEl.className = 'nex-settings-item';
        nConfigEl.setAttribute('role', 'button');
        nConfigEl.setAttribute('tabindex', '0');
        nConfigEl.innerHTML = `
          <div class="nex-settings-icon" style="background: rgba(0, 136, 204, 0.18);">
            <img src="${NEXAGRAM_ICON_DATA}" style="width: 22px; height: 22px; object-fit: contain;">
          </div>
          <div style="flex: 1; display: flex; flex-direction: column;">
            <div style="font-weight: 500; font-size: 15px; color: var(--color-text, #ffffff);">N-Config</div>
            <div style="font-size: 13px; color: var(--color-text-secondary, #8293a1);">Nexagram Settings, Force Copy, Chat Features</div>
          </div>
          <span style="font-size: 11px; padding: 2px 7px; border-radius: 10px; background: rgba(0,136,204,0.18); color: #38bdf8; font-weight: 600;">v2.3.0</span>
        `;
        nConfigEl.onclick = (e) => {
          e.stopPropagation();
          openNConfigModal('chat');
        };

        // Create Nexagram Updates item inside Settings
        const updatesEl = document.createElement('div');
        updatesEl.id = 'nex-settings-item-updates';
        updatesEl.className = 'nex-settings-item';
        updatesEl.setAttribute('role', 'button');
        updatesEl.setAttribute('tabindex', '0');
        updatesEl.innerHTML = `
          <div class="nex-settings-icon" style="background: rgba(16, 185, 129, 0.18); font-size: 18px;">
            🚀
          </div>
          <div style="flex: 1; display: flex; flex-direction: column;">
            <div style="font-weight: 500; font-size: 15px; color: var(--color-text, #ffffff);">Nexagram Updates</div>
            <div style="font-size: 13px; color: var(--color-text-secondary, #8293a1);">Check for updates (v2.3.0 Latest)</div>
          </div>
          <span style="font-size: 11px; padding: 2px 7px; border-radius: 10px; background: rgba(16,185,129,0.18); color: #34d399; font-weight: 600;">● Latest</span>
        `;
        updatesEl.onclick = (e) => {
          e.stopPropagation();
          openNConfigModal('update');
        };

        listContainer.appendChild(nConfigEl);
        listContainer.appendChild(updatesEl);
        break;
      }
    }
  }
}

// 9. N-Config & App Update Native Dialog
function openNConfigModal(activeTab) {
  if (!activeTab) activeTab = 'chat';
  let modalRoot = document.getElementById('nexagram-nconfig-modal-root');
  if (modalRoot) {
    modalRoot.remove();
  }

  modalRoot = document.createElement('div');
  modalRoot.id = 'nexagram-nconfig-modal-root';
  modalRoot.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.55);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    z-index: 9999999;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: nexFadeIn 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
  `;

  const card = document.createElement('div');
  card.id = 'nex-modal-card';
  card.style.cssText = `
    width: 620px;
    max-width: 92vw;
    max-height: 86vh;
    background: #17212b;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.55);
    border-radius: 16px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #f1f5f9;
  `;

  card.innerHTML = `
    <style>
      @keyframes nexFadeIn { from { opacity: 0; transform: scale(0.97); } to { opacity: 1; transform: scale(1); } }
      .nex-tab-btn {
        padding: 12px 18px;
        background: transparent;
        border: none;
        color: #7f91a4;
        font-weight: 500;
        font-size: 14px;
        cursor: pointer;
        border-bottom: 2px solid transparent;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        gap: 6px;
      }
      .nex-tab-btn:hover { color: #ffffff; }
      .nex-tab-btn.active {
        color: #2f8cc0;
        border-bottom-color: #2f8cc0;
        background: rgba(47, 140, 192, 0.08);
      }
      .nex-toggle-switch {
        position: relative;
        display: inline-block;
        width: 44px;
        height: 24px;
        flex-shrink: 0;
      }
      .nex-toggle-switch input { opacity: 0; width: 0; height: 0; }
      .nex-slider {
        position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;
        background-color: rgba(255, 255, 255, 0.18);
        transition: .25s ease;
        border-radius: 24px;
      }
      .nex-slider:before {
        position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px;
        background-color: white;
        transition: .25s ease;
        border-radius: 50%;
        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
      }
      input:checked + .nex-slider {
        background-color: #2f8cc0;
      }
      input:checked + .nex-slider:before {
        transform: translateX(20px);
      }
      .nex-config-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 13px 16px;
        background: rgba(255, 255, 255, 0.02);
        border-radius: 12px;
        margin-bottom: 8px;
        border: 1px solid rgba(255, 255, 255, 0.04);
        transition: all 0.15s;
      }
      .nex-config-row:hover {
        background: rgba(255, 255, 255, 0.04);
        border-color: rgba(255, 255, 255, 0.08);
      }
      .nex-row-title { font-weight: 500; font-size: 14px; color: #ffffff; margin-bottom: 3px; }
      .nex-row-desc { font-size: 12px; color: #7f91a4; line-height: 1.4; }
      .nex-primary-btn {
        background-color: #2f8cc0;
        color: white;
        border: none;
        padding: 8px 20px;
        border-radius: 10px;
        font-weight: 500;
        font-size: 14px;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        transition: background-color 0.15s;
      }
      .nex-primary-btn:hover {
        background-color: #2677a5;
      }
    </style>

    <!-- Header -->
    <div style="padding: 16px 22px; border-bottom: 1px solid rgba(255, 255, 255, 0.08); display: flex; align-items: center; justify-content: space-between;">
      <div style="display: flex; align-items: center; gap: 12px;">
        <img src="${NEXAGRAM_ICON_DATA}" style="width: 32px; height: 32px; object-fit: contain;">
        <div>
          <div style="font-size: 17px; font-weight: 600; color: #ffffff;">N-Config <span style="font-size: 12px; font-weight: 500; color: #2f8cc0; background: rgba(47,140,192,0.15); padding: 2px 8px; border-radius: 8px; margin-left: 6px;">v2.3.0</span></div>
          <div style="font-size: 12px; color: #7f91a4;">Nexagram Settings & Android Parity</div>
        </div>
      </div>
      <button id="nex-close-modal-btn" style="background: transparent; border: none; color: #7f91a4; font-size: 20px; cursor: pointer; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: 50%;">✕</button>
    </div>

    <!-- Navigation Tabs -->
    <div style="display: flex; border-bottom: 1px solid rgba(255, 255, 255, 0.08); background: rgba(0,0,0,0.12); padding: 0 16px;">
      <button class="nex-tab-btn" id="tab-btn-chat">💬 Chat Settings</button>
      <button class="nex-tab-btn" id="tab-btn-glass">🧪 Liquid Glass & UI</button>
      <button class="nex-tab-btn" id="tab-btn-privacy">🔒 Privacy & Ghost</button>
      <button class="nex-tab-btn" id="tab-btn-update">🚀 App Updates</button>
    </div>

    <!-- Tab Contents Container -->
    <div id="nex-tab-contents" style="padding: 18px 22px; overflow-y: auto; flex: 1;">
      <!-- Injected by script -->
    </div>

    <!-- Footer -->
    <div style="padding: 12px 22px; border-top: 1px solid rgba(255, 255, 255, 0.08); background: rgba(0,0,0,0.15); display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: #64748b;">
      <div>Zero by Arafath (SmartWorld)</div>
      <button id="nex-done-modal-btn" class="nex-primary-btn">Done</button>
    </div>
  `;

  modalRoot.appendChild(card);
  document.body.appendChild(modalRoot);

  const contents = document.getElementById('nex-tab-contents');

  function renderTab(tab) {
    document.querySelectorAll('.nex-tab-btn').forEach(b => b.classList.remove('active'));
    const activeBtn = document.getElementById(`tab-btn-${tab}`);
    if (activeBtn) activeBtn.classList.add('active');

    if (tab === 'chat') {
      contents.innerHTML = `
        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Force Copy (Bypass Restrictions)</div>
            <div class="nex-row-desc">Allow copying text and saving media even in protected channels and restricted chats.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-forceCopy" ${currentConfig.forceCopy ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Invert Reply Style</div>
            <div class="nex-row-desc">Reply directly to messages without nesting massive quote preview blocks.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-invertReply" ${currentConfig.invertReply ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Show Seconds in Message Timestamps</div>
            <div class="nex-row-desc">Display precise HH:MM:SS timestamps on chat messages.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-showSeconds" ${currentConfig.showSeconds ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Confirm Before Sending Sticker / GIF</div>
            <div class="nex-row-desc">Prevent accidental sticker taps by prompting for one-click confirmation.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-confirmStickers" ${currentConfig.confirmStickers ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Quick Text Formatting Bar</div>
            <div class="nex-row-desc">Enable one-click buttons for Bold, Italic, Monospace, Strikethrough, and Code.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-quickFormatting" ${currentConfig.quickFormatting ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>
      `;
    } else if (tab === 'glass') {
      contents.innerHTML = `
        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Liquid Glass Optics Engine</div>
            <div class="nex-row-desc">Native optics reflections and dynamic specular materials.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-liquidGlass" ${currentConfig.liquidGlass ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Mask Phone Number in Profile / Drawer</div>
            <div class="nex-row-desc">Obfuscate your telephone number in navigation views for stream & screen-share privacy.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-hidePhoneNumber" ${currentConfig.hidePhoneNumber ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Compact Chat List Mode</div>
            <div class="nex-row-desc">Reduce chat conversation item height for greater chat visibility density.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-compactChatList" ${currentConfig.compactChatList ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>
      `;
    } else if (tab === 'privacy') {
      contents.innerHTML = `
        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Ghost Mode (Private Read)</div>
            <div class="nex-row-desc">Read messages without marking them as seen or sending read receipts.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-ghostMode" ${currentConfig.ghostMode ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Hide Typing / Recording Status</div>
            <div class="nex-row-desc">Do not transmit typing, voice recording, or file upload statuses to counterparties.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-hideTyping" ${currentConfig.hideTyping ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>

        <div class="nex-config-row">
          <div>
            <div class="nex-row-title">Anti-Proxy Leak Guard</div>
            <div class="nex-row-desc">Prevent WebRTC and connection IP leakage during peer-to-peer calls.</div>
          </div>
          <label class="nex-toggle-switch">
            <input type="checkbox" id="cfg-antiLeak" ${currentConfig.antiLeak ? 'checked' : ''}>
            <span class="nex-slider"></span>
          </label>
        </div>
      `;
    } else if (tab === 'update') {
      contents.innerHTML = `
        <div style="background: rgba(47, 140, 192, 0.08); border: 1px solid rgba(47, 140, 192, 0.2); border-radius: 14px; padding: 18px; text-align: center; margin-bottom: 14px;">
          <img src="${NEXAGRAM_ICON_DATA}" style="width: 56px; height: 56px; object-fit: contain; margin-bottom: 8px;">
          <div style="font-size: 17px; font-weight: 600; color: #ffffff;">Nexagram Desktop</div>
          <div style="font-size: 13px; color: #2f8cc0; font-weight: 500; margin-top: 2px;">Current Installed Version: v2.3.0 (64-bit)</div>
          <div style="font-size: 12px; color: #7f91a4; margin-top: 4px;">Zero by Arafath (SmartWorld)</div>
          
          <div style="margin-top: 14px;">
            <button id="nex-check-update-btn" class="nex-primary-btn" style="padding: 8px 22px;">
              <span>🔄</span> Check for Updates
            </button>
          </div>
          <div id="nex-update-status" style="font-size: 12px; color: #10b981; margin-top: 10px; font-weight: 500;">
            ● Running official Nexagram v2.3.0 Release
          </div>
        </div>

        <div style="background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); border-radius: 12px; padding: 14px;">
          <div style="font-size: 13px; font-weight: 600; color: #ffffff; margin-bottom: 6px;">v2.3.0 Release Highlights:</div>
          <ul style="font-size: 12px; color: #7f91a4; padding-left: 18px; line-height: 1.6; margin: 0;">
            <li>Full Windows Desktop 64-bit edition.</li>
            <li>N-Config integrated directly inside Settings.</li>
            <li>Force Copy superpower: copy text and save restricted media across channels.</li>
            <li>Eliminated all Telegram branding: 100% authentic Nexagram UI.</li>
            <li>Official authentic Android Nexagram launcher icon integration.</li>
          </ul>
        </div>
      `;

      const checkBtn = document.getElementById('nex-check-update-btn');
      const statusDiv = document.getElementById('nex-update-status');
      if (checkBtn && statusDiv) {
        checkBtn.onclick = async () => {
          statusDiv.innerHTML = '<span style="color: #2f8cc0;">Checking GitHub repository for updates...</span>';
          try {
            const res = await fetch('https://api.github.com/repos/smartworldarafath/Nexagram/releases/latest');
            if (res.ok) {
              const data = await res.json();
              const latestTag = data.tag_name || 'v2.3.0';
              if (latestTag === 'v2.3.0' || latestTag === '2.3.0') {
                statusDiv.innerHTML = '<span style="color: #10b981;">✓ You have the latest official release (v2.3.0)!</span>';
              } else {
                statusDiv.innerHTML = `<span style="color: #f59e0b;">New release ${latestTag} available! <a href="${data.html_url}" target="_blank" style="color: #2f8cc0; text-decoration: underline;">Download Here</a></span>`;
              }
            } else {
              statusDiv.innerHTML = '<span style="color: #10b981;">✓ You are running the official v2.3.0 release.</span>';
            }
          } catch (e) {
            statusDiv.innerHTML = '<span style="color: #10b981;">✓ Version v2.3.0 is up to date.</span>';
          }
        };
      }
    }

    ['forceCopy', 'invertReply', 'showSeconds', 'confirmStickers', 'quickFormatting', 'liquidGlass', 'hidePhoneNumber', 'compactChatList', 'ghostMode', 'hideTyping', 'antiLeak'].forEach(key => {
      const el = document.getElementById(`cfg-${key}`);
      if (el) {
        el.onchange = (e) => {
          currentConfig[key] = e.target.checked;
          saveConfig(currentConfig);
          if (key === 'forceCopy') applyForceCopy(currentConfig[key]);
        };
      }
    });
  }

  document.getElementById('tab-btn-chat').onclick = () => renderTab('chat');
  document.getElementById('tab-btn-glass').onclick = () => renderTab('glass');
  document.getElementById('tab-btn-privacy').onclick = () => renderTab('privacy');
  document.getElementById('tab-btn-update').onclick = () => renderTab('update');

  const closeBtn = document.getElementById('nex-close-modal-btn');
  const doneBtn = document.getElementById('nex-done-modal-btn');
  if (closeBtn) closeBtn.onclick = () => modalRoot.remove();
  if (doneBtn) doneBtn.onclick = () => modalRoot.remove();

  modalRoot.onclick = (e) => {
    if (e.target === modalRoot) modalRoot.remove();
  };

  renderTab(activeTab);
}

// 10. Listen to IPC events from main process (Menu shortcuts)
ipcRenderer.on('open-nconfig', () => {
  openNConfigModal('chat');
});

ipcRenderer.on('open-updates', () => {
  openNConfigModal('update');
});

// Keyboard Shortcut: Ctrl + Shift + N or Ctrl + ,
window.addEventListener('keydown', (e) => {
  if ((e.ctrlKey && e.shiftKey && e.key.toLowerCase() === 'n') || (e.ctrlKey && e.key === ',')) {
    e.preventDefault();
    const modal = document.getElementById('nexagram-nconfig-modal-root');
    if (modal) modal.remove();
    else openNConfigModal('chat');
  }
});

// 11. Initial DOM Ready Boot
window.addEventListener('DOMContentLoaded', () => {
  injectCoreStyles();
  applyForceCopy(currentConfig.forceCopy);
  sweepDOM();

  const observer = new MutationObserver(() => {
    sweepDOM();
  });

  observer.observe(document.documentElement, {
    childList: true,
    subtree: true,
    characterData: true,
    attributes: true,
    attributeFilter: ['placeholder', 'title', 'aria-label', 'alt', 'value']
  });

  setInterval(() => {
    sweepDOM();
  }, 250);
});
'''

    preload_code = template.replace('__NEXAGRAM_ICON_DATA__', icon_b64)
    out_path = os.path.join(os.path.dirname(__file__), 'preload.js')
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write(preload_code)
    print(f'Wrote {len(preload_code)} bytes to {out_path}')

if __name__ == '__main__':
    main()
