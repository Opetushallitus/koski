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
- `npm test` ajaa testit
- `npm run build` kääntää lähdekoodit kansioon `./dist`
- `npm run lint` tarkastaa koodin tyypitykset ja formatoinnin
- `npm run fix` korjaa formatointivirheet
- `npm run clean` tyhjentää Parcelin välimuistin ja käännöskansion. Aja jos kääntäminen sekoilee esim. rebasen jälkeen.

## Hakemistorakenne

```
├── src
│   ├── components      Uudelleenkäytettävat React-komponentit, niiden tyylit sekä niiden testit
│   ├── style           Globaalit tyylitiedostot sekä muuttujat: värit, fonttikoot, mitat jne.
│   ├── utils           Omat apukirjastot
│   └── views           Näkymäkomponentit, tilanhallinta
└── test                Testien lisäkonfiguraatiot, apukirjastot ja testidata
    ├── mocks           Mockit
    └── snapshots       Jestin snapshotit (eivät tallennu testitiedoston luo, kuten oletuksena)
```
