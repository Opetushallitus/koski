package fi.oph.tor.api

import fi.oph.tor.json.Json._
import fi.oph.tor.schema._
import org.json4s.JValue

trait OpiskeluOikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö = UusiHenkilö("010101-123N", "Testi", "Testi", "Toivola")

  def defaultOpiskeluoikeus: Oikeus

  val tilaValmis: Koodistokoodiviite = Koodistokoodiviite("VALMIS", "suorituksentila")
  val tilaKesken: Koodistokoodiviite = Koodistokoodiviite("KESKEN", "suorituksentila")
}
