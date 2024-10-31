# OAuth2 Omadata-rajapinta

Tällä sivulla kuvataan rajapinnat, joilla kolmannet osapuolet (kumppanit) voivat pyytää
kansalaiselta käyttölupaa kansalaisen tietoihin ja hakea tietoja OAuth2-standardirajapinnan kautta.

Kumppanien tulee olla Opetushallituksen hyväksymä, ja kumppanin tekniset tunnisteet pitää olla
lisättyinä KOSKI-järjestelmään. Lisäämistä varten OPH:ssa tarvittavat tiedot on listattu seuraavassa
kappaleessa.

## Kumppanilta tarvittavat tekniset tiedot

<dl>
  <dt>(1) Kumppanin palvelun nimi</dt>
  <dd>
    Suomeksi, ruotsiksi ja englanniksi. Tällä nimellä palvelu esitetään OPH:n käyttöliittymissä kansalaiselle suostumusta häneltä kysyttäessä.
  </dd>
  <dt>(2) Kansalaiselta tarvittavat tiedot, Lista OAuth2 scopeja</dt>
  <dd>
    Tieto siitä, mitä henkilö- ja opiskeluoikeustietoja kumppanille pitäisi sallia. Ks. myöhempi Scopet-kappale.
  </dd>
  <dt>(3) Paluuosoitteet, OAuth2 redirect_urit</dt>
  <dd>
    Nämä voivat olla eri testiympäristössä ja tuotannossa. Osoitteita voi olla monia, ja testiympäristössä voidaan sallia myös localhost-osoitteita kehityksen helpottamiseksi.
  </dd>
  <dt>(4) Mahdolliset ketjutetut paluuosoitteet</dt>
  <dd>
    Jos redirect_uriin KOSKI-palvelusta tuleva pyyntö aiheuttaa uusia uudelleenohjauksia kumppanin palvelun sisällä, täytyy nekin osoitteet ottaa huomioon KOSKI-palvelun CSP:ssä (Content Security Policy)
  </dd>
  <dt>(5) mTLS-client-sertifikaatin tunnistetieto</dt>
  <dd>
    Subject distinguished name, subject dn. Sertifikaatin tulee olla yleisesti tunnetun CA:n tai DVV:n CA:n allekirjoittama. Tämä voi olla eri testiympäristössä ja tuotannossa. KOSKI-palvelu tarvitsee vain subject-nimen, mutta kätevintä voi olla toimittaa
    fullchain.pem-tiedosto. Sertifikaatin uusimisprosessi kannattaa rakentaa niin, että subject-nimi ei siinä muutu, jolloin KOSKI-palvelulle ei tarvitse toimittaa uutta nimeä sertifikaatin uusimisen jälkeen.
  </dd>
  <dt>(6) Kumppanin palvelun ip-osoitteet</dt>
  <dd>
    IP-osoitteet, joista liikenne sallitaan. Nämä voivat olla eri testiympäristössä ja tuotannossa.
  </dd>
</dl>

## Tiedot rajapinnasta

<dl>
  <dt>OAuth2-palvelun osoitteet ja muut metatiedot</dt>
  <dd>
    Ks. sisältö linkistä <a href="{{var:baseUrl}}/omadata-oauth2/.well-known/oauth-authorization-server">{{var:baseUrl}}/omadata-oauth2/.well-known/oauth-authorization-server</a>
  </dd>

  <dt>Resource endpoint</dt>
  <dd>
    <a href="{{var:luovutuspalveluBaseUrl}}/koski/api/omadata-oauth2/resource-server">{{var:luovutuspalveluBaseUrl}}/koski/api/omadata-oauth2/resource-server</a>
    <br/>Resource endpoint:ia käytetään samalla mTLS-tunnistuksella kuin OAuth2-rajapinnan token endpoint:ia.
  </dd>

  <dt>client_id</dt>
  <dd>
    Saat KOSKI-tiimiltä viimeistään OPH:n hyväksynnän ja edellisen kappaleen teknisten tietojen toimittamisen jälkeen.
  </dd>
</dl>

## Scopet ja palautettava data

Listan kaikista tuetuista scopeista saa edellisessä kappaleessa mainitusta metatieto-linkistä.

Toistaiseksi **OPISKELUOIKEUDET_**-scopeja ei voi käyttää kuin yhtä kerrallaan.

<dl>
  <dt>OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT</dt>
  <dd>
    opiskeluoikeudet-taulukossa palautetaan oppijan suoritetut tutkinnot. Skeema: <a href="{{var:baseUrl}}/json-schema-viewer/?schema=suoritetut-tutkinnot-oppija-schema.json">{{var:baseUrl}}/json-schema-viewer/?schema=suoritetut-tutkinnot-oppija-schema.json</a>
    Lisätietoja, ks. <a href="https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841">https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841</a>.
  </dd>
  <dt>OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT</dt>
  <dd>
    opiskeluoikeudet-taulukossa palautetaan oppijan aktiiviset ja päättyneet opinnot. Skeema: <a href="{{var:baseUrl}}/json-schema-viewer/?schema=aktiiviset-ja-paattyneet-opinnot-oppija-schema.json">{{var:baseUrl}}/json-schema-viewer/?schema=aktiiviset-ja-paattyneet-opinnot-oppija-schema.json</a>
    Lisätietoja, ks. <a href="https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841">https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841</a>.
  </dd>
  <dt>OPISKELUOIKEUDET_KAIKKI_TIEDOT</dt>
  <dd>
    opiskeluoikeudet-taulukossa palautetaan kaikki oppijan opiskeluoikeustiedot, skeema: <a href="{{var:baseUrl}}/json-schema-viewer#koski-oppija-schema.json">{{var:baseUrl}}/json-schema-viewer#koski-oppija-schema.json</a>
  </dd>
  <dt>HENKILOTIEDOT_HETU</dt>
  <dd>
    Palautettavassa json-objektissa on henkilö.hetu
  </dd>
  <dt>HENKILOTIEDOT_NIMI</dt>
  <dd>
    Palautettavassa json-objektissa on henkilö.etunimet, henkilö.sukunimi ja henkilö.kutsumanimi
  </dd>
  <dt>HENKILOTIEDOT_SYNTYMAAIKA</dt>
  <dd>
    Palautettavassa json-objektissa on henkilö.syntymäaika, muodossa YYYY-MM-DD
  </dd>
  <dt>HENKILOTIEDOT_KAIKKI_TIEDOT</dt>
  <dd>
    Palautettavassa json-objektissa on kaikki em. henkilötiedot.
  </dd>
</dl>

Esimerkki palautettavan datan rakenteesta:

    {
        "henkilö": {
            "hetu": "210281-9988",
            "syntymäaika": "1981-02-21",
            "etunimet": "Testi Henkilö",
            "sukunimi": "Testinen",
            "kutsumanimi": "Testi"
        },
        "opiskeluoikeudet": [
            {
                "oid": "1.2.246.562.15.20709430670",
                "oppilaitos": {
                    "oid": "1.2.246.562.10.51720121923",
                    ...
                },
                ...
                "suoritukset": [
                    {
                        "koulutusmoduuli": {
                            "tunniste": {
                                "koodiarvo": "374111",
                                ...
                            },
                            ...
                        },
                        "suoritustapa": {
                            ...
                        },
                        "vahvistus": {
                            "päivä": "2019-06-01"
                        },
                        "tyyppi": {
                            "koodiarvo": "ammatillinentutkinto",
                            ...
                        },
                        ...
                    }
                ],
                "tyyppi": {
                    "koodiarvo": "ammatillinenkoulutus",
                    ...
                }
            },
            ...
        ]
    }


## Huomioita datan käsittelystä

Kumppanin vastuulla on tarkistaa palautettujen henkilötietojen perusteella, että se sai odottamansa henkilön tiedot. Mitään teknistä estettä ei ole sille, etteikö kansalainen voisi pyytää
esim. ystäväänsä tekemään vahvaa tunnistautumista puolestaan, jolloin KOSKI-järjestelmä palauttaa kumppanille kyseisen ystävän tiedot.

## Esimerkkiapplikaatio

Osoitteessa [https://github.com/Opetushallitus/koski/tree/master/omadata-oauth2-sample](https://github.com/Opetushallitus/koski/tree/master/omadata-oauth2-sample) on rajapintaa käyttävän esimerkkiapplikaation lähdekoodi.

OPH:n testiympäristöissä esimerkkiapplikaatio pyörii osoitteissa:

* DEV-ympäristö: [https://oph-koski-omadataoauth2sample-dev.testiopintopolku.fi/](https://oph-koski-omadataoauth2sample-dev.testiopintopolku.fi/)
* QA-ympäristö: [https://oph-koski-omadataoauth2sample-qa.testiopintopolku.fi/](https://oph-koski-omadataoauth2sample-qa.testiopintopolku.fi/)

## Linkkejä spesifikaatioihin

[RFC6749 The OAuth 2.0 Authorization Framework](https://www.rfc-editor.org/rfc/rfc6749)

[RFC8705 OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens](https://www.rfc-editor.org/rfc/rfc8705)

[RFC7636 Proof Key for Code Exchange by OAuth Public Clients](https://www.rfc-editor.org/rfc/rfc7636)

[RFC8414 OAuth 2.0 Authorization Server Metadata](https://www.rfc-editor.org/rfc/rfc8414)

[OAuth 2.0 Form Post Response Mode](https://openid.net/specs/oauth-v2-form-post-response-mode-1_0.html)
