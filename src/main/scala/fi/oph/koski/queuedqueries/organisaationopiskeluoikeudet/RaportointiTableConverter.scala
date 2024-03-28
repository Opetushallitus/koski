package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.queuedqueries.CsvRecord
import fi.oph.koski.raportointikanta.{ROsasuoritusRow, RPäätasonSuoritusRow}

object RaportointitableIds {
  def päätasonSuoritusId(ooOid: String, ptsId: Long): String = s"$ooOid.$ptsId"
  def osasuoritusId(ooOid: String, ptsId: Long, osId: Long) = s"$ooOid.$ptsId.$osId"
}

class CsvPäätasonSuoritus(row: RPäätasonSuoritusRow) extends CsvRecord(row) {
  override def get(fieldName: String): Option[Any] = fieldName match {
    case "päätasonSuoritusId" => Some(RaportointitableIds.päätasonSuoritusId(row.opiskeluoikeusOid, row.päätasonSuoritusId))
    case _ => None
  }
}

class CsvOsauoritus(row: ROsasuoritusRow) extends CsvRecord(row) {
  override def get(fieldName: String): Option[Any] = fieldName match {
    case "päätasonSuoritusId" => Some(RaportointitableIds.päätasonSuoritusId(row.opiskeluoikeusOid, row.päätasonSuoritusId))
    case "osasuoritusId" => Some(RaportointitableIds.osasuoritusId(row.opiskeluoikeusOid, row.päätasonSuoritusId, row.osasuoritusId))
    case "ylempiOsasuoritusId" => Some(row.ylempiOsasuoritusId.map(RaportointitableIds.osasuoritusId(row.opiskeluoikeusOid, row.päätasonSuoritusId, _)))
    case _ => None
  }
}
