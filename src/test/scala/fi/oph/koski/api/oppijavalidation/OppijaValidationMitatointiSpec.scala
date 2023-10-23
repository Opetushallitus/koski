package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema._
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationMitätöintiSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Mitätöitäessä tehdään validaatioiden osana ajettavat tietojen täydennykset ja skipataan tarvittaessa turhat validoinnit" in {
    val oo = defaultOpiskeluoikeus.copy(
      suoritukset = List(suoritusKOPS)
    )

    val validoitumatonMaksuttomuusTieto =
      List(Maksuttomuus(date(2020, 12, 31), None, true))

    val opiskeluoikeusOid = setupOppijaWithAndGetOpiskeluoikeus(oo).oid.get

    val mitätöityJaMuutettuOpiskeluoikeus = mitätöityOpiskeluoikeus(oo).copy(
      oid = Some(opiskeluoikeusOid),
      oppilaitos = None,
      koulutustoimija = None,
      lisätiedot = Some(VapaanSivistystyönOpiskeluoikeudenLisätiedot(
        maksuttomuus = Some(validoitumatonMaksuttomuusTieto),
        oikeuttaMaksuttomuuteenPidennetty = None
      ))
    )

    putOpiskeluoikeus(mitätöityJaMuutettuOpiskeluoikeus) {
      verifyResponseStatusOk()
    }

    val mitätöityOoTietokannasta =
      KoskiApplicationForTests.opiskeluoikeusRepository
        .findByOid(opiskeluoikeusOid)(KoskiSpecificSession.systemUserMitätöidytJaPoistetut)
        .map(_.toOpiskeluoikeusUnsafe(KoskiSpecificSession.systemUserMitätöidytJaPoistetut))
        .getOrElse(throw new InternalError("Opiskeluoikeutta ei löytynyt"))

    mitätöityOoTietokannasta.mitätöity should be(true)
    mitätöityOoTietokannasta.oppilaitos.map(_.oid) should be(Some(varsinaisSuomenKansanopisto.oid))
    mitätöityOoTietokannasta.koulutustoimija.map(_.oid) should be(Some(varsinaisSuomenAikuiskoulutussäätiö.oid))
    mitätöityOoTietokannasta.suoritukset(0).osasuoritusLista(0).koulutusmoduuli.getLaajuus.map(_.arvo) should be(Some(4.0))
  }

  private def mitätöityOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus): VapaanSivistystyönOpiskeluoikeus = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(
        oo.tila.opiskeluoikeusjaksot ++
          List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusMitätöity)
          )
      )
    )
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOPS
}
