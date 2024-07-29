package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.luovutuspalvelu.opiskeluoikeus._
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

case class HslResponse(
  henkilö: HslHenkilo,
  opiskeluoikeudet: List[HslOpiskeluoikeus],
  suostumuksenPaattymispaiva: Option[LocalDate]
)

object HslResponse {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[HslOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.eb.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.esiopetus.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.luva.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.taiteenperusopetus.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.tuva.koodiarvo,
    // schema.OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo
  )
}
