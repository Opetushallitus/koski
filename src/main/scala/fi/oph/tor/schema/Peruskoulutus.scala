package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.arvosana.Arvosana
import fi.oph.tor.schema.generic.annotation.Description

@Description("Perusopetuksen opiskeluoikeus")
case class PeruskouluOpiskeluOikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  suoritukset: List[PeruskoulunPäättötodistus],
  opiskeluoikeudenTila: Option[OpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("peruskoulutus")
  tyyppi: KoodistoKoodiViite = KoodistoKoodiViite("peruskoulutus", Some("Peruskoulutus"), "opiskeluoikeudentyyppi", None)
) extends OpiskeluOikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
}

case class PeruskoulunPäättötodistus(
  @KoodistoKoodiarvo("peruskoulunpaattotodistus")
  tyyppi: KoodistoKoodiViite = KoodistoKoodiViite("peruskoulunpaattotodistus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Peruskoulutus,
  paikallinenId: Option[String],
  suorituskieli: Option[KoodistoKoodiViite],
  tila: KoodistoKoodiViite,
  alkamispäivä: Option[LocalDate],
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PeruskoulunArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  override val osasuoritukset: Option[List[PeruskoulunOppiaineSuoritus]]
) extends Suoritus

case class PeruskoulunOppiaineSuoritus(
  @KoodistoKoodiarvo("peruskoulunoppiainesuoritus")
  tyyppi: KoodistoKoodiViite = KoodistoKoodiViite(koodiarvo = "peruskoulunoppiainesuoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: PeruskoulunOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[KoodistoKoodiViite],
  tila: KoodistoKoodiViite,
  alkamispäivä: Option[LocalDate],
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PeruskoulunArviointi]] = None,
  vahvistus: Option[Vahvistus] = None
) extends Suoritus

@Description("Tutkintoon johtava koulutus")
case class Peruskoulutus(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("201100")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: KoodistoKoodiViite = KoodistoKoodiViite("201100", koodistoUri = "koulutus")
) extends Koulutusmoduuli

case class PeruskoulunArviointi(
  @KoodistoUri("arvosanat")
  arvosana: KoodistoKoodiViite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends Arviointi

trait PeruskoulunOppiaine extends Koulutusmoduuli {
  @Description("Peruskoulutuksen oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: KoodistoKoodiViite
}

  case class Oppiaine(
    tunniste: KoodistoKoodiViite
  ) extends PeruskoulunOppiaine

  case class Uskonto(
    @KoodistoKoodiarvo("KT")
    tunniste: KoodistoKoodiViite = KoodistoKoodiViite(koodiarvo = "KT", koodistoUri = "koskioppiaineetyleissivistava"),
    @Description("Mikä uskonto on kyseessä")
    @KoodistoUri("oppiaineuskonto")
    uskonto: KoodistoKoodiViite
  ) extends PeruskoulunOppiaine

  case class AidinkieliJaKirjallisuus(
    @KoodistoKoodiarvo("AI")
    tunniste: KoodistoKoodiViite = KoodistoKoodiViite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
    @Description("Mikä kieli on kyseessä")
    @KoodistoUri("oppiaineaidinkielijakirjallisuus")
    kieli: KoodistoKoodiViite
  ) extends PeruskoulunOppiaine
