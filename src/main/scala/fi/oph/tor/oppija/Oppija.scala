package fi.oph.tor.oppija

case class Oppija(oid: Option[String], hetu: Option[String], etunimet: Option[String], kutsumanimi: Option[String], sukunimi: Option[String])

object Oppija {
  type Id = String
}
