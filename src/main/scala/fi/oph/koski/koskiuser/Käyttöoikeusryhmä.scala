package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.Palvelurooli

sealed trait Käyttöoikeusryhmä {
  def nimi: String
  def kuvaus: String
  def palveluroolit: List[Palvelurooli]

  override def toString = "Käyttöoikeusryhmä " + nimi
}

case class OrganisaationKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val palveluroolit: List[Palvelurooli] = Nil) extends Käyttöoikeusryhmä

case class GlobaaliKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val palveluroolit: List[Palvelurooli] = Nil) extends Käyttöoikeusryhmä