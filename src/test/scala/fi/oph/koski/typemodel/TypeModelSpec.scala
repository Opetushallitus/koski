package fi.oph.koski.typemodel

import fi.oph.koski.schema.KoskiSchema
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TypeModelSpec extends AnyFreeSpec with Matchers {

  "TypeModel" - {
    val schemaTypes = SchemaExport.toTypeDef(KoskiSchema.schema)
    def tsTypesFor(model: TypeModelWithClassName): String = TypescriptTypes.build(Seq(model), KoskiSpecificTsGenerics.generics)

    "Should not contain duplicate classes" in {
      val classNames = schemaTypes.map(_.fullClassName)
      classNames should equal (classNames.distinct)
    }

    "Typescript types" - {
      "Schema scaling smoke test" in {
        val ts = TypescriptTypes.build(schemaTypes, KoskiSpecificTsGenerics.generics)
        println(ts)
      }

      "Union types" in {
        val localizedString = schemaTypes.find(_.fullClassName == "fi.oph.koski.schema.LocalizedString").get
        tsTypesFor(localizedString) should equal(
          """/*
            | * fi.oph.koski.schema
            | */
            |
            |type LocalizedString =
            |    | English
            |    | Finnish
            |    | Swedish""".stripMargin)
      }

      "Flattened schemas" in {
        val osaamisalajakso = schemaTypes.find(_.fullClassName == "fi.oph.koski.schema.Osaamisalajakso").get
        tsTypesFor(osaamisalajakso) should equal(
          """/*
            | * fi.oph.koski.schema
            | */
            |
            |type Osaamisalajakso =
            |    | {
            |    osaamisala: Koodistokoodiviite<"osaamisala", string>,
            |    alku?: string,
            |    loppu?: string
            |}
            |    | Koodistokoodiviite<"osaamisala", string>""".stripMargin
        )
      }

      "Generics" - {
        "Koodistokoodiviite can be typed with generics" in {
          val koodiviite = schemaTypes.find(_.fullClassName == "fi.oph.koski.schema.Koodistokoodiviite").get
          tsTypesFor(koodiviite) should equal(
            """/*
              | * fi.oph.koski.schema
              | */
              |
              |type Koodistokoodiviite<U extends string, A extends string> = {
              |    koodistoVersio?: number,
              |    koodiarvo: A,
              |    nimi?: LocalizedString,
              |    lyhytNimi?: LocalizedString,
              |    koodistoUri: U
              |}""".stripMargin)
        }

        "Expected koodisto URI and koodistoarvo are filled properly on reference on Koodistokoodiviite" in {
          val alkuvaihe = schemaTypes.find(_.fullClassName == "fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaihe").get
          tsTypesFor(alkuvaihe) should equal(
            """/*
              | * fi.oph.koski.schema
              | */
              |
              |type AikuistenPerusopetuksenAlkuvaihe = {
              |    perusteenDiaarinumero?: string,
              |    tunniste: Koodistokoodiviite<"suorituksentyyppi", "aikuistenperusopetuksenoppimaaranalkuvaihe">
              |}""".stripMargin)
        }
      }

      "Types from any Scala class" in {
        val types = TypeExport.toTypeDef(classOf[Backlog])
        val ts = TypescriptTypes.build(types)

        ts should equal(
          """/*
            | * fi.oph.koski.typemodel
            | */
            |
            |type Backlog = {
            |    name: string,
            |    tickets: Array<Ticket>
            |}
            |
            |type Ticket = {
            |    id: number,
            |    title: string
            |}""".stripMargin)
      }
    }
  }
}

case class Backlog(
  name: String,
  tickets: Seq[Ticket],
)

case class Ticket(
  id: Int,
  title: String
)


