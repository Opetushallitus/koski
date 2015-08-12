package fi.oph.tor.model

import java.util.Date

case class Suoritus(id: Option[Int], organisaatioId: String, personOid: String, komoOid: String, komoTyyppi: String,
                            status: String, suoritusPäivä: Option[Date], arviointi: Option[Arviointi],
                            osasuoritukset: List[Suoritus]) extends Identified