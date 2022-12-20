package fi.oph.koski.typemodel

import fi.oph.koski.schema.{AikuistenPerusopetuksenAlkuvaihe, Koodistokoodiviite, KoskiSchema, LocalizedString, Osaamisalajakso}
import fi.oph.koski.typemodel.TypescriptTypes.Options
import fi.oph.scalaschema.annotation.DefaultValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TypeModelSpec extends AnyFreeSpec with Matchers {
  val defaultOptions: Options = Options(generics = KoskiSpecificTsGenerics.generics)

  "TypeModel" - {
    "Should not contain duplicate classes in Koski schema" in {
      val schemaTypes = SchemaExport.toTypeDef(KoskiSchema.schema)
      val classNames = schemaTypes.map(_.fullClassName)
      classNames should equal (classNames.distinct)
    }

    "Typescript types" - {
      "Schema scaling smoke test" in {
        val schemaTypes = SchemaExport.toTypeDef(KoskiSchema.schema)
        TypescriptTypes.build(schemaTypes, defaultOptions)
      }

      "Union types" in {
        expectTsCodeFromClassToEqual(classOf[LocalizedString])(
          "LocalizedString.ts" ->
            """
              |import { English } from "./English"
              |import { Finnish } from "./Finnish"
              |import { Swedish } from "./Swedish"
              |
              |export type LocalizedString =
              |    | English
              |    | Finnish
              |    | Swedish
              |""".stripMargin
        )
      }

      "Flattened schemas" in {
        expectTsCodeFromClassToEqual(classOf[Osaamisalajakso])(
          "Osaamisalajakso.ts" ->
            """
              |import { Koodistokoodiviite } from "./Koodistokoodiviite"
              |import { LocalizedString } from "./LocalizedString"
              |
              |export type Osaamisalajakso =
              |    | {
              |        osaamisala: Koodistokoodiviite<"osaamisala", string>,
              |        alku?: string,
              |        loppu?: string
              |      }
              |    | Koodistokoodiviite<"osaamisala", string>
              |""".stripMargin
        )
      }

      "Generics" - {
        "Koodistokoodiviite can be typed with generics" in {
          expectTsCodeFromClassToEqual(classOf[Koodistokoodiviite])(
            "Koodistokoodiviite.ts" ->
              """
                |import { LocalizedString } from "./LocalizedString"
                |
                |export type Koodistokoodiviite<U extends string = string, A extends string = string> = {
                |    koodistoVersio?: number,
                |    koodiarvo: A,
                |    nimi?: LocalizedString,
                |    lyhytNimi?: LocalizedString,
                |    koodistoUri: U
                |}
                |""".stripMargin)
        }

        "Expected koodisto URI and koodistoarvo are filled properly on reference on Koodistokoodiviite" in {
          expectTsCodeFromClassToEqual(classOf[AikuistenPerusopetuksenAlkuvaihe])(
            "AikuistenPerusopetuksenAlkuvaihe.ts" ->
              """
                |import { Koodistokoodiviite } from "./Koodistokoodiviite"
                |import { LocalizedString } from "./LocalizedString"
                |
                |export type AikuistenPerusopetuksenAlkuvaihe = {
                |    perusteenDiaarinumero?: string,
                |    tunniste: Koodistokoodiviite<"suorituksentyyppi", "aikuistenperusopetuksenoppimaaranalkuvaihe">
                |}
                |""".stripMargin)
        }
      }

      "Class name exporting" in {
        expectTsCodeFromClassToEqual(classOf[Backlog], defaultOptions.copy(exportClassNamesAs = Some("$class")))(
          "Backlog.ts" ->
            """
              |import { Item } from "./Item"
              |
              |export type Backlog = {
              |    $class: "fi.oph.koski.typemodel.Backlog",
              |    id: number,
              |    name: string,
              |    tickets: Array<Item>
              |}
              |""".stripMargin,

          "Item.ts" ->
            """
              |import { Backlog } from "./Backlog"
              |import { Ticket } from "./Ticket"
              |
              |export type Item =
              |    | Backlog
              |    | Ticket
              |""".stripMargin,

          "Ticket.ts" ->
            """
              |export type Ticket = {
              |    $class: "fi.oph.koski.typemodel.Ticket",
              |    id: number,
              |    title: string
              |}""".stripMargin,
        )
      }

      "Type guards" in {
        expectTsCodeFromClassToEqual(classOf[Backlog], defaultOptions.copy(
          exportClassNamesAs = Some("$class"),
          exportTypeGuards = true,
        ))(
          "Backlog.ts" ->
            """
              |import { Item } from "./Item"
              |
              |export type Backlog = {
              |    $class: "fi.oph.koski.typemodel.Backlog",
              |    id: number,
              |    name: string,
              |    tickets: Array<Item>
              |}
              |
              |export const isBacklog = (a: any): a is Backlog => a?.$class === "fi.oph.koski.typemodel.Backlog"
              |""".stripMargin,

          "Item.ts" ->
            """
              |import { Backlog, isBacklog } from "./Backlog"
              |import { Ticket, isTicket } from "./Ticket"
              |
              |export type Item =
              |    | Backlog
              |    | Ticket
              |
              |export const isItem = (a: any): a is Item => isBacklog(a) || isTicket(a)
              |""".stripMargin,

          "Ticket.ts" ->
            """
              |export type Ticket = {
              |    $class: "fi.oph.koski.typemodel.Ticket",
              |    id: number,
              |    title: string
              |}
              |
              |export const isTicket = (a: any): a is Ticket => a?.$class === "fi.oph.koski.typemodel.Ticket"
              |""".stripMargin,
        )
      }

      "Object constructors" in {
        expectTsCodeFromClassToEqual(classOf[User], defaultOptions.copy(
          exportClassNamesAs = Some("$class"),
          exportConstructors = true,
        ))(
          "AccessRight.ts" ->
            """
              |export type AccessRight = {
              |    $class: "fi.oph.koski.typemodel.AccessRight",
              |    notes?: string,
              |    group: string
              |}
              |
              |export const AccessRight = (o: {
              |    notes?: string,
              |    group?: string
              |} = {}): AccessRight => ({$class: "fi.oph.koski.typemodel.AccessRight",group: "viewer", ...o})
              |
              |""".stripMargin,

          "User.ts" ->
            """
              |import { AccessRight } from "./AccessRight"
              |
              |export type User = {
              |    $class: "fi.oph.koski.typemodel.User",
              |    accountLocked: boolean,
              |    hobbies: Array<string>,
              |    username: string,
              |    accessRight: AccessRight,
              |    rank: string,
              |    age: number,
              |    ratings: Record<string, number>,
              |    nickname?: string
              |}
              |
              |export const User = (o: {
              |    accountLocked?: boolean,
              |    hobbies?: Array<string>,
              |    username: string,
              |    accessRight?: AccessRight,
              |    rank?: string,
              |    age?: number,
              |    ratings?: Record<string, number>,
              |    nickname?: string
              |}): User => ({accountLocked: false,hobbies: [],accessRight: AccessRight({group: "viewer"}),rank: "newbie",age: 18,ratings: {},$class: "fi.oph.koski.typemodel.User", ...o})
              |
              |""".stripMargin
        )
      }
    }
  }

  def expectTsCodeFromClassToEqual(clss: Class[_], options: Options = defaultOptions)(expected: (String, String)*): Unit =
    expectTsCodeToMatch(TypeExport.toTypeDef(clss), options)(expected: _*)

  def expectTsCodeToMatch(types: Seq[TypeModelWithClassName], options: Options = defaultOptions)(expected: (String, String)*): Unit = {
    val tsFiles = TypescriptTypes.build(types, options)
    expected.foreach { case (fileName, expectedContent) =>
      val tsFile = tsFiles.find(_.fileName == fileName)
      tsFile should not be empty
      expectLaxEqual(tsFile.get.content, expectedContent)
    }
  }

  def expectLaxEqual(actual: String, expected: String): Unit =
    trimLines(actual) should equal(trimLines(expected))

  def trimLines(source: String): String =
    source
      .split("\n")
      .filter(_.nonEmpty)
      .map(_.trim)
      .mkString("\n")
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

case class User(
  username: String,
  nickname: Option[String],
  hobbies: List[String],
  @DefaultValue(18)
  age: Int,
  @DefaultValue("newbie")
  rank: String,
  @DefaultValue(false)
  accountLocked: Boolean,
  ratings: Map[String, Int],
  accessRight: AccessRight,
)

case class AccessRight(
  notes: Option[String],
  @DefaultValue("viewer")
  group: String
)
