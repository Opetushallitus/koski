package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp
import java.time.LocalDateTime

@Title("Suoritusrekisterin kysely päivämäärän perusteella")
@Description("Palauttaa Suoritusrekisteriä varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista tietyn ajanhetken jälkeen.")
@Description("Vastauksen skeema on taulukko <a href=\"/koski/json-schema-viewer/?schema=suoritusrekisteri-result.json\">SureResponse</a>-objekteja.")
case class SuoritusrekisteriMuuttuneetJalkeenQuery(
  @EnumValues(Set("sure-muuttuneet"))
  `type`: String = "sure-muuttuneet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Aikaraja, jonka jälkeen muuttuneet opiskeluoikeudet haetaan.")
  @Description("Teknisistä syistä johtuen kannattaa aina hakea tiedot hieman pidemmältä ajalta ja suodattaa itse tulosjoukosta tarpeettomat tiedot.")
  muuttuneetJälkeen: LocalDateTime,
) extends SuoritusrekisteriQuery {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp, String)] =
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT opiskeluoikeus.id,
          opiskeluoikeus.aikaleima,
          COALESCE(henkilo.master_oid, henkilo.oid) AS master_oid
        FROM opiskeluoikeus
          JOIN henkilo ON henkilo.oid = opiskeluoikeus.oppija_oid
        WHERE COALESCE(henkilo.master_oid, henkilo.oid) IN (
          SELECT DISTINCT COALESCE(h.master_oid, h.oid) AS master_oid
          FROM henkilo h
            JOIN opiskeluoikeus o ON h.oid = o.oppija_oid
          WHERE aikaleima >= ${Timestamp.valueOf(muuttuneetJälkeen)}
            AND o.koulutusmuoto = ANY(${SuoritusrekisteriQuery.opiskeluoikeudenTyypit}))
        ORDER BY opiskeluoikeus.aikaleima
      """.as[(Int, Timestamp, String)])
}