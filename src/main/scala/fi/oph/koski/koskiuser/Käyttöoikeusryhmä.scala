package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.PalveluRooli

sealed trait Käyttöoikeusryhmä extends Käyttöoikeus {
  def nimi: String
  def kuvaus: String

  override def toString = "Käyttöoikeusryhmä " + nimi
}

case class OrganisaationKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, override val orgPalveluroolit: List[PalveluRooli] = Nil) extends Käyttöoikeusryhmä {
}

case class GlobaaliKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, override val globalPalveluroolit: List[PalveluRooli] = Nil) extends Käyttöoikeusryhmä {
}