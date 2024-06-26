package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema._
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

object HslOpiskeluoikeus {
  def apply(oo: Opiskeluoikeus): Option[HslOpiskeluoikeus] =
    (oo match {
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(HslAikuistenPerusopetuksenOpiskeluoikeus(o))
      case o: AmmatillinenOpiskeluoikeus => Some(HslAmmatillinenOpiskeluoikeus(o))
      case o: DIAOpiskeluoikeus => Some(HslDiaOpiskeluoikeus(o))
      case o: IBOpiskeluoikeus => Some(HslIBOpiskeluoikeus(o))
      case o: InternationalSchoolOpiskeluoikeus => Some(HslInternationalSchoolOpiskeluoikeus(o))
      case o: KorkeakoulunOpiskeluoikeus => Some(HslKorkeakoulunOpiskeluoikeus(o))
      case o: LukionOpiskeluoikeus => Some(HslLukionOpiskeluoikeus(o))
      case o: LukioonValmistavanKoulutuksenOpiskeluoikeus => Some(HslLukioonValmistavaKoulutus(o))
      case o: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => Some(HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(o))
      case o: PerusopetuksenLisäopetuksenOpiskeluoikeus => Some(HslPerusopetuksenLisäopetuksenOpiskeluoikeus(o))
      case o: PerusopetuksenOpiskeluoikeus => Some(HslPerusopetuksenOpiskeluoikeus(o))
      case o: YlioppilastutkinnonOpiskeluoikeus => Some(HslYlioppilastutkinnonOpiskeluoikeus(o))
      case _ => None
    })
}
