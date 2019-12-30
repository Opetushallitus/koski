package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.schema.{Koulutustoimija, OidOrganisaatio, OpiskeluoikeudenTyyppi, OrganisaatioWithOid}

object Rooli {
  type Role = String
  val READ = "READ"
  val READ_UPDATE = "READ_UPDATE"
  val TIEDONSIIRRON_MITATOINTI = "TIEDONSIIRRON_MITATOINTI"
  val OPHKATSELIJA = "OPHKATSELIJA"
  val OPHPAAKAYTTAJA = "OPHPAAKAYTTAJA"
  val YLLAPITAJA = "YLLAPITAJA"
  val TIEDONSIIRTO = "TIEDONSIIRTO"
  val LUOTTAMUKSELLINEN_KAIKKI_TIEDOT = "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"
  val LUOTTAMUKSELLINEN_KELA_SUPPEA = "LUOTTAMUKSELLINEN_KELA_SUPPEA"
  val LUOTTAMUKSELLINEN_KELA_LAAJA = "LUOTTAMUKSELLINEN_KELA_LAAJA"
  val LUKU_ESIOPETUS = "LUKU_ESIOPETUS"
  val GLOBAALI_LUKU_PERUSOPETUS = "GLOBAALI_LUKU_PERUSOPETUS"
  val GLOBAALI_LUKU_TOINEN_ASTE = "GLOBAALI_LUKU_TOINEN_ASTE"
  val GLOBAALI_LUKU_KORKEAKOULU = "GLOBAALI_LUKU_KORKEAKOULU"
  val TIEDONSIIRTO_LUOVUTUSPALVELU = "TIEDONSIIRTO_LUOVUTUSPALVELU"
  val TILASTOKESKUS = "TILASTOKESKUS"
  val VALVIRA = "VALVIRA"

  def globaalitKoulutusmuotoRoolit = List(GLOBAALI_LUKU_PERUSOPETUS, GLOBAALI_LUKU_TOINEN_ASTE, GLOBAALI_LUKU_KORKEAKOULU)
}

// this trait is intentionally left mostly blank to make it harder to accidentally mix global and organization-specific rights
trait Käyttöoikeus {
  def allowedOpiskeluoikeusTyypit: Set[String] = Set.empty
}

case class KäyttöoikeusGlobal(globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = globalPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "OPHKATSELIJA") => List(AccessType.read)
    case Palvelurooli("KOSKI", "OPHPAAKAYTTAJA") => List(AccessType.read, AccessType.write, AccessType.tiedonsiirronMitätöinti)
    case _ => Nil
  }

  override lazy val allowedOpiskeluoikeusTyypit: Set[String] = if (globalAccessType.contains(AccessType.read)) {
    OpiskeluoikeudenTyyppi.kaikkiTyypit.map(_.koodiarvo)
  } else {
    Set.empty
  }
}

trait OrgKäyttöoikeus extends Käyttöoikeus {
  def organisaatiokohtaisetPalveluroolit: List[Palvelurooli]
  def organisaatioAccessType: List[AccessType.Value] = organisaatiokohtaisetPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "READ") => List(AccessType.read)
    case Palvelurooli("KOSKI", "READ_UPDATE") => List(AccessType.read, AccessType.write)
    case Palvelurooli("KOSKI", "TIEDONSIIRRON_MITATOINTI") => List(AccessType.tiedonsiirronMitätöinti)
    case _ => Nil
  }

  override lazy val allowedOpiskeluoikeusTyypit: Set[String] = if (!organisaatioAccessType.contains(AccessType.read)) {
    Set.empty
  } else if (organisaatiokohtaisetPalveluroolit.exists(_.rooli == LUKU_ESIOPETUS)) {
    Set(OpiskeluoikeudenTyyppi.esiopetus.koodiarvo)
  } else {
    OpiskeluoikeudenTyyppi.kaikkiTyypit.map(_.koodiarvo)
  }

  def globalAccessType: List[AccessType.Value] = Nil
  def globalPalveluroolit: List[Palvelurooli] = Nil
}

case class KäyttöoikeusVarhaiskasvatusToimipiste(koulutustoimija: Koulutustoimija, organisaatio: OrganisaatioWithOid, organisaatiokohtaisetPalveluroolit: List[Palvelurooli]) extends OrgKäyttöoikeus
case class KäyttöoikeusOrg(organisaatio: OrganisaatioWithOid, organisaatiokohtaisetPalveluroolit: List[Palvelurooli], juuri: Boolean, oppilaitostyyppi: Option[String]) extends OrgKäyttöoikeus

case class KäyttöoikeusViranomainen(globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = if (globalPalveluroolit.exists(r => r.palveluName == "KOSKI" && Rooli.globaalitKoulutusmuotoRoolit.contains(r.rooli))) {
    List(AccessType.read)
  } else {
    Nil
  }

  override lazy val allowedOpiskeluoikeusTyypit: Set[String] = globalPalveluroolit.flatMap {
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_PERUSOPETUS) => List(
      OpiskeluoikeudenTyyppi.esiopetus,
      OpiskeluoikeudenTyyppi.perusopetus,
      OpiskeluoikeudenTyyppi.aikuistenperusopetus,
      OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus,
      OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus,
      OpiskeluoikeudenTyyppi.internationalschool
    )
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_TOINEN_ASTE) => List(
      OpiskeluoikeudenTyyppi.ammatillinenkoulutus,
      OpiskeluoikeudenTyyppi.ibtutkinto,
      OpiskeluoikeudenTyyppi.diatutkinto,
      OpiskeluoikeudenTyyppi.lukiokoulutus,
      OpiskeluoikeudenTyyppi.luva,
      OpiskeluoikeudenTyyppi.ylioppilastutkinto,
      OpiskeluoikeudenTyyppi.internationalschool
    )
    case Palvelurooli("KOSKI", GLOBAALI_LUKU_KORKEAKOULU) => List(
      OpiskeluoikeudenTyyppi.korkeakoulutus
    )
    case _ => Nil
  }.map(_.koodiarvo).toSet

  def isLuovutusPalveluAllowed: Boolean = globalPalveluroolit.contains(Palvelurooli("KOSKI", TIEDONSIIRTO_LUOVUTUSPALVELU))
}
