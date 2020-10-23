package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019OsasuoritusValidation {

  val lukiodiplomit = List("KOLD1", "KULD2", "KÄLD3", "LILD4", "MELD5", "MULD6", "TALD7", "TELD8")
  val lukiodiplomienSallitutOppiaineet = List(("KU", "KULD2"), ("LI", "LILD4"), ("MU", "MULD6"), ("TE", "TELD8"))

  def validate(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = {
    HttpStatus.fold(List(
      validateErityinenTutkinto(suoritus, parents),
      validateLukiodiplomiRakenne(suoritus, parents),
      validateLukiodiplomiLaajuus(suoritus),
      validateOppiaineenSuorituskieli(suoritus, parents),
      validateModuulinJaPaikallisenOpintojaksonSuorituskieli(suoritus, parents),
      validateModuulitPaikallisessaOppiaineessa(suoritus, parents)
    ))
  }

  private def validateErityinenTutkinto(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s, (p: SuoritettavissaErityisenäTutkintona2019) :: (_: LukionOppimääränSuoritus2019 | _: PreIBSuoritus2019) :: _) if (p.suoritettuErityisenäTutkintona) =>
      KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia(s"Osasuoritus ${suorituksenTunniste(suoritus)} ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona")
    case (s, (_: LukionOppiaineenSuoritus2019) :: (pp: LukionOppimääränSuoritus2019) :: _) if (pp.suoritettuErityisenäTutkintona) =>
      KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia(s"Osasuoritus ${suorituksenTunniste(suoritus)} ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona")
    case _ =>
      HttpStatus.ok
  }

  private def validateLukiodiplomiRakenne(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MuidenLukioOpintojenSuoritus2019) :: _)
      if p.koulutusmoduuli.tunniste.koodiarvo == "LD" &&
        !lukiodiplomit.contains(s.koulutusmoduuli.tunniste.koodiarvo) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuoritus ${suorituksenTunniste(suoritus)} ei ole sallittu lukiodiplomisuoritus")
    case (s: LukionModuulinSuoritus2019, (p: LukionOppimääränOsasuoritus2019) :: _)
      if p.koulutusmoduuli.tunniste.koodiarvo != "LD" &&
        lukiodiplomit.contains(s.koulutusmoduuli.tunniste.koodiarvo) &&
        !lukiodiplomienSallitutOppiaineet.contains((p.koulutusmoduuli.tunniste.koodiarvo, s.koulutusmoduuli.tunniste.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Lukiodiplomimoduuli (${suorituksenTunniste(suoritus)}) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus.")
    case _ =>
      HttpStatus.ok
  }

  private def validateLukiodiplomiLaajuus(suoritus: Suoritus): HttpStatus = suoritus match {
    case s: LukionModuulinSuoritus2019
      if (lukiodiplomit.contains(s.koulutusmoduuli.tunniste.koodiarvo) && (s.koulutusmoduuli.laajuusArvo(0.0) != 2.0)) =>
      KoskiErrorCategory.badRequest.validation.laajuudet.lukiodiplominLaajuusEiOle2Opintopistettä(s"Osasuorituksen ${suorituksenTunniste(suoritus)} laajuus ei ole oikea. Lukiodiplomimoduulin laajuus tulee olla aina 2 opintopistettä.")
    case _ =>
      HttpStatus.ok
  }

  private def validateOppiaineenSuorituskieli(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s: LukionOppiaineenSuoritus2019, (p: Suorituskielellinen) :: _)
      if s.suorituskieli.contains(p.suorituskieli) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Oppiaineen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli")
    case _ =>
      HttpStatus.ok
  }

  private def validateModuulinJaPaikallisenOpintojaksonSuorituskieli(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MahdollisestiSuorituskielellinen) :: _)
      if s.suorituskieli.exists(p.suorituskieli.contains) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuorituksen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin oppiaineen suorituskieli")
    case (s: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MahdollisestiSuorituskielellinen) :: (pp: Suorituskielellinen) :: _)
      if p.suorituskieli.isEmpty && s.suorituskieli.contains(pp.suorituskieli) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuorituksen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli")
    case _ =>
      HttpStatus.ok
  }

  private def validateModuulitPaikallisessaOppiaineessa(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s: LukionModuulinSuoritus2019, (p : LukionOppiaineenSuoritus2019) :: _) if p.koulutusmoduuli.isInstanceOf[PaikallinenLukionOppiaine2019] =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Paikalliseen oppiaineeseen ${p.koulutusmoduuli.tunniste} ei voi lisätä valtakunnallista moduulia ${s.koulutusmoduuli.tunniste}. Paikallisessa oppiaineessa voi olla vain paikallisia opintojaksoja.")
    case _ =>
      HttpStatus.ok
  }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }
}
