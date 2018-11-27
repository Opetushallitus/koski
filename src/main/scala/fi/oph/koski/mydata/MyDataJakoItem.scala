package fi.oph.koski.mydata

import java.sql.Timestamp
import java.time.LocalDate

case class MyDataJakoItem(asiakasId: String, asiakasName: String, expirationDate: LocalDate, timestamp: Timestamp, purpose: String)
