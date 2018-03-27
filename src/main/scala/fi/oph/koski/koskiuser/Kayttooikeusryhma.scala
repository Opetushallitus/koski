package fi.oph.koski.koskiuser

sealed trait Käyttöoikeusryhmä {
  def nimi: String
  def kuvaus: String
  def palveluroolit: List[Palvelurooli]

  override def toString = "Käyttöoikeusryhmä " + nimi
}

case class OrganisaationKäyttöoikeusryhmä private[koskiuser](nimi: String, tunniste: String, kuvaus: String, palveluroolit: List[Palvelurooli] = Nil) extends Käyttöoikeusryhmä

case class GlobaaliKäyttöoikeusryhmä private[koskiuser](nimi: String, kuvaus: String, palveluroolit: List[Palvelurooli] = Nil) extends Käyttöoikeusryhmä
