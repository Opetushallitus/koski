# Rikkinäisen opiskeluoikeushistorian debuggaustyökalu

Oppijan opiskeluoikeushistoria on hajalla, eikä jonkin version lataaminen onnistu, koska se epäonnistuu JsonPatch-kirjaston virheeseen.
Tällä työkalulla voit selvitellä tarkemmin, mikä kohta patchissa on rikki ja helpottaa manuaalisen korjauksen tekoa.

## 1. Asennus

`npm install`

## 2. Tutkittavan datan lisäys

Lataa ensin kannasta tutkittavat opiskeluoikeushistoriat. Operatiivisesta kannasta sen voi tehdä kyselyllä:

```sql
select
	opiskeluoikeushistoria.muutos,
	opiskeluoikeushistoria.versionumero
from opiskeluoikeushistoria
join opiskeluoikeus on opiskeluoikeus.id = opiskeluoikeushistoria.opiskeluoikeus_id
where opiskeluoikeus.oid = 'tutkittavan opiskeluoikeuden oid'
order by opiskeluoikeushistoria.versionumero;
```

Tallenna tuloksen json-muodossa niin, että jokainen rivi muodostaa oman json-objektin.
Posticossa tämä onnistuu valitsemalla kaikki tulosrivit, kontekstiklikkaus, Copy Special > Copy JSON Lines.
Korvaa tuloksella tiedoston `historia.jsons` sisältö.

## 3. Debuggaus

Aja `npm start`

Vastaus on jotain tämän tyylistä:

```
Versionumero 2 aiheuttaa virheen: Replace operation must point to an existing value!

muutos[1]: { op: 'replace', path: '/voiei', value: 'Tämä menee pieleen' }
```

Ensimmäinen rivi kertoo missä opiskeluhistorian versiossa virhe tapahtui ja mikä meni pieleen.
Alempi rivi kertoo tarkalleen, mikä patchin kohta aiheutti virheen.

Virheen sattuessa kirjoitetaan seuraavat tiedostot:

- `dump.json`, josta löytyy tila, mihin asti opiskeluoikeus saatiin rakennettua ennen virhettä.
- `hotfix-{versionumero}.json`, josta löytyy koko viallinen json patch

## 4. Korjaaminen

Avaa luotu `hotfix-{versionumero}.json` ja tee tarvittavat korjaukset patchiin. Aja debuggaus uudelleen.

Lopulta kun historia on korjattu, kirjoita hotfix-tiedosto(je)n sisällöt tietokantaan.
