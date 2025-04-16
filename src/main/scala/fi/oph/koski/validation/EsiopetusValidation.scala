package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Aikajakso, EsiopetuksenOpiskeluoikeudenLisätiedot, EsiopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, MahdollisestiAlkupäivällinenJakso}
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.koski.util.FinnishDateFormat

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
      :+ validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(config, oo)
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

  private def validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(config: Config, oo: EsiopetuksenOpiskeluoikeus): HttpStatus = {
    val validaatioVoimassa = LocalDate
      .now()
      .isEqualOrAfter(LocalDate.parse(
        config.getString("validaatiot.esiJaPerusopetuksenVanhojenJaksojenPäättymispäivänValidaatiotAstuvatVoimaan")
      ))

    val rajapäivä = LocalDate.parse(config.getString("validaatiot.varhennettuOppivelvollisuusVoimaan"))

    if (validaatioVoimassa) {
      val alkanutEnnenRajapäivää = oo.alkamispäivä.exists(_.isBefore(rajapäivä))
      val jatkuuRajapäivänJälkeen = oo.päättymispäivä.exists(_.isEqualOrAfter(rajapäivä)) || oo.päättymispäivä.isEmpty

      if (alkanutEnnenRajapäivää && jatkuuRajapäivänJälkeen) {
        lazy val viimeinenPvmStr = FinnishDateFormat.format(rajapäivä.minusDays(1))

        val oppivelvollisuudenPidennys = oo.lisätiedot.flatMap(_.pidennettyOppivelvollisuus)
        val viimeisinVammaisuusjakso = oo.lisätiedot.toList.flatMap(_.vammainen.toList.flatten).sortBy(_.alku).lastOption
        val viimeisinVaikeaVammaisuusjakso = oo.lisätiedot.toList.flatMap(_.vaikeastiVammainen.toList.flatten).sortBy(_.alku).lastOption

        HttpStatus.fold(
          HttpStatus.validateNot(oppivelvollisuudenPidennys.exists(_.contains(rajapäivä)))(
            KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus(s"Pidennetyn oppivelvollisuuden viimeinen mahdollinen päättymispäivä on $viimeinenPvmStr. Merkitse kyseisen päivän jälkeiset jaksot varhennettuun oppivelvollisuuteen.")
          ),
          HttpStatus.validateNot(viimeisinVammaisuusjakso.exists(_.contains(rajapäivä)))(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vammaisuuden jakson viimeinen mahdollinen päättymispäivä on $viimeinenPvmStr.")
          ),
          HttpStatus.validateNot(viimeisinVaikeaVammaisuusjakso.exists(_.contains(rajapäivä)))(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vaikeasti vammaisuuden jakson viimeinen mahdollinen päättymispäivä on $viimeinenPvmStr.")
          ),
        )
      } else {
        HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }
}
