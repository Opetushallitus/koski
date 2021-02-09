package fi.oph.koski.valpas.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.fixture.DatabaseFixtureCreator
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö, VerifiedHenkilöOid}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.{OpiskeluoikeudenOsittaisetTiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema._
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import slick.dbio.DBIO

class ValpasDatabaseFixtureCreator(application: KoskiApplication) extends DatabaseFixtureCreator(application, "opiskeluoikeus_valpas_fixture", "opiskeluoikeushistoria_valpas_fixture") {
  protected def oppijat = ValpasMockOppijat.defaultOppijat

  protected lazy val validatedOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    defaultOpiskeluOikeudet.zipWithIndex.map { case ((henkilö, oikeus), index) =>
      timed(s"Validating fixture ${index}", 500) {
        validator.validateAsJson(Oppija(henkilö.toHenkilötiedotJaOid, List(oikeus))) match {
          case Right(oppija) => (henkilö, oppija.tallennettavatOpiskeluoikeudet.head)
          case Left(status) => throw new RuntimeException(
            s"Fixture insert failed for ${henkilö.etunimet} ${henkilö.sukunimi} with data ${JsonSerializer.write(oikeus)}: ${status}"
          )
        }
      }
    }
  }

  protected lazy val invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List()

  private def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List(
    (ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021, ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus)
  )
}
