package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, OrganisaatioWithOid}

object Rooli {
  type Role = String
  val READ = "READ"
  val READ_UPDATE = "READ_UPDATE"
  val TIEDONSIIRRON_MITATOINTI = "TIEDONSIIRRON_MITATOINTI"
  val OPHKATSELIJA = "OPHKATSELIJA"
  val OPHPAAKAYTTAJA = "OPHPAAKAYTTAJA"
  val YLLAPITAJA = "YLLAPITAJA"
  val TIEDONSIIRTO = "TIEDONSIIRTO"
  val LUOTTAMUKSELLINEN = "LUOTTAMUKSELLINEN"
  val LUOTTAMUKSELLINEN_KAIKKI_TIEDOT = "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"
  val LUOTTAMUKSELLINEN_KELA_SUPPEA = "LUOTTAMUKSELLINEN_KELA_SUPPEA"
  val LUOTTAMUKSELLINEN_KELA_LAAJA = "LUOTTAMUKSELLINEN_KELA_LAAJA"
  val GLOBAALI_LUKU_PERUSOPETUS = "GLOBAALI_LUKU_PERUSOPETUS"
  val GLOBAALI_LUKU_TOINEN_ASTE = "GLOBAALI_LUKU_TOINEN_ASTE"
  val GLOBAALI_LUKU_KORKEAKOULU = "GLOBAALI_LUKU_KORKEAKOULU"
  val TIEDONSIIRTO_LUOVUTUSPALVELU = "TIEDONSIIRTO_LUOVUTUSPALVELU"

  def globaalitKoulutusmuotoRoolit = List(GLOBAALI_LUKU_PERUSOPETUS, GLOBAALI_LUKU_TOINEN_ASTE, GLOBAALI_LUKU_KORKEAKOULU)
}

trait Käyttöoikeus {
  // this trait is intentionally left blank to make it harder to accidentally mix global and organization-specific rights
}

case class KäyttöoikeusGlobal(globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = globalPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "OPHKATSELIJA") => List(AccessType.read)
    case Palvelurooli("KOSKI", "OPHPAAKAYTTAJA") => List(AccessType.read, AccessType.write, AccessType.tiedonsiirronMitätöinti)
    case _ => Nil
  }
  override def toString = globalPalveluroolit.mkString(",")
}

case class KäyttöoikeusOrg(organisaatio: OrganisaatioWithOid, organisaatiokohtaisetPalveluroolit: List[Palvelurooli], juuri: Boolean, oppilaitostyyppi: Option[String]) extends Käyttöoikeus {
  def organisaatioAccessType: List[AccessType.Value] = organisaatiokohtaisetPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "READ") => List(AccessType.read)
    case Palvelurooli("KOSKI", "READ_UPDATE") => List(AccessType.read, AccessType.write)
    case Palvelurooli("KOSKI", "TIEDONSIIRRON_MITATOINTI") => List(AccessType.tiedonsiirronMitätöinti)
    case _ => Nil
  }

  def globalAccessType: List[AccessType.Value] = Nil
  def globalPalveluroolit = Nil
  override def toString = organisaatiokohtaisetPalveluroolit.mkString(",")
}

case class KäyttöoikeusViranomainen(globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = if (globalPalveluroolit.exists(r => r.palveluName == "KOSKI" && Rooli.globaalitKoulutusmuotoRoolit.contains(r.rooli))) {
    List(AccessType.read)
  } else {
    Nil
  }

  def allowedOpiskeluoikeusTyypit: List[String] = globalPalveluroolit.flatMap {
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
  }.map(_.koodiarvo).distinct

  def isLuovutusPalveluAllowed: Boolean = globalPalveluroolit.contains(Palvelurooli("KOSKI", TIEDONSIIRTO_LUOVUTUSPALVELU))
}
