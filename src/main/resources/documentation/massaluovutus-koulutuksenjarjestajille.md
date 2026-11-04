# Massaluovutusrajapinta koulutuksenjärjestäjille

Tällä sivulla kuvataan koulutuksenjärjestäjille tarkoitetut KOSKI-datan massaluovutuskyselyt. Kyselyn luonti,
tilan seuranta ja tulosten lataus on kuvattu [yleisissä ohjeissa](/koski/dokumentaatio/rajapinnat/massaluovutus).
Kyselyt lähetetään osoitteeseen `POST {{var:baseUrl}}/api/massaluovutus`.
[Katso myös kaikki kyselyiden skeemat graafisessa muodossa](/koski/json-schema-viewer/?schema=massaluovutus-query.json).

Raporttinäkymän raportit, mukaan lukien päällekkäiset opiskeluoikeudet, on kuvattu
[raporttien dokumentaatiossa](/koski/dokumentaatio/rajapinnat/massaluovutus/raportit).

{{query:OrganisaationOpiskeluoikeudetCsv,OrganisaationOpiskeluoikeudetJson}}

{{query:SisaltyvatOpiskeluoikeudet}}

{{query:LuokalleJaaneetJson,LuokalleJaaneetCsv}}
