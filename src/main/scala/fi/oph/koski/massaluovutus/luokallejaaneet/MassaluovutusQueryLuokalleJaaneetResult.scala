package fi.oph.koski.massaluovutus.luokallejaaneet

import fi.oph.koski.schema.{KoskiSchema, PerusopetuksenOpiskeluoikeus}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.JValue

import java.time.LocalDateTime

@Title("Luokalle jääneet -kyselyn vastaus")
case class MassaluovutusQueryLuokalleJaaneetResult(
  @Description("Oppijan oid")
  oppijaOid: String,
  @Description("Luokka-aste, jolle luokalle jääminen on merkitty")
  luokka: String,
  @Description("Opiskeluoikeuden tiedot")
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus,
)

object MassaluovutusQueryLuokalleJaaneetResult {
  def apply(m: LuokalleJääntiMatch, luokka: String, oppijaOid: String): MassaluovutusQueryLuokalleJaaneetResult =
    MassaluovutusQueryLuokalleJaaneetResult(
      oppijaOid = oppijaOid,
      luokka = luokka,
      opiskeluoikeus = m.perusopetuksenOpiskeluoikeus,
    )

  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[MassaluovutusQueryLuokalleJaaneetResult]).asInstanceOf[ClassSchema])
}
