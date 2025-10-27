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
case class AmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  toimipiste: Option[Toimipiste],
  suoritustapa: schema.Koodistokoodiviite,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  osasuoritukset: Option[List[AmmatillisenTutkinnonOsasuoritus]],
  keskiarvo: Option[Double]
) extends AmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): AmmatillisenTutkinnonSuoritus = this.copy(osasuoritukset = os.map(_.collect {
    case s: AmmatillisenTutkinnonOsasuoritus => s
  }))
}

@Title("Ammatillisen tutkinnon osa/osia")
case class AmmatillisenTutkinnonOsaTaiOsia(
  @Title("Koulutus")
  koulutusmoduuli: schema.AmmatillinenTutkintoKoulutus, // Pitäisikö olla EQF NQF mukana?
  suoritustapa: schema.Koodistokoodiviite,
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  toinenTutkintonimike: Boolean,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  toinenOsaamisala: Boolean,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Tutkinnon osat")
  osasuoritukset: Option[List[AmmatillisenTutkinnonOsasuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  keskiarvo: Option[Double] = None,
  korotettuOpiskeluoikeusOid: Option[String] = None,
  korotettuKeskiarvo: Option[Double] = None,
) extends AmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): AmmatillisenTutkinnonOsaTaiOsia = this.copy(osasuoritukset = os.map(_.collect {
    case s: AmmatillisenTutkinnonOsasuoritus => s
  }))
}

@Title("Ammatillisen tutkinnon osia useasta tutkinnosta")
@OnlyWhen("koulutusmoduuli/tunniste/koodiarvo", "ammatillinentutkintoosittainenuseastatutkinnosta")
case class AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: schema.AmmatillinenOsiaUseastaTutkinnosta,
  suoritustapa: schema.Koodistokoodiviite,
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  toinenTutkintonimike: Boolean,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  toinenOsaamisala: Boolean,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Tutkinnon osat")
  osasuoritukset: Option[List[OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus]],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  keskiarvo: Option[Double] = None,
) extends AmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = this.copy(osasuoritukset = os.map(_.collect {
    case s: OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus => s
  }))
}

trait AmmatillisenTutkinnonOsasuoritus extends Osasuoritus

trait OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus extends Osasuoritus with WithTunnustettuBoolean

@Title("Yhteisen tutkinnon osan suoritus")
case class YhteisenTutkinnonOsanSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinto: Option[schema.AmmatillinenTutkintoKoulutus],
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[AmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  @Title("Osa-alueet")
  osasuoritukset: Option[List[YhteisenTutkinnonOsanOsaAlueenSuoritus]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus
  with OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus

@Title("Muun tutkinnon osan suoritus")
case class MuunTutkinnonOsanSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinto: Option[schema.AmmatillinenTutkintoKoulutus],
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[AmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus
  with OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus

@Title("Korkeakouluopintoja")
case class AmmatillinenKorkeakouluopintoja(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[AmmatillinenKorkeakouluopintojenSuoritus]],
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus]] = None,
  tyyppi: schema.Koodistokoodiviite
) extends AmmatillisenTutkinnonOsasuoritus

@Title("AmmatillinenArviointi")
case class AmmatillinenArviointi (
  arvosana: schema.Koodistokoodiviite,
  päivä: LocalDate,
  hyväksytty: Boolean
)

@Title("KorkeakouluopintojenSuoritus")
case class AmmatillinenKorkeakouluopintojenSuoritus(
  @Title("Kokonaisuus")
  koulutusmoduuli: schema.KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends WithTunnustettuBoolean

trait YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus extends WithTunnustettuBoolean {
  def koulutusmoduuli: schema.Koulutusmoduuli
  def arviointi: Option[List[AmmatillinenArviointi]]
  def tunnustettu: Option[schema.OsaamisenTunnustaminen]
  def suorituskieli: Option[schema.Koodistokoodiviite]
  def tyyppi: schema.Koodistokoodiviite
}

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
case class YhteisenTutkinnonOsanOsaAlueenSuoritus (
  @Title("Osa-alue")
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite,
) extends YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class LukioOpintojenSuoritus(
  koulutusmoduuli: schema.PaikallinenLukionOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Muiden opintovalmiuksia tukevien opintojen suoritus")
case class MuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: schema.PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[AmmatillinenArviointi]],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Ammatillinen tutkintokoulutus")
case class AmmatillinenTutkintoKoulutus(
  tunniste: schema.Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite],
) extends schema.Koulutusmoduuli {
  override def nimi = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

case class OsaamisenHankkimistapa (
  tunniste: schema.Koodistokoodiviite,
)

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: OsaamisenHankkimistapa
)

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  paikkakunta: schema.Koodistokoodiviite,
  maa: schema.Koodistokoodiviite
)
