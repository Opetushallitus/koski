# OAuth2 Omadata-rajapinta

Tällä sivulla kuvataan rajapinnat, joilla kolmannet osapuolet (kumppanit) voivat pyytää
kansalaiselta käyttölupaa kansalaisen tietoihin ja hakea tietoja OAuth2-standardirajapinnan kautta.

Kumppanin tulee olla Opetushallituksen hyväksymä, ja kumppanin tekniset tunnisteet pitää olla
lisättyinä KOSKI-järjestelmään. Lisäämistä varten OPH:ssa tarvittavat tiedot on listattu seuraavassa
kappaleessa.

OAuth2-rajapinnassa on käytössä:

* Authorization code flow
* POST response mode
* Mutual-TLS Client Authentication (ilman certificate-bound access tokeneita)
* PKCE

## Kumppanilta tarvittavat tekniset tiedot

<dl>
  <dt>(1) Kumppanin palvelun nimi</dt>
  <dd>
    Suomeksi, ruotsiksi ja englanniksi. Tällä nimellä palvelu esitetään OPH:n käyttöliittymissä kansalaiselle suostumusta häneltä kysyttäessä.
  </dd>
  <dt>(2) Kansalaiselta tarvittavat tiedot, lista OAuth2-scopeja</dt>
  <dd>
    Tieto siitä, mitä henkilö- ja opiskeluoikeustietoja kumppanille pitäisi sallia, ks. myöhempi Scopet-kappale.
  </dd>
  <dt>(3) Paluuosoitteet, OAuth2-redirect_urit</dt>
  <dd>
    Osoitteita voi olla monia, ja testiympäristössä voidaan sallia myös localhost-osoitteita kehityksen helpottamiseksi.
    <br/>Nämä voivat olla eri testiympäristössä ja tuotannossa.
  </dd>
  <dt>(4) Mahdolliset ketjutetut paluuosoitteet</dt>
  <dd>
    Jos redirect_uriin KOSKI-palvelusta tuleva pyyntö aiheuttaa uusia uudelleenohjauksia kumppanin palvelun sisällä toiseen domainiin, täytyy nekin osoitteet ottaa huomioon KOSKI-palvelun CSP:ssä (Content Security Policy)
    <br/>Nämä voivat olla eri testiympäristössä ja tuotannossa.
  </dd>
  <dt>(5) Mutual TLS (mTLS) -client-sertifikaatin tunnistetieto</dt>
  <dd>
    Subject distinguished name, subject dn. 
    <br/>Tekniset speksit varmenteelle ovat samat kuin viranomaisrajapinnoissa. Ne on dokumentoitu sivun 
    <a href="{{var:virkailijaBaseUrl}}/json-schema-viewerdokumentaatio/rajapinnat/luovutuspalvelu">{{var:virkailijaBaseUrl}}/json-schema-viewerdokumentaatio/rajapinnat/luovutuspalvelu</a>
    alussa.    
    <br/>Tämä voi olla eri testiympäristössä ja tuotannossa.
  </dd>
  <dt>(6) Kumppanin palvelun ip-osoitteet</dt>
  <dd>
    IP-osoitteet, joista liikenne sallitaan.
    <br/>Nämä voivat olla eri testiympäristössä ja tuotannossa.
  </dd>
  <dt>(7) Toive suostumuksen voimassaoloajasta</dt>
  <dd>
    Aika minuutteina.
    <br/>Tämä riippuu kumppanin käyttötapauksesta: esim. tarvitaanko kansalaisen tiedot vain yhden kerran, jolloin lyhyt voimassaoloaika riittää, vai tarvitseeko tietoihin päästä käsiksi pidemmän aikaa ilman,
    että kansalaiselta tarvitsee pyytää suostumusta uudestaan.
  </dd>
</dl>

## Tiedot rajapinnasta

Rajapinnan kutsumista varten kumppanin toteuttama palvelu tarvitsee seuraavat tiedot:

<dl>
  <dt>OAuth2-palvelun osoitteet ja muut metatiedot</dt>
  <dd>
    Nämä julkaistaan standardimuotoisina metatietoina.
    <br/>Ks. sisältö linkistä <a href="{{var:oppijaBaseUrl}}/omadata-oauth2/.well-known/oauth-authorization-server">{{var:oppijaBaseUrl}}/omadata-oauth2/.well-known/oauth-authorization-server</a>
  </dd>

  <dt>Token type</dt>
  <dd>Access token endpoint palauttaa aina Bearer-tokeneita.</dd>

  <dt>Authorization code:n ja access token:in voimassaoloaika</dt>
  <dd>Authorization code on lyhytaikainen (10 min) ja kertakäyttöinen.
    <br/>Access tokenin voimassaoloaika määritellään kumppanin toiveiden ja käyttötapauksen perusteella.
    <br/>Erillisiä refresh token:eita ei toistaiseksi tueta.
  </dd>

  <dt>Resource endpoint</dt>
  <dd>
    <a href="{{var:luovutuspalveluBaseUrl}}/koski/api/omadata-oauth2/resource-server">{{var:luovutuspalveluBaseUrl}}/koski/api/omadata-oauth2/resource-server</a>
    <br/>Resource endpoint:ia käytetään samalla mTLS-tunnistuksella kuin OAuth2-rajapinnan token endpoint:ia.
  </dd>

  <dt>client_id</dt>
  <dd>
    Saat client_id:n KOSKI-palvelun ylläpitäjiltä viimeistään OPH:n hyväksynnän ja edellisen kappaleen teknisten tietojen toimittamisen jälkeen.
  </dd>
</dl>

### Käyttäjän kielen asettaminen

Authorization endpoint tukee optionaalista ylimääräistä query-parametria `locale=(fi|sv|en)`, jolla käyttäjälle näkyvän kielen voi vaihtaa haluamakseen.

## Scopet ja palautettava data

Listan kaikista tuetuista scopeista saa edellisessä kappaleessa mainitusta metatieto-linkistä.

**OPISKELUOIKEUDET_**-scopeja ei voi käyttää kuin yhtä kerrallaan.

<dl>
  <dt>OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT</dt>
  <dd>
    opiskeluoikeudet-taulukossa palautetaan oppijan suoritetut tutkinnot. Skeema: <a href="{{var:virkailijaBaseUrl}}/json-schema-viewer/?schema=omadata-oauth2-suoritetut-tutkinnot-oppija-schema.json">{{var:virkailijaBaseUrl}}/json-schema-viewer/?schema=omadata-oauth2-suoritetut-tutkinnot-oppija-schema.json</a>
    Lisätietoja, ks. <a href="https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841">https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841</a>.
  </dd>
  <dt>OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT</dt>
  <dd>
    opiskeluoikeudet-taulukossa palautetaan oppijan aktiiviset ja päättyneet opinnot. Skeema: <a href="{{var:virkailijaBaseUrl}}/json-schema-viewer/?schema=omadata-oauth2-aktiiviset-ja-paattyneet-opinnot-oppija-schema.json">{{var:virkailijaBaseUrl}}/json-schema-viewer/?schema=omadata-oauth2-aktiiviset-ja-paattyneet-opinnot-oppija-schema.json</a>
    Lisätietoja, ks. <a href="https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841">https://wiki.eduuni.fi/pages/viewpage.action?pageId=371305841</a>.
  </dd>
  <dt>OPISKELUOIKEUDET_KAIKKI_TIEDOT</dt>
  <dd>
    opiskeluoikeudet-taulukossa palautetaan kaikki oppijan opiskeluoikeustiedot, mutta ilman luottamuksellisiksi määriteltyjä tietoja. Skeema: <a href="{{var:virkailijaBaseUrl}}/json-schema-viewer/?schema=omadata-oauth2-kaikki-tiedot-oppija-schema.json">{{var:virkailijaBaseUrl}}/json-schema-viewer/?schema=omadata-oauth2-kaikki-tiedot-oppija-schema.json</a>.
    Luottamukselliseksi määritellyt tiedot on merkitty tekstimuotoiseen skeemaan, ks. <a href="{{var:virkailijaBaseUrl}}/dokumentaatio/koski-oppija-schema.html">{{var:virkailijaBaseUrl}}/dokumentaatio/koski-oppija-schema.html</a>
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
  <dt>HENKILOTIEDOT_OPPIJANUMERO</dt>
  <dd>
    Palautettavassa json-objektissa on henkilö.oid, joka sisältää oppijanumeron
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
        ],
        "tokenInfo": {
            "scope": "HENKILOTIEDOT_KAIKKI_TIEDOT OPISKELUOIKEUDET_KAIKKI_TIEDOT",
            "expirationTime": "2024-12-12T15:16:12.474651+02:00"
        }
    }

### Virhetilanteet

OAuth2:n speksaamissa authorization ja token endpoint rajapinnoissa palautetaan virheilmoitukset speksin mukaisina. Esimerkiksi
token endpointissa virhekoodi `400 Bad Request` ja sisältönä JSON, esimerkiksi:

    {
      "error" : "invalid_client",
      "error_description" : "Varmenne puuttuu"
    }

Resource endpointeissa käytetään myös yleisempiä virhekoodeja. Esimerkiksi jos oppijalla ei ole tallennettuna mitään
opiskeluoikeuksia KOSKI-palveluun tai KOSKI-palvelun kautta välitettäviin VIRTA- ja Ylioppilastutkintorekistereihin,
palautetaan virhekoodi `404 Not Found`.

## Huomioita datan käsittelystä

Kumppanin vastuulla on tarkistaa palautettujen henkilötietojen perusteella, että se sai oikean ja odottamansa henkilön tiedot. Tämä johtuu siitä, että mitään teknistä estettä ei ole sille, että kansalainen pyytää
esim. ystäväänsä tekemään vahvan tunnistautumisen ja suostumuksen myöntämisen puolestaan. Tällöin KOSKI-järjestelmä palauttaa kumppanille kyseisen ystävän tiedot. Tämä voi tapahtua myös kansalaiskäyttäjän tietämättä
hyökkääjän toimesta, mikäli kansalainen on saatu käyttämään selainta jollain tavalla vaarantuneessa ympäristössä.

Myös saadun datan sisällön kanssa tulee olla huolellinen. Kansalainen pystyy teknisesti muuttamaan kumppanin pyytämän scopen ennen suostumuksen antamista. Tässä tapauksessa kumppani saattaa
saada kansalaisen tiedot eri scopella kuin itse pyysi. Esimerkiksi: jos kumppanin palvelulla on oikeudet sekä suoritettuihin tutkintoihin, että aktiivisiin ja päättyneisiin
opintoihin, voi edellisiä pyytäessään saadakin jälkimmäiset, joiden tulkitseminen suoraviivaisesti suoritetuiksi tutkinnoiksi olisi virheellistä.

## Esimerkkiapplikaatio

Osoitteessa [https://github.com/Opetushallitus/koski/tree/master/omadata-oauth2-sample](https://github.com/Opetushallitus/koski/tree/master/omadata-oauth2-sample) on rajapintaa käyttävän esimerkkiapplikaation lähdekoodi.

OPH:n testiympäristöissä esimerkkiapplikaation Node-JS -versio pyörii osoitteissa:

* DEV-ympäristö: [https://oph-koski-omadataoauth2sample-dev.testiopintopolku.fi/](https://oph-koski-omadataoauth2sample-dev.testiopintopolku.fi/)
* QA-ympäristö: [https://oph-koski-omadataoauth2sample-qa.testiopintopolku.fi/](https://oph-koski-omadataoauth2sample-qa.testiopintopolku.fi/)

Java-versio ei ole ajossa ympäristössä.

## Linkkejä spesifikaatioihin

[RFC6749 The OAuth 2.0 Authorization Framework](https://www.rfc-editor.org/rfc/rfc6749)

[RFC8705 OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens](https://www.rfc-editor.org/rfc/rfc8705)

[RFC7636 Proof Key for Code Exchange by OAuth Public Clients](https://www.rfc-editor.org/rfc/rfc7636)

[RFC8414 OAuth 2.0 Authorization Server Metadata](https://www.rfc-editor.org/rfc/rfc8414)

[RFC6819 OAuth 2.0 Threat Model and Security Considerations](https://www.rfc-editor.org/rfc/rfc6819.txt)

[RFC6750 The OAuth 2.0 Authorization Framework: Bearer Token Usage](https://www.rfc-editor.org/rfc/rfc6750)

[OAuth 2.0 Form Post Response Mode](https://openid.net/specs/oauth-v2-form-post-response-mode-1_0.html)

[OAuth 2.0 Security Best Current Practice](https://www.ietf.org/archive/id/draft-ietf-oauth-security-topics-25.html)
