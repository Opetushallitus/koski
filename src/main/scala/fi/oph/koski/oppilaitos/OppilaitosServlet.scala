package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OppilaitosServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/") {
    val oppilaitokset: Iterable[OidOrganisaatio] = application.oppilaitosRepository.oppilaitokset(koskiSession)
    oppilaitokset.toList
  }
  get("/opiskeluoikeustyypit/:oid") {
    val oppilaitostyypit: List[String] = application.organisaatioRepository.getOrganisaatioHierarkia(params("oid")).toList.flatMap(_.oppilaitostyyppi)
    println(oppilaitostyypit)
    oppilaitostyypit.flatMap {
      case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut).contains(tyyppi) => List("perusopetus")
      case tyyppi if List(perusJaLukioasteenKoulut).contains(tyyppi) => List("perusopetus", "lukiokoulutus")
      case _ => List("ammatillinenkoulutus")
    }
  }
}