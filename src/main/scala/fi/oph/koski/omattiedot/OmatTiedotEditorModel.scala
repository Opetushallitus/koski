package fi.oph.koski.omattiedot

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.oppilaitoksenOpiskeluoikeudetOrdering
import fi.oph.koski.editor._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.Hidden
import fi.oph.koski.util.{Timing, WithWarnings}
import fi.oph.scalaschema.annotation.SyntheticProperty
import mojave._

object OmatTiedotEditorModel extends Timing {
  def toEditorModel(oppijaWithWarnings: WithWarnings[Oppija])(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = timed("createModel") {
    val piilotetuillaTiedoilla = piilotaArvosanatKeskeneräisistäSuorituksista _ andThen
      piilotaSensitiivisetHenkilötiedot andThen
      piilotaKeskeneräisetPerusopetuksenPäättötodistukset

    buildModel(buildView(piilotetuillaTiedoilla(oppijaWithWarnings.getIgnoringWarnings), oppijaWithWarnings.warnings))
  }

  def opiskeluoikeudetOppilaitoksittain(oppija: Oppija): List[OppilaitoksenOpiskeluoikeudet] = {
    oppija.opiskeluoikeudet.groupBy(_.getOppilaitosOrKoulutusToimija).map {
      case (oppilaitos, opiskeluoikeudet) => OppijaEditorModel.toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
    }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering)
  }

  private def buildView(oppija: Oppija, warnings: Seq[HttpStatus])(implicit application: KoskiApplication, koskiSession: KoskiSession) = {
    val huollettavat = application.huollettavatService.getHuollettavatWithOid(koskiSession.oid).map(_.toHenkilötiedotJaOid)
    OmatTiedotEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], huollettavat, opiskeluoikeudetOppilaitoksittain(oppija), koskiSession.oid, warnings.flatMap(_.errors).map(_.key).toList)
  }

  private def buildModel(obj: AnyRef)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable = false)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }

  private def piilotaArvosanatKeskeneräisistäSuorituksista(oppija: Oppija) = {
    val keskeneräisetTaiLiianÄskettäinVahvistetut = traversal[Suoritus].filter { s =>
      s.vahvistus.isEmpty || !s.vahvistus.exists { v => v.päivä.plusDays(4).isBefore(LocalDate.now())}
    }.compose(päätasonSuorituksetTraversal)
    val piilotettavatOppiaineidenArvioinnit = (oppimääränArvioinnitTraversal ++ vuosiluokanArvioinnitTraversal ++ oppiaineenOppimääränArvioinnitTraversal).compose(keskeneräisetTaiLiianÄskettäinVahvistetut)
    val piilotettavaKäyttäytymisenArviointi = käyttäytymisenArviointiTraversal.compose(keskeneräisetTaiLiianÄskettäinVahvistetut)

    List(piilotettavaKäyttäytymisenArviointi, piilotettavatOppiaineidenArvioinnit).foldLeft(oppija) { (oppija, traversal) =>
      traversal.set(oppija)(None)
    }
  }

  private def piilotaSensitiivisetHenkilötiedot(oppija: Oppija) = {
    val t: Traversal[Oppija, TäydellisetHenkilötiedot] = traversal[Oppija].field[Henkilö]("henkilö").ifInstanceOf[TäydellisetHenkilötiedot]
    t.modify(oppija)((th: TäydellisetHenkilötiedot) => th.copy(hetu = None, kansalaisuus = None, turvakielto = None))
  }

  def piilotaKeskeneräisetPerusopetuksenPäättötodistukset(oppija: Oppija): Oppija = {
    def poistaKeskeneräisetPäättötodistukset = (suoritukset: List[PäätasonSuoritus]) => suoritukset.filter(_ match {
      case s: PerusopetuksenOppimääränSuoritus if !s.valmis => false
      case _ => true
    })

    def poistaOsasuoritukset = (suoritukset: List[PäätasonSuoritus]) => suoritukset.map(s =>
      shapeless.lens[PäätasonSuoritus].field[Option[List[Suoritus]]]("osasuoritukset").set(s)(None)
    )

    shapeless.lens[Oppija].field[Seq[Opiskeluoikeus]]("opiskeluoikeudet").modify(oppija)(_.map(oo => {
      val isKeskeneräinenPäättötodistusAinoaSuoritus = oo.suoritukset match {
        case (s: PerusopetuksenOppimääränSuoritus) :: Nil if s.kesken => true
        case _ => false
      }

      shapeless.lens[Opiskeluoikeus].field[List[PäätasonSuoritus]]("suoritukset").modify(oo)(
        if (isKeskeneräinenPäättötodistusAinoaSuoritus) poistaOsasuoritukset else poistaKeskeneräisetPäättötodistukset
      )
    }))
  }
}

case class OmatTiedotEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  @Hidden
  huollettavat: List[HenkilötiedotJaOid],
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet],
  @Hidden
  henkilöOid: String,
  @Hidden
  varoitukset: List[String]
) {
  @Hidden
  @SyntheticProperty
  def hasHuollettavia: Boolean = huollettavat.nonEmpty
}
