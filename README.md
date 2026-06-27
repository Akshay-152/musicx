# MusicX Web Application

## Overview

This is the **MusicX** web application – a modern, feature‑rich music player that works on both desktop browsers and mobile devices. The project uses plain HTML, CSS, and JavaScript (no framework) and integrates with Firebase for authentication and Firestore storage, as well as Cloudinary for media uploads.

---

## 🚀 Live Demo

**Try it here:** https://musicx-play.blogspot.com/

---

## Security‑Sensitive Configuration

The application requires a few secret values to operate:

| Service | Placeholder | What to replace it with |
|---------|-------------|------------------------|
| **Firebase** | `YOUR_FIREBASE_API_KEY`<br/>`YOUR_FIREBASE_AUTH_DOMAIN`<br/>`YOUR_FIREBASE_PROJECT_ID`<br/>`YOUR_FIREBASE_STORAGE_BUCKET`<br/>`YOUR_FIREBASE_MESSAGING_SENDER_ID`<br/>`YOUR_FIREBASE_APP_ID` | Your Firebase project credentials (found in the Firebase console under **Project settings → General → Your apps**). |
| **Cloudinary** | `YOUR_CLOUDINARY_CLOUD_NAME`<br/>`YOUR_CLOUDINARY_UPLOAD_PRESET` | Your Cloudinary account’s cloud name and an unsigned upload preset (create one in **Settings → Upload → Upload presets**). |

The source file `index.html` now contains only these placeholders – **no real keys are committed**. Before running the app locally or deploying, replace each placeholder with the corresponding value.

---

## Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/musicx.git
   cd musicx
   ```

2. **Install dependencies** (only a static‑file server is needed; you can use any, e.g., `http-server` or `live-server`).
   ```bash
   # If you have Node.js installed
   npm install -g live-server   # one‑time global install
   ```

3. **Configure the app**
   - Open `index.html` in a code editor.
   - Locate the **Firebase** and **Cloudinary** configuration block (around line 718).
   - Replace each placeholder with the real values from your Firebase/Cloudinary accounts.
   - Save the file.

4. **Run the app**
   ```bash
   live-server .
   ```
   The site will open automatically in your default browser.

5. **Testing mobile behaviour**
   - Open the dev tools, toggle the device toolbar, and refresh the page.
   - On a mobile viewport the page will automatically redirect to the APK download URL.
   - On desktop you will see an **Add to Home Screen** button (PWA install prompt).

---

## Deploying to GitHub Pages

1. **Commit your changes** (make sure you never commit actual secret keys!).
   ```bash
   git add .
   git commit -m "Initial commit – placeholders for config"
   ```

2. **Push to GitHub**
   ```bash
   git remote add origin https://github.com/your-username/musicx.git
   git push -u origin master
   ```

3. **Enable GitHub Pages**
   - Go to the repository on GitHub → Settings → Pages.
   - Choose the `master` (or `main`) branch and the root folder.
   - Save; GitHub will provide a URL like `https://your-username.github.io/musicx/`.

4. **Update the hosted version**
   - The hosted site will read the same `index.html`. Remember to keep the placeholders in the repo; add the real credentials **only** to a local copy or to a secret management system (e.g., GitHub Actions secrets) if you ever automate a build.

---

## Adding Real Credentials for Production (Optional)

If you want to automate deployment with the real keys without exposing them in the public repo, use **GitHub Secrets** and a CI workflow:

```yaml
# .github/workflows/deploy.yml
name: Deploy to GitHub Pages
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Replace placeholders
        run: |
          sed -i "s/YOUR_FIREBASE_API_KEY/${{ secrets.FIREBASE_API_KEY }}/" index.html
          sed -i "s/YOUR_FIREBASE_AUTH_DOMAIN/${{ secrets.FIREBASE_AUTH_DOMAIN }}/" index.html
          # repeat for each placeholder …
      - name: Deploy
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: .
```

Store every secret (API key, auth domain, etc.) in the repository **Settings → Secrets**. The workflow will replace the placeholders at build time, keeping the repo clean.

---

## License

MIT © 2026 Akshay‑152 – feel free to fork, modify, and use for personal projects.

---

*If you have any questions about the setup or need help obtaining Firebase/Cloudinary credentials, open an issue in the repository or contact the project maintainer.*
