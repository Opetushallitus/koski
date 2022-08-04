package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.documentation.ExamplesValma
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

  "Opiskeluoikeuden tilan alkupäivämäärä ei voi olla päiväys 31.8.2022 jälkeen" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2022, 9, 1), opiskeluoikeusLäsnä)
      ))
    )
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmaTilaEiSallittu())
    }
  }
}
