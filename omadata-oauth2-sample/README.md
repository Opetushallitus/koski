# OmaData OAuth2 esimerkki-applikaatio

Tässä hakemistossa on 2 esimerkkiapplikaatiota:

Node-JS -versio, jonka lähdekoodi alihakemistoissa [client](client/) ja [server](server/). Kehitysohjeet tälle alla.

Java-versio, jonka lähdekoodi hakemistossa [java](java/). Ks. [java/README.md](java/README.md).

## Node-JS -esimerkkiapplikaatio

### Kehitys

Vaatii, että Koski on käynnissä portissa 7021. ks. [README.md](../README.md#koski-sovelluksen-ajaminen-paikallisesti) ja 
että [koski-luovutuspalvelu](https://github.com/Opetushallitus/koski-luovutuspalvelu) -repositoryn sisältö on koski-hakemiston juuressa alihakemistossa

    /koski-luovutuspalvelu

Asenna riippuvuudet:

    /omadata-oauth2-sample/server % pnpm install --frozen-lockfile
    /smoketests % pnpm install --frozen-lockfile   # paikallista end-to-end -smoketestia varten

Käynnistä palvelut (esimerkit kahdessa terminaalissa):

    /omadata-oauth2-sample/server % pnpm run start:luovutuspalvelu:build
    /omadata-oauth2-sample/server % pnpm run start:local

Tämän jälkeen Node-esimerkki (UI + API) löytyy osoitteesta http://localhost:7051.

Koko putken rakenne:

    (1) Esimerkki-applikaatio (UI + API), http://localhost:7051

       kutsuu:
    (2) Docker-kontissa ajettu koski-luovutuspalvelu, https://localhost:7022
          Huom! Tämä on mutual TLS -putken testaamiseksi https, lokaalilla root-CA:lla.

      kutsuu:
    (3) Koski-backend, http://<lokaali-ip>:<port>
          Luovutuspalvelun docker-kontille näkyvä <lokaali-ip> on Github Actions CI:llä ajettaessa
          172.17.0.1 ja <port> arvottu.
          Lokaalisti <lokaali-ip> haetaan server/scripts/getmyip.js -skriptillä ja <port> on aina 7021.

Koko putken voi pyöräyttää ja ajaa smoketestin yhdellä komennolla (käynnistää luovutuspalvelun ja Node-apin, käyttää smoketests-skriptiä):

    /scripts/omadata-oauth2-e2e-test.sh 7021

### Node-JS -version ympäristöön vienti

Github actionsista käynnistettävissä, ks. [omadataoauth2sample_deploy.yml](../.github/workflows/omadataoauth2sample_deploy.yml)

## E2E-testit

Paikallinen smoketesti hyödyntää puppeteeria ja käyttää samoja klikkipolkuja kuin aiempi Playwright-setti.

- Käynnistä luovutuspalvelu ja Node-appi (ks. yllä).
- Aja testi:

      /omadata-oauth2-sample/server % pnpm run test:e2e

Testi käyttää oletuksena `http://localhost:7051`, mutta osoitteen voi ylikirjoittaa muuttujalla `OMADATA_URL`.
