package fi.oph.tor.json

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.KoodistoPalvelu
import fi.oph.tor.schema._
import org.json4s._
import org.json4s.reflect.TypeInfo

object KoodistoResolvingExtractor {
  def extract[T](json: JValue)(implicit mf: Manifest[T], koodistoPalvelu: KoodistoPalvelu): Either[HttpStatus, T] = {
    ContextualExtractor.extract[T, KoodistoPalvelu, HttpStatus](json, koodistoPalvelu)(mf, Json.jsonFormats + KoodistoResolvingDeserializer).left.map { statii =>
      HttpStatus.fold(statii)
    }
  }

  def koodistoPalvelu: Option[KoodistoPalvelu] = ContextualExtractor.context

  def resolveFailed(viite: KoodistoKoodiViite): KoodistoKoodiViite = ContextualExtractor.parseError(HttpStatus.badRequest("Koodia " + viite + " ei löydy koodistosta"))
}

object KoodistoResolvingDeserializer extends Deserializer[KoodistoKoodiViite] {
  private val TheClass = classOf[KoodistoKoodiViite]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), KoodistoKoodiViite] = {
    case (TypeInfo(TheClass, _), json) =>
      val viite = json.extract[KoodistoKoodiViite](Json.jsonFormats, Manifest.classType(TheClass))
      KoodistoResolvingExtractor.koodistoPalvelu match {
        case Some(koodistoPalvelu) => KoodistoPalvelu.validate(koodistoPalvelu, viite) match {
          case Some(viite) => viite
          case None => KoodistoResolvingExtractor.resolveFailed(viite)
        }
        case _ => viite
      }
  }
}