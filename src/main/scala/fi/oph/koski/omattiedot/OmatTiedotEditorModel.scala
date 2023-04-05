package fi.oph.koski.omattiedot

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.oppilaitoksenOpiskeluoikeudetOrdering
import fi.oph.koski.editor._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.huoltaja.{Huollettava, HuollettavatSearchResult, HuollettavienHakuOnnistui}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.Hidden
import fi.oph.koski.util.{Timing, WithWarnings}
import fi.oph.scalaschema.annotation.SyntheticProperty
import mojave._

object OmatTiedotEditorModel extends Timing {

  def toEditorModel(oppija: Oppija)(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel =
    toEditorModel(userOppija = oppija, näytettäväOppija = oppija, warnings = Nil)

  def toEditorModel(userOppija: WithWarnings[Oppija], näytettäväOppija: WithWarnings[Oppija])(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel =
    toEditorModel(userOppija.getIgnoringWarnings, näytettäväOppija.getIgnoringWarnings, warnings = näytettäväOppija.warnings)

  def toEditorModel(userOppija: Oppija, näytettäväOppija: Oppija, warnings: Seq[HttpStatus])(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel = timed("createModel") {
    buildModel(buildView(piilotetuillaTiedoilla(userOppija), piilotetuillaTiedoilla(näytettäväOppija), warnings))
  }

  def piilotetuillaTiedoilla(oppija: Oppija)(implicit koskiSession: KoskiSpecificSession): Oppija = {
    val piilota = piilotaArvosanatKeskeneräisistäSuorituksista _ andThen
      piilotaSensitiivisetHenkilötiedot andThen
      piilotaKeskeneräisetPerusopetuksenPäättötodistukset andThen
      piilotaTietojaSuoritusjaosta andThen
      piilotaLaajuuksia

    piilota(oppija)
  }

  def opiskeluoikeudetOppilaitoksittain(oppija: Oppija): List[OppilaitoksenOpiskeluoikeudet] = {
    oppija.opiskeluoikeudet.groupBy(_.getOppilaitosOrKoulutusToimija).map {
      case (oppilaitos, opiskeluoikeudet) => OppijaEditorModel.toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
    }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering)
  }

  private def buildView(userOppija: Oppija, näytettäväOppija: Oppija, warnings: Seq[HttpStatus])(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession) = {
    val huollettavat = koskiSession.huollettavat

    OmatTiedotEditorView(
      henkilö = näytettäväOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot],
      userHenkilö = userOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot],
      opiskeluoikeudet = opiskeluoikeudetOppilaitoksittain(näytettäväOppija),
      huollettavat = huollettavat.getOrElse(Nil),
      varoitukset = warnings.flatMap(_.errors).map(_.key).toList ++ huollettavat.left.map(_.errors.map(_.key)).left.getOrElse(Nil)
    )
  }

  private def buildModel(obj: AnyRef)(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable = false)(koskiSession, application.koodistoViitePalvelu, application.koskiLocalizationRepository)
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

  private def piilotaLaajuuksia(oppija: Oppija)= {
    val  keskenTaiVahvistettuEnnenLeikkuriPäivää = traversal[Suoritus].filter { suoritus =>
      (suoritus.isInstanceOf[PerusopetuksenVuosiluokanSuoritus] || suoritus.isInstanceOf[NuortenPerusopetuksenOppimääränSuoritus]) &&
        suoritus.vahvistus.forall(_.päivä.isBefore(LocalDate.of(2020, 8, 1)))
    }.compose(päätasonSuorituksetTraversal)

    val piilotettavatLaajuudet = nuortenPerusopetuksenPakollistenOppiaineidenLaajuudetTraversal.compose(keskenTaiVahvistettuEnnenLeikkuriPäivää)

    piilotettavatLaajuudet.set(oppija)(None)
  }

  private def piilotaTietojaSuoritusjaosta(oppija: Oppija)(implicit koskiSession: KoskiSpecificSession) = {
    if (koskiSession.user.isSuoritusjakoKatsominen) {
      piilotaLukuvuosimaksutiedot(oppija)
    } else {
      oppija
    }
  }

  private def piilotaLukuvuosimaksutiedot(oppija: Oppija)(implicit koskiSession: KoskiSpecificSession) = {
    val korjatutOpiskeluoikeudet = oppija.opiskeluoikeudet.map {
      case oo: KorkeakoulunOpiskeluoikeus if oo.lisätiedot.nonEmpty => {
        val korjatutLukukausiIlmottautuminen = oo.lisätiedot.get.lukukausiIlmoittautuminen.flatMap(ilmo =>
          Some(ilmo.copy(
            ilmoittautumisjaksot = ilmo.ilmoittautumisjaksot.map(_.copy(
              maksetutLukuvuosimaksut = None
            ))
          ))
        )

        val korjatutLisätiedot = oo.lisätiedot.get.copy(
          maksettavatLukuvuosimaksut = None,
          lukukausiIlmoittautuminen = korjatutLukukausiIlmottautuminen
        )
        oo.copy(
          lisätiedot = Some(korjatutLisätiedot)
        )
      }
      case oo: Any => oo
    }
    oppija.copy(
      opiskeluoikeudet = korjatutOpiskeluoikeudet
    )
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
  userHenkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet],
  huollettavat: List[Huollettava],
  @Hidden
  varoitukset: List[String]
)

