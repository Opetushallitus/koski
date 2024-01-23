package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax._

import java.time.LocalDate

object VSTKotoutumiskoulutus2022Validation {
  def validate(opiskeluoikeus: Opiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus =>
        validateAlkamispäivä(opiskeluoikeus)
      case _ =>
        HttpStatus.ok
    }

  def validate(suoritus: Suoritus): HttpStatus =
    suoritus match {
      case päätasonSuoritus: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =>
        HttpStatus.fold(
          päätasonSuoritus
            .osasuoritukset
            .getOrElse(List.empty)
            .map(validateOsasuoritus(päätasonSuoritus))
          :+ validatePakollisetOsasuoritukset(päätasonSuoritus)
        )
      case _ =>
        HttpStatus.ok
    }

  def validateAlkamispäivä(opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus): HttpStatus = {
    val opsinVaihtumispäivä = LocalDate.of(2022, 8, 1)

    val onRajapäivänJälkeen = opiskeluoikeus.alkamispäivä.map(_.isEqualOrAfter(opsinVaihtumispäivä))

    HttpStatus.fold(opiskeluoikeus.suoritukset.map(päätasonSuoritus => {
      val pitääOllaRajapäivänJälkeen = päätasonSuoritus match {
        case _: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 => Some(true)
        case _: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus     => Some(false)
        case _ => None
      }

      (onRajapäivänJälkeen, pitääOllaRajapäivänJälkeen) match {
        case (Some(true), Some(false))  => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2012()
        case (Some(false), Some(true))  => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2022()
        case _ => HttpStatus.ok
      }
    }))
  }

  def validatePakollisetOsasuoritukset(päätasonSuoritus: VapaanSivistystyönKoulutuksenPäätasonSuoritus): HttpStatus =
    josVahvistettu(päätasonSuoritus) {
      val pakollisetOsasuoritukset = Set(
        "yhteiskuntajatyoelamaosaaminen",
        "kielijaviestintaosaaminen",
        "ohjaus",
      )

      collectOsasuoritustenTyypit(päätasonSuoritus) match {
        case Some(tyypit) if pakollisetOsasuoritukset.forall(tyypit.contains) => HttpStatus.ok
        case _ => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.puuttuvaOpintokokonaisuus("Suoritusta ei voi vahvistaa ennen kuin kaikki pakolliset osasuoritukset on arvioitu")
      }
    }

  def validateOsasuoritus(päätasonSuoritus: VapaanSivistystyönKoulutuksenPäätasonSuoritus)(osasuoritus: VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022): HttpStatus =
    osasuoritus match {
      case s: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 => validateKieliJaViestintä(s)
      case s: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 => validateYhteiskuntaJaTyöosaaminen(s)
      case s: VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 => validateOhjausLaajuus(s, päätasonSuoritus)
      case _ => HttpStatus.ok
    }

  def validateKieliJaViestintä(osasuoritus: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022): HttpStatus =
    validateKieliJaViestintäOsasuoritustenArviointi(osasuoritus)

  def validateKieliJaViestintäOsasuoritustenArviointi(suoritus: VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022): HttpStatus =
    josArvioitu(suoritus) {
      val pakollisetKieliopintojenAlaosasuoritukset = Set(
        "kuullunymmartaminen",
        "luetunymmartaminen",
        "puhuminen",
        "kirjoittaminen"
      )

      collectOsasuoritustenTyypit(suoritus, Some(_.arvioitu)) match {
        case Some(tyypit) if pakollisetKieliopintojenAlaosasuoritukset.forall(tyypit.contains) => HttpStatus.ok
        case _ => KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("Kielten ja viestinnän osasuoritusta ei voi hyväksyä ennen kuin kaikki pakolliset alaosasuoritukset on arvioitu")
      }
    }

  def validateYhteiskuntaJaTyöosaaminen(suoritus: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022): HttpStatus =
    HttpStatus.fold(
      validateYhteiskuntaJaTyöosaaminenLaajuus(suoritus),
      validateTyössäoppimisjaksot(suoritus)
    )

  def validateYhteiskuntaJaTyöosaaminenLaajuus(suoritus: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022): HttpStatus =
    josArvioitu(suoritus) {
      validateLaajuus(suoritus, 20)
    }

  def validateTyössäoppimisjaksot(suoritus: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022): HttpStatus =
    josArvioitu(suoritus) {
      val työssäoppimisjaksojenYhteislaajuus = suoritus
        .osasuoritukset
        .map(_
          .filter(_.koulutusmoduuli.tunniste.koodiarvo == "tyossaoppiminen")
          .map(_.koulutusmoduuli.laajuusArvo(0))
          .sum)
      validateLaajuus("Työssäoppiminen", työssäoppimisjaksojenYhteislaajuus, 8.0)
    }

  def validateOhjausLaajuus(suoritus: VSTKotoutumiskoulutuksenOhjauksenSuoritus2022, päätasonSuoritus: VapaanSivistystyönKoulutuksenPäätasonSuoritus): HttpStatus =
    josVahvistettu(päätasonSuoritus) {
      validateLaajuus(suoritus, 7)
    }

  def josArvioitu(suoritus: Suoritus)(f: => HttpStatus): HttpStatus =
    if (suoritus.arvioitu) f else HttpStatus.ok

  def josVahvistettu(suoritus: Suoritus)(f: => HttpStatus): HttpStatus =
    if (suoritus.vahvistettu) f else HttpStatus.ok

  def validateLaajuus(suoritus: Suoritus, minimiLaajuus: Double): HttpStatus =
    validateLaajuus(suoritus.koulutusmoduuli.nimi.get("fi"), suoritus.koulutusmoduuli.getLaajuus.map { case l: LaajuusOpintopisteissä => l.arvo }, minimiLaajuus)

  def validateLaajuus(oppiaineenNimi: String, laajuus: Option[Double], minimiLaajuus: Double): HttpStatus =
    laajuus match {
      case Some(laajuus) if laajuus >= minimiLaajuus =>
        HttpStatus.ok
      case Some(laajuus) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusLiianSuppea(s"Oppiaineen '$oppiaineenNimi' suoritettu laajuus liian suppea (${laajuus} op, pitäisi olla vähintään $minimiLaajuus op)")
      case None =>
        KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu()
    }

  def collectOsasuoritustenTyypit(suoritus: Suoritus, filter: Option[Suoritus => Boolean] = None): Option[List[String]] =
    suoritus
      .osasuoritukset
      .map(_
        .filter(filter.getOrElse({ _ => true }))
        .map(_.koulutusmoduuli.tunniste.koodiarvo))
}
