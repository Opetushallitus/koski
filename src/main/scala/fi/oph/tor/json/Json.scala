package fi.oph.tor.json

import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s.jackson.Serialization

object Json {
  implicit val jsonFormats = GenericJsonFormats.genericFormats

  def write(x: AnyRef): String = {
    Serialization.write(x);
  }

  def read[A](json: String)(implicit mf : scala.reflect.Manifest[A]) : A = {
    Serialization.read(json)
  }
}
