package fi.oph.koski.tiedonsiirto

import java.sql.Timestamp
import java.time.LocalDateTime

import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json.toJValue
import fi.oph.koski.json.{GenericJsonFormats, Json, Json4sHttp4s, LocalDateTimeSerializer}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, KoskiUserInfo, KoskiUserRepository}
import fi.oph.koski.localization.{LocalizedString, LocalizedStringDeserializer}
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import fi.oph.koski.util._
import io.prometheus.client.Counter
import org.json4s.JsonAST.{JArray, JString}
import org.json4s.{JValue, _}


class TiedonsiirtoService(elasticSearch: ElasticSearch, mailer: TiedonsiirtoFailureMailer, organisaatioRepository: OrganisaatioRepository, henkilöRepository: HenkilöRepository, koodistoviitePalvelu: KoodistoViitePalvelu, userRepository: KoskiUserRepository) extends Logging with Timing {
  implicit val formats = GenericJsonFormats.genericFormats.preservingEmptyValues + LocalizedStringDeserializer + LocalDateTimeSerializer

  private val tiedonSiirtoVirheet = Counter.build().name("fi_oph_koski_tiedonsiirto_TiedonsiirtoService_virheet").help("Koski tiedonsiirto virheet").register()

  def deleteAll: Unit = {
    val doc = toJValue(Map("query" -> Map("match_all" -> Map())))

    val deleted = Http.runTask(elasticSearch.http
      .post(uri"/koski/tiedonsiirto/_delete_by_query", doc)(Json4sHttp4s.json4sEncoderOf[JValue]) {
        case (200, text, request) => (Json.parse(text) \ "deleted").extract[Int]
        case (status, text, request) if List(404, 409).contains(status) => 0
        case (status, text, request) => throw HttpStatusException(status, text, request)
      })

    logger.info(s"Tyhjennetty tiedonsiirrot ($deleted)")
  }

  def haeTiedonsiirrot(query: TiedonsiirtoQuery)(implicit koskiSession: KoskiSession): Either[HttpStatus, PaginatedResponse[Tiedonsiirrot]] = {
    haeTiedonsiirrot(filtersFrom(query), query.oppilaitos, query.paginationSettings)
  }

  def virheelliset(query: TiedonsiirtoQuery)(implicit koskiSession: KoskiSession): Either[HttpStatus, PaginatedResponse[Tiedonsiirrot]] = {
    haeTiedonsiirrot(Map("exists" -> Map("field" -> "virheet.key")) :: filtersFrom(query), query.oppilaitos, query.paginationSettings)
  }

  private def filtersFrom(query: TiedonsiirtoQuery)(implicit session: KoskiSession): List[Map[String, Any]] = {
    query.oppilaitos.toList.map(oppilaitos => Map("term" -> Map("oppilaitokset.oid" -> oppilaitos))) ++ tallentajaOrganisaatioFilter
  }

  private def tallentajaOrganisaatioFilter(implicit session: KoskiSession): List[Map[String, Any]] =
    if (session.hasGlobalReadAccess) {
      Nil
    } else {
      List(Map("terms" -> Map("tallentajaOrganisaatioOid" -> session.organisationOids(AccessType.read))))
    }


  private def haeTiedonsiirrot(filters: List[Map[String, Any]], oppilaitosOid: Option[String], paginationSettings: Option[PaginationSettings])(implicit koskiSession: KoskiSession): Either[HttpStatus, PaginatedResponse[Tiedonsiirrot]] = {
    AuditLog.log(AuditLogMessage(TIEDONSIIRTO_KATSOMINEN, koskiSession, Map(juuriOrganisaatio -> koskiSession.juuriOrganisaatio.map(_.oid).getOrElse("ei juuriorganisaatiota"))))

    val doc: Map[String, Any] = ElasticSearch.applyPagination(paginationSettings, Map(
      "query" -> ElasticSearch.allFilter(filters),
      "sort" -> List(Map("aikaleima" -> "desc"), Map("oppija.sukunimi.keyword" -> "asc"), Map("oppija.etunimet.keyword" -> "asc"))
    ))

    val rows: Seq[TiedonsiirtoDocument] = runSearch(doc)
      .map(response => (response \ "hits" \ "hits").extract[List[JValue]].map(j => (j \ "_source").extract[TiedonsiirtoDocument]))
      .getOrElse(Nil)

    val oppilaitosResult: Either[HttpStatus, Option[Oppilaitos]] = oppilaitosOid match {
      case Some(oppilaitosOid) =>
        val oppilaitos: Option[Oppilaitos] = organisaatioRepository.getOrganisaatioHierarkia(oppilaitosOid).flatMap(_.toOppilaitos)
        oppilaitos match {
          case Some(oppilaitos) => Right(Some(oppilaitos))
          case None => Left(KoskiErrorCategory.notFound.oppilaitostaEiLöydy(s"Oppilaitosta $oppilaitosOid ei löydy"))
        }
      case None =>
        Right(None)
    }

    oppilaitosResult.right.map { oppilaitos =>
      val converted: Tiedonsiirrot = Tiedonsiirrot(toHenkilönTiedonsiirrot(rows), oppilaitos = oppilaitos.map(_.toOidOrganisaatio))
      PaginatedResponse(paginationSettings, converted, rows.length)
    }
  }

  private def runSearch[T <: AnyRef](doc: T)(implicit mf: Manifest[T]) = {
    try {
      val response = Http.runTask(elasticSearch.http.post(uri"/koski/tiedonsiirto/_search", doc)(Json4sHttp4s.json4sEncoderOf[T])(Http.parseJson[JValue]))
      Some(response)
    } catch {
      case e: HttpStatusException if e.status == 400 =>
        logger.warn(e.getMessage)
        None
    }
  }

  def storeTiedonsiirtoResult(implicit koskiSession: KoskiSession, oppijaOid: Option[OidHenkilö], validatedOppija: Option[Oppija], data: Option[JValue], error: Option[TiedonsiirtoError]) {
    if (!koskiSession.isPalvelukäyttäjä && !koskiSession.isRoot) {
      return
    }

    val henkilö = data.flatMap(extractHenkilö(_, oppijaOid))
    val lahdejarjestelma: Option[String] = data.flatMap(extractLahdejarjestelma)
    val oppilaitokset: Option[List[OidOrganisaatio]] = data.map(_ \ "opiskeluoikeudet" \ "oppilaitos" \ "oid").map(jsonStringList).map(_.flatMap(organisaatioRepository.getOrganisaatio).map(_.toOidOrganisaatio))
    val koulutustoimija: Option[OidOrganisaatio] = validatedOppija.flatMap(_.opiskeluoikeudet.headOption.flatMap(_.koulutustoimija.map(_.toOidOrganisaatio)))

    val juuriOrganisaatio = if (koskiSession.isRoot) koulutustoimija else koskiSession.juuriOrganisaatio

    juuriOrganisaatio.foreach((org: OrganisaatioWithOid) => {
      val (data: Option[JValue], virheet: Option[List[ErrorDetail]]) = error.map(e => (Some(e.data), Some(e.virheet))).getOrElse((None, None))

      storeToElasticSearch(henkilö, org, oppilaitokset, data, virheet, lahdejarjestelma, koskiSession.oid, new Timestamp(System.currentTimeMillis))

      if (error.isDefined) {
        tiedonSiirtoVirheet.inc
        mailer.sendMail(org.oid)
      }
    })
  }


  def storeToElasticSearch(henkilö: Option[JValue] /*TODO: why not tiedonsiirtooppija*/, org: OrganisaatioWithOid,
                                   oppilaitokset: Option[List[OidOrganisaatio]], data: Option[JValue],
                                   virheet: Option[List[ErrorDetail]], lahdejarjestelma: Option[String],
                                    userOid: String, aikaleima: Timestamp) = {
    val idValue: String = henkilö.flatMap { henkilö =>
      val hetuTaiOid = henkilö.extract[HetuTaiOid]
      hetuTaiOid.hetu.orElse(hetuTaiOid.oid)
    }.getOrElse("")

    val document = TiedonsiirtoDocument(userOid, org.oid, henkilö.map(_.extract[TiedonsiirtoOppija]), oppilaitokset, data, virheet.toList.flatten.isEmpty, virheet.getOrElse(Nil), lahdejarjestelma, aikaleima)

    val documentId = org.oid + "_" + idValue
    val json = Map("doc_as_upsert" -> true, "doc" -> document)

    val response = Http.runTask(elasticSearch.http.post(uri"/koski/tiedonsiirto/${documentId}/_update", json)(Json4sHttp4s.json4sEncoderOf[Map[String, Any]])(Http.parseJson[JValue]))

    val result = (response \ "result").extract[String]
    if (!(List("created", "updated", "noop").contains(result))) {
      val msg = s"Elasticsearch indexing failed: ${Json.writePretty(response)}"
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = (response \ "items").extract[List[JValue]].map(_ \ "update" \ "_shards" \ "successful").map(_.extract[Int])
      Right(itemResults.sum)
    }
  }

  def yhteenveto(implicit koskiSession: KoskiSession, sorting: SortOrder): Seq[TiedonsiirtoYhteenveto] = {
    import fi.oph.koski.date.DateOrdering._
    var ordering = sorting.field match {
      case "aika" => Ordering.by{x: TiedonsiirtoYhteenveto => x.viimeisin}
      case "oppilaitos" => Ordering.by{x: TiedonsiirtoYhteenveto => x.oppilaitos.description.get(koskiSession.lang)}
    }
    if (sorting.descending) ordering = ordering.reverse

    val query = Json.parse("""{
                  |  "size": 0,
                  |  "aggs": {
                  |  	"organisaatio": {
                  |		  "terms": { "field": "tallentajaOrganisaatioOid.keyword", "size" : 20000 },
                  |		  "aggs": {
                  |			  "oppilaitos": {
                  |				  "terms": { "field": "oppilaitokset.oid.keyword", "size" : 20000 },
                  | 				"aggs": {
                  |	  				"käyttäjä": {
                  |		  				"terms": { "field": "tallentajaKäyttäjäOid.keyword", "size" : 20000 },
                  |             "aggs": {
                  |               "lähdejärjestelmä": {
                  |                 "terms": { "field": "lähdejärjestelmä.keyword", "size" : 20000, "missing": "-" },
                  |   				  		"aggs": {
                  |                   "viimeisin" : { "max" : { "field" : "aikaleima" } },
                  |				    		  	"fail": {
                  |						    	  	"filter": { "term": { "success": false }}}
                  |		  					    }
                  |			  		    	}
                  |               }
                  |             }
                  |				  	}
                  |				  }
                  |		  	}
                  |		  }
                  |  	}
                  |  }
                  |}""".stripMargin)

    runSearch(query).map { response =>
      for {
        orgResults <- (response \ "aggregations" \ "organisaatio" \ "buckets").extract[List[JValue]]
        tallentajaOrganisaatio = getOrganisaatio((orgResults \ "key").extract[String])
        oppilaitosResults <- (orgResults \ "oppilaitos" \ "buckets").extract[List[JValue]]
        oppilaitos = getOrganisaatio((oppilaitosResults \ "key").extract[String])
        userResults <- (oppilaitosResults \ "käyttäjä" \ "buckets").extract[List[JValue]]
        userOid = (userResults \ "key").extract[String]
        käyttäjä = userRepository.findByOid(userOid) getOrElse {
          logger.warn(s"Käyttäjää ${userOid} ei löydy henkilöpalvelusta")
          KoskiUserInfo(userOid, None, None)
        }
        lähdejärjestelmäResults <- (userResults \ "lähdejärjestelmä" \ "buckets").extract[List[JValue]]
        lähdejärjestelmäId = (lähdejärjestelmäResults \ "key").extract[String]
        lähdejärjestelmä = koodistoviitePalvelu.getKoodistoKoodiViite("lahdejarjestelma", lähdejärjestelmäId)
        siirretyt = (lähdejärjestelmäResults \ "doc_count").extract[Int]
        epäonnistuneet = (lähdejärjestelmäResults \ "fail" \ "doc_count").extract[Int]
        onnistuneet = siirretyt - epäonnistuneet
        viimeisin = new Timestamp((lähdejärjestelmäResults \ "viimeisin" \ "value").extract[Long]).toLocalDateTime
      } yield {
        TiedonsiirtoYhteenveto(tallentajaOrganisaatio, oppilaitos, käyttäjä, viimeisin, siirretyt, epäonnistuneet, onnistuneet, lähdejärjestelmä)
      }
    }.getOrElse(Nil).sorted(ordering)
  }

  private def getOrganisaatio(oid: String) = organisaatioRepository.getOrganisaatio(oid).map(_.toOidOrganisaatio).getOrElse(OidOrganisaatio(oid, Some(LocalizedString.unlocalized(oid))))

  private def jsonStringList(value: JValue) = value match {
    case JArray(xs) => xs.collect { case JString(x) => x }
    case JString(x) => List(x)
    case JNothing => Nil
    case JNull => Nil
  }

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

  private def extractHenkilö(data: JValue, oidHenkilö: Option[OidHenkilö])(implicit user: KoskiSession): Option[JValue] = {
    val annetutHenkilötiedot: JValue = data \ "henkilö"
    val annettuTunniste: HetuTaiOid = Json.fromJValue[HetuTaiOid](annetutHenkilötiedot)
    val oid: Option[String] = oidHenkilö.map(_.oid).orElse(annettuTunniste.oid)
    val haetutTiedot: Option[HenkilötiedotJaOid] = (oid, annettuTunniste.hetu) match {
      case (Some(oid), None) => henkilöRepository.findByOid(oid).map(_.toHenkilötiedotJaOid)
      case (None, Some(hetu)) => henkilöRepository.findOppijat(hetu).headOption
      case _ => None
    }
    haetutTiedot.map(toJValue).orElse(oidHenkilö match {
      case Some(oidHenkilö) => Some(annetutHenkilötiedot.merge(toJValue(oidHenkilö)))
      case None => annetutHenkilötiedot.toOption
    })
  }

  private def toHenkilönTiedonsiirrot(tiedonsiirrot: Seq[TiedonsiirtoDocument]): List[HenkilönTiedonsiirrot] = {
    tiedonsiirrot.map { row =>
      val rivi = TiedonsiirtoRivi(Math.random().toInt /*TODO tarvitaanko id?*/, row.aikaleima.toLocalDateTime, row.oppija, row.oppilaitokset.getOrElse(Nil), row.virheet, row.data, row.lähdejärjestelmä)
      HenkilönTiedonsiirrot(row.oppija, List(rivi))
    }.toList
  }

  lazy val init = {
    setupIndex
  }

  def setupIndex = {
    val mappings: Map[String, Any] = Map("properties" -> Map(
      "virheet" -> Map(
        "properties" -> Map(
          "key" -> Map(
            "type" -> "text"
          )
        ),
        "dynamic" -> false
      ),
      "data" -> Map(
        "properties" -> Map(
        ),
        "dynamic" -> false
      )
    ))
    Http.runTask(elasticSearch.http.put(uri"/koski-index/_mapping/tiedonsiirto", Json.toJValue(mappings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
  }

}

case class Tiedonsiirrot(henkilöt: List[HenkilönTiedonsiirrot], oppilaitos: Option[OidOrganisaatio])
case class HenkilönTiedonsiirrot(oppija: Option[TiedonsiirtoOppija], rivit: Seq[TiedonsiirtoRivi])
case class TiedonsiirtoRivi(id: Int, aika: LocalDateTime, oppija: Option[TiedonsiirtoOppija], oppilaitos: List[OidOrganisaatio], virhe: List[ErrorDetail], inputData: Option[AnyRef], lähdejärjestelmä: Option[String])
case class TiedonsiirtoOppija(oid: Option[String], hetu: Option[String], etunimet: Option[String], kutsumanimi: Option[String], sukunimi: Option[String], äidinkieli: Option[Koodistokoodiviite])
case class HetuTaiOid(oid: Option[String], hetu: Option[String])
case class TiedonsiirtoYhteenveto(tallentajaOrganisaatio: OidOrganisaatio, oppilaitos: OidOrganisaatio, käyttäjä: KoskiUserInfo, viimeisin: LocalDateTime, siirretyt: Int, virheelliset: Int, onnistuneet: Int, lähdejärjestelmä: Option[Koodistokoodiviite])
case class TiedonsiirtoQuery(oppilaitos: Option[String], paginationSettings: Option[PaginationSettings])
case class TiedonsiirtoKäyttäjä(oid: String, nimi: Option[String])
case class TiedonsiirtoError(data: JValue, virheet: List[ErrorDetail])

case class TiedonsiirtoDocument(tallentajaKäyttäjäOid: String, tallentajaOrganisaatioOid: String, oppija: Option[TiedonsiirtoOppija], oppilaitokset: Option[List[OidOrganisaatio]], data: Option[JValue], success: Boolean, virheet: List[ErrorDetail], lähdejärjestelmä: Option[String], aikaleima: Timestamp)