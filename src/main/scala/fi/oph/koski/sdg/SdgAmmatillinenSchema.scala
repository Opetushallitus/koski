package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{OnlyWhen, Title}

import java.time.LocalDate

@Title("Ammatillisten opintojen opiskeluoikeus")
case class AmmatillinenOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[AmmatillinenPäätasonSuoritus],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): AmmatillinenOpiskeluoikeus = {
    this.copy (
      suoritukset = suoritukset.collect { case s: AmmatillinenPäätasonSuoritus => s }
    )
  }
}

trait AmmatillinenPäätasonSuoritus extends Suoritus

@Title("Ammatillisen tutkinnon suoritus")
case class SdgAmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: SdgAmmatillinenTutkintoKoulutus,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  toimipiste: Option[SdgToimipiste],
  suoritustapa: schema.Koodistokoodiviite,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  osasuoritukset: Option[List[AmmatillisenTutkinnonOsasuoritus]],
  keskiarvo: Option[Double]
) extends AmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgAmmatillisenTutkinnonSuoritus = this.copy(osasuoritukset = os.map(_.collect {
    case s: AmmatillisenTutkinnonOsasuoritus => s
  }))
}

@Title("Ammatillisen tutkinnon osa/osia")
case class SdgAmmatillisenTutkinnonOsaTaiOsia(
  @Title("Koulutus")
  koulutusmoduuli: schema.AmmatillinenTutkintoKoulutus, // Pitäisikö olla EQF NQF mukana?
  suoritustapa: schema.Koodistokoodiviite,
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  toinenTutkintonimike: Boolean,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  toinenOsaamisala: Boolean,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Tutkinnon osat")
  osasuoritukset: Option[List[AmmatillisenTutkinnonOsasuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  keskiarvo: Option[Double] = None,
  korotettuOpiskeluoikeusOid: Option[String] = None,
  korotettuKeskiarvo: Option[Double] = None,
) extends AmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgAmmatillisenTutkinnonOsaTaiOsia = this.copy(osasuoritukset = os.map(_.collect {
    case s: AmmatillisenTutkinnonOsasuoritus => s
  }))
}

@Title("Ammatillisen tutkinnon osia useasta tutkinnosta")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "ammatillinentutkintoosittainenuseastatutkinnosta")
case class SdgAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: schema.AmmatillinenOsiaUseastaTutkinnosta,
  suoritustapa: schema.Koodistokoodiviite,
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  toinenTutkintonimike: Boolean,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  toinenOsaamisala: Boolean,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Tutkinnon osat")
  osasuoritukset: Option[List[OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus]],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  keskiarvo: Option[Double] = None,
) extends AmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = this.copy(osasuoritukset = os.map(_.collect {
    case s: OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus => s
  }))
}

trait AmmatillisenTutkinnonOsasuoritus extends Osasuoritus

trait OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus extends Osasuoritus with WithTunnustettuBoolean

@Title("Yhteisen tutkinnon osan suoritus")
case class SdgYhteisenTutkinnonOsanSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinto: Option[schema.AmmatillinenTutkintoKoulutus],
  @KoodistoKoodiarvo("2") // Yhteiset tutkinnon osat
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  @Title("Osa-alueet")
  osasuoritukset: Option[List[SdgYhteisenTutkinnonOsanOsaAlueenSuoritus]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite],
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus
  with OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus

@Title("Muun tutkinnon osan suoritus")
case class MuunTutkinnonOsanSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinto: Option[schema.AmmatillinenTutkintoKoulutus],
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  @KoodistoKoodiarvo("3") // Vapaavalintaiset tutkinnon osat
  @KoodistoKoodiarvo("4") // Tutkintoa yksilöllisesti laajentavat tutkinnon osat
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus
  with OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus

@Title("Korkeakouluopintoja")
@OnlyWhen("../../suoritustapa/koodiarvo", "reformi")
case class SdgAmmatillinenKorkeakouluopintoja(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[SdgAmmatillinenKorkeakouluopintojenSuoritus]],
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class SdgAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus]] = None,
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus

@Title("AmmatillinenArviointi")
case class SdgAmmatillinenArviointi (
  arvosana: schema.Koodistokoodiviite,
  päivä: LocalDate,
  hyväksytty: Boolean
)

@Title("KorkeakouluopintojenSuoritus")
case class SdgAmmatillinenKorkeakouluopintojenSuoritus(
  @Title("Kokonaisuus")
  koulutusmoduuli: schema.KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[SdgAmmatillinenArviointi]] = None,
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends WithTunnustettuBoolean

trait YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus extends WithTunnustettuBoolean {
  def koulutusmoduuli: schema.Koulutusmoduuli
  def arviointi: Option[List[SdgAmmatillinenArviointi]]
  def tunnustettu: Option[schema.OsaamisenTunnustaminen]
  def suorituskieli: Option[schema.Koodistokoodiviite]
  def tyyppi: schema.Koodistokoodiviite
}

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
case class SdgYhteisenTutkinnonOsanOsaAlueenSuoritus (
  @Title("Osa-alue")
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite,
) extends YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class SdgLukioOpintojenSuoritus(
  koulutusmoduuli: schema.PaikallinenLukionOpinto,
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Muiden opintovalmiuksia tukevien opintojen suoritus")
case class SdgMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: schema.PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Ammatillinen tutkintokoulutus")
case class SdgAmmatillinenTutkintoKoulutus(
  tunniste: schema.Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite],
) extends schema.Koulutusmoduuli {
  override def nimi = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

case class SdgOsaamisenHankkimistapa (
  tunniste: schema.Koodistokoodiviite,
)

case class SdgOsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: SdgOsaamisenHankkimistapa
)

case class SdgKoulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  paikkakunta: schema.Koodistokoodiviite,
  maa: schema.Koodistokoodiviite
)
