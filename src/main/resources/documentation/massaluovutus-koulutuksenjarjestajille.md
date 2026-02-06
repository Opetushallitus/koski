# Massaluovutusrajapinta koulutuksenjärjestäjille

Massaluovutusrajapinta on tarkoitettu pidempikestoisten kyselyiden tekemiseen KOSKI-datasta.
Rajapintaa käytetään seuraavanlaisesti.

## Uuden kyselyn luonti

Aloita kysely lähettämällä parametrit luontirajapintaan `POST {{var:baseUrl}}/api/massaluovutus`. Esimerkiksi Helsingin kaupungin tammikuussa 2024 alkaneet perusopetuksen opiskeluoikeudet voi hakea seuraavanlaisella kyselyllä.
Eri kyselyiden skeemat esitellään myöhemmin dokumentaatiossa. [Katso myös kaikki skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=massaluovutus-query.json).

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetCsv}}

Jos kyselyä ei voida tehdä puutteellisten käyttöoikeuksien (esim. ei oikeutta nähdä kysytyn
organisaation tietoja), puuttuvien parametrien tai muun syyn takia, kysely epäonnistuu välittömästi
jo tässä vaiheessa.

Kyselyn ollessa ok saadaan seuraavanlainen vastaus:

    {{json:PendingQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.PendingQueryResponse}}

KOSKI ei aloita kyselyn prosessointia välittömästi, vaan lisää sen työjonoon. Odottavan kyselyn
`status`-kenttä on `pending`. Tavallisesti kyselyiden käsittely aloitetaan lähes välittömästi,
mutta ruuhka- ja vikatilanteissa kysely voi pysyä odottavassa tilassa pidempiäkin aikoja.

Vastauksessa mukana tulee kyselyn tunniste kentässä `queryId`, jonka perusteella kyselyn tilaa
voi tiedustella polusta `GET {{var:baseUrl}}/api/massaluovutus/{queryId}`. Polku tilan kyselyyn löytyy myös valmiina
kentästä `resultsUrl`. Kyselyn lähtiessä pyörimään saatu vastaus on hyvin samankaltainen.
Tilaksi on vaihtunut `running` ja mukana on aloitusaika:

    {{json:RunningQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.RunningQueryResponse}}

Kysely voi palata `running`-tilasta takaisin `pending`-tilaan, jos kyselyn käsittely katkeaa
esimerkiksi sitä käsittelevän instanssin käynnistyessä uudelleen.

### Kyselyn valmistuminen

Kyselyn valmistuessa tilaksi vaihtuu `completed` ja vastauksessa on mukana lista tulostiedostoista.
Tiedostojen määrä vaihtelee tehdyn kyselyn perusteella. Tiedostot ovat haettavissa n. kolme vuorokautta
kyselyn valmistumisesta. **Huom!** Tiedostonlatauslinkki vastaa uudelleenohjauspyynnöllä, joten kytke
käyttämästäsi http-asiakasohjelmasta *follow redirects* päälle, jos saamasi tiedostot ovat tyhjiä.

    {{json:CompleteQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.CompleteQueryResponse}}

### Kyselyn epäonnistuminen

Kyselyn epäonnistuessa tila on `failed`. Epäonnistumisen syytä ei tietoturvasyistä kerrota,
mutta ne ovat lähes aina palvelinpään teknisiä ongelmia (eli vertautuvat HTTP-pyyntöjen 5xx-virheisiin):

    {{json:FailedQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.FailedQueryResponse}}

Kysely ei koskaan palaa takaisin `pending`- tai `running`-tilaan sen päädyttyä `failed`-tilaan,
vaan kutsujan on aloitettava uusi kysely. Jos kysely päätyy jatkuvasti `failed`-tilaan, ota
yhteyttä KOSKI-tiimiin.

{{title:fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv}}
{{docs:fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetCsv}}

{{title:fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson}}
{{docs:fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:OrganisaationOpiskeluoikeudetJson}}

{{title:fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet}}
{{docs:fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet}}

Esimerkki CSV-datan hakemisesta:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PaallekkaisetOpiskeluoikeudetCsv}}

Esimerkki laskentataulukkomuotoisen datan hakemisesta:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PaallekkaisetOpiskeluoikeudetXlsx}}

{{title:fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneet}}
{{docs:fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneet}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LuokalleJaaneetJson}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukionSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LukionSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Suoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:Lukio2019SuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LukioKurssikertymatXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:Lukio2019OpintopistekertymatXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:LukioDiaIbInternationalESHOpiskelijamaaratXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOpiskelijavuositiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AmmatillinenOpiskelijavuositiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AmmatillinenTutkintoSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AmmatillinenOsittainenSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuAmmatillinen}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:MuuAmmatillinenXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:TOPKSAmmatillinenXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetuksenVuosiluokkaXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetusSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AikuistenPerusopetusSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AikuistenPerusopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:AikuistenPerusopetuksenKurssikertymaXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:EsiopetusXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:EsiopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:PerusopetukseenValmistavaXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:TuvaPerusopetuksenOppijamaaratRaporttiXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:TuvaSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:IBSuoritustiedotXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTJOTPA}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:VSTJOTPAXlsx}}

{{title:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus}}
{{docs:fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryMuuKuinSaanneltyKoulutus}}

Esimerkki:

    POST {{var:baseUrl}}/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:MuuKuinSaanneltyKoulutusXlsx}}
