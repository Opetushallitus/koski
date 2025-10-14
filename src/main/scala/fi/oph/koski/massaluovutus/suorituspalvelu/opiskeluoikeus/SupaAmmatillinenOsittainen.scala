package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, Scale, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MaxValue, MinValue, OnlyWhen, Title}

import java.time.LocalDate

trait SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus {
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: AmmatillisenTutkinnonOsa
  def arviointi: Option[List[AmmatillinenArviointi]]
}

trait SupaOsittaisenJatkovalmiudetOsasuoritus extends SupaSuoritus

@Title("Ammatillisen tutkinnon osa/osia")
case class SupaAmmatillisenTutkinnonOsittainenSuoritus(
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  suoritustapa: Koodistokoodiviite,
  @MinValue(1) @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double],
  @Title("Korotettu painotettu keskiarvo")
  @MinValue(1) @MaxValue(5)
  @Scale(2)
  korotettuKeskiarvo: Option[Double],
  @Title("Korotetun suorituksen alkuperäinen opiskeluoikeus")
  @Description("Korotetun suorituksen alkuperäinen opiskeluoikeus")
  korotettuOpiskeluoikeusOid: Option[String]
) extends SupaAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaAmmatillisenTutkinnonOsittainenSuoritus {
  def apply(s: AmmatillisenTutkinnonOsittainenSuoritus): SupaAmmatillisenTutkinnonOsittainenSuoritus =
    SupaAmmatillisenTutkinnonOsittainenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SupaOsittaisenAmmatillisenTutkinnonOsanSuoritusmapper.apply),
      suoritustapa = s.suoritustapa,
      keskiarvo = s.keskiarvo,
      korotettuKeskiarvo = s.korotettuKeskiarvo,
      korotettuOpiskeluoikeusOid = s.korotettuOpiskeluoikeusOid
    )
}

@Title("Yhteinen tutkinnon osan suoritus")
case class SupaOsittaisenYhteisenAmmatillisenTutkinnonOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus]
) extends SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus

@Title("Muun tutkinnon osan suoritus")
case class SupaOsittaisenMuunAmmatillisenTutkinnonOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]]
) extends SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class SupaOsittaisenJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SupaOsittaisenJatkovalmiudetOsasuoritus]
) extends SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus

@Title("Korkeakouluopintoja")
case class SupaOsittaisenKorkeakouluopintoSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SupaKorkeakouluopintojenSuoritus]
) extends SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
  korotettu: Option[Koodistokoodiviite]
) extends SupaOsittaisenJatkovalmiudetOsasuoritus

@Title("Muiden opintovalmiuksia tukevien opintojen suoritus")
case class SupaOsittaisenMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenmuitaopintovalmiuksiatukeviaopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
  korotettu: Option[Koodistokoodiviite]
) extends SupaOsittaisenJatkovalmiudetOsasuoritus

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class SupaOsittaisenLukioOpintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenlukionopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PaikallinenLukionOpinto,
  arviointi: Option[List[AmmatillinenArviointi]]
) extends SupaOsittaisenJatkovalmiudetOsasuoritus

private object OsittainenJatkoOpintovalmiuksiaOsasuoritusMapper {
  def apply(s: YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
           ): SupaOsittaisenJatkovalmiudetOsasuoritus = s match {
    case ss: LukioOpintojenSuoritus =>
      SupaOsittaisenLukioOpintojenSuoritus(ss.tyyppi, ss.koulutusmoduuli, ss.arviointi)
    case ss: MuidenOpintovalmiuksiaTukevienOpintojenSuoritus =>
      SupaOsittaisenMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(ss)
    case ss: YhteisenTutkinnonOsanOsaAlueenSuoritus =>
      SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus(ss)
  }
}

object SupaOsittaisenMuidenOpintovalmiuksiaTukevienOpintojenSuoritus {
  def apply(s: MuidenOpintovalmiuksiaTukevienOpintojenSuoritus)
  : SupaOsittaisenMuidenOpintovalmiuksiaTukevienOpintojenSuoritus =
    SupaOsittaisenMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
      korotettu = s.korotettu
    )
}

object SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus {
  def apply(s: YhteisenTutkinnonOsanOsaAlueenSuoritus): SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus =
    SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
      korotettu = s.korotettu
    )
}

private object SupaOsittaisenAmmatillisenTutkinnonOsanSuoritusmapper {
  def apply(s: OsittaisenAmmatillisenTutkinnonOsanSuoritus): SupaOsittaisenAmmatillisenTutkinnonOsanSuoritus = s match {
    case ss: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =>
      SupaOsittaisenYhteisenAmmatillisenTutkinnonOsanSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
        osasuoritukset = ss.osasuoritukset.toList.flatten.map(SupaOsittaisenYhteisenTutkinnonOsanOsaAlueenSuoritus.apply)
      )

    case ss: MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =>
      SupaOsittaisenMuunAmmatillisenTutkinnonOsanSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointidd
      )

    case ss: OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
      SupaOsittaisenJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = None,
        osasuoritukset = ss.osasuoritukset.toList.flatten.map(
          OsittainenJatkoOpintovalmiuksiaOsasuoritusMapper.apply
        )
      )

    case ss: OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
      SupaOsittaisenKorkeakouluopintoSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = None,
        osasuoritukset = ss.osasuoritukset.toList.flatten.map(SupaKorkeakouluopintojenSuoritus.apply)
      )
  }
}
