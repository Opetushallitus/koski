package fi.oph.koski.raportit

import java.io.OutputStream
import java.time.{LocalDate, ZoneId}
import java.util.Date

import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.{SXSSFSheet, SXSSFWorkbook}
import org.apache.poi.xssf.usermodel.XSSFSheet

import scala.reflect.runtime.universe._

object ExcelWriter {

  def writeExcel[T <: Product : TypeTag](title: String, rows: Seq[T], columnSettings: Seq[(String, Column)], out: OutputStream): Unit = {

    // validate that columnSettings matches case class structure
    val namesFromCaseClass = symbolOf[T].asClass.primaryConstructor.typeSignature.paramLists.head.map(_.name.toString)
    val namesFromSettings = columnSettings.map(_._1)
    if (namesFromCaseClass != namesFromSettings) {
      throw new IllegalArgumentException(s"columnSettings does not match case class: $namesFromCaseClass $namesFromSettings")
    }

    val columnSettingsWithIndex = columnSettings.zipWithIndex

    val wb = new SXSSFWorkbook(100)
    try {
      val coreProps = wb.getXSSFWorkbook.getProperties.getCoreProperties
      coreProps.setTitle(title)
      coreProps.setCreator("Koski")

      val sh = wb.createSheet

      // SXSSFSheet does not expose "addIgnoredErrors" method. Hack around this.
      // based on https://stackoverflow.com/questions/47477912/apache-poi-how-to-use-addignorederrors-functionality-in-sxssfsheet
      val hiddenShField = classOf[SXSSFSheet].getDeclaredField("_sh")
      hiddenShField.setAccessible(true)
      hiddenShField.get(sh).asInstanceOf[XSSFSheet].addIgnoredErrors(new CellRangeAddress(0, 999999, 0, 999), IgnoredErrorType.NUMBER_STORED_AS_TEXT)

      val headingStyle = wb.createCellStyle()
      headingStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex)
      headingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
      headingStyle.setVerticalAlignment(VerticalAlignment.CENTER)

      val headingRow = sh.createRow(0)
      headingRow.setHeightInPoints(25)
      for ((cs, headingCol) <- columnSettingsWithIndex) {
        val cell = headingRow.createCell(headingCol)
        cell.setCellStyle(headingStyle)
        cell.setCellValue(cs._2.title)
        cs._2.width match {
          case None => sh.trackColumnForAutoSizing(headingCol)
          case Some(w) => sh.setColumnWidth(headingCol, w)
        }
      }

      val textStyle = wb.createCellStyle()
      textStyle.setDataFormat(BuiltinFormats.getBuiltinFormat("text").toShort)

      val dateStyle = wb.createCellStyle()
      dateStyle.setDataFormat(wb.getCreationHelper.createDataFormat.getFormat("yyyy-MM-dd"))

      sh.createFreezePane(0, 1)

      for (rowNumber <- rows.indices) {
        val row = sh.createRow(rowNumber + 1)
        val dataRow = rows(rowNumber)
        var colNumber = 0
        for (f <- dataRow.productIterator) {
          val cell = row.createCell(colNumber)
          f match {
            case s: String => cell.setCellStyle(textStyle); cell.setCellValue(s)
            case Some(s: String) => cell.setCellStyle(textStyle); cell.setCellValue(s)
            case d: LocalDate => cell.setCellStyle(dateStyle); cell.setCellValue(Date.from(d.atStartOfDay(ZoneId.systemDefault).toInstant))
            case Some(d: LocalDate) => cell.setCellStyle(dateStyle); cell.setCellValue(Date.from(d.atStartOfDay(ZoneId.systemDefault).toInstant))
            case i: Int => cell.setCellValue(i)
            case f: Float => cell.setCellValue(f)
            case b: Boolean => cell.setCellValue(b)
            case None => /* ok */
            case x: Any => throw new IllegalStateException("Not handled yet? " + x.toString)
          }
          colNumber += 1
        }
      }
      for ((cs, col) <- columnSettingsWithIndex) {
        if (cs._2.width.isEmpty) {
          sh.autoSizeColumn(col)
        }
      }
      wb.write(out)
      out.close()
    } finally {
      // deletes temporary files from disk
      wb.dispose()
    }
  }
}

case class Column(title: String, width: Option[Int] = None)
