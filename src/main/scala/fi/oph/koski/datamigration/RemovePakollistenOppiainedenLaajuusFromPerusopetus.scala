package fi.oph.koski.datamigration

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonDiff
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import rx.lang.scala.Observable


object RemovePakollistenOppiainedenLaajuusFromPerusopetus extends Logging {
  private val batchSize = 50
  val user = KoskiSpecificSession.systemUser

  def migrate()(implicit application: KoskiApplication) = {
    logger.info("Remove laajuudet")
    application.opiskeluoikeusQueryRepository
      .mapKaikkiOpiskeluoikeudetSivuittain(batchSize, user) {
        rows => rows.map {row =>
          try {
            row.toOpiskeluoikeus
          } catch {
            case _: Throwable =>
          }
        }
      }
      .flatMap {
        case opiskeluoikeus: PerusopetuksenOpiskeluoikeus if opiskeluoikeus.lähdejärjestelmänId.isDefined =>
          var changed = false
          val setChanged = () => changed = true

          val suoritukset = opiskeluoikeus.suoritukset.map {
            case oppimäärä: NuortenPerusopetuksenOppimääränSuoritus if oppimäärä.vahvistus.exists(_.päivä.isBefore(LocalDate.of(2020, 8, 1))) =>
              poistaLaajuusPakollisiltaOppiaineilta(oppimäärä)(setChanged)
            case vuosiluokka: PerusopetuksenVuosiluokanSuoritus if vuosiluokka.vahvistus.exists(_.päivä.isBefore(LocalDate.of(2020, 8, 1))) =>
              poistaLaajuusPakollisiltaOppiaineilta(vuosiluokka)(setChanged)
            case suoritus => suoritus
          }

          if (changed) {
            Observable.just(Update(original = opiskeluoikeus, updated = opiskeluoikeus.withSuoritukset(suoritukset)))
          } else {
            Observable.empty
          }
        case _ => Observable.empty
      }
     .map { case Update(original, updated) => JsonDiff.objectDiff(original, updated).toString }
     .foreach(jsondiff => logger.info(jsondiff))
  }

  private def poistaLaajuusPakollisiltaOppiaineilta(suoritus: PerusopetuksenPäätasonSuoritus)(setChanged: () => Unit) = {
    val osasuoritukset = suoritus.osasuoritukset.map(_.map {
      case oppiaine: NuortenPerusopetuksenOppiaineenSuoritus if oppiaine.koulutusmoduuli.pakollinen && oppiaine.koulutusmoduuli.laajuus.isDefined => {
        setChanged()
        oppiaine.copy(koulutusmoduuli = oppiaine.koulutusmoduuli.withLaajuus(None))
      }
      case oppiaine => oppiaine
    })
    suoritus.withOsasuoritukset(osasuoritukset)
  }
}

case class Update(
  original: KoskeenTallennettavaOpiskeluoikeus,
  updated: KoskeenTallennettavaOpiskeluoikeus
)
