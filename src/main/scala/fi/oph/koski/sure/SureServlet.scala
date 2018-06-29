package fi.oph.koski.sure

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, SensitiveDataFilter}
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.OppijaOidHaku
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueries, OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.Timing
import org.json4s.JValue
import org.scalatra.ContentEncodingSupport

class SureServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache with Timing {

  /*
  before() {
    if (!koskiSession.isPalvelukäyttäjä || !koskiSession.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }
  }
  */

  // huom, nämä rajapinnat palauttavat vain Koskeen tallennettuja opiskeluoikeuksia (ei Virta tai YTR)
  // huom: ei palauta mitätöityjä opiskeluoikeuksia

  // Huom! Nämä rajapinnat palauttavat vain henkilön OIDin, ei muita henkilötietoja. Syytä poistaa nimi/hetu-kentät
  // suren KoskiHenkilo case classista.

  // Huom! Tämä rajapinta palauttaa vain muuttuneet opiskeluoikeudet, ei näiden oppijoiden kaikkia opiskeluoikeuksia.
  // Yleensä oppijan "nykyisen tilan" päättely vaatii tiedot kaikista hänen opiskeluoikeuksistaan (ei voi olettaa
  // että viimeksi päivitetty opiskeluoikeus on relevantein, ja vanhempia ei tarvitse huomioida - aikaleima
  // voi päivittyä myös tietoteknisistä syistä). Tämä rajapinta siis palauttaa eri tietoja kuin post("/oids") alla!

  // TODO: milloin Sure käyttää muuttuneet+pagination hakua ison datamassan hakemiseen uudelleen?
  // Kuinka isoista datamääristä puhutaan? (tuo kysely on raskas jos käydään koko kanta läpi)

  // FIXME: Olisiko mahdollista tehdä "muuttuneet oppijat" rajapinta? (joka palauttaisi samat tiedot kuin post("/oids"))
  // Jonkin verran raskaampi kysely se on, ainakin jos haetaan isoja datamassoja sivutuksella.
  /*
  select * from opiskeluoikeus
  inner join (select distinct oppija_oid from opiskeluoikeus where aikaleima ... ) AS muuttuneet_oppijat
  on opiskleluoikeus.oppija_oid = muuttuneet_oppijat.oppija_oid
  order by oppija_oid
  val MuuttuneetOppijat = OpiskeluOikeudet.filter(_.aikaleima).map(_.oppijaOid).distinct
  val MuuttuneidenOppijoidenOpiskeluoikeudet = (OpiskeluoikeudetWithAccessCheck.innerJoin(MuuttuneetOppijat) on ((l,r) => l.oppijaOid === r.oppijaOid)).map { case (l,r) => l }.sortBy(_.oppijaOid)
   */

  // FIXME: muuttunutEnnen/Jälkeen muuttuneet timestamp-tyyppisiksi (eikä LocalDateTime), jotta toimivat oikein DST-siirtymissä
  // Pitää muuttaa myös Sureen.

  get("/muuttuneet") {
    val RequiredParameters = Set("muuttunutEnnen", "muuttunutJälkeen")
    val limitedParams = params.filterKeys(RequiredParameters.contains)
    if (limitedParams.keys != RequiredParameters)
      haltWithStatus(KoskiErrorCategory.badRequest.queryParam.missing())
    OpiskeluoikeusQueryFilter.parse(limitedParams.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
      case Right(filters) =>
        val serialize = SensitiveDataFilter(koskiSession).rowSerializer
        val queryForAuditLog = params.toList.map { case (p,v) => p + "=" + v }.mkString("&")
        val observable = OpiskeluoikeusQueryContext(request)(koskiSession, application).queryWithoutHenkilötiedotRaw(
          filters, paginationSettings, OpiskeluoikeusQueryContext.queryForAuditLog(params)
        )
        streamResponse[JValue](observable.map(t => serialize(OidHenkilö(t._1), t._2)), koskiSession)
      case Left(status) =>
        haltWithStatus(status)
    }
  }

  // palauttaa annettujen oppija-oidien kaikki (Koskeen talletetut) opiskeluoikeudet.
  // mikäli jollekin OIDille ei löydy yhtään opiskeluoikeutta, tämä ei ole virhe, vaan ko. OID vain puuttuu vastauksesta
  post("/oids") {
    val MaxOids = 1000
    withJsonBody { parsedJson =>
      val oids = JsonSerializer.extract[List[String]](parsedJson)
      if (oids.size > MaxOids) {
        haltWithStatus(KoskiErrorCategory.badRequest.queryParam(s"Liian monta oidia, enintään ${MaxOids} sallittu."))
      }
      oids.map(HenkilöOid.validateHenkilöOid).collectFirst { case Left(status) => status } match {
        case None =>
          val serialize = SensitiveDataFilter(koskiSession).rowSerializer
          val observable = OpiskeluoikeusQueryContext(request)(koskiSession, application).queryWithoutHenkilötiedotRaw(
            List(OppijaOidHaku(oids)), None, "oids=" + oids.take(3).mkString(",") + ",..."
          )
          streamResponse[JValue](observable.map(t => serialize(OidHenkilö(t._1), t._2)), koskiSession)
        case Some(status) => haltWithStatus(status)
      }
    }()
  }

  // Tarvittaisiinko tällainen? Mikä on rajauskriteeri (minkä vuoden ysiluokkalaiset)?
  // Entä arvosanoja korottavat/kymppiluokkalaiset/ym?
  get("/ysiluokkalaiset") {

  }
}
