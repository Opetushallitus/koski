# Todennetun Osaamisen Rekisteri (TOR)

Todennetun osaamisen rekisteri (TOR) tulee toimimaan kattavana opetustoimialan tietovarantona, joka tarjoaa
tutkintoon johtavat suoritustiedot eri koulutusasteilta. Yleinen TOR-dokumentaatio kootaan CSC:n wikiin: https://confluence.csc.fi/display/OPHPALV/Todennetun+osaamisen+rekisteri

Tässä git-repositoriossa on TOR-järjestelmän ohjelmakoodi, tietokannan rakennuslausekkeet ja tekninen dokumentaatio ohjelmistokehitystä varten.

TOR rakennetaan avoimen lähdekoodin periaatteilla ja järjestelmästä on mahdollista käynnistää kehitysinstanssi omalla työasemalla alustariippumattomasti (ainakin Linux, OSX tuettu).

## Käsitteet

Keskeiset entiteetiut, ja järjestelmät, joihin nämä tallennetaan.

| käsite         | selite                                       | tunniste         | tallennuspaikka        |
|----------------|----------------------------------------------|------------------|------------------------|
| Oppija         | Opiskelija, oppilas.                         | henkilöOid       | Henkilöpalvelu         |
| Organisaatio   | Oppilaitos, kunta, eri rooleissa             | organisaatioOid  | Organisaatiopalvelu    |
| Komo           | Koulutusmoduuli                              | ?                | ePerusteet             |
| Komoto         | Koulutusmoduulin toteutus (komo+aika+paikka) | id (numeerinen)  | TOR                    |
| Suoritus       | Oppijan suoritus (komoto, oppija, organisaatio, aika...) | id (numeerinen)  | TOR                    |
| Koodisto       | Kooditus objekteille, esim tutkintonimikkeet | id (tekstiä)     | Koodistopalvelu        |
| Koodi          | Yksittäisen objektin koodi koodistossa       | id (tekstiä)     | Koodistopalvelu        |
| Koodistoviite  | Viittaus koodistoon ja koodiin Suorituksesta | id               | TOR                    |

TOR-palvelun tietokantaskeema löytyy täältä: https://github.com/Opetushallitus/tor/blob/master/src/main/resources/db/migration/V1__init.sql

## Teknologiat

- PostgreSQL-tietokanta
- Scala 2.11.4 ohjelmointikieli ja kääntäjä
- Scalatra web framework
- Slick (http://slick.typesafe.com/doc/3.0.1/index.html) relaatiokantakirjaso ja slick-codegen koodigeneraattori
- Flyway migraatiotyökalu kannan skeeman rakentamiseen ja päivittämiseen kehityksessä ja tuotannossa
- Maven build-työkalu kehityskäyttöön ja asennettavan paketin rakentamiseen
- Mvn-depsujen lataus Jitpackilla, jolloin voidaan viitata suoraan Github-repoihin, eikä tarvitse itse buildata jar-artifaktoja

## Kehitystyökalut

Minimissään tarvitset

- Git
- Maven 3.x
- Postgres (osx: `brew install postgres`)
- Tekstieditori (kehitystiimi käyttää IntelliJ IDEA 14)

## Paikallinen Postgres-tietokanta

Kehityskäyttöön tarvitaan paikallinen Postgres-tietokanta. Alla sen pystytykseen ja käynnistykseen tarvittavat ohjeet.

### Kannan alustus

Asenna ensin postgres, ja kloonaa tämä repositorio. Sitten tor-hakemistossa seuraavasti:

    initdb -d postgres
    
### Postgren käynnistys

Käynnistä toria varten postgres-palvelin:

    postgres -D postgres

Palvelin jää pyörimään konsoliin ja voit sammuttaa sen painamalla ctrl-c.
    
### Kannan ja käyttäjän luonti

Kun postgre on käynnissä, pitää vielä luoda sinne tietokanta ja käyttäjä.

    createdb -E UTF-8 tor
    createuser -s tor -P  (salasanaksi tor)
    
### Skeeman luonti/migraatio

Skeema luodaan flywayllä migraatioskripteillä, jotka ovat hakemistossa `src/main/resources/db/migration`.
    
    mvn compile flyway:migrate
    
### SQL-yhteys paikalliseen kantaan

Jos ja kun haluat tarkastella paikallisen kehityskannan tilaa SQL-työkalulla, se onnistuu esimerkiksi Postgren omalla komentorivityökalulla `psql`:

    psql -v schema=tor --dbname=tor tor
    
Peruskomennot

    \dt    listaa taulut
    \q     poistuu psql:stä
    
Sitten vaikka

    select * from arviointi;
    
### Kantamigraatiot

Migraatiot ovat hakemistossa `src/main/resources/db/migration`. Migraation ajo paikalliseen kantaan tällä:
 
    mvn clean compile flyway:migrate 

Jos haluat tehdä migraatiot puhtaaseen kantaan, aja

    mvn clean compile flyway:clean flyway:migrate 

Uusia migraatioita tehdessä tulee myös ajaa koodigeneraattori,
joka generoi tauluja vastaavat luokat `src/main/scala/fi/oph/tor/db/Tables.scala` -tiedostoon. Koodigeneraattorin `fi.oph.tor.db.CodeGeneator`
voit ajaa IDE:ssä tai komentoriviltä
 
    mvn compile exec:java -Dexec.mainClass="fi.oph.tor.db.CodeGenerator"

Koodigeneraattori ottaa yhteyden paikalliseen kantaan, jonka rakenteesta se generoi koodin. Koodigeneraattorin luomia
luokkia käytetään vain tietokantaoperaatioihin, eikä siis käytetä järjestelmän sisäisenä tietomallina, saati sitten paljateta ulospäin.
Koodigenerointi on käytössä siksi, että kannan skeema ja sovelluskoodi varmasti pysyvät synkassa. Jos esim. tauluun lisätään uusi pakollinen
kenttä, seuraa siitä käännösvirhe, kunnes softa käsittelee tämän kentän.
    
## Maven-buildi

### Testit

Huom! Testit vaativat yllä kuvatun PostgreSQL-setupin.

Aja kaikki testit

`mvn test`

### War-paketointi

`mvn package`

### TOR-sovelluksen ajaminen kehitystyöasemalla

Varmista, että Postgre on alustettu ja käynnistetty yllä olevien ohjeiden mukaisesti.

Aja JettyLauncher-luokka IDEAsta/Eclipsestä, tai käynnistä TOR vaihtoehtoisesti komentoriviltä

    mvn compile exec:java -Dexec.mainClass="fi.oph.tor.jettylauncher.JettyLauncher"

Avaa selaimessa http://localhost:7021/tor/
Suoritus-testidatat näkyy http://localhost:7021/tor/suoritus/
