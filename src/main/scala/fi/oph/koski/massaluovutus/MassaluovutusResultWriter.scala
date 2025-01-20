package fi.oph.koski.massaluovutus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{DataSheet, ExcelWriter, OppilaitosRaporttiResponse}
import fi.oph.koski.util.CsvFormatter
import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import software.amazon.awssdk.core.internal.sync.FileContentStreamProvider
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.{BufferedOutputStream, FileOutputStream, InputStream, OutputStream}
import java.nio.file.{Files, Path}
import java.util.UUID
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag
import scala.util.Using

object QueryResultWriter {
  def defaultPartitionSize(config: Config): Long = {
    if (Environment.isServerEnvironment(config)) {
      1000000000L
    } else {
      50000L // Pieni partitiokoko testeille
    }
  }
}

case class QueryResultWriter(
  queryId: UUID,
  queries: QueryRepository,
  results: MassaluovutusResultRepository,
) {
  var objectKeys: mutable.Queue[String] = mutable.Queue[String]()
  private var predictedFileCount: Option[Int] = None

  def predictFileCount(count: Int): Unit = {
    predictedFileCount = Some(count)
  }

  def skipFile(): Unit = {
    predictedFileCount = predictedFileCount.map(_ - 1)
    updateProgress()
  }

  def putJson(name: String, json: String): Unit = {
    results.putStream(
      queryId = queryId,
      name = newObjectKey(s"$name.json"),
      provider = StringStreamProvider(json),
      contentType = QueryFormat.json,
    )
    updateProgress()
  }

  def putJson[T: TypeTag](name: String, obj: T)(implicit user: KoskiSpecificSession): Unit =
    putJson(name, JsonSerializer.write(obj))

  def putJson(name: String, json: JValue): Unit =
    putJson(name, JsonMethods.pretty(json))

  def createCsv[T <: Product](name: String, partitionSize: Option[Long])(implicit manager: Using.Manager): CsvStream[T] =
    manager(new CsvStream[T](s"$queryId-$name", partitionSize, { (file, partition) =>
      results.putFile(
        queryId = queryId,
        name = partition.fold(newObjectKey(s"$name.csv"))(i => newObjectKey(s"$name.csv.${"%02d".format(i + 1)}")),
        file = file,
        contentType = partition.fold(QueryFormat.csv)(_ => QueryFormat.csvPartition),
      )
      updateProgress()
    }))

  def createStream(name: String, contentType: String)(implicit manager: Using.Manager): UploadStream =
    manager(new UploadStream(s"$queryId-$name", { provider =>
      results.putStream(
        queryId = queryId,
        name = newObjectKey(name),
        provider = provider,
        contentType = contentType,
      )
      updateProgress()
    }))

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
            val csv = createCsv[Product](name, None)
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

  private def updateProgress(): Boolean = {
    queries.setProgress(
      id = queryId.toString,
      resultFiles = objectKeys.toList,
      progress = predictedFileCount
        .filter(_ > 0)
        .map(100 * objectKeys.size / _)
    )
  }
}

class CsvStream[T <: Product](name: String, partitionSize: Option[Long], upload: (Path, Option[Int]) => Unit) extends AutoCloseable {
  var temporaryFile: Path = Files.createTempFile(s"$name-", ".csv")
  private var fileStream: FileOutputStream = new FileOutputStream(temporaryFile.toFile)
  private var outputStream: BufferedOutputStream = new BufferedOutputStream(fileStream)
  private var headerWritten: Boolean = false
  private var partitionBytesWritten: Long = 0
  private var partition: Int = 0

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
        upload(temporaryFile, partitionSize.map { _ => partition })
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

  private def write(data: String): Unit = {
    val bytes = data.getBytes("UTF-8")

    partitionSize.foreach { maxSize =>
      if (partitionBytesWritten + bytes.length > maxSize) {
        createNewPartition()
      }
    }

    outputStream.write(bytes)
    partitionBytesWritten += bytes.length
  }

  private def createNewPartition(): Unit = {
    save()
    temporaryFile = Files.createTempFile(s"$name-", ".csv")
    fileStream = new FileOutputStream(temporaryFile.toFile)
    outputStream = new BufferedOutputStream(fileStream)
    partition += 1
    partitionBytesWritten = 0
  }

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
