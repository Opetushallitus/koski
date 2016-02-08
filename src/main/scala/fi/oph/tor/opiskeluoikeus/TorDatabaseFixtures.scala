package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db._
import fi.oph.tor.oppija.{OppijaRepository, MockOppijaRepository, MockOppijat, VerifiedOppijaOid}
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._
import fi.oph.tor.tor.TorValidator
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.util.Timing
import slick.dbio.DBIO

class TorDatabaseFixtureCreator(database: TorDatabase, repository: OpiskeluOikeusRepository, oppijaRepository: OppijaRepository, validator: TorValidator) extends Futures with GlobalExecutionContext with Timing {
  def resetFixtures: Unit = timed("resetFixtures", 10) {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")
    implicit val user = MockUsers.kalle.asTorUser

    val oppijat: List[FullHenkilö] = oppijaRepository.findOppijat("")
    val deleteOpiskeluOikeudet = oppijat.map{oppija => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oppija.oid).delete}

    await(database.db.run(DBIO.sequence(deleteOpiskeluOikeudet)))

    defaultOpiskeluOikeudet.foreach { case (oid, oikeus) =>
      validator.validateAsJson(TorOppija(OidHenkilö(oid), List(oikeus))) match {
        case Right(oppija) => repository.createOrUpdate(VerifiedOppijaOid(oid), oppija.opiskeluoikeudet(0))
      }
    }
  }

  private def defaultOpiskeluOikeudet = {
    List((MockOppijat.eero.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.eerola.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.teija.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto)),
      (MockOppijat.markkanen.oid, OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.omnomnia)))
  }
}

object OpiskeluOikeusTestData {
  def opiskeluOikeus(oppilaitosId: String, koulutusKoodi: Int = 351301): OpiskeluOikeus = {
    val oppilaitos: Oppilaitos = Oppilaitos(oppilaitosId, None, None)
    val koulutusKoodiViite = KoodistoKoodiViite(koulutusKoodi.toString, None, "koulutus", None)

    OpiskeluOikeus(
      None,
      None,
      None,
      None,
      None,
      None,
      oppilaitos,
      Suoritus(
        None,
        TutkintoKoulutustoteutus(TutkintoKoulutus(koulutusKoodiViite, Some("39/011/2014")), None, None, None, None),
        None,
        KoodistoKoodiViite("KESKEN", "suorituksentila"),
        None,
        oppilaitos,
        None,
        None,
        None
      ),
      hojks = None,
      None,
      None,
      None
    )
  }
}