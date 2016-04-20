package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.localization.LocalizedString._
import fi.oph.tor.localization.LocalizedStringImplicits._
import fi.oph.tor.schema.generic.annotation.{Description, MaxItems, MinItems}

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
  opiskeluoikeudenTila: Option[YleissivistäväOpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukiokoulutus", Some("Lukiokoulutus"), "opiskeluoikeudentyyppi", None)
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
  @KoodistoKoodiarvo("lukionoppiainesuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiainesuoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukionOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends Oppiaineensuoritus

case class LukionKurssinSuoritus(
  @KoodistoKoodiarvo("lukionkurssisuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssisuoritus", koodistoUri = "suorituksentyyppi"),
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
}

  case class ValtakunnallinenLukionKurssi(
    @Description("Lukion kurssi")
    @KoodistoUri("lukionkurssit")
    @OksaUri("tmpOKSAID873", "kurssi")
    tunniste: Koodistokoodiviite,
    override val laajuus: Option[Laajuus]
  ) extends LukionKurssi with KoodistostaLöytyväKoulutusmoduuli

  case class PaikallinenLukionKurssi(
    tunniste: Paikallinenkoodi,
    override val laajuus: Option[Laajuus]
  ) extends LukionKurssi with PaikallinenKoulutusmoduuli


case class Ylioppilastutkinto(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("301000")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with EPerusteistaLöytyväKoulutusmoduuli

case class LukionMatematiikka(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[Laajuus] = None
) extends LukionOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description = oppimäärä.description
}
