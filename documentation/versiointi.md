## Tallennetun tiedon versiointi

Koski-tietokantaan tallennetaan viimeisimpien tietojen lisäksi myös versiohistoria opiskeluoikeuksittain. Historiatietojen
pohjalta voidaan nähdä kuka on muuttanut mitä ja milloin. Lisäksi historiatietojen avulla voidaan nähdä opiskeluoikeuden
tiedot minä tahansa ajankohtana.

Kun opiskeluoikeus lisätään Koskeen, syntyy `opiskeluoikeus` -tauluun rivi

    ID    VERSIONUMERO    ...    DATA
    1234  1                      { "tyyppi": { "koodiarvo: ... }
    
Lisäksi syntyy vastaava rivi `opiskeluoikeushistoria` -tauluun:

    OPISKELUOIKEUS_ID    VERSIONUMERO   AIKALEIMA             KAYTTAJA_OID                MUUTOS
    1234                 1              2017-02-10 13:06:40   1.2.246.562.24.99999999987  [{"op": "add", "path": "", "value": ...}]
    
Kun opiskeluoikeutta muutetaan, tallennetaan uusin tieto `opiskeluoikeus`-tauluun ja versionumero päivitetään arvoon 2. Lisäksi
`versiohistoria`-tauluun syntyy uusi rivi:

    OPISKELUOIKEUS_ID    VERSIONUMERO   AIKALEIMA             KAYTTAJA_OID                MUUTOS
    1234                 2              2017-02-11 11:51:10   1.2.246.562.24.99999999987  [{ "op": "replace", "path": "/suoritukset/0/alkamispäivä" ... }]
    
Historiariveille siis tallennetaan muuttajan käyttäjä-oid ja aikaleima ja lisäksi JSONB-muotoinen diff muutoksesta. Näiden, tyypillisesti vähän tilaa vievien
diffien pohjalta voidaan palata takaisin mihin tahansa tallennettuun versioon.

Tallennetun tiedon versiontia voi testata/demota Kosken dokumentaatiosivulla (esimerkiksi testiympäristössä https://dev.koski.opintopolku.fi/koski/documentation).

## Tietomallin versiointi

