package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, Organisaatiotyyppi}
import fi.oph.koski.schema.{Koodistokoodiviite, OpiskeluoikeudenTyyppi}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class OppilaitosServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(session).toList
  }

  val perusopetuksenTyypit = List(OpiskeluoikeudenTyyppi.perusopetus, OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus, OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus, OpiskeluoikeudenTyyppi.aikuistenperusopetus)
  val esiopetuksenTyypit = List(OpiskeluoikeudenTyyppi.esiopetus, OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus)
  val ammatillisenTyypit = List(OpiskeluoikeudenTyyppi.ammatillinenkoulutus)
  val lukionTyypit = List(OpiskeluoikeudenTyyppi.lukiokoulutus, OpiskeluoikeudenTyyppi.ibtutkinto, OpiskeluoikeudenTyyppi.luva)
  val saksalaisenKoulunTyypit = List(OpiskeluoikeudenTyyppi.diatutkinto)
  val internationalSchoolTyypit = List(OpiskeluoikeudenTyyppi.internationalschool)
  val vapaanSivistysTyönTyypit = List(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus)
  val tuvaTyypit = List(OpiskeluoikeudenTyyppi.tuva)

  get("/opiskeluoikeustyypit/:oid") {
    val organisaatiot = application.organisaatioRepository.getOrganisaatioHierarkia(params("oid")).toList
    (byOppilaitosTyyppi(organisaatiot) ++ byOrganisaatioTyyppi(organisaatiot))
      .distinct
      .flatMap(t => application.koodistoViitePalvelu.validate("opiskeluoikeudentyyppi", t.koodiarvo))
      .filter(t => session.allowedOpiskeluoikeusTyypit.contains(t.koodiarvo))
  }

  private def byOppilaitosTyyppi(organisaatiot: List[OrganisaatioHierarkia]) =
    organisaatiot.flatMap(_.oppilaitostyyppi).flatMap {
      case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut).contains(tyyppi) => perusopetuksenTyypit ++ esiopetuksenTyypit ++ tuvaTyypit
      case tyyppi if List(ammatillisetOppilaitokset, ammatillisetErityisoppilaitokset, ammatillisetErikoisoppilaitokset, ammatillisetAikuiskoulutusKeskukset).contains(tyyppi) => perusopetuksenTyypit ++ ammatillisenTyypit ++ tuvaTyypit
      case tyyppi if List(lukio).contains(tyyppi) => perusopetuksenTyypit ++ lukionTyypit ++ tuvaTyypit
      case tyyppi if List(perusJaLukioasteenKoulut).contains(tyyppi) => perusopetuksenTyypit ++ esiopetuksenTyypit ++ lukionTyypit ++ saksalaisenKoulunTyypit ++ internationalSchoolTyypit ++ tuvaTyypit
      case _ => perusopetuksenTyypit ++ ammatillisenTyypit ++ vapaanSivistysTyönTyypit ++ tuvaTyypit
    }

  private def byOrganisaatioTyyppi(organisaatiot: List[OrganisaatioHierarkia]) =
    if (organisaatiot.flatMap(_.organisaatiotyypit).contains(Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA)) {
      esiopetuksenTyypit
    } else {
      Nil
    }

}
