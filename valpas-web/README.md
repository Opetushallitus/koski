# Valpas

## Vaatimukset

- Node (tarkempi versio tiedostossa `/.nvmrc`)

### Suositellaan

- nvm

## Asennus

```
nvm install
nvm use
npm install
```

## Kehitys

- `npm start` kääntää lähdekoodin, jää kuuntelemaan muutoksia ja käynnistää web-palvelimen osoitteeseen http://localhost:1234/
- `npm test` ajaa kaikki testit
  - `npm run test:unit` ajaa vain
  - `npm run test:integration` ajaa vain integraatiotestit (polun `test/integrationtests` alla olevat testit, jotka vaativan backendin)
  - `npm run test:integration:browserstack` ajaa integraatiotestit Browserstackia vasten
- `npm run build:local` kääntää lähdekoodit kansioon `./dist` asetuksilla, joissa backend löytyy localhostista
- `npm run build:prod` kääntää tuotantoversion
- `npm run lint` tarkastaa koodin tyypitykset ja formatoinnin
- `npm run fix` korjaa formatointivirheet
- `npm run clean` tyhjentää Parcelin välimuistin ja käännöskansion. Aja jos kääntäminen sekoilee esim. rebasen jälkeen.

### Hakemistorakenne

```
├── src                     Lähdekoodit ja yksikkötestit niiden rinnalla
│   ├── components          Uudelleenkäytettävat React-komponentit, niiden tyylit
│   ├── style               Globaalit tyylitiedostot sekä muuttujat: värit, fonttikoot, mitat jne.
│   ├── utils               Omat apukirjastot
│   └── views               Näkymäkomponentit, tilanhallinta
├── test                    Testien lisäkonfiguraatiot, apukirjastot ja testidata
│   ├── integrationtests    Integraatiotestit
│   ├── mocks               Mockit
│   └── snapshots           Jestin snapshotit (eivät tallennu testitiedoston luo, kuten oletuksena)
└── local_modules
    └── parcel-plugin-skip-external-assets
                            Paikallinen parcel-plugin raamien käsittelyä varten
```

## Backend

Valpas käyttää Koskea backendinä. Valpas-toiminnallisuus on toistaiseksi feature flagin `features.valpas` takana.

## Testit

### Frontendin yksikkö-/komponenttitestit

Yksikkötestit sisältävät frontendin React-komponenttien, apukirjastojen, tilanhallinnan yms. testit ja ne voidaan ajaa ilman backendiä.

Voit ajaa yksikkötestit komennolla `npm run test:unit`.

### Integraatiotestit

Integraatiotestejä varten pitää Koski-backendin olla pystyssä (oletuksena `localhost:7021`) tai testit pitää käynnistää ajamalla `ValpasFrontSpec.scala`.
Jos backend on valmiiksi ajossa, voi testit ajaa komennolla `npm run test:integration`.

Integraatiotestien ajon ajaksi käynnistyy porttiin 1234 http-palvelin, joka jakaa käännetyt frontendin tiedostot.

Testit ajetaan headless-selaimessa. Jos haluat selainikkunan näkyviin, aja testit komennolla `npm run test:integration:debug`

### BrowserStack-testien ajaminen

Voit ajaa integraatiotestit BrowserStackissa komennolla `npm run test:integration:browserstack`

Testin ajamista voi ohjata seuraavilla ympäristömuuttujilla. Tunnukset löydät sivulta https://www.browserstack.com/accounts/settings otsikon _Automate_ alta.

```
BROWSERSTACK_USERNAME         Käyttäjänimi
BROWSERSTACK_ACCESS_KEY       Access key
BROWSERSTACK_BROWSER          Selain (ja versio), esim. "firefox 80.0"
BROWSERSTACK_OS               Käyttöjärjestelmä (ja versio), esim. "windows 10"
```

## Virkailija-raamit

Paikallisen palvelimen käynnistys untuvaopintopolusta ladattavien raamien kanssa:

```
npm run start:raamit
```

Hakemistossa `local_modules/parcel-plugin-skip-external-assets` on paikallinen parcel-plugin raamiskriptin
käsittelyä varten. Jos päivität plugin-koodia, ota muutokset käyttöön paikallisesti:

```
                                                  $ cd local_modules/parcel-plugin-skip-external-assets
 local_modules/parcel-plugin-skip-external-assets $ npm version minor
                                                  $ cd ../..
                                                  $ npm upgrade parcel-plugin-skip-external-assets
```
