#!/usr/bin/env node
/* Adds the home-screen widget, the notification icon and version numbers to the generated android/ project.
   Run after `npx cap add android`. Safe to run more than once. */
const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const appId = JSON.parse(fs.readFileSync(path.join(root, "capacitor.config.json"), "utf8")).appId;
const app = path.join(root, "android", "app");
const main = path.join(app, "src", "main");
if (!fs.existsSync(main)) { console.error("android/ not found. Run: npx cap add android"); process.exit(1); }

/* 1. Java widget provider, with the real package name */
const javaDir = path.join(main, "java", ...appId.split("."));
fs.mkdirSync(javaDir, { recursive: true });
const src = fs.readFileSync(path.join(root, "android-extras", "java", "FactWidgetProvider.java"), "utf8");
fs.writeFileSync(path.join(javaDir, "FactWidgetProvider.java"), src.split("__APP_ID__").join(appId));

/* 2. Resources */
function copyDir(from, to) {
  fs.mkdirSync(to, { recursive: true });
  for (const f of fs.readdirSync(from)) fs.copyFileSync(path.join(from, f), path.join(to, f));
}
for (const d of ["layout", "xml", "drawable", "values"]) {
  copyDir(path.join(root, "android-extras", "res", d), path.join(main, "res", d));
}

/* 3. Register the widget in AndroidManifest.xml */
const mf = path.join(main, "AndroidManifest.xml");
let xml = fs.readFileSync(mf, "utf8");
if (!xml.includes("FactWidgetProvider")) {
  const receiver = `
        <receiver android:name=".FactWidgetProvider" android:exported="true" android:label="GeoBharat fact">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/fact_widget_info" />
        </receiver>
`;
  xml = xml.replace("</application>", receiver + "    </application>");
  fs.writeFileSync(mf, xml);
}

/* 4. Version numbers (override with VERSION_CODE and VERSION_NAME) */
const gradle = path.join(app, "build.gradle");
let g = fs.readFileSync(gradle, "utf8");
const code = process.env.VERSION_CODE || "1";
const name = process.env.VERSION_NAME || "1.0.0";
g = g.replace(/versionCode\s+\d+/, `versionCode ${code}`).replace(/versionName\s+"[^"]*"/, `versionName "${name}"`);
fs.writeFileSync(gradle, g);

console.log(`Patched android project for ${appId} (versionCode ${code}, versionName ${name}).`);
