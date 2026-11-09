## Nexagram v2.3.2 Release

### 🚀 Highlights & New Features in v2.3.2

#### 1. 🔄 Complete Upstream Synchronization (Nagram 13.1.1 Engine)
- Integrated 185+ upstream commits and optimizations from upstream Nagram (Nov 2026 release).
- Telegram Core 13.1.1 foundation with enhanced stability, faster startup, and reduced memory footprint.
- Restored stock font size and bubble radius defaults for pixel-perfect balance.

#### 2. 🎨 Live Interactive Settings Previews
- **Live Action Bar Preview:** Instant interactive preview right inside General Settings (NekoGeneralSettingsActivity) showcasing custom title, centered title, download icon, decorations, and app bar shadow in real-time.
- **Live Folder Tabs Preview:** Interactive folder tabs preview showing selected tab style, stroke, badge count, and layout options.
- **Live Double Tap Action Preview:** Visual feedback cell in Chat Settings (NekoChatSettingsActivity) displaying active double-tap behaviors for incoming and outgoing messages.
- **Sticker Shape Selector:** Choose between dynamic sticker shapes (original, rounded, square) directly from Chat Settings.

#### 3. 🛡️ Advanced Member Admin Actions
- **Avatar Long-Press Quick Actions:** In supergroups where you have ban permissions, long-pressing any member's avatar immediately presents options to **Change Permissions**, **Ban Member**, or **Delete All Messages** without needing their message on screen.
- **User Mention Quick Actions:** Long-press any @mention in chat to quickly manage user permissions or ban/delete history.
- **Member Removal with History Cleanup:** Removing a member from the member list now directly offers to purge all of their historical messages in one action.

#### 4. 📁 Scoped Storage & Multi-File Album Sharing
- **External App File Sharing:** Fixed Android Scoped Storage EACCES permission errors when sharing multiple files from external file managers or Download folders. Files are properly cached and sent cleanly.
- **Album Grouping:** Files picked via the system file picker now automatically group into single album messages instead of spamming separate single messages.

#### 5. 🔓 Enhanced Forced Copy & Privacy Controls
- Enhanced copy protection logic in private chats while fully preserving Nexagram's **Forced Copy** capability. If enabled in N-Config, users can copy text from restricted and protected dialogs without artificial restrictions.
- Added support for auto-loading self user info when supergroup participants are hidden.
- Fixed bot forum topic message ID routing and persistent keyboard back navigation.

#### 6. 💻 Full Platform Coverage & Dedicated Update Pipeline
- **Dedicated Releases Pipeline:** Strictly queries https://api.github.com/repos/smartworldarafath/Nexagram/releases for official releases and updates.
- **Desktop Client & Website:** Synced version v2.3.2 across desktop_client, installer.iss, and GitHub Pages website (docs/index.html).

---
## Nexagram v2.3.0 Release

### 🚀 Highlights & Bug Fixes in v2.3.0

#### 1. 🔄 App Update Source Bug Resolved (Pure Nexagram Updates)
- **Eliminated Nagram Upstream Overrides:** Previously, background update checks were listening to upstream Nagram Telegram channel metadata (`nagram_remote_metadata`), causing the app to erroneously alert users about updates from `nextalone/nagram`.
- **Dedicated Nexagram GitHub Pipeline:** Rewrote `UpdateHelper` to query the official Nexagram repository API directly:
  `https://api.github.com/repos/smartworldarafath/Nexagram/releases`
- **Clean Fallback & Zero Upstream Leaks:** All update dialogues, popup notices, and `BuildVars.GITHUB_RELEASE_URL` now point exclusively to `smartworldarafath/Nexagram`.

#### 2. 📱 Tablet Channel Limit (500 Channels) Root Cause & Fix
- **The Issue:** On phones, joining channels worked smoothly, but on a newly installed tablet, attempting to join a channel threw an error: "channel joining limit (500) exceeded" despite only having ~16 visible chats.
- **Root Cause Analysis:** 
  1. *Server-Side vs Local Count:* Telegram server-side databases maintain the lifetime joined channels count (including old, archived, and deleted channels where the user remained registered as a participant). A newly set up tablet only fetches the first page of recent dialogs (~16 chats), creating the visual illusion that the user has only 16 channels.
  2. *Tablet Paywall Trap:* When `CHANNELS_TOO_MUCH` occurred, the app checked `if (fragment.getParentActivity() != null)`. Because tablets use split-screen layouts, this condition was always true, forcing `LimitReachedBottomSheet` (a Telegram Premium upsell dialog) to appear instead of `TooManyCommunitiesActivity`. Furthermore, because the tablet had no local history, `TL_channels_getInactiveChannels` returned 0 chats—leaving the user with an empty list and no way to leave channels.
- **The Fix:**
  - `AlertsCreator.java` now routes `CHANNELS_TOO_MUCH` directly to `TooManyCommunitiesActivity` across both phones and tablets.
  - Implemented automatic cloud fallback: If `getInactiveChannels` returns empty, the app queries `TL_messages_getAllChats` from Telegram servers to fetch all 500 channels tied to the account so users can view, search, and leave unneeded channels with one tap.
  - Added identical fallback protection to `LimitReachedBottomSheet`.

#### 3. 💻 Windows Desktop Edition v2.3.0
- **Updated to v2.3.0** with full feature parity, liquid glass styling, N-Config, and custom chat options.
- **Installer (.exe):** `Nexagram-Setup-v2.3.0.exe` (Windows 64-bit Installer)
- **Portable (.zip):** `Nexagram-v2.3.0-windows-x64.zip` (No installation required)

---
*Built with ❤️ by SmartWorld / Zero by Arafath*
