package fi.oph.tor.json

import fi.oph.tor.log.Logging
import org.http4s._
import org.http4s.headers.`Content-Type`
import org.json4s.Formats
import org.json4s.jackson.Serialization._

import scalaz.\/._

object Json4sHttp4s extends Logging {
  private def parseJson4s[A] (json:String)(implicit formats: Formats, mf: Manifest[A]) = scala.util.Try(read[A](json)).map(right).recover {
    case t: Throwable =>
      logger.error(s"json decoding failed for ${json}", t)
      left(ParseFailure("json decoding failed", t.getMessage))
  }.get

  def json4sOf[A](implicit formats: Formats, mf: Manifest[A]): EntityDecoder[A] = EntityDecoder.decodeBy[A](MediaType.`application/json`){(msg) =>
    DecodeResult(EntityDecoder.decodeString(msg)(Charset.`UTF-8`).map(parseJson4s[A]))
  }

  def json4sEncoderOf[A <: AnyRef](implicit formats: Formats, mf: Manifest[A]): EntityEncoder[A] = EntityEncoder.stringEncoder(Charset.`UTF-8`).contramap[A](item => write[A](item))
    .withContentType(`Content-Type`(MediaType.`application/json`))
}
