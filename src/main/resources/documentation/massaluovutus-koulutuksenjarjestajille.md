# Massaluovutusrajapinta koulutusjärjestäjille

Massaluovutusrajapinta on tarkoitettu pidempikestoisten kyselyiden tekemiseen KOSKI-datasta.
Rajapintaa käytetään seuraavanlaisesti.

## Uuden kyselyn luonti

Aloita kysely lähettämällä parametrit luontirajapintaan `POST {{var:baseUrl}}/api/massaluovutus`. Esimerkiksi Helsingin kaupungin
eronnut-tilassa olevat perusopetuksen opiskeluoikeudet vuoden 2024 tammikuulta voisi hakea
seuraavanlaisella kyselyllä.
Eri kyselyiden skeemat esitellään myöhemmin dokumentaatiossa. [Katso myös kaikki skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=massaluovutus-query.json).

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetCsv}}

Jos kyselyä ei voida tehdä puutteellisten käyttöoikeuksien (esim. ei oikeutta nähdä kysytyn
organisaation tietoja), puuttuvien parametrien tai muun syyn takia, kysely epäonnistuu välittömästi
jo tässä vaiheessa.

Kyselyn ollessa ok saadaan seuraavanlainen vastaus:

    {{json:PendingQueryResponse}}

{{docs:fi.oph.koski.queuedqueries.PendingQueryResponse}}

KOSKI ei aloita kyselyn prosessointia välittömästi, vaan lisää sen työjonoon. Odottavan kyselyn
`status`-kenttä on `pending`. Tavallisesti kyselyiden käsittely aloitetaan lähes välittömästi,
mutta ruuhka- ja vikatilanteissa kysely voi pysyä odottavassa tilassa pidempiäkin aikoja.

Vastauksessa mukana tulee kyselyn tunniste kentässä `queryId`, jonka perusteella kyselyn tilaa
voi tiedustella polusta `GET {{var:baseUrl}}/api/massaluovutus/{queryId}`. Polku tilan kyselyyn löytyy myös valmiina
kentästä `resultsUrl`. Kyselyn lähtiessä pyörimään saatu vastaus on hyvin samankaltainen.
Tilaksi on vaihtunut `running` ja mukana on aloitusaika:

    {{json:RunningQueryResponse}}

{{docs:fi.oph.koski.queuedqueries.RunningQueryResponse}}

Kysely voi palata `running`-tilasta takaisin `pending`-tilaan, jos kyselyn käsittely katkeaa
esimerkiksi sitä käsittelevän instanssin käynnistyessä uudelleen.

### Kyselyn valmistuminen

Kyselyn valmistuessa tilaksi vaihtuu `completed` ja vastauksessa on mukana lista tulostiedostoista.
Tiedostojen määrä vaihtelee tehdyn kyselyn perusteella. Tiedostot ovat haettavissa n. kolme vuorokautta
kyselyn valmistumisesta.

    {{json:CompleteQueryResponse}}

{{docs:fi.oph.koski.queuedqueries.CompleteQueryResponse}}

### Kyselyn epäonnistuminen

Kyselyn epäonnistuessa tila on `failed`. Epäonnistumisen syytä ei tietoturvasyistä kerrota,
mutta ne ovat lähes aina palvelinpään teknisiä ongelmia (eli vertautuvat HTTP-pyyntöjen 5xx-virheisiin):

    {{json:FailedQueryResponse}}

{{docs:fi.oph.koski.queuedqueries.FailedQueryResponse}}

Kysely ei koskaan palaa takaisin `pending`- tai `running`-tilaan sen päädyttyä `failed`-tilaan,
vaan kutsujan on aloitettava uusi kysely. Jos kysely päätyy jatkuvasti `failed`-tilaan, ota
yhteyttä KOSKI-tiimiin.

{{title:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetCsv}}
{{docs:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetCsv}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetCsv}}

{{title:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetJson}}
{{docs:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetJson}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetJson}}

{{title:fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudet}}
{{docs:fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudet}}

Esimerkki CSV-datan hakemisesta:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PaallekkaisetOpiskeluoikeudetCsv}}

Esimerkki laskentataulukkomuotoisen datan hakemisesta:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PaallekkaisetOpiskeluoikeudetXlsx}}
