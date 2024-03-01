package fi.oph.koski.queuedqueries

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.util.CsvFormatter
import software.amazon.awssdk.core.internal.sync.FileContentStreamProvider
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.{BufferedOutputStream, FileOutputStream, InputStream, OutputStream}
import java.nio.file.{Files, Path}
import java.util.UUID
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

case class QueryResultWriter(
  queryId: UUID,
  results: QueryResultsRepository,
) {
  var objectKeys: mutable.Queue[String] = mutable.Queue[String]()

  def putJson(name: String, json: String): Unit =
    results.put(
      queryId = queryId,
      name = newObjectKey(s"$name.json"),
      provider = StringStreamProvider(json),
      contentType = "application/json",
    )

  def putJson[T: TypeTag](name: String, obj: T): Unit =
    putJson(name, JsonSerializer.writeWithRoot(obj))

  def createCsv[T <: Product](name: String): CsvStream[T] =
    new CsvStream[T](s"$queryId-$name", provider => results.put(
      queryId = queryId,
      name = newObjectKey(s"$name.csv"),
      provider = provider,
      contentType = "text/csv",
    ))

  def createStream(name: String, contentType: String): UploadStream =
    new UploadStream(s"$queryId-$name", provider => results.put(
      queryId = queryId,
      name = newObjectKey(name),
      provider = provider,
      contentType = contentType,
    ))

  private def newObjectKey(objectKey: String): String = {
    if (objectKeys.contains(objectKey)) {
      throw new RuntimeException(s"$objectKey already exists")
    }
    objectKeys += objectKey
    objectKey
  }
}

class CsvStream[T <: Product](name: String, uploadWith: (ContentStreamProvider) => Unit) extends AutoCloseable {
  val temporaryFile: Path = Files.createTempFile(s"$name-", ".csv")
  private val fileStream: FileOutputStream = new FileOutputStream(temporaryFile.toFile)
  private val outputStream: BufferedOutputStream = new BufferedOutputStream(fileStream)
  private var headerWritten: Boolean = false

  def put(data: T): Unit = {
    if (!headerWritten) {
      headerWritten = true
      write(headerOf(data))
    }
    write(recordOf(data))
  }

  def put(data: Seq[T]): Unit =
    data.foreach { put }

  def close(): Unit = {
    closeIntermediateStreams()
    deleteTemporaryFile()
  }

  def save(): Unit = {
    closeIntermediateStreams()
    if (headerWritten) {
      uploadWith(provider)
    }
    deleteTemporaryFile()
  }

  def provider: ContentStreamProvider = new FileContentStreamProvider(temporaryFile)

  private def closeIntermediateStreams(): Unit = {
    outputStream.close()
    fileStream.close()
  }

  private def deleteTemporaryFile(): Unit = {
    if (Files.exists(temporaryFile)) {
      Files.delete(temporaryFile)
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
