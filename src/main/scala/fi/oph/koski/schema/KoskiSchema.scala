package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.EiTallennetaOpiskeluoikeudenDataan
import fi.oph.scalaschema._
import org.json4s.{JArray, JObject, JValue}
import org.json4s.jackson.JsonMethods

object KoskiSchema {
  lazy val schemaFactory: SchemaFactory = SchemaFactory()
  lazy val schema = createSchema(classOf[Oppija]).asInstanceOf[ClassSchema]
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema)
  lazy val schemaJsonString = JsonMethods.compact(schemaJson)
  lazy val strictDeserialization = ExtractionContext(schemaFactory, allowEmptyStrings = false)
  lazy val lenientDeserialization = ExtractionContext(schemaFactory, ignoreUnexpectedProperties = true)
  lazy val lenientDeserializationWithIgnoringNonValidatingListItems =
    ExtractionContext(schemaFactory, ignoreUnexpectedProperties = true, ignoreNonValidatingListItems = true)
  lazy val lenientDeserializationWithIgnoringNonValidatingListItemsWithoutValidation =
    ExtractionContext(schemaFactory, ignoreUnexpectedProperties = true, ignoreNonValidatingListItems = true, validate = false)
  lazy val lenientDeserializationWithoutValidation = ExtractionContext(schemaFactory, ignoreUnexpectedProperties = true, validate = false)

  def createSchema(clazz: Class[_]) = schemaFactory.createSchema(clazz) match {
    case s: AnyOfSchema => s
    case s: ClassSchema => s.moveDefinitionsToTopLevel
    case _ => ???
  }

  def skipSyntheticProperties(s: ClassSchema, p: Property): List[Property] = if (p.synthetic) Nil else List(p)

  // Suodattaa pois kentät, joita ei tallenneta opiskeluoikeuden dataan (esim. lukuhetkellä täydennetty oppilaitostyyppi).
  def skipEiTallennettavatKentät(s: ClassSchema, p: Property): List[Property] =
    if (eiTallennetaOpiskeluoikeudenDataan(p)) Nil else List(p)

  private def eiTallennetaOpiskeluoikeudenDataan(p: Property): Boolean =
    p.metadata.exists(_.isInstanceOf[EiTallennetaOpiskeluoikeudenDataan])

  // @EiTallennetaOpiskeluoikeudenDataan-merkittyjen kenttien nimet skeemasta. Näitä kenttiä ei tallenneta dataan eikä
  // oteta vastaan syötteessä (ks. poistaEiTallennettavatKentät), vaan ne täydennetään lukuhetkellä.
  //
  // Poisto perustuu kentän nimeen, joten varmistetaan että merkityn kentän nimi on skeemassa yksikäsitteinen: samaa
  // nimeä ei saa esiintyä myös ei-merkittynä kenttänä. Muuten nimeen perustuva poisto osuisi väärään kenttään. Ristiriita
  // kaatuu selkeään virheeseen heti kun skeema rakennetaan.
  lazy val eiTallennettavatKenttänimet: Set[String] = {
    val käsitellyt = scala.collection.mutable.Set.empty[String]
    val merkityt = scala.collection.mutable.Set.empty[String]
    val muut = scala.collection.mutable.Set.empty[String]
    def kerää(s: Schema): Unit = s match {
      case s: ClassRefSchema => kerää(s.resolve(schemaFactory))
      case s: SchemaWithClassName if käsitellyt.contains(s.fullClassName) => ()
      case s: ClassSchema =>
        käsitellyt.add(s.fullClassName)
        s.properties.foreach { p =>
          (if (eiTallennetaOpiskeluoikeudenDataan(p)) merkityt else muut) += p.key
          kerää(p.schema)
        }
      case s: AnyOfSchema =>
        käsitellyt.add(s.fullClassName)
        s.alternatives.foreach(kerää)
      case s: OptionalSchema => kerää(s.itemSchema)
      case s: ListSchema => kerää(s.itemSchema)
      case s: MapSchema => kerää(s.itemSchema)
      case _ => ()
    }
    kerää(schema)
    val ristiriidat = merkityt.intersect(muut)
    require(
      ristiriidat.isEmpty,
      s"@EiTallennetaOpiskeluoikeudenDataan-kentän nimi (${ristiriidat.mkString(", ")}) esiintyy skeemassa myös " +
        "ei-merkittynä kenttänä. Nimeen perustuva poisto osuisi tällöin väärään kenttään."
    )
    merkityt.toSet
  }

  // Poistaa JValuesta kaikki @EiTallennetaOpiskeluoikeudenDataan-merkityt kentät (lukuhetkellä täydennettävät).
  def poistaEiTallennettavatKentät(json: JValue): JValue = json match {
    case JObject(fields) =>
      JObject(fields.collect { case (key, value) if !eiTallennettavatKenttänimet.contains(key) => (key, poistaEiTallennettavatKentät(value)) })
    case JArray(items) => JArray(items.map(poistaEiTallennettavatKentät))
    case muu => muu
  }
}
