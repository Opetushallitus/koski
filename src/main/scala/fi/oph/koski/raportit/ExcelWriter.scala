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
import org.apache.poi.xssf.streaming.{SXSSFCell, SXSSFSheet, SXSSFWorkbook}
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
          case ds: SheetWithColumnSettings => writeDataSheet(wb, sh, ds)
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

  private def writeDataSheet(wb: SXSSFWorkbook, sh: SXSSFSheet, dataSheet: SheetWithColumnSettings): Unit = {
    addIgnoredErrors(sh)

    val headingRow = createHeadingRow(sh)
    val writeToHeadingCell = createHeadingCellWriter(wb, sh)
    for (columnAndIndex <- dataSheet.columnSettingsWithIndex) {
      val cell = headingRow.createCell(columnAndIndex._2)
      writeToHeadingCell(columnAndIndex, cell)
    }

    //sets column names visible when user is scrolling through rows
    sh.createFreezePane(0, 1)

    val writeDataToCell = createDataCellWriter(wb)

    for (rowNumber <- dataSheet.rows.indices) {
      val row = sh.createRow(rowNumber + 1)
      val dataRow = dataSheet.rowIterator(rowNumber)
      var colNumber = 0
      for (data <- dataRow) {
        val cell = row.createCell(colNumber)
        writeDataToCell(data, cell)
        colNumber += 1
      }
    }

    for ((cs, col) <- dataSheet.columnSettingsWithIndex) {
      if (cs.width.isEmpty) {
        sh.autoSizeColumn(col)
      }
    }
  }

  private def addIgnoredErrors(sh: SXSSFSheet) = {
    // SXSSFSheet does not expose "addIgnoredErrors" method. Hack around this.
    // based on https://stackoverflow.com/questions/47477912/apache-poi-how-to-use-addignorederrors-functionality-in-sxssfsheet
    val hiddenShField = classOf[SXSSFSheet].getDeclaredField("_sh")
    hiddenShField.setAccessible(true)
    hiddenShField.get(sh).asInstanceOf[XSSFSheet].addIgnoredErrors(new CellRangeAddress(0, 999999, 0, 999), IgnoredErrorType.NUMBER_STORED_AS_TEXT)
  }

  private def createHeadingRow(sh: SXSSFSheet) = {
    val headingRow = sh.createRow(0)
    headingRow.setHeightInPoints(25)
    headingRow
  }

  type HeadingCellWriter = ((Column, Int), SXSSFCell) => Unit
  private def createHeadingCellWriter(wb: SXSSFWorkbook, sh: SXSSFSheet): HeadingCellWriter = {
    val headingStyle = wb.createCellStyle()
    headingStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex)
    headingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    headingStyle.setVerticalAlignment(VerticalAlignment.CENTER)

    (columnAndIndex: (Column, Int), cell: SXSSFCell) => setHeadingCellValue(columnAndIndex, cell, headingStyle, sh)
  }

  private def setHeadingCellValue(columnAndIndex: (Column, Int), cell: SXSSFCell, headingStyle: CellStyle, sh: SXSSFSheet) = {
    val (column, columnIndex) = columnAndIndex
    cell.setCellStyle(headingStyle)
    cell.setCellValue(column.title)
    column.width match {
      case None => sh.trackColumnForAutoSizing(columnIndex)
      case Some(w) => sh.setColumnWidth(columnIndex, w)
    }
  }

  type DataCellWriter = (Any, SXSSFCell) => Unit
  private def createDataCellWriter(wb: SXSSFWorkbook): DataCellWriter = {
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

    (data: Any, cell: SXSSFCell) => setDataCellValue(data, cell, textStyle, dateStyle, floatStyle, booleanStyle)
  }

  private def setDataCellValue(data: Any, cell: SXSSFCell, textStyle: CellStyle, dateStyle: CellStyle, floatStyle: CellStyle, booleanStyle: CellStyle): Unit = data match {
    case Some(x) => setDataCellValue_(x, cell, textStyle, dateStyle, floatStyle, booleanStyle)
    case None => /* do nothing */
    case x => setDataCellValue_(x, cell, textStyle, dateStyle, floatStyle, booleanStyle)
  }

  private def setDataCellValue_(data: Any, cell: SXSSFCell, textStyle: CellStyle, dateStyle: CellStyle, floatStyle: CellStyle, booleanStyle: CellStyle) = data match {
    case s: String => setCellStyleAndValue(cell, s, textStyle)
    case d: LocalDate => setCellStyleAndValue(cell, d, dateStyle)
    case i: Int => cell.setCellValue(i)
    case f: Double => setCellStyleAndValue(cell, f, floatStyle)
    case b: Boolean => cell.setCellStyle(booleanStyle); cell.setCellValue(if (b) 1 else 0)
    case x: Any => throw new IllegalStateException("Not handled yet? " + x.toString)
  }

  private def setCellStyleAndValue(cell: SXSSFCell, s: String, cs: CellStyle) {
    cell.setCellStyle(cs)
    cell.setCellValue(s)
  }

  private def setCellStyleAndValue(cell: SXSSFCell, d: LocalDate, cs: CellStyle) {
    cell.setCellStyle(cs)
    cell.setCellValue(Date.from(d.atStartOfDay(ZoneId.systemDefault).toInstant))
  }

  private def setCellStyleAndValue(cell: SXSSFCell, f: Double, cs: CellStyle) {
    if (f != 0) {
      cell.setCellStyle(cs)
    } else {
      // use the default style
    }
    cell.setCellValue(f)
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

object CompactColumn {
  def apply(title: String): Column = Column(title, width = Some(2000))
}

sealed trait Sheet {
  def title: String
}

trait SheetWithColumnSettings extends Sheet {
  def columnSettingsWithIndex: Seq[(Column, Int)]
  def rowIterator(rowIndex: Int): Iterator[Any]
  def rows: Seq[Any]
}

case class DataSheet(title: String, rows: Seq[Product], columnSettings: Seq[(String, Column)]) extends SheetWithColumnSettings {
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

  override def columnSettingsWithIndex: Seq[(Column, Int)] =
    columnSettings.zipWithIndex.map { case ((x, column), index) => (column, index)}

  override def rowIterator(rowIndex: Int): Iterator[Any] = rows(rowIndex).productIterator
}

case class DynamicDataSheet(title: String, rows: Seq[Seq[Any]], columnSettings: Seq[Column]) extends SheetWithColumnSettings {
  override def columnSettingsWithIndex: Seq[(Column, Int)] = columnSettings.zipWithIndex
  override def rowIterator(rowIndex: Int): Iterator[Any] = rows(rowIndex).iterator
}

case class DocumentationSheet(title: String, text: String) extends Sheet

case class WorkbookSettings(title: String, password: Option[String])
