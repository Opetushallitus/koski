# EditorModel

Koski käyttää datan esittämiseen, manipulointiin ja tallentamiseen EditorModelia. EditorModel on
rakennettu `scala-schema`-kirjaston päälle, jolla tietomalli serialisoidaan JSON-objektiksi ja deserialisodaan
JSON-objektista tietomallia esittäväksi luokkahierarkiaksi Kosken back-endissä. Annotaatioilla päätetään, mihin
tietomallin haaraan JSON-objekti halutaan deserialisoida. Annotaatiolla voidaan myös validoida dataa, esim. mitä
sallittuja koodiston arvoja haaralla on.

Kosken front-endissä EditorModel on toteutettu FRP-kirjasto Bacon.js:llä. Mutaatiot tietomallin dataan käyttävät
Bacon.js:n Streameja ja tiedonhaussa Propertyjä.

## Esimerkkejä

Seuraavissa esimerkeissä käytetään Kosken dokumentaation (https://virkailija.untuvaopintopolku.fi/koski/json-schema-viewer#koski-oppija-schema.json), joka esittää Kosken tietomallin rakenteen. Vastatkoon `opiskeluoikeusModel` koskeen tallennettua opiskeluoikeutta.

### Propertyn haku mallista

```typescript
export const Suoritukset = ({opiskeluoikeusModel}) => {
    // Haetaan suoritukset
    const suoritukset = modelLookup(opiskeluoikeusModel, "suoritukset")
    return modelItems(suoritukset).map(suoritus =>
        <Suoritus model = {suoritus} />)
}
```
