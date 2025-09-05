package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.schema.annotation.{Example, KoodistoUri, ReadOnly, Tooltip}
import fi.oph.scalaschema.annotation._

sealed trait Organisaatio extends Localized

object Organisaatio {
  type Oid = String
  def isValidOrganisaatioOid(oid: String) = {
    // 10 tuotantoluokka
    // 99, 199 käytössä testiopintopolussa/untuvassa testidatalle
    """^1\.2\.246\.562\.(10|99|199)\.\d{11,24}$""".r.findFirstIn(oid).isDefined
  }
}

@Title("Organisaatio-OID")
@Description("Opintopolun organisaatiopalvelusta löytyvä organisaatio, jonka OID-tunniste on tiedossa")
case class OidOrganisaatio(
  oid: Organisaatio.Oid,
  nimi: Option[LocalizedString] = None,
  @Description("Organisaation kotipaikka")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = None
}

@Description("Opintopolun organisaatiopalvelusta löytyvä koulutustoimija-tyyppinen, oppilaitoksen ylätasolla oleva organisaatio. Tiedon syötössä tietoa ei tarvita; organisaation tiedot haetaan Organisaatiopalvelusta")
case class Koulutustoimija(
  oid: Organisaatio.Oid,
  nimi: Option[LocalizedString] = None,
  @Description("Koulutustoimijan Y-tunnus")
  @RegularExpression("^\\d{7}-\\d$")
  @Example("1234567-8")
  @Discriminator
  @Title("Y-tunnus")
  yTunnus: Option[String] = None,
  @Description("Koulutustoimijan kotipaikka")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = None

  def toTuntematonOppilaitos = Oppilaitos(
    oid = oid,
    nimi = nimi,
    kotipaikka = kotipaikka,
    // Selkeä feikkikoodiarvo kertomaan tiedon hyödyntäjille, ettei tämä ole oikea oppilaitos
    oppilaitosnumero = Some(Koodistokoodiviite("EI OPPILAITOSTA", "oppilaitosnumero")),
  )
}

@Description("Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio")
case class Oppilaitos(
  oid: Organisaatio.Oid,
  @Description("5-numeroinen oppilaitosnumero, esimerkiksi 00001")
  @ReadOnly("Tiedon syötössä oppilaitosnumeroa ei tarvita; numero haetaan Organisaatiopalvelusta")
  @KoodistoUri("oppilaitosnumero")
  @Discriminator
  oppilaitosnumero: Option[Koodistokoodiviite] = None,
  nimi: Option[LocalizedString] = None,
  @Description("Oppilaitoksen kotipaikka")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = Some(this)
}

@Description("Opintopolun organisaatiopalvelusta löytyvä toimipiste-tyyppinen organisaatio")
@IgnoreInAnyOfDeserialization
case class Toimipiste(
  oid: Organisaatio.Oid,
  nimi: Option[LocalizedString] = None,
  @Description("Toimipisteen kotipaikka")
  kotipaikka: Option[Koodistokoodiviite] = None
) extends OrganisaatioWithOid with DefaultDescription {
  def toOppilaitos = None
}

@Description("Yritys, jolla on y-tunnus")
case class Yritys(
  @Title("Yritys")
  @Description("Yrityksen nimi")
  @Tooltip("Yrityksen nimi")
  nimi: LocalizedString,
  @Title("Y-tunnus")
  @Description("Yrityksen Y-tunnus")
  @Tooltip("Yrityksen Y-tunnus")
  @RegularExpression("^\\d{7}-\\d$")
  @Example("1234567-8")
  @Discriminator
  yTunnus: String
) extends Organisaatio {
  def description = nimi
}

@Description("Tutkintotoimikunta")
case class Tutkintotoimikunta(
  nimi: LocalizedString,
  @Description("Tutkintotoimikunnan numero")
  @Discriminator
  tutkintotoimikunnanNumero: String
) extends Organisaatio {
  def description = nimi
}

trait OrganisaatioWithOid extends Organisaatio {
  @Description("Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, koulutuksen järjestäjän oid")
  // 10 tuotantoluokka
  // 99, 199 käytössä testiopintopolussa/untuvassa testidatalle
  @RegularExpression("""^1\.2\.246\.562\.(10|99|199)\.\d{11,24}$""")
  @Discriminator
  def oid: Organisaatio.Oid
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def nimi: Option[LocalizedString]
  def toOppilaitos: Option[Oppilaitos]
  def toOidOrganisaatio = OidOrganisaatio(oid, nimi)
  def toKoulutustoimija: Option[Koulutustoimija] = this match {
    case kt: Koulutustoimija => Some(kt)
    case _ => None
  }
  @KoodistoUri("kunta")
  def kotipaikka: Option[Koodistokoodiviite]
}

trait DefaultDescription extends OrganisaatioWithOid {
  def description = nimi.getOrElse(LocalizedString.unlocalized(oid))
}

trait OrganisaatioonLiittyvä {
  def omistajaOrganisaatio: Option[OrganisaatioWithOid]
}

case class OpiskeluoikeudenOrganisaatiohistoria(
  muutospäivä: LocalDate,
  @Title("Aikaisempi oppilaitos")
  oppilaitos: Option[Oppilaitos],
  @Title("Aikaisempi koulutustoimija")
  koulutustoimija: Option[Koulutustoimija]
)
