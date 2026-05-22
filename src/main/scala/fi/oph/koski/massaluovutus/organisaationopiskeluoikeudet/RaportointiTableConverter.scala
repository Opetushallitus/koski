package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.massaluovutus.CsvRecord
import fi.oph.koski.raportointikanta.{ROsasuoritusRow, RPäätasonSuoritusRow}

class CsvPäätasonSuoritus(row: RPäätasonSuoritusRow) extends CsvRecord(row) {
  override def get(fieldName: String): Option[Any] = fieldName match {
    case "päätasonSuoritusId" => Some(row.päätasonSuoritusId)
    case _ => None
  }
}

class CsvOsauoritus(row: ROsasuoritusRow) extends CsvRecord(row) {
  override def get(fieldName: String): Option[Any] = fieldName match {
    case "päätasonSuoritusId" => Some(row.päätasonSuoritusId)
    case "osasuoritusId" => Some(row.osasuoritusId)
    case "ylempiOsasuoritusId" => Some(row.ylempiOsasuoritusId)
    case _ => None
  }
}
