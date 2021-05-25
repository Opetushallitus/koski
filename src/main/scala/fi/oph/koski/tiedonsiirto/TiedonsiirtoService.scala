package fi.oph.koski.tiedonsiirto

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.elasticsearch.{ElasticSearch, ElasticSearchIndex}
import fi.oph.koski.henkilo.{HenkilöOid, HenkilöRepository, Hetu}
import fi.oph.koski.http.{ErrorDetail, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.{extract, validateAndExtract}
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.json._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.KoskiAuditLogMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import fi.oph.koski.util.OptionalLists.optionalList
import fi.oph.koski.util._
import fi.oph.scalaschema.{SerializationContext, Serializer}
import io.prometheus.client.Counter
import org.json4s.JsonAST.{JArray, JString}
import org.json4s.jackson.JsonMethods
import org.json4s.{JValue, _}

object TiedonsiirtoService {
  private val settings = Map(
    "analysis" -> Map(
      "filter" -> Map(
        "finnish_folding" -> Map(
          "type" -> "icu_folding",
          "unicodeSetFilter" -> "[^åäöÅÄÖ]"
        )
      ),
      "analyzer" -> Map(
        "default" -> Map(
          "tokenizer" -> "icu_tokenizer",
          "filter" -> Array("finnish_folding", "lowercase")
        )
      )
    )
  )

  private val mapping = Map(
    "properties" -> Map(
      "virheet" -> Map(
        "properties" -> Map(
          "key" -> Map(
            "type" -> "text",
            "fields" -> Map(
              "keyword" -> Map(
                "ignore_above" -> 256,
                "type" -> "keyword"
              )
            )
          )
        ),
        "dynamic" -> false
      ),
      "data" -> Map(
        "properties" -> Map(
        ),
        "dynamic" -> false
      )
    )
  )
}

class TiedonsiirtoService(
  elastic: ElasticSearch,
  organisaatioRepository: OrganisaatioRepository,
  henkilöRepository: HenkilöRepository,
  koodistoviitePalvelu: KoodistoViitePalvelu,
  hetu: Hetu
) extends Logging {

  val index = new ElasticSearchIndex(
    elastic = elastic,
    name = "tiedonsiirto",
    mappingVersion = 2,
    mapping = TiedonsiirtoService.mapping,
    settings = TiedonsiirtoService.settings,
    initialLoader = () => ???
  )

  private val serializationContext = SerializationContext(KoskiSchema.schemaFactory, omitEmptyFields = false)
  private val tiedonSiirtoVirheet = Counter.build()
    .name("fi_oph_koski_tiedonsiirto_TiedonsiirtoService_virheet")
    .help("Koski tiedonsiirto virheet")
    .register()
  private val tiedonsiirtoBuffer = new ConcurrentBuffer[TiedonsiirtoDocument]

  def init(): Unit = index.init

  def statistics(): TiedonsiirtoStatistics = TiedonsiirtoStatistics(index)

  def haeTiedonsiirrot(query: TiedonsiirtoQuery)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, PaginatedResponse[Tiedonsiirrot]] = {
    haeTiedonsiirrot(filtersFrom(query), query.oppilaitos, query.paginationSettings)
  }

  def virheelliset(query: TiedonsiirtoQuery)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, PaginatedResponse[Tiedonsiirrot]] = {
    haeTiedonsiirrot(Map("exists" -> Map("field" -> "virheet.key")) :: filtersFrom(query), query.oppilaitos, query.paginationSettings)
  }

  def delete(ids: List[String])(implicit koskiSession: KoskiSpecificSession): Unit = {
    val query = toJValue(Map("query" -> ElasticSearch.allFilter(
      Map("terms" -> Map("_id" -> ids))
        :: Map("exists" -> Map("field" -> "virheet.key"))
        :: tallentajaOrganisaatioFilters(AccessType.tiedonsiirronMitätöinti))))
    index.deleteByQuery(query, refresh = true)
  }

  private def filtersFrom(query: TiedonsiirtoQuery)(implicit session: KoskiSpecificSession): List[Map[String, Any]] = {
    // vastaavasti kuin yhteenveto-kyselyssä, käytä tallentajaOrganisaatioOid:ia jos ja vain jos oppilaitos-OID puuttuu
    query.oppilaitos.toList.map(oppilaitos => {
      ElasticSearch.anyFilter(List(
        Map("term" -> Map("oppilaitokset.oid" -> oppilaitos)),
        Map("bool" -> Map(
          "must_not" -> Map("exists" -> Map("field" -> "oppilaitokset.oid")),
          "must" -> Map("term" -> Map("tallentajaOrganisaatioOid" -> oppilaitos))
        ))
      ))
    }) ++ tallentajaOrganisaatioFilters()
  }

  private def tallentajaOrganisaatioFilters(accessType: AccessType.Value = AccessType.read)
                                           (implicit session: KoskiSpecificSession): List[Map[String, Any]] = {
    tallentajaOrganisaatioFilter(accessType).toList
  }

  private def tallentajaOrganisaatioFilter(accessType: AccessType.Value = AccessType.read)
                                          (implicit session: KoskiSpecificSession): Option[Map[String, Any]] = {
    if (session.hasGlobalReadAccess) {
      None
    } else {
      val orgFilter = ElasticSearch.anyFilter(List(
        Map("terms" -> Map("tallentajaOrganisaatioOid" -> session.organisationOids(accessType))),
        Map("terms" -> Map("oppilaitokset.oid" -> session.organisationOids(accessType)))
      ))
      val filter = if (session.hasKoulutusmuotoRestrictions) {
        ElasticSearch.allFilter(List(orgFilter, Map(
          "terms" -> Map("koulutusmuoto" -> session.allowedOpiskeluoikeusTyypit)
        )))
      } else {
        orgFilter
      }
      Some(filter)
    }
  }

  private def haeTiedonsiirrot(filters: List[Map[String, Any]],
                               oppilaitosOid: Option[String],
                               paginationSettings: Option[PaginationSettings])
                              (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, PaginatedResponse[Tiedonsiirrot]] = {
    koskiSession.juuriOrganisaatiot.map(_.oid).foreach { oid =>
      AuditLog.log(KoskiAuditLogMessage(TIEDONSIIRTO_KATSOMINEN, koskiSession, Map(juuriOrganisaatio -> oid)))
    }

    val query = toJValue(ElasticSearch.applyPagination(paginationSettings, Map(
      "query" -> ElasticSearch.allFilter(filters),
      "sort" -> List(Map("aikaleima" -> "desc"), Map("oppija.sukunimi.keyword" -> "asc"), Map("oppija.etunimet.keyword" -> "asc"))
    )))

    val rows: Seq[TiedonsiirtoDocument] = index.runSearch(query)
      .map(response => extract[List[JValue]](response \ "hits" \ "hits").map(j => extract[TiedonsiirtoDocument](j \ "_source", ignoreExtras = true)))
      .getOrElse(Nil)

    val oppilaitosResult: Either[HttpStatus, Option[OrganisaatioWithOid]] = oppilaitosOid match {
      case Some(organisaatioOid) =>
        val org: Option[OrganisaatioWithOid] = organisaatioRepository.getOrganisaatioHierarkia(organisaatioOid).map(_.toOrganisaatio)
        org match {
          case Some(organisaatio) => Right(Some(organisaatio))
          case None => Left(KoskiErrorCategory.notFound.oppilaitostaEiLöydy(s"Oppilaitosta $oppilaitosOid ei löydy"))
        }
      case None =>
        Right(None)
    }

    oppilaitosResult.map { oppilaitos =>
      val converted: Tiedonsiirrot = Tiedonsiirrot(toHenkilönTiedonsiirrot(rows), oppilaitos = oppilaitos.map(_.toOidOrganisaatio))
      PaginatedResponse(paginationSettings, converted, rows.length)
    }
  }

  def storeTiedonsiirtoResult(implicit koskiSession: KoskiSpecificSession, oppijaOid: Option[OidHenkilö], validatedOppija: Option[Oppija], data: Option[JValue], error: Option[TiedonsiirtoError]) {
    if (!koskiSession.isPalvelukäyttäjä && !koskiSession.isRoot) {
      return
    }

    val henkilö = data.flatMap(extractHenkilö(_, oppijaOid))
    val lahdejarjestelma: Option[String] = data.flatMap(extractLahdejarjestelma)

    val oppilaitokset = validatedOppija
      .map(_.opiskeluoikeudet.toList.filter(_.oppilaitos.isDefined).map(_.oppilaitos.get.toOidOrganisaatio))
      .filter(_.nonEmpty)
      .orElse(data.flatMap(extractOppilaitos))
      .orElse(data.flatMap(extractOppilaitosFromToimipiste))
      .map(_.distinct)

    val koulutustoimija: List[OidOrganisaatio] = validatedOppija.flatMap(_.opiskeluoikeudet.headOption.flatMap(_.koulutustoimija.map(_.toOidOrganisaatio))).toList match {
      case Nil => oppilaitokset.toList.flatten.flatMap(organisaatioRepository.findKoulutustoimijaForOppilaitos).map(_.toOidOrganisaatio)
      case kt => kt
    }

    val suoritustiedot: Option[List[TiedonsiirtoSuoritusTiedot]] = validatedOppija.map(toSuoritustiedot)
    val koulutusmuoto = validatedOppija
      .flatMap(_.opiskeluoikeudet.headOption.map(_.tyyppi.koodiarvo))
      .orElse(data.flatMap(extractKoulutusmuoto))

    val juuriOrganisaatiot = if (koskiSession.isRoot) koulutustoimija else koskiSession.juuriOrganisaatiot

    juuriOrganisaatiot.foreach((org: OrganisaatioWithOid) => {
      val (data: Option[JValue], virheet: Option[List[ErrorDetail]]) = error.map(e => (Some(e.data), Some(e.virheet))).getOrElse((None, None))

      storeToElasticSearch(henkilö, org, oppilaitokset, koulutusmuoto, suoritustiedot, data, virheet, lahdejarjestelma, koskiSession.oid, Some(koskiSession.username), new Timestamp(System.currentTimeMillis))

      if (error.isDefined) {
        tiedonSiirtoVirheet.inc
      }
    })
  }

  def storeToElasticSearch(
    henkilö: Option[TiedonsiirtoOppija],
    org: OrganisaatioWithOid,
    oppilaitokset: Option[List[OidOrganisaatio]],
    koulutusmuoto: Option[String],
    suoritustiedot: Option[List[TiedonsiirtoSuoritusTiedot]],
    data: Option[JValue],
    virheet: Option[List[ErrorDetail]],
    lahdejarjestelma: Option[String],
    userOid: String,
    username: Option[String],
    aikaleima: Timestamp
  ) = {
    val tiedonsiirtoDoc = TiedonsiirtoDocument(
      userOid,
      username,
      org.oid,
      henkilö,
      oppilaitokset,
      koulutusmuoto,
      suoritustiedot,
      data,
      virheet.toList.flatten.isEmpty,
      virheet.getOrElse(Nil),
      lahdejarjestelma,
      aikaleima
    )
    tiedonsiirtoBuffer.append(tiedonsiirtoDoc)
  }

  def syncToElasticsearch(refresh: Boolean): Unit = synchronized {
    val tiedonsiirrot = tiedonsiirtoBuffer.popAll
    if (tiedonsiirrot.nonEmpty) {
      logger.debug(s"Syncing ${tiedonsiirrot.length} tiedonsiirrot documents")

      val docsAndIds = tiedonsiirrot.map { tiedonsiirto =>
        val doc = Serializer.serialize(tiedonsiirto, serializationContext)
        val id = tiedonsiirto.id
        (doc, id)
      }
      docsAndIds
        .grouped(1000)
        .map(group => index.updateBulk(group, upsert = true, refresh = refresh))
        .collect { case (errors, response) if errors => JsonMethods.pretty(response) }
        .foreach(resp => logger.error(s"Elasticsearch indexing failed: $resp"))
      logger.debug(s"Done syncing ${tiedonsiirrot.length} tiedonsiirrot documents")
    }
  }

  private def yhteenvetoOrdering(sorting: SortOrder, lang: String) = {
    val ordering = sorting.field match {
      case "aika" => Ordering.by{x: TiedonsiirtoYhteenveto => x.viimeisin.getTime}
      case "oppilaitos" => Ordering.by{x: TiedonsiirtoYhteenveto => x.oppilaitos.description.get(lang)}
      case "siirretyt" => Ordering.by{x: TiedonsiirtoYhteenveto => x.siirretyt}
      case "virheelliset" => Ordering.by{x: TiedonsiirtoYhteenveto => x.virheelliset}
      case "onnistuneet" => Ordering.by{x: TiedonsiirtoYhteenveto => x.onnistuneet}
    }
    if (sorting.descending) {
      ordering.reverse
    } else {
      ordering
    }
  }

  def yhteenveto(implicit koskiSession: KoskiSpecificSession, sorting: SortOrder): Seq[TiedonsiirtoYhteenveto] = {
    index.runSearch(yhteenvetoQuery).map { response =>
      for {
        orgResults <- extract[List[JValue]](response \ "aggregations" \ "organisaatio" \ "buckets")
        tallentajaOrganisaatioOid = extract[String](orgResults \ "key")
        tallentajaOrganisaatio = OidOrganisaatio(tallentajaOrganisaatioOid, Some(LocalizedString.unlocalized(tallentajaOrganisaatioOid)))
        oppilaitosResults <- extract[List[JValue]](orgResults \ "oppilaitos" \ "buckets")
        oppilaitosOidOrMissing = extract[String](oppilaitosResults \ "key")
        oppilaitosOid = if (oppilaitosOidOrMissing == "-") tallentajaOrganisaatioOid else oppilaitosOidOrMissing
        userResults <- extract[List[JValue]](oppilaitosResults \ "käyttäjä" \ "buckets")
        userOid = extract[String](userResults \ "key")
        lähdejärjestelmäResults <- extract[List[JValue]](userResults \ "lähdejärjestelmä" \ "buckets")
        lähdejärjestelmäId = extract[String](lähdejärjestelmäResults \ "key")
        lähdejärjestelmä = koodistoviitePalvelu.validate("lahdejarjestelma", lähdejärjestelmäId)
        siirretyt = extract[Int](lähdejärjestelmäResults \ "doc_count")
        epäonnistuneet = extract[Int](lähdejärjestelmäResults \ "fail" \ "doc_count")
        onnistuneet = siirretyt - epäonnistuneet
        viimeisin = new Timestamp(extract[Long](lähdejärjestelmäResults \ "viimeisin" \ "value"))

        tuoreDokumentti = extract[JArray](lähdejärjestelmäResults \ "tuoreDokumentti" \ "hits" \ "hits" \ "_source").arr
        oppilaitos = tuoreDokumentti
          .flatMap(t => t \ "oppilaitokset" match {
            case oa: JArray => extract[List[OidOrganisaatio]](oa)
            case _ => List()
          })
          .find(_.oid == oppilaitosOid)
          .getOrElse(getOrganisaatio(oppilaitosOid))
        käyttäjä: TiedonsiirtoKäyttäjä = tuoreDokumentti
          .find(t => t \ "tallentajaKäyttäjäOid" match {
            case JString(oid) if oid == userOid => true
            case _ => false
          })
          .flatMap(t => extract[Option[String]](t \ "tallentajaKäyttäjätunnus"))
          .map(username => TiedonsiirtoKäyttäjä(userOid, Some(username)))
          .getOrElse(TiedonsiirtoKäyttäjä(userOid, None))
      } yield {
        TiedonsiirtoYhteenveto(
          tallentajaOrganisaatio,
          oppilaitos,
          käyttäjä,
          viimeisin,
          siirretyt,
          epäonnistuneet,
          onnistuneet,
          lähdejärjestelmä
        )
      }
    }.getOrElse(Nil)
     .sorted(yhteenvetoOrdering(sorting, koskiSession.lang))
  }

  private def yhteenvetoQuery(implicit koskiSession: KoskiSpecificSession): JValue = {
    toJValue(Map(
      "size" -> 0,
      "aggs" ->
        Map(
          "organisaatio" -> Map(
            "terms" -> Map("field" -> "tallentajaOrganisaatioOid.keyword", "size" -> 20000),
            "aggs" -> Map(
              "oppilaitos" -> Map(
                "terms" -> Map("field" -> "oppilaitokset.oid.keyword", "size" -> 20000, "missing" -> "-"),
                "aggs" -> Map(
                  "käyttäjä" -> Map(
                    "terms" -> Map("field" -> "tallentajaKäyttäjäOid.keyword", "size" -> 20000),
                    "aggs" -> Map(
                      "lähdejärjestelmä" -> Map(
                        "terms" -> Map("field" -> "lähdejärjestelmä.keyword", "size" -> 20000, "missing" -> "-"),
                        "aggs" -> Map(
                          "viimeisin" -> Map("max" -> Map("field" -> "aikaleima")),
                          "fail" -> Map("filter" -> Map("term" -> Map("success" -> false))),
                          "tuoreDokumentti" -> Map("top_hits" ->
                            Map(
                              "sort" -> Array(Map("aikaleima" -> Map("order" -> "desc"))),
                              "_source" -> Map("includes" -> Array(
                                "oppilaitokset.oid",
                                "oppilaitokset.nimi",
                                "tallentajaKäyttäjäOid",
                                "tallentajaKäyttäjätunnus"
                              )),
                              "size" -> 1
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
    ) ++ tallentajaOrganisaatioFilter().map(filter => Map("query" -> filter)).getOrElse(Map()))
  }

  private def getOrganisaatio(oid: String) = {
    organisaatioRepository
      .getOrganisaatio(oid)
      .map(_.toOidOrganisaatio)
      .getOrElse(OidOrganisaatio(oid, Some(LocalizedString.unlocalized(oid))))
  }

  private def jsonStringList(value: JValue) = value match {
    case JArray(xs) => xs.collect { case JString(x) => x }
    case JString(x) => List(x)
    case JNothing => Nil
    case JNull => Nil
    case _ => throw new RuntimeException("Unreachable match arm" )
  }

  private def extractOppilaitos(data: JValue): Option[List[OidOrganisaatio]] =
    optionalList(jsonStringList(data \ "opiskeluoikeudet" \ "oppilaitos" \ "oid")
      .flatMap(organisaatioRepository.getOrganisaatio)
      .map(_.toOidOrganisaatio))

  private def extractOppilaitosFromToimipiste(data: JValue): Option[List[OidOrganisaatio]] = optionalList(for {
    JArray(suoritukset) <- data \ "opiskeluoikeudet" \ "suoritukset"
    JString(toimipisteOid) <- suoritukset.map(_ \ "toimipiste" \ "oid")
    toimipiste <- organisaatioRepository.getOrganisaatio(toimipisteOid)
    oppilaitos <- organisaatioRepository.findOppilaitosForToimipiste(toimipiste)
  } yield oppilaitos.toOidOrganisaatio)

  private def extractLahdejarjestelma(data: JValue): Option[String] = {
    data \ "opiskeluoikeudet" match {
      case JArray(opiskeluoikeudet) =>
        val lähdejärjestelmä: List[String] = opiskeluoikeudet.flatMap { opiskeluoikeus: JValue =>
          opiskeluoikeus \ "lähdejärjestelmänId" \ "lähdejärjestelmä" \ "koodiarvo" match {
            case JString(lähdejärjestelmä) => Some(lähdejärjestelmä)
            case _ => None
          }
        }
        lähdejärjestelmä.headOption
      case _ => None
    }
  }

  private def extractKoulutusmuoto(data: JValue): Option[String] =
    jsonStringList(data \ "opiskeluoikeudet" \ "tyyppi" \ "koodiarvo").headOption

  private def extractHenkilö(data: JValue,
                             oidHenkilö: Option[OidHenkilö])
                            (implicit user: KoskiSpecificSession): Option[TiedonsiirtoOppija] = {
    val annetutHenkilötiedot: JValue = data \ "henkilö"
    val annettuTunniste: Option[HetuTaiOid] = validateAndExtract[HetuTaiOid](
      annetutHenkilötiedot, ignoreExtras = true
    ).map { tunniste =>
      tunniste.copy(
        hetu = tunniste.hetu.flatMap(hetu.validate(_).toOption),
        oid = tunniste.oid.flatMap(HenkilöOid.validateHenkilöOid(_).toOption)
      )
    }.toOption
    val oid: Option[String] = oidHenkilö.map(_.oid).orElse(annettuTunniste.flatMap(_.oid))

    val haetutTiedot: Option[TiedonsiirtoOppija] = (oid, annettuTunniste.flatMap(_.hetu)) match {
      case (Some(oid), None) => henkilöRepository.findByOid(oid).map { h =>
        TiedonsiirtoOppija(Some(h.oid), h.hetu, h.syntymäaika, Some(h.etunimet), Some(h.kutsumanimi), Some(h.sukunimi))
      }
      // Tarkistaa vain oppijanumerorekisterin - ei luo uutta oppijanumeroa Virta/YTR-tietojen pohjalta
      case (None, Some(hetu)) => henkilöRepository.opintopolku.findByHetu(hetu).map { h =>
        TiedonsiirtoOppija(Some(h.oid), h.hetu, syntymäaika = None, Some(h.etunimet), Some(h.kutsumanimi), Some(h.sukunimi))
      }
      case _ => None
    }

    haetutTiedot.orElse(oidHenkilö match {
      case Some(oidHenkilö) => {
        validateAndExtract[TiedonsiirtoOppija](
          annetutHenkilötiedot.merge(JsonSerializer.serializeWithRoot(oidHenkilö)),
          ignoreExtras = true
        ).toOption
      }
      case None => {
        annetutHenkilötiedot.toOption.flatMap(
          validateAndExtract[TiedonsiirtoOppija](_, ignoreExtras = true).toOption
        )
      }
    })
  }

  private def toSuoritustiedot(oppija: Oppija): List[TiedonsiirtoSuoritusTiedot] = {
    val suoritukset = oppija.opiskeluoikeudet.flatMap(_.suoritukset).toList

    suoritukset.map(s => {
      val osaamisalat = s match {
        case s: Osaamisalallinen => s.osaamisala.map(_.map(_.osaamisala.nimi))
        case _ => None
      }

      val tutkintonimikkeet = s match {
        case s: Tutkintonimikkeellinen => s.tutkintonimike.map(_.map(_.nimi))
        case _ => None
      }

      TiedonsiirtoSuoritusTiedot(
        KoulutusmoduulinTiedonsiirtoTiedot(s.koulutusmoduuli.tunniste.getNimi),
        osaamisalat.map(_.map(OsaamisalanTiedonsiirtoTiedot)),
        tutkintonimikkeet.map(_.map(TutkintonimikkeenTiedonsiirtoTiedot))
      )
    })
  }

  private def toHenkilönTiedonsiirrot(tiedonsiirrot: Seq[TiedonsiirtoDocument]): List[HenkilönTiedonsiirrot] = {
    tiedonsiirrot.map { row =>
      val rivi = TiedonsiirtoRivi(
        row.id,
        row.aikaleima,
        row.oppija,
        row.oppilaitokset.getOrElse(Nil),
        row.suoritustiedot.getOrElse(Nil),
        row.virheet,
        row.data,
        row.lähdejärjestelmä
      )
      HenkilönTiedonsiirrot(row.oppija, List(rivi))
    }.toList
  }
}

case class Tiedonsiirrot(henkilöt: List[HenkilönTiedonsiirrot], oppilaitos: Option[OidOrganisaatio])
case class HenkilönTiedonsiirrot(oppija: Option[TiedonsiirtoOppija], rivit: Seq[TiedonsiirtoRivi])
case class TiedonsiirtoRivi(id: String,
                            aika: Timestamp,
                            oppija: Option[TiedonsiirtoOppija],
                            oppilaitos: List[OidOrganisaatio],
                            suoritustiedot: List[TiedonsiirtoSuoritusTiedot],
                            virhe: List[ErrorDetail],
                            inputData: Option[JValue],
                            lähdejärjestelmä: Option[String])
case class TiedonsiirtoOppija(oid: Option[String],
                              hetu: Option[String],
                              syntymäaika: Option[LocalDate],
                              etunimet: Option[String],
                              kutsumanimi: Option[String],
                              sukunimi: Option[String])
case class HetuTaiOid(oid: Option[String], hetu: Option[String])
case class TiedonsiirtoYhteenveto(tallentajaOrganisaatio: OidOrganisaatio,
                                  oppilaitos: OidOrganisaatio,
                                  käyttäjä: TiedonsiirtoKäyttäjä,
                                  viimeisin: Timestamp,
                                  siirretyt: Int,
                                  virheelliset: Int,
                                  onnistuneet: Int,
                                  lähdejärjestelmä: Option[Koodistokoodiviite])
case class TiedonsiirtoQuery(oppilaitos: Option[String],
                             paginationSettings: Option[PaginationSettings])
case class TiedonsiirtoKäyttäjä(oid: String, käyttäjätunnus: Option[String])
case class TiedonsiirtoError(data: JValue, virheet: List[ErrorDetail])

case class TiedonsiirtoDocument(tallentajaKäyttäjäOid: String,
                                tallentajaKäyttäjätunnus: Option[String],
                                tallentajaOrganisaatioOid: String,
                                oppija: Option[TiedonsiirtoOppija],
                                oppilaitokset: Option[List[OidOrganisaatio]],
                                koulutusmuoto: Option[String],
                                suoritustiedot: Option[List[TiedonsiirtoSuoritusTiedot]],
                                data: Option[JValue],
                                success: Boolean,
                                virheet: List[ErrorDetail],
                                lähdejärjestelmä: Option[String],
                                aikaleima: Timestamp) {
  def id: String = tallentajaOrganisaatioOid + "_" + oppijaId
  private def oppijaId: String = oppija.flatMap(h => h.hetu.orElse(h.oid)).getOrElse("")
}

case class TiedonsiirtoSuoritusTiedot(
  koulutusmoduuli: KoulutusmoduulinTiedonsiirtoTiedot,
  osaamisalat: Option[List[OsaamisalanTiedonsiirtoTiedot]],
  tutkintonimike: Option[List[TutkintonimikkeenTiedonsiirtoTiedot]]
)

case class KoulutusmoduulinTiedonsiirtoTiedot(nimi: Option[LocalizedString])
case class OsaamisalanTiedonsiirtoTiedot(nimi: Option[LocalizedString])
case class TutkintonimikkeenTiedonsiirtoTiedot(nimi: Option[LocalizedString])
