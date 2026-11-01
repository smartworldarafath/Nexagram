// Nexagram Website JavaScript
// Platform Detection & Dynamic GitHub Release Info

(function () {
  'use strict';

  function detectPlatform() {
    const ua = navigator.userAgent;
    if (/Android/i.test(ua)) return 'android';
    if (/iPhone|iPad|iPod/i.test(ua) || (/Macintosh/i.test(ua) && navigator.maxTouchPoints > 1)) return 'ios';
    if (/Windows/i.test(ua)) return 'windows';
    if (/Macintosh|Mac OS X/i.test(ua)) return 'macos';
    if (/Linux|X11/i.test(ua)) return 'linux';
    return null;
  }

  function highlightPlatform() {
    const p = detectPlatform();
    if (!p) return;
    const tile = document.querySelector(`.hero-downloads [data-dl="${p}"]`);
    if (tile) {
      tile.classList.add('is-detected');
      const note = document.getElementById('detected-note');
      if (note) note.hidden = false;
    }
  }

  async function fetchLatestRelease() {
    const defaultVer = 'v2.3.1';
    const versionElements = document.querySelectorAll('[data-version]');
    
    // Set fallback version immediately
    versionElements.forEach(el => {
      el.textContent = defaultVer;
      el.dataset.status = 'ok';
    });

    try {
      const res = await fetch('https://api.github.com/repos/smartworldarafath/Nexagram/releases/latest');
      if (!res.ok) return;
      const data = await res.json();
      const latestTag = data.tag_name || defaultVer;

      versionElements.forEach(el => {
        el.textContent = latestTag;
        el.dataset.status = 'ok';
      });

      // Update direct download links if assets available
      if (data.assets && Array.isArray(data.assets)) {
        let apkUrl = null, exeUrl = null, zipUrl = null;
        for (const asset of data.assets) {
          if (asset.name.endsWith('.apk')) apkUrl = asset.browser_download_url;
          if (asset.name.endsWith('.exe')) exeUrl = asset.browser_download_url;
          if (asset.name.endsWith('.zip')) zipUrl = asset.browser_download_url;
        }

        if (apkUrl) {
          document.querySelectorAll('a[data-dl="android"]').forEach(a => a.href = apkUrl);
        }
        if (exeUrl) {
          document.querySelectorAll('a[data-dl="windows"]').forEach(a => a.href = exeUrl);
        }
        if (zipUrl) {
          document.querySelectorAll('a[data-dl="windows-zip"]').forEach(a => a.href = zipUrl);
        }
      }
    } catch (e) {
      // Graceful fallback to v2.3.1
      versionElements.forEach(el => {
        el.textContent = defaultVer;
        el.dataset.status = 'ok';
      });
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    highlightPlatform();
    fetchLatestRelease();
  });
})();
