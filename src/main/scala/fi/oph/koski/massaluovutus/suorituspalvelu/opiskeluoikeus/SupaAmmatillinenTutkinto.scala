package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{OsittaisenAmmatillisenTutkinnonOsanSuoritus, _}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, Scale, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MaxValue, MinValue, OnlyWhen, Title}

import java.time.LocalDate

@Title("Ammatillinen opiskeluoikeus")
@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class SupaAmmatillinenOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: AmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[SupaAmmatillinenPäätasonSuoritus],
) extends SupaOpiskeluoikeus

object SupaAmmatillinenTutkinto {
  def apply(oo: AmmatillinenOpiskeluoikeus, oppijaOid: String): SupaAmmatillinenOpiskeluoikeus =
    SupaAmmatillinenOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.collect {
        case s: AmmatillisenTutkinnonSuoritus => SupaAmmatillisenTutkinnonSuoritus(s)
        case s: TelmaKoulutuksenSuoritus => SupaTelmaKoulutuksenSuoritus(s)
        case s: AmmatillisenTutkinnonOsittainenSuoritus => SupaAmmatillisenTutkinnonOsittainenSuoritus(s)
      }
    )
}

trait SupaAmmatillinenPäätasonSuoritus extends SupaSuoritus

@Title("Ammatillisen tutkinnon suoritus")
@Description("Suoritettavan ammatillisen tutkinnon tiedot")
case class SupaAmmatillisenTutkinnonSuoritus(
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[SupaAmmatillisenTutkinnonOsasuoritus]],
  @Description("Tutkinnon suoritustapa (näyttö / ops / reformi). Ammatillisen perustutkinnon voi suorittaa joko opetussuunnitelmaperusteisesti tai näyttönä. Ammatillisen reformin (531/2017) mukaiset suoritukset välitetään suoritustavalla reformi. ")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  suoritustapa: Koodistokoodiviite,
  @Title("Painotettu keskiarvo")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double],
) extends SupaAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaAmmatillisenTutkinnonSuoritus {
  def apply(s: AmmatillisenTutkinnonSuoritus): SupaAmmatillisenTutkinnonSuoritus =
    SupaAmmatillisenTutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.map(SupaAmmatillisenTutkinnonOsasuoritus.apply)).filter(_.nonEmpty),
      suoritustapa = s.suoritustapa,
      keskiarvo = s.keskiarvo,
    )
}

trait SupaAmmatillisenTutkinnonOsasuoritus {
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: AmmatillisenTutkinnonOsa
  def arviointi: Option[List[AmmatillinenArviointi]]
}

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
@Description("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class SupaAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: Option[List[SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus]],
) extends SupaAmmatillisenTutkinnonOsasuoritus

trait SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus extends SupaSuoritus

object SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus {
  def apply(s: YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus): SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =
    s match {
      case ss: LukioOpintojenSuoritus => SupaLukioOpintojenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
      )
      case ss: MuidenOpintovalmiuksiaTukevienOpintojenSuoritus => SupaMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
      )
      case ss: YhteisenTutkinnonOsanOsaAlueenSuoritus => SupaYhteisenTutkinnonOsanOsaAlueenSuoritus(
        tyyppi = ss.tyyppi,
        koulutusmoduuli = ss.koulutusmoduuli,
        arviointi = ss.arviointi,
      )
    }
}

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class SupaLukioOpintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenlukionopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PaikallinenLukionOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Muiden opintovalmiuksia tukevien opintojen suoritus")
case class SupaMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenmuitaopintovalmiuksiatukeviaopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
@Description("Yhteisen tutkinnon osan osa-alueen suorituksen tiedot")
case class SupaYhteisenTutkinnonOsanOsaAlueenSuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

object SupaYhteisenTutkinnonOsanOsaAlueenSuoritus {
  def apply(s: YhteisenTutkinnonOsanOsaAlueenSuoritus): SupaYhteisenTutkinnonOsanOsaAlueenSuoritus =
    SupaYhteisenTutkinnonOsanOsaAlueenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}

@Title("Korkeakouluopintoja")
@Description("Korkeakouluopintoja")
case class SupaAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: Option[List[SupaKorkeakouluopintojenSuoritus]],
) extends SupaAmmatillisenTutkinnonOsasuoritus

@Title("Korkeakouluopintojen suoritus")
case class SupaKorkeakouluopintojenSuoritus(
  @KoodistoKoodiarvo("ammatillinenkorkeakouluopintoja")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]],
)

object SupaKorkeakouluopintojenSuoritus {
  def apply(s: KorkeakouluopintojenSuoritus): SupaKorkeakouluopintojenSuoritus = SupaKorkeakouluopintojenSuoritus(
    tyyppi = s.tyyppi,
    koulutusmoduuli = s.koulutusmoduuli,
    arviointi = s.arviointi,
  )
}

@Title("Muun tutkinnon osan suoritus")
@Description("Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus")
case class SupaMuunAmmatillisenTutkinnonOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: Option[List[SupaAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus]],
) extends SupaAmmatillisenTutkinnonOsasuoritus

@Title("Ammatillisen tutkinnon osaa pienempi kokonaisuus")
@Description("Muiden kuin yhteisten tutkinnon osien osasuoritukset")
case class SupaAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosaapienempikokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]],
)

object SupaAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus {
  def apply(s: AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus): SupaAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
    SupaAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}

@Title("Yhteisen tutkinnon osan suoritus")
@Description("Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus")
case class SupaYhteisenAmmatillisenTutkinnonOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  osasuoritukset: Option[List[SupaYhteisenTutkinnonOsanOsaAlueenSuoritus]],
) extends SupaAmmatillisenTutkinnonOsasuoritus

object SupaAmmatillisenTutkinnonOsasuoritus {
  def apply(s: AmmatillisenTutkinnonOsanSuoritus): SupaAmmatillisenTutkinnonOsasuoritus = {
    s match {
      case ss: AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
        SupaAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.map(_.map(
            SupaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus.apply
          )).filter(_.nonEmpty),
        )
      case ss: AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
        SupaAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.map(_.map(SupaKorkeakouluopintojenSuoritus.apply)).filter(_.nonEmpty)
        )
      case ss: MuunAmmatillisenTutkinnonOsanSuoritus =>
        SupaMuunAmmatillisenTutkinnonOsanSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.map(_.map(SupaAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus.apply)).filter(_.nonEmpty)
        )
      case ss: YhteisenAmmatillisenTutkinnonOsanSuoritus =>
        SupaYhteisenAmmatillisenTutkinnonOsanSuoritus(
          tyyppi = ss.tyyppi,
          koulutusmoduuli = ss.koulutusmoduuli,
          arviointi = ss.arviointi,
          osasuoritukset = ss.osasuoritukset.map(_.map(SupaYhteisenTutkinnonOsanOsaAlueenSuoritus.apply)).filter(_.nonEmpty)
        )
    }
  }
}

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
@Description("Yhteisen tutkinnon osan osa-alueen suorituksen tiedot")
case class SupaYhteisenTutkinnonOsanOsasuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SupaSuoritus

object SupaYhteisenTutkinnonOsanOsasuoritus {
  def apply(s: YhteisenTutkinnonOsanOsaAlueenSuoritus): SupaYhteisenTutkinnonOsanOsasuoritus =
    SupaYhteisenTutkinnonOsanOsasuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}
