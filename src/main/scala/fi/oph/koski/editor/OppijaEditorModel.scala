package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.scalaschema.{ClassSchema, ExtractionContext}

object OppijaEditorModel {
  // Note: even with editable=true, editability will be checked based on organizational access on the lower level
  def toEditorModel(oppija: Oppija, editable: Boolean)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    implicit val opiskeluoikeusOrdering = new Ordering[Option[LocalDate]] {
      override def compare(x: Option[LocalDate], y: Option[LocalDate]) = (x, y) match {
        case (None, Some(_)) => 1
        case (Some(_), None) => -1
        case (None, None) => 0
        case (Some(x), Some(y)) => if (x.isBefore(y)) { -1 } else { 1 }
      }
    }

    val tyypit = oppija.opiskeluoikeudet.groupBy(_.tyyppi).map {
      case (tyyppi, opiskeluoikeudet) =>
        val oppilaitokset = opiskeluoikeudet.groupBy(_.getOppilaitos).map {
          case (oppilaitos, opiskeluoikeudet) =>
            OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sortBy(_.alkamispäivä).map {
              case oo: PerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(perusopetuksenSuoritustenJärjestysKriteeri))
              case oo: AmmatillinenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(_.alkamispäivä).reverse)
              case oo: Any => oo
            })
        }.toList.sortBy(_.opiskeluoikeudet(0).alkamispäivä)
        OpiskeluoikeudetTyypeittäin(tyyppi, oppilaitokset)
    }.toList.sortBy(_.opiskeluoikeudet(0).opiskeluoikeudet(0).alkamispäivä).reverse
    val editorView = OppijaEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], tyypit)
    buildModel(editorView, editable)
  }

  def buildModel(obj: AnyRef, editable: Boolean)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
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
}

object EditorSchema {
  lazy val schema = KoskiSchema.schemaFactory.createSchema(classOf[OppijaEditorView].getName).asInstanceOf[ClassSchema].moveDefinitionsToTopLevel
  lazy val deserializationContext = ExtractionContext(schema, validate = false)
}

case class OppijaEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OpiskeluoikeudetTyypeittäin]
)

case class OpiskeluoikeudetTyypeittäin(@KoodistoUri("opiskeluoikeudentyyppi") tyyppi: Koodistokoodiviite, opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet])
case class OppilaitoksenOpiskeluoikeudet(oppilaitos: Oppilaitos, opiskeluoikeudet: List[Opiskeluoikeus])