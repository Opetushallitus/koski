package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{Hidden, KoodistoUri}
import fi.oph.koski.util.Timing
import fi.oph.scalaschema.{ClassSchema, ExtractionContext}
import fi.oph.koski.date.DateOrdering.{localDateOrdering,localDateOptionOrdering}

object OppijaEditorModel extends Timing {
  val opiskeluoikeusOrdering: Ordering[Opiskeluoikeus] =
    Ordering.by { o: Opiskeluoikeus => o.alkamispäivä }(localDateOptionOrdering)

  val oppilaitoksenOpiskeluoikeudetOrdering: Ordering[OppilaitoksenOpiskeluoikeudet] =
    Ordering.by { oo: OppilaitoksenOpiskeluoikeudet => oo.latestAlkamispäiväForOrdering }(localDateOptionOrdering).reverse

  val opiskeluoikeudetTyypeittäinOrdering: Ordering[OpiskeluoikeudetTyypeittäin] =
    Ordering.by { ot: OpiskeluoikeudetTyypeittäin => ot.latestAlkamispäiväForOrdering }(localDateOptionOrdering).reverse

  // Note: even with editable=true, editability will be checked based on organizational access on the lower level
  def toEditorModel(oppija: Oppija, editable: Boolean)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = timed("createModel") {
    val tyypit = oppija.opiskeluoikeudet.groupBy(oo => application.koodistoViitePalvelu.validateRequired(oo.tyyppi)).map {
      case (tyyppi, opiskeluoikeudet) =>
        val oppilaitokset = opiskeluoikeudet.groupBy(oo => oo.getOppilaitosOrKoulutusToimija).map {
          case (oppilaitos, opiskeluoikeudet) => toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
        }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering)
        OpiskeluoikeudetTyypeittäin(tyyppi, oppilaitokset)
    }.toList.sorted(opiskeluoikeudetTyypeittäinOrdering)
    buildModel(OppijaEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], tyypit), editable)
  }

  def buildModel(obj: AnyRef, editable: Boolean)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }

  def toOppilaitoksenOpiskeluoikeus(oppilaitos: OrganisaatioWithOid, opiskeluoikeudet: Seq[Opiskeluoikeus]) = {
    OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sorted(opiskeluoikeusOrdering).map {
      case oo: AikuistenPerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(aikuistenPerusopetuksenSuoritustenJärjestysKriteeri))
      case oo: PerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(perusopetuksenSuoritustenJärjestysKriteeri))
      case oo: AmmatillinenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(ammatillisenSuoritustenJärjestysKriteeri))
      case oo: Any => oo
    })
  }

  def aikuistenPerusopetuksenSuoritustenJärjestysKriteeri(s: AikuistenPerusopetuksenPäätasonSuoritus) = {
    val primary = s match {
      case s: AikuistenPerusopetuksenOppimääränSuoritus => 0
      case s: AikuistenPerusopetuksenAlkuvaiheenSuoritus => 1
      case s: PerusopetuksenOppiaineenOppimääränSuoritus => 2
      case _ => 100
    }
    val secondary = s.valmis
    (primary, secondary)
  }

  def perusopetuksenSuoritustenJärjestysKriteeri(s: PerusopetuksenPäätasonSuoritus) = {
    val primary = s match {
      case s: PerusopetuksenOppimääränSuoritus => -100 // ensin oppimäärän suoritus
      case s: PerusopetuksenOppiaineenOppimääränSuoritus => 0 // oppiaineiden oppimäärien suoritukset
      case s: PerusopetuksenVuosiluokanSuoritus => - s.koulutusmoduuli.tunniste.koodiarvo.toInt // sitten luokka-asteet
      case _ => 100
    }
    val secondary = s.valmis
    (primary, secondary)
  }

  def ammatillisenSuoritustenJärjestysKriteeri(s: AmmatillinenPäätasonSuoritus): Int = {
    s.alkamispäivä.map(a => -a.toEpochDay.toInt).getOrElse(0)
  }
}

object EditorSchema {
  lazy val schema = KoskiSchema.schemaFactory.createSchema(classOf[OppijaEditorView].getName).asInstanceOf[ClassSchema].moveDefinitionsToTopLevel
  lazy val deserializationContext = ExtractionContext(KoskiSchema.schemaFactory, validate = false)
}

case class OppijaEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OpiskeluoikeudetTyypeittäin]
)

case class OpiskeluoikeudetTyypeittäin(@KoodistoUri("opiskeluoikeudentyyppi") tyyppi: Koodistokoodiviite, opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]) {
  lazy val latestAlkamispäiväForOrdering: Option[LocalDate] =
    Some(opiskeluoikeudet.collect { case o if o.latestAlkamispäiväForOrdering.nonEmpty => o.latestAlkamispäiväForOrdering.get }).filter(_.nonEmpty).map(_.max(localDateOrdering))

}
case class OppilaitoksenOpiskeluoikeudet(oppilaitos: OrganisaatioWithOid, opiskeluoikeudet: List[Opiskeluoikeus]) {
  lazy val latestAlkamispäiväForOrdering: Option[LocalDate] =
    Some(opiskeluoikeudet.collect { case o if o.alkamispäivä.nonEmpty => o.alkamispäivä.get }).filter(_.nonEmpty).map(_.max(localDateOrdering))
}
