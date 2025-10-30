# Valpas

## Vaatimukset

- Node (tarkempi versio tiedostossa `/.nvmrc`)

### Suositellaan

- nvm

## Asennus

```
nvm install
nvm use
pnpm install --frozen-lockfile
```

## Kehitys

- `pnpm start` kääntää lähdekoodin, jää kuuntelemaan muutoksia ja kopioi tiedostot ../target/webapp/ -hakemistoon
  Koskessa hostaamista varten. Kun Koski on käynnissä, löytyy Valpas:
  - Virkailijan näkymä: http://localhost:7021/valpas/virkailija
  - Oppijan näkymä: http://localhost:7021/valpas
- `pnpm test` ajaa kaikki testit
  - `pnpm run test:unit` ajaa vain
  - `pnpm run test:integration` ajaa vain integraatiotestit (polun `test/integrationtests` alla olevat testit, jotka vaativan backendin)
  - `pnpm run test:integration:debug -- -t "Näyttää listan oppijoista European School of Helsingille"` ajaa vain hakuehtoon sopivat testit
  - `env SHOW_BROWSER=true npx jest --config jest.integrationtests.config.js test/integrationtests/kansalainen.test.ts` ajaa yhden testitiedoston avoimessa selaimessa
- `pnpm run build:local` kääntää lähdekoodit kansioon `./dist-nonce` asetuksilla, joissa backend löytyy localhostista, ja kopioi
  sisällön tarpeellisin muutoksin Koskessa hostaamista varten ../target/webapp/ -hakemistoon.
- `pnpm run build:prod` kääntää tuotantoversion
- `pnpm run lint` tarkastaa koodin tyypitykset ja formatoinnin
- `pnpm run fix` korjaa formatointivirheet
- `pnpm run clean` tyhjentää Parcelin välimuistin ja käännöskansiot. Aja jos kääntäminen sekoilee esim. rebasen jälkeen.

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

Voit ajaa yksikkötestit komennolla `pnpm run test:unit`.

### Integraatiotestit

Integraatiotestejä varten pitää Koski-backendin olla pystyssä (oletuksena `localhost:7021`) tai testit pitää käynnistää ajamalla `ValpasFrontSpec.scala`.
Jos backend on valmiiksi ajossa, voi testit ajaa komennolla `pnpm run test:integration`.

Testit ajetaan headless-selaimessa. Jos haluat selainikkunan näkyviin, aja testit komennolla `pnpm run test:integration:debug`

## Käyttäjätunnukset

Omalta koneelta ajaessa sisään voi kirjautua mm. tunnuksilla `valpas-monta`, `valpas-helsinki` tai `valpas-jkl-normaali` (käyttäjätunnus ja salasana ovat samat).

Mock-käyttäjät luodaan `ValpasMockUsers.scala` -tiedostossa

## Virkailija- ja oppijaraamit

Ks. ohjeet Kosken käynnistämisestä proxytettyjen raamien kanssa:
https://github.com/Opetushallitus/koski/blob/master/documentation/raamien-ajo-lokaalisti.md

Tämän jälkeen Valpas-buildin saa käyttämään Kosken hostaamia raameja:

```
pnpm run start:raamit
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

Parcel ei välttämättä tajua ympäristömuuttujien vaihtuneen. Korjaa ongelma ajamalla `pnpm run clean`, joka tyhjentää välimuistin.
