package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, Scale, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MaxValue, MinValue, OnlyWhen, Title}

import java.time.LocalDate

@Title("Ammatillinen opiskeluoikeus")
@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class SureAmmatillinenOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: AmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[SureAmmatillinenPäätasonSuoritus],
) extends SureOpiskeluoikeus

object SureAmmatillinenTutkinto {
  def apply(oo: AmmatillinenOpiskeluoikeus): SureAmmatillinenOpiskeluoikeus =
    SureAmmatillinenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.collect {
        case s: AmmatillisenTutkinnonSuoritus => SureAmmatillisenTutkinnonSuoritus(s)
        case s: TelmaKoulutuksenSuoritus => SureTelmaKoulutuksenSuoritus(s)
      }
    )
}

trait SureAmmatillinenPäätasonSuoritus extends SureSuoritus

@Title("Ammatillisen tutkinnon suoritus")
@Description("Suoritettavan ammatillisen tutkinnon tiedot")
case class SureAmmatillisenTutkinnonSuoritus(
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SureAmmatillisenTutkinnonOsasuoritus],
  @Description("Tutkinnon suoritustapa (näyttö / ops / reformi). Ammatillisen perustutkinnon voi suorittaa joko opetussuunnitelmaperusteisesti tai näyttönä. Ammatillisen reformin (531/2017) mukaiset suoritukset välitetään suoritustavalla reformi. ")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  suoritustapa: Koodistokoodiviite,
  @Title("Painotettu keskiarvo")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double],
) extends SureAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureAmmatillisenTutkinnonSuoritus {
  def apply(s: AmmatillisenTutkinnonSuoritus): SureAmmatillisenTutkinnonSuoritus =
    SureAmmatillisenTutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SureAmmatillisenTutkinnonOsasuoritus.apply),
      suoritustapa = s.suoritustapa,
      keskiarvo = s.keskiarvo,
    )
}

trait SureAmmatillisenTutkinnonOsasuoritus {
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: AmmatillisenTutkinnonOsa
  def arviointi: Option[List[AmmatillinenArviointi]]
}

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
@Description("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class SureAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus],
) extends SureAmmatillisenTutkinnonOsasuoritus

trait SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus extends SureSuoritus

object SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus {
  def apply(s: YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus): SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =
    s match {
      case ss: LukioOpintojenSuoritus => SureLukioOpintojenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
      )
      case ss: MuidenOpintovalmiuksiaTukevienOpintojenSuoritus => SureMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
      )
      case ss: YhteisenTutkinnonOsanOsaAlueenSuoritus => SureYhteisenTutkinnonOsanOsaAlueenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
      )
    }
}

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class SureLukioOpintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenlukionopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PaikallinenLukionOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Muiden opintovalmiuksia tukevien opintojen suoritus")
case class SureMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenmuitaopintovalmiuksiatukeviaopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
@Description("Yhteisen tutkinnon osan osa-alueen suorituksen tiedot")
case class SureYhteisenTutkinnonOsanOsaAlueenSuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

object SureYhteisenTutkinnonOsanOsaAlueenSuoritus {
  def apply(s: YhteisenTutkinnonOsanOsaAlueenSuoritus): SureYhteisenTutkinnonOsanOsaAlueenSuoritus =
    SureYhteisenTutkinnonOsanOsaAlueenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}

@Title("Korkeakouluopintoja")
@Description("Korkeakouluopintoja")
case class SureAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SureKorkeakouluopintojenSuoritus],
) extends SureAmmatillisenTutkinnonOsasuoritus

@Title("Korkeakouluopintojen suoritus")
case class SureKorkeakouluopintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenkorkeakouluopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]],
)

object SureKorkeakouluopintojenSuoritus {
  def apply(s: KorkeakouluopintojenSuoritus): SureKorkeakouluopintojenSuoritus = SureKorkeakouluopintojenSuoritus(
    tyyppi = s.tyyppi,
    koulutusmoduuli = s.koulutusmoduuli,
    arviointi = s.arviointi,
  )
}

@Title("Muun tutkinnon osan suoritus")
@Description("Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus")
case class SureMuunAmmatillisenTutkinnonOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SureAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus],
) extends SureAmmatillisenTutkinnonOsasuoritus

@Title("Ammatillisen tutkinnon osaa pienempi kokonaisuus")
@Description("Muiden kuin yhteisten tutkinnon osien osasuoritukset")
case class SureAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosaapienempikokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]],
)

object SureAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus {
  def apply(s: AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus): SureAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
    SureAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}

@Title("Yhteisen tutkinnon osan suoritus")
@Description("Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus")
case class SureYhteisenAmmatillisenTutkinnonOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: List[SureYhteisenTutkinnonOsanOsaAlueenSuoritus],
) extends SureAmmatillisenTutkinnonOsasuoritus

object SureAmmatillisenTutkinnonOsasuoritus {
  def apply(s: AmmatillisenTutkinnonOsanSuoritus): SureAmmatillisenTutkinnonOsasuoritus = {
    s match {
      case ss: AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
        SureAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.toList.flatten.map(SureYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus.apply),
        )
      case ss: AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
        SureAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.toList.flatten.map(SureKorkeakouluopintojenSuoritus.apply)
        )
      case ss: MuunAmmatillisenTutkinnonOsanSuoritus =>
        SureMuunAmmatillisenTutkinnonOsanSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.toList.flatten.map(SureAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus.apply)
        )
      case ss: YhteisenAmmatillisenTutkinnonOsanSuoritus =>
        SureYhteisenAmmatillisenTutkinnonOsanSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.toList.flatten.map(SureYhteisenTutkinnonOsanOsaAlueenSuoritus.apply)
        )
    }
  }
}

@Title("TELMA-koulutuksen suoritus")
@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class SureTelmaKoulutuksenSuoritus(
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: TelmaKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SureTelmaKoulutuksenOsanSuoritus],
) extends SureAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureTelmaKoulutuksenSuoritus {
  def apply(s: TelmaKoulutuksenSuoritus): SureTelmaKoulutuksenSuoritus =
    SureTelmaKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SureTelmaKoulutuksenOsanSuoritus.apply),
    )
}

@Title("TELMA-koulutuksen osan suoritus")
@Description("Suoritettavan TELMA-koulutuksen osan tiedot")
case class SureTelmaKoulutuksenOsanSuoritus(
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: TelmaKoulutuksenOsa,
  arviointi: Option[List[TelmaJaValmaArviointi]],
) extends SureSuoritus

object SureTelmaKoulutuksenOsanSuoritus {
  def apply(s: TelmaKoulutuksenOsanSuoritus): SureTelmaKoulutuksenOsanSuoritus =
    SureTelmaKoulutuksenOsanSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi
    )
}

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
@Description("Yhteisen tutkinnon osan osa-alueen suorituksen tiedot")
case class SureYhteisenTutkinnonOsanOsasuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SureSuoritus

object SureYhteisenTutkinnonOsanOsasuoritus {
  def apply(s: YhteisenTutkinnonOsanOsaAlueenSuoritus): SureYhteisenTutkinnonOsanOsasuoritus =
    SureYhteisenTutkinnonOsanOsasuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}
