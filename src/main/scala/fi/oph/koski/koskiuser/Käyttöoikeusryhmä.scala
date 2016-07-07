package fi.oph.koski.koskiuser

sealed trait Käyttöoikeusryhmä {
  def nimi: String
  def kuvaus: String
  def orgAccessType: List[AccessType.Value]
  def globalAccessType: List[AccessType.Value]
  override def toString = "Käyttöoikeusryhmä " + nimi
}

case class OrganisaationKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val orgAccessType: List[AccessType.Value] = Nil) extends Käyttöoikeusryhmä {
  def globalAccessType = Nil
}

case class GlobaaliKäyttöoikeusryhmä private[koskiuser](val nimi: String, val kuvaus: String, val globalAccessType: List[AccessType.Value] = Nil) extends Käyttöoikeusryhmä {
  def orgAccessType = Nil
}