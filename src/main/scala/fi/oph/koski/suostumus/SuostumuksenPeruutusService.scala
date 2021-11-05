package fi.oph.koski.suostumus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, PoistettuOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.henkilo.{HenkilönTunnisteet}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Opiskeluoikeus, SuostumusPeruttavissaOpiskeluoikeudelta}

import java.sql.{Date, Timestamp}
import java.time.Instant

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
          suostumusPeruttavissa(_, henkilö)
        ).find (_.oid.getOrElse("") == oid) match {
          case Some(oo) =>
            val timestamp = Timestamp.from(Instant.now())
            val opiskeluoikeudenId = runDbSync(KoskiTables.OpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).head
            val lisäysResult = runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.insertOrUpdate(PoistettuOpiskeluoikeusRow(
              oid,
              oo.päättymispäivä.map(Date.valueOf),
              oo.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
              oo.lähdejärjestelmänId.map(_.id).flatten,
              timestamp
            )))
            lisäysResult match {
              case `eiLisättyjäRivejä` =>
                logger.error(s"Virhe lisättäessä opiskeluoikeutta $oid poistettujen opiskeluoikeuksien tauluun")
                KoskiErrorCategory.internalError.apply("Virhe lisättäessä opiskeluoikeutta poistettujen opiskeluoikeuksien tauluun")
              case _ =>
                opiskeluoikeusRepository.deleteOpiskeluoikeus(oid)(user) match {
                  case HttpStatus.ok =>
                    perustiedotIndexer.deleteByIds(List(opiskeluoikeudenId), true)
                    HttpStatus.ok
                  case _ =>
                    // Pyritään poistamaan äsken lisätty opiskeluoikeus poistettujen taulusta,
                    // jos opiskeluoikeutta ei voitu poistaa Kosken opiskeluoikeuksien taulusta.
                    logger.error(s"Virhe poistettaessa opiskeluoikeutta $oid")
                    runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.filter(_.oid === oid).delete)
                    KoskiErrorCategory.internalError.apply("Opiskeluoikeutta ei voitu poistaa")
                }
            }
          case None =>
            KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumisele(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako," +
              s"viranomainen on katsonut opiskeluoikeuden tietoja tai opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  def suostumusPeruttavissa(oo: Opiskeluoikeus, henkilö: HenkilönTunnisteet)(implicit user: KoskiSpecificSession) =
    suorituksetPerutettavaaTyyppiä(oo) && !suoritusjakoTehty(oo)

  def suorituksetPerutettavaaTyyppiä(oo: Opiskeluoikeus) = {
    val muitaPäätasonSuorituksiaKuinPeruttavissaOlevia = oo.suoritukset.exists {
      case _: SuostumusPeruttavissaOpiskeluoikeudelta => false
      case _ => true
    }
    !muitaPäätasonSuorituksiaKuinPeruttavissaOlevia
  }

  private def suoritusjakoTehty(oo: Opiskeluoikeus)(implicit user: KoskiSpecificSession) = {
    application.opiskeluoikeusRepository.suoritusjakoTehty(oo.oid.get)
  }

  // Kutsutaan vain fixtureita resetoitaessa
  def deleteAll() = {
    runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.delete)
  }
}
