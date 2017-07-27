package fi.oph.koski.schema

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.localization.{Localizable, Localized, LocalizedString}
import fi.oph.scalaschema.annotation._

sealed trait Organisaatio extends Localized

object Organisaatio {
  type Oid = String
}

object OrganisaatioOid {
  def isValidOrganisaatioOid(oid: String) = {
    """^1\.2\.246\.562\.10\.\d{11,24}$""".r.findFirstIn(oid).isDefined
  }

  def validateOrganisaatioOid(oid: String): Either[HttpStatus, String] = {
    if (isValidOrganisaatioOid(oid)) {
      Right(oid)
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: " + oid + ". Esimerkki oikeasta muodosta: 1.2.246.562.10.00000000001."))
    }
  }
}

@Title("Organisaatio-OID")
@Description("Opintopolun organisaatiopalvelusta löytyvä organisaatio, jonka OID-tunniste on tiedossa")
case class OidOrganisaatio(
  oid: Organisaatio.Oid,
  nimi: Option[LocalizedString] = None,
  @Description("Organisaation kotipaikka.")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = None
}

@Description("Opintopolun organisaatiopalvelusta löytyvä koulutustoimija-tyyppinen, oppilaitoksen ylätasolla oleva organisaatio. Tiedon syötössä tietoa ei tarvita; organisaation tiedot haetaan Organisaatiopalvelusta")
case class Koulutustoimija(
  oid: Organisaatio.Oid,
  nimi: Option[LocalizedString] = None,
  @Description("Koulutustoimijan Y-tunnus")
  @RegularExpression("\\d{7}-\\d")
  @Discriminator
  @Title("Y-tunnus")
  yTunnus: Option[String] = None,
  @Description("Koulutustoimijan kotipaikka.")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = None
}

@Description("Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio.")
case class Oppilaitos(
  oid: String,
  @Description("5-numeroinen oppilaitosnumero, esimerkiksi 00001")
  @ReadOnly("Tiedon syötössä oppilaitosnumeroa ei tarvita; numero haetaan Organisaatiopalvelusta")
  @KoodistoUri("oppilaitosnumero")
  @Discriminator
  oppilaitosnumero: Option[Koodistokoodiviite] = None,
  @Description("Oppilaitoksen kotipaikka.")
  nimi: Option[LocalizedString] = None,
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = Some(this)
}

@Description("Opintopolun organisaatiopalvelusta löytyvä toimipiste-tyyppinen organisaatio.")
@IgnoreInAnyOfDeserialization
case class Toimipiste(
  oid: String,
  nimi: Option[LocalizedString] = None,
  @Description("Toimipisteen kotipaikka.")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = None
}

@Description("Yritys, jolla on y-tunnus")
case class Yritys(
  @Title("Yritys")
  nimi: LocalizedString,
  @Title("Y-tunnus")
  @RegularExpression("\\d{7}-\\d")
  @Discriminator
  yTunnus: String
) extends Organisaatio {
  def description = nimi
}

@Description("Tutkintotoimikunta")
case class Tutkintotoimikunta(
  nimi: LocalizedString,
  @Discriminator
  tutkintotoimikunnanNumero: String
) extends Organisaatio {
  def description = nimi
}

trait OrganisaatioWithOid extends Organisaatio {
  @Description("Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid.")
  @RegularExpression("""1\.2\.246\.562\.10\.\d{11,24}""")
  @Discriminator
  def oid: String
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def nimi: Option[LocalizedString]
  def toOppilaitos: Option[Oppilaitos]
  def toOidOrganisaatio = OidOrganisaatio(oid, nimi)
  @KoodistoUri("kunta")
  def kotipaikka: Option[Koodistokoodiviite]
}

trait DefaultDescription extends OrganisaatioWithOid {
  def description = nimi.getOrElse(LocalizedString.unlocalized(oid))
}

trait OrganisaatioonLiittyvä {
  def omistajaOrganisaatio: Option[OrganisaatioWithOid]
}
