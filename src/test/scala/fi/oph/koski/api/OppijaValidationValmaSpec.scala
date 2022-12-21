package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.{ExampleData, ExamplesValma}
import fi.oph.koski.documentation.ExamplesValma.valmaKoulutuksenSuoritus
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class OppijaValidationValmaSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with KoskiHttpSpec {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    valmaKoulutuksenSuoritus.copy(koulutusmoduuli = valmaKoulutuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
  override def tag: TypeTag[AmmatillinenOpiskeluoikeus] = implicitly[TypeTag[AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: AmmatillinenOpiskeluoikeus = ExamplesValma.valmaOpiskeluoikeus

  "Opiskeluoikeuden valmistumistilan alkupäivämäärä voi olla päiväys 31.5.2023 tai aikaisemmin vaikka peruste ei ole voimassa" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2021, 10, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2023, 5, 31), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      suoritukset = List(valmaKoulutuksenSuoritus.copy(
        koulutusmoduuli = ValmaKoulutus(laajuus = Some(LaajuusOsaamispisteissä(65)), perusteenDiaarinumero = Some("OOO-2658-2017"))
      ))
    )
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatusOk()
    }
  }

  "Opiskeluoikeuden tilan alkupäivämäärä ei voi olla päiväys 31.5.2023 jälkeen" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2023, 6, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      suoritukset = List(valmaKoulutuksenSuoritus.copy(
        koulutusmoduuli = ValmaKoulutus(laajuus = Some(LaajuusOsaamispisteissä(65)), perusteenDiaarinumero = Some("OOO-2658-2017"))
      ))
    )
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmaTilaEiSallittu())
    }
  }
}
