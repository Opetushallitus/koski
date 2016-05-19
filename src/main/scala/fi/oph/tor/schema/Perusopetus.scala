package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{MaxValue, MinValue, Description}
import fi.oph.tor.localization.LocalizedString.{concat, finnish}

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  @Description("Onko tavoitteena perusopetuksen koko oppimäärän vai yksittäisen oppiaineen suoritus")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tavoite: Koodistokoodiviite,
  suoritukset: List[PerusopetuksenPäätasonSuoritus],
  tila: Option[YleissivistäväOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("perusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetus", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

trait PerusopetuksenPäätasonSuoritus extends Suoritus

case class PerusopetuksenVuosiluokanSuoritus(
  @Description("Luokkaaste numeroin")
  @MinValue(1)
  @MaxValue(9)
  luokkaAste: Int,
  @Description("Luokan tunniste, esimerkiksi 9C")
  luokka: String,
  override val alkamispäivä: Option[LocalDate],
  paikallinenId: Option[String],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  suorituskieli: Option[Koodistokoodiviite],
  koulutusmoduuli: Perusopetus,
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenvuosiluokka", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus {
  override def arviointi = None
  override def vahvistus = None
  override def tarvitseeVahvistuksen = false
}

@Description("Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.")
case class PerusopetuksenOppimääränSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Vahvistus] = None,
  koulutusmoduuli: Perusopetus,
  @KoodistoUri("perusopetuksenoppimaara")
  oppimäärä: Koodistokoodiviite,
  @KoodistoUri("perusopetuksensuoritustapa")
  suoritustapa: Koodistokoodiviite,
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppimaara", koodistoUri = "suorituksentyyppi"),
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  override val osasuoritukset: Option[List[PerusopetuksenOppiaineenSuoritus]] = None
) extends PerusopetuksenPäätasonSuoritus {
  def arviointi: Option[List[Arviointi]] = None
}

@Description("Perusopetuksen yksittäisen oppiaineen oppimäärän suoritus erillisenä kokonaisuutena")
case class PerusopetuksenOppiaineenOppimääränSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  override val vahvistus: Option[Vahvistus] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: PerusopetuksenOppiaine,
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None
) extends PerusopetuksenPäätasonSuoritus with OppiaineenSuoritus

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta")
case class PerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @KoodistoKoodiarvo("perusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None
) extends OppiaineenSuoritus

@Description("Perusopetus")
case class Perusopetus(
 perusteenDiaarinumero: Option[String],
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("201101")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus")
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = true
}

trait PerusopetuksenOppiaine extends YleissivistavaOppiaine {
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

case class MuuPeruskoulunOppiaine(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine

case class PeruskoulunUskonto(
  @KoodistoKoodiarvo("KT")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "KT", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä uskonto on kyseessä")
  @KoodistoUri("oppiaineuskonto")
  uskonto: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine {
  override def description = concat(nimi, ", ", uskonto)
}

case class PeruskoulunAidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine

case class PeruskoulunVierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenOppiaine {
  override def description = concat(nimi, ", ", kieli)
}

case class LaajuusVuosiviikkotunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko")
) extends Laajuus
