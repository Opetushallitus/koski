package fi.oph.koski.typemodel

import fi.oph.koski.typemodel.TypescriptTypes.{GenericsObject, GenericsProperty}

object KoskiSpecificTsGenerics {
  def generics: Seq[GenericsObject] = Seq(
    GenericsObject(
      className = "fi.oph.koski.schema.Koodistokoodiviite",
      mapping = Map(
        "koodistoUri" -> GenericsProperty(
          name = "U",
          extend = "string",
          getType = fromPropEnums,
        ),
        "koodiarvo" -> GenericsProperty(
          name = "A",
          extend = "string",
          getType = fromPropEnums,
        ),
      ),
    ),
  )

  def fromPropEnums(model: TypeModel): Option[TypeModel] = model match {
    case t: EnumType[_] => Some(t)
    case _ => None
  }
}
