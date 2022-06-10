# valpas_qa_peruskoulujen_oidit.txt

Tiedosto sisältää joka rivillä ensin oppilaitoksen oidin ja sen jälkeen oppilaitoksesta haettavien oppijoiden oidit.

Ilman oppija-oideja jää Sure-rajapinta testaamatta.

Oppilaitosten muuttuessa tiedostoon laitettavat rivit voi hakea raportointitietokannasta sql-kyselyllä:

```sql
with oppilaitokset as (
    select unnest(string_to_array(
        -- Tähän riville kaikki oppilaitos-oidit välilyönnillä erotettuna:
            '1.2.246.562.10.10059380252 1.2.246.562.10.10097597065 1.2.246.562.10.102806581210 ...',
            ' '
        )) as oppilaitos_oid
)
   , loytyvat_oppijat as (
        select unnest(string_to_array(
            -- Tähän riville kaikki oppija-oidit, jotka löytyvät Valppaasta (valpas_qa_oppija_oidit.txt) välilyönnillä erotettuna:
                '1.2.246.562.24.50770631005 1.2.246.562.24.10001511889 1.2.246.562.24.10001665997  ...',
                ' '
            )) as oppija_oid
    )
select
    concat(
            oppilaitos_oid,
            ' ',
            (select
                 string_agg(distinct r_opiskeluoikeus.oppija_oid, ' ')
             from
                 r_opiskeluoikeus
                     join loytyvat_oppijat on r_opiskeluoikeus.oppija_oid = loytyvat_oppijat.oppija_oid
             where
                 oppilaitos_oid = oppilaitokset.oppilaitos_oid)
        ) as row
from oppilaitokset;
```
