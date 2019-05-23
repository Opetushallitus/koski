package fi.oph.koski.raportit

import java.io.FileOutputStream

import org.scalatest.{FreeSpec, Matchers}
import org.apache.poi.ss.usermodel._
import java.io.File
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import org.apache.poi.EncryptedDocumentException

import scala.collection.JavaConverters._


class ExcelWriterSpec extends FreeSpec with Matchers {

  "ExcelWriter" - {

    "Excelin avaus vaatii salasanan" in {
      an[EncryptedDocumentException] should be thrownBy (withExcel(dataSheetAndDocumentationSheetTestCase(password = "wrong_password")) { _ => 1 should equal(1) })
    }

    "Excelin luonti, DataSheet ja DocumentationSheet" - {
      withExcel(dataSheetAndDocumentationSheetTestCase()) { wb =>
        "Luo data ja dokumentaatio välilehdet" in {
          wb.getNumberOfSheets should equal(2)
          wb.getSheetAt(0).getSheetName should equal("data_sheet_title")
          wb.getSheetAt(1).getSheetName should equal("documentation_sheet_title")
        }
        "DataSheet" - {
          "otsikko kolumnit" in {
            val headingRow = wb.getSheet("data_sheet_title").iterator.next

            headingRow.getPhysicalNumberOfCells should equal(10)

            headingRow.getCell(0).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(0).getStringCellValue should equal("Str")

            headingRow.getCell(1).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(1).getStringCellValue should equal("OptionStr")

            headingRow.getCell(2).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(2).getStringCellValue should equal("LocalDate")

            headingRow.getCell(3).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(3).getStringCellValue should equal("OptionLocalDate")

            headingRow.getCell(4).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(4).getStringCellValue should equal("Int")

            headingRow.getCell(5).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(5).getStringCellValue should equal("OptionInt")

            headingRow.getCell(6).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(6).getStringCellValue should equal("Double")

            headingRow.getCell(7).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(7).getStringCellValue should equal("OptionDouble")

            headingRow.getCell(8).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(8).getStringCellValue should equal("Boolean")

            headingRow.getCell(9).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(9).getStringCellValue should equal("OptionBoolean")
          }
          "Luo otsikko riville kommentit" in {
            val headingRow = wb.getSheet("data_sheet_title").iterator.next
            headingRow.getCell(0).getCellComment.getString.getString should equal("kommentti")
          }
          "Ei luo kommenttia jos sitä ei ole määritelty" in {
            val headingRow = wb.getSheet("data_sheet_title").iterator.next
            headingRow.iterator.asScala.drop(1).foreach(_.getCellComment should equal(null))
          }
          "Data solujen formatointi" - {
            val dataSheetIterator = wb.getSheet("data_sheet_title").iterator
            val _ = dataSheetIterator.next //skip heading row
            val firstDataRow = dataSheetIterator.next
            val secondDataRow = dataSheetIterator.next

            "Stringit" in {
              firstDataRow.getCell(0).getStringCellValue should equal("foo")
              firstDataRow.getCell(1).getStringCellValue should equal("bar")

              secondDataRow.getCell(0).getStringCellValue should equal("foo")
              secondDataRow.getCell(1).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Päivämäärät" in {
              firstDataRow.getCell(2).getCellStyle.getDataFormatString should equal("yyyy-MM-dd")
              firstDataRow.getCell(2).getDateCellValue should equal(Date.valueOf(date(2000, 1, 1)))
              firstDataRow.getCell(3).getCellStyle.getDataFormatString should equal("yyyy-MM-dd")
              firstDataRow.getCell(3).getDateCellValue should equal(Date.valueOf(date(2000, 2, 2)))

              secondDataRow.getCell(2).getCellStyle.getDataFormatString should equal("yyyy-MM-dd")
              secondDataRow.getCell(2).getDateCellValue should equal(Date.valueOf(date(2000, 1, 1)))
              secondDataRow.getCell(3).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Intit" in {
              firstDataRow.getCell(4).getNumericCellValue should equal(1)
              firstDataRow.getCell(5).getNumericCellValue should equal(2)

              secondDataRow.getCell(4).getNumericCellValue should equal(1)
              secondDataRow.getCell(5).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Doublet" in {
              firstDataRow.getCell(6).getCellStyle.getDataFormatString should equal("#.0")
              firstDataRow.getCell(6).getNumericCellValue should equal(2.0)
              firstDataRow.getCell(7).getCellStyle.getDataFormatString should equal("#.0")
              firstDataRow.getCell(7).getNumericCellValue should equal(3.0)

              secondDataRow.getCell(6).getCellStyle.getDataFormatString should equal("General")
              secondDataRow.getCell(6).getNumericCellValue should equal(0)
              secondDataRow.getCell(7).getCellStyle.getDataFormatString should equal("General")
              secondDataRow.getCell(7).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Booleanit" in {
              firstDataRow.getCell(8).getCellStyle.getDataFormatString should equal("\"kyllä\";;\"ei\";")
              firstDataRow.getCell(8).getNumericCellValue should equal(1)
              firstDataRow.getCell(9).getCellStyle.getDataFormatString should equal("\"kyllä\";;\"ei\";")
              firstDataRow.getCell(9).getNumericCellValue should equal(0)

              secondDataRow.getCell(8).getCellStyle.getDataFormatString should equal("\"kyllä\";;\"ei\";")
              secondDataRow.getCell(8).getNumericCellValue should equal(1)
              secondDataRow.getCell(9).getCellTypeEnum should equal(CellType.BLANK)
            }
          }
        }
        "Kirjoittaa dokumentaation" in {
          val firstRow = wb.getSheet("documentation_sheet_title").iterator.next
          firstRow.getCell(0).getStringCellValue should equal("dokumentaatio")
        }
      }
    }
    "DynamicDataSheet" - {
      withExcel(dynamicDataSheetAndDocumentationSheet()) { wb =>
        "Luo data ja dokumentaatio välilehdet" in {
          wb.getNumberOfSheets should equal(2)
          wb.getSheetAt(0).getSheetName should equal("data_sheet_title")
          wb.getSheetAt(1).getSheetName should equal("documentation_sheet_title")
        }
        "Datat" - {
          "otsikko kolumnit" in {
            val headingRow = wb.getSheet("data_sheet_title").iterator.next

            headingRow.getPhysicalNumberOfCells should equal(10)

            headingRow.getCell(0).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(0).getStringCellValue should equal("Str")

            headingRow.getCell(1).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(1).getStringCellValue should equal("OptionStr")

            headingRow.getCell(2).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(2).getStringCellValue should equal("LocalDate")

            headingRow.getCell(3).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(3).getStringCellValue should equal("OptionLocalDate")

            headingRow.getCell(4).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(4).getStringCellValue should equal("Int")

            headingRow.getCell(5).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(5).getStringCellValue should equal("OptionInt")

            headingRow.getCell(6).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(6).getStringCellValue should equal("Double")

            headingRow.getCell(7).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(7).getStringCellValue should equal("OptionDouble")

            headingRow.getCell(8).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(8).getStringCellValue should equal("Boolean")

            headingRow.getCell(9).getCellTypeEnum should equal(CellType.STRING)
            headingRow.getCell(9).getStringCellValue should equal("OptionBoolean")
          }
          "Data solujen formatointi" - {
            val dataSheetIterator = wb.getSheet("data_sheet_title").iterator
            val _ = dataSheetIterator.next //skip heading row
            val firstDataRow = dataSheetIterator.next
            val secondDataRow = dataSheetIterator.next

            "Stringit" in {
              firstDataRow.getCell(0).getStringCellValue should equal("foo")
              firstDataRow.getCell(1).getStringCellValue should equal("bar")

              secondDataRow.getCell(0).getStringCellValue should equal("foo")
              secondDataRow.getCell(1).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Päivämäärät" in {
              firstDataRow.getCell(2).getCellStyle.getDataFormatString should equal("yyyy-MM-dd")
              firstDataRow.getCell(2).getDateCellValue should equal(Date.valueOf(date(2000, 1, 1)))
              firstDataRow.getCell(3).getCellStyle.getDataFormatString should equal("yyyy-MM-dd")
              firstDataRow.getCell(3).getDateCellValue should equal(Date.valueOf(date(2000, 2, 2)))

              secondDataRow.getCell(2).getCellStyle.getDataFormatString should equal("yyyy-MM-dd")
              secondDataRow.getCell(2).getDateCellValue should equal(Date.valueOf(date(2000, 1, 1)))
              secondDataRow.getCell(3).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Intit" in {
              firstDataRow.getCell(4).getNumericCellValue should equal(1)
              firstDataRow.getCell(5).getNumericCellValue should equal(2)

              secondDataRow.getCell(4).getNumericCellValue should equal(1)
              secondDataRow.getCell(5).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Doublet" in {
              firstDataRow.getCell(6).getCellStyle.getDataFormatString should equal("#.0")
              firstDataRow.getCell(6).getNumericCellValue should equal(2.0)
              firstDataRow.getCell(7).getCellStyle.getDataFormatString should equal("#.0")
              firstDataRow.getCell(7).getNumericCellValue should equal(3.0)

              secondDataRow.getCell(6).getCellStyle.getDataFormatString should equal("General")
              secondDataRow.getCell(6).getNumericCellValue should equal(0)
              secondDataRow.getCell(7).getCellStyle.getDataFormatString should equal("General")
              secondDataRow.getCell(7).getCellTypeEnum should equal(CellType.BLANK)
            }
            "Booleanit" in {
              firstDataRow.getCell(8).getCellStyle.getDataFormatString should equal("\"kyllä\";;\"ei\";")
              firstDataRow.getCell(8).getNumericCellValue should equal(1)
              firstDataRow.getCell(9).getCellStyle.getDataFormatString should equal("\"kyllä\";;\"ei\";")
              firstDataRow.getCell(9).getNumericCellValue should equal(0)

              secondDataRow.getCell(8).getCellStyle.getDataFormatString should equal("\"kyllä\";;\"ei\";")
              secondDataRow.getCell(8).getNumericCellValue should equal(1)
              secondDataRow.getCell(9).getCellTypeEnum should equal(CellType.BLANK)
            }
          }
          "Kirjoittaa dokumentaation" in {
            val firstRow = wb.getSheet("documentation_sheet_title").iterator.next
            firstRow.getCell(0).getStringCellValue should equal("dokumentaatio")
          }
        }
      }
    }
  }

  private def dataSheetAndDocumentationSheetTestCase(password: String = excelPassword): (WorkbookSettings, Seq[Sheet]) = {
    val workbookSettings = WorkbookSettings(expectedExcelTitle, Some(password))
    val mockDataRows = Seq(
      MockDataRow("foo", Some("bar"), date(2000, 1, 1), Some(date(2000, 2, 2)), 1, Some(2), 2.0, Some(3.0), true, Some(false)),
      MockDataRow("foo", None, date(2000, 1, 1), None, 1, None, 0.0, None, true, None)
    )

    val dataSheet = DataSheet("data_sheet_title", mockDataRows, mockDataColumnSettings)
    val documentationSheet = DocumentationSheet("documentation_sheet_title", "dokumentaatio")

    (workbookSettings, Seq(dataSheet, documentationSheet))
  }

  private def dynamicDataSheetAndDocumentationSheet(password: String = excelPassword): (WorkbookSettings, Seq[Sheet]) = {
    val workbookSettings = WorkbookSettings(expectedExcelTitle, Some(password))
    val dynamicRows= Seq(
      Seq("foo", Some("bar"), date(2000, 1, 1), Some(date(2000, 2, 2)), 1, Some(2), 2.0, Some(3.0), true, Some(false)),
      Seq("foo", None, date(2000, 1, 1), None, 1, None, 0.0, None, true, None)
    )
    val dynamicColumnSettings = Seq(Column("Str"), Column("OptionStr"), Column("LocalDate"), Column("OptionLocalDate"), Column("Int"), Column("OptionInt"), Column("Double"), Column("OptionDouble"), Column("Boolean"), Column("OptionBoolean"))

    val dataSheet = DynamicDataSheet("data_sheet_title", dynamicRows, dynamicColumnSettings)
    val documentationSheet = DocumentationSheet("documentation_sheet_title", "dokumentaatio")

    (workbookSettings, Seq(dataSheet, documentationSheet))
  }

  private def withExcel(params: (WorkbookSettings, Seq[Sheet]))(tests: (Workbook => Unit)): Unit = {
    val (workbookSettings, sheets) = params
    val file = new File("excel_file_for_tests.xlsx")
    val outputStream = new FileOutputStream(file)
    try {
      ExcelWriter.writeExcel(workbookSettings, sheets, outputStream)
      val wb: Workbook = WorkbookFactory.create(file, excelPassword)
      tests(wb)
    } finally {
      outputStream.close
      file.delete
    }
  }

  private case class MockDataRow
  (
    str: String,
    optionStr: Option[String],
    localdate: LocalDate,
    optionLocaldate: Option[LocalDate],
    int: Int,
    optionInt: Option[Int],
    double: Double,
    optionDouble: Option[Double],
    boolean: Boolean,
    optionBoolean: Option[Boolean]
  )

  lazy val expectedExcelTitle = "expected_excel_title"
  lazy val excelPassword = "kalasana"
  lazy val mockDataColumnSettings: Seq[(String, Column)] = Seq(
    "str" -> Column("Str", comment = Some("kommentti")),
    "optionStr" -> Column("OptionStr"),
    "localdate" -> Column("LocalDate"),
    "optionLocaldate" -> Column("OptionLocalDate"),
    "int" -> Column("Int"),
    "optionInt" -> Column("OptionInt"),
    "double" -> Column("Double"),
    "optionDouble" -> Column("OptionDouble"),
    "boolean" -> Column("Boolean"),
    "optionBoolean" -> Column("OptionBoolean")
  )
}
