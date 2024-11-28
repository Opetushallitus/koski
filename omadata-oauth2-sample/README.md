## OmaData OAuth2 esimerkki-applikaatio

# Kehitys

Vaatii, että Koski on käynnissä portissa 7021. ks. [README.md](../README.md#koski-sovelluksen-ajaminen-paikallisesti) ja 
että [koski-luovutuspalvelu](https://github.com/Opetushallitus/koski-luovutuspalvelu) -repositoryn sisältö on koski-hakemiston juuressa alihakemistossa

    /koski-luovutuspalvelu

Lisäksi pitää olla ajettuna:

    /omadata-oauth2-sample/server % npm install
    /omadata-oauth2-sample/client % npm install

Tämän jälkeen tarvittavat palvelut, mukaanlukien luovutuspalvelun, voi käynnistää:

    /omadata-oauth2-sample/client % npm run start-with-server-and-luovutuspalvelu

Koko putken rakenne:

    (1) Esimerkki-applikaation frontti, http://localhost:7050

       kutsuu (development serverin proxyn kautta):
    (2) Esimerkki-applikaation backend, http://localhost:7051

       kutsuu:
    (3) Docker-kontissa ajettu koski-luovutuspalvelu, https://localhost:7022
          Huom! Tämä on mutual TLS -putken testaamiseksi https, lokaalilla root-CA:lla.

      kutsuu:
    (4) Koski-backend, http://<lokaali-ip>:<port>
          Luovutuspalvelun docker-kontille näkyvä <lokaali-ip> on Github Actions CI:llä ajettaessa
          172.17.0.1 ja <port> arvottu.
          Lokaalisti <lokaali-ip> haetaan client/scripts/getmyip.js -skriptillä ja <port> on aina 7021.

## E2E-testit

Em. koko putki -osuuden lisäksi pitää olla ajettuna:

    /omadata-oauth2-sample/client % npx playwright install

Testien käynnistys:

    npm run playwright:test

Ks. muut playwright-skriptit, esim. debug-moodi [package.json](client/package.json).

Tarvittaessa, ja esim. CI:llä, Playwright-konfiguraation webServer-osuus [playwright.config.ts](client/playwright.config.ts) käynnistää
esimerkki-applikaation frontin ja backendin, sekä luovutuspalvelun.

# Ympäristöön vienti

Github actionsista käynnistettävissä, ks. [omadataoauth2sample_deploy.yml](../.github/workflows/omadataoauth2sample_deploy.yml)
