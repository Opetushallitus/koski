package fi.oph.tor.schema.generic

import org.reflections.Reflections

import scala.reflect.runtime.{universe => ru}

class ScalaJsonSchema(val metadatasSupported: MetadataSupport*) {
  case class ScanState(root: Boolean = true, foundTypes: collection.mutable.Set[String] = collection.mutable.Set.empty, createdTypes: collection.mutable.Set[ClassType] = collection.mutable.Set.empty) {
    def childState = copy(root = false)
  }

  def createSchemaType(className: String): SchemaType = {
    createSchemaType(reflect.runtime.currentMirror.classSymbol(Class.forName(className)).toType, ScanState())
  }

  def createSchemaType(obj: AnyRef): SchemaType = createSchemaType(obj.getClass.getName)

  private def createSchemaType(tpe: ru.Type, state: ScanState): SchemaType = {
    val typeName = tpe.typeSymbol.fullName

    if (typeName == "scala.Option") {
      // Option[T] becomes the schema of T with required set to false
      OptionalType(createSchemaType(tpe.asInstanceOf[ru.TypeRefApi].args.head, state))
    } else if (isListType(tpe)) {
      // (Traversable)[T] becomes a schema with items set to the schema of T
      ListType(createSchemaType(tpe.asInstanceOf[ru.TypeRefApi].args.head, state))
    } else {
      schemaTypeForScala.getOrElse(typeName, {
        if (tpe.typeSymbol.isClass) {
          if (tpe.typeSymbol.isAbstract) {
            OneOf(findImplementations(tpe, state))
          } else {
            createClassSchema(tpe, state)
          }
        } else {
          throw new RuntimeException("What is this type: " + tpe)
        }
      })
    }
  }

  private lazy val schemaTypeForScala = Map(
    "org.joda.time.DateTime" -> DateType(),
    "java.util.Date" -> DateType(),
    "java.time.LocalDate" -> DateType(),
    "java.lang.String" -> StringType(),
    "scala.Boolean" -> BooleanType(),
    "scala.Int" -> NumberType(),
    "scala.Long" -> NumberType(),
    "scala.Double" -> NumberType()
  )

  private def createClassSchema(tpe: ru.Type, state: ScanState): SchemaType = {
    val className: String = tpe.typeSymbol.fullName
    val metadata: List[Metadata] = metadataFromAnnotations(tpe.typeSymbol)
    val ref: ClassTypeRef = ClassTypeRef(className, metadata)
    if (!state.foundTypes.contains(className)) {
      state.foundTypes.add(className)

      val params = tpe.typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.head
      val properties: List[Property] = params.map{ paramSymbol =>
        val term = paramSymbol.asTerm
        val termType = createSchemaType(term.typeSignature, state.childState)
        val termName: String = term.name.decoded.trim
        Property(termName, termType, metadataFromAnnotations(term))
      }.toList

      if (state.root) {
        ClassType(className, properties, metadata, state.createdTypes.toList.sortBy(_.simpleName))
      } else {
        state.createdTypes.add(ClassType(className, properties, metadata))
        ref
      }
    } else {
      ref
    }
  }

  private def metadataFromAnnotations(symbol: ru.Symbol): List[Metadata] = {
    symbol.annotations.flatMap { annotation =>
      metadatasSupported.flatMap { metadataSupport =>
        val f: PartialFunction[(String, List[String]), List[Metadata]] = metadataSupport.extractMetadata orElse { case _ => Nil }

        f(annotation.tree.tpe.toString, annotation.tree.children.tail.map(_.toString.replaceAll("\"$|^\"", "").replace("\\\"", "\"").replace("\\'", "'")))
      }
    }
  }

  private def isListType(tpe: ru.Type): Boolean = {
    tpe.baseClasses.exists(s => s.fullName == "scala.collection.Traversable" ||
      s.fullName == "scala.Array" ||
      s.fullName == "scala.Seq" ||
      s.fullName == "scala.List" ||
      s.fullName == "scala.Vector")
  }

  private def findImplementations(tpe: ru.Type, state: ScanState): List[TypeWithClassName] = {
    import collection.JavaConverters._
    import reflect.runtime.currentMirror

    val javaClass: Class[_] = Class.forName(tpe.typeSymbol.asClass.fullName)
    val reflections = new Reflections(javaClass.getPackage.getName)

    val implementationClasses = reflections.getSubTypesOf(javaClass).asScala

    implementationClasses.toList.map { klass =>
      createSchemaType(currentMirror.classSymbol(klass).toType, state).asInstanceOf[TypeWithClassName]
    }
  }
}

