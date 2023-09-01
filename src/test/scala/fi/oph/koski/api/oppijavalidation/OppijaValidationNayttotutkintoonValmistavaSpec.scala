package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillinenOpiskeluoikeusNäyttötutkinnonJaNäyttöönValmistavanSuorituksilla, ammatillisetTutkinnonOsat, hyväksytty, tutkinnonOsanSuoritus}
import fi.oph.koski.documentation.AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.{AmmattitutkintoExample, ExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

class OppijaValidationNayttotutkintoonValmistavaSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  override def defaultOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeus.copy(suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus))

  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): AmmatillinenOpiskeluoikeus =
    AmmattitutkintoExample.opiskeluoikeus.copy(suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(tutkinto = AmmattitutkintoExample.tutkinto.copy(perusteenDiaarinumero = diaari))))

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
  override def vääräntyyppisenPerusteenId: Long = 1372910

  "Voi merkitä valmiiksi vaikka ei sisällä osasuorituksia" in {
     putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(osasuoritukset = None)))) {
       verifyResponseStatusOk()
     }
  }

  "Opiskeluoikeus voi päättyä tilaan 'Katsotaan eronneeksi' vaikka suoritus on vahvistettu" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), opiskeluoikeusKatsotaanEronneeksi, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(osasuoritukset = None)))) {
      verifyResponseStatusOk()
    }
  }

  "Opiskeluoikeus ei voi päättyä tilaan 'Katsotaan eronneeksi' kun löytyy valmistavan suorituksen lisäksi vahvistettu koko tutkinnon suoritus" in {
    val oo = ammatillinenOpiskeluoikeusNäyttötutkinnonJaNäyttöönValmistavanSuorituksilla(
      vahvistus = ExampleData.vahvistus(),
      tutkinnonOsasuoritukset = Some(List(tutkinnonOsanSuoritus("100832", "Kasvun tukeminen ja ohjaus", ammatillisetTutkinnonOsat, hyväksytty)))
    ).copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 8, 1), opiskeluoikeusKatsotaanEronneeksi, Some(ExampleData.valtionosuusRahoitteinen))
      )))

    putOpiskeluoikeus(oo) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
    }
  }

  "Ei voi siirtää opiskeluoikeutta jos tutkinnon peruste ei ole voimassa (eikä siirtymäaikaa ole jäljellä) opiskeluoikeuden päättymisen päivämäärällä" in {
    val oo = ammatillinenOpiskeluoikeusNäyttötutkinnonJaNäyttöönValmistavanSuorituksilla(
      vahvistus = ExampleData.vahvistus(),
      tutkinnonOsasuoritukset = Some(List(tutkinnonOsanSuoritus("100832", "Kasvun tukeminen ja ohjaus", ammatillisetTutkinnonOsat, hyväksytty)))
    ).copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(date(2066, 5, 13), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
      )))

    putOpiskeluoikeus(oo) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa())
    }
  }
}
