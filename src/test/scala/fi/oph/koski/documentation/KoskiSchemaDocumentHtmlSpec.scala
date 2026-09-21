package fi.oph.koski.documentation

import fi.oph.koski.TestEnvironment
import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema}
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.ClassSchema
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.Node

class KoskiSchemaDocumentHtmlSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val html = KoskiSchemaDocumentHtml.html(
    focusEntities = { schema: ClassSchema => schema.simpleName == "korkeakoulunopiskeluoikeus" },
    expandEntities = { _: ClassSchema => false },
    lang = "fi",
    nonce = "nonce"
  )

  private lazy val entities: Seq[Node] = (html \\ "div").filter(_ \@ "class" == "entity")
  private def heading(entity: Node): Node = (entity \ "h3").head
  private def title(entity: Node): String = heading(entity).child.filter(_.label != "span").text.trim
  private def entitiesTitled(t: String): Seq[Node] = entities.filter(title(_) == t)
  private def row(entity: Node, key: String): Node =
    (entity \\ "tr").find(tr => (tr \ "td").headOption.exists(_.text.trim.startsWith(key))).get

  "Annotaatioilla rajatut saman luokan skeemat esitetään yhtenä entiteettinä" in {
    entitiesTitled("Koodistokoodiviite") should have length 1
    heading(entitiesTitled("Koodistokoodiviite").head) \@ "id" should equal("koodistokoodiviite")
  }

  "Ankkurit ovat yksikäsitteisiä" in {
    val ids = entities.map(heading(_) \@ "id")
    ids.diff(ids.distinct) should be(empty)
  }

  "Kaikki sivun sisäiset linkit osoittavat olemassa olevaan ankkuriin" in {
    val ids = entities.map(heading(_) \@ "id").toSet
    val targets = (html \\ "a").map(_ \@ "href").filter(_.startsWith("#")).map(href => java.net.URLDecoder.decode(href.drop(1), "UTF-8"))
    targets.filterNot(ids.contains) should be(empty)
  }

  "Sallitut arvot" - {
    "näytetään viittaavalla rivillä" in {
      val suunta = row(entitiesTitled("Liikkuvuusjakso").head, "suunta")
      suunta.text should include("virtaliikkuvuudensuunta")
      (suunta \\ "a").map(_ \@ "href") should contain("#koodistokoodiviite")
    }

    "kerrotaan käyttöpaikasta riippuviksi, kun entiteetin variantit eroavat" in {
      val koodistoUri = row(entitiesTitled("Koodistokoodiviite").head, "koodistoUri")
      koodistoUri.text should include("Sallitut arvot riippuvat käyttöpaikasta")
    }

    "näytetään entiteetissä, kun ne ovat kaikissa käyttöpaikoissa samat" in {
      val rootSchema = KoskiSchema.createSchema(classOf[YhdenKoodistonJuuri]).asInstanceOf[ClassSchema]
      val yksiKoodisto = KoskiSchemaDocumentHtml.html(lang = "fi", nonce = "nonce")(rootSchema)
      val koodistokoodiviite = (yksiKoodisto \\ "div").filter(_ \@ "class" == "entity").find(title(_) == "Koodistokoodiviite").get
      row(koodistokoodiviite, "koodistoUri").text should include("Sallittu arvo: kieli")
    }
  }
}

case class YhdenKoodistonJuuri(
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite
)
