# Kyselyrajapinta

Kyselyrajapinta (/api/kyselyt) on tarkoitettu pitkäkestoisten ja raskaiden
kyselyiden ajoalustaksi. Sen idea on säädellä tietokannalle ja muille palveluille
aiheutuvaa kuormaa rajoittamalla samaan aikaan ajossa olevien kyselyiden
määrää, mutta samalla tarjoten käyttäjille vakaasti ja nopeasti toimivan
rajapinnan.

Kaikki kyselyt aloitetaan riippumatta niiden tyypistä samalla
`POST /api/kyselyt` -metodilla, joka lisää kyselyn operatiivisen tietokannan
`kysely`-tauluun. Käyttäjä saa linkin, josta voi kysellä kyselyn edistymistä.
Sen osoite on muotoa `GET /api/kyselyt/[tunniste]`. Kyselyn tullessa
valmiiksi vastaus sisältää linkkejä kyselyn tulostiedostoihin.
Tiedostomuoto voi olla mitä tahansa csv:stä json:iin ja
xml-muotoisiin taulukkolaskentatiedostoihin riippuen tehdystä kyselystä.

Eri kyselytyypit määritellään `QueryParameters`-luokasta
perityllä case classilla, joka myös toteuttaa miten kysely ajetaan.
Toteutetuista kyselyistä luodaan automaattisesti
[dokumentaatio](http://localhost:7021/koski/dokumentaatio/rajapinnat/kyselyt).

Jokainen KOSKI-applikaation instanssi toimii potentiaaliesti ns. query workerina,
eli poimii jonosta kyselyitä ja ajaa niitä yhden kerrallaan. Työn tilaa
ylläpidetään samassa `kyselyt`-taulussa. Odottavan työn tila on `pending`,
työn alla oleva on `working` ja loppuneet työt ovat joko `complete` tai
`failed` riippuen siitä onnistuiko työ onnistuneesti. Aloitetuilla töillä
on lisäksi tallennettuna sitä ajavan instanssin taskARN sekä lopputuloksesta
riippuen lista tulostiedostoista tai virheilmoitus.

Instanssi tarkastaa aina ennen työn aloittamista, onko kyseinen instanssi enää
query worker. Se määräytyy siten, että ECS:stä kysytään kaikki KOSKI-instanssit
ja *n* uusinta instanssia toimivat workereina. Lukumäärä *n* määräytyy
konfiguraation `kyselyt.concurrency` mukaan. Tämän mekanismin avulla voidaan
estää deploymentin takia pian sammuvien instanssien aloittamasta uutta työtä.

Aloitettu työ saattaa silti jäädä kesken sen ollessa tarpeeksi pitkä tai
instanssin sammuessa enemmän tai vähemmän yllättäen. Tällaiset orpojen
töiden tunnistamiseen jokainen instanssi (riippumatta siitä onko worker vai ei)
siivoaa kyselyitä noin minuutin välein. Työ tunnistetaan orvoksi, jos sen
tila on `working`, mutta sitä ajavan workerin taskARNia ei löydy ECS:n
palauttamasta listasta. Työ merkitään takaisin `pending`-tilaan ja sen
meta-kenttään lisätään tieto uudelleenyrityksestä. Jos kysely orpoutuu
kolme kertaa, siihen saattaa liittyä jokin vakava ongelma ja
uudelleenpriorisoinnin sijaan se asetetaan `failed`-tilaan.

Kyselyistä saatuja tulokset kirjoitetaan tiedostoihin, jotka siirretään
S3-buckettiin. Kirjoittamista varten jokainen kyselyn toteutus saa
käyttöönsä `QueryResultWriter`-olion, jolla luodut tiedostot siirretään
automaattisesti oikeaan paikkaan. Tiedostojen S3-avaimet muodostuvat
kyselyn tunnisteesta ja tiedostonimistä.

Käyttäjän ladataessa tiedostoa (polku `GET /api/kyselyt/[tunniste]/[tiedosto]`)
sille luodaan S3:n tarjoama ns. *presigned request url* ja käyttäjä ohjataan
kyseiseen urliin. Kyseinen url on hetken voimassa oleva osoite, jonka avulla
tiedosto saadaan ladattua suoraan S3:n kautta. Tiedoston sisältöä ei siis
enää siirretä KOSKI-instanssin kautta.

S3-buckettiin on konfiguroitu aggressiivinen retentioaika ja tulostiedostot
siivotaan pois muutaman päivän kuluessa. Sen jälkeen tuloksia ei voi enää
ladata, vaan kysely on ajettava uudelleen.
