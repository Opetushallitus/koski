package fi.oph.koski.todistus

import java.io.File

/**
 * Command-line tool for analyzing PDF signatures.
 *
 * Wrapper script: See scripts/analyze-pdf-signature.sh
 */
object PdfSignatureAnalyzerCLI {

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      printUsage()
      System.exit(1)
    }

    val pdfFilePath = args(0)

    val pdfFile = new File(pdfFilePath)

    if (!pdfFile.exists()) {
      println(s"Error: File not found: $pdfFilePath")
      System.exit(1)
    }

    if (!pdfFile.isFile) {
      println(s"Error: Path is not a file: $pdfFilePath")
      System.exit(1)
    }

    if (!pdfFile.canRead) {
      println(s"Error: Cannot read file: $pdfFilePath")
      System.exit(1)
    }

    println(s"Analyzing PDF: $pdfFilePath")
    println()

    PdfSignatureAnalyzer.analyzePdfFile(pdfFile) match {
      case scala.util.Success(report) =>
        println(report.summary)

        if (!report.overallValid) {
          System.exit(2)
        }

      case scala.util.Failure(exception) =>
        println(s"Error during analysis: ${exception.getMessage}")
        exception.printStackTrace()
        System.exit(3)
    }
  }

  private def printUsage(): Unit = {
    println("PDF Signature Analyzer - Analyze PDF signatures")
    println()
    println("Usage:")
    println("  mvn exec:java -Dexec.mainClass=\"fi.oph.koski.todistus.PdfSignatureAnalyzerCLI\" -Dexec.args=\"<pdf-file>\" -Dexec.classpathScope=compile")
    println()
    println("Parameters:")
    println("  pdf-file   Path to the PDF file to analyze")
    println()
    println("Exit codes:")
    println("  0 - Analysis successful and PDF is valid")
    println("  1 - Invalid parameters")
    println("  2 - Analysis successful but PDF has issues")
    println("  3 - Analysis failed")
    println()
    println("Examples:")
    println("  mvn exec:java -Dexec.mainClass=\"fi.oph.koski.todistus.PdfSignatureAnalyzerCLI\" -Dexec.args=\"certificate.pdf\" -Dexec.classpathScope=compile")
  }
}
