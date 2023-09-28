# Mockdatan päivittäminen

## Koodisto

Päivitä lokaali koodisto vastaamaan tuotannossa olevia datoja ajamalla skriptin `scripts/fetch_prod_koodistot.sh`

Skripti jättää osan isommista koodistoista lataamatta. Näihin koodistoihin tehtävät päivitykset pitää tehdä
itse käsipelillä. Tuotannossa olevan datan saa seuraavien rajapintojen kautta:

`koodisto/koodistot` -kansioon tuleva data: https://virkailija.opintopolku.fi/koodisto-service/rest/codes/{KOODISTO}/{VERSIO}
`koodisto/koodit` -kansioon tuleva data: https://virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/withrelations/{KOODISTO}/{VERSIO}

## Lokalisointi

Päivitä lokaalit käännökset vastaamaan tuotannossa olevia ajamalla skripti `scripts/fetch_prod_localizations.sh`

Skripti päivittää sekä Kosken että Valppaan käännökset ja tekee muutokset default-text-\*.json -tiedostoihin.

## Tutkintojen rakenteet

Päivitä tutkintojen rakenteet vastaamaan tuotannossa olevia versioita tietyin poikkeuksin ajamalla `fetch_prod_tutkintojen_rakenteet.sh`

Skripti hakee tiedot automaattisesti ainoastaan niille rakenteille, jotka on tallennettuna `eperusteet` -kansioon `rakenne-` -etuliitteellä
ja joiden datassa on sekä `diaarinumero` että tuotannosta löytyvä kelvollinen `id`. Moniin rakenteisiin on tehty paikallista testaamista
varten muokkauksia, jotka löytyvät tiedostosta, jonka nimi on tyyppiä `rakenne-esimerkki.json.patch`. Tiedostoon voi laittaa mitä tahansa
muokkauksia, jotka voi ajaa `jq`:lla json-datalle.

Voit päivittää patchin jsonin päälle näin: `scripts/fetch_prod_tutkintojen_rakenteet.sh "rakenne-esimerkki.json"`

Esimerkkejä:

```
.voimassaoloLoppuu = null
```

Erota eri muokkaukset toisistaan `|` -merkillä. Tiedostossa voi käyttää rivinvaihtoja.

```
.voimassaoloLoppuu = null
| .siirtymaPaattyy = null
```

Taulukon sisään viitataan näin:

```
.koulutukset[0].koulutuskoodiArvo = "357304"
| .koulutukset[0].koulutuskoodiUri = "koulutus_357304"
```

Ja taulukkoon voi lisätä uusia tietuieita näin:

```
.koulutukset += [{
        "nimi": {
        "fi": "Autoalan työjohto (lisätty paikallista testaamista varten)",
        "sv": "Grundexamen inom bilbranschen",
        "en": "Vehicle Technology, VQ",
        "_id": "285307"
        },
        "koulutuskoodiArvo": "457305",
        "koulutuskoodiUri": "koulutus_457305",
        "koulutusalakoodi": "koulutusalaoph2002_5",
        "opintoalakoodi": "opintoalaoph2002_509"
    }]
```
