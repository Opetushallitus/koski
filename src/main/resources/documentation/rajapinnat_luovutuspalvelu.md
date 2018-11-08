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

#### /koski/api/luovutuspalvelu/hetu (v1)

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

**HUOM** Allaoleva esimerkkiviesti on vielä kesken "henkilö"-elementin osalta. Siinä pitää paremmin huomioida
tilanteet, jossa samalla henkilöllä on useampi oppijanumero (ja mahdollisesti useampi kuin yksi hetu).

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

Vastaus, kun henkilöä ei löydy:

    HTTP/1.1 404 Not Found
    Content-Type: application/json
    
    [
      {
        "key": "notFound.oppijaaEiLöydyTaiEiOikeuksia",
        "message": "Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
      }
    ]

Vastaus, kun henkilö löytyy, mutta hänelle ei löydy opiskeluoikeuksia: sama kuin henkilöä ei löydy.

Vastaus kun Virta-tietoja ei saada haettua:

    HTTP/1.1 503 Service Unavailable
    Content-Type: application/json

    [
      {
        "key": "unavailable.virta",
        "message": "Korkeakoulutuksen opiskeluoikeuksia ei juuri nyt saada haettua. Yritä myöhemmin uudelleen."
      }
    ]

Vastaus kun Ylioppilastutkintorekisterin tietoja ei saada haettua:

    HTTP/1.1 503 Service Unavailable
    Content-Type: application/json

    [
      {
        "key": "unavailable.ytr",
        "message": "Ylioppilastutkintojen tietoja ei juuri nyt saada haettua. Yritä myöhemmin uudelleen."
      }
    ]

Myös muita virheitä voi esiintyä. Esim. HTTP status 400 ja "badRequest.queryParam" tarkoittaa että
pyynnössä oli jotain virheellistä. HTTP status 5xx tarkoittaa tilapäistä virhettä Koskessa.

---------------

#### /koski/api/luovutuspalvelu/oppijanumero

Tulossa myöhemmin. Tällä kutsulla haetaan yhden henkilön tiedot oppijanumeron perusteella.

Dokumentaatiossa pitää kuvata miten käsitellään tilanteet, joissa samalla henkilöllä
on useampi kuin yksi oppijanumero (tai hetu).

-----------------

#### /koski/api/luovutuspalvelu/hetut

Tulossa myöhemmin.

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

Jos jollekkin hetulle ei löydy opiskeluoikeuksia, tämä ei ole virhe vaan hetu puuttuu vastauslistasta.

Lisää kuvaukset:

    Tulossa myöhemmin.
