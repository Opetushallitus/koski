package fi.oph.koski.documentation

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Description

import scala.xml.{Node, Elem}

case class SchemaToNiceHtml(implicit val user: KoskiSession) extends LocalizedHtml {
  def buildHtml(schema: ClassSchema, exampleData: AnyRef): List[Elem] = {
    buildHtml(Property("", schema, Nil), exampleData, schema, NodeContext("0", None))
  }

  private def buildHtml(property: Property, obj: Any, schema: ClassSchema, context: NodeContext): List[Elem] = (obj, property.schema) match {
    case (o: Suoritus, t:ClassSchema) => suoritusHtml(property, schema, o, context)
    case (o: Läsnäolojakso, t:ClassSchema) => simplePropertyHtml(dateFormatter.format(o.alku), i(o.tila)  )
    case (o: Opiskeluoikeusjakso, t:ClassSchema) => simplePropertyHtml(dateFormatter.format(o.alku), i(o.tila)  )
    case (o: Localizable, t:ClassSchema)  => buildValueHtml(property, i(o), context)
    case (o: Vahvistus, t:ClassSchema) => vahvistusHTML(property, o, context)
    case (o: AnyRef, t:ClassSchema) => buildHtmlForObject(property, o, schema, context)
    case (xs: Iterable[_], t:ListSchema) => buildHtmlForArray(property, xs, schema, context)
    case (x: Number, t:NumberSchema) => buildValueHtml(property, x, context)
    case (x: Boolean, t:BooleanSchema) => buildValueHtmlString(property, x.toString, context)
    case (x: AnyRef, t:DateSchema) => buildValueHtml(property, x, context)
    case (x: String, t:StringSchema) => buildValueHtml(property, x, context)
    case (x: Option[_], t:OptionalSchema) => buildHtml(property.copy(schema = t.itemSchema), x.get, schema, context)
    case (x: AnyRef, t:OptionalSchema) => buildHtml(property.copy(schema = t.itemSchema), x, schema, context)
    case (x: AnyRef, t:AnyOfSchema) => buildHtml(property.copy(schema = findOneOfSchema(t, x)), x, schema, context)
    case (x: AnyRef, t:ClassRefSchema) => buildHtml(property.copy(schema = schema.getSchema(t.fullClassName).get), x , schema, context)
    case _ =>
      throw new RuntimeException("Unexpected input: " + obj + ", " + property.schema)
  }

  def suoritusHtml(property: Property, schema: ClassSchema, suoritus: Suoritus, context: NodeContext) = {
    def simpleHtml = simplePropertyHtml(i(suoritus.koulutusmoduuli.description), i(suoritus.arvosanaNumeroin.getOrElse(suoritus.arvosanaKirjaimin)))
    suoritus match {
      case s: PerusopetuksenToiminta_AlueenSuoritus =>
        simpleHtml
      case s: OppiaineenSuoritus =>
        simpleHtml
      case _ =>
        buildHtmlForObject(property, suoritus, schema, context)
    }
  }

  def vahvistusHTML(property: Property, vahvistus: Vahvistus, context: NodeContext) = {
    val myöntäjät = vahvistus.myöntäjäHenkilöt.map { h => h.nimi + ", " + i(h.titteli)}.mkString(", ")
    val value = i(vahvistus.paikkakunta.nimi) + " " + dateFormatter.format(vahvistus.päivä) + " " + myöntäjät
    buildValueHtml(property, value, context)
  }

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }


  private def keyHtml(property: Property) = {
    property.key match {
      case ""          => ""
      case _           => property.key
    }
  }

  private def buildHtmlForObject(property: Property, obj: AnyRef, schema: ClassSchema, context: NodeContext): List[Elem] = {
    val propertyElems = {
      val classSchema: ClassSchema = property.schema.asInstanceOf[ClassSchema]
      val propertiesWithValue: List[(Int, Property, AnyRef)] = classSchema.properties.zipWithIndex.flatMap { case (property: Property, index: Int) =>
        val value = classSchema.getPropertyValue(property, obj)
        value match {
          case None => None
          case _ => Some((index, property, value))
        }
      }

      propertiesWithValue.flatMap { case (i, property, value) =>
        buildHtml(property, value, schema, context.child)
      }
    }
    val allMetadata: List[Metadata] = property.metadata ++ property.schema.metadata
    List(<div class="property object">
      { propertyHeaderHtml(property) }
      {propertyElems}
    </div>)
  }

  private def propertyHeaderHtml(property: Property) = <div class="property-header">
    <h3>{keyHtml(property)}</h3>
    {metadataHtml(property.metadata ++ property.schema.metadata)}
  </div>

  private def buildHtmlForArray(property: Property, xs: Iterable[_], schema: ClassSchema, context: NodeContext) = {
    val propertyElems = xs.flatMap { item =>
      <li>{buildHtml(Property("", property.schema.asInstanceOf[ListSchema].itemSchema, Nil), item, schema, context.child)}</li>
    }.toList

    List(
      <div class="property array">
        { propertyHeaderHtml(property) }
        <ul>
          { propertyElems }
        </ul>
      </div>
    )
  }

  private def buildValueHtml(property: Property, value: AnyRef, context: NodeContext) = {
    buildValueHtmlString(property, value.toString, context)
  }

  private def buildValueHtmlString(property: Property, value: String, context: NodeContext) = {
    simplePropertyHtml(keyHtml(property), value)
  }

  def simplePropertyHtml(keyHtml: AnyRef, value: String): List[Elem] = {
    List(<div class="property simple">
      <span class="key">{keyHtml} </span>
      <span class="value"> {value} </span>
    </div>)
  }

  private def metadataHtml(metadatas: List[Metadata]) = {
    {
      metadatas.flatMap {
        case Description(desc) => Some(<span class="description">{desc}</span>)
        case ReadOnly(desc) => Some(<span class="readonly">{desc}</span>)
        case k: KoodistoUri =>Some(<span class="koodisto">Koodisto: {k.asLink}</span>)
        case k: KoodistoKoodiarvo =>Some(<span class="koodiarvo">Hyväksytty koodiarvo: {k.arvo}</span>)
        case o: OksaUri => Some(<span class="oksa">Oksa: {o.asLink}</span>)
        case _ => None
      }
    }
  }

  case class NodeContext(id: String, parent: Option[NodeContext]) {
    var idCounter = 0

    def depth: Int = parent match {
      case Some(p) => p.depth + 1
      case None => 0
    }

    def child = {
      NodeContext(id + "-" + generateId(), Some(this))
    }

    def path:String = parent match {
      case Some(p) => p.path + " node-" + id
      case None => "node-" + id
    }

    private def generateId() = {
      idCounter = idCounter + 1
      idCounter
    }
  }
}
