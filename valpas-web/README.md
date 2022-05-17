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

- `npm start` kääntää lähdekoodin, jää kuuntelemaan muutoksia ja kopioi tiedostot ../target/webapp/ -hakemistoon
  Koskessa hostaamista varten. Kun Koski on käynnissä, löytyy Valpas:
  - Virkailijan näkymä: http://localhost:7021/koski/valpas/v2/virkailija
  - Oppijan näkymä: http://localhost:7021/koski/valpas/v2
- `npm test` ajaa kaikki testit
  - `npm run test:unit` ajaa vain
  - `npm run test:integration` ajaa vain integraatiotestit (polun `test/integrationtests` alla olevat testit, jotka vaativan backendin)
  - `npm run test:integration:browserstack` ajaa integraatiotestit Browserstackia vasten
- `npm run build:local` kääntää lähdekoodit kansioon `./dist-nonce` asetuksilla, joissa backend löytyy localhostista, ja kopioi
  sisällön tarpeellisin muutoksin Koskessa hostaamista varten ../target/webapp/ -hakemistoon.
- `npm run build:prod` kääntää tuotantoversion
- `npm run lint` tarkastaa koodin tyypitykset ja formatoinnin
- `npm run fix` korjaa formatointivirheet
- `npm run clean` tyhjentää Parcelin välimuistin ja käännöskansiot. Aja jos kääntäminen sekoilee esim. rebasen jälkeen.

### Hakemistorakenne

```
├── src                     Lähdekoodit ja yksikkötestit niiden rinnalla
│   ├── views               Näkymäkomponentit
│   └── *                   Uudelleenkäytettävät komponentit ja tyylit yms.
└── test                    Integraatiotestit
```

## Backend

Valpas käyttää Koskea backendinä.

## Testit

### Frontendin yksikkö-/komponenttitestit

Yksikkötestit sisältävät frontendin React-komponenttien, apukirjastojen, tilanhallinnan yms. testit ja ne voidaan ajaa ilman backendiä.

Voit ajaa yksikkötestit komennolla `npm run test:unit`.

### Integraatiotestit

Integraatiotestejä varten pitää Koski-backendin olla pystyssä (oletuksena `localhost:7021`) tai testit pitää käynnistää ajamalla `ValpasFrontSpec.scala`.
Jos backend on valmiiksi ajossa, voi testit ajaa komennolla `npm run test:integration`.

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

## Käyttäjätunnukset

Omalta koneelta ajaessa sisään voi kirjautua mm. tunnuksilla `valpas-monta`, `valpas-helsinki` tai `valpas-jkl-normaali` (käyttäjätunnus ja salasana ovat samat).

Mock-käyttäjät luodaan `ValpasMockUsers.scala` -tiedostossa

## Virkailija- ja oppijaraamit

Ks. ohjeet Kosken käynnistämisestä proxytettyjen raamien kanssa:
https://github.com/Opetushallitus/koski/blob/master/documentation/raamien-ajo-lokaalisti.md

Tämän jälkeen Valpas-buildin saa käyttämään Kosken hostaamia raameja:

```
npm run start:raamit
```

## CAS-kirjautumisen testaaminen lokaalisti

Käynnistä Koski-backend seuraavalla lisäkonfiguraatiolla, jossa **etunimi.sukunimi** on sama käyttäjänimi kuin esim. untuvapolussa:

```
mock.casClient.usernameForAllVirkailijaTickets="etunimi.sukunimi"
```

Lisää tunnuksesi tiedostoon `src/main/scala/fi/oph/koski/valpas/valpasuser/ValpasMockUsers.scala`.

Lisää **.env** -tiedostoon seuraava rivi:

```
OPINTOPOLKU_VIRKAILIJA_URL=https://virkailija.untuvaopintopolku.fi
```

Parcel ei välttämättä tajua ympäristömuuttujien vaihtuneen. Korjaa ongelma ajamalla `npm run clean`, joka tyhjentää välimuistin.
