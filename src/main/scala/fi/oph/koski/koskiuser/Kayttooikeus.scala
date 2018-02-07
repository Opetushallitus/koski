package fi.oph.koski.koskiuser

import fi.oph.koski.schema.OrganisaatioWithOid

object Rooli {
  val READ = "READ"
  val READ_UPDATE = "READ_UPDATE"
  val TIEDONSIIRRON_MITATOINTI = "TIEDONSIIRRON_MITATOINTI"
  val OPHKATSELIJA = "OPHKATSELIJA"
  val OPHPAAKAYTTAJA = "OPHPAAKAYTTAJA"
  val YLLAPITAJA = "YLLAPITAJA"
  val TIEDONSIIRTO = "TIEDONSIIRTO"
  val LUOTTAMUKSELLINEN = "LUOTTAMUKSELLINEN"
}

trait Käyttöoikeus {
  // this trait is intentionally left blank to make it harder to accidentally mix global and organization-specific rights
}

case class KäyttöoikeusGlobal(val globalPalveluroolit: List[Palvelurooli]) extends Käyttöoikeus {
  def globalAccessType: List[AccessType.Value] = globalPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "OPHKATSELIJA") => List(AccessType.read)
    case Palvelurooli("KOSKI", "OPHPAAKAYTTAJA") => List(AccessType.read, AccessType.write, AccessType.tiedonsiirronMitätöinti)
    case _ => Nil
  }
}

case class KäyttöoikeusOrg(val organisaatio: OrganisaatioWithOid, val organisaatiokohtaisetPalveluroolit: List[Palvelurooli], juuri: Boolean, oppilaitostyyppi: Option[String]) extends Käyttöoikeus {
  def organisaatioAccessType: List[AccessType.Value] = organisaatiokohtaisetPalveluroolit flatMap {
    case Palvelurooli("KOSKI", "READ") => List(AccessType.read)
    case Palvelurooli("KOSKI", "READ_UPDATE") => List(AccessType.read, AccessType.write)
    case Palvelurooli("KOSKI", "TIEDONSIIRRON_MITATOINTI") => List(AccessType.tiedonsiirronMitätöinti)
    case _ => Nil
  }
  def globalAccessType: List[AccessType.Value] = Nil
  def globalPalveluroolit = Nil
}
