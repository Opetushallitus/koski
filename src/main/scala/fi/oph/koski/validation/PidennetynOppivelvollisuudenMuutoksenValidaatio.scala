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
    päättymispäivä: Option[LocalDate],
    lisätiedot: Option[PidennettyOppivelvollisuus]
  ): HttpStatus = {
    val validaatioVoimassa = LocalDate
      .now()
      .isEqualOrAfter(LocalDate.parse(
        config.getString("validaatiot.esiJaPerusopetuksenVanhojenJaksojenPäättymispäivänValidaatiotAstuvatVoimaan")
      ))
    val rajapäiväAlku = LocalDate.parse(config.getString("validaatiot.pidennetynOppivelvollisuudenViimeinenSallittuAloituspäivä"))
    val rajapäiväLoppu = LocalDate.parse(config.getString("validaatiot.pidennetynOppivelvollisuudenViimeinenKäyttöpäivä"))
    val jatkuuRajapäivänJälkeen = päättymispäivä.forall(_.isAfter(rajapäiväLoppu))

    if (validaatioVoimassa) {
      val invalidOppivelvollisuudenPidennysAlku = filterInvalidJaksotAlku(lisätiedot.flatMap(_.pidennettyOppivelvollisuus).toList, rajapäiväAlku)
      val invalidOppivelvollisuudenPidennysLoppu = filterInvalidJaksotLoppu(lisätiedot.flatMap(_.pidennettyOppivelvollisuus).toList, rajapäiväLoppu, jatkuuRajapäivänJälkeen)
      val invalidVammaisuusjaksotAlku = filterInvalidJaksotAlku(lisätiedot.toList.flatMap(_.vammainen.toList.flatten), rajapäiväAlku)
      val invalidVammaisuusjaksotLoppu = filterInvalidJaksotLoppu(lisätiedot.toList.flatMap(_.vammainen.toList.flatten), rajapäiväLoppu, jatkuuRajapäivänJälkeen)
      val invalidVaikeaVammaisuusjaksotAlku = filterInvalidJaksotAlku(lisätiedot.toList.flatMap(_.vaikeastiVammainen.toList.flatten), rajapäiväAlku)
      val invalidVaikeaVammaisuusjaksotLoppu = filterInvalidJaksotLoppu(lisätiedot.toList.flatMap(_.vaikeastiVammainen.toList.flatten), rajapäiväLoppu, jatkuuRajapäivänJälkeen)
      val invalidErityisenTuenJaksotAlku = filterInvalidJaksotAlku(lisätiedot.toList.flatMap(_.erityisenTuenPäätökset.toList.flatten).map(_.toAikajakso), rajapäiväAlku)
      val invalidErityisenTuenJaksotLoppu = filterInvalidJaksotLoppu(lisätiedot.toList.flatMap(_.erityisenTuenPäätökset.toList.flatten).map(_.toAikajakso), rajapäiväLoppu, jatkuuRajapäivänJälkeen)

      HttpStatus.fold(
        HttpStatus.validate(invalidOppivelvollisuudenPidennysAlku.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus(s"Pidennetyn oppivelvollisuuden (${formatJaksot(invalidOppivelvollisuudenPidennysAlku)}) viimeinen mahdollinen alkamispäivä on ${FinnishDateFormat.format(rajapäiväAlku)}.")
        ),
        HttpStatus.validate(invalidOppivelvollisuudenPidennysLoppu.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus(s"Pidennetyn oppivelvollisuuden (${formatJaksot(invalidOppivelvollisuudenPidennysLoppu)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(rajapäiväLoppu)}.")
        ),
        HttpStatus.validate(invalidVammaisuusjaksotAlku.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vammaisuuden jakson (${formatJaksot(invalidVammaisuusjaksotAlku)}) viimeinen mahdollinen alkamispäivä on ${FinnishDateFormat.format(rajapäiväAlku)}.")
        ),
        HttpStatus.validate(invalidVammaisuusjaksotLoppu.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vammaisuuden jakson (${formatJaksot(invalidVammaisuusjaksotLoppu)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(rajapäiväLoppu)}.")
        ),
        HttpStatus.validate(invalidVaikeaVammaisuusjaksotAlku.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vaikeasti vammaisuuden jakson (${formatJaksot(invalidVaikeaVammaisuusjaksotAlku)}) viimeinen mahdollinen alkamispäivä on ${FinnishDateFormat.format(rajapäiväAlku)}.")
        ),
        HttpStatus.validate(invalidVaikeaVammaisuusjaksotLoppu.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vaikeasti vammaisuuden jakson (${formatJaksot(invalidVaikeaVammaisuusjaksotLoppu)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(rajapäiväLoppu)}.")
        ),
        HttpStatus.validate(invalidErityisenTuenJaksotAlku.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date(s"Erityisen tuen päätöksen (${formatJaksot(invalidErityisenTuenJaksotAlku)}) viimeinen mahdollinen alkamispäivä on ${FinnishDateFormat.format(rajapäiväAlku)}.")
        ),
        HttpStatus.validate(invalidErityisenTuenJaksotLoppu.isEmpty)(
          KoskiErrorCategory.badRequest.validation.date(s"Erityisen tuen päätöksen (${formatJaksot(invalidErityisenTuenJaksotLoppu)}) viimeinen mahdollinen päättymispäivä on ${FinnishDateFormat.format(rajapäiväLoppu)}.")
        ),
      )
    } else {
      HttpStatus.ok
    }
  }

  private def filterInvalidJaksotAlku(jaksot: List[Aikajakso], rajapäiväAlku: LocalDate): List[Aikajakso] =
    jaksot.filter(j => j.alku.isAfter(rajapäiväAlku))

  private def filterInvalidJaksotLoppu(jaksot: List[Aikajakso], rajapäiväLoppu: LocalDate, jatkuuRajapäivänJälkeen: Boolean): List[Aikajakso] =
    jaksot.filter(j => j.loppu.fold(jatkuuRajapäivänJälkeen)(jaksonLoppu => jaksonLoppu.isAfter(rajapäiväLoppu)))

  private def formatJaksot(jaksot: List[Aikajakso]): String =
    jaksot.map(_.toFinnishDateFormat).mkString(", ")
}
