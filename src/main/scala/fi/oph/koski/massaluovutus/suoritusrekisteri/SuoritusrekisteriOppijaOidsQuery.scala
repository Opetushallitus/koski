package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp

@Title("Suoritusrekisterin kysely oppijoiden perusteella")
@Description("Palauttaa Suoritusrekisteriä varten räätälöidyt tiedot annettujen oppijoiden opiskeluoikeuksista.")
@Description("Vastauksen skeema on taulukko <a href=\"/koski/json-schema-viewer/?schema=suoritusrekisteri-result.json\">SureResponse</a>-objekteja.")
case class SuoritusrekisteriOppijaOidsQuery(
  @EnumValues(Set("sure-oppijat"))
  `type`: String = "sure-oppijat",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Lista oppijoiden oideista, joiden tiedot haetaan")
  oppijaOids: Seq[String],
) extends SuoritusrekisteriQuery {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)] = {
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT opiskeluoikeus.id, opiskeluoikeus.aikaleima, coalesce(henkilo.master_oid, henkilo.oid) as master_oid
        FROM opiskeluoikeus
        JOIN henkilo ON henkilo.oid = opiskeluoikeus.oppija_oid
        WHERE
          (henkilo.oid = any($oppijaOids) OR
          henkilo.master_oid = any($oppijaOids))
          AND koulutusmuoto = any(${SuoritusrekisteriQuery.opiskeluoikeudenTyypit})
      """.as[(Int, Timestamp, String)])
  }
}
