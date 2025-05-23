package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp

@Title("Suorituspalvelun kysely oppijoiden perusteella")
@Description("Palauttaa Suorituspalvelua varten räätälöidyt tiedot annettujen oppijoiden opiskeluoikeuksista.")
@Description("Vastauksen skeema on taulukko <a href=\"/koski/json-schema-viewer/?schema=suorituspalvelu-result.json\">SupaResponse</a>-objekteja.")
case class SuorituspalveluOppijaOidsQuery(
  @EnumValues(Set("supa-oppijat"))
  `type`: String = "supa-oppijat",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Lista oppijoiden oideista, joiden tiedot haetaan")
  oppijaOids: Seq[String],
) extends SuorituspalveluQuery {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)] = {
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT opiskeluoikeus.id, opiskeluoikeus.aikaleima, coalesce(henkilo.master_oid, henkilo.oid) as master_oid
        FROM opiskeluoikeus
          JOIN henkilo ON henkilo.oid = opiskeluoikeus.oppija_oid
        WHERE COALESCE(henkilo.master_oid, henkilo.oid) IN (
          SELECT DISTINCT COALESCE(h.master_oid, h.oid)
          FROM henkilo h
          WHERE h.oid = any($oppijaOids)
          )
        AND opiskeluoikeus.koulutusmuoto = any(${SuorituspalveluQuery.opiskeluoikeudenTyypit})
      """.as[(Int, Timestamp, String)])
  }
}
