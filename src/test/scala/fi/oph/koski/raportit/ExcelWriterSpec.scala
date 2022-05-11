package fi.oph.koski.raportit

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.localization.{LocalizationReader, LocalizationRepository}
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.ss.usermodel._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.{File, FileOutputStream}
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.collection.JavaConverters._


class ExcelWriterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "ExcelWriter" - {

    "Simple Excel Writing test" in {
      noException shouldBe thrownBy(
        withExcel(TestCase()) { wb =>
          wb.getNumberOfSheets should equal(1)
          wb.getSheetAt(0).getSheetName should equal("       datasheet_title")
        }
      )
    }
  }

  private lazy val data = "data"

  private def TestCase() = {
    val workbookSettings = WorkbookSettings(expectedExcelTitle, Some("PASSWORD"))
    val dynamicRows= Seq(Seq("foo"))
    val dynamicColumnSettings = Seq(Column(s"${data}bar", comment = Some(data)))
    val dataSheet = DynamicDataSheet(s"${data}datasheet_title", dynamicRows, dynamicColumnSettings)
    (workbookSettings, Seq(dataSheet))
  }


  private def withExcel(params: (WorkbookSettings, Seq[Sheet]))(tests: (Workbook => Unit)): Unit = {
    val (workbookSettings, sheets) = params
    val file = new File("excel_file_for_tests.xlsx")
    val outputStream = new FileOutputStream(file)
    val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
    val booleanTextValues = ExcelWriter.BooleanCellStyleLocalizedValues(t)
    try {
      ExcelWriter.writeExcel(workbookSettings, sheets, booleanTextValues, outputStream)
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
  )

  lazy val expectedExcelTitle = "expected_excel_title"
  lazy val excelPassword = "kalasana"
  lazy val mockDataColumnSettings: Seq[(String, Column)] = Seq(
    "str" -> Column("Str", comment = Some("kommentti"))
  )

  lazy val mockDataColumnSettingsWithGrouping: Seq[(String, Column)] = Columns.flattenGroupingColumns(List(
    "str" -> Column("Str", comment = Some("kommentti"))
  ))
}
