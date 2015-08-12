package fi.oph.tor.model



case class Suoritus(id: Option[Int], organisaatioId: String, personOid: String, komoOid: String, komoTyyppi: String,
                            status: String, arviointi: Option[Arviointi],
                            osasuoritukset: List[Suoritus]) extends Identified