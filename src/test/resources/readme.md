# valpas_qa_peruskoulujen_oidit.txt

Tiedosto sisältää joka rivillä ensin oppilaitoksen oidin ja sen jälkeen oppilaitoksesta haettavien oppijoiden oidit.

Ilman oppija-oideja jää Sure-rajapinta testaamatta.

Oppilaitosten muuttuessa tiedostoon laitettavat rivit voi hakea raportointitietokannasta sql-kyselyllä:

```sql
with oppilaitokset as (
    select unnest(string_to_array(
        -- Tähän riville kaikki oppilaitos-oidit välilyönnillä erotettuna:
        '1.2.246.562.10.67636414343 1.2.246.562.10.14613773812 1.2.246.562.10.62858797335',
        ' '
    )) as oppilaitos_oid)
select
    concat(
        oppilaitos_oid,
        ' ',
        (select string_agg(oppija_oid, ' ') from r_opiskeluoikeus where oppilaitos_oid = oppilaitokset.oppilaitos_oid)
    ) as row
from oppilaitokset;
```
