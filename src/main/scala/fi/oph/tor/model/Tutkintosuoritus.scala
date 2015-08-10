package fi.oph.tor.model

import fi.oph.tor.model.Identified.Id

trait Koulutusmoduulisuoritus {
  def komoOid: String
  def status: String
  def arviointi: Option[Arviointi]
}
trait Identified {
  def id: Option[Id]
}
object Identified {
  type Id = Int

  def withoutId(t: Tutkintosuoritus): Tutkintosuoritus = t.copy(id = None, osasuoritukset = t.osasuoritukset.map(withoutId), arviointi = t.arviointi.map(withoutId))
  def withoutId(t: Tutkinnonosasuoritus): Tutkinnonosasuoritus = t.copy(id = None, kurssisuoritukset = t.kurssisuoritukset.map(withoutId), arviointi = t.arviointi.map(withoutId))
  def withoutId(t: Kurssisuoritus): Kurssisuoritus = t.copy(id = None, arviointi = t.arviointi.map(withoutId))
  def withoutId(t: Arviointi): Arviointi = t.copy(id = None)
}

// The Identified.id alias not used below, because it causes json4s-jackson serialization to hang

case class Arviointi(id: Option[Int], asteikko: String, numero: Int, kuvaus: Option[String]) {

}

case class Tutkintosuoritus(id: Option[Int], organisaatioId: String, personOid: String, komoOid: String,
                            status: String, arviointi: Option[Arviointi],
                            osasuoritukset: List[Tutkinnonosasuoritus]) extends Koulutusmoduulisuoritus {
}

case class Tutkinnonosasuoritus(id: Option[Int], komoOid: String,
                                status: String, arviointi: Option[Arviointi],
                                kurssisuoritukset: List[Kurssisuoritus]) extends Koulutusmoduulisuoritus

case class Kurssisuoritus(id: Option[Int], komoOid: String,
                          status: String, arviointi: Option[Arviointi]) extends Koulutusmoduulisuoritus

