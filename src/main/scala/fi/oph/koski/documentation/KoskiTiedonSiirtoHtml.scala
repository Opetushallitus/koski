package fi.oph.koski.documentation

import com.tristanhunt.knockoff.DefaultDiscounter._
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.util.Markdown
import fi.oph.scalaschema.ClassSchema

object KoskiTiedonSiirtoHtml {
  private val schemaViewerUrl = "/koski/json-schema-viewer#koski-oppija-schema.json"
  private val schemaDocumentUrl = "/koski/dokumentaatio/koski-oppija-schema.html"
  private val schemaFileUrl = "/koski/api/documentation/koski-oppija-schema.json"
  private def general =s"""

# Koski-tiedonsiirtoprotokolla

Tässä dokumentissa kuvataan Koski-järjestelmän tiedonsiirrossa käytettävä protokolla. Lisätietoja Koski-järjestelmästä löydät [Opetushallituksen wiki-sivustolta](https://confluence.csc.fi/display/OPHPALV/Koski). Järjestelmän lähdekoodit ja kehitysdokumentaatio [Githubissa](https://github.com/Opetushallitus/koski).

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

Käytettävällä JSON-formaatilla voidaan siirtää tietoja perusopetusta, lukiokoulutusta ja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä Koskeen ja eteenpäin tietoja tarvitsevien viranomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin.

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
Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen [koulutuskoodi](/koski/dokumentaatio/koodisto/koulutus/latest).

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

  val categoryExamples: Map[String, List[Example]] = Map(
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

  val apiOperations: List[ApiOperation] = KoskiApiOperations.operations

  val htmlTextSections = List(general, rest_apis, annotated_data).map(Markdown.markdownToXhtmlString)
}