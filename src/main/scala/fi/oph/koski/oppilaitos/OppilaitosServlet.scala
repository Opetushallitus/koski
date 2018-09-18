package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OppilaitosServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(koskiSession).toList
  }

  val perusopetuksenTyypit = List(OpiskeluoikeudenTyyppi.perusopetus, OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus, OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus, OpiskeluoikeudenTyyppi.aikuistenperusopetus)
  val esiopetuksenTyypit = List(OpiskeluoikeudenTyyppi.esiopetus)
  val ammatillisenTyypit = List(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
  val lukionTyypit = List(OpiskeluoikeudenTyyppi.lukiokoulutus, OpiskeluoikeudenTyyppi.ibtutkinto)

  get("/opiskeluoikeustyypit/:oid") {
    val oppilaitostyypit: List[String] = application.organisaatioRepository.getOrganisaatioHierarkia(params("oid")).toList.flatMap(_.oppilaitostyyppi)
    oppilaitostyypit.flatMap {
      case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut).contains(tyyppi) => perusopetuksenTyypit ++ esiopetuksenTyypit
      case tyyppi if List(ammatillisetOppilaitokset, ammatillisetErityisoppilaitokset, ammatillisetErikoisoppilaitokset, ammatillisetAikuiskoulutusKeskukset).contains(tyyppi) => perusopetuksenTyypit ++ ammatillisenTyypit
      case tyyppi if List(lukio).contains(tyyppi) => perusopetuksenTyypit ++ lukionTyypit
      case tyyppi if List(perusJaLukioasteenKoulut).contains(tyyppi) => perusopetuksenTyypit ++ esiopetuksenTyypit ++ lukionTyypit
      case _ => perusopetuksenTyypit ++ ammatillisenTyypit
    }.map(_.koodiarvo).flatMap(application.koodistoViitePalvelu.validate("opiskeluoikeudentyyppi", _))
  }
}
