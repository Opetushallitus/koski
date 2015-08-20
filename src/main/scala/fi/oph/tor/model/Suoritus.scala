package fi.oph.tor.model

import java.util.Date

case class Suoritus(id: Option[Int], suoritusPäivä: Option[Date], järjestäjäOrganisaatioId: String, myöntäjäOrganisaatioId: String, oppijaId: String, status: String, kuvaus: Option[String], komoto: Komoto, arviointi: Option[Arviointi],
                            osasuoritukset: List[Suoritus]) extends Identified

case class Komoto(id: Option[Int], nimi: Option[String], kuvaus: Option[String], komoId: Option[String], komoTyyppi: Option[String], koodistoId: Option[String], koodistoKoodi: Option[String], ePeruste: Option[String])