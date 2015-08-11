# Todennetun Osaamisen Rekisteri (TOR)

## Teknologiat

- PostgreSQL
- Scala 2.11.4
- Scalatra
- Slick (http://slick.typesafe.com/doc/3.0.1/index.html) ja slick-codegen
- Mvn-depsujen lataus Jitpackilla, jolloin voidaan viitata suoraan Github-repoihin, eikä tarvitse itse buildata jar-artifaktoja

## Lokaali tietokanta

### Kannan alustus

Asenna ensin postgres ja flyway. Sitten

    initdb -d postgres
    
### Postgren käynnistys

    postgres -D postgres
    
### Kannan ja käyttäjän luonti

Kun postgre on käynnissä, pitää vielä luoda sinne tietokanta ja käyttäjä.

    createdb -E UTF-8 tor
    createuser -s tor -P  (salasanaksi tor)
    
### Skeeman luonti/migraatio

Skeema luodaan flywayllä migraatioskripteillä, jotka ovat hakemistossa `src/main/resources/db/migration`.
    
    mvn compile flyway:migrate
    
### SQL-yhteys paikalliseen kantaan

Komentorivillä voit käyttää `psql`:

    psql -v schema=tor --dbname=tor tor
    
Peruskomennot

    \dt    listaa taulut
    
Sitten vaikka

    select * from arviointi;
    
### Kantamigraatiot

Migraatiot ovat hakemistossa `src/main/resources/db/migration`. Migraation ajo paikalliseen kantaan tällä:
 
    mvn compile flyway:migrate 

Uusia migraatioita tehdessä tulee myös ajaa koodigeneraattori,
joka generoi tauluja vastaavat luokat `src/main/scala/fi/oph/tor/db/Tables.scala` -tiedostoon. Koodigeneraattorin `fi.oph.tor.db.CodeGeneator`
voit ajaa IDE:ssä. Koodigeneraattori ottaa yhteyden paikalliseen kantaan, jonka rakenteesta se generoi koodin. Koodigeneraattorin luomia
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

### Käynnistä IDEAsta/Eclipsestä

Aja JettyLauncher-luokka.

### Käynnistä komentoriviltä

`mvn exec:java`

### Avaa selaimessa

Avaa selaimessa http://localhost:7021/tor/
Tutkintosuoritus-testidatat näkyy http://localhost:7021/tor/tutkintosuoritus/
