package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic._

import scala.xml.Elem

object SchemaToJsonHtml {
  def buildHtml(obj: Any, tyep: SchemaType, schema: ScalaJsonSchema): Elem = (obj, tyep) match {
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

  private def name(f: String) = <span class="name">"{f}"</span>
  private def intersperse[T](l: List[T], spacer: T) = l.zipWithIndex.flatMap {
    case (x, index) => if(index == 0) {List(x)} else {List(spacer, x)}
  }

  private def buildHtmlForObject(obj: AnyRef, tyep: ClassType, schema: ScalaJsonSchema): Elem = {
    <div>
      {" { "}
      {tyep.metadata.flatMap {
        case DescriptionAnnotation(desc) => Some(<span class="description">{desc}</span>)
        case _ => None
      }}
      <ul>
        {
        val propertiesWithValue: List[(Property, AnyRef)] = tyep.properties.flatMap { property: Property =>
          val value = tyep.getPropertyValue(property, obj)
          value match {
            case None => None
            case x => Some((property, x))
          }
        }

        intersperse(propertiesWithValue.map { case (property, value) =>
          <li>
            {name(property.key)} : {buildHtml(value, property.tyep, schema)}
          </li>
        }, <li class="spacer">,</li>)
        }
      </ul>
      {" } "}
    </div>
  }

  private def buildValueHtml(value: AnyRef) = buildValueHtmlString(Json.write(value))

  private def buildValueHtmlString(value: String) = {
    <span class="value">{value}</span>
  }

}
