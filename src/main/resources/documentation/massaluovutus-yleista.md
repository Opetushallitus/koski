# Massaluovutusrajapinnat

Massaluovutusrajapinta on tarkoitettu pidempikestoisten kyselyiden tekemiseen KOSKI-datasta ja Valpas-palvelun
koostamasta datasta. Kysely luodaan yhdellä pyynnöllä, KOSKI käsittelee sen taustalla ja valmiit tulostiedostot
ladataan erikseen.

Tällä sivulla kuvataan kaikille kyselyille yhteinen toimintatapa. Käytettävissä olevat kyselyt ja niiden
parametrit on kuvattu omilla sivuillaan:

- [Koulutuksenjärjestäjät](/koski/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat): organisaation opiskeluoikeudet ja muut KOSKI-datan kyselyt
- [Raportit](/koski/dokumentaatio/rajapinnat/massaluovutus/raportit): raporttinäkymän raportit koneluettavasti
- [Oppivelvollisuuden valvonta](/koski/dokumentaatio/rajapinnat/massaluovutus/valpas): kuntien oppivelvollisuuden valvonnan kyselyt

Rajapinnan osoite riippuu kyselystä:

- KOSKI-datan kyselyt ja raportit: `{{var:baseUrl}}/api/massaluovutus`
- Oppivelvollisuuden valvonnan kyselyt: `{{var:baseUrl}}/valpas/api/massaluovutus`

Tämän sivun esimerkeissä käytetään ensimmäistä osoitetta. Kyselyn tila ja tulostiedostot haetaan aina samasta
osoitteesta, johon kysely luotiin.

## Uuden kyselyn luonti

Aloita kysely lähettämällä parametrit luontirajapintaan `POST {{var:baseUrl}}/api/massaluovutus`. Esimerkiksi
Helsingin kaupungin tammikuussa 2024 alkaneet perusopetuksen opiskeluoikeudet voi hakea seuraavanlaisella kyselyllä:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetCsv}}

Jos kyselyä ei voida tehdä puutteellisten käyttöoikeuksien (esim. ei oikeutta nähdä kysytyn
organisaation tietoja), puuttuvien parametrien tai muun syyn takia, kysely epäonnistuu välittömästi
jo tässä vaiheessa.

Kyselyn ollessa ok saadaan seuraavanlainen vastaus:

    {{json:PendingQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.PendingQueryResponse|title=Vastauksen kentät}}

## Kyselyn tilan seuranta

KOSKI ei aloita kyselyn prosessointia välittömästi, vaan lisää sen työjonoon. Odottavan kyselyn
`status`-kenttä on `pending`. Tavallisesti kyselyiden käsittely aloitetaan lähes välittömästi,
mutta ruuhka- ja vikatilanteissa kysely voi pysyä odottavassa tilassa pidempiäkin aikoja.

Vastauksessa mukana tulee kyselyn tunniste kentässä `queryId`, jonka perusteella kyselyn tilaa
voi tiedustella polusta `GET {{var:baseUrl}}/api/massaluovutus/{queryId}`. Polku tilan kyselyyn löytyy myös valmiina
kentästä `resultsUrl`. Kyselyn lähtiessä pyörimään saatu vastaus on hyvin samankaltainen.
Tilaksi on vaihtunut `running` ja mukana on aloitusaika:

    {{json:RunningQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.RunningQueryResponse|title=Vastauksen kentät}}

Kysely voi palata `running`-tilasta takaisin `pending`-tilaan, jos kyselyn käsittely katkeaa
esimerkiksi sitä käsittelevän instanssin käynnistyessä uudelleen.

## Kyselyn valmistuminen

Kyselyn valmistuessa tilaksi vaihtuu `complete` ja vastauksessa on mukana lista tulostiedostoista.
Tiedostojen määrä vaihtelee tehdyn kyselyn perusteella. Tiedostot ovat haettavissa n. kolme vuorokautta
kyselyn valmistumisesta. **Huom!** Tiedostonlatauslinkki vastaa uudelleenohjauspyynnöllä, joten kytke
käyttämästäsi http-asiakasohjelmasta *follow redirects* päälle, jos saamasi tiedostot ovat tyhjiä.

    {{json:CompleteQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.CompleteQueryResponse|title=Vastauksen kentät}}

## Kyselyn epäonnistuminen

Kyselyn epäonnistuessa tila on `failed`. Epäonnistumisen syytä ei tietoturvasyistä kerrota,
mutta ne ovat lähes aina palvelinpään teknisiä ongelmia (eli vertautuvat HTTP-pyyntöjen 5xx-virheisiin):

    {{json:FailedQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.FailedQueryResponse|title=Vastauksen kentät}}

Kysely ei koskaan palaa takaisin `pending`- tai `running`-tilaan sen päädyttyä `failed`-tilaan,
vaan kutsujan on aloitettava uusi kysely. Jos kysely päätyy jatkuvasti `failed`-tilaan, ota
yhteyttä KOSKI-tiimiin.
