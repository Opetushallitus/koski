package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString._
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, Discriminator, Title}

trait Koulutusmoduuli extends Localized {
  @Representative
  @Discriminator
  def tunniste: KoodiViite
  def nimi: LocalizedString
  def description: LocalizedString = nimi
  def isTutkinto = false
  // Vertailutekijä tarkistettaessa duplikaatteja
  def identiteetti: AnyRef = tunniste
  def getLaajuus: Option[Laajuus] = None
  def laajuusArvo(default: Double): Double =
    getLaajuus.map(_.arvo).getOrElse(default)
}

trait KoulutusmoduuliPakollinenLaajuus extends Koulutusmoduuli {
  def laajuus: Laajuus
  override def getLaajuus: Option[Laajuus] = Some(laajuus)
}

trait KoulutusmoduuliPakollinenLaajuusOpintopisteissä extends KoulutusmoduuliPakollinenLaajuus {
  def laajuus: LaajuusOpintopisteissä
}

trait KoulutusmoduuliPakollinenLaajuusVuosiviikkotunneissa extends KoulutusmoduuliPakollinenLaajuus {
  def laajuus: LaajuusVuosiviikkotunneissa
}

trait KoulutusmoduuliValinnainenLaajuus extends Koulutusmoduuli {
  def laajuus: Option[Laajuus]
  override def getLaajuus: Option[Laajuus] = laajuus
}

trait KoodistostaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Koodistokoodiviite
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

trait KoodistostaLöytyväKoulutusmoduuliValinnainenLaajuus extends KoodistostaLöytyväKoulutusmoduuli with KoulutusmoduuliValinnainenLaajuus
trait KoodistostaLöytyväKoulutusmoduuliPakollinenLaajuus extends KoodistostaLöytyväKoulutusmoduuli with KoulutusmoduuliPakollinenLaajuus

trait KoulutustyypinSisältäväKoulutusmoduuli extends KoodistostaLöytyväKoulutusmoduuli {
  @KoodistoUri("koulutustyyppi")
  @Hidden
  @ReadOnly("Koulutustyypin koodia ei tarvita syöttövaiheessa; Koski päättelee sen automaattisesti koulutuskoodin perusteella.")
  def koulutustyyppi: Option[Koodistokoodiviite]
}

trait Koulutus extends KoulutustyypinSisältäväKoulutusmoduuli {
  @Description("Tutkinnon 6-numeroinen tutkintokoodi. Sama kuin tilastokeskuksen koulutuskoodi")
  @KoodistoUri("koulutus")
  @OksaUri("tmpOKSAID560", "tutkinto")
  def tunniste: Koodistokoodiviite
  def koulutustyyppi: Option[Koodistokoodiviite]
}

trait Diaarinumerollinen {
  @Description("Tutkinnon perusteen diaarinumero. Ks. ePerusteet-palvelu.")
  @Tooltip("Tutkinnon perusteen diaarinumero.")
  @Title("Peruste")
  @ClassName("peruste")
  def perusteenDiaarinumero: Option[String]
}

trait PerusteenNimellinen {
  @Description("Tutkinnon perusteen nimi. Tiedon syötössä tietoa ei tarvita; tieto haetaan e-perusteet palvelusta.")
  def perusteenNimi: Option[LocalizedString]
}

trait Laajuudeton extends KoulutusmoduuliValinnainenLaajuus {
  override def laajuus: Option[Laajuus] = None
}

trait LaajuuttaEiValidoida extends KoulutusmoduuliValinnainenLaajuus

trait Tutkinto extends KoulutusmoduuliValinnainenLaajuus {
  override def isTutkinto = true
}

trait DiaarinumerollinenKoulutus extends Koulutus with Diaarinumerollinen

trait PaikallinenKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: PaikallinenKoodi
  def nimi = tunniste.nimi
  @MultiLineString(5)
  def kuvaus: LocalizedString
}

trait PaikallinenKoulutusmoduuliValinnainenLaajuus extends PaikallinenKoulutusmoduuli with KoulutusmoduuliValinnainenLaajuus

trait PaikallinenKoulutusmoduuliPakollinenLaajuus extends PaikallinenKoulutusmoduuli with KoulutusmoduuliPakollinenLaajuus

trait Valinnaisuus {
  @Description("Onko pakollinen osa tutkinnossa (true/false)")
  def pakollinen: Boolean
}

trait Kieliaine extends Koulutusmoduuli {
  @Tooltip("Opiskeltava kieli.")
  def kieli: Koodistokoodiviite
  override def identiteetti: AnyRef = (super.identiteetti, kieli)
  protected def kieliaineDescription: LocalizedString = Kieliaine.description(nimi, kieli)
}

object Kieliaine {
  def description(nimi: LocalizedString, kieli: Koodistokoodiviite) = concat(nimi, unlocalized(", "), kieli.nimi.getOrElse(unlocalized(kieli.koodiarvo)))
}

trait Äidinkieli extends Kieliaine

trait Oppimäärä extends Koulutusmoduuli {
  def oppimäärä: Koodistokoodiviite
}

trait OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[A <: Laajuus]
  extends KoulutusmoduuliValinnainenLaajuus {
  def laajuus: Option[A]
  def makeLaajuus(laajuusArvo: Double): A

  final def withLaajuus(laajuusArvo: Double): OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[A] =
    this.withLaajuus(Some(makeLaajuus(laajuusArvo)))
  final def withLaajuusNone(): OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[A] = this.withLaajuus(None)

  final def withLaajuus(laajuus: Option[A]): OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[A] = {
    import mojave._
    shapeless
      .lens[OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[A]]
      .field[Option[A]]("laajuus")
      .set(this)(laajuus)
  }
}

trait OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuliLaajuusOpintopisteissä
  extends OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä] {
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
}

trait OpintopistelaajuuksienYhteenlaskennanOhittavaKoulutusmoduuli[A <: Laajuus] extends OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[A]

trait OpintopistelaajuuksienYhteenlaskennanOhittavaKoulutusmoduuliLaajuusOpintopisteissä
  extends OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[LaajuusOpintopisteissä] {
  override def makeLaajuus(laajuusArvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(laajuusArvo)
}
