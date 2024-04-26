package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema._
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.{JArray, JField, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatra.test.ClientResponse

import java.time.LocalDate
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

    val mitätöityJaMuutettuOpiskeluoikeus = mitätöityOpiskeluoikeus(oo, date(2022, 5, 31)).copy(
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

  "Mitätöinnin voi merkitä samalle päivälle" in {
    resetFixtures()
    val oid = putOpiskeluoikeus(defaultOpiskeluoikeus) {
      verifyResponseStatusOk()
      opiskeluoikeusOid(response)
    }
    val mitätöinti = mitätöityOpiskeluoikeus(defaultOpiskeluoikeus, LocalDate.now()).copy(oid = Some(oid))
    putOpiskeluoikeus(mitätöinti) {
      verifyResponseStatusOk()
    }
  }

  "Mitätöintiä ei voi merkitä tulevaisuuteen" in {
    resetFixtures()
    val oid = putOpiskeluoikeus(defaultOpiskeluoikeus) {
      verifyResponseStatusOk()
      opiskeluoikeusOid(response)
    }
    val mitätöinti = mitätöityOpiskeluoikeus(defaultOpiskeluoikeus, LocalDate.now().plusDays(1)).copy(oid = Some(oid))
    putOpiskeluoikeus(mitätöinti) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.mitätöintiTulevaisuudessa())
    }
  }

  private def mitätöityOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus, pvm: LocalDate): VapaanSivistystyönOpiskeluoikeus = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(
        oo.tila.opiskeluoikeusjaksot ++
          List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(pvm, opiskeluoikeusMitätöity)
          )
      )
    )
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOPS

  private def opiskeluoikeusOid(response: ClientResponse): String = {
    (for {
      JObject(body) <- JsonMethods.parse(response.body)
      JField("opiskeluoikeudet", opiskeluoikeudet) <- body
      JArray(List(opiskeluoikeus)) <- opiskeluoikeudet
      JObject(opiskeluoikeusObj) <- opiskeluoikeus
      JField("oid", oid) <- opiskeluoikeusObj
    } yield JsonSerializer.extract[String](oid)).head
  }
}
