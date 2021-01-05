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
