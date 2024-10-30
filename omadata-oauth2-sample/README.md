## OmaData OAuth2 esimerkki-applikaatio

# Kehitys

## Backend

    cd server
    npm install
    npm start

## Frontend

    cd client
    npm install
    npm start

## E2E-testit

Testit olettavat, että Koski-backend on käynnissä (ja Koski-frontti buildattuna) ja että
koski-luovutuspalvelu -repositoryn sisältö on koski-hakemiston juuressa alihakemistossa

    /koski-luovutuspalvelu

Lisäksi pitää olla ajettuna:

    /omadata-oauth2-sample/server % npm install
    /omadata-oauth2-sample/client % npm install
    /omadata-oauth2-sample/client % npx playwright install

Testien käynnistys:

    npm run playwright:test

Ks. muut playwright-skriptit, esim. debug-moodi [package.json](client/package.json).

Testit testaavat koko putken paikallisesti ajetuilla palveluilla:

    (1) Esimerkki-applikaation frontti, http://localhost:7050

       kutsuu:
    (2) Esimerkki-applikaation backend, http://localhost:7051

       kutsuu:
    (3) Docker-kontissa ajettu koski-luovutuspalvelu, https://localhost:7022
          Huom! Tämä on mutual TLS -putken testaamiseksi https, lokaalilla root-CA:lla.

      kutsuu:
    (4) Koski-backend, http://<lokaali-ip>:<port>
          Luovutuspalvelun docker-kontille näkyvä <lokaali-ip> on Github Actions CI:llä ajettaessa
          172.17.0.1 ja <port> arvottu.
          Lokaalisti <lokaali-ip> haetaan client/scripts/getmyip.js -skriptillä ja <port> on aina 7021.

Playwright-konfiguraation webServer-osuus [playwright.config.ts](client/playwright.config.ts) käynnistää
esimerkki-applikaation frontin ja backendin, sekä luovutuspalvelun.

Palvelut (1)-(3) saa manuaalitestausta varten käynnistettyä:

    npm run start-with-server-and-luovutuspalvelu

# Ympäristöön vienti

Github actionsista käynnistettävissä, ks. [omadataoauth2sample_deploy.yml](../.github/workflows/omadataoauth2sample_deploy.yml)
