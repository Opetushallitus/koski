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

## /koski/api/luovutuspalvelu/haku

Tällä kutsulla haetaan usean (max. 1000 kpl) opiskeluoikeuden tiedot hakuehtojen perusteella. Käyttö rajattu vain tietyille viranomaisille.

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

***Huomio sivutuksen käytöstä:*** Sivutus tehdään tilattomasti, minkä vuoksi osa hakuparametreista ei toimi täysin luotettavasti, jos opiskeluoikeuksia
muutetaan kesken sivutettujen pyyntöjen. Tällöin opiskeluoikeuksia voi jäädä puuttumaan myöhemmältä sivulta, jos jokin aiemman sivun opiskeluoikeus
poistuu omalta sivultaan siihen tehtyjen muutosten vuoksi. Vain `opiskeluoikeudenTyyppi` ja `muuttunutJälkeen` toimivat luotettavasti sivutuksen kanssa.
Niissäkin on toistaiseksi mahdollista epäluotettavuutta, jos aiemmalla sivulla esiintyneitä oppijoita linkitetään kesken haun. Kaikkien
hakuparametrien kanssa on myös mahdollista, että jo aiemmalla sivulla palauttu opiskeluoikeus palautetaan uudestaan myöhemmällä sivulla.

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

Rajapinta palauttaa tiedot vain, jos oppijalla on Valviraa kiinnostavien ammattitutkintojen suorituksia.
Tällä hetkellä niitä ovat sosiaali- ja terveysalan perustutkinto (371101), hieroja (371171) ja
hieronnan ammattitutkinto (374111).

Mikäli oppijaa tai suorituksia ei löydy, palauttaa rajapinta

    HTTP/1.1 404 Not Found
    Content-Type: application/json

    ...

## /koski/api/luovutuspalvelu/kela/hetu

Esimerkkipyyntö

    POST /koski/api/luovutuspalvelu/kela/hetu HTTP/1.1
    Content-Type: application/json

    {
      "hetu": "180859-914S"
    }
Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    {
      "henkilö": {
        "oid": "1.2.246.562.24.123456789",
        "hetu": "180859-914S",
        "syntymäaika": "1959-08-18",
        "etunimi": "Matti",
        "sukunimi": "Mallikas",
        "kutsumanimi": "Matti"
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


## /koski/api/luovutuspalvelu/kela/hetut

Tällä kutsulla haetaan usean (max. 1000 kpl) henkilön tiedot henkilötunnusten perusteella.

Tällä kutsulla ei voi hakea `ylioppilastutkinto`-tyyppisiä opiskeluoikeuksia,
koska ne vaatisivat erilliset taustajärjestelmäkutsut (Ylioppilastutkintorekisteri) jokaiselle
henkilötunnukselle.

Esimerkkipyyntö

    POST /koski/api/luovutuspalvelu/kela/hetut HTTP/1.1
    Content-Type: application/json

    {
      "hetut": ["180859-914S", "020654-9025", "010326-953H"]
    }

Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    [
      {
        "henkilö": {
          "oid": "1.2.246.562.24.123456789",
          "hetu": "180859-914S",
          "syntymäaika": "1959-08-18",
          "etunimi": "Matti",
          "sukunimi": "Mallikas",
          "kutsumanimi": "Matti"
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

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=kela-oppija-schema.json">täällä</a>.

## /koski/valpas/api/luovutuspalvelu/kela/hetu

Esimerkkipyyntö

    POST /koski/valpas/api/luovutuspalvelu/kela/hetu HTTP/1.1
    Content-Type: application/json

    {
      "hetu": "180859-914S"
    }

Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    {
      "henkilö": {
        "oid": "1.2.246.562.24.123456789",
        "hetu": "181005A1560",
        ...
      },
      "oppivelvollisuudenKeskeytykset": [
        {
          "uuid": "0b457498-7392-48aa-821e-45234f853588",
          ...
        },
        {
          "uuid": "4f7c49b7-8f29-4b0a-85cf-9d2318c205b8",
          ...
        }
      ]
    }

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=valpas-kela-oppija-schema.json">täällä</a>.

## /koski/valpas/api/luovutuspalvelu/kela/hetut

Esimerkkipyyntö

    POST /koski/valpas/api/luovutuspalvelu/kela/hetut HTTP/1.1
    Content-Type: application/json

    {
      "hetut": ["181005A1560", "...", "..."]
    }

Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    [
      {
        "henkilö": {
          "oid": "1.2.246.562.24.123456789",
           "hetu": "181005A1560",
          ...
        },
        "oppivelvollisuudenKeskeytykset": [
          {
            "uuid": "0b457498-7392-48aa-821e-45234f853588",
            ...
          },
          {
            "uuid": "4f7c49b7-8f29-4b0a-85cf-9d2318c205b8",
            ...
          }
        ]
      },
      ...
    ]

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=valpas-kela-oppija-schema.json">täällä</a>.

## /koski/api/luovutuspalvelu/migri/hetu
## /koski/api/luovutuspalvelu/migri/oid

Näillä kutsuilla haetaan henkilön oppija-data hetun tai oppija-oidin perusteella.

Palautettavan JSON-rakenteen tietomallin dokumentaatio on [täällä](https://opintopolku.fi/koski/json-schema-viewer/?schema=migri-oppija-schema.json).

### Esimerkkipyynnöt

    POST /koski/api/luovutuspalvelu/migri/hetu HTTP/1.1
    Content-Type: application/json
    Body: {
      "hetu": "180859-914S"
    }

    POST /koski/api/luovutuspalvelu/migri/oid HTTP/1.1
    Content-Type: application/json
    Body: {
      "oid": "1.2.246.562.24.82405337123"
    }

## /koski/api/luovutuspalvelu/migri/valinta/hetut
## /koski/api/luovutuspalvelu/migri/valinta/oid

Näillä kutsuilla haetaan usean henkilön (enintään 5000 kpl) valintakoetulokset henkilötunnusten tai oppija-oidien perusteella.

### Esimerkkipyynnöt

    POST /koski/api/luovutuspalvelu/migri/valinta/hetut HTTP/1.1
    Content-Type: application/json
    Body: {
      "hetut": ["180859-914S", "020654-9025", "010326-953H"]
    }

    POST /koski/api/luovutuspalvelu/migri/valinta/oid HTTP/1.1
    Content-Type: application/json
    Body: {
        "oids": ["1.2.246.562.24.82405337123", "1.2.246.562.24.82405337995"]
    }

Tiedot haetaan Valintarekisteristä. Koski välittää pyynnöt Valintarekisterin rajapintaan ja palauttaa vastaukset kutsujalle.

Katso esimerkkivastaukset ja virhekoodit tarkemmin [Valintarekisterin dokumentaatiosta](https://wiki.eduuni.fi/display/ophpolku/Valintarekisterin+rajapinta+Migrille).

## /koski/api/luovutuspalvelu/ytl/oppijat

Tällä kutsulla haetaan usean (enintään 1000 kpl) henkilön tiedot henkilötunnusten tai
oppija-oidien perusteella.

### Esimerkkipyyntö

    POST /koski/api/luovutuspalvelu/ytl/oppijat HTTP/1.1
    Content-Type: application/json

    {
      "hetut": ["180859-914S", "020654-9025", "010326-953H"],
      "oidit": ["1.2.246.562.24.82405337123", "1.2.246.562.24.82405337995"],
      "opiskeluoikeuksiaMuuttunutJälkeen": "2022-01-27T05:27:49.276Z"
    }

### Ammatillisten erityisoppilaitosten käsittely

Mikäli opiskeluoikeuden tiedoissa esiintyy missä tahansa (oppilaitos, organisaatiohistoria, suoritus, vahvistus)
ammatillinen erityisoppilaitos, ei oppijan tiedoissa palauteta mitään organisaatiotietoja.

### Linkitettyjen oppijoiden käsittely

Jos oppijaan on linkitetty toisia oppijoita, palautetaan aina kaikki saman henkilön opiskeluoikeudet riippumatta
siitä, millä oppija-oidilla ne on tallennettu. Jos samaa oppijaa kysytään useammalla eri oidilla tai hetulla,
palautetaan sama lista opiskeluoikeuksia niille kaikille.

### opiskeluoikeuksiaMuuttunutJälkeen

opiskeluoikeuksiaMuuttunutJälkeen on valinnainen parametri. Jos se on määritelty, palautetaan oppijan opiskeluoikeudet
vain, jos jokin hänen opiskeluoikeutensa on muuttunut kyseisen ajanhetken jälkeen. Aikaleima on määriteltävä
esimerkkipyynnön mukaisesti UTC-ajassa. Huomioitavia erikoistapauksia:

1. Jos oppijan opiskeluoikeuksia on mitätöity ajanhetken jälkeen, palautetaan oppijan tiedot tyhjällä opiskeluoikeudet-listalla,
vaikka normaalitapauksissa oppijaa ei palauteta lainkaan, jos hänellä ei ole yhtään YTL:ää kiinnostavaa opiskeluoikeutta.
2. Jos oppijaan on linkitetty muita oppijoita, palautetaan oppijan opiskeluoikeudet aina. Tämä johtuu siitä, että
järjestelmässä ei ole saatavilla tietoa linkityksen tapahtumisajasta, joten sen oletetaan varmuuden vuoksi aina
tapahtuneen annetun ajanhetken jälkeen.

### Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    [
      {
        "henkilö": {
          ...
        },
        "opiskeluoikeudet": [
          {
            ...
          },
          ...
        ]
      },
      ...
    ]

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=ytl-oppija-schema.json">täällä</a>.

## /koski/valpas/api/luovutuspalvelu/ytl/oppijat

Tällä kutsulla haetaan usean (enintään 1000 kpl) henkilön tiedot henkilötunnusten tai
oppija-oidien perusteella.

### Esimerkkipyyntö

    POST /koski/valpas/api/luovutuspalvelu/ytl/oppijat HTTP/1.1
    Content-Type: application/json

    {
      "hetut": ["180859-914S", "020654-9025", "010326-953H"],
      "oidit": ["1.2.246.562.24.82405337123", "1.2.246.562.24.82405337995"]
    }

### Esimerkkivastaus

    HTTP/1.1 200 OK
    Content-Type: application/json

    [
      {
        "oppijaOid": "1.2.246.562.24.00000000001",
        ...
      },
      ...
    ]

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=ytl-valpas-oppija-schema.json">täällä</a>.

Jos rajapinta ei palauta vastaavan oppijan tietoja, oppijan tietoja ei löydy Valppaasta, eikä oikeutta koulutuksen maksuttomuuteen voida
sen myötä päätellä Valppaan tietojen perusteella.

Hetu palautetaan vain oppijoille, jotka on haettu hetun avulla.
