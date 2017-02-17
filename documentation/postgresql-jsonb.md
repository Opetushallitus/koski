### JSON(B)-opas

JSONB-koulutus Koski-järjestelmän kehittäjille ja ylläpitäjille.

Esitietoina odotamme sinun tuntevan relaatiotietokantojen perusteet ja SQL-kyselykielen.

Koulutuksessa tutustutaan PostgreSQL:n JSONB-ominaisuuksiin käytännön harjoituksilla Koski-järjestelmän kantaa vasten. Koulutusta varten on tehty testikanta, jossa on Koski-tietokannan schema ja jonkin verran testidataa.

Koulutuksen tavoitteena on oppia käyttämään PostgreSQL:n JSONB SQL-laajennuksia tiedon hakuun Koski-kannasta. 

Referenssinä kannattaa käyttää PostgreSQL 9.5 JSON(B)-dokumentaatiota: https://www.postgresql.org/docs/9.5/static/functions-json.html

Lisäksi voi lukaista PostgreSQL JSON -tutoriaalin netissä: http://schinckel.net/2014/05/25/querying-json-in-postgres/

Koski-järjestelmän tietuekuvaus löytyy [dokumentaatiosivulta](https://koskidev.koski.oph.reaktor.fi/koski/documentation). Data tietokannassa noudattaa samaa JSON-schemaa, joka on kuvattu dokumentaatiossa.

### SQL-kyselyjä

Haetaan kaikkien opiskeluoikeuksien määrä

```sql
select count(*) from opiskeluoikeus;
````

“Normaaleja” kenttiä voi hakea SQL:llä. Esimerkiksi haku oppijan oidilla:

```sql
select * from opiskeluoikeus where oppija_oid = '1.2.246.562.24.68660987408'
````

### JSON-data haku operaattoreilla `->` ja `->>`

Opiskeluoikeus-taulun `data`-kenttä sisältää JSONB-dataa, joka näkyy SQL-työkalussa pitkänä tekstipötkönä 
ja jota voidaan käsitellä Postgren JSON-operaattoreilla. Esim.

```sql
select data ->> 'tila' from opiskeluoikeus
```

Tässä `->>` -operaattorilla haetaan `data`-kentän JSONB-oliosta `tila`-kentän arvo merkkijonona. 

Jos halutaan porautua syvemmälle JSONB-rakenteeseen, voidaan rakenteessa mennä taso kerrallaan sisään `->` -operaattorilla, ja lopuksi
tulostaa halutun kentän arvo merkkijonona `->>` -operaattorilla.

Esimerkiksi haetaan oppilaitoksen tunniste:

```sql
select data -> 'oppilaitos' ->> 'oid' from opiskeluoikeus
```

Ryhmitellään opiskeluoikeudet tyypin mukaan:

```
select data -> 'tyyppi' ->> 'koodiarvo' as tyyppi, count(*) as lkm
from opiskeluoikeus
group by data -> 'tyyppi' ->> 'koodiarvo'
```

Kaikkia JSONB-operaattoreita ja funktioita voidaan käyttää myös SQL-kyselyn `WHERE` -osiossa ja indekseissä.

*Harjoitus:*: Hae niiden oppijoiden oidit, joilla on opiskeluoikeus Stadin ammattiopistossa

*Harjoitus:*: Hae opiskeluoikeuksien lukumäärät, ryhmiteltynä oppilaitoksittain. Näytä oppilaitoksen nimi, oid ja lukumäärä. Järjestä lukumäärän mukaan, suurin lukumäärä ensin.

### JSON-listan viimenen alkio `jsonb_array_length`-funktiolla

Monissa JSONB-kentissä on lista (json array) asioita. Listan viimeinen alkio saadaan haettua `jsonb_array_length` -funktion avulla.
Esimerkiksi opiskeluoikeuden tilahistorian viimeisin, eli voimassa oleva tila:

```sql
select data -> 'tila' -> 'opiskeluoikeusjaksot' -> (jsonb_array_length(data -> 'tila' -> 'opiskeluoikeusjaksot') - 1) 
from opiskeluoikeus
```

Haetaan opiskeluoikeudet, jotka ovat tilassa “Läsnä”:

```sql
select * from opiskeluoikeus 
where data -> 'tila' -> 'opiskeluoikeusjaksot' -> (jsonb_array_length(data -> 'tila' -> 'opiskeluoikeusjaksot') - 1) -> 'tila' ->> 'koodiarvo' = 'lasna'
```

*Harjoitus*: Hae oppijat, joilla on aktiivinen (päättymispäivä puuttuu) opiskeluoikeus Stadin ammattiopistossa,
ja joiden opiskeluoikeuden tila on "Läsnä".

### JSON-listojen purkaminen `jsonb_array_elements`-funktiolla

Listatyyppisten kenttien sisältö voidaan tarvittaessa avata omaksi SQL-tulosjoukokseen funktiolla `jsonb_array_elements`, jolloin
listan alkioita voidaan käsitellä erillisinä riveinä, aivan kuin ne olisiva omassa taulussaan tietokannassa.
Haetaan kaikki suorituksen opiskeluoikeus-olioien sisältä.

```sql
select oppija_oid, jsonb_array_elements(data -> 'suoritukset') from opiskeluoikeus;
```

Oppijoiden suorittamien valmiiden päätason suoritusten tyypit:

```sql
select oppija_oid, suoritus -> 'tyyppi' ->> 'koodiarvo' as tyyppi 
from (
  select oppija_oid, jsonb_array_elements(data -> 'suoritukset') as suoritus 
  from opiskeluoikeus
) as suoritukset
where suoritus -> 'tila' ->> 'koodiarvo' = 'VALMIS';
```

Perusopetuksen päättötodistuksen aineiden arvosanat:

```sql
select oppija_oid,
  ainesuoritus -> 'koulutusmoduuli' -> 'tunniste' ->> 'koodiarvo' as aine,
  ainesuoritus -> 'arviointi' -> (jsonb_array_length(ainesuoritus -> 'arviointi') - 1) -> 'arvosana' ->> 'koodiarvo' as arvosana
from (
  select oppija_oid, jsonb_array_elements(suoritus -> 'osasuoritukset') as ainesuoritus 
  from (
    select oppija_oid, jsonb_array_elements(data -> 'suoritukset') as suoritus 
    from opiskeluoikeus
    where data -> 'tyyppi' ->> 'koodiarvo' = 'perusopetus'
  ) as suoritukset

  where suoritukset.suoritus -> 'tyyppi' ->> 'koodiarvo' = 'perusopetuksenoppimaara'
    and suoritukset.suoritus -> 'tila' ->> 'koodiarvo' = 'VALMIS'
 ) as ainesuoritukset
```

*Harjoitus*: Hae oppijoiden suorittamien valmiiden ammatillisten tutkinnon osien tunnisteet, nimet ja arvosanat

*Harjoitus*: Laske oppijoiden suorittamien valmiiden ammatillisten tutkinnon osien arvosanojen keskiarvo oppijoittain

### Tietojen haku `@>` -operaattorilla

Opiskeluoikeudet, joihin sisältyy lukion oppimäärän suoritus:

```sql
select *
from opiskeluoikeus
where data -> 'suoritukset' @> '[{"koulutusmoduuli": {"tunniste": {"koodiarvo": "309902"}}}]'
```

Oppijat, joilla on lukion oppimäärän suorituksen osasuorituksia (ainesuorituksia):

```sql
select oppija_oid
from opiskeluoikeus
where data -> 'suoritukset' @> '[{"koulutusmoduuli": {"tunniste": {"koodiarvo": "309902"}}, "osasuoritukset": [{}]}]'
```

Yksi taso syvemmälle, eli ne lukion oppimäärän suoritukset, joista löytyy ainesuorituksia joiden sisällä on kurssisuorituksia:

```sql
select oppija_oid
from opiskeluoikeus
where data -> 'suoritukset' @> '[{"koulutusmoduuli": {"tunniste": {"koodiarvo": "309902"}}, "osasuoritukset": [{"osasuoritukset":[{}]}]}]'
```

Lopuksi osasuoritusten määrät lukion oppimäärän suorituksissa:

```sql
with suoritus as

(select id, oppija_oid, jsonb_array_elements(data -> 'suoritukset') as suoritus
from opiskeluoikeus
where data -> 'suoritukset' @> '[{"koulutusmoduuli": {"tunniste": {"koodiarvo": "309902"}}}]')

select id, oppija_oid, jsonb_array_length(suoritus -> 'osasuoritukset') osasuorituksia
from suoritus
order by osasuorituksia desc;
```

### Tietojen päivittäminen

SQL-harjoituksissa ei tehdä kantaan muutoksia eikä poistoja; nämä operaatiot on tehtävä Koski-järjestelmän rajapintojen kautta, jotta kannan (ja erityisesti tietojen muutoshistorian) eheys säilyisi. 

Koski-järjestelmän testiympäristön [dokumentaatiosivu](https://koskidev.koski.oph.reaktor.fi/koski/documentation) kuvaa nämä rajapinnat ja mahdollistaa tietojen syötön ja katselun käyttäen selainkäyttöliittymää.

Jos oppijan tietohin halutaan tehdä manuaalinen muutos voidaan se tehdä seuraavasti:

1. Haetaan oppijan opiskeluoikeudet JSON-muodossa käyttäen GET-rajapintaa
2. Muokataan JSON-dokumenttia tekstieditorilla
3. Tallennetaan muutokset käyttäen PUT-rajapintaa. Tämä rajapinta validoi tietojen oikeamuotoisuuden ja tallentaa myös aina uuden rivin opiskeluoikeuden versiohistoriaan.

Jos kantaan halutaan tehdä massamuutoksia useampiin opiskeluoikeuksiin tai niihin sisältyviin suorituksiin, on myös nämä muutokset tehtävä HTTP-rajapinnan kautta, esimerkiksi kirjoittamalla skripti, joka hakee halutut tiedot ja päivittää ne käyttäen GET- ja PUT-rajapintoja. Muutokset suoraan SQL-kielellä ovat äärimmäisen virhealttiita. Mieti: osaatko rakentaa muutoksesta manuaalisesti versionumerorivin, joka sisältää muutoksen JSON-diffin, joka mahdollistaa palaamisen mihin tahansa aiempaan versioon tiedoista?
