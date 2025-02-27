package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019OsasuoritusValidation {

  val lukiodiplomit = List("KOLD1", "KULD2", "KÄLD3", "LILD4", "MELD5", "MULD6", "TALD7", "TELD8")
  val lukiodiplomienSallitutOppiaineet = List(("KU", "KULD2"), ("LI", "LILD4"), ("MU", "MULD6"), ("TE", "TELD8"))
  val äidinkielenSallitutModuulit = List("ÄI1", "ÄI2", "ÄI3", "ÄI4", "ÄI5", "ÄI6", "ÄI7", "ÄI8", "ÄI9", "ÄI10", "ÄI11", "MO1", "MO2", "MO3", "MO4", "MO5", "MO6", "MO7", "MO8", "MO9", "MO10", "MO11", "ÄIS1", "ÄIS2", "ÄIS3", "ÄIS4", "ÄIS5", "ÄIS6", "ÄIS7", "ÄIS8", "ÄIS9", "ÄIS10", "ÄIS11", "ÄIR1", "ÄIR2", "ÄIR3", "ÄIR4", "ÄIR5", "ÄIR6", "ÄIR7", "ÄIR8", "ÄIR9", "ÄIR10", "ÄIR11", "ÄIV1", "ÄIV2", "ÄIV3", "ÄIV4", "ÄIV5", "ÄIV6", "ÄIV7", "ÄIV8", "ÄIV9", "ÄIV10", "ÄIV11", "S21", "S22", "S23", "S24", "S25", "S26", "S27", "S28", "S29", "S210", "S211", "SV21", "SV22", "SV23", "SV24", "SV25", "SV26", "SV27", "SV28", "SV29", "SV210", "SV211", "MOS1", "MOS2", "MOS3", "MOS4", "MOS5", "MOS6", "MOS7", "MOS8", "MOS9", "MOS10", "MOS11", "MOR1", "MOR2", "MOR3", "MOR4", "MOR5", "MOR6", "MOR7", "MOR8", "MOR9", "MOR10", "MOR11", "MOT1", "MOT2", "MOT3", "MOT4", "MOT5", "MOT6", "MOT7", "MOT8", "MOT9", "MOT10", "MOT11")

  def validate(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = {
    HttpStatus.fold(List(
      validateErityinenTutkinto(suoritus, parents),
      validateLukiodiplomiRakenne(suoritus, parents),
      validateLukiodiplomiLaajuus(suoritus),
      validateOppiaineenSuorituskieli(suoritus, parents),
      validateModuulinJaPaikallisenOpintojaksonSuorituskieli(suoritus, parents),
      validateModuulitPaikallisessaOppiaineessa(suoritus, parents),
      validateModuulitÄidinkielessä(suoritus, parents),
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
    case (_: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (_: MuidenLukioOpintojenSuoritus2019) :: _) |
         (_: PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (_: MuidenLukioOpintojenPreIBSuoritus2019) :: _)
      if parents.head.koulutusmoduuli.tunniste.koodiarvo == "LD" &&
        !lukiodiplomit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuoritus ${suorituksenTunniste(suoritus)} ei ole sallittu lukiodiplomisuoritus")
    case (_: LukionModuulinSuoritus2019, (_: LukionOppimääränOsasuoritus2019) :: _) |
         (_: PreIBLukionModuulinSuoritus2019, (_: PreIBLukionOsasuoritus2019) :: _)
      if parents.head.koulutusmoduuli.tunniste.koodiarvo != "LD" &&
        lukiodiplomit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) &&
        !lukiodiplomienSallitutOppiaineet.contains((parents.head.koulutusmoduuli.tunniste.koodiarvo, suoritus.koulutusmoduuli.tunniste.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Lukiodiplomimoduuli (${suorituksenTunniste(suoritus)}) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus.")
    case _ =>
      HttpStatus.ok
  }

  private def validateLukiodiplomiLaajuus(suoritus: Suoritus): HttpStatus = suoritus match {
    case _: LukionModuulinSuoritus2019 | _: PreIBLukionModuulinSuoritus2019
      if (lukiodiplomit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) && (suoritus.koulutusmoduuli.laajuusArvo(0.0) != 2.0)) =>
      KoskiErrorCategory.badRequest.validation.laajuudet.lukiodiplominLaajuusEiOle2Opintopistettä(s"Osasuorituksen ${suorituksenTunniste(suoritus)} laajuus ei ole oikea. Lukiodiplomimoduulin laajuus tulee olla aina 2 opintopistettä.")
    case _ =>
      HttpStatus.ok
  }

  private def validateOppiaineenSuorituskieli(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s: LukionOppiaineenSuoritus2019, (p: Suorituskielellinen) :: _)
      if s.suorituskieli.contains(p.suorituskieli) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Oppiaineen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli")
    case (s: LukionOppiaineenPreIBSuoritus2019, (p: Suorituskielellinen) :: _)
      if s.suorituskieli.contains(p.suorituskieli) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Oppiaineen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli")
    case _ =>
      HttpStatus.ok
  }

  private def validateModuulinJaPaikallisenOpintojaksonSuorituskieli(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (s: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MahdollisestiSuorituskielellinen) :: _)
      if s.suorituskieli.exists(p.suorituskieli.contains) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuorituksen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin oppiaineen suorituskieli")
    case (s: PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MahdollisestiSuorituskielellinen) :: _)
      if s.suorituskieli.exists(p.suorituskieli.contains) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuorituksen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin oppiaineen suorituskieli")
    case (s: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MahdollisestiSuorituskielellinen) :: (pp: Suorituskielellinen) :: _)
      if p.suorituskieli.isEmpty && s.suorituskieli.contains(pp.suorituskieli) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuorituksen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli")
    case (s: PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, (p: MahdollisestiSuorituskielellinen) :: (pp: Suorituskielellinen) :: _)
      if p.suorituskieli.isEmpty && s.suorituskieli.contains(pp.suorituskieli) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Osasuorituksen ${suorituksenTunniste(suoritus)} suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli")
    case _ =>
      HttpStatus.ok
  }

  private def validateModuulitPaikallisessaOppiaineessa(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (_: LukionModuulinSuoritus2019, (_ : LukionOppiaineenSuoritus2019) :: _) |
         (_: PreIBLukionModuulinSuoritus2019, (_ : LukionOppiaineenPreIBSuoritus2019) :: _)
      if parents.head.koulutusmoduuli.isInstanceOf[PaikallinenLukionOppiaine2019] =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Paikalliseen oppiaineeseen ${parents.head.koulutusmoduuli.tunniste} ei voi lisätä valtakunnallista moduulia ${suoritus.koulutusmoduuli.tunniste}. Paikallisessa oppiaineessa voi olla vain paikallisia opintojaksoja.")
    case _ =>
      HttpStatus.ok
  }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }

  private def validateModuulitÄidinkielessä(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = (suoritus, parents) match {
    case (_: LukionModuulinSuoritus2019, (_ : LukionOppiaineenSuoritus2019) :: _)
      if parents.head.koulutusmoduuli.isInstanceOf[LukionÄidinkieliJaKirjallisuus2019] && !äidinkielenSallitutModuulit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) =>
        KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Äidinkielen oppiaineeseen ei voi lisätä moduulia ${suoritus.koulutusmoduuli.tunniste}.")
    case _ =>
      HttpStatus.ok
  }
}
