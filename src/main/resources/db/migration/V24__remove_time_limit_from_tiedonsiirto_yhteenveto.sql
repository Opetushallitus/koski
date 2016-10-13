drop view tiedonsiirto_yhteenveto;

create view tiedonsiirto_yhteenveto as (WITH siirrot AS (
    SELECT id,
      aikaleima,
      virheet,
      oppija,
      lahdejarjestelma,
      tallentaja_organisaatio_oid as tallentaja_organisaatio,
      kayttaja_oid as kayttaja,
      jsonb_array_elements(oppilaitos) ->> 'oid' AS oppilaitos
    FROM tiedonsiirto
), latest AS (
    SELECT * FROM siirrot
    WHERE ID in (
      SELECT max(siirrot.id) AS id
      FROM siirrot
      GROUP BY siirrot.oppija, siirrot.oppilaitos
    )
), org_tiedonsiirto AS (
    SELECT tallentaja_organisaatio, kayttaja, oppilaitos, max(aikaleima) AS viimeisin, max(lahdejarjestelma) AS lahdejarjestelma, count(*) AS siirretyt, sum((CASE WHEN virheet is null THEN 0 ELSE 1 END)) as virheelliset
    FROM latest
    GROUP BY oppilaitos, tallentaja_organisaatio, kayttaja
), org_opiskeluoikeudet AS (
    SELECT oppilaitos_oid AS oppilaitos, count(*) AS opiskeluoikeudet
    FROM opiskeluoikeus
    GROUP BY oppilaitos_oid
)

SELECT org_tiedonsiirto.oppilaitos,
  tallentaja_organisaatio,
  kayttaja,
  viimeisin,
  siirretyt,
  virheelliset,
  opiskeluoikeudet,
  lahdejarjestelma
FROM org_tiedonsiirto
  LEFT JOIN org_opiskeluoikeudet ON org_tiedonsiirto.oppilaitos = org_opiskeluoikeudet.oppilaitos);
