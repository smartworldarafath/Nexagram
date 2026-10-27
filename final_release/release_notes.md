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
