package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{Hidden, KoodistoUri}
import fi.oph.koski.util.{Timing, WithWarnings}
import fi.oph.scalaschema.{ClassSchema, ExtractionContext}
import fi.oph.koski.util.DateOrdering.{localDateOptionOrdering, localDateOrdering}

object OppijaEditorModel extends Timing {

  val opiskeluoikeusOrdering: Ordering[Opiskeluoikeus] = Ordering.by {
    o: Opiskeluoikeus => o match {
      case k: KorkeakoulunOpiskeluoikeus => (korkeakoulunOpiskeluoikeuksienJärjestysKriteeri(k), k.alkamispäivä)
      case _ => (0, o.alkamispäivä)
    }
  } (Ordering.Tuple2(Ordering.Int, localDateOptionOrdering.reverse))

  def korkeakoulunOpiskeluoikeuksienJärjestysKriteeri(oo: KorkeakoulunOpiskeluoikeus): Int = {
    val suoritus = oo.suoritukset.headOption
    val aktiivinenTaiOptio = oo.tila.opiskeluoikeusjaksot.lastOption.exists(!_.opiskeluoikeusPäättynyt)
    val ensisijainen = oo.lisätiedot.exists(_.ensisijaisuusVoimassa(LocalDate.now))
    (suoritus, aktiivinenTaiOptio, ensisijainen) match {
      case (Some(s: KorkeakoulututkinnonSuoritus), _, _) if s.valmis => 1 // valmis tutkinto
      case (Some(s: KorkeakouluSuoritus), _, _) if s.valmis => 2 // valmis muu opiskeluoikeus
      case _ if oo.synteettinen => 3// ei-opiskeluoikeuteen liitetyt kurssit ("orphanages")
      case (_, true, true) => 4 // aktiivinen/optio opiskeluoikeus, ensisijainen
      case (_, true, _) => 5 // aktiivinen/optio opiskeluoikeus, ei ensisijainen
      case (_, _, true) => 6 // passivoitu/luopunut, ensisijainen
      case _ => 7 // passivoitu/luopunut, ei ensisijainen
    }
  }

  val oppilaitoksenOpiskeluoikeudetOrdering: Ordering[OppilaitoksenOpiskeluoikeudet] =
    Ordering.by { oo: OppilaitoksenOpiskeluoikeudet => oo.latestAlkamispäiväForOrdering }(localDateOptionOrdering).reverse

  val opiskeluoikeudetTyypeittäinOrdering: Ordering[OpiskeluoikeudetTyypeittäin] =
    Ordering.by { ot: OpiskeluoikeudetTyypeittäin => ot.latestAlkamispäiväForOrdering }(localDateOptionOrdering).reverse

  // Note: even with editable=true, editability will be checked based on organizational access on the lower level
  def toEditorModel(oppijaWithWarnings: WithWarnings[Oppija], editable: Boolean)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = timed("createModel") {
    val oppija = oppijaWithWarnings.getIgnoringWarnings
    val tyypit = oppija.opiskeluoikeudet.groupBy(oo => application.koodistoViitePalvelu.validateRequired(oo.tyyppi)).map {
      case (tyyppi, opiskeluoikeudet) =>
        val oppilaitokset = opiskeluoikeudet.groupBy(oo => oo.getOppilaitosOrKoulutusToimija).map {
          case (oppilaitos, opiskeluoikeudet) => toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
        }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering)
        OpiskeluoikeudetTyypeittäin(tyyppi, oppilaitokset)
    }.toList.sorted(opiskeluoikeudetTyypeittäinOrdering)
    buildModel(OppijaEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], tyypit, oppijaWithWarnings.warnings.flatMap(_.errors).map(_.key).toList), editable)
  }

  def buildModel(obj: AnyRef, editable: Boolean)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }

  def toOppilaitoksenOpiskeluoikeus(oppilaitos: OrganisaatioWithOid, opiskeluoikeudet: Seq[Opiskeluoikeus]) = {
    OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sorted(opiskeluoikeusOrdering).map {
      case oo: AikuistenPerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(aikuistenPerusopetuksenSuoritustenJärjestysKriteeri))
      case oo: PerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(perusopetuksenSuoritustenJärjestysKriteeri))
      case oo: AmmatillinenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(ammatillisenSuoritustenJärjestysKriteeri))
      case oo: IBOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(ibSuoritustenJärjestysKriteeri))
      case oo: DIAOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(diaSuoritustenJärjestysKritteri))
      case oo: InternationalSchoolOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(internationalSchoolJärjestysKriteeri))
      case oo: Any => oo
    })
  }

  def aikuistenPerusopetuksenSuoritustenJärjestysKriteeri(s: AikuistenPerusopetuksenPäätasonSuoritus) = {
    val primary = s match {
      case s: AikuistenPerusopetuksenOppimääränSuoritus => 0
      case s: AikuistenPerusopetuksenAlkuvaiheenSuoritus => 1
      case s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus => 2
      case _ => 100
    }
    val secondary = s.valmis
    (primary, secondary)
  }

  def perusopetuksenSuoritustenJärjestysKriteeri(s: PerusopetuksenPäätasonSuoritus) = {
    val primary = s match {
      case s: PerusopetuksenOppimääränSuoritus => -100 // ensin oppimäärän suoritus
      case s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus => 0 // oppiaineiden oppimäärien suoritukset
      case s: PerusopetuksenVuosiluokanSuoritus => - s.koulutusmoduuli.tunniste.koodiarvo.toInt // sitten luokka-asteet
      case _ => 100
    }
    val secondary = s.valmis
    (primary, secondary)
  }

  def ammatillisenSuoritustenJärjestysKriteeri(s: AmmatillinenPäätasonSuoritus): Int = {
    s.alkamispäivä.map(a => -a.toEpochDay.toInt).getOrElse(0)
  }

  def ibSuoritustenJärjestysKriteeri(s : IBPäätasonSuoritus): Int = {
    s match {
      case _: IBTutkinnonSuoritus => -1
      case _: PreIBSuoritus => 0
      case _ => 1
    }
  }

  def diaSuoritustenJärjestysKritteri(s : DIAPäätasonSuoritus): Int = {
    s match {
      case _: DIATutkinnonSuoritus => -1
      case _: DIAValmistavanVaiheenSuoritus => 0
      case _ => 1
    }
  }

  def internationalSchoolJärjestysKriteeri(s: InternationalSchoolVuosiluokanSuoritus): Int = if (s.koulutusmoduuli.tunniste.koodiarvo == "explorer") {
    0
  } else {
    0 - s.koulutusmoduuli.tunniste.koodiarvo.toInt
  }
}

object EditorSchema {
  lazy val schema = KoskiSchema.schemaFactory.createSchema(classOf[OppijaEditorView].getName).asInstanceOf[ClassSchema].moveDefinitionsToTopLevel
  lazy val deserializationContext = ExtractionContext(KoskiSchema.schemaFactory, validate = false)
}

case class OppijaEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OpiskeluoikeudetTyypeittäin],
  @Hidden
  varoitukset: List[String]
)

case class OpiskeluoikeudetTyypeittäin(@KoodistoUri("opiskeluoikeudentyyppi") tyyppi: Koodistokoodiviite, opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]) {
  lazy val latestAlkamispäiväForOrdering: Option[LocalDate] =
    Some(opiskeluoikeudet.collect { case o if o.latestAlkamispäiväForOrdering.nonEmpty => o.latestAlkamispäiväForOrdering.get }).filter(_.nonEmpty).map(_.max(localDateOrdering))

}
case class OppilaitoksenOpiskeluoikeudet(oppilaitos: OrganisaatioWithOid, opiskeluoikeudet: List[Opiskeluoikeus]) {
  lazy val latestAlkamispäiväForOrdering: Option[LocalDate] =
    Some(opiskeluoikeudet.collect {
      case o if o.alkamispäivä.nonEmpty => o.alkamispäivä.get
      case o: YlioppilastutkinnonOpiskeluoikeus if o.suoritukset.headOption.exists(_.vahvistus.nonEmpty) => o.suoritukset.head.vahvistus.get.päivä
    }).filter(_.nonEmpty).map(_.max(localDateOrdering))
}
