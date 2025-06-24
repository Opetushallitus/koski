package fi.oph.koski.documentation

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.hakemuspalvelu.HakemuspalveluSchema
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.kela.KelaSchema
import fi.oph.koski.koodisto.Koodistot
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.luovutuspalvelu.HslResponse
import fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetResult
import fi.oph.koski.massaluovutus.suorituspalvelu.SupaResponse
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaResult
import fi.oph.koski.migri.MigriSchema
import fi.oph.koski.massaluovutus.{QueryDocumentation, QueryResponse}
import fi.oph.koski.omadataoauth2.{OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet, OmaDataOAuth2Documentation, OmaDataOAuth2KaikkiOpiskeluoikeudet, OmaDataOAuth2SuoritetutTutkinnot}
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.suoritusjako.{AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä, SuoritetutTutkinnotOppijaJakolinkillä}
import fi.oph.koski.valpas.kela.ValpasKelaSchema
import fi.oph.koski.valpas.oppija.ValpasInternalSchema
import fi.oph.koski.valpas.ytl.ValpasYtlSchema
import fi.oph.koski.valvira.ValviraSchema
import fi.oph.koski.vkt.VktSchema
import fi.oph.koski.ytl.YtlSchema

import scala.reflect.runtime.{universe => ru}

class DocumentationApiServlet(application: KoskiApplication) extends KoskiSpecificApiServlet with Unauthenticated with NoCache {
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
    KoskiSchema.schemaJson
  }

  get("/valvira-oppija-schema.json") {
    ValviraSchema.schemaJson
  }

  get("/hakemuspalvelu-oppija-schema.json") {
    HakemuspalveluSchema.schemaJson
  }

  get("/hsl-oppija-schema.json") {
    HslResponse.schemaJson
  }

  get("/kela-oppija-schema.json") {
    KelaSchema.schemaJson
  }

  get("/suoritetut-tutkinnot-oppija-schema.json") {
    SuoritetutTutkinnotOppijaJakolinkillä.schemaJson
  }

  get("/aktiiviset-ja-paattyneet-opinnot-oppija-schema.json") {
    AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä.schemaJson
  }

  get("/vkt-oppija-schema.json") {
    VktSchema.schemaJson
  }

  get("/ytl-oppija-schema.json") {
    YtlSchema.schemaJson
  }

  get("/ytl-valpas-oppija-schema.json") {
    ValpasYtlSchema.schemaJson
  }

  get("/valpas-kela-oppija-schema.json") {
    ValpasKelaSchema.schemaJson
  }

  get("/valpas-internal-laaja-schema.json") {
    ValpasInternalSchema.laajaSchemaJson
  }

  get("/valpas-internal-suppea-schema.json") {
    ValpasInternalSchema.suppeaSchemaJson
  }

  get("/valpas-internal-kunta-suppea-schema.json") {
    ValpasInternalSchema.kuntaSuppeaSchemaJson
  }

  get("/migri-oppija-schema.json") {
    MigriSchema.schemaJson
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

  get("/valintalaskenta-result.json") {
    ValintalaskentaResult.schemaJson
  }

  get("/suorituspalvelu-result.json") {
    SupaResponse.schemaJson
  }

  get("/luokalle-jaaneet-result.json") {
    MassaluovutusQueryLuokalleJaaneetResult.schemaJson
  }

  get("/omadata-oauth2-suoritetut-tutkinnot-oppija-schema.json") {
    OmaDataOAuth2SuoritetutTutkinnot.schemaJson
  }

  get("/omadata-oauth2-aktiiviset-ja-paattyneet-opinnot-oppija-schema.json") {
    OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet.schemaJson
  }

  get("/omadata-oauth2-kaikki-tiedot-oppija-schema.json") {
    OmaDataOAuth2KaikkiOpiskeluoikeudet.schemaJson
  }

  override def toJsonString[T: ru.TypeTag](x: T): String = JsonSerializer.writeWithRoot(x)
}
