package fi.oph.koski.typemodel

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EnumValue, KoodistoEnumModelBuilder, LocalizedHtml}
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.scalaschema.annotation.SyntheticProperty
import org.scalatra.ContentEncodingSupport

import scala.collection.immutable

class TypeModelServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
  with RequiresVirkailijaOrPalvelukäyttäjä
  with ContentEncodingSupport
  with NoCache
{
  get("/constraints/:schemaClass") {
    val className = s"fi.oph.koski.schema.${params("schemaClass").replaceAll("[^\\w\\däÄöÖ]", "")}"
    val typeAndSubtypes = TypeExport.toTypeDef(Class.forName(className))
    val requestedType = typeAndSubtypes.find(_.fullClassName == className)
    val constraint = requestedType
      .map(t => Constraints.build(t, typeAndSubtypes))
      .getOrElse(AnyConstraint())

    renderObject[Constraint](constraint)
  }

  get[GroupedKoodistot]("/koodisto/:koodistoUri") {
    toGroupedKoodistoValues(getKooditFromRequestParams)
  }

  private def localization: LocalizedHtml =
    LocalizedHtml.get(session, application.koskiLocalizationRepository)

  private def getKooditFromRequestParams: List[Koodistokoodiviite] =
    koodistojenKoodit(koodistotByString(params("koodistoUri")))

  private def koodistojenKoodit(koodistot: List[KoodistoViite]): List[Koodistokoodiviite] =
    koodistot.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet)

  private def toGroupedKoodistoValues(koodit: List[Koodistokoodiviite]): GroupedKoodistot =
    GroupedKoodistot(
      koodit.groupBy(viite => {
        KoodistoEnumModelBuilder
          .koodistoName(viite)(localization, application.koodistoViitePalvelu)
          .getOrElse(viite.koodistoUri)
      })
    )

  private def koodistotByString(str: String): List[KoodistoViite] = {
    // note: silently omits non-existing koodistot from result
    val koodistoUriParts = str.split(",").toList
    koodistoUriParts flatMap { part: String =>
      application.koodistoViitePalvelu.getLatestVersionOptional(part)
    }
  }
}

case class GroupedKoodistot(
  koodistot: Map[String, List[Koodistokoodiviite]],
)
