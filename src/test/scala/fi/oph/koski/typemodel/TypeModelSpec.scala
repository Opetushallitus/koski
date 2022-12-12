package fi.oph.koski.typemodel

import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.typemodel.TypescriptTypes.Options
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TypeModelSpec extends AnyFreeSpec with Matchers {

  "TypeModel" - {
    val schemaTypes = SchemaExport.toTypeDef(KoskiSchema.schema)
    val defaultOptions = Options(generics = KoskiSpecificTsGenerics.generics)
    def tsTypesFor(model: TypeModelWithClassName, options: Options = defaultOptions): String = TypescriptTypes.build(Seq(model), options)

    "Should not contain duplicate classes" in {
      val classNames = schemaTypes.map(_.fullClassName)
      classNames should equal (classNames.distinct)
    }

    "Typescript types" - {
      "Schema scaling smoke test" in {
        TypescriptTypes.build(schemaTypes, defaultOptions)
      }

      "Union types" in {
        val localizedString = schemaTypes.find(_.fullClassName == "fi.oph.koski.schema.LocalizedString").get
        tsTypesFor(localizedString) should equal(
          """/*
            | * fi.oph.koski.schema
            | */
            |
            |export type LocalizedString =
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
            |export type Osaamisalajakso =
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
              |export type Koodistokoodiviite<U extends string, A extends string> = {
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
              |export type AikuistenPerusopetuksenAlkuvaihe = {
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
            |export type Backlog = {
            |    id: number,
            |    name: string,
            |    tickets: Array<Item>
            |}
            |
            |export type Item =
            |    | Backlog
            |    | Ticket
            |
            |export type Ticket = {
            |    id: number,
            |    title: string
            |}""".stripMargin)
      }

      "Class name exporting" in {
        val types = TypeExport.toTypeDef(classOf[Backlog])
        val ts = TypescriptTypes.build(types, defaultOptions.copy(exportClassNamesAs = Some("$class")))

        ts should equal(
          """/*
            | * fi.oph.koski.typemodel
            | */
            |
            |export type Backlog = {
            |    $class: "Backlog",
            |    id: number,
            |    name: string,
            |    tickets: Array<Item>
            |}
            |
            |export type Item =
            |    | Backlog
            |    | Ticket
            |
            |export type Ticket = {
            |    $class: "Ticket",
            |    id: number,
            |    title: string
            |}""".stripMargin)
      }

      "Type guards" in {
        val types = TypeExport.toTypeDef(classOf[Backlog])
        val ts = TypescriptTypes.build(types, defaultOptions.copy(
          exportClassNamesAs = Some("$class"),
          exportTypeGuards = true,
        ))

        ts should equal(
          """/*
            | * fi.oph.koski.typemodel
            | */
            |
            |export type Backlog = {
            |    $class: "Backlog",
            |    id: number,
            |    name: string,
            |    tickets: Array<Item>
            |}
            |
            |export type Item =
            |    | Backlog
            |    | Ticket
            |
            |export type Ticket = {
            |    $class: "Ticket",
            |    id: number,
            |    title: string
            |}
            |
            |// Type guards
            |
            |export const isBacklog = (a: any): a is Backlog => a?.$class === "Backlog"
            |
            |export const isItem = (a: any): a is Item => isBacklog(a) || isTicket(a)
            |
            |export const isTicket = (a: any): a is Ticket => a?.$class === "Ticket"""".stripMargin)
      }
    }
  }
}

trait Item {
  def id: Int
}

case class Backlog(
  id: Int,
  name: String,
  tickets: Seq[Item],
) extends Item

case class Ticket(
  id: Int,
  title: String
) extends Item

