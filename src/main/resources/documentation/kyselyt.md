# Kyselyrajapinta

Kyselyrajapinta on tarkoitettu pidempikestoisten kyselyiden tekemiseen KOSKI-datasta.
Rajapintaa käytetään seuraavanlaisesti.

## Uuden kyselyn luonti

Aloita kysely lähettämällä parametrit luontirajapintaan `POST /api/kyselyt`. Esimerkiksi Helsingin kaupungin
eronnut-tilassa olevat perusopetuksen opiskeluoikeudet vuoden 2024 tammikuulta voisi hakea
seuraavanlaisella kyselyllä.
Eri kyselyiden skeemat esitellään myöhemmin dokumentaatiossa. Voit myös [katso kaikki skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=kyselyt-query.json).

    POST /api/kyselyt

    {{json:OrganisaationOpiskeluoikeudetCsv}}

Jos kyselyä ei voida tehdä puutteellisten käyttöoikeuksien (esim. ei oikeutta nähdä kysytyn
organisaation tietoja), puuttuvien parametrien tai muun syyn takia, kysely epäonnistuu välittömästi
jo tässä vaiheessa.

Kyselyn ollessa ok saadaan seuraavanlainen vastaus:

    {{json:PendingQueryResponse}}

KOSKI ei aloita kyselyn prosessointia välittömästi, vaan lisää sen työjonoon.  Odottavan kyselyn
`status`-kenttä on `pending`. Tavallisesti kyselyiden käsittely aloitetaan lähes välittömästi,
mutta ruuhka- ja vikatilanteissa kysely voi pysyä odottavassa tilassa pidempiäkin aikoja.

Vastauksessa mukana tulee kyselyn tunniste kentässä `queryId`, jonka perusteella kyselyn tilaa
voi tiedustella polusta `GET /api/kyselyt/{queryId}`. Polku tilan kyselyyn löytyy myös valmiina
kentästä `resultsUrl`. Kyselyn lähtiessä pyörimään saatu vastaus on hyvin samankaltainen.
Tilaksi on vaihtunut `running` ja mukana on aloitusaika:

    {{json:RunningQueryResponse}}

### Kyselyn valmistuminen

Kyselyn valmistuessa tilaksi vaihtuu `completed` ja vastauksessa on mukana lista tulostiedostoista.
Tiedostojen määrä vaihtelee tehdyn kyselyn perusteella:

    {{json:CompleteQueryResponse}}

### Kyselyn epäonnistuminen

Kyselyn epäonnistuessa tila on `failed`. Epäonnistumisen syytä ei tietoturvasyistä kerrota,
mutta ne ovat lähes aina palvelinpään teknisiä ongelmia (eli vertautuvat HTTP-pyyntöjen 5xx-virheisiin):

    {{json:FailedQueryResponse}}



{{title:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetCsv}}
{{docs:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetCsv}}

Esimerkki:

    POST /api/kyselyt

    {{json:OrganisaationOpiskeluoikeudetCsv}}

{{title:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetJson}}
{{docs:fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.QueryOrganisaationOpiskeluoikeudetJson}}

Esimerkki:

    POST /api/kyselyt

    {{json:OrganisaationOpiskeluoikeudetJson}}

{{title:fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudet}}
{{docs:fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudet}}

Esimerkki CSV-datan hakemisesta:

    POST /api/kyselyt

    {{json:PaallekkaisetOpiskeluoikeudetCsv}}

Esimerkki laskentataulukkomuotoisen datan hakemisesta:

    POST /api/kyselyt

    {{json:PaallekkaisetOpiskeluoikeudetXlsx}}
