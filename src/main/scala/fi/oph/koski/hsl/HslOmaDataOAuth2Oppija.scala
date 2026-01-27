package fi.oph.koski.hsl

import fi.oph.koski.luovutuspalvelu.opiskeluoikeus.HslOpiskeluoikeus
import fi.oph.koski.omadataoauth2.{OmaDataOAuth2Henkilötiedot, OmaDataOAuth2TokenInfo}
import fi.oph.koski.schema
import fi.oph.scalaschema.annotation.Title
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object HslOmaDataOAuth2Oppija {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[HslOmaDataOAuth2Oppija]).asInstanceOf[ClassSchema])
}

@Title("Omadata OAuth2 HSL opiskeluoikeudet")
case class HslOmaDataOAuth2Oppija(
  henkilö: OmaDataOAuth2Henkilötiedot,
  opiskeluoikeudet: List[HslOpiskeluoikeus],
  tokenInfo: OmaDataOAuth2TokenInfo
)
