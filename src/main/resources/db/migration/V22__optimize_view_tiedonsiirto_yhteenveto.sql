drop view tiedonsiirto_yhteenveto;

create view tiedonsiirto_yhteenveto as (WITH siirrot AS (
    SELECT tiedonsiirto.id,
      tiedonsiirto.aikaleima,
      tiedonsiirto.virheet,
      tiedonsiirto.oppija,
      tiedonsiirto.lahdejarjestelma,
      jsonb_array_elements(tiedonsiirto.oppilaitos) ->> 'oid' AS oppilaitos
    FROM tiedonsiirto
), latest AS (
    SELECT * FROM siirrot
    WHERE ID in (
      SELECT max(siirrot.id) AS id
      FROM siirrot
      WHERE siirrot.aikaleima > (now() - '7 days'::interval)
      GROUP BY siirrot.oppija, siirrot.oppilaitos
    )
), org_tiedonsiirto AS (
    SELECT oppilaitos, max(aikaleima) AS viimeisin, max(lahdejarjestelma) AS lahdejarjestelma, count(*) AS siirretyt, sum((CASE WHEN virheet is null THEN 0 ELSE 1 END)) as virheelliset
    FROM latest
    GROUP BY oppilaitos
), org_opiskeluoikeudet AS (
    SELECT oppilaitos_oid AS oppilaitos, count(*) AS opiskeluoikeudet
    FROM opiskeluoikeus
    GROUP BY oppilaitos_oid
)

SELECT org_tiedonsiirto.oppilaitos,
  viimeisin,
  siirretyt,
  virheelliset,
  opiskeluoikeudet,
  lahdejarjestelma
FROM org_tiedonsiirto
  LEFT JOIN org_opiskeluoikeudet ON org_tiedonsiirto.oppilaitos = org_opiskeluoikeudet.oppilaitos);