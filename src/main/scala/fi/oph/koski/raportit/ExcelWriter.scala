package fi.oph.koski.raportit

import java.io.OutputStream
import java.time.{LocalDate, ZoneId}
import java.util.Date

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.poifs.crypt.{EncryptionInfo, EncryptionMode, Encryptor}
import org.apache.poi.poifs.crypt.temp.{EncryptedTempData, SXSSFWorkbookWithCustomZipEntrySource}
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.{SXSSFSheet, SXSSFWorkbook}
import org.apache.poi.xssf.usermodel.XSSFSheet

object ExcelWriter {

  def writeExcel(workbookSettings: WorkbookSettings, sheets: Seq[Sheet], out: OutputStream): Unit = {

    val wb = if (workbookSettings.password.isEmpty) new SXSSFWorkbook else new SXSSFWorkbookWithCustomZipEntrySource
    try {
      val coreProps = wb.getXSSFWorkbook.getProperties.getCoreProperties
      coreProps.setTitle(workbookSettings.title)
      coreProps.setCreator("Koski")
      sheets.foreach { sheet =>
        val sh = wb.createSheet(sheet.title)
        sheet match {
          case ds: DataSheet => writeDataSheet(wb, sh, ds)
          case ds: DocumentationSheet => writeDocumentationSheet(wb, sh, ds)
        }
      }
      if (workbookSettings.password.isEmpty) {
        wb.write(out)
      } else {
        // based on https://github.com/apache/poi/blob/f509d1deae86866ed531f10f2eba7db17e098473/src/examples/src/org/apache/poi/xssf/streaming/examples/SavePasswordProtectedXlsx.java
        val tempData = new EncryptedTempData
        try {
          wb.write(tempData.getOutputStream)
          val opc = OPCPackage.open(tempData.getInputStream)
          val fs = new POIFSFileSystem
          val enc = Encryptor.getInstance(new EncryptionInfo(EncryptionMode.agile))
          enc.confirmPassword(workbookSettings.password.get)
          opc.save(enc.getDataStream(fs))
          fs.writeFilesystem(out)
        } finally {
          tempData.dispose()
        }
      }
      out.close()
    } finally {
      // deletes temporary files from disk
      wb.dispose()
    }
  }

  private def writeDataSheet(wb: SXSSFWorkbook, sh: SXSSFSheet, dataSheet: DataSheet): Unit = {
    // SXSSFSheet does not expose "addIgnoredErrors" method. Hack around this.
    // based on https://stackoverflow.com/questions/47477912/apache-poi-how-to-use-addignorederrors-functionality-in-sxssfsheet
    val hiddenShField = classOf[SXSSFSheet].getDeclaredField("_sh")
    hiddenShField.setAccessible(true)
    hiddenShField.get(sh).asInstanceOf[XSSFSheet].addIgnoredErrors(new CellRangeAddress(0, 999999, 0, 999), IgnoredErrorType.NUMBER_STORED_AS_TEXT)

    val headingStyle = wb.createCellStyle()
    headingStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex)
    headingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    headingStyle.setVerticalAlignment(VerticalAlignment.CENTER)

    val textStyle = wb.createCellStyle()
    textStyle.setDataFormat(BuiltinFormats.getBuiltinFormat("text").toShort)

    val dateStyle = wb.createCellStyle()
    dateStyle.setDataFormat(wb.getCreationHelper.createDataFormat.getFormat("yyyy-MM-dd"))
    dateStyle.setAlignment(HorizontalAlignment.LEFT)

    val floatStyle = wb.createCellStyle()
    floatStyle.setDataFormat(wb.getCreationHelper.createDataFormat().getFormat("#.0"))

    val booleanStyle = wb.createCellStyle()
    booleanStyle.setDataFormat(wb.getCreationHelper.createDataFormat().getFormat("\"kyllä\";;\"ei\";"))
    booleanStyle.setAlignment(HorizontalAlignment.LEFT)

    val columnSettingsWithIndex = dataSheet.columnSettings.zipWithIndex
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

    sh.createFreezePane(0, 1)

    for (rowNumber <- dataSheet.rows.indices) {
      val row = sh.createRow(rowNumber + 1)
      val dataRow = dataSheet.rows(rowNumber)
      var colNumber = 0
      for (f <- dataRow.productIterator) {
        val cell = row.createCell(colNumber)
        f match {
          case s: String => cell.setCellStyle(textStyle); cell.setCellValue(s)
          case Some(s: String) => cell.setCellStyle(textStyle); cell.setCellValue(s)
          case d: LocalDate => cell.setCellStyle(dateStyle); cell.setCellValue(Date.from(d.atStartOfDay(ZoneId.systemDefault).toInstant))
          case Some(d: LocalDate) => cell.setCellStyle(dateStyle); cell.setCellValue(Date.from(d.atStartOfDay(ZoneId.systemDefault).toInstant))
          case i: Int => cell.setCellValue(i)
          case f: Double => cell.setCellStyle(floatStyle); cell.setCellValue(f)
          case b: Boolean => cell.setCellStyle(booleanStyle); cell.setCellValue(if (b) 1 else 0)
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
  }

  private def writeDocumentationSheet(wb: SXSSFWorkbook, sh: SXSSFSheet, documentationSheet: DocumentationSheet): Unit = {
    val documentationStyle = wb.createCellStyle()
    documentationStyle.setWrapText(true)
    documentationStyle.setVerticalAlignment(VerticalAlignment.CENTER)

    val row = sh.createRow(0)
    sh.trackColumnForAutoSizing(0)
    val cell = row.createCell(0)
    cell.setCellStyle(documentationStyle)
    cell.setCellValue(documentationSheet.text)
    sh.autoSizeColumn(0)
  }
}

case class Column(title: String, width: Option[Int] = None)

sealed trait Sheet {
  def title: String
}

case class DataSheet(title: String, rows: Seq[Product], columnSettings: Seq[(String, Column)]) extends Sheet {
  def verifyColumnSettingsVsCaseClass(): Unit = {
    // validate that columnSettings matches case class structure
    if (rows.nonEmpty) {
      val namesFromCaseClass = rows.head.getClass.getDeclaredFields.map(_.getName).toList
      val namesFromSettings = columnSettings.map(_._1)
      if (namesFromCaseClass != namesFromSettings) {
        throw new IllegalArgumentException(s"columnSettings does not match case class: $namesFromCaseClass $namesFromSettings")
      }
    }
  }
  verifyColumnSettingsVsCaseClass()
}

case class DocumentationSheet(title: String, text: String) extends Sheet

case class WorkbookSettings(title: String, password: Option[String])
