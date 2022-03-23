package fi.oph.koski.suostumus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, PoistettuOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusPoistoUtils
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema.{Opiskeluoikeus, SuostumusPeruttavissaOpiskeluoikeudelta}
import slick.jdbc.GetResult

case class SuostumuksenPeruutusService(protected val application: KoskiApplication) extends Logging with QueryMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  lazy val db = application.masterDatabase.db
  lazy val perustiedotIndexer = application.perustiedotIndexer
  lazy val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  lazy val henkilöRepository = application.henkilöRepository

  val eiLisättyjäRivejä = 0

  def listaaPerututSuostumukset() = {
    implicit val getResult: GetResult[PoistettuOpiskeluoikeusRow] = {
      GetResult[PoistettuOpiskeluoikeusRow](r =>
        PoistettuOpiskeluoikeusRow(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<)
      )
    }

    runDbSync(
      sql"""
           select oid, oppija_oid, oppilaitos_nimi, oppilaitos_oid, paattymispaiva, lahdejarjestelma_koodi, lahdejarjestelma_id, mitatoity_aikaleima, suostumus_peruttu_aikaleima
           from poistettu_opiskeluoikeus
           order by coalesce(mitatoity_aikaleima, suostumus_peruttu_aikaleima);
         """.as[PoistettuOpiskeluoikeusRow]
    )
  }

  def peruutaSuostumus(oid: String)(implicit user: KoskiSpecificSession): HttpStatus = {
    henkilöRepository.findByOid(user.oid) match {
      case Some(henkilö) =>
        val opiskeluoikeudet = opiskeluoikeusRepository.findByCurrentUser(henkilö)(user).get
        opiskeluoikeudet.filter(
          suostumusPeruttavissa(_)
        ).find (_.oid.contains(oid)) match {
          case Some(oo) =>
            val opiskeluoikeudenId = runDbSync(KoskiTables.OpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).head
            runDbSync(
              DBIO.seq(
                OpiskeluoikeusPoistoUtils.poistaOpiskeluOikeus(opiskeluoikeudenId, oid, oo, oo.versionumero.map(v => v + 1).getOrElse(VERSIO_1), henkilö.oid, false),
                application.perustiedotSyncRepository.addDeleteToSyncQueue(opiskeluoikeudenId)
              )
            )
            AuditLog.log(KoskiAuditLogMessage(KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN, user, Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)))
            HttpStatus.ok
          case None =>
            KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, " +
              s"viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa tai opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  def suoritusjakoTekemättäWithAccessCheck(oid: String)(implicit user: KoskiSpecificSession): HttpStatus = {
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.KANSALAINEN_SUORITUSJAKO_TEKEMÄTTÄ_KATSOMINEN, user, Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)))
    henkilöRepository.findByOid(user.oid) match {
      case Some(henkilö) =>
        opiskeluoikeusRepository.findByCurrentUser(henkilö)(user).get.exists(oo =>
          oo.oid.get == oid && !suoritusjakoTehty(oo)) match {
          case true => HttpStatus.ok
          case false => KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Suorituksesta on tehty suoritusjako.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  private def suostumusPeruttavissa(oo: Opiskeluoikeus)(implicit user: KoskiSpecificSession) =
    suorituksetPeruutettavaaTyyppiä(oo) && !suoritusjakoTehty(oo)

  def suorituksetPeruutettavaaTyyppiä(oo: Opiskeluoikeus) = {
    val muitaPäätasonSuorituksiaKuinPeruttavissaOlevia = oo.suoritukset.exists {
      case _: SuostumusPeruttavissaOpiskeluoikeudelta => false
      case _ => true
    }
    !muitaPäätasonSuorituksiaKuinPeruttavissaOlevia
  }

  private def suoritusjakoTehty(oo: Opiskeluoikeus) = {
    opiskeluoikeusRepository.suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oo.oid.get)
  }

  // Kutsutaan vain fixtureita resetoitaessa
  def deleteAll() = {
    if (application.config.getString("opintopolku.virkailija.url") == "mock") {
      runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.delete)
    } else {
      throw new RuntimeException("Peruutettujen suostumusten taulua ei voi tyhjentää tuotantotilassa")
    }
  }
}
