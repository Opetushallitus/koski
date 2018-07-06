package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString._
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, Discriminator, Title}

trait Koulutusmoduuli extends Localized {
  @Representative
  @Discriminator
  def tunniste: KoodiViite
  def laajuus: Option[Laajuus]
  def nimi: LocalizedString
  def description = nimi
  def isTutkinto = false
  // Vertailutekijä tarkistettaessa duplikaatteja
  def identiteetti: AnyRef = tunniste
}

trait KoodistostaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Koodistokoodiviite
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

trait Koulutus extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Tutkinnon 6-numeroinen tutkintokoodi. Sama kuin tilastokeskuksen koulutuskoodi")
  @KoodistoUri("koulutus")
  @OksaUri("tmpOKSAID560", "tutkinto")
  def tunniste: Koodistokoodiviite
  @KoodistoUri("koulutustyyppi")
  @Hidden
  @ReadOnly("Koulutustyypin koodia ei tarvita syöttövaiheessa; Koski päättelee sen automaattisesti koulutuskoodin perusteella.")
  def koulutustyyppi: Option[Koodistokoodiviite]
}

trait Diaarinumerollinen {
  @Description("Tutkinnon perusteen diaarinumero. Ks. ePerusteet-palvelu.")
  @Tooltip("Tutkinnon perusteen diaarinumero.")
  @Title("Peruste")
  @ClassName("peruste")
  def perusteenDiaarinumero: Option[String]
}

trait Laajuudeton extends Koulutusmoduuli {
  override def laajuus: Option[Laajuus] = None
}

trait LaajuuttaEiValidoida extends Koulutusmoduuli

trait Tutkinto extends Koulutusmoduuli {
  override def isTutkinto = true
}

trait DiaarinumerollinenKoulutus extends Koulutus with Diaarinumerollinen

trait PaikallinenKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: PaikallinenKoodi
  def nimi = tunniste.nimi
  @MultiLineString(5)
  def kuvaus: LocalizedString
}

trait Valinnaisuus {
  @Description("Onko pakollinen osa tutkinnossa (true/false)")
  def pakollinen: Boolean
}

trait Kieliaine extends Koulutusmoduuli {
  @Tooltip("Opiskeltava kieli.")
  def kieli: Koodistokoodiviite
  override def identiteetti: AnyRef = (super.identiteetti, kieli)
  protected def kieliaineDescription = concat(nimi, unlocalized(", "), kieli.nimi.getOrElse(unlocalized(kieli.koodiarvo)))
}

trait Äidinkieli extends Kieliaine
