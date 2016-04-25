package fi.oph.tor.json

import fi.oph.tor.log.Logging
import org.http4s._
import org.http4s.headers.`Content-Type`
import org.json4s.Formats
import org.json4s.jackson.Serialization._

object Json4sHttp4s extends Logging {
  def json4sEncoderOf[A <: AnyRef](implicit formats: Formats, mf: Manifest[A]): EntityEncoder[A] = EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[A](item => write[A](item))
    .withContentType(`Content-Type`(MediaType.`application/json`))
}
