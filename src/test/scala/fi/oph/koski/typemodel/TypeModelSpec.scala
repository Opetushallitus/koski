package fi.oph.koski.typemodel

import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.typemodel.TypescriptTypes.Options
import fi.oph.scalaschema.annotation.DefaultValue
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
              |export type Koodistokoodiviite<U extends string = string, A extends string = string> = {
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

      "Object constructors" in {
        val types = TypeExport.toTypeDef(classOf[User])
        val ts = TypescriptTypes.build(types, defaultOptions.copy(
          exportClassNamesAs = Some("$class"),
          exportConstructors = true,
        ))

        ts should equal(
          """/*
            | * fi.oph.koski.typemodel
            | */
            |
            |export type User = {
            |    $class: "User",
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
            |export type AccessRight = {
            |    $class: "AccessRight",
            |    notes?: string,
            |    group: string
            |}
            |
            |// Object constructors
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
            |}): User => ({accountLocked: false,hobbies: [],accessRight: AccessRight({group: "viewer"}),rank: "newbie",age: 18,ratings: {},$class: "User", ...o})
            |
            |export const AccessRight = (o: {
            |    notes?: string,
            |    group?: string
            |} = {}): AccessRight => ({$class: "AccessRight",group: "viewer", ...o})""".stripMargin)
      }

      "Object constructors with deep defaults" in {
        val types = List(
          "fi.oph.koski.schema.YlioppilastutkinnonSuoritus",
          "fi.oph.koski.schema.Ylioppilastutkinto",
        ).map(cn => schemaTypes.find(_.fullClassName == cn).get)

        val ts = TypescriptTypes.build(types, defaultOptions.copy(
          exportClassNamesAs = Some("$class"),
          exportConstructors = true,
        ))

        ts should equal(
          """/*
            | * fi.oph.koski.schema
            | */
            |
            |export type YlioppilastutkinnonSuoritus = {
            |    $class: "YlioppilastutkinnonSuoritus",
            |    tyyppi: Koodistokoodiviite<"suorituksentyyppi", "ylioppilastutkinto">,
            |    tila?: Koodistokoodiviite<"suorituksentila", string>,
            |    koulusivistyskieli?: Array<Koodistokoodiviite<"kieli", "FI" | "SV">>,
            |    pakollisetKokeetSuoritettu: boolean,
            |    koulutusmoduuli: Ylioppilastutkinto,
            |    toimipiste?: OrganisaatioWithOid,
            |    osasuoritukset?: Array<YlioppilastutkinnonKokeenSuoritus>,
            |    vahvistus?: Organisaatiovahvistus
            |}
            |
            |export type Ylioppilastutkinto = {
            |    $class: "Ylioppilastutkinto",
            |    tunniste: Koodistokoodiviite<"koulutus", "301000">,
            |    perusteenDiaarinumero?: string,
            |    koulutustyyppi?: Koodistokoodiviite<"koulutustyyppi", string>
            |}
            |
            |// Object constructors
            |
            |export const YlioppilastutkinnonSuoritus = (o: {
            |    tyyppi?: Koodistokoodiviite<"suorituksentyyppi", "ylioppilastutkinto">,
            |    tila?: Koodistokoodiviite<"suorituksentila", string>,
            |    koulusivistyskieli?: Array<Koodistokoodiviite<"kieli", "FI" | "SV">>,
            |    pakollisetKokeetSuoritettu: boolean,
            |    koulutusmoduuli?: Ylioppilastutkinto,
            |    toimipiste?: OrganisaatioWithOid,
            |    osasuoritukset?: Array<YlioppilastutkinnonKokeenSuoritus>,
            |    vahvistus?: Organisaatiovahvistus
            |}): YlioppilastutkinnonSuoritus => ({tyyppi: Koodistokoodiviite({koodiarvo: "ylioppilastutkinto",koodistoUri: "suorituksentyyppi"}),koulutusmoduuli: Ylioppilastutkinto({tunniste: Koodistokoodiviite({koodiarvo: "301000",koodistoUri: "koulutus"})}),$class: "YlioppilastutkinnonSuoritus", ...o})
            |
            |export const Ylioppilastutkinto = (o: {
            |    tunniste?: Koodistokoodiviite<"koulutus", "301000">,
            |    perusteenDiaarinumero?: string,
            |    koulutustyyppi?: Koodistokoodiviite<"koulutustyyppi", string>
            |} = {}): Ylioppilastutkinto => ({$class: "Ylioppilastutkinto",tunniste: Koodistokoodiviite({koodiarvo: "301000",koodistoUri: "koulutus"}), ...o})""".stripMargin)
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
