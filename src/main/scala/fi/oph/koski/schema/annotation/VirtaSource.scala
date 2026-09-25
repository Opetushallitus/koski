package fi.oph.koski.schema.annotation

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST.{JObject, JString}

case class VirtaSource(path: String, rule: String = "") extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject): JObject = {
    val virta = JObject(("path" -> JString(path)) :: (if (rule.isEmpty) Nil else List("rule" -> JString(rule))))
    appendToDescription(obj.merge(JObject("virta" -> virta)), VirtaSource.clause(path, rule))
  }
}

case class VirtaDerived(rule: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject): JObject =
    appendToDescription(obj.merge(JObject("virta" -> JObject("derived" -> JString(rule)))), VirtaSource.clause("johdettu Koskessa", rule))
}

// Kenttäkohtainen pidempi selite, kun polku ja lyhyt sääntö eivät riitä. json4s merge yhdistää
// sisäkkäiset objektit, joten järjestyksellä VirtaSource/VirtaNote ei ole väliä.
case class VirtaNote(text: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject): JObject =
    obj.merge(JObject("virta" -> JObject("note" -> JString(text))))
}

object VirtaSource {
  def clause(source: String, rule: String): String =
    if (rule.isEmpty) s"(Virta: $source)" else s"(Virta: $source — $rule)"
}
