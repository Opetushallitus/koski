package fi.oph.koski.validation

import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object AhvenanmaanPerusopetuksenValidation {
  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = oo match {
    case ahvenanmaanOo: AhvenanmaanPerusopetuksenOpiskeluoikeus =>
      HttpStatus.fold(ahvenanmaanOo.suoritukset.map {
        case oppimäärä: AhvenanmaanPerusopetuksenOppimääränSuoritus if oppimäärä.vahvistettu =>
          validateYsiluokanSuoritusTaiSitäEiTarvita(ahvenanmaanOo, oppimäärä)
        case _ => HttpStatus.ok
      })
    case _ => HttpStatus.ok
  }

  // Koskee vain oppivelvollisia: muiden kuin oppivelvollisten oppimäärän suoritus
  // (AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus) on eri luokka, eikä sen
  // opiskeluoikeudella saa muutenkaan olla vuosiluokan suorituksia.
  private def validateYsiluokanSuoritusTaiSitäEiTarvita(
    oo: AhvenanmaanPerusopetuksenOpiskeluoikeus,
    oppimäärä: AhvenanmaanPerusopetuksenOppimääränSuoritus
  ): HttpStatus = {
    val vahvistettuYsiluokanSuoritusOlemassa = oo.suoritukset.exists {
      case vuosiluokka: AhvenanmaanPerusopetuksenVuosiluokanSuoritus =>
        vuosiluokka.koulutusmoduuli.tunniste.koodiarvo == "9" && vuosiluokka.vahvistettu
      case _: Any => false
    }

    val kotiopetusVoimassaPäättötodistuksenVahvistuspäivänä =
      oppimäärä.vahvistus.exists(vahvistus => oo.kotiopetuksessa(vahvistus.päivä))

    val erityinenTutkinto = oppimäärä.suoritustapa == suoritustapaErityinenTutkinto

    HttpStatus.validate(
      vahvistettuYsiluokanSuoritusOlemassa ||
        kotiopetusVoimassaPäättötodistuksenVahvistuspäivänä ||
        erityinenTutkinto
    )(
      KoskiErrorCategory.badRequest.validation.tila.ahvenanmaanPerusopetuksenOppimääräIlmanYsiluokanSuoritusta()
    )
  }
}
