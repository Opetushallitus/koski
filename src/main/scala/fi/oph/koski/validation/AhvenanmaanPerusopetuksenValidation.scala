package fi.oph.koski.validation

import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.schema._

object AhvenanmaanPerusopetuksenValidation {
  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = oo match {
    case ahvenanmaanOo: AhvenanmaanPerusopetuksenOpiskeluoikeus =>
      HttpStatus.fold(validateOppilaitos(ahvenanmaanOo) :: validateAlkuvaihe(ahvenanmaanOo) :: ahvenanmaanOo.suoritukset.map {
        case oppimäärä: AhvenanmaanPerusopetuksenOppimääränSuoritus if oppimäärä.vahvistettu =>
          validateYsiluokanSuoritusTaiSitäEiTarvita(ahvenanmaanOo, oppimäärä)
        case _ => HttpStatus.ok
      })
    case mannerSuomenOo if korvattavatMannerSuomenTyypit.contains(mannerSuomenOo.tyyppi.koodiarvo) =>
      HttpStatus.validate(!mannerSuomenOo.oppilaitos.exists(AhvenanmaanKunnat.onAhvenanmaalainenKunta))(
        KoskiErrorCategory.badRequest.validation.organisaatio.ahvenanmaalainenOppilaitos()
      )
    case _ => HttpStatus.ok
  }

  // Samat tyypit, jotka OppilaitosServlet korvaa luontidialogissa Ahvenanmaan perusopetuksella.
  // Perusopetukseen valmistava opetus jää sallituksi, koska dialogi tarjoaa sitä esiopetuksen rinnalla.
  private val korvattavatMannerSuomenTyypit = Set(
    OpiskeluoikeudenTyyppi.perusopetus,
    OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus,
    OpiskeluoikeudenTyyppi.aikuistenperusopetus
  ).map(_.koodiarvo)

  // Kotipaikka on ainoa organisaatiodatasta löytyvä tunnusmerkki ahvenanmaalaiselle oppilaitokselle,
  // ks. OppilaitosServlet, joka tarjoaa tyyppiä luontidialogissa samalla perusteella.
  private def validateOppilaitos(oo: AhvenanmaanPerusopetuksenOpiskeluoikeus): HttpStatus =
    HttpStatus.validate(oo.oppilaitos.exists(AhvenanmaanKunnat.onAhvenanmaalainenKunta))(
      KoskiErrorCategory.badRequest.validation.organisaatio.eiAhvenanmaalainenOppilaitos()
    )

  // Alkuvaihe (Inledningsskedet) kuuluu vain muiden kuin oppivelvollisten opintoihin;
  // oppivelvollisten opinnot kirjataan vuosiluokkina.
  private def validateAlkuvaihe(oo: AhvenanmaanPerusopetuksenOpiskeluoikeus): HttpStatus =
    HttpStatus.validate(
      oo.lisätiedot.flatMap(_.alkuvaihe).isEmpty ||
        oo.suoritukset.exists(_.isInstanceOf[AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus])
    )(
      KoskiErrorCategory.badRequest.validation.rakenne.ahvenanmaanAlkuvaiheVainMuilleKuinOppivelvollisille()
    )

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
