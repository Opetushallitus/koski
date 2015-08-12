package fi.oph.tor.model

// The Identified.id alias not used below, because it causes json4s-jackson serialization to hang
case class Arviointi(id: Option[Int], asteikko: String, numero: Int, kuvaus: Option[String]) extends Identified
