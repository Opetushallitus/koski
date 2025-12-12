package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.Palvelurooli

object ValpasPalvelurooli {
  def apply(rooli: ValpasRooli.Role): Palvelurooli = Palvelurooli("VALPAS", rooli)
}

object ValpasRooli {
  type Role = String
  val OPPILAITOS_HAKEUTUMINEN = "OPPILAITOS_HAKEUTUMINEN"
  val OPPILAITOS_SUORITTAMINEN = "OPPILAITOS_SUORITTAMINEN"
  val OPPILAITOS_MAKSUTTOMUUS = "OPPILAITOS_MAKSUTTOMUUS"
  val KUNTA = "KUNTA"
  val KELA = "KELA"
  val YTL = "YTL"
  val KUNTA_MASSALUOVUTUS = "KUNTA_MASSALUOVUTUS"
}
