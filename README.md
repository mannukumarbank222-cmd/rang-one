# Bachat Mela (बचत मेला)

Hindi-first native Android deal finder (package `app.bachatmela.deals`). Plain Java, no third-party libraries.

- Deals: `deals/deals.json` on this branch is fetched by the app at startup
  (`https://raw.githubusercontent.com/mannukumarbank222-cmd/rang-one/bachat-mela/deals/deals.json`), cached,
  with a bundled copy in `app/src/main/assets/deals.json` as fallback. Edit `deals/deals.json` to change deals or
  swap `link` values for affiliate (EarnKaro / Cuelinks) links — no app update needed. Pushing only `deals/**` does not rebuild.
- Fields: id, title, store, category (fashion | electronics | home | beauty | books_toys), mrp, price, image, link, description.
- CI: `.github/workflows/bachat-mela.yml` builds a signed release APK, takes emulator screenshots, and publishes a release.
