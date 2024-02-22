package fi.oph.koski.kyselyt

import fi.oph.koski.json.JsonSerializer
import org.json4s.JValue
import software.amazon.awssdk.http.ContentStreamProvider

import java.io.InputStream
import java.util.UUID
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

case class QueryResultWriter(
  queryId: UUID,
  results: KyselyTulosRepository,
) {
  var files: mutable.Queue[String] = mutable.Queue[String]()

  def putJson(name: String, json: String): Unit =
    results.put(
      queryId = queryId,
      name = newFile(name, "json"),
      provider = StringStreamProvider(json),
      contentType = "application/json",
    )

  def putJson[T: TypeTag](name: String, obj: T): Unit =
    putJson(name, JsonSerializer.writeWithRoot(obj))

  private def newFile(name: String, ext: String): String = {
    val filename = s"$name.$ext"
    if (files.contains(filename)) {
      throw new RuntimeException(s"$filename already exists")
    }
    files += filename
    filename
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
