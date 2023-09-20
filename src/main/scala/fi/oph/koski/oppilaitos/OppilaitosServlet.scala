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
  // TODO: TOR-2052 - EB-tutkinto
  val eshTyypit = List(OpiskeluoikeudenTyyppi.europeanschoolofhelsinki)
  val muunKuinSäännellynKoulutuksenTyypit = List(OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus)
  val taiteenPerusopetuksenTyypit = List(OpiskeluoikeudenTyyppi.taiteenperusopetus)
  val pohjoiskalotinTyypit = List(OpiskeluoikeudenTyyppi.ammatillinenkoulutus, OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus)

  // Organisaatiopoikkeukset, jotka käydään ensin läpi. Esimerkiksi, jos on tarve näyttää vain yhdentyypinen opiskeluoikeus organisaatiolle, laita OID:t tähän listaan.
  val organisaatioPoikkeukset = Map(
    // Pohjoiskalotin koulutussäätiö
    // Tämä on toistaiseksi ainoa muuta kuin Muksia tallentava Ei tiedossa -tyyppinen oppilaitos. Tällä hetkellä pelkän
    // organisaatiodatan perusteella ei ole muuta tapaa tunnistaa muks-oppilaitoksia, eikä niillä ole erillistä
    // käyttöikeuttakaan.
    List("1.2.246.562.10.2013120211542064151791", "1.2.246.562.10.88417511545") -> pohjoiskalotinTyypit,
    // European School of Helsinki
    List("1.2.246.562.10.962346066210", "1.2.246.562.10.13349113236", "1.2.246.562.10.12798841685") -> eshTyypit
  )

  get("/opiskeluoikeustyypit/:oid") {
    val organisaatiot = application.organisaatioRepository.getOrganisaatioHierarkia(params("oid")).toList
    (byOrganisaatioPoikkeus(organisaatiot) match {
      case Nil => (byOppilaitosTyyppi(organisaatiot) ++ byOrganisaatioTyyppi(organisaatiot))
      case loytyneetPoikkeustyypit => loytyneetPoikkeustyypit
    }).distinct
      .flatMap(t => application.koodistoViitePalvelu.validate("opiskeluoikeudentyyppi", t.koodiarvo))
      .filter(t => session.allowedOpiskeluoikeusTyypit.contains(t.koodiarvo))
  }

  private def byOppilaitosTyyppi(organisaatiot: List[OrganisaatioHierarkia]) =
    organisaatiot.flatMap(_.oppilaitostyyppi).flatMap {
      case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut).contains(tyyppi) => perusopetuksenTyypit ++ esiopetuksenTyypit ++ tuvaTyypit
      case tyyppi if List(ammatillisetOppilaitokset, ammatillisetErityisoppilaitokset, ammatillisetErikoisoppilaitokset, ammatillisetAikuiskoulutusKeskukset).contains(tyyppi) => perusopetuksenTyypit ++ ammatillisenTyypit ++ tuvaTyypit
      case tyyppi if List(lukio).contains(tyyppi) => perusopetuksenTyypit ++ lukionTyypit ++ tuvaTyypit
      case tyyppi if List(perusJaLukioasteenKoulut).contains(tyyppi) => perusopetuksenTyypit ++ esiopetuksenTyypit ++ lukionTyypit ++ saksalaisenKoulunTyypit ++ internationalSchoolTyypit ++ tuvaTyypit
      case tyyppi if List(eiTiedossaOppilaitokset).contains(tyyppi) => List.empty
      case _ => perusopetuksenTyypit ++ ammatillisenTyypit ++ vapaanSivistysTyönTyypit ++ tuvaTyypit
    }

  private def byOrganisaatioTyyppi(organisaatiot: List[OrganisaatioHierarkia]) =
    if (organisaatiot.flatMap(_.organisaatiotyypit).contains(Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA)) {
      esiopetuksenTyypit ++ muunKuinSäännellynKoulutuksenTyypit ++ taiteenPerusopetuksenTyypit
    } else {
      muunKuinSäännellynKoulutuksenTyypit ++ taiteenPerusopetuksenTyypit
    }

  private def byOrganisaatioPoikkeus(organisaatiot: List[OrganisaatioHierarkia]): List[Koodistokoodiviite] =
    organisaatioPoikkeukset.filter(_._1.exists(poikkeusOid => organisaatiot.map(_.oid).contains(poikkeusOid))).flatMap(_._2).toList

}
