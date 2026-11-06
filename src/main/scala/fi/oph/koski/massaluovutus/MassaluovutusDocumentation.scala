package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.Markdown
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetExamples
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.{QueryOrganisaationOpiskeluoikeudetCsvDocumentation, QueryOrganisaationOpiskeluoikeudetJsonDocumentation}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudetDocumentation
import fi.oph.koski.massaluovutus.raportit.{QueryAikuistenPerusopetuksenKurssikertymaDocumentation, QueryAikuistenPerusopetuksenOppijamaaratRaporttiDocumentation, QueryAikuistenPerusopetusSuoritustiedotDocumentation, QueryAmmatillinenOpiskelijavuositiedotDocumentation, QueryAmmatillinenOsittainenSuoritustiedotDocumentation, QueryAmmatillinenTutkintoSuoritustiedotDocumentation, QueryEsiopetuksenOppijamaaratRaporttiDocumentation, QueryEsiopetusDocumentation, QueryIBSuoritustiedotDocumentation, QueryLukio2019OpintopistekertymatDocumentation, QueryLukio2019SuoritustiedotDocumentation, QueryLukioDiaIbInternationalESHOpiskelijamaaratDocumentation, QueryLukioKurssikertymatDocumentation, QueryLukionSuoritustiedotDocumentation, QueryLuvaOpiskelijamaaratDocumentation, QueryMuuAmmatillinenDocumentation, QueryMuuKuinSaanneltyKoulutusDocumentation, QueryPerusopetukseenValmistavaDocumentation, QueryPerusopetuksenLisaopetuksenOppijamaaratRaporttiDocumentation, QueryPerusopetuksenOppijamaaratRaporttiDocumentation, QueryPerusopetuksenVuosiluokkaDocumentation, QueryTOPKSAmmatillinenDocumentation, QueryTuvaPerusopetuksenOppijamaaratRaporttiDocumentation, QueryTuvaSuoritustiedotDocumentation, QueryVSTJOTPADocumentation, QueryVSTKOPSDocumentation, QueryVSTOsaamismerkitDocumentation, QueryVSTVapaatavoitteinenDocumentation}
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQueryDocumentation
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat.ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation
import fi.oph.koski.massaluovutus.valpas.oppivelvolliset.ValpasOppivelvollisetQuery
import fi.oph.koski.schema
import fi.oph.koski.util.TryWithLogging
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.JValue

import java.net.URI
import java.time.{Duration, OffsetDateTime}
import java.util.UUID
import scala.io.Source
import scala.reflect.runtime.universe.TypeTag
import scala.util.matching.Regex
import scala.xml.{Elem, Node, NodeSeq, Text}

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
    "massaluovutus_yleista" -> "documentation/massaluovutus-yleista.md",
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
      val expanded = addQueryDocs(addClassTitles(addClassDocs(addJsonExamples(application, html))))
      addVariableTexts(application, addToc(addHeadingIds(expanded)))
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

  // Direktiivien yhteinen muoto: {{nimi:arg1,arg2|lippu|avain=arvo}}
  case class DirectiveArgs(names: List[String], flags: Set[String], options: Map[String, String]) {
    def exclude: Set[String] = options.get("exclude").map(_.split(',').map(_.trim).toSet).getOrElse(Set.empty)
  }

  def parseDirectiveArgs(arg: String): DirectiveArgs = {
    val parts = arg.split('|').toList.map(_.trim)
    val names = parts.head.split(',').toList.map(_.trim).filter(_.nonEmpty)
    val (keyValues, flags) = parts.tail.partition(_.contains('='))
    DirectiveArgs(
      names,
      flags.toSet,
      keyValues.map { kv =>
        val Array(k, v) = kv.split("=", 2)
        k.trim -> v.trim
      }.toMap,
    )
  }

  // {{docs:luokka[,luokka]|title=Otsikko|exclude=kenttä,kenttä|open}}: kenttätaulukko avattavassa lohkossa
  def addClassDocs(markdown: String): String =
    """\{\{docs:(?<args>.+?)}}""".r
      .replaceAllIn(markdown, { m =>
        val args = parseDirectiveArgs(m.group("args"))
        Regex.quoteReplacement(PropertyHtmlDocs.propertiesDetails(
          classNames = args.names,
          title = args.options.getOrElse("title", "Parametrit"),
          exclude = args.exclude,
          open = args.flags.contains("open"),
        ))
      })

  def addClassTitles(markdown: String): String =
    """\{\{title:(?<name>.+?)}}""".r
      .replaceAllIn(markdown, { m =>
        val className = m.group("name")
        Regex.quoteReplacement(PropertyHtmlDocs.headingForClass(className))
      })

  // {{query:esimerkki[,esimerkki]|h3|raportti|exclude=kenttä}}: kyselyn otsikko, kuvaus, parametrit ja esimerkki.
  // Useampi esimerkki samassa direktiivissä tuottaa formaattikohtaiset välilehdet (esim. CSV/JSON).
  // raportti = jättää pois RaporttikyselynYhteisetParametrit-kentät, jotka kuvataan raporttisivulla kerran.
  def addQueryDocs(markdown: String): String =
    """\{\{query:(?<args>.+?)}}""".r
      .replaceAllIn(markdown, { m =>
        val args = parseDirectiveArgs(m.group("args"))
        val examples = args.names.map(name => QueryExamples.queryByName(name).toRight(name))
        val html = examples.collectFirst { case Left(name) => s"Esimerkkiä ei löydy: $name" }.getOrElse {
          val exclude = args.exclude ++ (if (args.flags.contains("raportti")) RaporttikyselynYhteisetParametrit.kentät else Set.empty)
          QueryHtmlDocs.render(
            examples = examples.collect { case Right(e) => e },
            headingLevel = if (args.flags.contains("h3")) 3 else 2,
            exclude = exclude,
          )
        }
        Regex.quoteReplacement(html)
      })

  // Otsikoille ankkurit sivun sisäistä navigointia varten. Kyselyosioiden otsikoilla on jo kyselytyypin mukainen id,
  // joten markdown-otsikon ankkuri saa juoksevan numeron, jos se osuisi samaan.
  def addHeadingIds(html: String): String = {
    val usedIds = scala.collection.mutable.Set.from("""id="([^"]+)"""".r.findAllMatchIn(html).map(_.group(1)))
    """(?s)<h([23])>(.*?)</h\1>""".r
      .replaceAllIn(html, { m =>
        val level = m.group(1)
        val content = m.group(2)
        val base = slug(content)
        val id = LazyList.from(1).map(i => if (i == 1) base else s"$base-$i").find(!usedIds.contains(_)).get
        usedIds += id
        Regex.quoteReplacement(s"""<h$level id="$id">$content</h$level>""")
      })
  }

  // {{toc}}: sisällysluettelo sivun h2- ja h3-otsikoista. Ajetaan addHeadingIds:n jälkeen, jotta ankkurit ovat olemassa.
  def addToc(html: String): String =
    if (!html.contains("{{toc}}")) html else {
      val headings = """(?s)<h([23]) id="([^"]+)">(.*?)</h\1>""".r.findAllMatchIn(html).toList
        .map(m => (m.group(1).toInt, m.group(2), m.group(3).replaceAll("<[^>]+>", "").trim))
      val grouped = headings.foldLeft(List.empty[((String, String), List[(String, String)])]) {
        case (acc, (2, id, text)) => ((id, text), Nil) :: acc
        case (Nil, (_, id, text)) => ((id, text), Nil) :: Nil
        case ((parent, children) :: rest, (_, id, text)) => (parent, children :+ (id, text)) :: rest
      }.reverse
      val toc =
        <nav class="toc">
          <ul>
            {grouped.map { case ((id, text), children) =>
              <li>
                <a href={s"#$id"}>{text}</a>
                {if (children.nonEmpty) <ul>{children.map { case (cid, ctext) => <li><a href={s"#$cid"}>{ctext}</a></li> }}</ul> else NodeSeq.Empty}
              </li>
            }}
          </ul>
        </nav>
      html.replace("{{toc}}", toc.toString())
    }

  def slug(headingHtml: String): String =
    headingHtml
      .replaceAll("<[^>]+>", "")
      .trim
      .toLowerCase
      .replaceAll("[^\\p{L}\\p{N}]+", "-")
      .stripPrefix("-")
      .stripSuffix("-")

  def addVariableTexts(application: KoskiApplication, markdown: String): String = {
    val rootUrl = URI.create(application.config.getString("koski.root.url"))
    val host = rootUrl.getHost
    val baseUrl = rootUrl.getRawPath

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
      {description(schema.metadata)}
    </div>.toString()
  }

  def propertiesForClass(className: String): String =
    renderPropertiesTable(getSchema(className)).toString

  def propertiesDetails(classNames: List[String], title: String, exclude: Set[String], open: Boolean): String =
    details(title, renderPropertiesTable(mergedProperties(classNames.map(getSchema), exclude)), open).toString

  def details(title: String, content: NodeSeq, open: Boolean = false): Elem = {
    val elem = <details class="doc-details"><summary>{title}</summary>{content}</details>
    if (open) elem % scala.xml.Attribute(None, "open", Text("open"), scala.xml.Null) else elem
  }

  def renderPropertiesTable(schema: SchemaWithClassName): Node =
    schema match {
      case c: ClassSchema => renderPropertiesTable(c.properties)
      case _ => Text("todo")
    }

  def renderPropertiesTable(properties: Seq[Property]): Node =
    <table>
      <tr>
        <th>Kenttä</th>
        <th>Arvo</th>
        <th>Selite</th>
      </tr>
      {sort(properties).map(renderProperty)}
      <tr>
        <td colspan="3">
          <b>*</b> = pakollinen kenttä
        </td>
      </tr>
    </table>

  // Saman kyselyn eri formaattiluokkien kentät yhdistettynä: yhteisen kentän sallitut arvot kootaan kaikista luokista
  def mergedProperties(schemas: List[SchemaWithClassName], exclude: Set[String]): Seq[Property] = {
    val all = schemas.collect { case c: ClassSchema => c.properties }.flatten
    all.map(_.key).distinct.filterNot(exclude).map { key =>
      val variants = all.filter(_.key == key)
      val enumValues = variants.flatMap(p => stringEnumValues(p.schema)).distinct
      val first = variants.head
      if (enumValues.nonEmpty && variants.size > 1) first.copy(schema = withEnumValues(first.schema, enumValues)) else first
    }
  }

  private def stringEnumValues(schema: Schema): List[String] = schema match {
    case s: StringSchema => s.enumValues.getOrElse(Nil)
    case o: OptionalSchema => stringEnumValues(o.itemSchema)
    case _ => Nil
  }

  private def withEnumValues(schema: Schema, values: List[String]): Schema = schema match {
    case s: StringSchema => s.copy(enumValues = Some(values))
    case o: OptionalSchema => o.copy(itemSchema = withEnumValues(o.itemSchema, values))
    case other => other
  }

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
            val strs = enums.map(value => s""""$value"""")
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

  def description(metadata: List[Metadata]): Node =
    descriptions(metadata.collect { case d: Description => d.text })

  def descriptions(texts: List[String]): Node =
    scala.xml.XML.loadString("<div class=\"description\">" + texts.mkString(" ") + "</div>")

  private def sort(properties: Seq[Property]): Seq[Property] =
    properties.sortBy(p => if (isOptional(p)) 1 else 0)

  private def isOptional(property: Property): Boolean =
    property.schema.isInstanceOf[OptionalSchema]

  def getSchema(className: String): SchemaWithClassName =
    SchemaFactory().createSchema(Class.forName(className))

}

object QueryHtmlDocs {
  case class FormatTab(label: String, descriptions: List[String], properties: Seq[Property], example: QueryExample)

  def render(examples: List[QueryExample], headingLevel: Int, exclude: Set[String]): String = {
    val schemas = examples.map(e => PropertyHtmlDocs.getSchema(e.query.getClass.getName).asInstanceOf[ClassSchema])
    val titles = schemas.map(_.metadata.collect { case Title(t) => t })
    val descriptions = schemas.map(_.metadata.collect { case d: Description => d.text })
    val commonTitles = titles.head.filter(t => titles.tail.forall(_.contains(t)))
    val commonDescriptions = descriptions.head.filter(d => descriptions.tail.forall(_.contains(d)))
    val title = if (commonTitles.nonEmpty) commonTitles.mkString(" ") else schemas.head.title
    val id = queryType(schemas.head).getOrElse(QueryDocumentation.slug(title))

    val tabs = examples.zip(schemas).zip(descriptions).map { case ((example, schema), descs) =>
      FormatTab(
        label = formatLabel(example.query.format),
        descriptions = descs.filterNot(commonDescriptions.contains),
        properties = PropertyHtmlDocs.mergedProperties(List(schema), exclude),
        example = example,
      )
    }

    val heading: Elem = if (headingLevel == 3) <h3 id={id}>{title}</h3> else <h2 id={id}>{title}</h2>
    val body: NodeSeq = if (tabs.size == 1) tabBody(tabs.head) else formatTabs(id, tabs)
    val section: Elem =
      <section class="query-doc">
        {heading}
        {PropertyHtmlDocs.descriptions(commonDescriptions)}
        {body}
      </section>
    section.toString()
  }

  private def formatTabs(id: String, tabs: List[FormatTab]): NodeSeq = {
    val inputs = tabs.zipWithIndex.map { case (_, i) =>
      val input = <input type="radio" name={s"format-$id"} id={s"format-$id-$i"}/>
      if (i == 0) input % scala.xml.Attribute(None, "checked", Text("checked"), scala.xml.Null) else input
    }
    <div class="format-tabs">
      {inputs}
      <div class="format-tab-labels">
        {tabs.zipWithIndex.map { case (tab, i) => <label for={s"format-$id-$i"}>{tab.label}</label> }}
      </div>
      {tabs.map(tab => <section class="format-tab">{tabBody(tab)}</section>)}
    </div>
  }

  private def tabBody(tab: FormatTab): NodeSeq =
    <div class="query-doc-body">
      {if (tab.descriptions.nonEmpty) PropertyHtmlDocs.descriptions(tab.descriptions) else NodeSeq.Empty}
      {PropertyHtmlDocs.details("Parametrit", PropertyHtmlDocs.renderPropertiesTable(tab.properties))}
      {PropertyHtmlDocs.details("Esimerkki", exampleRequest(tab.example))}
    </div>

  // {{var:…}}-muuttujat korvataan vasta direktiivien jälkeen, joten ne voi jättää tähän sellaisenaan
  private def exampleRequest(example: QueryExample): Node =
    <pre><code>{Text(s"POST {{var:baseUrl}}${apiPath(example.query)} HTTP/1.1\n{{var:headers}}\n\n${example.json}")}</code></pre>

  private def apiPath(query: MassaluovutusQueryParameters): String = query match {
    case _: ValpasMassaluovutusQueryParameters => "/valpas/api/massaluovutus"
    case _ => "/api/massaluovutus"
  }

  private def queryType(schema: ClassSchema): Option[String] =
    schema.properties.find(_.key == "type").map(_.schema).collect {
      case s: StringSchema => s.enumValues.flatMap(_.headOption).map(_.toString)
    }.flatten

  private def formatLabel(format: String): String = format match {
    case QueryFormat.json => "JSON"
    case QueryFormat.csv | QueryFormat.csvPartition => "CSV"
    case QueryFormat.xlsx => "XLSX"
    case other => other
  }
}

case class QueryExample(query: MassaluovutusQueryParameters, json: String)

object QueryExamples {
  lazy val queryId: String = UUID.randomUUID().toString
  lazy val createdAt: OffsetDateTime = OffsetDateTime.now()
  lazy val startedAt: OffsetDateTime = createdAt.plus(Duration.ofSeconds(1))
  lazy val finishedAt: OffsetDateTime = startedAt.plus(Duration.ofMinutes(5))

  def jsonByName(application: KoskiApplication, name: String): Option[String] = queryByName(name).map(_.json).orElse(name match {
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
    case _ => None
  })


  // Kyselyesimerkit myös olioina {{query:…}}-direktiiviä varten
  def queryByName(name: String): Option[QueryExample] = name match {
    case "OrganisaationOpiskeluoikeudetCsv" => Some(example(QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example))
    case "OrganisaationOpiskeluoikeudetJson" => Some(example(QueryOrganisaationOpiskeluoikeudetJsonDocumentation.example))
    case "Valintalaskenta" => Some(example(ValintalaskentaQueryDocumentation.example))
    case "LuokalleJaaneetJson" => Some(example(MassaluovutusQueryLuokalleJaaneetExamples.jsonQuery))
    case "LuokalleJaaneetCsv" => Some(example(MassaluovutusQueryLuokalleJaaneetExamples.csvQuery))
    case "ValpasOppivelvollisetQuery" => Some(example(ValpasOppivelvollisetQuery.example))
    case "ValpasEiOppivelvollisuuttaSuorittavatQuery" => Some(example(ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation.example))
    case "PaallekkaisetOpiskeluoikeudetCsv" => Some(example(QueryPaallekkaisetOpiskeluoikeudetDocumentation.csvExample))
    case "AmmatillinenOpiskelijavuositiedotXlsx" => Some(example(QueryAmmatillinenOpiskelijavuositiedotDocumentation.xlsxExample))
    case "AmmatillinenTutkintoSuoritustiedotXlsx" => Some(example(QueryAmmatillinenTutkintoSuoritustiedotDocumentation.xlsxExample))
    case "AmmatillinenOsittainenSuoritustiedotXlsx" => Some(example(QueryAmmatillinenOsittainenSuoritustiedotDocumentation.xlsxExample))
    case "LukionSuoritustiedotXlsx" => Some(example(QueryLukionSuoritustiedotDocumentation.xlsxExample))
    case "Lukio2019SuoritustiedotXlsx" => Some(example(QueryLukio2019SuoritustiedotDocumentation.xlsxExample))
    case "LukioKurssikertymatXlsx" => Some(example(QueryLukioKurssikertymatDocumentation.xlsxExample))
    case "Lukio2019OpintopistekertymatXlsx" => Some(example(QueryLukio2019OpintopistekertymatDocumentation.xlsxExample))
    case "LukioDiaIbInternationalESHOpiskelijamaaratXlsx" => Some(example(QueryLukioDiaIbInternationalESHOpiskelijamaaratDocumentation.xlsxExample))
    case "MuuAmmatillinenXlsx" => Some(example(QueryMuuAmmatillinenDocumentation.xlsxExample))
    case "PaallekkaisetOpiskeluoikeudetXlsx" => Some(example(QueryPaallekkaisetOpiskeluoikeudetDocumentation.xlsxExample))
    case "TOPKSAmmatillinenXlsx" => Some(example(QueryTOPKSAmmatillinenDocumentation.xlsxExample))
    case "PerusopetuksenVuosiluokkaXlsx" => Some(example(QueryPerusopetuksenVuosiluokkaDocumentation.xlsxExample))
    case "AikuistenPerusopetusSuoritustiedotXlsx" => Some(example(QueryAikuistenPerusopetusSuoritustiedotDocumentation.xlsxExample))
    case "AikuistenPerusopetuksenOppijamaaratRaporttiXlsx" => Some(example(QueryAikuistenPerusopetuksenOppijamaaratRaporttiDocumentation.xlsxExample))
    case "AikuistenPerusopetuksenKurssikertymaXlsx" => Some(example(QueryAikuistenPerusopetuksenKurssikertymaDocumentation.xlsxExample))
    case "PerusopetuksenOppijamaaratRaporttiXlsx" => Some(example(QueryPerusopetuksenOppijamaaratRaporttiDocumentation.xlsxExample))
    case "EsiopetusXlsx" => Some(example(QueryEsiopetusDocumentation.xlsxExample))
    case "EsiopetuksenOppijamaaratRaporttiXlsx" => Some(example(QueryEsiopetuksenOppijamaaratRaporttiDocumentation.xlsxExample))
    case "PerusopetukseenValmistavaXlsx" => Some(example(QueryPerusopetukseenValmistavaDocumentation.xlsxExample))
    case "TuvaPerusopetuksenOppijamaaratRaporttiXlsx" => Some(example(QueryTuvaPerusopetuksenOppijamaaratRaporttiDocumentation.xlsxExample))
    case "TuvaSuoritustiedotXlsx" => Some(example(QueryTuvaSuoritustiedotDocumentation.xlsxExample))
    case "IBSuoritustiedotXlsx" => Some(example(QueryIBSuoritustiedotDocumentation.xlsxExample))
    case "VSTJOTPAXlsx" => Some(example(QueryVSTJOTPADocumentation.xlsxExample))
    case "VSTVapaatavoitteinenXlsx" => Some(example(QueryVSTVapaatavoitteinenDocumentation.xlsxExample))
    case "VSTKOPSXlsx" => Some(example(QueryVSTKOPSDocumentation.xlsxExample))
    case "VSTOsaamismerkitXlsx" => Some(example(QueryVSTOsaamismerkitDocumentation.xlsxExample))
    case "MuuKuinSaanneltyKoulutusXlsx" => Some(example(QueryMuuKuinSaanneltyKoulutusDocumentation.xlsxExample))
    case "LuvaOpiskelijamaaratXlsx" => Some(example(QueryLuvaOpiskelijamaaratDocumentation.xlsxExample))
    case "PerusopetuksenLisaopetuksenOppijamaaratRaporttiXlsx" => Some(example(QueryPerusopetuksenLisaopetuksenOppijamaaratRaporttiDocumentation.xlsxExample))
    case _ => None
  }

  private def example[T <: MassaluovutusQueryParameters : TypeTag](query: T): QueryExample =
    QueryExample(query, JsonSerializer.writeWithRoot(query, pretty = true))

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

@Title("Raporttikyselyiden yhteiset parametrit")
@Description("Kaikilla raporttikyselyillä on kyselykohtaisten parametrien lisäksi nämä parametrit.")
case class RaporttikyselynYhteisetParametrit(
  @Description("Tulosten toimitusformaatti. Raportit palautetaan xlsx-muodossa.")
  @EnumValues(Set(QueryFormat.xlsx))
  format: String,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid],
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String],
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String],
)

object RaporttikyselynYhteisetParametrit {
  val kentät: Set[String] = Set("format", "organisaatioOid", "language", "password")
}
