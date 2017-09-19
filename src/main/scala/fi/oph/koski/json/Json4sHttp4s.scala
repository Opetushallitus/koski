package fi.oph.koski.json

import fi.oph.koski.log.Logging
import org.http4s._
import org.http4s.headers.`Content-Type`

import scala.reflect.runtime.universe.TypeTag

object Json4sHttp4s extends Logging {
  def json4sEncoderOf[A : TypeTag]: EntityEncoder[A] = EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[A](item => JsonSerializer.writeWithRoot[A](item))
    .withContentType(`Content-Type`(MediaType.`application/json`))
  def multiLineJson4sEncoderOf[A : TypeTag]: EntityEncoder[Seq[A]] = EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[Seq[A]](itemList => itemList.map(JsonSerializer.writeWithRoot[A](_)).mkString("\n") + "\n")
    .withContentType(`Content-Type`(MediaType.`application/json`))

}
