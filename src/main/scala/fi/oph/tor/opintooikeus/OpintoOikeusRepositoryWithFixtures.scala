package fi.oph.tor.opintooikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.oppija.MockOppijaRepository
import fi.oph.tor.oppilaitos.{Oppilaitos}
import fi.oph.tor.tutkinto.Tutkinto
import slick.dbio.DBIO

class OpintoOikeusRepositoryWithFixtures(db: DB) extends PostgresOpintoOikeusRepository(db) {
  private val oppijat = new MockOppijaRepository
  private val autoalanPerustutkinto: Tutkinto = Tutkinto("39/011/2014", "351301", Some("Autoalan perustutkinto"))

  private def defaultOpintoOikeudet = {
    List((oppijat.eero.oid, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("1"))),
         (oppijat.eerola.oid, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("1"))),
         (oppijat.teija.oid, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("1"))),
         (oppijat.markkanen.oid, OpintoOikeus(autoalanPerustutkinto, Oppilaitos("3"))))
  }

  override def resetFixtures: Unit = {
    await(db.run(DBIO.seq(
      OpintoOikeudet.delete,
      OpintoOikeudet ++= defaultOpintoOikeudet.map{case (oid, oikeus) => new OpintoOikeusRow(oid, oikeus)}
    )))
  }
}
