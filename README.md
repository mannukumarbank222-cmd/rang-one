# Rang One – native Android browser (v4.8)

Rang One is a light native web browser by PlayRang Technologies (India).
Native Java app (minSdk 24, targetSdk 34, no third-party libraries), Hindi / English UI.

## Features
- **Tabs** with a tab switcher (thumbnails), **private tabs**, bookmarks, history, speed-dial home screen.
- **Video & audio downloader** (new in 4.8): finds `<video>`, `<audio>`, `<source>`, direct media links (.mp4, .webm, .mkv, .mov, .3gp, .mp3, .m4a, .aac, .ogg, .wav, .flac…), `og:video` tags and media network requests. A floating **Download video (N)** button appears when media is found; long-press on a video also offers the download. Files are saved with Android DownloadManager (cookies, User-Agent and Referer are sent). HLS (.m3u8) / DASH / blob streams are detected and explained clearly instead of failing silently.
- **Downloads screen**: live status, size and progress bar, open, share, copy link, delete.
- **Ad & tracker blocker** (new): built-in list of common ad / tracking domains, on by default, count of blocked requests per page (menu → Ad blocker), toggle in Settings.
- **Reading mode** (new): the article text in a clean native view with A− / A+ font size.
- **Night mode for web pages** (new): darkens any website (WebView algorithmic darkening with the dark theme, CSS night filter otherwise).
- **Save page as PDF** (new, Android print framework), **Save offline** (web archive) and offline pages list.
- **Add to Home screen** (new): pinned shortcut for the current page (Android 8+).
- **Translate page** (new) into Hindi, English and 10 other Indian languages.
- **Text size** (new, 75–200 %) and **Data saver** (new, no images).
- Find in page, desktop site, share, full-screen video (rotates to landscape), file upload, settings (search engine, theme, language, pop-up blocker, JavaScript, clear data) and an About screen.

## Permissions
INTERNET, ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE (Android 9 and older only, to save downloads in the Downloads folder), POST_NOTIFICATIONS (Android 13+, download notifications). No login, no ads, no analytics. All user data stays on the device.

Privacy policy: https://mannukumarbank222-cmd.github.io/rang-studio/privacy-policy-rang-one.html

Built by GitHub Actions on every push to `main`; the signed APK (`RangOne_v4.8.apk`) and real emulator screenshots are attached to Releases.
