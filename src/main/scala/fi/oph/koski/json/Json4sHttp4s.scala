package fi.oph.koski.json

import cats.effect.IO
import org.http4s._
import org.http4s.headers.`Content-Type`

import scala.reflect.runtime.universe.TypeTag

object Json4sHttp4s {
  def json4sEncoderOf[A : TypeTag]: EntityEncoder[IO, A] =
    EntityEncoder
      .stringEncoder[IO](Charset.`UTF-8`)
      .contramap[A](item => JsonSerializer.writeWithRoot[A](item))
      .withContentType(`Content-Type`(MediaType.application.json))

  def multiLineJson4sEncoderOf[A : TypeTag]: EntityEncoder[IO, Seq[A]] =
    EntityEncoder
      .stringEncoder[IO](Charset.`UTF-8`)
      .contramap[Seq[A]](itemList => itemList.map(JsonSerializer.writeWithRoot[A](_)).mkString("\n") + "\n")
      .withContentType(`Content-Type`(MediaType.application.json))

}
