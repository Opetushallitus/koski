package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OppilaitosServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(koskiSession).toList
  }
  get("/opiskeluoikeustyypit/:oid") {
    val oppilaitostyypit: List[String] = application.organisaatioRepository.getOrganisaatioHierarkia(params("oid")).toList.flatMap(_.oppilaitostyyppi)
    oppilaitostyypit.flatMap {
      case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut, perusJaLukioasteenKoulut).contains(tyyppi) => List("perusopetus")
      case tyyppi if List(ammatillisetOppilaitokset, ammatillisetErityisoppilaitokset, ammatillisetErikoisoppilaitokset, ammatillisetAikuiskoulutusKeskukset).contains(tyyppi) => List("ammatillinenkoulutus")
      case _ => List("ammatillinenkoulutus", "perusopetus")
    }.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViite("opiskeluoikeudentyyppi", _))
  }
}