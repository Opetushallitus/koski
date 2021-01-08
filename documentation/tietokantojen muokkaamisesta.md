## Tietokantayhteydet

Tietokantoihin pääsee käsiksi avaamalla VPN-yhteyden sopivan Koski-ympäristön AWS-sisäverkkoon. Secrets Managersista löydät tietokantojen osoitteet, nimet ja tunnistautumistiedot.

## Tuotantodatan muokkaamisesta

Tuotantodatan muokkaamista tulisi tietenkin välttää, mutta sikäli niin päätyy tekemään, tulee ottaa huomioon versiohistoria. Kosken versiohistoria on muutoshistoria ja näistä muutoksista pitäisi pystyä rakentamaan samanlainen JSON-dokumentti, kuin mikä on nyt talletettuna päätauluun.

### Query-esimerkkejä

Esimerkiksi näin voisi hakea historiatietokannasta viimeisimmät muutos-rivit koulutustoimijan mukaan:
```sql
select count(*)
from opiskeluoikeushistoria
join opiskeluoikeus on opiskeluoikeushistoria.opiskeluoikeus_id = opiskeluoikeus.id
and opiskeluoikeushistoria.versionumero = opiskeluoikeus.versionumero 
where koulutustoimija_oid = '1.2.246.562.10.98140037775'
```
Jos jostain syystä ei voisi luottaa opiskeluoikjeuden versionumeroon, niin voisi myös hakea suurimman versionumeron mukaan:
```sql
select *
from opiskeluoikeushistoria
where (opiskeluoikeus_id, versionumero) in (
	select opiskeluoikeus_id, MAX(opiskeluoikeushistoria.versionumero)
	from opiskeluoikeushistoria
	join opiskeluoikeus on opiskeluoikeushistoria.opiskeluoikeus_id = opiskeluoikeus.id
	where koulutustoimija_oid = '1.2.246.562.10.98140037775'
	group by opiskeluoikeus_id
)
```

Syy sisäkkäisen queryn käyttöön on se, että MAX-aggregaatti ei toimisi jos groupattaisiin vaihtelevien data-sarakkeiden mukaan.

Postgres ei välttämättä osaa aina optimoida filtterien evaluointia JSONB-datoja käsiteltäessä. Tällöin järjestyksen voi pakottaa (väärin)käyttämällä `case when` -rakennetta:

```sql
select *
from opiskeluoikeushistoria
join opiskeluoikeus on opiskeluoikeushistoria.opiskeluoikeus_id = opiskeluoikeus.id and opiskeluoikeushistoria.versionumero = opiskeluoikeus.versionumero 
where (case when koulutustoimija_oid = '1.2.246.562.10.98140037775' then (case when data -> 'lisätiedot' is null then 1 end) end) = 1;
```

Tässä haetaan viimeisin historiarivi, johon liittyvällä opiskeluoikeudella ei ole lisätietoja.

#### Datan päivitys

Päivittäessä historiatietokantaan uutta entryä, täytyy muutoksessa päivittää ylimmän puuttuvan `path`in sisältö kerralla sisään.

Esimerkiksi jos opiskeluoikeudelta puuttuu `lisätiedot`-kenttä kokonaan ja haluaisi päivittää `lisätiedot`-kenttään kuuluvan `henkilöstökoulutus`-kentän arvoksi `true`, lisätään silloin tällainen operaatio muutoshistoriaan:
`{"op": "add", "path": "/lisätiedot", "value": {"henkilöstökoulutus": true}}`

Eli koko `lisätiedot`-kenttä päivitetään kerralla.

Jos opiskeluoikeudella olisi jo `lisätiedot`-kentässä jotain sisältöä, mutta ei vielä `henkilöstökoulutus`-kenttää, käytettäisiin muotoa:
`{"op": "add", "path": "/lisätiedot/henkilöstökoulutus", "value": true}`

Jos jo olemassaolevan kentän arvoa muutetaan, operaattorina on `replace`, eikä `add`.

Esimerkki viimeisimmän historia-rivin päivittämisestä:
```sql
update opiskeluoikeushistoria
set muutos = muutos || '[{"op": "add", "path": "/lisätiedot/henkilöstökoulutus", "value": true}]'::jsonb
where (opiskeluoikeus_id, versionumero) in (
	select opiskeluoikeus_id, MAX(opiskeluoikeushistoria.versionumero)
	from opiskeluoikeushistoria
	join opiskeluoikeus on opiskeluoikeushistoria.opiskeluoikeus_id = opiskeluoikeus.id
	where (case when koulutustoimija_oid = '1.2.246.562.10.98140037775' then (case when data -> 'lisätiedot' is not null then 1) end) = 1
	group by opiskeluoikeus_id
)
and muutos::varchar not like '%henkilöstökoulutus%'
```

Tätä päivitystä ennen oltiin jo lisätty `"lisätiedot": {"henkilökoulutus": true}`-rakenne sellaisille sopiville opinto-oikeuksille, joilla ei lisätiedot-rakennetta vielä ollenkaan ollut. Tässä päivityksessä lisättiin vielä `"henkilökoulutus": true` lisätietoihin sellaisiin viimeisimpiin historiariveihin, joissa tätä kenttää ei oltu lisätty.

Päätaulun eli `opiskeluoikeus`-taulun kohdalla voi myös olla tarpeen huomioida se, että onko JSON-rakenteessa ylemmät rakenteet jo olemassa.

Esimerkiksi jos `lisätiedot`-kenttää ei ole olemassa, voidaan se luoda valmiiksi alustettuna tai tyhjänä vaikkapa näin:
```
update opiskeluoikeus
set data = jsonb_set(data, '{lisätiedot}', '{}')
where (case when koulutustoimija_oid = '1.2.246.562.10.98140037775' then (case when data -> 'lisätiedot' is not null then 1 end) end) = 1;
```
Tässä on tarpeen käsitellä vain sellaisia opiskeluoikeuksia, joilla ei vielä lisätietoa ole, jottei olemassaolevat lisätiedot ylikirjottautuisi.

Tämän jälkeen voidaan sitten päivittää `henkilöstökoulutus`-kenttä kaikille sopiville opiskeluoikeuksille:
```
update opiskeluoikeus
set data = jsonb_set(data, '{lisätiedot,henkilöstökoulutus}', 'true')
where koulutustoimija_oid = '1.2.246.562.10.98140037775'
```

Vinkki: DBeaver on oikein hyvä softa tähän hommaan, queryjen ajamisen lisäksi  siinä voi myös suoraan muokata palautettuja sarakkeita haku-queryillä ja sitten tallettaa ne, jolloin se ajaa päivitysskriptin tietokantaa vasten.
