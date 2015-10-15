package fi.oph.tor.oppija

case class Oppija(oid: String, sukunimi: String, etunimet: String, hetu: String)

object Oppija {
  type Id = String
}
