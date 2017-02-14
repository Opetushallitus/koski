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

Versiohistoria näkyy myös virkailijan käyttöliittymässä opiskeluoikeuksittain.

## Tietomallin versiointi

Kosken tietomalli on suunniteltu tukemaan paitsi eri koulutustyyppien opiskeluoikeuksia ja suorituksia, myös näiden eri
versioita. Esimerkiksi ammatillisen koulutuksen reformin odotetaan tuovan tietomallin ammatillista koulutusta koskeviin
 osiin joitain muutoksia.
 
### Taaksepäin yhteensopivuus

Ehdoton vaatimus tietomallille on, että kaikki Koskeen tallennetut tiedot tulee voida lukea ja näyttää Koskesta kaikilla
tulevaisuuden versioilla Koski-applikaatiosta. Tämä tarkoittaa käytännössä sitä, että tietomallista ei koskaan voida
poistaa kenttiä ja rakenteita, jotka on sinne lisätty, ja joita on tallennettu Kosken tuotantotietokantaan. Taaksepäin
yhteensopivuuden säilyvyys on varmistettu automaattisella testillä 
[BackwardCompatibilitySpec](https://github.com/Opetushallitus/koski/blob/master/src/test/scala/fi/oph/koski/versioning/BackwardCompatibilitySpec.scala),
joka varmistaa, että aiemmin tallennetut JSON-dokumentit ovat virheettömästi luettavissa uusimmall versiolla Koski-ohjelmakoodista.

Joitain muutoksia tietomalliin voidaan tehdä rikkomatta taaksepäin yhteensopivuutta. Esimerkiksi

- Uuden, ei-pakollisen kentän lisääminen
- Uuden vaihtoehdon lisääminen (esimerkiksi tuki jollekin uuden tyyppiselle opiskeluoikeudelle tai suoritukselle)
- Lisäykset ja muutokset koodistoihin (koodistoja ei tallenneta Koskeen, mutta Koskeen tallenentuissa koodistoviittauksissa tallennetaan aina käytetyn koodiston versionumero)

Uusia pakollisia kenttiä ei kuitenkaan voi taaksepäin yhteensopivasti lisätä, eikä myöskään poistaa olemassa olevia kenttiä,
tai muuttaa niiden rakennetta. Jos tehdyt muutokset ovat taaksepäin yhteensopimattomia, ei automaattinen testi mene läpi.

### Koodistoversiot

Kaikki "valitse 1 vaihtoehto" -tyyppiset kentät Kosken tietomallissa on toteutettu käyttäen Opintopolun Koodistopalvelua.
Käytännössä näiden kenttien arvo siirretään koskeen muodossa

```json
{
    "koodiarvo" : "ammatillinenkoulutus",
    "koodistoUri" : "opiskeluoikeudentyyppi"
}
```

Tässä `koodistoUri` määrittelee käytettävän koodiston ja `koodiarvo` kyseisestä koodistosta löytyvän koodin. Näiden lisäksi
voidaan käyttää kenttää `koodistoVersio`, jos tiedetään, mihin versioon koodistosta halutaan viitata. Käytännössä tietoja
Koskeen siirtävillä organisaatioilla ei kuitenkaan ole tiedossa koodistojen versionumeroita, joten he siirtävät tiedot
ilman versionumeroita. Tällöin käytetään koodiston uusinta versiota.

Koska Koskeen tallennettuja tietoja on voitava katsella myös tulevaisuudessa, tallennetaan koodistoviitteet Koski-kantaan
aina täydennetyssä muodossa, joka sisältää myös käytetyn koodiston version ja koodin kuvauksen:

```json
{
    "koodiarvo": "ammatillinenkoulutus",
    "nimi": {
        "fi": "Ammatillinen koulutus"
    },
    "lyhytNimi": {
        "fi": "Ammatillinen koulutus"
    },
    "koodistoUri": "opiskeluoikeudentyyppi",
    "koodistoVersio": 1
}
```


### Scheman haaroitus

Jos tiedon rakenne tai pakolliset kentät muuttuvat, on schemaan ja/tai validaattoriin tehtävä "haara". 

Esimerkiksi jos haluttaisiin, että ammatillisten
opintojen opiskeluoikeuksiin tulisi vuodesta 2018 lähtien uusi pakollinen kenttä K, olisi ammatillisen opiskeluoikeuden
rakenne haaroitettava siten, että aiemmin tallennetut opiskeluoikeudet olisivat edelleen valideja. Yksinkertaisin tapa
tehdä tämä haaroitus olisi lisätä uusi kenttä ei-pakollisena ja kirjoittaa kosken validaatiokoodiin [KoskiValidator](https://github.com/Opetushallitus/koski/blob/3f22f81547574c1bb63c78c3467a6ca142b85df8/src/main/scala/fi/oph/koski/validation/KoskiValidator.scala) 
ehto (pseudokoodia):

    IF alkamisvuosi >= 2018 and K.isEmpty THEN ERROR "Kenttä K on pakollinen vuoden 2018 jälkeen"
    
Jos muutokset ovat laajempia, voi olla kuitenkin järkevää haaroittaa schema "isommin", eli tehdä [AmmatillinenOpiskeluoikeus](https://github.com/Opetushallitus/koski/blob/fe5a01459fec18b4a32465040bd7d558f1ff6509/src/main/scala/fi/oph/koski/schema/Ammatillinen.scala#L10)
-luokasta rinnakkainen versio, esimerkiksi AmmatillinenOpiskeluoikeus2018, jossa olisi hieman erilaiset kenttämäärittelyt.
Lisäksi pitäisi määritellä, miten lähetetystä JSON-datasta tunnistetaan, käytetäänkö vanhempaa vai uudempaa opiskeluoikeusrakennetta.
Tämä on opiskeluoikeuksien tapauksessa tehtävissä suoraviivaisesti esimerkiksi opiskeluoikeuden tyypin (koodistoarvo) tai
tyypin koodistoversion perusteella, ks. [OpiskeluoikeusDeserializer](https://github.com/Opetushallitus/koski/blob/e0e5d220b1a8a1f90bf43366426d75c395814d5a/src/main/scala/fi/oph/koski/schema/Deserializers.scala#L174).

### Case lukion kurssit

Lukion kursseille käytetään koodistoa `lukionkurssit`, jossa on uusimman opetussuunnitelman mukaiset kurssit. Ilmeni tarve
tukea myös vanhempien opetussuunnitelmien kursseja. Tässä tapauksessa ei ole mahdollisuutta hyödyntää koodistojen versiointia,
koska nyt pitäisi lisätä nimenomaan tuettua vanhempia versioita ja koodiston versiohistoriaa ei ole mahdollista/järkevää
lähteä muuttamaan. Tässä tapauksessa päädyimme luomaan uudet koodistot `lukionkurssitops2004aikuiset` ja `lukionkurssitops2003nuoret`, joissa
on vanhemman opetussuunnitelman mukaiset kurssit. 

Näiden koodistojen käyttöönotto Koskessa tapahtui lisäämällä 
`ValtakunnallinenLukionKurssi`-luokan `tunniste`-kenttään lisää `@KoodistoUri`-annotaatioita uusia hyväksyttyjä koodistoja varten.
Nyt siis Koski hyväksyy lukion kursseille myös näiden koodistojen mukaisia koodeja. Käytetty koodisto määritellään
Koskeen tallennettavassa JSON-dokumentissa `koodistoUri`-kentässä.