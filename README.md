# GeoBharat

India geography quiz and learning app. Six levels (Class 6-8 to UPSC), endless questions that never repeat, 14 lessons that match your level, Hindi and English, a scoreboard and a daily reminder. Works offline.

This repository builds three things from one code base:

| What | How |
| --- | --- |
| **Web app / PWA** | `www/` folder, published with GitHub Pages |
| **Android app** (Play Store) | Capacitor wrapper, built by GitHub Actions |
| **Android home-screen widget and daily notification** | `android-extras/` and the Capacitor Local Notifications plugin |

```
www/                 the app (index.html, manifest, service worker, icons, privacy.html)
assets/              icon and splash sources; Android icons are generated from these
android-extras/      home-screen widget (Java + XML) added to the Android project
scripts/             patch-android.js adds the widget and version numbers
store/               Play Store icon, feature graphic, screenshots
.github/workflows/   android.yml (APK + AAB) and pages.yml (web version)
STORE_LISTING.md     text and answers to paste into Play Console
```

The Android project (`android/`) is not stored in the repo. It is created fresh by `npx cap add android` each build.

---

## Part 1: Put it on GitHub

1. Create an account at github.com if you do not have one.
2. Click **New repository**. Name it `geobharat`, keep it **Public** (needed for free GitHub Pages), do not add a README.
3. On your computer, open a terminal in this folder and run:
   ```
   git init
   git add .
   git commit -m "GeoBharat first version"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/geobharat.git
   git push -u origin main
   ```
   (No computer terminal? On the repo page click **Add file > Upload files**, drag in everything from this folder, including the hidden `.github` folder, and commit.)
4. Edit these placeholders: `YOUR NAME` in `LICENSE`, `YOUR-EMAIL@example.com` in `www/privacy.html`.

### Turn on the web version
Repo **Settings > Pages > Build and deployment > Source: GitHub Actions**. Then open the **Actions** tab, choose **Publish web version** and click **Run workflow**. In a minute your game is live at `https://YOUR-USERNAME.github.io/geobharat/`, and your privacy policy is at `.../privacy.html`. Phones can use **Add to Home screen** to install it as an app.

---

## Part 2: Build the Android app

### 2a. Change the app ID (important)
Open `capacitor.config.json`. `"appId": "com.geobharat.quiz"` must be **unique on the whole Play Store and can never be changed after you publish**. Use something you own, for example `io.github.YOUR-USERNAME.geobharat`. Use only lowercase letters, numbers and dots.

### 2b. Make a signing key (once, keep it safe)
You need a Java JDK installed (or do this in any Linux/Mac/Windows terminal that has `keytool`).
```
keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```
Remember the passwords. **Back up `upload.jks` somewhere safe, and never commit it to GitHub.**

Turn it into text for GitHub:
- Mac/Linux: `base64 -w0 upload.jks > upload.jks.b64` (on Mac use `base64 -i upload.jks -o upload.jks.b64`)
- Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload.jks")) | Out-File upload.jks.b64`

### 2c. Add four secrets
Repo **Settings > Secrets and variables > Actions > New repository secret**:

| Name | Value |
| --- | --- |
| `KEYSTORE_BASE64` | the whole text inside `upload.jks.b64` |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | the key password |

### 2d. Run the build
Open **Actions > Build Android app > Run workflow**. After about 8-12 minutes you get two downloads under **Artifacts**:
- `geobharat-apk-debug`: install on your phone to test (the widget and notifications work here).
- `geobharat-aab-for-play-store`: the signed file you upload to the Play Store.

To make a numbered release instead, push a tag: `git tag v1.0.0 && git push --tags`. The files are attached to a GitHub release automatically. Each run increases the Android `versionCode` automatically, which Play requires for every update.

### Test on your phone
1. Download the debug APK on the phone, tap it and allow installation from this source.
2. Open the app, go to the reminder box, switch it on, allow notifications, press **Test** and you should get a notification within a second.
3. Long-press the home screen, choose **Widgets > GeoBharat**, and drag the widget out.

---

## Part 3: Publish on the Play Store

1. Create a Google Play developer account at play.google.com/console (one-time fee of 25 USD, identity check needed).
2. **Create app**: name `GeoBharat - India Geography`, language, App (not game), Free.
3. Fill in the store page, graphics and questionnaires. Everything you need to paste is in **STORE_LISTING.md**.
4. **Testing and release > Production** (or **Closed testing** first), **Create new release**. Accept Play App Signing, then upload `geobharat-signed.aab`.
5. Add release notes, for example "First release".
6. Send the app for review. First review usually takes from a few hours to a few days.

**Important rule for new personal accounts:** Google currently requires a closed test with at least 12 testers who stay opted in for 14 days before you can apply for production access. Start the closed test early, ask friends and classmates to join with their Gmail addresses, and then apply for production. Organisation accounts do not have this rule. Check the current requirement in Play Console, because Google can change it.

---

## Run or change the app on your computer
- Edit `www/index.html`, open it in any browser to try it.
- Build locally (needs Node 22, JDK 21 and Android Studio):
  ```
  npm install
  npx cap add android
  npx @capacitor/assets generate --android
  node scripts/patch-android.js
  npx cap sync android
  npx cap open android
  ```

## How the pieces work
- **Questions**: a built-in bank plus an offline generator that makes new questions from fact tables, so questions never run out and never repeat. Seen questions are remembered on the device.
- **Lessons**: 14 hand-written lessons shown by level, plus a fact list. Inside Claude the app can also write fresh lessons; the Play Store app uses the built-in ones.
- **Notification**: the app calls the Capacitor Local Notifications plugin to schedule a repeating daily alarm at the time you choose. It survives the app being closed and, with the plugin's receiver, a phone restart. In a normal browser it falls back to the web Notification API while the page is open.
- **Widget**: `FactWidgetProvider.java` shows one of 35 facts, changing every day. The arrow shows the next fact. Add more facts to the `FACTS` array.

## Known limits
- Daily alerts use exact alarms where allowed. Some phone makers (Xiaomi, Oppo, Vivo, Realme) stop background alarms unless the user allows **Autostart** or turns off battery optimisation for the app.
- Offline lessons are in English with Hindi fact lists. Full Hindi lessons are available only when the app runs inside Claude.
- The content was written from general knowledge and checked by reading, not against a textbook. Check figures against your syllabus before relying on them in an exam.
