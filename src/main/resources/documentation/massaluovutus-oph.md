# Massaluovutusrajapinta Opetushallituksen sisäisille palveluille

Massaluovutusrajapinta on tarkoitettu pidempikestoisten kyselyiden tekemiseen KOSKI-datasta.
Rajapintaa käytetään seuraavanlaisesti. [Katso myös kaikki skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=massaluovutus-oph-query.json).

## Uuden kyselyn luonti

Aloita kysely lähettämällä parametrit luontirajapintaan `POST {{var:baseUrl}}/api/massaluovutus`.

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:Valintalaskenta}}

Jos kyselyä ei voida tehdä puutteellisten käyttöoikeuksien (esim. ei oikeutta nähdä kysytyn
organisaation tietoja), puuttuvien parametrien tai muun syyn takia, kysely epäonnistuu välittömästi
jo tässä vaiheessa.

Kyselyn ollessa ok saadaan seuraavanlainen vastaus:

    {{json:ValintalaskentaPendingQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.PendingQueryResponse}}

KOSKI ei aloita kyselyn prosessointia välittömästi, vaan lisää sen työjonoon. Odottavan kyselyn
`status`-kenttä on `pending`. Tavallisesti kyselyiden käsittely aloitetaan lähes välittömästi,
mutta ruuhka- ja vikatilanteissa kysely voi pysyä odottavassa tilassa pidempiäkin aikoja.

Vastauksessa mukana tulee kyselyn tunniste kentässä `queryId`, jonka perusteella kyselyn tilaa
voi tiedustella polusta `GET {{var:baseUrl}}/api/massaluovutus/{queryId}`. Polku tilan kyselyyn löytyy myös valmiina
kentästä `resultsUrl`. Kyselyn lähtiessä pyörimään saatu vastaus on hyvin samankaltainen.
Tilaksi on vaihtunut `running` ja mukana on aloitusaika:

    {{json:ValintalaskentaRunningQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.RunningQueryResponse}}

Kysely voi palata `running`-tilasta takaisin `pending`-tilaan, jos kyselyn käsittely katkeaa
esimerkiksi sitä käsittelevän instanssin käynnistyessä uudelleen.

### Kyselyn valmistuminen

Kyselyn valmistuessa tilaksi vaihtuu `completed` ja vastauksessa on mukana lista tulostiedostoista.
Tiedostojen määrä vaihtelee tehdyn kyselyn perusteella. Tiedostot ovat haettavissa n. kolme vuorokautta
kyselyn valmistumisesta. **Huom!** Tiedostonlatauslinkki vastaa uudelleenohjauspyynnöllä, joten kytke
käyttämästäsi http-asiakasohjelmasta *follow redirects* päälle, jos saamasi tiedostot ovat tyhjiä.

    {{json:ValintalaskentaCompleteQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.CompleteQueryResponse}}

### Kyselyn epäonnistuminen

Kyselyn epäonnistuessa tila on `failed`. Epäonnistumisen syytä ei tietoturvasyistä kerrota,
mutta ne ovat lähes aina palvelinpään teknisiä ongelmia (eli vertautuvat HTTP-pyyntöjen 5xx-virheisiin):

    {{json:ValintalaskentaFailedQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.FailedQueryResponse}}

Kysely ei koskaan palaa takaisin `pending`- tai `running`-tilaan sen päädyttyä `failed`-tilaan,
vaan kutsujan on aloitettava uusi kysely. Jos kysely päätyy jatkuvasti `failed`-tilaan, ota
yhteyttä KOSKI-tiimiin.

{{title:fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery}}
{{docs:fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery}}

{{title:fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery}}
{{docs:fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluMuuttuneetJalkeenQuery}}

{{title:fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery}}
{{docs:fi.oph.koski.massaluovutus.suorituspalvelu.SuorituspalveluOppijaOidsQuery}}

## Suorituspalvelun kysely opiskeluoikeuden version perusteella

Suora rajapinta yksittäisen opiskeluoikeuden tietyn version noutamiseen. Tämä rajapinta ei ole osana massaluovutuksen rajapintoja. Sen kautta pyydetyt tiedot saadaan suoraan rajapintakutsun vastauksessa.

Palauttaa Suorituspalvelua varten räätälöidyt tiedot.
Vastauksen skeema on yksittäinen <a href="/koski/json-schema-viewer/?schema=suorituspalvelu-versio-result.json">SupaOpiskeluoikeudenVersioResponse</a>-objekti.

Rajapintaa kutsutaan GET-pyynnöllä, jossa välitetään mukana opiskeluoikeuden oid sekä versionumero.

Kutsu muodostetaan seuraavalla tavalla:

`GET {{var:baseUrl}}/api/supa/<opiskeluoikeusOid>/<versionumero>`

Esimerkiksi opiskeluoikeuden `1.2.246.562.15.99999999999` versio `1` voidaan pyytää kutsulla:

`GET {{var:baseUrl}}/api/supa/1.2.246.562.15.99999999999/1`

Rajapinta palauttaa 404-virhekoodin, mikäli pyydettyä opiskeluoikeutta tai versiota ei löydy KOSKI-datasta.
Myös mitätöityjen tai poistettujen opiskeluoikeuksien tapauksissa rajapinta palauttaa 404-virhekoodin.
