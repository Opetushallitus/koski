package fi.oph.koski.suostumus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, PoistettuOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation, Logging}
import fi.oph.koski.schema.{Opiskeluoikeus, SuostumusPeruttavissaOpiskeluoikeudelta}
import slick.dbio
import slick.dbio.Effect.Write
import org.json4s._

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

case class SuostumuksenPeruutusService(protected val application: KoskiApplication) extends Logging with QueryMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  lazy val db = application.masterDatabase.db
  lazy val perustiedotIndexer = application.perustiedotIndexer
  lazy val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  lazy val henkilöRepository = application.henkilöRepository

  val eiLisättyjäRivejä = 0

  def listaaPerututSuostumukset() = {
    runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.sortBy(_.aikaleima.desc).result)
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
            runDbSync(DBIO.seq(
              opiskeluoikeudenPoistonQuery(oid),
              poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(oo, henkilö),
              opiskeluoikeudenHistorianPoistonQuery(opiskeluoikeudenId)
            ).transactionally)
            perustiedotIndexer.deleteByIds(List(opiskeluoikeudenId), true)
            AuditLog.log(KoskiAuditLogMessage(KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN, user, Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)))
            HttpStatus.ok
          case None =>
            KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, " +
              s"viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa tai opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  private def opiskeluoikeudenPoistonQuery(oid: String): dbio.DBIOAction[Int, NoStream, Write] = {
    KoskiTables.OpiskeluOikeudet.filter(_.oid === oid).map(_.updateableFields).update((JObject.apply(), 0, None, None, None, None, "", true, Date.valueOf(LocalDate.now()), None, List(), true))
  }

  private def opiskeluoikeudenHistorianPoistonQuery(id: Int): dbio.DBIOAction[Int, NoStream, Write] = {
    KoskiTables.OpiskeluoikeusHistoria.filter(_.opiskeluoikeusId === id).delete
  }

  private def poistettujenOpiskeluoikeuksienTauluunLisäämisenQuery(opiskeluoikeus: Opiskeluoikeus, oppija: LaajatOppijaHenkilöTiedot): dbio.DBIOAction[Int, NoStream, Write] = {
    val timestamp = Timestamp.from(Instant.now())

    KoskiTables.PoistetutOpiskeluoikeudet.insertOrUpdate(PoistettuOpiskeluoikeusRow(
      opiskeluoikeus.oid.get,
      oppija.oid,
      opiskeluoikeus.oppilaitos.map(_.nimi.map(_.get("fi"))).flatten,
      opiskeluoikeus.oppilaitos.map(_.oid),
      opiskeluoikeus.päättymispäivä.map(Date.valueOf),
      opiskeluoikeus.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      opiskeluoikeus.lähdejärjestelmänId.map(_.id).flatten,
      timestamp
    ))
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
