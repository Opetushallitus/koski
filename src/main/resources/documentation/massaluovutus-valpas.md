# Massaluovutusrajapinnat oppivelvollisuuden valvontaan

Massaluovutusrajapinta on tarkoitettu pidempikestoisten kyselyiden tekemiseen KOSKI-datasta ja Valpas-palvelun koostamasta datasta.
Tässä dokumentaatiossa kuvataan oppivelvollisuuden valvontaan tarkoitettujen rajapintojen käyttö sekä
rajapintaan tehtävien kyselyiden ja vastausten muoto.

## Uuden kyselyn luonti

Aloita kysely lähettämällä parametrit luontirajapintaan `POST {{var:baseUrl}}/valpas/api/massaluovutus`. Kaikki erilaiset mahdolliset kyselyt on esitelty myöhemmin dokumentaatiossa. [Katso myös kyselyiden skeeman visualisointi](/koski/json-schema-viewer/?schema=valpas-massaluovutus-query.json).

Esimerkiksi Helsingin kaupungin kaikkien oppivelvollisten oppijoiden, joilla ei tällä hetkellä ole oppivelvollisuuden
suorittamiseen kelpaavaa opiskeluoikeutta, tiedot voi hakea seuraavanlaisella kyselyllä.


    POST {{var:baseUrl}}/valpas/api/massaluovutus HTTP/1.1
    {{var:headers}}

    {{json:ValpasEiOppivelvollisuuttaSuorittavatQuery}}

Jos kyselyä ei voida tehdä puutteellisten käyttöoikeuksien (esim. ei oikeutta nähdä kysytyn
kunnan tietoja), puuttuvien parametrien tai muun syyn takia, kysely epäonnistuu välittömästi
jo tässä vaiheessa.

Kyselyn ollessa ok saadaan seuraavanlainen vastaus:

    {{json:ValpasPendingQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.PendingQueryResponse}}

KOSKI ei aloita kyselyn prosessointia välittömästi, vaan lisää sen työjonoon. Odottavan kyselyn
`status`-kenttä on `pending`. Tavallisesti kyselyiden käsittely aloitetaan lähes välittömästi,
mutta ruuhka- ja vikatilanteissa kysely voi pysyä odottavassa tilassa pidempiäkin aikoja.

Vastauksessa mukana tulee kyselyn tunniste kentässä `queryId`, jonka perusteella kyselyn tilaa
voi tiedustella polusta `GET {{var:baseUrl}}/valpas/api/massaluovutus/{queryId}`. Polku tilan kyselyyn löytyy myös valmiina
kentästä `resultsUrl`. Kyselyn lähtiessä pyörimään saatu vastaus on hyvin samankaltainen.
Tilaksi on vaihtunut `running` ja mukana on aloitusaika:

    {{json:ValpasRunningQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.RunningQueryResponse}}

Kysely voi palata `running`-tilasta takaisin `pending`-tilaan, jos kyselyn käsittely katkeaa
esimerkiksi sitä käsittelevän instanssin käynnistyessä uudelleen.

### Kyselyn valmistuminen

Kyselyn valmistuessa tilaksi vaihtuu `completed` ja vastauksessa on mukana lista tulostiedostoista.
Tiedostojen määrä vaihtelee tehdyn kyselyn perusteella. Tiedostot ovat haettavissa n. kolme vuorokautta
kyselyn valmistumisesta. **Huom!** Tiedostonlatauslinkki vastaa uudelleenohjauspyynnöllä, joten kytke
käyttämästäsi http-asiakasohjelmasta *follow redirects* päälle, jos saamasi tiedostot ovat tyhjiä.

    {{json:ValpasCompleteQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.CompleteQueryResponse}}

### Kyselyn epäonnistuminen

Kyselyn epäonnistuessa tila on `failed`. Epäonnistumisen syytä ei tietoturvasyistä kerrota,
mutta ne ovat lähes aina palvelinpään teknisiä ongelmia (eli vertautuvat HTTP-pyyntöjen 5xx-virheisiin):

    {{json:ValpasFailedQueryResponse}}

{{docs:fi.oph.koski.massaluovutus.FailedQueryResponse}}

Kysely ei koskaan palaa takaisin `pending`- tai `running`-tilaan sen päädyttyä `failed`-tilaan,
vaan kutsujan on aloitettava uusi kysely. Jos kysely päätyy jatkuvasti `failed`-tilaan, ota
yhteyttä KOSKI-tiimiin.




{{title:fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery}}

{{docs:fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQuery}}


Esimerkki:


    POST {{var:baseUrl}}/valpas/api/massaluovutus HTTP/1.1

    {{var:headers}}


    {{json:ValpasEiOppivelvollisuuttaSuorittavatQuery}}

[Vastauksena saatavien json-dokumenttien rakennetta voi tarkastella skeeman visualisoinnissa](/koski/json-schema-viewer/?schema=valpas-ei-oppivelvollisuutta-suorittavat-result.json).


{{title:fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery}}

{{docs:fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery}}


Esimerkki:


    POST {{var:baseUrl}}/valpas/api/massaluovutus HTTP/1.1

    {{var:headers}}


    {{json:ValpasOppivelvollisetQuery}}

[Vastauksena saatavien json-dokumenttien rakennetta voi tarkastella skeeman visualisoinnissa](/koski/json-schema-viewer/?schema=valpas-oppivelvolliset-result.json).
