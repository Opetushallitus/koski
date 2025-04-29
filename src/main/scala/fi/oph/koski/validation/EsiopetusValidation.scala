package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Aikajakso, EsiopetuksenOpiskeluoikeudenLisätiedot, EsiopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, MahdollisestiAlkupäivällinenJakso}
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.koski.util.FinnishDateFormat
import fi.oph.koski.validation.PidennetynOppivelvollisuudenMuutoksenValidaatio.validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin

import java.time.LocalDate

object EsiopetusValidation {
  def validateOpiskeluoikeus(config: Config)(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    oo match {
      case e: EsiopetuksenOpiskeluoikeus => validateEsiopetuksenOpiskeluoikeus(config, e)
      case _ => HttpStatus.ok
    }

  private def validateEsiopetuksenOpiskeluoikeus(config: Config, oo: EsiopetuksenOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(
      oo.lisätiedot.toList.flatMap(lisätiedot => List(
        validateTuenJaksojenPäällekkäisyys(lisätiedot),
        validateOppivelvollisuudenPidennysjaksojenPäällekkäisyys(lisätiedot),
      ))
      :+ validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(config, oo.alkamispäivä, oo.päättymispäivä, oo.lisätiedot),
    )

  private def validateTuenJaksojenPäällekkäisyys(tiedot: EsiopetuksenOpiskeluoikeudenLisätiedot): HttpStatus = {
    val erityisenTuenPäätökset = tiedot.erityisenTuenPäätökset.toList.flatten
    val tuenPäätöksenJaksot = tiedot.tuenPäätöksenJaksot.toList.flatten

    if (erityisenTuenPäätökset.nonEmpty && tuenPäätöksenJaksot.nonEmpty) {
      HttpStatus.validateNot(MahdollisestiAlkupäivällinenJakso.overlap(erityisenTuenPäätökset, tuenPäätöksenJaksot))(
        KoskiErrorCategory.badRequest.validation.date.erityisenTuenPäätös(s"Erityisen tuen päätöksen jakso ja tuen päätöksen jakso eivät saa olla päällekkäin")
      )
    } else {
      HttpStatus.ok
    }
  }

  private def validateOppivelvollisuudenPidennysjaksojenPäällekkäisyys(tiedot: EsiopetuksenOpiskeluoikeudenLisätiedot): HttpStatus = {
    val pidennettyOppivelvollisuus = tiedot.pidennettyOppivelvollisuus.toList
    val varhennetunOppivelvollisuudenJaksot = tiedot.varhennetunOppivelvollisuudenJaksot.toList.flatten

    if (pidennettyOppivelvollisuus.nonEmpty && varhennetunOppivelvollisuudenJaksot.nonEmpty) {
      HttpStatus.validateNot(Aikajakso.overlap(pidennettyOppivelvollisuus, varhennetunOppivelvollisuudenJaksot))(
        KoskiErrorCategory.badRequest.validation.date.erityisenTuenPäätös(s"Pidennetyn oppivelvollisuuden päivämääräväli ja varhennetun oppivelvollisuuden jakso eivät saa olla päällekkäin")
      )
    } else {
      HttpStatus.ok
    }
  }
}
