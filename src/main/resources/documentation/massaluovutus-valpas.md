# Massaluovutusrajapinnat oppivelvollisuuden valvontaan

Tällä sivulla kuvataan oppivelvollisuuden valvontaan tarkoitetut massaluovutuskyselyt, jotka hakevat tietoja
KOSKI-datasta ja Valpas-palvelun koostamasta datasta. Kyselyn luonti, tilan seuranta ja tulosten lataus on
kuvattu [yleisissä ohjeissa](/koski/dokumentaatio/rajapinnat/massaluovutus). Oppivelvollisuuden valvonnan
kyselyt lähetetään osoitteeseen `POST {{var:baseUrl}}/valpas/api/massaluovutus`, ja myös kyselyn tila ja
tulostiedostot haetaan tästä osoitteesta.
[Katso myös kyselyiden skeeman visualisointi](/koski/json-schema-viewer/?schema=valpas-massaluovutus-query.json).

{{query:ValpasEiOppivelvollisuuttaSuorittavatQuery}}

[Vastauksena saatavien json-dokumenttien rakennetta voi tarkastella skeeman visualisoinnissa](/koski/json-schema-viewer/?schema=valpas-ei-oppivelvollisuutta-suorittavat-result.json).

{{query:ValpasOppivelvollisetQuery}}

[Vastauksena saatavien json-dokumenttien rakennetta voi tarkastella skeeman visualisoinnissa](/koski/json-schema-viewer/?schema=valpas-oppivelvolliset-result.json).
