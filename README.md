# Todennetun Osaamisen Rekisteri (TOR)

Todennetun osaamisen rekisteri (TOR) tulee toimimaan kattavana opetustoimialan tietovarantona, joka tarjoaa
tutkintoon johtavat suoritustiedot eri koulutusasteilta. Yleinen TOR-dokumentaatio kootaan CSC:n wikiin: https://confluence.csc.fi/display/OPHPALV/Todennetun+osaamisen+rekisteri

Tässä git-repositoriossa on TOR-järjestelmän ohjelmakoodi, tietokannan rakennuslausekkeet ja tekninen dokumentaatio ohjelmistokehitystä varten.

TOR on [EUPL](LICENSE.txt)-lisensoitu sovellus, josta on mahdollista käynnistää kehitysinstanssi omalla työasemalla, alla olevien kehitysohjeiden mukaisesti. TOR-sovellus on alustariippumaton, sillä se pyörii Java-virtuaalikoneella. Kehitysympäristö toimii sellaisenaan ainakin Linux ja OSX-käyttöjärjestelmissä.

## Käsitteet

Keskeiset entiteetiut, ja järjestelmät, joihin nämä tallennetaan.

| käsite         | selite                                       | tunniste         | tallennuspaikka        |
|----------------|----------------------------------------------|------------------|------------------------|
| Oppija         | Opiskelija, oppilas.                         | henkilöOid       | Henkilöpalvelu         |
| Organisaatio   | Oppilaitos, kunta, eri rooleissa             | organisaatioOid  | Organisaatiopalvelu    |
| Komo           | Koulutusmoduuli                              | ?                | ePerusteet             |
| Komoto         | Kaikkiin opintosuorituksiin liittyvä         |                  |                        |
|                | koulutusmoduulin toteutus (komo+aika+paikka) | id (numeerinen)  | TOR                    |
| Suoritus       | Oppijan suoritus (komoto, oppija, organisaatio, aika...) | id (numeerinen)  | TOR        |
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

Minimissään tarvitset nämä:

- Git
- GNU Make
- Maven 3 (osx: `brew install maven`)
- Postgres (osx: `brew install postgres`)
- Tekstieditori (kehitystiimi käyttää IntelliJ IDEA 14)

## Paikallinen PostgreSQL-tietokanta

Kehityskäyttöön tarvitaan paikallinen PostgreSQL-tietokanta. TOR-sovellus luo paikallisen kannan, skeeman ja käyttäjän
automaattisesti ja käynnistää myös tarvittavan PostgreSQL-serveriprosessin.

Paikallisen kannan konfiguraatio on tiedostossa `postgresql/postgresql.conf` ja tietokannan datahakemisto on `postgresql/data`.

Jos haluat pitää Postgresin käynnissä erikseen, voit käynnistää sen komentoriviltä komennolla

    make postgres

PostgreSQL jää pyörimään konsoliin ja voit sammuttaa sen painamalla ctrl-c.

Käynnistyessään Tor-sovellus huomaa, jos tietokanta on jo käynnissä, eikä siinä tapauksessa yritä käynnistää sitä.

Kehityksessä käytetään kahta kantaa: `tor` jota käytetään normaalisti ja `tortest` jota käytetään automaattisissa
testeissä (tämä kanta tyhjennetään aina testiajon alussa). Molemmat kannat sisältävät `tor` -skeeman, ja sijaitsevat
fyysisesti samassa datahakemistossa.


### SQL-yhteys paikalliseen kantaan

Jos ja kun haluat tarkastella paikallisen kehityskannan tilaa SQL-työkalulla, se onnistuu esimerkiksi Postgren omalla komentorivityökalulla `psql`:

    psql tor tor
    psql tortest tor

Peruskomennot

    \dt    listaa taulut
    \q     poistuu psql:stä

Sitten vaikka

    select * from arviointi;

### Kantamigraatiot

Skeema luodaan flywayllä migraatioskripteillä, jotka ovat hakemistossa `src/main/resources/db/migration`.

Tor-sovellus ajaa migraatiot automaattisesti käynnistyessään.

Kannan rakennetta muuttavien migraatioiden yhteydessä tulee ajaa myös koodigeneraattori,
joka generoi tauluja vastaavat luokat `src/main/scala/fi/oph/tor/db/Tables.scala` -tiedostoon.
Koodigeneraattorin `fi.oph.tor.db.CodeGenerator` voit ajaa IDE:ssä tai komentoriviltä

    make codegen

Koodigeneraattori ajaa migraatiot paikalliseen kantaan, jonka rakenteesta se sitten generoi koodin.

Koodigeneraattorin luomia luokkia käytetään vain tietokantaoperaatioihin, eikä siis käytetä järjestelmän sisäisenä tietomallina,
saati sitten paljateta ulospäin rajapinnoissa. Koodigenerointi on käytössä siksi, että kannan skeema ja sovelluskoodi varmasti
pysyvät synkassa. Jos esim. tauluun lisätään uusi pakollinen kenttä, seuraa siitä käännösvirhe, kunnes softa käsittelee tämän kentän.

## Buildi

TOR:n buildiin kuuluu frontin buildaus npm:llä ja serverin buildaus Mavenilla. Tätä helpottamaan on otettu käyttöön `make`, jonka avulla
eri taskit on helppo suorittaa. Ks `Makefile`-tiedosto.

Buildaa koko systeemi

    make build

Aja kaikki testit

    make test

### TOR-sovelluksen ajaminen kehitystyöasemalla

Aja JettyLauncher-luokka IDEAsta/Eclipsestä, tai käynnistä TOR vaihtoehtoisesti komentoriviltä

    make build
    make run

Avaa selaimessa

    http://localhost:7021/tor

Suoritus-testidatat näkyy

    http://localhost:7021/tor/suoritus/
    
Mocha-testit voit ajaa selaimessa osoitteessa

    http://localhost:7021/tor/test/runner.html

## Asennus pilveen (CSC:n ePouta)

Ennakkovaatimukset:

1. Sinulla on tunnus CSC:n cPouta-ympäristöön
2. Poudan ympäristö(muuttuja)määrittely: https://pouta.csc.fi/dashboard/project/access_and_security/api_access/openrc/ on ladattu ja käytössä (`source Project_2000079-openrc.sh`)
3. Sinun julkinen ssh avain on lisättynä tänne: https://github.com/reaktor/oph-poutai-env/tree/master/roles/ssh.init/files/public_keys (ja koneiden konfiguraatio on päivitetty)
4. Käytössäsi on Ruby 2.0 tai uudempi

Tämän jälkeen voit pushata uuden version TOR:sta ajamalla,

    make deploy

jonka jälkeen sen pitäisi löytyä täältä: http://86.50.169.123/tor/
Lokien katsominen onnistuu komennolla:

    make tail
