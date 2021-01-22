package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.Palvelurooli

object ValpasPalvelurooli {
  def apply(rooli: ValpasRooli.Role): Palvelurooli = Palvelurooli("VALPAS", rooli)
}

object ValpasRooli {
  type Role = String
  val OPPILAITOS = "OPPILAITOS"
  val KUNTA = "KUNTA"
}
