package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid] = None,
  tila: Option[AmmatillinenOpiskeluoikeudenTila] = None,
  läsnäolotiedot: Option[Läsnäolotiedot] = None,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Henkilövahvistus] = None,
  @Description("Työhön ja itsenäiseen elämään valmentava koulutuksen osasuoritukset")
  override val osasuoritukset: Option[List[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TyöhönJaItsenäiseenElämäänValmentavaKoulutus
) extends Suoritus {
  def arviointi = None
}

case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus(
  paikallinenId: Option[String] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telmakoulutuksenosa", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa,
  arviointi: Option[List[TelmaArviointi]]
) extends Suoritus {
  override def osasuoritukset = None
}

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class TyöhönJaItsenäiseenElämäänValmentavaKoulutus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("999903")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999903", koodistoUri = "koulutus"),
  laajuus: Option[Laajuus] = None
) extends KoodistostaLöytyväKoulutusmoduuli

@Description("Työhön ja itsenäiseen elämään valmentava koulutuksen osa")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusOsaamispisteissä],
  @Description("Onko pakollinen osa tutkinnossa")
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli

case class TelmaArviointi(
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends KoodistostaLöytyväArviointi with ArviointiPäivämäärällä with SanallinenArviointi {
  override def hyväksytty = arvosana.koodiarvo match {
    case "0" => false
    case "Hylätty" => false
    case _ => true
  }
}
