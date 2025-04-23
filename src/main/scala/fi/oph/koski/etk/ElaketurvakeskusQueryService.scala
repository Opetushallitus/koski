package fi.oph.koski.etk

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.DB
import slick.jdbc.GetResult

import scala.concurrent.duration.DurationInt

class ElaketurvakeskusQueryService(val db: DB) extends QueryMethods {

  def ammatillisetPerustutkinnot(request: TutkintotietoRequest): EtkResponse = {
    val queryResult = ammatillisetPerustutkinnotAikajaksolta(request.alku, request.loppu)
    val kuoriopiskeluoikeuksienOidit = queryResult.flatMap(_.sisältyyOpiskeluoikeuteen).distinct
    val tutkintotiedot = queryResult
      .filterNot(row => kuoriopiskeluoikeuksienOidit.contains(row.opiskeluoikeus_oid))
      .map(_.toTutkintotieto).toList

    EtkResponse(
      vuosi = request.vuosi,
      aikaleima = Timestamp.from(Instant.now),
      tutkintojenLkm = tutkintotiedot.size,
      tutkinnot = tutkintotiedot
    )
  }

  private def ammatillisetPerustutkinnotAikajaksolta(alku: LocalDate, loppu: LocalDate): Seq[EtkTutkintotietoRow] = {
    val alkuDate = Date.valueOf(alku)
    val loppuDate = Date.valueOf(loppu)
    implicit val getResult = GetResult[EtkTutkintotietoRow](r => EtkTutkintotietoRow(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))
    runDbSync(ammatillisetPerustutkinnotAikajaksolta(alkuDate, loppuDate).as[EtkTutkintotietoRow], timeout = 10.minutes)
  }

  private def ammatillisetPerustutkinnotAikajaksolta(alku: Date, loppu: Date) = {
    sql"""
    select
        oo.koulutusmuoto,
        oo.alkamispaiva,
        oo.paattymispaiva,
        oo.sisaltyy_opiskeluoikeuteen_oid,
        h.hetu,
        h.syntymaaika,
        h.sukunimi,
        h.etunimet,
        oo.versionumero,
        oo.opiskeluoikeus_oid,
        oo.oppija_oid
      from
        r_opiskeluoikeus oo
        join r_paatason_suoritus ps on (oo.opiskeluoikeus_oid = ps.opiskeluoikeus_oid)
        left join r_henkilo h on (h.oppija_oid = oo.oppija_oid)
      where
        oo.koulutusmuoto = 'ammatillinenkoulutus'
        and oo.viimeisin_tila = 'valmistunut'
        and ${alku} <= oo.paattymispaiva and oo.paattymispaiva <= ${loppu}
        and ps.suorituksen_tyyppi = 'ammatillinentutkinto'
        and (ps.koulutusmoduuli_koulutustyyppi in ('1', '4', '13', '26') or ps.koulutusmoduuli_koodisto = 'koulutus' and ps.koulutusmoduuli_koodiarvo in ('381101', '381108'))
        and (h.syntymaaika + interval '18 years') <= oo.paattymispaiva"""
  }
}

case class EtkTutkintotietoRow(
  koulutusmuoto: String,
  alkamispaiva: Option[Date],
  paattymispaiva: Option[Date],
  sisältyyOpiskeluoikeuteen: Option[String],
  hetu: Option[String],
  syntymäaika: Option[Date],
  sukunimi: String,
  etunimet: String,
  versionumero: Int,
  opiskeluoikeus_oid: String,
  oppija_oid: String
) {
  def toTutkintotieto: EtkTutkintotieto = EtkTutkintotieto(
      henkilö = EtkHenkilö(
        hetu = hetu,
        syntymäaika = syntymäaika.map(_.toLocalDate),
        sukunimi = sukunimi,
        etunimet = etunimet,
        sukupuoli = None,
      ),
      tutkinto = EtkTutkinto(
        tutkinnonTaso = Some(koulutusmuoto),
        alkamispäivä = alkamispaiva.map(_.toLocalDate),
        päättymispäivä = paattymispaiva.map(_.toLocalDate)
      ),
      viite = Some(EtkViite(
        opiskeluoikeusOid = Some(opiskeluoikeus_oid),
        opiskeluoikeusVersionumero = Some(versionumero),
        oppijaOid = Some(oppija_oid)
      ))
    )
}
