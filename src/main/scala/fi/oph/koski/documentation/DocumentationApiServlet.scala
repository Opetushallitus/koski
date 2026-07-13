package fi.oph.koski.documentation

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koodisto.Koodistot
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetResult
import fi.oph.koski.massaluovutus.suorituspalvelu.SupaResponse
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaResult
import fi.oph.koski.massaluovutus.{QueryDocumentation, QueryResponse}
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Documentation
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.supa.SupaOpiskeluoikeudenVersioResponse
import fi.oph.koski.valpas.massaluovutus.{ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult, ValpasOppivelvollisetMassaluovutusResult}

import scala.reflect.runtime.{universe => ru}

class DocumentationApiServlet(application: KoskiApplication) extends KoskiSpecificApiServlet with Unauthenticated with NoCache {
  private lazy val localizedSchemas =
    new LocalizedSchemas(application.koskiLocalizationRepository)(application.cacheManager)

  get("/categoryNames.json") {
    KoskiTiedonSiirtoHtml.categoryNames
  }

  get("/categoryExampleMetadata.json") {
    KoskiTiedonSiirtoHtml.categoryExamples
  }

  get("/categoryExamples/:category/:name/table.html") {
    renderOption(KoskiErrorCategory.notFound)(KoskiTiedonSiirtoHtml.jsonTableHtmlContents(params("category"), params("name")))
  }

  get("/sections.html") {
    KoskiTiedonSiirtoHtml.htmlTextSections ++ QueryDocumentation.htmlTextSections(application) ++ OmaDataOAuth2Documentation.htmlTextSections(application)
  }

  get("/apiOperations.json") {
    KoskiTiedonSiirtoHtml.apiOperations
  }

  get("/examples/:name.json") {
    renderOption(KoskiErrorCategory.notFound)(Examples.oppijaExamples.find(_.name == params("name")).map(_.data))
  }
  get("/koski-oppija-schema.json") {
    localizedSchemas("koski-oppija-schema.json")
  }

  get("/valvira-oppija-schema.json") {
    localizedSchemas("valvira-oppija-schema.json")
  }

  get("/hakemuspalvelu-oppija-schema.json") {
    localizedSchemas("hakemuspalvelu-oppija-schema.json")
  }

  get("/hsl-oppija-schema.json") {
    localizedSchemas("hsl-oppija-schema.json")
  }

  get("/kela-oppija-schema.json") {
    localizedSchemas("kela-oppija-schema.json")
  }

  get("/suoritetut-tutkinnot-oppija-schema.json") {
    localizedSchemas("suoritetut-tutkinnot-oppija-schema.json")
  }

  get("/aktiiviset-ja-paattyneet-opinnot-oppija-schema.json") {
    localizedSchemas("aktiiviset-ja-paattyneet-opinnot-oppija-schema.json")
  }

  get("/kios-oppija-schema.json") {
    localizedSchemas("kios-oppija-schema.json")
  }

  get("/sdg-oppija-schema.json") {
    localizedSchemas("sdg-oppija-schema.json")
  }

  get("/ytl-oppija-schema.json") {
    localizedSchemas("ytl-oppija-schema.json")
  }

  get("/ytl-valpas-oppija-schema.json") {
    localizedSchemas("ytl-valpas-oppija-schema.json")
  }

  get("/valpas-kela-oppija-schema.json") {
    localizedSchemas("valpas-kela-oppija-schema.json")
  }

  get("/valpas-internal-laaja-schema.json") {
    localizedSchemas("valpas-internal-laaja-schema.json")
  }

  get("/valpas-internal-suppea-schema.json") {
    localizedSchemas("valpas-internal-suppea-schema.json")
  }

  get("/valpas-internal-kunta-suppea-schema.json") {
    localizedSchemas("valpas-internal-kunta-suppea-schema.json")
  }

  get("/valpas-internal-heturouhinta-schema.json") {
    localizedSchemas("valpas-internal-heturouhinta-schema.json")
  }

  get("/valpas-internal-kuntarouhinta-schema.json") {
    localizedSchemas("valpas-internal-kuntarouhinta-schema.json")
  }

  get("/migri-oppija-schema.json") {
    localizedSchemas("migri-oppija-schema.json")
  }

  get("/koodistot.json") {
    renderObject[List[String]](Koodistot.koodistoAsetukset.filter(_.koodistoVersio.isEmpty).map(_.toString))
  }

  get("/massaluovutus-response.json") {
    QueryDocumentation.responseSchemaJson
  }

  get("/massaluovutus-query.json") {
    QueryDocumentation.querySchemaJson
  }

  get("/massaluovutus-oph-query.json") {
    QueryDocumentation.ophQuerySchemaJson
  }

  get("/valpas-massaluovutus-query.json") {
    QueryDocumentation.valpasQuerySchemaJson
  }

  get("/valpas-oppivelvolliset-result.json") {
    ValpasOppivelvollisetMassaluovutusResult.schemaJson
  }

  get("/valpas-ei-oppivelvollisuutta-suorittavat-result.json") {
    ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult.schemaJson
  }

  get("/valintalaskenta-result.json") {
    ValintalaskentaResult.schemaJson
  }

  get("/suorituspalvelu-result.json") {
    SupaResponse.schemaJson
  }

  get("/suorituspalvelu-versio-result.json") {
    SupaOpiskeluoikeudenVersioResponse.schemaJson
  }

  get("/luokalle-jaaneet-result.json") {
    MassaluovutusQueryLuokalleJaaneetResult.schemaJson
  }

  get("/omadata-oauth2-suoritetut-tutkinnot-oppija-schema.json") {
    localizedSchemas("omadata-oauth2-suoritetut-tutkinnot-oppija-schema.json")
  }

  get("/omadata-oauth2-aktiiviset-ja-paattyneet-opinnot-oppija-schema.json") {
    localizedSchemas("omadata-oauth2-aktiiviset-ja-paattyneet-opinnot-oppija-schema.json")
  }

  get("/omadata-oauth2-kaikki-tiedot-oppija-schema.json") {
    localizedSchemas("omadata-oauth2-kaikki-tiedot-oppija-schema.json")
  }

  get("/omadata-oauth2-kaikki-tiedot-ja-valintatiedot-oppija-schema.json") {
    localizedSchemas("omadata-oauth2-kaikki-tiedot-ja-valintatiedot-oppija-schema.json")
  }

  override def toJsonString[T: ru.TypeTag](x: T): String = JsonSerializer.writeWithRoot(x)
}
