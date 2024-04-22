package fi.oph.koski.massaluovutus

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{DataSheet, ExcelWriter, OppilaitosRaporttiResponse}
import fi.oph.koski.util.CsvFormatter
import software.amazon.awssdk.core.internal.sync.FileContentStreamProvider
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.{BufferedOutputStream, FileOutputStream, InputStream, OutputStream}
import java.nio.file.{Files, Path}
import java.util.UUID
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag
import scala.util.Using

case class QueryResultWriter(
  queryId: UUID,
  queries: QueryRepository,
  results: MassaluovutusResultRepository,
) {
  var objectKeys: mutable.Queue[String] = mutable.Queue[String]()

  def putJson(name: String, json: String): Unit =
    results.putStream(
      queryId = queryId,
      name = newObjectKey(s"$name.json"),
      provider = StringStreamProvider(json),
      contentType = QueryFormat.json,
    )

  def putJson[T: TypeTag](name: String, obj: T)(implicit user: KoskiSpecificSession): Unit =
    putJson(name, JsonSerializer.write(obj))

  def createCsv[T <: Product](name: String)(implicit manager: Using.Manager): CsvStream[T] =
    manager(new CsvStream[T](s"$queryId-$name", file => results.putFile(
      queryId = queryId,
      name = newObjectKey(s"$name.csv"),
      file = file,
      contentType = QueryFormat.csv,
    )))

  def createStream(name: String, contentType: String)(implicit manager: Using.Manager): UploadStream =
    manager(new UploadStream(s"$queryId-$name", provider => results.putStream(
      queryId = queryId,
      name = newObjectKey(name),
      provider = provider,
      contentType = contentType,
    )))

  def putReport(report: OppilaitosRaporttiResponse, format: String, localizationReader: LocalizationReader)(implicit manager: Using.Manager): Unit = {
    format match {
      case QueryFormat.csv =>
        val datasheets = report.sheets.collect { case s: DataSheet => s }
        datasheets
          .foreach { sheet =>
            val name = if (datasheets.length > 1) {
              CsvFormatter.snakecasify(sheet.title)
            } else {
              report.filename.replace(".xlsx", "")
            }
            val csv = createCsv[Product](name)
            manager.acquire(csv)
            csv.put(sheet.rows)
            csv.save()
          }
      case QueryFormat.xlsx =>
        val upload = createStream(report.filename, format)
        manager.acquire(upload)
        ExcelWriter.writeExcel(
          report.workbookSettings,
          report.sheets,
          ExcelWriter.BooleanCellStyleLocalizedValues(localizationReader),
          upload.output,
        )
        upload.save()
      case format: Any =>
        throw new Exception(s"$format is not a supported datasheet export format")
    }
  }

  def patchMeta(meta: QueryMeta): QueryMeta = queries.patchMeta(queryId.toString, meta)

  private def newObjectKey(objectKey: String): String = {
    if (objectKeys.contains(objectKey)) {
      throw new RuntimeException(s"$objectKey already exists")
    }
    objectKeys += objectKey
    objectKey
  }
}

class CsvStream[T <: Product](name: String, upload: (Path) => Unit) extends AutoCloseable {
  val temporaryFile: Path = Files.createTempFile(s"$name-", ".csv")
  private val fileStream: FileOutputStream = new FileOutputStream(temporaryFile.toFile)
  private val outputStream: BufferedOutputStream = new BufferedOutputStream(fileStream)
  private var headerWritten: Boolean = false

  def put(data: T): Unit = {
    synchronized {
      if (!headerWritten) {
        headerWritten = true
        write(headerOf(data))
      }
    }
    write(recordOf(data))
  }

  def put(data: Seq[T]): Unit =
    data.foreach { put }

  def put(record: CsvRecord[T]): Unit = {
    synchronized {
      if (!headerWritten) {
        headerWritten = true
        write(CsvFormatter.formatRecord(record.csvFields))
      }
    }
    write(CsvFormatter.formatRecord(record.values))
  }

  def close(): Unit = {
    closeIntermediateStreams()
    deleteTemporaryFile()
  }

  def save(): Unit = {
    synchronized {
      closeIntermediateStreams()
      if (headerWritten) {
        upload(temporaryFile)
      }
      deleteTemporaryFile()
    }
  }

  private def closeIntermediateStreams(): Unit = {
    outputStream.close()
    fileStream.close()
  }

  private def deleteTemporaryFile(): Unit = {
    synchronized {
      if (Files.exists(temporaryFile)) {
        Files.delete(temporaryFile)
      }
    }
  }

  private def write(data: String): Unit =
    outputStream.write(data.getBytes("UTF-8"))

  private def recordOf(data: T): String =
    CsvFormatter.formatRecord(data.productIterator.to)

  private def headerOf(data: T): String =
    CsvFormatter.formatRecord(
      data.getClass
        .getDeclaredFields
        .map(_.getName)
        .map(CsvFormatter.snakecasify)
    )
}

class UploadStream(name: String, uploadWith: (ContentStreamProvider) => Unit) extends AutoCloseable {
  val temporaryFile: Path = Files.createTempFile(s"$name-", ".tmp")
  private val fileStream: FileOutputStream = new FileOutputStream(temporaryFile.toFile)
  private val outputStream: BufferedOutputStream = new BufferedOutputStream(fileStream)

  def output: OutputStream = outputStream

  def provider: ContentStreamProvider = new FileContentStreamProvider(temporaryFile)

  def save(): Unit = {
    closeIntermediateStreams()
    uploadWith(provider)
    deleteTemporaryFile()
  }

  def close(): Unit = {
    closeIntermediateStreams()
    deleteTemporaryFile()
  }

  private def closeIntermediateStreams(): Unit = {
    outputStream.close()
    fileStream.close()
  }

  private def deleteTemporaryFile(): Unit = {
    if (Files.exists(temporaryFile)) {
      Files.delete(temporaryFile)
    }
  }
}

case class StringStreamProvider(content: String) extends ContentStreamProvider {
  def newStream(): InputStream = ByteInputStream(content)
}

class ByteInputStream[T <: Byte](stream: Stream[T]) extends InputStream {
  private val iter = stream.iterator
  override def read(): Int = if (iter.hasNext) iter.next else -1
}

object ByteInputStream {
  def apply(string: String) = new ByteInputStream(string.getBytes("UTF-8").toStream)
}

abstract class CsvRecord[T <: Product](val self: T) {
  def get(fieldName: String): Option[Any]

  lazy val fields: Seq[String] =
    self
      .getClass
      .getDeclaredFields
      .map(_.getName)
      .toList

  def csvFields: Seq[String] =
    fields.map(CsvFormatter.snakecasify)

  def values: Seq[Any] =
    self
      .productIterator
      .toSeq
      .zip(fields)
      .map { case (default, field) => get(field).getOrElse(default) }
      .toList
}
