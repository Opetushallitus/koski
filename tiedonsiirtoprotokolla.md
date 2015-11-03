## Esimerkki

Esimerkki oppijasta, jolla on yksi opinto-oikeus.

´´´json
{
  "henkilo": {
    "oid": "1.2.246.562.24.64226782669",
    "hetu": "271095-9975",
    "etunimet": "thor",
    "kutsumanimi": "thor",
    "sukunimi": "kypärä"
  },
  "opintoOikeudet": [
    {
      "tutkinto": {
        "ePerusteetDiaarinumero": "8/011/2013",
        "tutkintoKoodi": "367304",
        "nimi": "Puunkorjuun erikoisammattitutkinto"
      },
      "oppilaitosOrganisaatio": {
        "oid": "1.2.246.562.10.52251087186",
        "nimi": "Stadin ammattiopisto"
      },
      "suoritustapa": "naytto",
      "suoritukset": [
        {
          "koulutusModuuli": {
            "tyyppi": "tutkinnonosa",
            "koodi": "104632"
          },
          "arviointi": {
            "asteikko": {
              "koodistoUri": "ammattijaerikoisammattitutkintojenarviointiasteikko",
              "versio": 1
            },
            "arvosana": {
              "id": "ammattijaerikoisammattitutkintojenarviointiasteikko_hylatty",
              "nimi": "Hylätty"
            }
          }
        }
      ],
      "id": 582
    }
  ]
}
```

## Kenttäkuvaukset

### henkilo

Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö.

#### henkilo.oid

Henkilön tunniste OpintoPolku-palvelussa

#### henkilo.hetu

Suomalainen henkilötunnus

#### henkilo.etunimet

#### henkilo.kutsumanimi

Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille "Juha-Matti Petteri" kelpaavat joko "Juha-Matti", "Juha", "Matti" tai "Petteri".

#### henkilo.sukunimi

### opintoOikeudet

Lista henkilön opinto-oikeuksista. Sisältää vain ne opinto-oikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja.

#### opintoOikeudet.id

Opinto-oikeuden uniikki tunniste. Tietoja syötettäessä kenttä ei ole pakollinen. Tietoja päivitettäessä TOR tunnistaa opinto-oikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, diaarinumero) perusteella.

#### opintoOikeudet.tutkinto

Opinto-oikeuteen liittyvän tutkinnon tiedot.

##### opintoOikeudet.tutkinto.ePerusteetDiaarinumero

Tutkinnon perusteen diaarinumero (pakollinen) TODO: koodisto

##### opintoOikeudet.tutkinto.tutkintoKoodi

Tutkinnon 6-numeroinen tutkintokoodi (pakollinen) TODO: koodisto

##### opintoOikeudet.tutkinto.nimi

Tutkinnon (kielistetty) nimi. Tiedon syötössä tämän kentän arvo jätetään huomioimatta; arvo haetaan ePerusteista.

#### opintoOikeudet.oppilaitosOrganisaatio

Oppilaitos, jossa opinnot on suoritettu

##### opintoOikeudet.organisaatio.oid

Oppilaitoksen tunniste Opintopolku-järjestelmässä TODO: koodisto

##### opintoOikeudet.organisaatio.nimi

Oppilaitoksen (kielistetty) nimi. Tiedon syötössä tämän kentän arvo jätetään huomioimatta; arvo haetaan Organisaatiopalvelusta.

#### opintoOikeudet.suoritustapa

Tutkinnon suoritustapa. Joko `naytto` tai `ops`. TODO: tälle koodisto

#### opintoOikeudet.suoritukset

Opinto-oikeuteen liittyvät suoritukset

##### opintoOikeudet.suoritukset.koulutusModuuli

Suoritukseen liittyvän koulutusmoduulin (esim. tutkinnon osa) tunniste

###### opintoOikeudet.suoritukset.koulutusmoduuli.tyyppi

Koulutusmoduulin tyyppi. Tällä hetkellä vain arvo `tutkinnonosa` kelpaa. TODO: tälle koodisto

###### opintoOikeudet.suoritukset.koulutusmoduuli.koodi

Koulutusmoduulin tunniste. Tällä hetkellä tähän tulee tutkinnon osan id ePerusteissa.

#### opintoOikeudet.suoritukset.arviointi

Suorituksen arviointi. Ei pakollinen

##### opintoOikeudet.suoritukset.arviointi.asteikko

Arviointiasteikko-koodiston tunniste ja versio

##### opintoOikeudet.suoritukset.arviointi.asteikko.koodistoUri

##### opintoOikeudet.suoritukset.arviointi.asteikko.versio

##### opintoOikeudet.suoritukset.arviointi.arvosana

Arvosanan tunniste

##### opintoOikeudet.suoritukset.arviointi.arvosana.id

Arvosanan koodiarvo koodistossa. TODO: rename

##### opintoOikeudet.suoritukset.arviointi.arvosana.nimi

Arvosanan nimi tekstimuodossa. TODO: tämän hakeminen koodistosta.