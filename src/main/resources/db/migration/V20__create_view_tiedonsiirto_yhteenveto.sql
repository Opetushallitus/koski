create view tiedonsiirto_yhteenveto as (with siirrot as (
    select id, aikaleima, virheet, oppija, lahdejarjestelma, jsonb_array_elements(oppilaitos) ->> 'oid' as oppilaitos
    from tiedonsiirto
), latest as (
    select max(id) as id
    from siirrot
    where aikaleima > current_timestamp - interval '7 days'
    group by oppija, oppilaitos
), org_virheelliset as (
    select siirrot.oppilaitos, count(*) as virheet
    from siirrot
      join latest on (siirrot.id = latest.id)
    where siirrot.virheet is not null
    group by siirrot.oppilaitos
), org_aikaleimat as (
    select max(aikaleima) as aikaleima, oppilaitos
    from siirrot
    where id in (select id from latest)
    group by oppilaitos
), org_opiskeluoikeudet as (
    select data -> 'oppilaitos' ->> 'oid' as oppilaitos, count(*) as lukumäärä
    from opiskeluoikeus
    group by data -> 'oppilaitos' ->> 'oid'
), org_lahdejarjestelma as (
    select oppilaitos, lahdejarjestelma
    from siirrot
    where id in (select id from latest)
    and lahdejarjestelma is not null
)

select org_aikaleimat.oppilaitos, aikaleima as viimeisin, virheet, org_opiskeluoikeudet.lukumäärä as opiskeluoikeudet, lahdejarjestelma
from org_aikaleimat
  left join org_virheelliset on (org_aikaleimat.oppilaitos = org_virheelliset.oppilaitos)
  left join org_opiskeluoikeudet on (org_aikaleimat.oppilaitos = org_opiskeluoikeudet.oppilaitos)
  left join org_lahdejarjestelma on (org_aikaleimat.oppilaitos = org_lahdejarjestelma.oppilaitos))