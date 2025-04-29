package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{Aikajakso, PidennettyOppivelvollisuus}
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.util.FinnishDateFormat

import java.time.LocalDate

object PidennetynOppivelvollisuudenMuutoksenValidaatio {
  def validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(
    config: Config,
    alkamispäivä: Option[LocalDate],
    päättymispäivä: Option[LocalDate],
    lisätiedot: Option[PidennettyOppivelvollisuus]
  ): HttpStatus = {
    val validaatioVoimassa = LocalDate
      .now()
      .isEqualOrAfter(LocalDate.parse(
        config.getString("validaatiot.esiJaPerusopetuksenVanhojenJaksojenPäättymispäivänValidaatiotAstuvatVoimaan")
      ))
    val rajapäivä = LocalDate.parse(config.getString("validaatiot.varhennettuOppivelvollisuusVoimaan"))

    lazy val pidennetynOppivelvollisuudenViimeinenKäyttöpäivä = LocalDate.parse(config.getString("validaatiot.pidennetynOppivelvollisuudenViimeinenKäyttöpäivä"))
    lazy val vammaisuustietojenViimeinenKäyttöpäivä = LocalDate.parse(config.getString("validaatiot.vammaisuustietojenViimeinenKäyttöpäivä"))
    lazy val erityisenTuenPäätöstenViimeinenKäyttöpäivä = LocalDate.parse(config.getString("validaatiot.erityisenTuenPäätöstenViimeinenKäyttöpäivä"))

    if (validaatioVoimassa) {
      val alkanutEnnenRajapäivää = alkamispäivä.exists(_.isBefore(rajapäivä))
      val jatkuuRajapäivänJälkeen = päättymispäivä.exists(_.isEqualOrAfter(rajapäivä)) || päättymispäivä.isEmpty

      if (alkanutEnnenRajapäivää && jatkuuRajapäivänJälkeen) {
        val invalidOppivelvollisuudenPidennys = filterInvalidJaksot(lisätiedot.flatMap(_.pidennettyOppivelvollisuus).toList, pidennetynOppivelvollisuudenViimeinenKäyttöpäivä)
        val invalidVammaisuusjaksot = filterInvalidJaksot(lisätiedot.toList.flatMap(_.vammainen.toList.flatten), pidennetynOppivelvollisuudenViimeinenKäyttöpäivä)
        val invalidVaikeaVammaisuusjaksot = filterInvalidJaksot(lisätiedot.toList.flatMap(_.vaikeastiVammainen.toList.flatten), vammaisuustietojenViimeinenKäyttöpäivä)
        val invalidErityisenTuenJaksot = filterInvalidJaksot(lisätiedot.toList.flatMap(_.erityisenTuenPäätökset.toList.flatten).map(_.toAikajakso), erityisenTuenPäätöstenViimeinenKäyttöpäivä)

        HttpStatus.fold(
          HttpStatus.validate(invalidOppivelvollisuudenPidennys.isEmpty)(
            KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus(s"Pidennetyn oppivelvollisuuden (${formatJaksot(invalidOppivelvollisuudenPidennys)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(pidennetynOppivelvollisuudenViimeinenKäyttöpäivä)}.")
          ),
          HttpStatus.validate(invalidVammaisuusjaksot.isEmpty)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vammaisuuden jakson (${formatJaksot(invalidVammaisuusjaksot)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(vammaisuustietojenViimeinenKäyttöpäivä)}.")
          ),
          HttpStatus.validate(invalidVaikeaVammaisuusjaksot.isEmpty)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vaikeasti vammaisuuden jakson (${formatJaksot(invalidVaikeaVammaisuusjaksot)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(vammaisuustietojenViimeinenKäyttöpäivä)}.")
          ),
          HttpStatus.validate(invalidErityisenTuenJaksot.isEmpty)(
            KoskiErrorCategory.badRequest.validation.date(s"Erityisen tuen päätöksen (${formatJaksot(invalidErityisenTuenJaksot)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(erityisenTuenPäätöstenViimeinenKäyttöpäivä)}.")
          ),
        )
      } else {
        HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }

  private def filterInvalidJaksot(jaksot: List[Aikajakso], rajapäivä: LocalDate): List[Aikajakso] =
    jaksot.filter(j => j.alku.isEqualOrAfter(rajapäivä) || j.loppu.fold(true)(_.isAfter(rajapäivä)))

  private def formatJaksot(jaksot: List[Aikajakso]): String =
    jaksot.map(_.toFinnishDateFormat).mkString(", ")
}
