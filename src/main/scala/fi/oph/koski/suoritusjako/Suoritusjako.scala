package fi.oph.koski.suoritusjako

import java.time.LocalDate
import java.sql.Timestamp

case class Suoritusjako(secret: String, expirationDate: LocalDate, timestamp: Timestamp, jaonTyyppi: String)
