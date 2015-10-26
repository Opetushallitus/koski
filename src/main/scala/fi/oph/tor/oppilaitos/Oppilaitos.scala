package fi.oph.tor.oppilaitos

case class Oppilaitos(oid: String, nimi: Option[String] = None) extends OppilaitosOrId
case class OppilaitosId(oid: String) extends OppilaitosOrId

trait OppilaitosOrId { def oid: String }
