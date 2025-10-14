package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{OnlyWhen, Title}

import java.time.LocalDate

@Title("Ammatillisten opintojen opiskeluoikeus")
case class SdgAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: schema.AmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[SdgAmmatillinenPäätasonSuoritus],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
) extends SdgOpiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): SdgAmmatillinenOpiskeluoikeus = {
    this.copy (
      suoritukset = suoritukset.collect { case s: SdgAmmatillinenPäätasonSuoritus => s }
    )
  }
}

trait SdgAmmatillinenPäätasonSuoritus extends Suoritus

@Title("Ammatillisen tutkinnon suoritus")
case class SdgAmmatillisenTutkinnonSuoritus(
  koulutusmoduuli: SdgAmmatillinenTutkintoKoulutus,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  toimipiste: Option[Toimipiste],
  suoritustapa: schema.Koodistokoodiviite,
  osaamisala: Option[List[schema.Osaamisalajakso]],
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  osasuoritukset: Option[List[SdgAmmatillisenTutkinnonOsasuoritus]],
  keskiarvo: Option[Double]
) extends SdgAmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgAmmatillisenTutkinnonSuoritus = this.copy(osasuoritukset = os.map(_.collect {
    case s: SdgAmmatillisenTutkinnonOsasuoritus => s
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
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Tutkinnon osat")
  osasuoritukset: Option[List[SdgAmmatillisenTutkinnonOsasuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  keskiarvo: Option[Double] = None,
  korotettuOpiskeluoikeusOid: Option[String] = None,
  korotettuKeskiarvo: Option[Double] = None,
) extends SdgAmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgAmmatillisenTutkinnonOsaTaiOsia = this.copy(osasuoritukset = os.map(_.collect {
    case s: SdgAmmatillisenTutkinnonOsasuoritus => s
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
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  suorituskieli: schema.Koodistokoodiviite,
  @Title("Tutkinnon osat")
  osasuoritukset: Option[List[SdgOsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus]],
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: schema.Koodistokoodiviite,
  keskiarvo: Option[Double] = None,
) extends SdgAmmatillinenPäätasonSuoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = this.copy(osasuoritukset = os.map(_.collect {
    case s: SdgOsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus => s
  }))
}

trait SdgAmmatillisenTutkinnonOsasuoritus extends Osasuoritus

trait SdgOsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus extends Osasuoritus

@Title("Yhteisen tutkinnon osan suoritus")
case class SdgYhteisenTutkinnonOsanSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinto: Option[schema.AmmatillinenTutkintoKoulutus],
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Boolean,
  @Title("Osa-alueet")
  osasuoritukset: Option[List[SdgYhteisenTutkinnonOsanOsaAlueenSuoritus]] = None,
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite
) extends SdgAmmatillisenTutkinnonOsasuoritus
  with SdgOsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus

@Title("Muun tutkinnon osan suoritus")
case class SdgMuunTutkinnonOsanSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinto: Option[schema.AmmatillinenTutkintoKoulutus],
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Boolean,
  suorituskieli: Option[schema.Koodistokoodiviite],
  tyyppi: schema.Koodistokoodiviite
) extends SdgAmmatillisenTutkinnonOsasuoritus
  with SdgOsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus

@Title("Korkeakouluopintoja")
case class SdgAmmatillinenKorkeakouluopintoja(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[SdgAmmatillinenKorkeakouluopintojenSuoritus]],
  tyyppi: schema.Koodistokoodiviite
) extends SdgAmmatillisenTutkinnonOsasuoritus

@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
case class SdgAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsa,
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osasuoritukset: Option[List[SdgYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus]] = None,
  tyyppi: schema.Koodistokoodiviite
) extends SdgAmmatillisenTutkinnonOsasuoritus

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
  tunnustettu: Boolean,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
)

trait SdgYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus {
  def koulutusmoduuli: schema.Koulutusmoduuli
  def arviointi: Option[List[SdgAmmatillinenArviointi]]
  def tunnustettu: Boolean
  def suorituskieli: Option[schema.Koodistokoodiviite]
  def tyyppi: schema.Koodistokoodiviite
}

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
case class SdgYhteisenTutkinnonOsanOsaAlueenSuoritus (
  @Title("Osa-alue")
  koulutusmoduuli: schema.AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Boolean,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite,
) extends SdgYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class SdgLukioOpintojenSuoritus(
  koulutusmoduuli: schema.PaikallinenLukionOpinto,
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Boolean,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends SdgYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

@Title("Muiden opintovalmiuksia tukevien opintojen suoritus")
case class SdgMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: schema.PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[SdgAmmatillinenArviointi]],
  tunnustettu: Boolean,
  suorituskieli: Option[schema.Koodistokoodiviite] = None,
  tyyppi: schema.Koodistokoodiviite
) extends SdgYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus

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
