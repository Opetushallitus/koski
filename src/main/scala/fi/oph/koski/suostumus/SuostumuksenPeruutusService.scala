package fi.oph.koski.suostumus

import fi.oph.koski.config.{AppConfig, Environment, KoskiApplication}
import fi.oph.koski.db.{KoskiTables, PoistettuOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusPoistoUtils
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema.{Opiskeluoikeus, SuostumusPeruttavissaOpiskeluoikeudelta}
import slick.jdbc.GetResult
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot
import fi.oph.koski.suoritusjako.SuoritusIdentifier

case class SuostumuksenPeruutusService(protected val application: KoskiApplication) extends Logging with QueryMethods {

  lazy val db = application.masterDatabase.db
  lazy val perustiedotIndexer = application.perustiedotIndexer
  lazy val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  lazy val henkilöRepository = application.henkilöRepository
  lazy val suoritusjakoRepository = application.suoritusjakoRepository

  val eiLisättyjäRivejä = 0

  def listaaPerututSuostumukset() = {
    implicit val getResult: GetResult[PoistettuOpiskeluoikeusRow] = {
      GetResult[PoistettuOpiskeluoikeusRow](r =>
        PoistettuOpiskeluoikeusRow(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.nextArray[String]().toList,r.<<)
      )
    }

    runDbSync(
      sql"""
           select oid, oppija_oid, oppilaitos_nimi, oppilaitos_oid, paattymispaiva, lahdejarjestelma_koodi, lahdejarjestelma_id, mitatoity_aikaleima, suostumus_peruttu_aikaleima, koulutusmuoto, suoritustyypit, versio
           from poistettu_opiskeluoikeus
           order by coalesce(mitatoity_aikaleima, suostumus_peruttu_aikaleima) desc;
         """.as[PoistettuOpiskeluoikeusRow]
    )
  }

  def etsiPoistetut(oids: Seq[String]): Seq[PoistettuOpiskeluoikeusRow] =
    runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.filter(r => r.oid inSet oids).result)

  def peruutaSuostumus(
    oid: String,
    suorituksenTyyppi: Option[String]
  )(implicit user: KoskiSpecificSession): HttpStatus = {
    henkilöRepository.findByOid(user.oid) match {
      case Some(henkilö) =>
        val opiskeluoikeudet = opiskeluoikeusRepository.findByCurrentUser(henkilö)(user).get
        val opiskeluoikeus = opiskeluoikeudet.filter(oo =>
          suostumusPeruttavissa(oo, henkilö.oid, suorituksenTyyppi)
        ).find (_.oid.contains(oid))

        (opiskeluoikeus, suorituksenTyyppi) match {
          // Jos enemmän kuin yksi suoritus ja suorituksen tyyppi annettu, peru suostumus suoritukselta
          case (Some(oo), Some(tyyppi)) if oo.suoritukset.map(_.tyyppi.koodiarvo).contains(tyyppi) && oo.suoritukset.size > 1 =>
            peruutaSuostumusSuoritukselta(oid, user, henkilö, oo, tyyppi)
          // Jos täsmälleen yksi suoritus ja suorituksen tyyppi annettu, peru suostumus opiskeluoikeudelta
          case (Some(oo), Some(tyyppi)) if oo.suoritukset.map(_.tyyppi.koodiarvo).contains(tyyppi) => peruutaSuostumusOpiskeluoikeudelta(oid, user, henkilö, oo)
          // Jos suorituksen tyyppiä ei annettu, peru suostumus opiskeluoikeudelta
          case (Some(oo), None) => peruutaSuostumusOpiskeluoikeudelta(oid, user, henkilö, oo)
          case (_, _) =>
            KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, " +
              s"viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua " +
              s"tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  private def peruutaSuostumusSuoritukselta(
    oid: String,
    user: KoskiSpecificSession,
    henkilö: LaajatOppijaHenkilöTiedot,
    oo: Opiskeluoikeus,
    tyyppi: String
  ): HttpStatus = {
    val opiskeluoikeudenId = runDbSync(KoskiTables.KoskiOpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).head
    val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(
      opiskeluoikeudenId,
      oo.withSuoritukset(oo.suoritukset.filter(_.tyyppi.koodiarvo != tyyppi)),
      application.henkilöRepository.opintopolku.withMasterInfo(henkilö),
    )
    val aiemminPoistettuRivi = runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.filter(_.oid === oid).result).headOption
    runDbSync(
      DBIO.seq(
        OpiskeluoikeusPoistoUtils
          .poistaPäätasonSuoritus(
            opiskeluoikeusOid = oid,
            opiskeluoikeusId = opiskeluoikeudenId,
            oo = oo,
            suorituksenTyyppi = tyyppi,
            versionumero = oo.versionumero.map(v => v + 1).getOrElse(VERSIO_1),
            oppijaOid = henkilö.oid,
            mitätöity = false,
            aiemminPoistettuRivi = aiemminPoistettuRivi,
            application.historyRepository
          ),
        application.perustiedotSyncRepository.addToSyncQueue(perustiedot, true)
      )
    )
    teeLogimerkintäSähköpostinotifikaatiotaVarten(oid)
    AuditLog.log(
      KoskiAuditLogMessage(
        KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN,
        user,
        Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid, KoskiAuditLogMessageField.suorituksenTyyppi -> tyyppi)
      )
    )
    HttpStatus.ok
  }

  private def peruutaSuostumusOpiskeluoikeudelta(
    oid: String,
    user: KoskiSpecificSession,
    henkilö: LaajatOppijaHenkilöTiedot,
    oo: Opiskeluoikeus
  ): HttpStatus = {
    val opiskeluoikeudenId = runDbSync(KoskiTables.KoskiOpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).head
    val aiemminPoistettuRivi = runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.filter(_.oid === oid).result).headOption
    runDbSync(
      DBIO.seq(
        OpiskeluoikeusPoistoUtils
          .poistaOpiskeluOikeus(
            id = opiskeluoikeudenId,
            oid = oid,
            oo = oo,
            versionumero = oo.versionumero.map(v => v + 1).getOrElse(VERSIO_1),
            oppijaOid = henkilö.oid,
            mitätöity = false,
            aiemminPoistettuRivi = aiemminPoistettuRivi
          ),
        application.perustiedotSyncRepository.addDeleteToSyncQueue(opiskeluoikeudenId)
      )
    )
    teeLogimerkintäSähköpostinotifikaatiotaVarten(oid)
    AuditLog.log(
      KoskiAuditLogMessage(
        KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN,
        user,
        Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)
      )
    )
    HttpStatus.ok
  }

  def teeTestimerkintäSähköpostinotifikaatiotaVarten(): Unit = {
    teeLogimerkintäSähköpostinotifikaatiotaVarten("[TÄMÄ ON TESTIVIESTI]")
  }

  private def teeLogimerkintäSähköpostinotifikaatiotaVarten(oid: String): Unit = {
    logger.warn(s"Kansalainen perui suostumuksen. Opiskeluoikeus ${oid}. Ks. tarkemmat tiedot ${AppConfig.virkailijaOpintopolkuUrl(application.config).getOrElse("mock")}/koski/api/opiskeluoikeus/suostumuksenperuutus")
  }

  def suoritusjakoTekemättäWithAccessCheck(
    oid: String,
    suorituksenTyyppi: Option[String]
  )(implicit user: KoskiSpecificSession): HttpStatus = {
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.KANSALAINEN_SUORITUSJAKO_TEKEMÄTTÄ_KATSOMINEN, user, Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)))
    henkilöRepository.findByOid(user.oid) match {
      case Some(henkilö) =>
        opiskeluoikeusRepository.findByCurrentUser(henkilö)(user).get.exists(oo =>
          oo.oid.contains(oid) &&
            !suorituksenTyyppi.map(suoritusjakoTehty(oo, henkilö.oid, _)).getOrElse(suoritusjakoTehty(oo))) match {
          case true => HttpStatus.ok
          case false => KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Suorituksesta on tehty suoritusjako.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  private def suostumusPeruttavissa(
    oo: Opiskeluoikeus,
    oppijaOid: String,
    suorituksenTyyppi: Option[String]
  )(implicit user: KoskiSpecificSession): Boolean = suorituksenTyyppi match {
    case Some(tyyppi) => suorituksetPeruutettavaaTyyppiä(oo) && !suoritusjakoTehty(oo, oppijaOid, tyyppi)
    case None => suorituksetPeruutettavaaTyyppiä(oo) && !suoritusjakoTehty(oo)
  }

  def suorituksetPeruutettavaaTyyppiä(oo: Opiskeluoikeus): Boolean = {
    val muitaPäätasonSuorituksiaKuinPeruttavissaOlevia = oo.suoritukset.exists {
      case _: SuostumusPeruttavissaOpiskeluoikeudelta => false
      case _ => true
    }
    !muitaPäätasonSuorituksiaKuinPeruttavissaOlevia
  }

  private def suoritusjakoTehty(oo: Opiskeluoikeus): Boolean = {
    opiskeluoikeusRepository.suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oo.oid.get)
  }

  private def suoritusjakoTehty(
    oo: Opiskeluoikeus,
    oppijaOid: String,
    suorituksenTyyppi: String
  ): Boolean = {
    opiskeluoikeusRepository.suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oo.oid.get) &&
      suoritusjakoRepository
        .getAll(oppijaOid)
        .flatMap(sj => JsonSerializer.extract[List[SuoritusIdentifier]](sj.suoritusIds))
        .exists(suoritusId => suoritusIdMatches(suoritusId, oo, suorituksenTyyppi))
  }

  private def suoritusIdMatches(
    suoritusId: SuoritusIdentifier,
    oo: Opiskeluoikeus,
    suorituksenTyyppi: String
  ): Boolean = {
    val lähdejärjestelmäMatch = suoritusId.lähdejärjestelmänId == oo.lähdejärjestelmänId.flatMap(_.id)
    val opiskeluOidMatch = suoritusId.opiskeluoikeusOid.isEmpty || suoritusId.opiskeluoikeusOid == oo.oid
    val oppilaitosMatch = suoritusId.oppilaitosOid == oo.oppilaitos.map(_.oid)
    val suoritusMatch = suoritusId.suorituksenTyyppi == suorituksenTyyppi
    val koulutusModuulinTunnisteMatch = oo.suoritukset.exists(s =>
      suoritusId.suorituksenTyyppi == s.tyyppi.koodiarvo &&
        suoritusId.koulutusmoduulinTunniste == s.koulutusmoduuli.tunniste.koodiarvo
    )

    lähdejärjestelmäMatch && opiskeluOidMatch && oppilaitosMatch && suoritusMatch && koulutusModuulinTunnisteMatch
  }

  // Kutsutaan vain fixtureita resetoitaessa
  def deleteAll() = {
    if (Environment.isMockEnvironment(application.config)) {
      runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.delete)
    } else {
      throw new RuntimeException("Peruutettujen suostumusten taulua ei voi tyhjentää tuotantotilassa")
    }
  }
}
