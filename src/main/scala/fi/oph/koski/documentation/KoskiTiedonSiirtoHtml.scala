package fi.oph.koski.documentation

import java.io.FileNotFoundException

import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema.ClassSchema

import scala.io.Source

object KoskiTiedonSiirtoHtml {

  val categoryNames: Seq[String] = Seq(
    "Esiopetus",
    "Perusopetukseen valmistava opetus",
    "Perusopetus",
    "Perusopetuksen lisäopetus",
    "Lukiokoulutus",
    "IB-koulutus",
    "Ammatillinen koulutus",
    "Valmentava koulutus",
    "Vapaa sivistystyö",
    "Korkeakoulu (Virrasta)",
    "Ylioppilastutkinto (Ylioppilastutkintorekisteristä)",
    "Tutkintokoulutukseen valmentava koulutus"
  )

  val categoryExamples: Map[String, List[Example]] = Map(
    "Esiopetus" -> ExamplesEsiopetus.examples,
    "Perusopetukseen valmistava opetus" -> ExamplesPerusopetukseenValmistavaOpetus.examples,
    "Perusopetus" -> ExamplesPerusopetus.examples,
    "Perusopetuksen lisäopetus" -> ExamplesPerusopetuksenLisaopetus.examples,
    "Lukiokoulutus" -> (ExamplesLukio.examples ++ ExamplesLukioonValmistavaKoulutus.examples),
    "IB-koulutus" -> ExamplesIB.examples,
    "Ammatillinen koulutus" -> ExamplesAmmatillinen.examples,
    "Valmentava koulutus" -> (ExamplesValma.examples ++ ExamplesTelma.examples),
    "Vapaa sivistystyö" -> ExamplesVapaaSivistystyö.examples,
    "Korkeakoulu (Virrasta)" -> ExamplesKorkeakoulu.examples,
    "Ylioppilastutkinto (Ylioppilastutkintorekisteristä)" -> ExamplesYlioppilastutkinto.examples,
    "Tutkintokoulutukseen valmentava koulutus" -> ExamplesTutkintokoulutukseenValmentavaKoulutus.examples,
  )

  val jsonTableHtmlContentsCache: collection.mutable.Map[(String, String), String] = collection.mutable.Map()

  def jsonTableHtmlContents(categoryName: String, exampleName: String): Option[String] = {
    val key = (categoryName, exampleName)
    if (!jsonTableHtmlContentsCache.contains(key)) {
      categoryExamples.get(categoryName).flatMap(_.find(_.name == exampleName)) match {
        case Some(v) => {
          val rows = SchemaToJsonHtml.buildHtml(KoskiSchema.schema.asInstanceOf[ClassSchema], v.data)
          val result = rows.map(_.toString()).mkString("")
          jsonTableHtmlContentsCache.update(key, result)
        }
        case None => return None
      }
    }
    jsonTableHtmlContentsCache.get(key)
  }

  private def markdownResource(name: String): String = {
    try {
      val src = Source.fromResource(name)
      val value = src.mkString
      src.close()
      value
    } catch {
      case e: Exception => throw new FileNotFoundException(name)
    }
  }

  val apiOperations: List[ApiOperation] = KoskiApiOperations.operations

  val htmlTextSections = Seq(
    "yleista",
    "tietomalli",
    "tietomalli_esimerkit",
    "koodistot",
    "rajapinnat_oppilashallintojarjestelmat",
    "rajapinnat_luovutuspalvelu",
    "rajapinnat_palveluvayla_omadata"
  ).map(name => (name, Markdown.markdownToXhtmlString(markdownResource(s"documentation/$name.md")))).toMap
}
