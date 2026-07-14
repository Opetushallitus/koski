package fi.oph.koski.documentation

import fi.oph.koski.cache.{CacheManager, KeyValueCache, RefreshingCache}
import fi.oph.koski.hakemuspalvelu.HakemuspalveluOppija
import fi.oph.koski.kela.KelaOppija
import fi.oph.koski.kios.KiosOppija
import fi.oph.koski.localization.{LocalizationRepository, SchemaLocalizationEnricher}
import fi.oph.koski.luovutuspalvelu.HslResponse
import fi.oph.koski.migri.MigriOppija
import fi.oph.koski.omadataoauth2.{OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet, OmaDataOAuth2KaikkiOpiskeluoikeudet, OmaDataOAuth2KaikkiOpiskeluoikeudetJaValintatiedot, OmaDataOAuth2SuoritetutTutkinnot}
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.sdg.SdgOppija
import fi.oph.koski.suoritusjako.{AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä, SuoritetutTutkinnotOppijaJakolinkillä}
import fi.oph.koski.valpas.kela.ValpasKelaOppija
import fi.oph.koski.valpas.oppija.{OppijaHakutilanteillaLaajatTiedot, OppijaHakutilanteillaSuppeatTiedot, OppijaKuntailmoituksillaSuppeatTiedot}
import fi.oph.koski.valpas.rouhinta.{HeturouhinnanTulos, KuntarouhinnanTulos}
import fi.oph.koski.valpas.ytl.YtlMaksuttomuustieto
import fi.oph.koski.valvira.ValviraOppija
import fi.oph.koski.ytl.YtlOppija
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import scala.concurrent.duration.DurationInt

class LocalizedSchemas(localizationRepository: LocalizationRepository)(implicit cacheManager: CacheManager) {
  private def build(clazz: Class[_]): () => ClassSchema =
    () => KoskiSchema.createSchema(clazz).asInstanceOf[ClassSchema]

  private val schemasByName: Map[String, () => ClassSchema] = Map(
    "koski-oppija-schema.json" -> (() => KoskiSchema.schema),
    "valvira-oppija-schema.json" -> build(classOf[ValviraOppija]),
    "hakemuspalvelu-oppija-schema.json" -> build(classOf[HakemuspalveluOppija]),
    "hsl-oppija-schema.json" -> build(classOf[HslResponse]),
    "kela-oppija-schema.json" -> build(classOf[KelaOppija]),
    "suoritetut-tutkinnot-oppija-schema.json" -> build(classOf[SuoritetutTutkinnotOppijaJakolinkillä]),
    "aktiiviset-ja-paattyneet-opinnot-oppija-schema.json" -> build(classOf[AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä]),
    "kios-oppija-schema.json" -> build(classOf[KiosOppija]),
    "sdg-oppija-schema.json" -> build(classOf[SdgOppija]),
    "ytl-oppija-schema.json" -> build(classOf[YtlOppija]),
    "ytl-valpas-oppija-schema.json" -> build(classOf[YtlMaksuttomuustieto]),
    "valpas-kela-oppija-schema.json" -> build(classOf[ValpasKelaOppija]),
    "valpas-internal-laaja-schema.json" -> build(classOf[OppijaHakutilanteillaLaajatTiedot]),
    "valpas-internal-suppea-schema.json" -> build(classOf[OppijaHakutilanteillaSuppeatTiedot]),
    "valpas-internal-kunta-suppea-schema.json" -> build(classOf[OppijaKuntailmoituksillaSuppeatTiedot]),
    "valpas-internal-heturouhinta-schema.json" -> build(classOf[HeturouhinnanTulos]),
    "valpas-internal-kuntarouhinta-schema.json" -> build(classOf[KuntarouhinnanTulos]),
    "migri-oppija-schema.json" -> build(classOf[MigriOppija]),
    "omadata-oauth2-suoritetut-tutkinnot-oppija-schema.json" -> build(classOf[OmaDataOAuth2SuoritetutTutkinnot]),
    "omadata-oauth2-aktiiviset-ja-paattyneet-opinnot-oppija-schema.json" -> build(classOf[OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet]),
    "omadata-oauth2-kaikki-tiedot-oppija-schema.json" -> build(classOf[OmaDataOAuth2KaikkiOpiskeluoikeudet]),
    "omadata-oauth2-kaikki-tiedot-ja-valintatiedot-oppija-schema.json" -> build(classOf[OmaDataOAuth2KaikkiOpiskeluoikeudetJaValintatiedot])
  )

  private val cache = KeyValueCache[String, JValue](
    new RefreshingCache("LocalizedSchemas", RefreshingCache.Params(1.minute, maxSize = 50)),
    (name: String) => localize(schemasByName(name)())
  )

  def contains(name: String): Boolean = schemasByName.contains(name)

  def apply(name: String): JValue = cache(name)

  private def localize(schema: ClassSchema): JValue =
    SchemaToJson.toJsonSchema(schema)(new SchemaLocalizationEnricher(localizationRepository.localizations))
}
