package fi.oph.koski.documentation

import com.tristanhunt.knockoff.DefaultDiscounter._
import fi.oph.koski.http.ErrorCategory
import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema.ClassSchema
import fi.oph.koski.json.Json

import scala.xml.Elem

object KoskiTiedonSiirtoHtml {
  private val schemaViewerUrl = "/koski/json-schema-viewer#koski-oppija-schema.json"
  private val schemaDocumentUrl = "/koski/documentation/koski-oppija-schema.html"
  private val schemaFileUrl = "/koski/api/documentation/koski-oppija-schema.json"
  private def general =s"""

# Koski-tiedonsiirtoprotokolla

Tässä dokumentissa kuvataan Koski-järjestelmän tiedonsiirrossa käytettävä protokolla. Lisätietoja Koski-järjestelmästä löydät [Opetushallituksen wiki-sivustolta](https://confluence.csc.fi/display/OPHPALV/Koski). Järjestelmän lähdekoodit ja kehitysdokumentaatio [Githubissa](https://github.com/Opetushallitus/koski).

Protokolla, kuten Koski-järjestelmäkin, on työn alla, joten kaikki voi vielä muuttua.

Muutama perusasia tullee kuitenkin säilymään:

- Rajapinnan avulla järjestelmään voi tallentaa tietoja oppijoiden opinto-oikeuksista, opintosuorituksista ja läsnäolosta oppilaitoksissa
- Rajapinnan avulla tietoja voi myös hakea ja muokata
- Rajapinnan käyttö vaatii autentikoinnin ja pääsy tietoihin rajataan käyttöoikeusryhmillä.
  Näin ollen esimerkiksi oikeus oppilaan tietyssä oppilaitoksessa suorittamien opintojen päivittämiseen voidaan antaa kyseisen oppilaitoksen henkilöstölle
- Rajapinta mahdollistaa myös automaattiset tiedonsiirrot tietojärjstelmien välillä. Näin esimerkiksi tietyt viranomaiset voivat saada tietoja Koskesta.
  Samoin oppilaitoksen tietojärjestelmät voivat päivittää tietoja Koskeen.
- Järjestelmä tarjoaa REST-tyyppisen tiedonsiirtorajapinnan, jossa dataformaattina on JSON
- Samaa tiedonsiirtoprotokollaa ja dataformaattia pyritään soveltuvilta osin käyttämään sekä käyttöliittymille,
  jotka näyttävät tietoa loppukäyttäjille, että järjestelmien väliseen kommunikaatioon

## JSON-dataformaatti

Käytettävästä JSON-formaatista on laadittu työversio, jonka toivotaan vastaavan ammatillisen koulutuksen tarpeisiin.
Tällä formaatilla siis tulisi voida siirtää tietoja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä Koskeen ja eteenpäin
tietoja tarvitsevien viramomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin.
Formaattia on tarkoitus laajentaa soveltumaan myös muiden koulutustyyppien tarpeisiin, mutta näitä tarpeita ei ole vielä riittävällä tasolla kartoitettu,
jotta konkreettista dataformaattia voitaisiin suunnitella. Yksi formaatin suunnittelukriteereistä on toki ollut sovellettavuus muihinkin koulutustyyppeihin.

### JSON Schema

Käytettävä JSON-dataformaatti on kuvattu [JSON-schemalla](http://json-schema.org/), jota vasten siirretyt tiedot voidaan myös automaattisesti validoida.

<div class="preview-image-links">
  <a href="${schemaViewerUrl}">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-preview.png">
    </div>
    <div class="caption">Visualisoitu JSON-schema</div>
    <p>Voi tarkastella schemaa visualisointityökalun avulla. Tällä työkalulla voi myös validoida JSON-viestejä schemaa vasten. Klikkaamalla kenttiä saat näkyviin niiden tarkemmat kuvaukset.</p>
  </a>
  <a href="${schemaDocumentUrl}">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-html-preview.png">
    </div>
    <div class="caption">Printattava dokumentti</div>
    <p>Printattava versio schemasta</p>
  </a>
  <a href="${schemaFileUrl}">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-json-preview.png">
    </div>
    <div class="caption">Lataa JSON-tiedostona</div>
    <p>Voit myös ladata scheman tiedostona</p>
  </a>
</div>

Tietokentät, joissa validit arvot on lueteltavissa, on kooditettu käyttäen hyväksi Opintopolku-järjestelmään kuuluvaa [Koodistopalvelua](https://github.com/Opetushallitus/koodisto).
Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen [koulutuskoodi](/koski/documentation/koodisto/koulutus/latest).

Scalaa osaaville ehkä nopein tapa tutkia tietomallia on kuitenkin sen lähdekoodi. Githubista löytyy sekä [scheman](https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/schema/Oppija.scala),
että [esimerkkien](https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/documentation/Examples.scala) lähdekoodit.

"""

  def rest_apis ="""

## REST-rajapinnat

Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.

Rajapinnat on lueteltu ja kuvattu alla. Voit myös testata rajapintojen toimintaa tällä sivulla, kunhan käyt ensin [kirjautumassa sisään](/koski) järjestelmään.
Saat tarvittavat tunnukset Koski-kehitystiimiltä pyydettäessä.

Rajapintojen käyttämät virhekoodit on myös kuvattu alla. Virhetapauksissa rajapinnat käyttävät alla kuvattuja HTTP-statuskoodeja ja sisällyttävät tarkemmat virhekoodit ja selitteineen JSON-tyyppiseen paluuviestiin.
Samaan virhevastaukseen voi liittyä useampi virhekoodi/selite.

  """

  def annotated_data="""
## Esimerkkidata annotoituna

Toinen hyvä tapa tutustua tiedonsiirtoprotokollaan on tutkia esimerkkiviestejä.
Alla joukko viestejä, joissa oppijan opinnot ovat eri vaiheissa. Kussakin esimerkissa on varsinaisen JSON-sisällön lisäksi schemaan pohjautuva annotointi ja linkitykset koodistoon ja OKSA-sanastoon.
    """

  def htmlExamples = {
    <div>
      { examplesHtml(ExamplesEsiopetus.examples, "Esiopetus") }
      { examplesHtml(ExamplesPerusopetukseenValmistavaOpetus.examples, "Perusopetukseen valmistava opetus") }
      { examplesHtml(ExamplesPerusopetus.examples, "Perusopetus") }
      { examplesHtml(ExamplesPerusopetuksenLisaopetus.examples, "Perusopetuksen lisäopetus") }
      { examplesHtml(ExamplesLukio.examples ++ ExamplesLukioonValmistavaKoulutus.examples, "Lukiokoulutus") }
      { examplesHtml(ExamplesIB.examples, "IB-koulutus") }
      { examplesHtml(ExamplesAmmatillinen.examples, "Ammatillinen koulutus") }
      { examplesHtml(ExamplesValma.examples ++ ExamplesTelma.examples, "Valmentava koulutus") }
      { examplesHtml(ExamplesKorkeakoulu.examples, "Korkeakoulu (Virrasta)") }
      { examplesHtml(ExamplesYlioppilastutkinto.examples, "Ylioppilastutkinto (Ylioppilastutkintorekisteristä)") }
    </div>
  }

  val categoryNames: Seq[String] = Seq(
    "Esiopetus",
    "Perusopetukseen valmistava opetus",
    "Perusopetus",
    "Perusopetuksen lisäopetus",
    "Lukiokoulutus",
    "IB-koulutus",
    "Ammatillinen koulutus",
    "Valmentava koulutus",
    "Korkeakoulu (Virrasta)",
    "Ylioppilastutkinto (Ylioppilastutkintorekisteristä)"
  )

  val categoryExamples: Map[String, Seq[Example]] = Map(
    "Esiopetus" -> ExamplesEsiopetus.examples,
    "Perusopetukseen valmistava opetus" -> ExamplesPerusopetukseenValmistavaOpetus.examples,
    "Perusopetus" -> ExamplesPerusopetus.examples,
    "Perusopetuksen lisäopetus" -> ExamplesPerusopetuksenLisaopetus.examples,
    "Lukiokoulutus" -> (ExamplesLukio.examples ++ ExamplesLukioonValmistavaKoulutus.examples),
    "IB-koulutus" -> ExamplesIB.examples,
    "Ammatillinen koulutus" -> ExamplesAmmatillinen.examples,
    "Valmentava koulutus" -> (ExamplesValma.examples ++ ExamplesTelma.examples),
    "Korkeakoulu (Virrasta)" -> ExamplesKorkeakoulu.examples,
    "Ylioppilastutkinto (Ylioppilastutkintorekisteristä)" -> ExamplesYlioppilastutkinto.examples
  )

  val categoryExampleMetadata: Map[String, Seq[_]] = {
    categoryExamples.mapValues(_ map {e: Example =>
      Map(
        "name" -> e.name,
        "link" -> s"/koski/api/documentation/examples/${e.name}.json",
        "description" -> e.description
      )
    })
  }

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

  val apiOperations = {
    KoskiApiOperations.operations.map {operation =>
      Map(
        "method" -> operation.method,
        "path" -> operation.path,
        "operation" -> operation.path,
        "summary" -> operation.summary,
        "doc" -> operation.doc.toString(),
        "errorCategories" -> operation.statusCodes.flatMap(_.flatten),
        "examples" -> operation.examples,
        "parameters" -> operation.parameters.map {parameter => Map(
          "name" -> parameter.name,
          "description" -> parameter.description,
          "examples" -> parameter.examples,
          "type" -> {
            val par = parameter // XXX: Fixes a weird type error
            par match {
              case p: QueryParameter => "query"
              case p: PathParameter => "path"
            }
          }
        )}
      )
    }
  }

  val htmlTextSections = Vector(general, rest_apis, annotated_data).map(s => toXHTML(knockoff(s)).toString())


  def examplesJson(examples: List[Example], title: String) = {
    Map(
      "title" -> title,
      "examples" -> examples.map { e: Example =>
        Map (
          "link" -> s"/koski/documentation/examples/${e.name}.json",
          "description" -> e.description,
          "jsontable" -> <table class="json">{SchemaToJsonHtml.buildHtml(KoskiSchema.schema.asInstanceOf[ClassSchema], e.data).map(_.toString()).toVector}</table>
        )
      }.toVector
    )
  }

  def examplesHtml(examples: List[Example], title: String) = {
    <h3>{title}</h3>
    <ul class="example-list">
      {
        examples.map { example: Example =>
          <li class="example-item">
            <a class="example-link">
              {example.description}
            </a>
            <a class="example-as-json" href={"/koski/documentation/examples/" + example.name + ".json"} target="_blank">lataa JSON</a>
            <table class="json">
              {SchemaToJsonHtml.buildHtml(KoskiSchema.schema.asInstanceOf[ClassSchema], example.data)}
            </table>
          </li>
        }
      }
    </ul>
  }
}