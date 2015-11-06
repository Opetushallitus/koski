package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic._

import scala.xml.Elem

object SchemaToJsonHtml {
  def buildHtml(obj: Any, tyep: SchemaType, schema: ScalaJsonSchema): AnyRef = (obj, tyep) match {
    case (o: AnyRef, t:ClassType) => buildHtmlForObject(o, t, schema)
    case (xs: Iterable[_], t:ListType) => <span>[{intersperse(xs.map{ value => buildHtml(value, t.itemType, schema) }.toList, ",")}]</span>
    case (x: Number, t:NumberType) => buildValueHtml(x)
    case (x: Boolean, t:BooleanType) => buildValueHtmlString(x.toString)
    case (x: AnyRef, t:DateType) => buildValueHtml(x)
    case (x: String, t:StringType) => buildValueHtml(x)
    case (x: Option[_], t:OptionalType) => buildHtml(x.get, t.itemType, schema)
    case (x: AnyRef, t:OneOf) => buildHtml(x, t.matchType(x), schema)
    case (x: AnyRef, t:ClassTypeRef) => buildHtml(x, schema.createSchema(t.fullClassName), schema)
  }

  private def intersperse[T](l: List[T], spacer: T) = l.zipWithIndex.flatMap {
    case (x, index) => if(index == 0) {List(x)} else {List(spacer, x)}
  }

  private def buildHtmlForObject(obj: AnyRef, tyep: ClassType, schema: ScalaJsonSchema): Elem = {
    <div class="object">
      {" { "}
      {metadataHtml(tyep.metadata)}
      <ul>
        {
          val propertiesWithValue: List[(Int, Property, AnyRef)] = tyep.properties.zipWithIndex.flatMap { case (property: Property, index: Int) =>
            val value = tyep.getPropertyValue(property, obj)
            value match {
              case None => None
              case x => Some((index, property, x))
            }
          }
          propertiesWithValue.map { case (i, property, value) =>
            <li class="property">
              <span class="key">{property.key}</span>:
              <span class="value">{buildHtml(value, property.tyep, schema)}</span>
              {metadataHtml(property.metadata)}
              {if (i < propertiesWithValue.length - 1) { "," } else { "" } }
            </li>
          }
        }
      </ul>
      {" } "}
    </div>
  }

  private def metadataHtml(metadatas: List[Metadata]) = {
    metadatas.flatMap {
      case DescriptionAnnotation(desc) => Some(<span class="description">{desc}</span>)
      case KoodistoAnnotation(koodistoNimi) =>Some(<a href={"https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/"+koodistoNimi+"/1"} class="koodisto">Koodisto: {koodistoNimi}</a>)
      case _ => None
    }
  }

  private def buildValueHtml(value: AnyRef) = buildValueHtmlString(Json.write(value))

  private def buildValueHtmlString(value: String) = {
    {value}
  }

}
