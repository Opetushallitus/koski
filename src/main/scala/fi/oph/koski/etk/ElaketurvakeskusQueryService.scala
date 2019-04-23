package fi.oph.koski.etk

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import slick.jdbc.GetResult

class ElaketurvakeskusQueryService(val db: DB) extends KoskiDatabaseMethods {

  def ammatillisetPerustutkinnotAikajaksolta(alku: LocalDate, loppu: LocalDate): Seq[EtkTutkintotietoRow] = {
    val alkuDate = Date.valueOf(alku)
    val loppuDate = Date.valueOf(loppu)
    implicit val getResult = GetResult[EtkTutkintotietoRow](r => EtkTutkintotietoRow(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))
    runDbSync(ammatillisetPerustutkinnotAikajaksolta(alkuDate, loppuDate).as[EtkTutkintotietoRow])
  }

  private def ammatillisetPerustutkinnotAikajaksolta(alku: Date, loppu: Date) = {
    sql"""
    select
        oo.koulutusmuoto,
        oo.alkamispaiva,
        oo.paattymispaiva,
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
        and oo.sisaltyy_opiskeluoikeuteen_oid is null
        and ps.suorituksen_tyyppi = 'ammatillinentutkinto'
        and ps.vahvistus_paiva >= ${alku} and ps.vahvistus_paiva <= ${loppu}
        and (ps.koulutusmoduuli_koulutustyyppi in ('1', '4', '13', '26') or ps.koulutusmoduuli_koodisto = 'koulutus' and ps.koulutusmoduuli_koodiarvo in ('381101', '381108'))"""
  }
}

case class EtkTutkintotietoRow
(
  koulutusmuoto: String,
  alkamispaiva: Option[Date],
  paattymispaiva: Option[Date],
  hetu: Option[String],
  syntymäaika: Option[Date],
  sukunimi: String,
  etunimet: String,
  versionumero: Int,
  opiskeluoikeus_oid: String,
  oppija_oid: String
)
