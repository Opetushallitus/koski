### JSON(B)-opas

JSONB-koulutus Koski-järjestelmän kehittäjille ja ylläpitäjille.

Esitietoina odotamme sinun tuntevan relaatiotietokantojen perusteet ja SQL-kyselykielen.

Koulutuksessa tutustutaan PostgreSQL:n JSON(B)-ominaisuuksiin käytännön harjoituksilla Koski-järjestelmän kantaa vasten. Koulutusta varten on tehty testikanta, jossa on Koski-tietokannan schema ja jonkin verran testidataa.

PostgreSQL 9.5 JSON functions: https://www.postgresql.org/docs/9.5/static/functions-json.html(()

Hyvä PostgreSQL JSON -tutoriaali netissä: http://schinckel.net/2014/05/25/querying-json-in-postgres/

### Kyselyjä

Haetaan kaikki opiskeluoikeudet

```sql
select * from opiskeluoikeus;
````

“Normaaleja” kenttiä voi hakea SQL:llä. Esimerkiksi

```sql
select * from opiskeluoikeus where oppija_oid = ‘1.2.246.562.24.68660987408'
````

Data-kenttä sisältää JSON(B)-dataa, jota voidaan käsitellä Postgren JSON-operaattoreilla. Esim

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

Monissa JSONB-kentissä on lista (json array) asioita. Listan viimeinen (uusin) alkio saadaan haettua `jsonb_array_length` -funktion avustuksella.
Esimerkiksi opiskeluoikeuden tilahistorian viimeisin (nykyinen) tila:

```sql
select data -> 'tila' -> 'opiskeluoikeusjaksot' -> (jsonb_array_length(data -> 'tila' -> 'opiskeluoikeusjaksot') - 1) 
from opiskeluoikeus
```

Kaikkia JSONB-operaattoreita ja funktioita voidaan käyttää myös SQL-kyselyn `WHERE` -osiossa. 
Haetaan opiskeluoikeudet, jotka ovat tilassa “Läsnä”:

```sql
select * from opiskeluoikeus 
where data -> 'tila' -> 'opiskeluoikeusjaksot' -> (jsonb_array_length(data -> 'tila' -> 'opiskeluoikeusjaksot') - 1) -> 'tila' ->> 'koodiarvo' = 'lasna'
```

Listatyyppisten kenttien sisältö voidaan tarvittaessa avata omaksi SQL-tulosjoukokseen funktiolla `jsonb_array_elements`, jolloin
listan alkioita voidaan käsitellä erillisinä riveinä, aivan kuin ne olisiva omassa taulussaan tietokannassa.
Haetaan kaikki suorituksen opiskeluoikeus-olioien sisältä.

```sql
select oppija_oid, jsonb_array_elements(data -> 'suoritukset') from opiskeluoikeus;
```

Oppijoiden suorittamien valmiiden päätason suoritusten tyypit:

```sql
select oppija_oid, suoritus -> 'tyyppi' ->> 'koodiarvo' as tyyppi from
(select oppija_oid, jsonb_array_elements(data -> 'suoritukset') as suoritus from opiskeluoikeus) as suoritukset
where suoritus -> 'tila' ->> 'koodiarvo' = 'VALMIS';
```

Perusopetuksen päättötodistuksen aineiden arvosanat:

```sql
select oppija_oid,
  ainesuoritus -> 'koulutusmoduuli' -> 'tunniste' ->> 'koodiarvo' as aine,
  ainesuoritus -> 'arviointi' -> (jsonb_array_length(ainesuoritus -> 'arviointi') - 1) -> 'arvosana' ->> 'koodiarvo' as arvosana
from
(select oppija_oid, jsonb_array_elements(suoritus -> 'osasuoritukset') as ainesuoritus from
  (select oppija_oid, jsonb_array_elements(data -> 'suoritukset') as suoritus from opiskeluoikeus
    where data -> 'tyyppi' ->> 'koodiarvo' = 'perusopetus') as suoritukset

  where suoritukset.suoritus -> 'tyyppi' ->> 'koodiarvo' = 'perusopetuksenoppimaara'
    and suoritukset.suoritus -> 'tila' ->> 'koodiarvo' = 'VALMIS') as ainesuoritukset
```
