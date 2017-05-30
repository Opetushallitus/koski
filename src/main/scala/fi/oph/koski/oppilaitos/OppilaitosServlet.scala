package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OppilaitosServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(koskiSession).toList
  }

  val perusopetuksenTyypit = List("perusopetus", "perusopetukseenvalmistavaopetus", "perusopetuksenlisaopetus")
  val ammatillisenTyypit = List("ammatillinenkoulutus")

  get("/opiskeluoikeustyypit/:oid") {
    val oppilaitostyypit: List[String] = application.organisaatioRepository.getOrganisaatioHierarkia(params("oid")).toList.flatMap(_.oppilaitostyyppi)
    oppilaitostyypit.flatMap {
      case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut, perusJaLukioasteenKoulut).contains(tyyppi) => perusopetuksenTyypit
      case tyyppi if List(ammatillisetOppilaitokset, ammatillisetErityisoppilaitokset, ammatillisetErikoisoppilaitokset, ammatillisetAikuiskoulutusKeskukset).contains(tyyppi) => perusopetuksenTyypit ++ ammatillisenTyypit
      case _ => perusopetuksenTyypit ++ ammatillisenTyypit
    }.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViite("opiskeluoikeudentyyppi", _))
  }
}