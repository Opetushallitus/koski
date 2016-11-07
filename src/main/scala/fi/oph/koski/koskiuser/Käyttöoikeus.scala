package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.PalveluRooli
import fi.oph.koski.schema.OrganisaatioWithOid

object Rooli {
  val READ = "READ"
  val READ_UPDATE = "READ_UPDATE"
  val OPHKATSELIJA = "OPHKATSELIJA"
  val OPHPAAKAYTTAJA = "OPHPAAKAYTTAJA"
  val YLLAPITAJA = "YLLAPITAJA"
  val TIEDONSIIRTO = "TIEDONSIIRTO"
}

trait Käyttöoikeus {
  def palveluRoolit = orgPalveluroolit ++ globalPalveluroolit
  def orgPalveluroolit: List[PalveluRooli] = Nil
  def globalPalveluroolit: List[PalveluRooli] = Nil

  def orgAccessType: List[AccessType.Value] = orgPalveluroolit flatMap {
    case PalveluRooli("KOSKI", "READ") => List(AccessType.read)
    case PalveluRooli("KOSKI", "READ_UPDATE") => List(AccessType.read, AccessType.write)
    case _ => Nil
  }
  def globalAccessType: List[AccessType.Value] = globalPalveluroolit flatMap {
    case PalveluRooli("KOSKI", "OPHKATSELIJA") => List(AccessType.read)
    case PalveluRooli("KOSKI", "OPHPAAKAYTTAJA") => List(AccessType.read, AccessType.write)
    case _ => Nil
  }
}

case class KäyttöoikeusGlobal(override val globalPalveluroolit: List[PalveluRooli]) extends Käyttöoikeus

case class KäyttöoikeusOrg(organisaatio: OrganisaatioWithOid, override val orgPalveluroolit: List[PalveluRooli], juuri: Boolean, oppilaitostyyppi: Option[String]) extends Käyttöoikeus
