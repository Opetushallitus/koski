package fi.oph.tor.opintooikeus

import fi.oph.tor.db._
import fi.oph.tor.oppija.MockOppijaRepository
import fi.oph.tor.oppilaitos.Oppilaitos
import fi.oph.tor.tutkinto.Tutkinto
import slick.dbio.DBIO
import Tables._
import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import TorDatabase._

object TorDatabaseFixtures extends Futures with GlobalExecutionContext {
  private val oppijat = new MockOppijaRepository
  private val autoalanPerustutkinto: Tutkinto = Tutkinto("39/011/2014", "351301", Some("Autoalan perustutkinto"))

  private def defaultOpintoOikeudet = {
    List((oppijat.eero.oid.get, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("1"))),
         (oppijat.eerola.oid.get, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("1"))),
         (oppijat.teija.oid.get, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("1"))),
         (oppijat.markkanen.oid.get, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("3"))))
  }

  def resetFixtures(database: TorDatabase): Unit = {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")
    await(database.db.run(DBIO.seq(
      OpintoOikeudet.delete,
      OpintoOikeudet ++= defaultOpintoOikeudet.map{case (oid, oikeus) => new OpintoOikeusRow(oid, oikeus)}
    )))
  }
}