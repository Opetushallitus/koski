## Rajapinnat viranomaisille (luovutuspalvelu)

Tällä sivulla kuvataan rajapinnat, joilla tietyille viranomaisille voidaan luovuttaa tietoja Koskesta.

Rajapinnat ovat REST-tyyppisiä, ja dataformaattina on JSON. 

Kutsut käyttävät REST-tyylistä poiketen POST-metodia, koska pyyntöjen URLit päätyvät helposti
erilaisiin lokeihin (esim. kuormantasaimet, virheilmoitukset), ja näihin lokeihin ei haluta henkilötunnuksia.

Pyynnöt luovutuspalveluun tunnistetaan TLS-kättelyn palvelinvarmenteella ja rajataan IP-osoitteen perusteella.
Tätä varten kutsujan on ilmoitettava käytettävän varmenteen subject-nimi ja ne IP-osoitteet/verkot, joista pyynnöt tehdään.

Palvelinvarmenteen on oltava luotettavan CA:n myöntämä (yleisesti selaimien hyväksymät CA:t tai Väestörekisterikeskus),
ja sen "extended key usage" -kentässä on sallittava "client authentication" -käyttötarkoitus.
Salausprotokollan on oltava TLS 1.2 tai uudempi.

Rajapintojen käyttöönoton yhteydessä on saatavissa pyynnöstä myös lista esimerkkihenkilöistä,
joiden avulla rajapintaintegraatiota on mahdollista testata.

------------

## /koski/api/luovutuspalvelu/hetu (v1)

Tällä kutsulla haetaan yhden henkilön tiedot.

Esimerkkipyyntö:

    POST /koski/api/luovutuspalvelu/hetu HTTP/1.1
    Content-Type: application/json

    {
      "v": 1,
      "hetu": "180859-914S",
      "opiskeluoikeudenTyypit": ["perusopetus", "korkeakoulutus"]
    }

Pyynnön kenttien kuvaukset:

 * `v` - rajapinnan versionumero, tässä aina 1.
 * `hetu` - haettava henkilötunnus. 
 * `opiskeluoikeudentyypit` - lista opiskeluoikeuden tyyppejä, joista kutsuja on kiinnostunut. 
    Sallitut arvot löytyvät [opiskeluoikeudentyyppi](/koski/dokumentaatio/koodisto/opiskeluoikeudentyyppi/latest) koodistosta.
    Tällä hetkellä arvot `korkeakoulutus` ja `ylioppilastutkinto` aiheuttavat ylimääräisen kutsun taustarekisteriin
    (Virta ja Ylioppilastutkintorekisteri), joten niitä tulee käyttää vain jos tiedot todella tarvitaan.
     
Vastaus, kun henkilö löytyy:
<a name="vastaukset"></a>

Siinä harvinaisessa tilanteessa, että henkilöllä on useampi kuin yksi oid palautetaan kaikki niihin siirretyt opinnot.
Henkilön "oid"-kentässä näytetään oppijan varsinainen oppijanumero.

    HTTP/1.1 200 OK
    Content-Type: application/json

    {
      "henkilö": {
        "oid": "1.2.246.562.24.123456789",
        "hetu": "180859-914S",
        "syntymäaika": "1959-08-18",
        "turvakielto": false
      },
      "opiskeluoikeudet": [
        {
          "oid": "1.2.246.562.15.31643973527",
          "versionumero": 1,
          "aikaleima": "2018-09-25T14:03:58.700770",
          ...
        },
        ...
      ]
    }

Tarkempi kuvaus opiskeluoikeudet-elementin sisällöstä löytyy [tietomallin](/koski/dokumentaatio/tietomalli) dokumentaatiosta.

Vastaus, kun henkilöä ei löydy, tai henkilö löytyy mutta hänelle ei löydy opiskeluoikeuksia
(olennainen tieto on `notFound.oppijaaEiLöydyTaiEiOikeuksia`, tarkka `message`-kentän sisältö voi muuttua):

    HTTP/1.1 404 Not Found
    Content-Type: application/json
    
    [
      {
        "key": "notFound.oppijaaEiLöydyTaiEiOikeuksia",
        "message": "Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
      }
    ]

Muut vastaukset ovat virheitä, ja käsitellään HTTP-tilakoodin perusteella. 
Tilakoodit 400-499 tarkoittavat virheellistä pyyntöä (jolloin uudelleen yrittäminen ei välttämättä auta),
ja tilakoodit 500-599 tarkoittavat useimmiten väliaikaista virhettä (jolloin pyyntöä kannattaa yrittää myöhemmin uudelleen).
Useimmissa tapauksissa vastauksen body:stä löytyy tarkempi virhekoodi tai -viesti, josta on apua kehittäjille virheen 
syyn selvittämisessä.

Erityisesti voidaan mainita seuraavat virheet:

 * 400 (Bad Request) tulee esim. jos jokin pyynnön kentistä on virheellinen (esim. hetu-kentässä on tieto joka ei muodoltaan ole henkilötunnus).
 * 403 (Forbidden) viittaa ongelmaan käyttöoikeuksissa (esim. palvelinvarmenne tai IP-osoite ei ole sallittu).
 * 503 (Service Unavailable) tulee esim. jos Virran tai Ylioppilastutkintorekisterin tietoja ei saada haettua:

Esimerkit 503 vastauksista:

    HTTP/1.1 503 Service Unavailable
    Content-Type: application/json

    [
      {
        "key": "unavailable.virta",
        "message": "Korkeakoulutuksen opiskeluoikeuksia ei juuri nyt saada haettua. Yritä myöhemmin uudelleen."
      }
    ]

    HTTP/1.1 503 Service Unavailable
    Content-Type: application/json

    [
      {
        "key": "unavailable.ytr",
        "message": "Ylioppilastutkintojen tietoja ei juuri nyt saada haettua. Yritä myöhemmin uudelleen."
      }
    ]


---------------

## /koski/api/luovutuspalvelu/oid (v1)

Tällä kutsulla haetaan yhden henkilön tiedot oppijanumeron perusteella.


Esimerkkipyyntö:

    POST /koski/api/luovutuspalvelu/oid HTTP/1.1
    Content-Type: application/json

    {
      "v": 1,
      "oid": "1.2.246.562.24.54718336656",
      "opiskeluoikeudenTyypit": ["perusopetus", "korkeakoulutus"]
    }

Pyynnön kenttien kuvaukset:

 * `v` - rajapinnan versionumero, tässä aina 1.
 * `oid` - haettava oppijanumero.
    Siinä tapauksessa, että henkilöllä on useampi kuin yksi oid voi millä tahansa niistä hakea. Mitä tahansa niistä käytetäänkin haussa palautetaan oppijan kaikki tiedot.
 * `opiskeluoikeudentyypit` - lista opiskeluoikeuden tyyppejä, joista kutsuja on kiinnostunut.
    Sallitut arvot löytyvät [opiskeluoikeudentyyppi](/koski/dokumentaatio/koodisto/opiskeluoikeudentyyppi/latest) koodistosta.
    Tällä hetkellä arvot `korkeakoulutus` ja `ylioppilastutkinto` aiheuttavat ylimääräisen kutsun taustarekisteriin
    (Virta ja Ylioppilastutkintorekisteri), joten niitä tulee käyttää vain jos tiedot todella tarvitaan.

Paluuviesti on sama kuin pyynnössä `/koski/api/luovutuspalvelu/hetu` [ks. yllä](#vastaukset)

-----------------

## /koski/api/luovutuspalvelu/hetut

Tällä kutsulla haetaan usean (max. 1000 kpl) henkilön tiedot henkilötunnusten perusteella.

Tällä kutsulla ei voi hakea `korkeakoulutus`- tai `ylioppilastutkinto`-tyyppisiä opiskeluoikeuksia,
koska ne vaatisivat erilliset taustajärjestelmäkutsut (Virta / Ylioppilastutkintorekisteri) jokaiselle 
henkilötunnukselle. 

Esimerkkipyyntö:

    POST /koski/api/luovutuspalvelu/hetut HTTP/1.1
    Content-Type: application/json

    {
      "v": 1,
      "hetut": ["180859-914S", "020654-9025", "010326-953H"],
      "opiskeluoikeudenTyypit": ["perusopetus"]
    }

Pyynnön kenttien kuvaukset:

 * `v` - rajapinnan versionumero, tässä aina 1.
 * `hetut` - lista haettavista henkilötunnuksista.
 * `opiskeluoikeudentyypit` - lista opiskeluoikeuden tyyppejä, joista kutsuja on kiinnostunut.
    Sallitut arvot löytyvät [opiskeluoikeudentyyppi](/koski/dokumentaatio/koodisto/opiskeluoikeudentyyppi/latest) koodistosta.
    `korkeakoulutus`- tai `ylioppilastutkinto`-tyyppisiä opiskeluoikeuksia ei voi hakea massahaulla.

Vastaus, kun pyyntö suoritetaan onnistuneesti:

    HTTP/1.1 200 OK
    Content-Type: application/json

    [
      {
        "henkilö": {
          "oid": "1.2.246.562.24.123456789",
          "hetu": "180859-914S",
          "syntymäaika": "1959-08-18",
          "turvakielto": false
        },
        "opiskeluoikeudet": [
          {
            "oid": "1.2.246.562.15.31643973527",
            "versionumero": 1,
            "aikaleima": "2018-09-25T14:03:58.700770",
            ...
          },
          ...
        ]
      },
      ...
    ]

Jos jollekin hetulle ei löydy opiskeluoikeuksia, tämä ei ole virhe vaan hetu puuttuu vastauslistasta.

## /koski/api/luovutuspalvelu/haku

Tällä kutsulla haetaan usean (max. 1000 kpl) henkilön tiedot hakuehtojen perusteella. Käyttö rajattu vain tietyille viranomaisille.

Tällä kutsulla ei voi hakea `korkeakoulutus`- tai `ylioppilastutkinto`-tyyppisiä opiskeluoikeuksia,

Esimerkkipyyntö (hakee ensimmäiset 100kpl ammatillisen ja lukion opiskeluoikeuksia jotka ovat alkaneet 31.12.2018 tai sen jälkeen):

    GET /koski/api/luovutuspalvelu/haku?v=1&pageSize=100&pageNumber=0&opiskeluoikeusAlkanutViimeistään=2018-12-31&opiskeluoikeudenTyyppi=ammatillinenkoulutus&opiskeluoikeudenTyyppi=lukiokoulutus' HTTP/1.1

Hakuparametrien kuvaukset:

 * `v` - rajapinnan versionumero, tässä aina 1.
 * `opiskeluoikeudenTyyppi` - opiskeluoikeuden tyyppi, josta kutsuja on kiinnostunut, voidaan antaa useita. Rajaa tulokset niihin opiskeluoikeuksiin jotka kuuluvat annettuihin tyyppeihin.
    Sallitut arvot löytyvät [opiskeluoikeudentyyppi](/koski/dokumentaatio/koodisto/opiskeluoikeudentyyppi/latest) koodistosta.
    `korkeakoulutus`- tai `ylioppilastutkinto`-tyyppisiä opiskeluoikeuksia ei voi hakea.
 * `opiskeluoikeusPäättynytAikaisintaan` - Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt. Muotoa: `yyyy-mm-dd`. Rajaa tulokset niihin opiskeluoikeuksiin jotka ovat päättyneet annetuna päivänä tai sen jälkeen.
 * `opiskeluoikeusPäättynytViimeistään` - Päivämäärä jota ennen opiskeluoikeus on päättynyt. Muotoa: `yyyy-mm-dd`. Rajaa tulokset niihin opiskeluoikeuksiin jotka ovat päättyneet annetuna päivänä tai sitä ennen.
 * `opiskeluoikeusAlkanutAikaisintaan` - Päivämäärä jonka jälkeen opiskeluoikeus on alkanut. Muotoa: `yyyy-mm-dd`. Rajaa tulokset niihin opiskeluoikeuksiin jotka ovat alkaneet annetuna päivänä tai sen jälkeen.
 * `opiskeluoikeusAlkanutViimeistään` - Päivämäärä jota ennen opiskeluoikeus on alkanut. Muotoa: `yyyy-mm-dd`. Rajaa tulokset niihin opiskeluoikeuksiin jotka ovat alkaneet annetuna päivänä tai sitä ennen.
 * `muuttunutEnnen` - Aikaleima jota ennen opiskeluoikeus on muuttunut. Aikaleima annetaan UTC-ajassa, ISO 8601-muodossa, esim. `2018-12-03T10:15:30Z`. Rajaa tulokset niihin opiskeluoikeuksiin jotka ovat muuttuneet ennen annettua aikaleimaa.
 * `muuttunutJälkeen` - Aikaleima jonka jälkeen opiskeluoikeus on muuttunut. Aikaleima annetaan UTC-ajassa, ISO 8601-muodossa, esim. `2018-12-03T10:15:30Z`. Rajaa tulokset niihin opiskeluoikeuksiin jotka ovat muuttuneet annettun aikaleiman jälkeen.
 * `pageSize` - Sivukoko. Määrittää kuinka monta opiskeluoikeutta vastauksessa palautetaan. Oletusarvo on 1000 opiskeluoikeutta.
 * `pageNumber` - Sivunumero. Määrittää ohitettavien sivujen määrän. Oletusarvo on 0 (näytetään ensimmäinen sivu).

Vastaus, kun pyyntö suoritetaan onnistuneesti:

    HTTP/1.1 200 OK
    Content-Type: application/json

    [
      {
        "henkilö": {
          "oid": "1.2.246.562.24.123456789",
          "hetu": "180859-914S",
          "syntymäaika": "1959-08-18",
          "etunimet": "Eeva Katariina",
          "kutsumanimi": "Eeva",
          "sukunimi": "Lehtinen",
          "äidinkieli" : {
            "koodiarvo" : "FI",
            "nimi" : {
              "fi" : "suomi",
              "sv" : "finska",
              "en" : "Finnish"
            },
            "lyhytNimi" : {
              "fi" : "suomi",
              "sv" : "finska",
              "en" : "Finnish"
            },
            "koodistoUri" : "kieli",
            "koodistoVersio" : 1
          },
          "kansalaisuus" : [ {
            "koodiarvo" : "246",
            "nimi" : {
              "fi" : "Suomi",
              "sv" : "Finland",
              "en" : "Finland"
            },
            "lyhytNimi" : {
              "fi" : "FI",
              "sv" : "FI",
              "en" : "FI"
            },
            "koodistoUri" : "maatjavaltiot2",
            "koodistoVersio" : 2
          } ],
          "turvakielto": false
        },
        "opiskeluoikeudet": [
          {
            "oid": "1.2.246.562.15.31643973527",
            "versionumero": 1,
            "aikaleima": "2018-09-25T14:03:58.700770",
            ...
          },
          ...
        ]
      },
      ...
    ]

## /koski/api/luovutuspalvelu/valvira/

Tällä kutsulla Valvira voi hakea suoritustietoja.

Esimerkkipyyntö

    GET /koski/api/luovutuspalvelu/valvira/010326-953H' HTTP/1.1

Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    {
      "henkilö": {
        "hetu": "010326-953H"
      },
      "opiskeluoikeudet": [
        {
          ...
          "suoritukset": [
            {
              "koulutusmoduuli": {
                "tunniste": {
                  "koodiarvo": "371101",
                  "nimi": {
                    "fi": "Sosiaali- ja terveysalan perustutkinto",
                    ...
                  },
                  ...
                },
                ...
              },
              ...
            }
          ],
          ...
        }
      ]
    }

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=valvira-oppija-schema.json">täällä</a>.

Mikäli oppijaa tai suorituksia ei löydy, palauttaa rajapinta

    HTTP/1.1 404 Not Found
    Content-Type: application/json

    ...
