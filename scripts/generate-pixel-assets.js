const fs = require('fs');
const path = require('path');

// Output directories
const darkDir = path.join(__dirname, '..', 'assets', 'readme', 'dark');
const lightDir = path.join(__dirname, '..', 'assets', 'readme', 'light');
const pubDarkDir = path.join(__dirname, '..', 'public', 'assets', 'readme', 'dark');
const pubLightDir = path.join(__dirname, '..', 'public', 'assets', 'readme', 'light');

for (const d of [darkDir, lightDir, pubDarkDir, pubLightDir]) {
  fs.mkdirSync(d, { recursive: true });
}

// 12-point scalloped Material 3 Expressive flower path (precisely centered at 0, 0)
const CENTERED_FLOWER_PATH = "M24.6,5.0C24.2,5.5 23.8,5.9 23.4,6.4C23.1,6.9 22.7,7.3 22.5,7.8C22.2,8.3 22.0,8.7 21.8,9.2C21.7,9.8 21.6,10.3 21.5,10.9C21.4,11.4 21.4,12.0 21.4,12.6C21.3,13.2 21.3,13.9 21.2,14.5C21.1,15.1 20.9,15.7 20.7,16.3C20.4,16.8 20.1,17.3 19.7,17.8C19.3,18.2 18.9,18.6 18.4,18.9C17.8,19.2 17.2,19.4 16.7,19.6C16.1,19.8 15.4,19.9 14.8,20.0C14.2,20.2 13.6,20.3 13.1,20.4C12.5,20.5 12.0,20.7 11.5,20.9C11.0,21.1 10.6,21.4 10.2,21.7C9.7,22.0 9.3,22.4 8.9,22.9C8.5,23.3 8.1,23.7 7.6,24.2C7.2,24.6 6.7,25.0 6.2,25.4C5.7,25.8 5.2,26.1 4.7,26.3C4.1,26.5 3.5,26.6 2.9,26.7C2.3,26.7 1.7,26.6 1.1,26.4C0.6,26.3 -0.0,26.0 -0.6,25.8C-1.3,25.5 -1.8,25.2 -2.3,24.9C-2.8,24.6 -3.3,24.3 -3.8,24.1C-4.3,23.9 -4.8,23.7 -5.3,23.6C-5.8,23.5 -6.4,23.5 -6.9,23.5C-7.5,23.5 -8.0,23.5 -8.6,23.6C-9.2,23.6 -9.9,23.7 -10.5,23.8C-11.1,23.8 -11.7,23.8 -12.3,23.7C-12.9,23.6 -13.5,23.5 -14.1,23.2C-14.6,23.0 -15.1,22.6 -15.5,22.2C-15.9,21.8 -16.3,21.3 -16.6,20.8C-17.0,20.2 -17.2,19.6 -17.4,19.1C-17.7,18.5 -17.9,17.9 -18.1,17.4C-18.3,16.8 -18.5,16.3 -18.8,15.9C-19.1,15.4 -19.4,15.0 -19.8,14.6C-20.1,14.2 -20.5,13.8 -21.0,13.5C-21.5,13.1 -22.0,12.8 -22.5,12.5C-23.0,12.1 -23.5,11.8 -24.0,11.4C-24.4,11.0 -24.9,10.5 -25.3,10.1C-25.6,9.6 -25.9,9.0 -26.1,8.5C-26.3,7.9 -26.4,7.3 -26.4,6.7C-26.4,6.1 -26.3,5.5 -26.1,4.9C-26.0,4.3 -25.8,3.7 -25.6,3.1C-25.4,2.5 -25.2,2.0 -25.1,1.4C-24.9,0.9 -24.8,0.3 -24.7,-0.2C-24.7,-0.7 -24.7,-1.2 -24.7,-1.8C-24.8,-2.3 -24.9,-2.9 -25.1,-3.4C-25.2,-4.0 -25.4,-4.6 -25.6,-5.2C-25.7,-5.8 -25.9,-6.4 -26.0,-7.0C-26.0,-7.6 -26.0,-8.3 -25.9,-8.8C-25.8,-9.4 -25.7,-10.0 -25.4,-10.5C-25.1,-11.0 -24.7,-11.5 -24.3,-11.9C-23.9,-12.4 -23.4,-12.8 -22.9,-13.1C-22.4,-13.5 -21.8,-13.8 -21.3,-14.1C-20.8,-14.4 -20.3,-14.7 -19.8,-15.0C-19.4,-15.4 -19.0,-15.7 -18.6,-16.1C-18.3,-16.5 -18.0,-17.0 -17.7,-17.4C-17.4,-17.9 -17.2,-18.5 -17.0,-19.0C-16.7,-19.5 -16.5,-20.1 -16.2,-20.7C-15.9,-21.2 -15.6,-21.8 -15.2,-22.3C-14.8,-22.7 -14.4,-23.2 -13.9,-23.5C-13.4,-23.8 -12.9,-24.1 -12.3,-24.3C-11.7,-24.4 -11.1,-24.5 -10.5,-24.5C-9.9,-24.5 -9.2,-24.4 -8.6,-24.4C-8.0,-24.3 -7.4,-24.2 -6.8,-24.1C-6.3,-24.0 -5.8,-23.9 -5.3,-23.9C-4.6,-23.9 -4.1,-24.0 -3.6,-24.1C-3.1,-24.2 -2.5,-24.4 -2.0,-24.7C-1.5,-24.9 -1.0,-25.2 -0.4,-25.4C0.1,-25.7 0.7,-26.0 1.3,-26.2C1.9,-26.4 2.5,-26.6 3.1,-26.6C3.7,-26.7 4.3,-26.7 4.9,-26.6C5.4,-26.4 6.0,-26.2 6.5,-25.9C7.0,-25.6 7.5,-25.2 8.0,-24.8C8.4,-24.4 8.9,-23.9 9.3,-23.5C9.7,-23.0 10.1,-22.5 10.5,-22.1C10.9,-21.7 11.2,-21.3 11.7,-21.0C12.1,-20.7 12.6,-20.5 13.1,-20.2C13.6,-20.0 14.1,-19.9 14.7,-19.7C15.2,-19.6 15.9,-19.5 16.4,-19.3C17.0,-19.1 17.7,-18.9 18.2,-18.7C18.8,-18.4 19.3,-18.1 19.8,-17.8C20.2,-17.4 20.6,-16.9 21.0,-16.4C21.3,-15.9 21.5,-15.4 21.7,-14.8C21.8,-14.2 21.9,-13.6 21.9,-13.0C22.0,-12.3 22.0,-11.7 22.0,-11.1C22.0,-10.5 22.0,-9.9 22.1,-9.4C22.2,-8.8 22.3,-8.3 22.4,-7.8C22.6,-7.3 22.8,-6.8 23.1,-6.3C23.4,-5.9 23.7,-5.4 24.1,-4.9C24.4,-4.4 24.8,-3.9 25.2,-3.4C25.5,-2.9 25.8,-2.3 26.0,-1.7C26.3,-1.2 26.4,-0.6 26.5,0.0C26.5,0.6 26.5,1.2 26.3,1.8C26.2,2.3 25.9,2.9 25.5,3.4C25.3,4.0 24.9,4.5 24.6,5.0Z";

// Helper to write file to both assets/readme and public/assets/readme
function saveSvg(subPath, content) {
  const p1 = path.join(__dirname, '..', 'assets', 'readme', subPath);
  const p2 = path.join(__dirname, '..', 'public', 'assets', 'readme', subPath);
  fs.mkdirSync(path.dirname(p1), { recursive: true });
  fs.mkdirSync(path.dirname(p2), { recursive: true });
  fs.writeFileSync(p1, content);
  fs.writeFileSync(p2, content);
}

// Explicit Path Icons without currentColor
const ICONS = {
  download: (c) => `<path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" fill="${c}"/>`,
  github: (c) => `<path fill-rule="evenodd" clip-rule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" fill="${c}"/>`,
  heart: (c) => `<path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="${c}"/>`,
  telegram: (c) => `<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 0 0-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.75-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z" fill="${c}"/>`,
  android: (c) => `<path d="M17.52 15.34c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm-11.05 0c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm11.41-6.02l2-3.46a.42.42 0 0 0-.15-.57.42.42 0 0 0-.57.15l-2.02 3.5A9.85 9.85 0 0 0 12 8.1c-1.85 0-3.59.31-5.14.85L4.84 5.45a.42.42 0 0 0-.57-.15.42.42 0 0 0-.15.57l2 3.46C2.69 11.19.34 14.66 0 18.76h24c-.34-4.1-2.69-7.57-6.12-9.44z" fill="${c}"/>`,
  rocket: (c) => `<path d="M12 2.5s4 1.5 6 6.5c1.2 3-.5 6-1.5 7.5L18 20l-3.5-1.5c-1.5 1-4.5 2.7-7.5 1.5-5-2-6.5-6-6.5-6s3.5-1.5 6.5-1.5c.5-1 1.5-2.5 3-4 1.5-1.5 3-3.5 3-6z" fill="${c}"/>`,
  sparkles: (c) => `<path d="M9 3L7.5 7.5 3 9l4.5 1.5L9 15l1.5-4.5L15 9l-4.5-1.5z M19 11l-1 3-3 1 3 1 1 3 1-3 3-1-3-1z M17 3l-.6 1.8L14.6 5.4l1.8.6.6 1.8.6-1.8 1.8-.6-1.8-.6z" fill="${c}"/>`,
  shield: (c) => `<path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" fill="${c}"/>`,
  lock: (c) => `<path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" fill="${c}"/>`,
  code: (c) => `<path d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z" fill="${c}"/>`,
  contribute: (c) => `<path d="M18 16c-.79 0-1.5.31-2.03.81L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.53.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.51 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.51-.31 2.04-.81l7.05 4.12c-.05.22-.09.45-.09.69 0 1.66 1.34 3 3 3s3-1.34 3-3-1.34-3-3-3z" fill="${c}"/>`,
  license: (c) => `<path d="M12 3L2 12h3v8h14v-8h3L12 3zm0 4.5l5 4.5H7l5-4.5zM12 18a2 2 0 1 1 0-4 2 2 0 0 1 0 4z" fill="${c}"/>`,
  terminal: (c) => `<path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 14H4V8h16v10zm-12-3l-1.4-1.4L8.8 11 6.6 8.8 8 7.4l3.6 3.6L8 15zm5 0h6v-2h-6v2z" fill="${c}"/>`,
  palette: (c) => `<path d="M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L4.35 19.4c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0l1.9-1.9C9.23 19.59 10.57 20 12 20c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-5 9c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm3-4c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm4 0c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm3 4c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z" fill="${c}"/>`,
  chat: (c) => `<path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z" fill="${c}"/>`,
  devices: (c) => `<path d="M4 6h16v10H4V6zm18 12H2v2h20v-2zm-6-9h4v6h-4V9z" fill="${c}"/>`
};

function renderIcon(name, size, color) {
  const iconFn = ICONS[name] || ICONS.sparkles;
  const iconSvg = iconFn(color);
  const scale = size / 24;
  const offset = -size / 2;
  return `<g transform="translate(${offset}, ${offset}) scale(${scale})">${iconSvg}</g>`;
}

// -------------------------------------------------------------
// 1. ROTATING PIXEL BADGE (Height: 28, Flower Center: 14, 14)
// -------------------------------------------------------------
function generateBadge({ name, label, icon, flowerDark, flowerLight, iconDark, iconLight, width }) {
  // DARK with active 8s SMIL rotation + fallback CSS
  const darkSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="28" viewBox="0 0 ${width} 28">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 8s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M14,0H${width - 14}A14,14 0 0 1 ${width},14V14A14,14 0 0 1 ${width - 14},28H14A14,14 0 0 1 0,14V14A14,14 0 0 1 14,0Z" fill="#221b24" stroke="#362938" stroke-width="1"/>
    <g transform="translate(14, 14)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="8s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerDark}" transform="scale(0.44)"/>
      </g>
      ${renderIcon(icon, 11, iconDark)}
    </g>
    <text x="27" y="18" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="11.5" font-weight="600" fill="#f0e2ee">${label}</text>
  </svg>`;
  saveSvg(path.join('dark', `badge-${name}.svg`), darkSvg);

  // LIGHT
  const lightSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="28" viewBox="0 0 ${width} 28">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 8s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M14,0H${width - 14}A14,14 0 0 1 ${width},14V14A14,14 0 0 1 ${width - 14},28H14A14,14 0 0 1 0,14V14A14,14 0 0 1 14,0Z" fill="#f0e5ef" stroke="#ded0dd" stroke-width="1"/>
    <g transform="translate(14, 14)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="8s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerLight}" transform="scale(0.44)"/>
      </g>
      ${renderIcon(icon, 11, iconLight)}
    </g>
    <text x="27" y="18" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="11.5" font-weight="600" fill="#261b28">${label}</text>
  </svg>`;
  saveSvg(path.join('light', `badge-${name}.svg`), lightSvg);
}

// -------------------------------------------------------------
// 2. ROTATING ACTION BUTTON (Height: 68, Flower Center: 38, 34)
// -------------------------------------------------------------
function generateButton({ name, sublabel, label, icon, flowerDark, flowerLight, iconDark, iconLight, bgDark, bgLight, textDark, textLight, width = 216 }) {
  // DARK with active 10s SMIL rotation + fallback CSS
  const darkSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="68" viewBox="0 0 ${width} 68">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 10s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M34,1H${width - 34}A33,33 0 0 1 ${width - 1},34V34A33,33 0 0 1 ${width - 34},67H34A33,33 0 0 1 1,34V34A33,33 0 0 1 34,1Z" fill="${bgDark || '#261e27'}" stroke="#3d2f3e" stroke-width="1"/>
    <g transform="translate(38, 34)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="10s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerDark}" transform="scale(1.05)" />
      </g>
      ${renderIcon(icon, 24, iconDark)}
    </g>
    <text x="74" y="27" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="10" font-weight="700" fill="${textDark ? textDark + '99' : '#caa7cd'}" letter-spacing="0.08em">${sublabel.toUpperCase()}</text>
    <text x="74" y="47" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="16" font-weight="700" fill="${textDark || '#ffffff'}">${label}</text>
  </svg>`;
  saveSvg(path.join('dark', `btn-${name}.svg`), darkSvg);

  // LIGHT
  const lightSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="68" viewBox="0 0 ${width} 68">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 10s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M34,1H${width - 34}A33,33 0 0 1 ${width - 1},34V34A33,33 0 0 1 ${width - 34},67H34A33,33 0 0 1 1,34V34A33,33 0 0 1 34,1Z" fill="${bgLight || '#f1e6f0'}" stroke="#dccbda" stroke-width="1"/>
    <g transform="translate(38, 34)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="10s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerLight}" transform="scale(1.05)" />
      </g>
      ${renderIcon(icon, 24, iconLight)}
    </g>
    <text x="74" y="27" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="10" font-weight="700" fill="${textLight ? textLight + '99' : '#695168'}" letter-spacing="0.08em">${sublabel.toUpperCase()}</text>
    <text x="74" y="47" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="16" font-weight="700" fill="${textLight || '#211824'}">${label}</text>
  </svg>`;
  saveSvg(path.join('light', `btn-${name}.svg`), lightSvg);
}

// -------------------------------------------------------------
// 3. ROTATING HEADER BANNER (1000 x 104, Flower Center: 56, 52)
// -------------------------------------------------------------
function generateHeader({ name, title, subtitle, icon, tags = [], flowerDark, flowerLight, iconDark, iconLight }) {
  function renderTags(tagsList, isDark) {
    let x = 970;
    let out = '';
    const pillBg = isDark ? '#3d2e3f' : '#ddcedd';
    const textColor = isDark ? '#ffffff' : '#221824';
    
    for (let i = tagsList.length - 1; i >= 0; i--) {
      const t = tagsList[i];
      const pillWidth = t.length * 8 + 26;
      x -= pillWidth;
      out += `<g transform="translate(${x}, 36)">
        <rect width="${pillWidth}" height="32" rx="16" fill="${pillBg}"/>
        <text x="${pillWidth / 2}" y="20.5" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="13" font-weight="700" fill="${textColor}" text-anchor="middle">${t}</text>
      </g>`;
      x -= 10;
    }
    return out;
  }

  // DESKTOP DARK
  const dDark = `<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="104" viewBox="0 0 1000 104">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 12s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M52,0H948A52,52 0 0 1 1000,52V52A52,52 0 0 1 948,104H52A52,52 0 0 1 0,52V52A52,52 0 0 1 52,0Z" fill="#231b24"/>
    <g transform="translate(56, 52)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="12s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerDark}" transform="scale(1.52)" />
      </g>
      ${renderIcon(icon, 36, iconDark)}
    </g>
    <text x="114" y="47" font-family="system-ui, -apple-system, 'Google Sans', Roboto, sans-serif" font-size="32" font-weight="700" fill="#ffffff">${title}</text>
    <text x="114" y="74" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="15" font-weight="400" fill="#cbb4ca">${subtitle}</text>
    ${renderTags(tags, true)}
  </svg>`;
  saveSvg(path.join('dark', `h-${name}.svg`), dDark);

  // DESKTOP LIGHT
  const dLight = `<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="104" viewBox="0 0 1000 104">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 12s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M52,0H948A52,52 0 0 1 1000,52V52A52,52 0 0 1 948,104H52A52,52 0 0 1 0,52V52A52,52 0 0 1 52,0Z" fill="#ede2ed"/>
    <g transform="translate(56, 52)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="12s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerLight}" transform="scale(1.52)" />
      </g>
      ${renderIcon(icon, 36, iconLight)}
    </g>
    <text x="114" y="47" font-family="system-ui, -apple-system, 'Google Sans', Roboto, sans-serif" font-size="32" font-weight="700" fill="#211824">${title}</text>
    <text x="114" y="74" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="15" font-weight="400" fill="#675466">${subtitle}</text>
    ${renderTags(tags, false)}
  </svg>`;
  saveSvg(path.join('light', `h-${name}.svg`), dLight);

  // MOBILE RESPONSIVE VERSION (640 x 90, Flower Center: 46, 45)
  const mDark = `<svg xmlns="http://www.w3.org/2000/svg" width="640" height="90" viewBox="0 0 640 90">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 12s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M45,0H595A45,45 0 0 1 640,45V45A45,45 0 0 1 595,90H45A45,45 0 0 1 0,45V45A45,45 0 0 1 45,0Z" fill="#231b24"/>
    <g transform="translate(46, 45)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="12s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerDark}" transform="scale(1.28)" />
      </g>
      ${renderIcon(icon, 30, iconDark)}
    </g>
    <text x="96" y="42" font-family="system-ui, -apple-system, 'Google Sans', Roboto, sans-serif" font-size="25" font-weight="700" fill="#ffffff">${title}</text>
    <text x="96" y="66" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="13" font-weight="400" fill="#cbb4ca">${subtitle}</text>
  </svg>`;
  saveSvg(path.join('dark', `hm-${name}.svg`), mDark);

  const mLight = `<svg xmlns="http://www.w3.org/2000/svg" width="640" height="90" viewBox="0 0 640 90">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 12s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M45,0H595A45,45 0 0 1 640,45V45A45,45 0 0 1 595,90H45A45,45 0 0 1 0,45V45A45,45 0 0 1 45,0Z" fill="#ede2ed"/>
    <g transform="translate(46, 45)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="12s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerLight}" transform="scale(1.28)" />
      </g>
      ${renderIcon(icon, 30, iconLight)}
    </g>
    <text x="96" y="42" font-family="system-ui, -apple-system, 'Google Sans', Roboto, sans-serif" font-size="25" font-weight="700" fill="#211824">${title}</text>
    <text x="96" y="66" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="13" font-weight="400" fill="#675466">${subtitle}</text>
  </svg>`;
  saveSvg(path.join('light', `hm-${name}.svg`), mLight);
}

// -------------------------------------------------------------
// 4. ROTATING FEATURE CARD (430 x 218, Flower Center: 64, 60)
// -------------------------------------------------------------
function generateCard({ name, title, lines, icon, flowerDark, flowerLight, iconDark, iconLight }) {
  const tLines = lines.map((l, i) => `<tspan x="36" dy="${i === 0 ? 0 : 20}">${l}</tspan>`).join('');

  // DARK
  const darkSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="430" height="218" viewBox="0 0 430 218">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 12s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <clipPath id="c_${name}"><rect width="430" height="218" rx="32" /></clipPath>
    <rect width="430" height="218" rx="32" fill="#1b131c" stroke="#362938" stroke-width="1"/>
    <g clip-path="url(#c_${name})">
      <path d="M440,110 C420,115 390,125 375,115 C360,105 340,65 315,55 C295,47 285,15 305,-5 C325,-25 355,-55 390,-55 C430,-55 470,20 475,50 C480,80 455,105 440,110 Z" fill="#261d28" />
    </g>
    <g transform="translate(64, 60)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="12s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerDark}" transform="scale(1.18)" />
      </g>
      ${renderIcon(icon, 30, iconDark)}
    </g>
    <text x="36" y="125" font-family="system-ui, -apple-system, 'Google Sans', Roboto, sans-serif" font-size="21" font-weight="700" fill="#ffffff">${title}</text>
    <text x="36" y="152" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="13.5" font-weight="400" fill="#beabbe">
      ${tLines}
    </text>
  </svg>`;
  saveSvg(path.join('dark', `card-${name}.svg`), darkSvg);

  // LIGHT
  const lightSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="430" height="218" viewBox="0 0 430 218">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 12s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <clipPath id="cl_${name}"><rect width="430" height="218" rx="32" /></clipPath>
    <rect width="430" height="218" rx="32" fill="#f4ebf4" stroke="#e0cee0" stroke-width="1"/>
    <g clip-path="url(#cl_${name})">
      <path d="M440,110 C420,115 390,125 375,115 C360,105 340,65 315,55 C295,47 285,15 305,-5 C325,-25 355,-55 390,-55 C430,-55 470,20 475,50 C480,80 455,105 440,110 Z" fill="#ece0ed" />
    </g>
    <g transform="translate(64, 60)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="12s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerLight}" transform="scale(1.18)" />
      </g>
      ${renderIcon(icon, 30, iconLight)}
    </g>
    <text x="36" y="125" font-family="system-ui, -apple-system, 'Google Sans', Roboto, sans-serif" font-size="21" font-weight="700" fill="#211824">${title}</text>
    <text x="36" y="152" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="13.5" font-weight="400" fill="#5f4e60">
      ${tLines}
    </text>
  </svg>`;
  saveSvg(path.join('light', `card-${name}.svg`), lightSvg);
}

// -------------------------------------------------------------
// 5. ROTATING TECH STACK PILL (Height: 42, Flower Center: 21, 21)
// -------------------------------------------------------------
function generateStackPill({ name, label, icon, flowerDark, flowerLight, iconDark, iconLight, width }) {
  // DARK with active 8s rotation
  const darkSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="42" viewBox="0 0 ${width} 42">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 8s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M21,0H${width - 21}A21,21 0 0 1 ${width},21V21A21,21 0 0 1 ${width - 21},42H21A21,21 0 0 1 0,21V21A21,21 0 0 1 21,0Z" fill="#231b24" stroke="#362938" stroke-width="1"/>
    <g transform="translate(21, 21)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="8s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerDark}" transform="scale(0.68)" />
      </g>
      ${renderIcon(icon, 17, iconDark)}
    </g>
    <text x="42" y="26" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="14.5" font-weight="600" fill="#f0e2ee">${label}</text>
  </svg>`;
  saveSvg(path.join('dark', `pill-${name}.svg`), darkSvg);

  // LIGHT
  const lightSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="42" viewBox="0 0 ${width} 42">
    <defs>
      <style>
        @keyframes m3e-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .m3e-flower-spin { animation: m3e-spin 8s linear infinite; transform-origin: 0px 0px; }
      </style>
    </defs>
    <path d="M21,0H${width - 21}A21,21 0 0 1 ${width},21V21A21,21 0 0 1 ${width - 21},42H21A21,21 0 0 1 0,21V21A21,21 0 0 1 21,0Z" fill="#f0e5ef" stroke="#ded0dd" stroke-width="1"/>
    <g transform="translate(21, 21)">
      <g class="m3e-flower-spin">
        <animateTransform attributeName="transform" type="rotate" from="0 0 0" to="360 0 0" dur="8s" repeatCount="indefinite"/>
        <path d="${CENTERED_FLOWER_PATH}" fill="${flowerLight}" transform="scale(0.68)" />
      </g>
      ${renderIcon(icon, 17, iconLight)}
    </g>
    <text x="42" y="26" font-family="system-ui, -apple-system, Roboto, sans-serif" font-size="14.5" font-weight="600" fill="#261b28">${label}</text>
  </svg>`;
  saveSvg(path.join('light', `pill-${name}.svg`), lightSvg);
}

// -------------------------------------------------------------
// EXECUTE GENERATION FOR NEXAGRAM
// -------------------------------------------------------------
console.log('Generating Nexagram Google Pixel M3E assets...');

// 1. Hero Buttons
generateButton({ name: 'download', sublabel: 'LATEST RELEASE', label: 'Download APK', icon: 'download', flowerDark: '#6750A4', flowerLight: '#EADDFF', iconDark: '#FFFFFF', iconLight: '#21005D', width: 216 });
generateButton({ name: 'github', sublabel: 'OPEN SOURCE', label: 'Source code', icon: 'github', flowerDark: '#4A3B4F', flowerLight: '#E8DCEB', iconDark: '#FFFFFF', iconLight: '#251A29', width: 206 });
generateButton({ name: 'support', sublabel: 'BUY ME A COFFEE', label: 'Support project', icon: 'heart', flowerDark: '#853B55', flowerLight: '#FFD8E4', iconDark: '#FFFFFF', iconLight: '#3B071F', width: 224 });
generateButton({ name: 'telegram', sublabel: 'TELEGRAM CLIENT', label: 'NekoX Fork', icon: 'telegram', flowerDark: '#1E5268', flowerLight: '#C4E7FF', iconDark: '#FFFFFF', iconLight: '#001E2E', width: 216 });

// 2. Badges
generateBadge({ name: 'android', label: 'Android 8.0+', icon: 'android', flowerDark: '#2E6A45', flowerLight: '#C3EED0', iconDark: '#FFFFFF', iconLight: '#00210E', width: 130 });
generateBadge({ name: 'telegram', label: 'Telegram Client', icon: 'telegram', flowerDark: '#1E5268', flowerLight: '#C4E7FF', iconDark: '#FFFFFF', iconLight: '#001E2E', width: 145 });
generateBadge({ name: 'nekox', label: 'Based on NekoX', icon: 'rocket', flowerDark: '#8A5128', flowerLight: '#FFDCC1', iconDark: '#FFFFFF', iconLight: '#351500', width: 145 });
generateBadge({ name: 'liquid', label: 'Liquid Glass 4.2', icon: 'sparkles', flowerDark: '#754877', flowerLight: '#FBD7FC', iconDark: '#FFFFFF', iconLight: '#2C132E', width: 155 });
generateBadge({ name: 'proxy', label: 'Built-in Proxies', icon: 'lock', flowerDark: '#3A5E53', flowerLight: '#CCE8DE', iconDark: '#FFFFFF', iconLight: '#042018', width: 138 });
generateBadge({ name: 'privacy', label: 'Privacy Hardened', icon: 'shield', flowerDark: '#455173', flowerLight: '#D8E2FF', iconDark: '#FFFFFF', iconLight: '#001A41', width: 155 });
generateBadge({ name: 'license', label: 'GPL-3.0 License', icon: 'license', flowerDark: '#455173', flowerLight: '#D8E2FF', iconDark: '#FFFFFF', iconLight: '#001A41', width: 140 });
generateBadge({ name: 'opensource', label: '100% Open Source', icon: 'code', flowerDark: '#2B4A75', flowerLight: '#D0E1FD', iconDark: '#FFFFFF', iconLight: '#071E3D', width: 155 });
generateBadge({ name: 'prs', label: 'PRs welcome', icon: 'contribute', flowerDark: '#776228', flowerLight: '#FFE193', iconDark: '#FFFFFF', iconLight: '#251A00', width: 125 });

// 3. Header Banners
generateHeader({ name: 'features', title: 'Nexagram Features', subtitle: 'Supercharged messaging, advanced chat tools & Liquid Glass UI', icon: 'sparkles', tags: ['Liquid Glass', 'M3 Expressive', 'Messaging'], flowerDark: '#6750A4', flowerLight: '#EADDFF', iconDark: '#FFFFFF', iconLight: '#21005D' });
generateHeader({ name: 'nekox', title: 'NekoX & Upstream Changes', subtitle: 'Built-in proxies, unlimited accounts, and custom emoji packs', icon: 'rocket', tags: ['Proxy Suite', 'Security', 'MTProxy'], flowerDark: '#8A5128', flowerLight: '#FFDCC1', iconDark: '#FFFFFF', iconLight: '#351500' });
generateHeader({ name: 'stack', title: 'Tech Stack & Architecture', subtitle: 'Native Android core powered by Kotlin, Java, C++ & Material 3 Expressive', icon: 'devices', tags: ['Kotlin', 'C++ / NDK', 'M3 Expressive'], flowerDark: '#2B4A75', flowerLight: '#D0E1FD', iconDark: '#FFFFFF', iconLight: '#071E3D' });
generateHeader({ name: 'compilation', title: 'Compilation & Build Guide', subtitle: 'Step-by-step instructions for Linux, macOS, WSL2, and GitHub Actions', icon: 'terminal', tags: ['Gradle', 'NDK', 'CI/CD'], flowerDark: '#776228', flowerLight: '#FFE193', iconDark: '#FFFFFF', iconLight: '#251A00' });
generateHeader({ name: 'faq', title: 'Frequently Asked Questions', subtitle: 'Key differences between Nexagram, NekoX, upstream builds and push services', icon: 'chat', tags: ['FAQ', 'NekoX', 'Troubleshooting'], flowerDark: '#1E5268', flowerLight: '#C4E7FF', iconDark: '#FFFFFF', iconLight: '#001E2E' });
generateHeader({ name: 'contributors', title: 'Contributors & Credits', subtitle: 'Thanks to our open source creators and upstream developers', icon: 'contribute', tags: ['NekoX', 'Nekogram', 'Nullgram'], flowerDark: '#3A5E53', flowerLight: '#CCE8DE', iconDark: '#FFFFFF', iconLight: '#042018' });
generateHeader({ name: 'support', title: 'Support & Sponsorship', subtitle: 'Help sustain Nexagram open-source development and maintenance', icon: 'heart', tags: ['Buy Me Coffee', 'SupportKori', 'Payoneer'], flowerDark: '#853B55', flowerLight: '#FFD8E4', iconDark: '#FFFFFF', iconLight: '#3B071F' });

// 4. Bento Grid Cards
generateCard({ name: 'liquid', title: 'Liquid Glass Optics', lines: ['120Hz fluid glass surfaces with physics-based blur,', 'interactive tab dock, and native Material 3 Expressive', 'curvature tailored for modern Android viewports.'], icon: 'sparkles', flowerDark: '#754877', flowerLight: '#FBD7FC', iconDark: '#FFFFFF', iconLight: '#2C132E' });
generateCard({ name: 'proxy', title: 'Built-in Universal Proxy', lines: ['Integrated VMess, Shadowsocks, SSR, Trojan-GFW,', 'and IPv6 MTProxy. Subscription parsing, auto-switching,', 'and latency speed testing right in the app.'], icon: 'lock', flowerDark: '#3A5E53', flowerLight: '#CCE8DE', iconDark: '#FFFFFF', iconLight: '#042018' });
generateCard({ name: 'messages', title: 'Supercharged Messaging', lines: ['Combine messages, editable text styling, forced copy,', 'invert reply, and instant quick-reply actions straight', 'from the intuitive long-click context menu.'], icon: 'chat', flowerDark: '#1E5268', flowerLight: '#C4E7FF', iconDark: '#FFFFFF', iconLight: '#001E2E' });
generateCard({ name: 'privacy', title: 'Hardened Privacy & Security', lines: ['Completely telemetry-free, zero tracking analytics,', 'unlimited concurrent accounts, and fine-grained', 'media cache and auto-delete storage controls.'], icon: 'shield', flowerDark: '#2E6A45', flowerLight: '#C3EED0', iconDark: '#FFFFFF', iconLight: '#00210E' });
generateCard({ name: 'customization', title: 'Expressive Personalization', lines: ['Custom emoji pack support without packaging limits,', 'adaptive launcher icons, smooth chat scroll preview,', 'and expressive Material 3 tonal color themes.'], icon: 'palette', flowerDark: '#6750A4', flowerLight: '#EADDFF', iconDark: '#FFFFFF', iconLight: '#21005D' });
generateCard({ name: 'performance', title: 'Native C++ & Kotlin Engine', lines: ['Optimized TDLib and WebRTC core architecture,', 'hardware noise suppression, voice enhancement, and', 'native library integrity verification.'], icon: 'devices', flowerDark: '#455173', flowerLight: '#D8E2FF', iconDark: '#FFFFFF', iconLight: '#001A41' });

// 5. Tech Stack Pills
generateStackPill({ name: 'android', label: 'Android SDK 34', icon: 'android', flowerDark: '#2E6A45', flowerLight: '#C3EED0', iconDark: '#FFFFFF', iconLight: '#00210E', width: 165 });
generateStackPill({ name: 'kotlin', label: 'Kotlin 2.0', icon: 'code', flowerDark: '#853B55', flowerLight: '#FFD8E4', iconDark: '#FFFFFF', iconLight: '#3B071F', width: 135 });
generateStackPill({ name: 'java', label: 'Java 17+', icon: 'terminal', flowerDark: '#333333', flowerLight: '#E5E5E5', iconDark: '#FFFFFF', iconLight: '#000000', width: 125 });
generateStackPill({ name: 'cpp', label: 'C++ / NDK', icon: 'code', flowerDark: '#2B4A75', flowerLight: '#D0E1FD', iconDark: '#FFFFFF', iconLight: '#071E3D', width: 135 });
generateStackPill({ name: 'm3e', label: 'Material 3 Expressive', icon: 'palette', flowerDark: '#6750A4', flowerLight: '#EADDFF', iconDark: '#FFFFFF', iconLight: '#21005D', width: 216 });
generateStackPill({ name: 'liquid', label: 'Liquid Glass 4.2', icon: 'sparkles', flowerDark: '#754877', flowerLight: '#FBD7FC', iconDark: '#FFFFFF', iconLight: '#2C132E', width: 170 });
generateStackPill({ name: 'telegram', label: 'Telegram MTProto', icon: 'telegram', flowerDark: '#1E5268', flowerLight: '#C4E7FF', iconDark: '#FFFFFF', iconLight: '#001E2E', width: 180 });
generateStackPill({ name: 'license', label: 'GNU GPLv3', icon: 'license', flowerDark: '#776228', flowerLight: '#FFE193', iconDark: '#FFFFFF', iconLight: '#251A00', width: 135 });

// 6. Generate Local HTML Preview
const htmlPreview = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Nexagram - Google Pixel M3E README Assets Preview</title>
  <style>
    body {
      font-family: system-ui, -apple-system, 'Google Sans', Roboto, sans-serif;
      margin: 0;
      padding: 40px 20px;
      background: #0e0a0f;
      color: #f0e2ee;
    }
    .container {
      max-width: 1040px;
      margin: 0 auto;
    }
    h1, h2 {
      text-align: center;
      color: #ffffff;
    }
    .preview-box {
      border: 1px solid #362938;
      border-radius: 24px;
      padding: 30px;
      margin-bottom: 40px;
      background: #1b131c;
    }
    .preview-box.light {
      background: #faf3f9;
      color: #211824;
      border-color: #e0cee0;
    }
    .preview-box.light h2 {
      color: #211824;
    }
    .center {
      text-align: center;
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 12px;
      align-items: center;
      margin: 20px 0;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }
    .grid img {
      max-width: 100%;
      height: auto;
    }
    .banner-row img {
      width: 100%;
      margin-bottom: 16px;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>Google Pixel / Android Material 3 Expressive README Assets</h1>
    <p style="text-align:center; color:#caa7cd;">Nexagram - Authentic 12-petal rotating scalloped flower shapes with SMIL animations</p>

    <!-- DARK MODE -->
    <div class="preview-box">
      <h2>🌙 Dark Mode (Default)</h2>
      
      <div class="center">
        <img src="assets/readme/dark/btn-download.svg" alt="Download APK" height="50">
        <img src="assets/readme/dark/btn-github.svg" alt="Source Code" height="50">
        <img src="assets/readme/dark/btn-support.svg" alt="Support Project" height="50">
        <img src="assets/readme/dark/btn-telegram.svg" alt="Telegram Client" height="50">
      </div>

      <div class="center">
        <img src="assets/readme/dark/badge-android.svg" height="28">
        <img src="assets/readme/dark/badge-telegram.svg" height="28">
        <img src="assets/readme/dark/badge-nekox.svg" height="28">
        <img src="assets/readme/dark/badge-liquid.svg" height="28">
        <img src="assets/readme/dark/badge-proxy.svg" height="28">
        <img src="assets/readme/dark/badge-privacy.svg" height="28">
        <img src="assets/readme/dark/badge-license.svg" height="28">
        <img src="assets/readme/dark/badge-opensource.svg" height="28">
        <img src="assets/readme/dark/badge-prs.svg" height="28">
      </div>

      <div class="banner-row">
        <img src="assets/readme/dark/h-features.svg" alt="Features Header">
        <img src="assets/readme/dark/h-nekox.svg" alt="NekoX Header">
        <img src="assets/readme/dark/h-stack.svg" alt="Stack Header">
      </div>

      <div class="grid">
        <img src="assets/readme/dark/card-liquid.svg">
        <img src="assets/readme/dark/card-proxy.svg">
        <img src="assets/readme/dark/card-messages.svg">
        <img src="assets/readme/dark/card-privacy.svg">
        <img src="assets/readme/dark/card-customization.svg">
        <img src="assets/readme/dark/card-performance.svg">
      </div>

      <div class="center" style="margin-top: 30px;">
        <img src="assets/readme/dark/pill-android.svg" height="38">
        <img src="assets/readme/dark/pill-kotlin.svg" height="38">
        <img src="assets/readme/dark/pill-java.svg" height="38">
        <img src="assets/readme/dark/pill-cpp.svg" height="38">
        <img src="assets/readme/dark/pill-m3e.svg" height="38">
        <img src="assets/readme/dark/pill-liquid.svg" height="38">
        <img src="assets/readme/dark/pill-telegram.svg" height="38">
        <img src="assets/readme/dark/pill-license.svg" height="38">
      </div>
    </div>

    <!-- LIGHT MODE -->
    <div class="preview-box light">
      <h2>☀️ Light Mode</h2>
      
      <div class="center">
        <img src="assets/readme/light/btn-download.svg" alt="Download APK" height="50">
        <img src="assets/readme/light/btn-github.svg" alt="Source Code" height="50">
        <img src="assets/readme/light/btn-support.svg" alt="Support Project" height="50">
        <img src="assets/readme/light/btn-telegram.svg" alt="Telegram Client" height="50">
      </div>

      <div class="center">
        <img src="assets/readme/light/badge-android.svg" height="28">
        <img src="assets/readme/light/badge-telegram.svg" height="28">
        <img src="assets/readme/light/badge-nekox.svg" height="28">
        <img src="assets/readme/light/badge-liquid.svg" height="28">
        <img src="assets/readme/light/badge-proxy.svg" height="28">
        <img src="assets/readme/light/badge-privacy.svg" height="28">
        <img src="assets/readme/light/badge-license.svg" height="28">
        <img src="assets/readme/light/badge-opensource.svg" height="28">
        <img src="assets/readme/light/badge-prs.svg" height="28">
      </div>

      <div class="banner-row">
        <img src="assets/readme/light/h-features.svg" alt="Features Header">
        <img src="assets/readme/light/h-nekox.svg" alt="NekoX Header">
        <img src="assets/readme/light/h-stack.svg" alt="Stack Header">
      </div>

      <div class="grid">
        <img src="assets/readme/light/card-liquid.svg">
        <img src="assets/readme/light/card-proxy.svg">
        <img src="assets/readme/light/card-messages.svg">
        <img src="assets/readme/light/card-privacy.svg">
        <img src="assets/readme/light/card-customization.svg">
        <img src="assets/readme/light/card-performance.svg">
      </div>

      <div class="center" style="margin-top: 30px;">
        <img src="assets/readme/light/pill-android.svg" height="38">
        <img src="assets/readme/light/pill-kotlin.svg" height="38">
        <img src="assets/readme/light/pill-java.svg" height="38">
        <img src="assets/readme/light/pill-cpp.svg" height="38">
        <img src="assets/readme/light/pill-m3e.svg" height="38">
        <img src="assets/readme/light/pill-liquid.svg" height="38">
        <img src="assets/readme/light/pill-telegram.svg" height="38">
        <img src="assets/readme/light/pill-license.svg" height="38">
      </div>
        <img src="assets/readme/light/pill-cpp.svg" height="38">
        <img src="assets/readme/light/pill-m3e.svg" height="38">
        <img src="assets/readme/light/pill-liquid.svg" height="38">
      </div>
    </div>
  </div>
</body>
</html>`;

fs.mkdirSync(path.join(__dirname, '..', 'public'), { recursive: true });
fs.writeFileSync(path.join(__dirname, '..', 'public', 'readme-preview.html'), htmlPreview);

console.log('Successfully generated all Google Pixel M3E SVG assets and public/readme-preview.html!');
