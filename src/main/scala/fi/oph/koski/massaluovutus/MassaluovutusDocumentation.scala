package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.Markdown
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetExamples
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.{QueryOrganisaationOpiskeluoikeudetCsvDocumentation, QueryOrganisaationOpiskeluoikeudetJsonDocumentation}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudetDocumentation
import fi.oph.koski.massaluovutus.raportit.{QueryAikuistenPerusopetuksenKurssikertymaDocumentation, QueryAikuistenPerusopetuksenOppijamaaratRaporttiDocumentation, QueryAikuistenPerusopetusSuoritustiedotDocumentation, QueryAmmatillinenOpiskelijavuositiedotDocumentation, QueryAmmatillinenOsittainenSuoritustiedotDocumentation, QueryAmmatillinenTutkintoSuoritustiedotDocumentation, QueryEsiopetuksenOppijamaaratRaporttiDocumentation, QueryEsiopetusDocumentation, QueryIBSuoritustiedotDocumentation, QueryLukio2019OpintopistekertymatDocumentation, QueryLukio2019SuoritustiedotDocumentation, QueryLukioDiaIbInternationalESHOpiskelijamaaratDocumentation, QueryLukioKurssikertymatDocumentation, QueryLukionSuoritustiedotDocumentation, QueryLuvaOpiskelijamaaratDocumentation, QueryMuuAmmatillinenDocumentation, QueryMuuKuinSaanneltyKoulutusDocumentation, QueryPerusopetukseenValmistavaDocumentation, QueryPerusopetuksenLisaopetuksenOppijamaaratRaporttiDocumentation, QueryPerusopetuksenOppijamaaratRaporttiDocumentation, QueryPerusopetuksenVuosiluokkaDocumentation, QueryTOPKSAmmatillinenDocumentation, QueryTuvaPerusopetuksenOppijamaaratRaporttiDocumentation, QueryTuvaSuoritustiedotDocumentation, QueryVSTJOTPADocumentation}
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQueryDocumentation
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation
import fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery
import fi.oph.koski.schema
import fi.oph.koski.util.TryWithLogging
import fi.oph.koski.xml.NodeSeqImplicits._
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.JValue

import java.net.URL
import java.time.{Duration, OffsetDateTime}
import java.util.UUID
import scala.io.Source
import scala.reflect.runtime.universe.TypeTag
import scala.xml.{Node, Text}

object QueryDocumentation extends Logging {
  // Skeema-jsonit

  lazy val responseSchemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[QueryResponseWrapper]).asInstanceOf[ClassSchema])

  lazy val querySchemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[QueryParametersWrapper]).asInstanceOf[ClassSchema])

  lazy val ophQuerySchemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[OphQueryParametersWrapper]).asInstanceOf[ClassSchema])

  lazy val valpasQuerySchemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[ValpasQueryParametersWrapper]).asInstanceOf[ClassSchema])

  // HTML-stringit, jotka palautetaan polusta /koski/api/documentation/sections.html

  private val sectionSources = Map(
    "massaluovutus_koulutuksenjarjestajat" -> "documentation/massaluovutus-koulutuksenjarjestajille.md",
    "massaluovutus_raportit" -> "documentation/massaluovutus-raportit.md",
    "massaluovutus_oph" -> "documentation/massaluovutus-oph.md",
    "massaluovutus_valpas" -> "documentation/massaluovutus-valpas.md"
  )

  def htmlTextSections(application: KoskiApplication): Map[String, String] =
    sectionSources.view.mapValues(htmlTextSection(application)).toMap

  def htmlTextSection(application: KoskiApplication)(path: String): String =
    TryWithLogging.andResources(logger, { use =>
      val source = use(Source.fromResource(path)).mkString
      val html = Markdown.markdownToXhtmlString(source)
      addVariableTexts(application, addClassTitles(addClassDocs(addJsonExamples(application, html))))
    }).getOrElse(missingSection(path))

  def missingSection(name: String): String =
    <p style="color: red"><b>Virhe:</b> resurssia {name} ei löydy</p>
      .toString()

  def addJsonExamples(application: KoskiApplication, markdown: String): String =
    """\{\{json:(?<name>\w+)}}""".r
      .replaceAllIn(markdown, { m =>
        val name = m.group("name")
        QueryExamples.jsonByName(application, name).getOrElse(s"Esimerkkiä ei löydy: $name")
      })

  def addClassDocs(markdown: String): String =
    """\{\{docs:(?<name>.+?)}}""".r
      .replaceAllIn(markdown, { m =>
        val className = m.group("name")
        PropertyHtmlDocs.propertiesForClass(className)
      })

  def addClassTitles(markdown: String): String =
    """\{\{title:(?<name>.+?)}}""".r
      .replaceAllIn(markdown, { m =>
        val className = m.group("name")
        PropertyHtmlDocs.headingForClass(className)
      })

  def addVariableTexts(application: KoskiApplication, markdown: String): String = {
    val rootUrl = new URL(application.config.getString("koski.root.url"))
    val host = rootUrl.getHost
    val baseUrl = rootUrl.getPath

    val vars = Map(
      "baseUrl" -> baseUrl,
      "headers" ->
        s"""|Host: $host
          |Content-Type: application/json
          |Accept: application/json
          |Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
          |Caller-Id: 1.2.246.562.10.00000000001.myservice
          |CSRF: 1.2.246.562.10.00000000001.myservice""".stripMargin
    )

    """\{\{var:(?<name>.+?)}}""".r
      .replaceAllIn(markdown, { m => vars.getOrElse(m.group("name"), "!!! NOT FOUND !!!") })
  }

}

object PropertyHtmlDocs {
  def headingForClass(className: String): String = {
    val schema = getSchema(className)
    <div>
      <h2>
        {schema.title}
      </h2>
      <p>
        {description(schema.metadata)}
      </p>
    </div>.toString()
  }

  def propertiesForClass(className: String): String =
    renderPropertiesTable(getSchema(className)).toString

  def renderPropertiesTable(schema: SchemaWithClassName): Node =
    <table>
      <tr>
        <th>Kenttä</th>
        <th>Arvo</th>
        <th>Selite</th>
      </tr>
      {schema match {
        case c: ClassSchema => sort(c.properties).map(renderProperty)
        case _ => "todo"
      }}
      <tr>
        <td colspan="3">
          <b>*</b> = pakollinen kenttä
        </td>
      </tr>
    </table>

  private def renderProperty(prop: Property): Node =
    <tr>
      <td>{if (isOptional(prop)) prop.key else <b>{prop.key}*</b>}</td>
      <td>{renderType(prop.schema)}</td>
      <td>{description(prop.metadata)}</td>
    </tr>

  private def renderType(schema: Schema): Node =
    schema match {
      case s: StringSchema =>
        s.enumValues match {
          case None => Text("Merkkijono")
          case Some(enums) =>
            val strs = enums.map(value => s"\"$value\"")
            if (strs.length == 1) {
              Text(strs.head)
            } else {
              <ul class="property-types">
                {strs.map(s => <li>{s}</li>)}
              </ul>
            }
        }
      case d: DateSchema => Text(
        if (d.dateType.getSimpleName.contains("Time")) {
          "Aikaleima (vvvv-kk-ppThh:mm:ss)"
        } else {
          "Päivämäärä (vvvv-kk-pp)"
        })
      case _: NumberSchema => Text("Luku")
      case _: BooleanSchema => Text("true/false")
      case o: OptionalSchema => renderType(o.itemSchema)
      case c: ClassSchema => Text(s"Objekti (${c.title})")
      case c: ClassRefSchema => Text(s"Objekti (${c.title})")
      case l: ListSchema => Text(s"${renderType(l.itemSchema)}[]")
      case a: Schema => Text(a.toString)
    }

  private def description(metadata: List[Metadata]) = {
    scala.xml.XML.loadString("<div>" + metadata.collect { case d: Description => d.text }.mkString(" ") + "</div>")
  }

  private def sort(properties: Seq[Property]): Seq[Property] =
    properties.sortBy(p => if (isOptional(p)) 1 else 0)

  private def isOptional(property: Property): Boolean =
    property.schema.isInstanceOf[OptionalSchema]

  private def getSchema(className: String): SchemaWithClassName =
    SchemaFactory().createSchema(Class.forName(className))

}

object QueryExamples {
  lazy val queryId: String = UUID.randomUUID().toString
  lazy val createdAt: OffsetDateTime = OffsetDateTime.now()
  lazy val startedAt: OffsetDateTime = createdAt.plus(Duration.ofSeconds(1))
  lazy val finishedAt: OffsetDateTime = startedAt.plus(Duration.ofMinutes(5))

  def jsonByName(application: KoskiApplication, name: String): Option[String] = name match {
    case "OrganisaationOpiskeluoikeudetCsv" => asJson(QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example)
    case "OrganisaationOpiskeluoikeudetJson" => asJson(QueryOrganisaationOpiskeluoikeudetJsonDocumentation.example)
    case "PendingQueryResponse" => asJson(pendingQuery(
      QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example,
      application.config.getString("koski.root.url"),
    ))
    case "RunningQueryResponse" => asJson(runningQuery(
      QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example,
      QueryOrganisaationOpiskeluoikeudetCsvDocumentation.outputFiles.take(1),
      application.config.getString("koski.root.url"),
    ))
    case "CompleteQueryResponse" => asJson(completedQuery(
      QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example,
      QueryOrganisaationOpiskeluoikeudetCsvDocumentation.outputFiles,
      application.config.getString("koski.root.url"),
      None,
    ))
    case "FailedQueryResponse" => asJson(faileddQuery(
      QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example,
      List.empty,
      application.config.getString("koski.root.url"),
    ))
    case "Valintalaskenta" => asJson(ValintalaskentaQueryDocumentation.example)
    case "ValintalaskentaPendingQueryResponse" => asJson(pendingQuery(
      ValintalaskentaQueryDocumentation.example,
      application.config.getString("koski.root.url"),
    ))
    case "ValintalaskentaRunningQueryResponse" => asJson(runningQuery(
      ValintalaskentaQueryDocumentation.example,
      ValintalaskentaQueryDocumentation.outputFiles.take(1),
      application.config.getString("koski.root.url"),
    ))
    case "ValintalaskentaCompleteQueryResponse" => asJson(completedQuery(
      ValintalaskentaQueryDocumentation.example,
      ValintalaskentaQueryDocumentation.outputFiles,
      application.config.getString("koski.root.url"),
      None,
    ))
    case "ValintalaskentaFailedQueryResponse" => asJson(faileddQuery(
      ValintalaskentaQueryDocumentation.example,
      List.empty,
      application.config.getString("koski.root.url"),
    ))
    case "LuokalleJaaneetJson" => asJson(MassaluovutusQueryLuokalleJaaneetExamples.jsonQuery)
    case "LuokalleJaaneetCsv" => asJson(MassaluovutusQueryLuokalleJaaneetExamples.csvQuery)
    case "ValpasOppivelvollisetQuery" => asJson(ValpasOppivelvollisetQuery.example)
    case "ValpasEiOppivelvollisuuttaSuorittavatQuery" => asJson(ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation.example)
    case "ValpasPendingQueryResponse" => asJson(pendingQuery(
      ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation.example,
      application.config.getString("koski.root.url") + "/valpas"
    ))
    case "ValpasRunningQueryResponse" => asJson(runningQuery(
      ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation.example,
      List("0.json"),
      application.config.getString("koski.root.url") + "/valpas",
    ))
    case "ValpasCompleteQueryResponse" => asJson(completedQuery(
      ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation.example,
      List("0.json", "1.json"),
      application.config.getString("koski.root.url") + "/valpas",
      None,
    ))
    case "ValpasFailedQueryResponse" => asJson(faileddQuery(
      ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation.example,
      List.empty,
      application.config.getString("koski.root.url") + "/valpas",
    ))
    case "AmmatillinenOpiskelijavuositiedotXlsx" => asJson(QueryAmmatillinenOpiskelijavuositiedotDocumentation.xlsxExample)
    case "AmmatillinenTutkintoSuoritustiedotXlsx" => asJson(QueryAmmatillinenTutkintoSuoritustiedotDocumentation.xlsxExample)
    case "AmmatillinenOsittainenSuoritustiedotXlsx" => asJson(QueryAmmatillinenOsittainenSuoritustiedotDocumentation.xlsxExample)
    case "LukionSuoritustiedotXlsx" => asJson(QueryLukionSuoritustiedotDocumentation.xlsxExample)
    case "Lukio2019SuoritustiedotXlsx" => asJson(QueryLukio2019SuoritustiedotDocumentation.xlsxExample)
    case "LukioKurssikertymatXlsx" => asJson(QueryLukioKurssikertymatDocumentation.xlsxExample)
    case "Lukio2019OpintopistekertymatXlsx" => asJson(QueryLukio2019OpintopistekertymatDocumentation.xlsxExample)
    case "LukioDiaIbInternationalESHOpiskelijamaaratXlsx" => asJson(QueryLukioDiaIbInternationalESHOpiskelijamaaratDocumentation.xlsxExample)
    case "MuuAmmatillinenXlsx" => asJson(QueryMuuAmmatillinenDocumentation.xlsxExample)
    case "PaallekkaisetOpiskeluoikeudetXlsx" => asJson(QueryPaallekkaisetOpiskeluoikeudetDocumentation.xlsxExample)
    case "TOPKSAmmatillinenXlsx" => asJson(QueryTOPKSAmmatillinenDocumentation.xlsxExample)
    case "PerusopetuksenVuosiluokkaXlsx" => asJson(QueryPerusopetuksenVuosiluokkaDocumentation.xlsxExample)
    case "AikuistenPerusopetusSuoritustiedotXlsx" => asJson(QueryAikuistenPerusopetusSuoritustiedotDocumentation.xlsxExample)
    case "AikuistenPerusopetuksenOppijamaaratRaporttiXlsx" => asJson(QueryAikuistenPerusopetuksenOppijamaaratRaporttiDocumentation.xlsxExample)
    case "AikuistenPerusopetuksenKurssikertymaXlsx" => asJson(QueryAikuistenPerusopetuksenKurssikertymaDocumentation.xlsxExample)
    case "PerusopetuksenOppijamaaratRaporttiXlsx" => asJson(QueryPerusopetuksenOppijamaaratRaporttiDocumentation.xlsxExample)
    case "EsiopetusXlsx" => asJson(QueryEsiopetusDocumentation.xlsxExample)
    case "EsiopetuksenOppijamaaratRaporttiXlsx" => asJson(QueryEsiopetuksenOppijamaaratRaporttiDocumentation.xlsxExample)
    case "PerusopetukseenValmistavaXlsx" => asJson(QueryPerusopetukseenValmistavaDocumentation.xlsxExample)
    case "TuvaPerusopetuksenOppijamaaratRaporttiXlsx" => asJson(QueryTuvaPerusopetuksenOppijamaaratRaporttiDocumentation.xlsxExample)
    case "TuvaSuoritustiedotXlsx" => asJson(QueryTuvaSuoritustiedotDocumentation.xlsxExample)
    case "IBSuoritustiedotXlsx" => asJson(QueryIBSuoritustiedotDocumentation.xlsxExample)
    case "VSTJOTPAXlsx" => asJson(QueryVSTJOTPADocumentation.xlsxExample)
    case "MuuKuinSaanneltyKoulutusXlsx" => asJson(QueryMuuKuinSaanneltyKoulutusDocumentation.xlsxExample)
    case "LuvaOpiskelijamaaratXlsx" => asJson(QueryLuvaOpiskelijamaaratDocumentation.xlsxExample)
    case "PerusopetuksenLisaopetuksenOppijamaaratRaporttiXlsx" => asJson(QueryPerusopetuksenLisaopetuksenOppijamaaratRaporttiDocumentation.xlsxExample)
    case _ => None
  }

  def pendingQuery(query: MassaluovutusQueryParameters, rootUrl: String): PendingQueryResponse =
    PendingQueryResponse(
      queryId = queryId,
      requestedBy = "1.2.246.562.24.123123123123",
      query = query,
      createdAt = createdAt,
      resultsUrl = resultsUrl(rootUrl, queryId),
    )

  def runningQuery(query: MassaluovutusQueryParameters, files: List[String], rootUrl: String): RunningQueryResponse =
    RunningQueryResponse(
      queryId = queryId,
      requestedBy = "1.2.246.562.24.123123123123",
      query = query,
      createdAt = createdAt,
      startedAt = startedAt,
      files = files.map(MassaluovutusServletUrls.file(rootUrl, queryId, _)),
      resultsUrl = resultsUrl(rootUrl, queryId),
      progress = Some(QueryProgress.from(75, startedAt.toLocalDateTime))
    )

  def completedQuery(query: MassaluovutusQueryParameters, files: List[String], rootUrl: String, password: Option[String]): CompleteQueryResponse =
    CompleteQueryResponse(
      queryId = queryId,
      requestedBy = "1.2.246.562.24.123123123123",
      query = query,
      createdAt = createdAt,
      startedAt = startedAt,
      finishedAt = finishedAt,
      files = files.map(MassaluovutusServletUrls.file(rootUrl, queryId, _)),
      password = password,
      sourceDataUpdatedAt = Some(createdAt),
    )

  def faileddQuery(query: MassaluovutusQueryParameters, files: List[String], rootUrl: String): FailedQueryResponse =
    FailedQueryResponse(
      queryId = queryId,
      requestedBy = "1.2.246.562.24.123123123123",
      query = query,
      createdAt = createdAt,
      startedAt = startedAt,
      finishedAt = finishedAt,
      files = files.map(MassaluovutusServletUrls.file(rootUrl, queryId, _)),
      hint = None,
      error = None,
    )

  private def resultsUrl(rootUrl: String, queryId: String): String =
    MassaluovutusServletUrls.query(
      rootUrl,
      queryId,
    )

  private def asJson[T: TypeTag](t: T): Option[String] =
    Some(JsonSerializer.writeWithRoot(t, pretty = true))
}

@Title("Massaluovutusrajapinnasta saatava vastaus")
case class QueryResponseWrapper(` `: QueryResponse)

@Title("Massaluovutusrajapintaan tehtävä kysely koulutuksenjärjestäjille")
case class QueryParametersWrapper(` `: KoulutuksenjärjestäjienMassaluovutusQueryParameters)

@Title("Massaluovutusrajapintaan tehtävä kysely Opetushallituksen palveluille")
case class OphQueryParametersWrapper(` `: OpetushallituksenMassaluovutusQueryParameters)

@Title("Valppaan massaluovutusrajapintaan tehtävä kysely")
case class ValpasQueryParametersWrapper(` `: ValpasMassaluovutusQueryParameters)
