package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.localization.LocalizedString.{finnish, concat}

@Description("Lukion opiskeluoikeus")
case class LukionOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  @MinItems(1) @MaxItems(1)
  suoritukset: List[LukionOppimääränSuoritus],
  tila: Option[YleissivistäväOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukiokoulutus", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))

}

case class LukionOppimääränSuoritus(
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  koulutusmoduuli: Ylioppilastutkinto,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  override val osasuoritukset: Option[List[LukionOppiaineenSuoritus]]
) extends Suoritus

case class LukionOppiaineenSuoritus(
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukionOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends Oppiaineensuoritus

case class LukionKurssinSuoritus(
  @KoodistoKoodiarvo("lukionkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssi", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukionKurssi,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None
) extends Suoritus {
  def vahvistus: Option[Vahvistus] = None
}

sealed trait LukionKurssi extends Koulutusmoduuli {
  def pakollinen: Boolean = false
  def laajuus: Option[LaajuusKursseissa]
}

  case class ValtakunnallinenLukionKurssi(
    @Description("Lukion kurssi")
    @KoodistoUri("lukionkurssit")
    @OksaUri("tmpOKSAID873", "kurssi")
    tunniste: Koodistokoodiviite,
    override val laajuus: Option[LaajuusKursseissa]
  ) extends LukionKurssi with KoodistostaLöytyväKoulutusmoduuli

  case class PaikallinenLukionKurssi(
    tunniste: Paikallinenkoodi,
    override val laajuus: Option[LaajuusKursseissa]
  ) extends LukionKurssi with PaikallinenKoulutusmoduuli


case class Ylioppilastutkinto(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("301000")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
}


trait LukionOppiaine extends YleissivistavaOppiaine {
  def laajuus: Option[LaajuusKursseissa]
}

case class MuuOppiaine(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine

case class Uskonto(
  @KoodistoKoodiarvo("KT")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "KT", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä uskonto on kyseessä")
  @KoodistoUri("oppiaineuskonto")
  uskonto: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine {
  override def description = concat(nimi, ", ", uskonto)
}

case class AidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine

case class VierasTaiToinenKotimainenKieli(
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
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine {
  override def description = concat(nimi, ", ", kieli)
}

case class LukionMatematiikka(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description = oppimäärä.description
}

case class LaajuusKursseissa(
  arvo: Float,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "4", nimi = Some(finnish("kurssia")))
) extends Laajuus
