# Koski
Koski toimii kattavana opetustoimialan tietovarantona, joka tarjoaa
tutkintoon johtavat suoritustiedot eri koulutusasteilta. Yleinen Koski-dokumentaatio
kootaan [wikiin](https://wiki.eduuni.fi/display/OPHPALV/Koski).


## Kosken hiekkalaatikkoympäristö tiedon hyödyntäjille

Koskesta on saatavilla hiekkalaatikkoympäristö tiedon hyödyntäjille, jonka kautta
Kosken toimintaan voi tutustua.


## Käyttäjätunnukset

Lista kaikista käytössä olevista käyttäjistä löytyy
[Githubista](https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/koskiuser/MockUsers.scala).
Käyttäjätunnus ja salasana ovat samat ja ne löytyy toisena argumenttina. Esimerkiksi riviltä
`val kalle = MockUser("käyttäjä", "kalle", "1.2.246.562.24.99999999987", (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja).toSet)`
käyttäjätunnus ja salasana ovat kalle.


## Tiedonhyödyntäjien rajapinnat

Tiedon hyödyntäjien rajapinnat on dokumentoitu osana [koskipalvelua](https://sandbox.dev.koski.opintopolku.fi/koski/dokumentaatio).

### Luovutuspalvelu

Dokumentaatio: https://sandbox.dev.koski.opintopolku.fi/koski/dokumentaatio/rajapinnat/luovutuspalvelu

Esimerkki viranomainen: `curl --header "Content-Type: application/json" --data '{ "hetu": "020655-2479" }' "https://sandbox.dev.koski.opintopolku.fi/koski/api/luovutuspalvelu/migri/hetu" --user Lasse:Lasse`

Esimerkki tilastokeskus: `curl "https://sandbox.dev.koski.opintopolku.fi/koski/api/luovutuspalvelu/haku?v=1&pageSize=10&pageNumber=0&opiskeluoikeudenTyyppi=ammatillinenkoulutus&muuttunutJälkeen=2015-06-24T15:24:49Z" --user Teppo:Teppo`

### Opintohallintarajapinnat

Dokumentaatio: https://sandbox.dev.koski.opintopolku.fi/koski/dokumentaatio/rajapinnat/oppilashallintojarjestelmat

Esimerkki: `curl "https://sandbox.dev.koski.opintopolku.fi/koski/api/henkilo/search?query=eero" --user kalle:kalle`

Dokumentaatiossa on mahdollisuus luoda erilaisia kokeiluun soveltuvia pyyntöjä.

### Palveluväylä

Palveluväylä rajapintaa ei ole saatavilla hiekkalaatikkoympäristössä.


## Virkailijan käyttöliittymä

Virkailijan käyttöliittymään voi tutustua osoitteessa: https://sandbox.dev.koski.opintopolku.fi/koski/virkailija

Käyttäjätunnus/salasana: Esimerkiksi kalle/kalle

HUOMIOI, ETTÄ YMPÄRISTÖSSÄ VAIN LUKUOMINAISUUDET TOIMIVAT.
KAIKKI LISÄYS- JA PÄIVITYSTOIMINNOT AIHEUTTAVAT VIRHEEN.

## Kansalaisen käyttöliittymä

Kansalaisen käyttöliittymään voi tutustua osoitteessa: https://sandbox.dev.koski.opintopolku.fi/koski

Sisäänkirjautumiseen voi valita testikäyttäjän sisäänkirjautumissivulta.

HUOMIOI, ETTÄ YMPÄRISTÖSSÄ VAIN LUKUOMINAISUUDET TOIMIVAT.
KAIKKI LISÄYS- JA PÄIVITYSTOIMINNOT AIHEUTTAVAT VIRHEEN.
