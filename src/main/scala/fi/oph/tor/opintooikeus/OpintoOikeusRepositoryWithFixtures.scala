package fi.oph.tor.opintooikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.oppija.MockOppijaRepository
import slick.dbio.DBIO

class OpintoOikeusRepositoryWithFixtures(db: DB) extends PostgresOpintoOikeusRepository(db) {
  private val oppijat = new MockOppijaRepository

  private def defaultOpintoOikeudet = List(
    OpintoOikeus(oppijaOid = oppijat.eero.oid, ePerusteetDiaarinumero = "39/011/2014", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.eerola.oid, ePerusteetDiaarinumero = "39/011/2014", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.teija.oid, ePerusteetDiaarinumero = "39/011/2014", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.markkanen.oid, ePerusteetDiaarinumero = "39/011/2014", oppilaitosOrganisaatio =  "3")
  )

  override def resetFixtures: Unit = {
    await(db.run(DBIO.seq(
      OpintoOikeudet.delete,
      OpintoOikeudet ++= defaultOpintoOikeudet.map(new OpintoOikeusRow(_))
    )))
  }
}
