package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.Tiedonsiirto
import fi.oph.koski.db.{KoskiDatabaseMethods, TiedonsiirtoRow}
import fi.oph.koski.http.ErrorDetail
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.util.Timing
import rx.lang.scala.Observable

class TiedonsiirtoMigrator(tiedonsiirtoService: TiedonsiirtoService, val db: DB) extends KoskiDatabaseMethods with Timing {
  def runMigration(minId: Int) = {
    println(s"Migrating with minId=$minId")
    val tableQuery = Tiedonsiirto

    val rows: Observable[TiedonsiirtoRow] = streamingQuery(tableQuery.filter(_.id >= minId).sortBy(_.id))

    val total = rows.foldLeft(0){ case (count, row) =>
      println("Migrate " + row)
      tiedonsiirtoService.storeToElasticSearch(
        row.oppija.map(extract[TiedonsiirtoOppija](_)),
        OidOrganisaatio(row.tallentajaOrganisaatioOid),
        row.oppilaitos.map(extract[List[OidOrganisaatio]](_)),
        row.data,
        row.virheet.map(extract[List[ErrorDetail]](_)),
        row.lahdejarjestelma,
        row.kayttajaOid,
        None,
        row.aikaleima
      )
      count + 1
    }

    total.toBlocking.foreach { t => println(s"$t rows migrated") }
  }
}

object TiedonsiirtoMigrator extends App {
  val koski = KoskiApplication.apply
  val minId = sys.env.getOrElse("MIN_ID", "0").toInt
  koski.tiedonsiirtoService.init
  new TiedonsiirtoMigrator(koski.tiedonsiirtoService, koski.replicaDatabase.db).runMigration(minId)
}