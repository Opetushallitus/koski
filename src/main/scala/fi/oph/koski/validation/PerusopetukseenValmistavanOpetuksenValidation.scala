package fi.oph.koski.validation

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Aikajakso, KoskeenTallennettavaOpiskeluoikeus, PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot}

object PerusopetukseenValmistavanOpetuksenValidation {

  private val lisäopetusVoimassaAlkaen = LocalDate.of(2026, 8, 1)
  private val maxLisäopetusPäiviä = 365L

  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    oo match {
      case p: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => validatePerusopetukseenValmistavanOpetuksenOpiskeluoikeus(p)
      case _ => HttpStatus.ok
    }

  private def validatePerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    opiskeluoikeus: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus
  ): HttpStatus = {
    opiskeluoikeus.lisätiedot match {
      case Some(lisätiedot) => validateLisäopetusLisätiedot(lisätiedot, opiskeluoikeus.alkamispäivä)
      case None => HttpStatus.ok
    }
  }

  private def validateLisäopetusLisätiedot(
    lisätiedot: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenLisätiedot,
    alkamispäivä: Option[LocalDate]
  ): HttpStatus = {
    lisätiedot.lisäopetus match {
      case Some(jaksot) if jaksot.nonEmpty =>
        HttpStatus.fold(
          validateLisäopetusJaksojenAlkamispäivät(jaksot),
          validateLisäopetuksenKokonaiskesto(jaksot),
          validateLisäopetusJaksojenPäällekkäisyys(jaksot)
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateLisäopetusJaksojenAlkamispäivät(jaksot: List[Aikajakso]): HttpStatus = {
    val alkaaEnnenRajapäivää = jaksot.exists(_.alku.isBefore(lisäopetusVoimassaAlkaen))
    HttpStatus.validate(!alkaaEnnenRajapäivää)(
      KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetusEiSallittuEnnen()
    )
  }

  private def validateLisäopetuksenKokonaiskesto(jaksot: List[Aikajakso]): HttpStatus = {
    val kokonaispäivät = jaksot.map(jaksonPäivät).sum
    HttpStatus.validate(kokonaispäivät <= maxLisäopetusPäiviä)(
      KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetuksenKestoYlittääVuoden()
    )
  }

  private def jaksonPäivät(jakso: Aikajakso): Long = {
    jakso.loppu match {
      case Some(loppu) => ChronoUnit.DAYS.between(jakso.alku, loppu) + 1
      case None => 1 // Jos loppupäivää ei ole, lasketaan vain alkupäivä
    }
  }

  private def validateLisäopetusJaksojenPäällekkäisyys(jaksot: List[Aikajakso]): HttpStatus = {
    val päällekkäisiäJaksoja = jaksot.combinations(2).exists {
      case List(a, b) => a.overlaps(b)
      case _ => false
    }
    HttpStatus.validate(!päällekkäisiäJaksoja)(
      KoskiErrorCategory.badRequest.validation.perusopetukseenValmistavaOpetus.lisäopetusJaksotPäällekkäin()
    )
  }
}
