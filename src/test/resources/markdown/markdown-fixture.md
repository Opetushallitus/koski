# Otsikko 1

## Otsikko 2
Kappale heti otsikon perässä ilman tyhjää riviä.

### Otsikko 3

#### Otsikko 4

## /koski/api/luovutuspalvelu/kela/versiohistoria/:opiskeluoikeusOid/:versionumero
## /koski/api/luovutuspalvelu/migri/oid

## Kappaleet ja tekstityylit

Kappale, joka jatkuu
seuraavalle riville. Ääkköset: åäö ÅÄÖ, "lainausmerkit", 'heittomerkit', (1) ja 1.10.2026 rivin keskellä.
Alleviivat sanojen sisällä eivät ole korostuksia: redirect_urit, client_id:n, body:ssä, Authorization code:n.

**Lihavoitu** ja *kursivoitu* teksti. **Huom!** Lauseen keskellä *follow redirects* päälle.

**OPISKELUOIKEUDET_**-scopeja ei voi käyttää kuin yhtä kerrallaan.

Selain ohjataan osoitteeseen **/koski/omadata/valtuutus/acme**
, missä käyttäjä voi antaa luvan.
**/koski/omadata/valtuutus/acme?callback=https%3A%2F%2Fwww.acme.org%2Fuser%2F**

Koodia rivin sisällä: `Authorization`-headeri, `locale=(fi|sv|en)`, `POST {{var:baseUrl}}/api/massaluovutus`,
`GET {{var:baseUrl}}/api/massaluovutus/{queryId}` ja `GET {{var:baseUrl}}/api/supa/opiskeluoikeus/<opiskeluoikeusOid>/<versionumero>`.

`GET {{var:baseUrl}}/api/supa/opiskeluoikeus/1.2.246.562.15.99999999999/1`

## Linkit

Linkki [Koodistopalveluun](https://github.com/Opetushallitus/koodisto) ja suhteellinen linkki [koulutuskoodiin](/koski/dokumentaatio/koodisto/koulutus/latest).
[Katso myös kaikki skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=massaluovutus-query.json).

Linkki, jonka teksti on osoite: [https://github.com/Opetushallitus/koski/tree/master/omadata-oauth2-sample](https://github.com/Opetushallitus/koski/tree/master/omadata-oauth2-sample).

Ks. tarkemmin: [wiki](https://wiki.eduuni.fi/pages/viewpage.action?pageId=190613191#Aikuistenperusopetuksenopiskeluoikeudenl%C3%A4sn%C3%A4olotiedot-Opiskeluoikeudentilat/L%C3%A4sn%C3%A4olojaopintojenlopettaminen)

Kuvattu [wikissä](https://wiki.eduuni.fi/display/OPHPALV/Oma+Data%3A+Opiskelijastatus+-+palautuvat+tiedot),
mutta käytännössä se on osajoukko [tietomallista](/koski/dokumentaatio/tietomalli).

[RFC6749 The OAuth 2.0 Authorization Framework](https://www.rfc-editor.org/rfc/rfc6749)

[RFC6819 OAuth 2.0 Threat Model and Security Considerations](https://www.rfc-editor.org/rfc/rfc6819.txt)

Pelkkä osoite ei ole linkki: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/mutual-authentication.html , otsikon alla.
Tilaa tuote "asiakasvarmenne" (https://dvv.fi/palveluvarmenteet).

## HTML tekstin seassa

Palautettavan JSON-rakenteen tietomallin dokumentaatio on
<a href="/koski/json-schema-viewer/?schema=kela-oppija-schema.json">täällä</a>.

Vastauksen skeema on yksittäinen <a href="/koski/json-schema-viewer/?schema=suorituspalvelu-versio-result.json">SupaOpiskeluoikeudenVersioResponse</a>-objekti.

Vastauksen skeema on saatavana <a href="/koski/json-schema-viewer/?schema=valintalaskenta-result.json">täältä.</a>

## Luettelot

* Authorization code flow
* Mutual-TLS Client Authentication (ilman certificate-bound access tokeneita)
* [Yksittäisen oppijan näkymän tietomalli.](/koski/json-schema-viewer/?schema=valpas-internal-laaja-schema.json)

- HTTP-otsake nimeltä `CSRF`. Suositeltava arvo on sama kuin käytetty `Caller-Id`.
- `valintatiedot` (false): palautetaanko henkilön opiskelijavalintatiedot. Tiedot haetaan
  Opintopolun opiskelijavalintapalveluista. Jos valintatietoja ei saada haettua, kutsu palauttaa virheen 503.
- `osasuoritukset` (false): palautetaanko suoritusten osasuoritukset

Body-kenttien kuvaukset:

 * hetu: haettavan kansalaisen henkilötunnus.

Numeroitu luettelo ilman tyhjiä rivejä:

1. Ensimmäinen kohta, joka jatkuu
seuraavalle riville ilman sisennystä.
2. Toinen kohta.

Numeroitu luettelo tyhjillä riveillä:

1. Ensimmäinen kohta, joka jatkuu
seuraavalle riville ilman sisennystä.

2. Toinen kohta, joka jatkuu
seuraavalle riville.

## Koodilohkot

Esimerkkipyyntö

    POST /koski/api/luovutuspalvelu/keha/sdg/hetu?vainVahvistetut=false&osasuoritukset=false HTTP/1.1
    Content-Type: application/json

    {
      "hetut": ["180859-914S", "020654-9025"],
      "issue": "issue #123",
      ...
    }
Teksti heti koodilohkon perässä.

    GET /koski/api/luovutuspalvelu/valvira/010326-953H' HTTP/1.1

Koodilohko, jossa on useita tyhjiä rivejä:


    POST {{var:baseUrl}}/valpas/api/massaluovutus HTTP/1.1

    {{var:headers}}


    {{json:ValpasOppivelvollisetQuery}}

XML koodilohkossa:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Body>
        <kns1:opintoOikeudet>
          <![CDATA[{"henkilö":{"oid":"1.2.123.456.78.12345678901"},"suoritukset":[{}]}]]>
        </kns1:opintoOikeudet>
        <detail/>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>

------------

## Paikkamerkit

{{docs:fi.oph.koski.massaluovutus.PendingQueryResponse}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot}}

## HTML-lohkot

<div class="preview-image-links">
  <a href="/koski/json-schema-viewer#koski-oppija-schema.json">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-preview.png">
    </div>
    <div class="caption">Visualisoitu JSON-schema</div>
    <p>Voi tarkastella schemaa visualisointityökalun avulla.</p>
  </a>
</div>

<dl>
  <dt>(1) Kumppanin palvelun nimi</dt>
  <dd>
    Suomeksi, ruotsiksi ja englanniksi.
    <br/>Ks. <a href="{{var:virkailijaBaseUrl}}/dokumentaatio/rajapinnat/luovutuspalvelu">{{var:virkailijaBaseUrl}}/dokumentaatio/rajapinnat/luovutuspalvelu</a>
  </dd>

  <dt>Token type</dt>
  <dd>Access token endpoint palauttaa aina Bearer-tokeneita.</dd>
</dl>
