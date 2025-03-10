package fi.oph.koski.suoritusjako

import com.typesafe.config.Config
import fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotService
import fi.oph.koski.db.SuoritusjakoRow

import java.time.LocalDate
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_SUORITUSJAKO_LISAYS, KANSALAINEN_SUORITUSJAKO_LISAYS_AKTIIVISET_JA_PAATTYNEET_OPINNOT, KANSALAINEN_SUORITUSJAKO_LISAYS_SUORITETUT_TUTKINNOT}
import fi.oph.koski.log.KoskiAuditLogMessageField.oppijaHenkiloOid
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema._
import fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotService
import fi.oph.koski.util.ChainingSyntax.chainingOps
import fi.oph.koski.util.WithWarnings
import org.scalatra.servlet.RichRequest

import javax.servlet.http.HttpServletRequest


case class SuoritusjakoPayload(
  tyyppi: String
)
class SuoritusjakoService(
  suoritusjakoRepository: SuoritusjakoRepository,
  oppijaFacade: KoskiOppijaFacade,
  suoritetutTutkinnotService: SuoritetutTutkinnotService,
  aktiivisetJaPäättyneetOpinnotService: AktiivisetJaPäättyneetOpinnotService
) extends Logging {
  private def addSuoritusjako(oppijaOid: String, suoritusIds: List[SuoritusIdentifier], kokonaisuudet: List[SuoritusjakoPayload])(implicit koskiSession: KoskiSpecificSession) = {
    val secret = SuoritusjakoSecret.generateNew
    suoritusjakoRepository.create(secret, oppijaOid, suoritusIds, kokonaisuudet)
  }
  def putBySuoritusIds(oppijaOid: String, suoritusIds: List[SuoritusIdentifier])(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, Suoritusjako] = {
    getOpiskeluoikeudetSuoritusIdentifierinMukaan(oppijaOid, suoritusIds) match {
      case Left(status) => Left(status)
      case Right(opiskeluoikeudet) =>
        AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_LISAYS, koskiSession, Map(oppijaHenkiloOid -> oppijaOid)))
        val suoritusjako = addSuoritusjako(oppijaOid, suoritusIds, List())
        // Kaikkia opiskeluoikeuksia ei talleteta Koskeen, jolloin niillä ei välttämättä ole oidia.
        // Ei yritetä merkata sellaisille opiskeluoikeuksille suoritusjakoa tehdyksi.
        opiskeluoikeudet.map(_.oid match {
          case Some(oid) if suoritusjako.isRight => oppijaFacade.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(oid)
          case _ =>
        })
        suoritusjako
    }
  }

  def putByKokonaisuudet(oppijaOid: String, kokonaisuudet: List[SuoritusjakoPayload])(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, Suoritusjako] = {
    // Tässä toSet, koska bugin vuoksi sama tyyppi saattaa esiintyä listalla useammin kuin kerran
    kokonaisuudet.map(_.tyyppi).toSet.toList match {
      case List("suoritetut-tutkinnot") =>
        addSuoritusjako(oppijaOid, List(), kokonaisuudet)
          .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_LISAYS_SUORITETUT_TUTKINNOT, koskiSession, Map(oppijaHenkiloOid -> oppijaOid))))
      case List("aktiiviset-ja-paattyneet-opinnot") =>
        addSuoritusjako(oppijaOid, List(), kokonaisuudet)
          .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_LISAYS_AKTIIVISET_JA_PAATTYNEET_OPINNOT, koskiSession, Map(oppijaHenkiloOid -> oppijaOid))))
      case t =>
        Left(KoskiErrorCategory.internalError(s"Tuntematon suoritusjakotyyppi: ${t}"))
    }
  }

  def delete(oppijaOid: String, secret: String): HttpStatus = {
    suoritusjakoRepository.delete(oppijaOid, secret)
  }

  def update(oppijaOid: String, secret: String, expirationDate: LocalDate): HttpStatus = {
    if (expirationDate.isBefore(LocalDate.now) || expirationDate.isAfter(LocalDate.now.plusYears(1))) {
      KoskiErrorCategory.badRequest()
    } else {
      suoritusjakoRepository.update(oppijaOid, secret, expirationDate)
    }
  }

  // Sessio tarvitaan JSON serialisoinnille, jotta saadaan kenttiä piilotettua/näkyviin jakolinkin luomispäivän mukaan
  def getOppijaJakolinkilläAndSession(secret: String, request: RichRequest, config: Config): Either[HttpStatus, (WithWarnings[OppijaJakolinkillä], KoskiSpecificSession)] = {
    for {
      row <- suoritusjakoRepository.get(secret)
      session <- getSessionFromRow(row, config, request)
      result <- getOppijaJakolinkilläFromRow(row, session)
    } yield (result, session)
  }

  def getSessionFromRow(row: SuoritusjakoRow, config: com.typesafe.config.Config, request: RichRequest): Either[HttpStatus, KoskiSpecificSession] = {
    val erityishenkilötiedotJakolinkkeihinAlkaen = LocalDate.parse(config.getString("erityishenkilötiedotJakolinkkeihinAlkaen")).minusDays(1)
    if (row.aikaleima.toLocalDateTime.toLocalDate.isAfter(erityishenkilötiedotJakolinkkeihinAlkaen)) {
      Right(KoskiSpecificSession.suoritusjakoKatsominenErityisilläHenkilötiedoillaUser(request))
    } else {
      Right(KoskiSpecificSession.suoritusjakoKatsominenUser(request))
    }
  }

  def getOppijaJakolinkilläFromRow(row: SuoritusjakoRow, session: KoskiSpecificSession): Either[HttpStatus, WithWarnings[OppijaJakolinkillä]] = {
    if (row.jaonTyyppi.isDefined) {
      Left(KoskiErrorCategory.notFound())
    } else {
      oppijaFacade.findOppija(row.oppijaOid)(session).map(_.map { oppija =>
        val suoritusIdentifiers = JsonSerializer.extract[List[SuoritusIdentifier]](row.suoritusIds)
        val filtered = filterOpiskeluoikeudet(oppija.opiskeluoikeudet, suoritusIdentifiers)
        val oppijaResult = oppija.copy(opiskeluoikeudet = filtered)
        OppijaJakolinkillä(
          jakolinkki = Some(Jakolinkki(row.voimassaAsti.toLocalDate())),
          henkilö = oppijaResult.henkilö,
          opiskeluoikeudet = oppijaResult.opiskeluoikeudet
        )
      })
    }
  }

  def getAll(oppijaOid: String): Seq[Suoritusjako] = {
    suoritusjakoRepository.getAll(oppijaOid).map(jako =>
      Suoritusjako(jako.secret, jako.voimassaAsti.toLocalDate, jako.aikaleima, jaonTyyppi = jako.jaonTyyppi))
  }

  def getSuoritetutTutkinnot(secret: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SuoritetutTutkinnotOppijaJakolinkillä] = {
    suoritusjakoRepository.get(secret).flatMap(
      o =>
        (o.oppijaOid, o.jaonTyyppi) match {
          case (oppijaOid, Some("suoritetut-tutkinnot")) => {
            findSuoritetutTutkinnotOppija(oppijaOid)
              .map(_.copy(
                jakolinkki = Some(Jakolinkki(o.voimassaAsti.toLocalDate))
              ))
          }
          case _ => Left(KoskiErrorCategory.notFound())
      }
    )
  }

  def findSuoritetutTutkinnotOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, SuoritetutTutkinnotOppijaJakolinkillä] = {
    suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = true
    ).map(oppijaLaajatTiedot => {
      SuoritetutTutkinnotOppijaJakolinkillä(
        henkilö = SuoritusjakoHenkilö.fromOppijaHenkilö(oppijaLaajatTiedot.henkilö),
        opiskeluoikeudet = oppijaLaajatTiedot.opiskeluoikeudet
      )
    })
  }

  def getAktiivisetJaPäättyneetOpinnot(secret: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä] = {
    suoritusjakoRepository.get(secret).flatMap(
      o =>
        (o.oppijaOid, o.jaonTyyppi) match {
          case (oppijaOid, Some("aktiiviset-ja-paattyneet-opinnot")) =>
            findAktiivisetJaPäättyneetOpinnotOppija(oppijaOid)
              .map(_.copy(
                jakolinkki = Some(Jakolinkki(o.voimassaAsti.toLocalDate))
              ))
          case _ => Left(KoskiErrorCategory.notFound())
        }
    )
  }

  def findAktiivisetJaPäättyneetOpinnotOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä] = {
    aktiivisetJaPäättyneetOpinnotService.findAktiivisetJaPäättyneetOpinnotOppija(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = true
    ).map(oppijaLaajatTiedot => {
      AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä(
        henkilö = SuoritusjakoHenkilö.fromOppijaHenkilö(oppijaLaajatTiedot.henkilö),
        opiskeluoikeudet = oppijaLaajatTiedot.opiskeluoikeudet
      )
    })
  }

  private def filterOpiskeluoikeudet(opiskeluoikeudet: Seq[Opiskeluoikeus], suoritusIds: List[SuoritusIdentifier]): Seq[Opiskeluoikeus] = {
    opiskeluoikeudet.flatMap { oo =>
      filterSuoritukset(oo, suoritusIds) match {
        case Nil => Nil
        case filtered => List(withSuoritukset(oo, filtered))
      }
    }
  }

  private def getOpiskeluoikeudetSuoritusIdentifierinMukaan(oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Either[HttpStatus, Seq[Opiskeluoikeus]] = {
    oppijaFacade.findOppija(oppijaOid)(KoskiSpecificSession.systemUser).map { oppijaWithWarnings =>
      oppijaWithWarnings.map { oppija =>
        suoritusIds.map(suoritusId =>
          oppija.opiskeluoikeudet.find(oo =>
            oo.suoritukset.exists(isMatchingSuoritus(oo, _, suoritusId))
          )
        )
      }
    } match {
      case Right(WithWarnings(matches, _)) if matches.isEmpty => Left(KoskiErrorCategory.badRequest.format())
      case Right(WithWarnings(matches, _)) if matches.exists(_.isEmpty) => Left(KoskiErrorCategory.notFound.suoritustaEiLöydy())
      case Right(WithWarnings(matches, _)) => Right(matches.flatten)
      case Left(status) => Left(status)
    }
  }

  private def filterSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritusIds: List[SuoritusIdentifier]): List[Suoritus] = {
    val filtered = opiskeluoikeus.suoritukset.filter { suoritus =>
      suoritusIds.exists(suoritusId => isMatchingSuoritus(opiskeluoikeus, suoritus, suoritusId))
    }

    resolveDuplicateSuoritusMatches(filtered)
  }

  /**
    * Suodattaa pois vanhan duplikaattisuorituksen siinä tilanteessa, jossa oppija suorittaa vuosiluokan uudestaan.
    * Odottaa syötesuoritusten sisältyvän samaan opiskeluoikeuteen.
    * @param suoritukset kyseisen opiskeluoikeuden päätason suoritukset
    * @return suoritukset, joista on mahdollisesti suodatettu pois duplikaatteja
    */
  private def resolveDuplicateSuoritusMatches(suoritukset: List[Suoritus]): List[Suoritus] = {
    def findMostRecentAlternative(alternatives: List[Suoritus]): List[Suoritus] = {
      val areAllVuosiluokanSuoritus = alternatives.map {
        case _: PerusopetuksenVuosiluokanSuoritus => true
        case _ => false
      }.forall(v => v)

      if (areAllVuosiluokanSuoritus) {
        alternatives.map { case s: PerusopetuksenVuosiluokanSuoritus => s }.filterNot(_.jääLuokalle) match {
          case Nil => alternatives
          case tuplaukset: List[Suoritus] => tuplaukset
        }
      } else {
        alternatives
      }
    }

    val (multipleMatches, singleMatches) = suoritukset
      .groupBy(s => (s.tyyppi.koodiarvo, s.koulutusmoduuli.tunniste.koodiarvo))
      .values.toList
      .partition(_.lengthCompare(1) > 0)

    multipleMatches.flatMap(findMostRecentAlternative) ++ singleMatches.flatten
  }

  private def isMatchingSuoritus(opiskeluoikeus: Opiskeluoikeus, suoritus: PäätasonSuoritus, suoritusId: SuoritusIdentifier): Boolean = {
    def checkKoulutusmoduulinTunniste(suoritusId: SuoritusIdentifier) = {
      if (suoritusId.suorituksenTyyppi == "korkeakoulunopintojakso") {
        // Korkeakoulun opintojakso on päätason suoritus, mutta jaettaessa jaetaan samalla kaikki "kelluvat"
        // opintojaksot (ei siis spesifillä koulutusmoduulin tunnisteella juuri tiettyä opintojaksoa).
        suoritusId.koulutusmoduulinTunniste == ""
      } else {
        suoritus.koulutusmoduuli.tunniste.koodiarvo == suoritusId.koulutusmoduulinTunniste
      }
    }

    opiskeluoikeus.lähdejärjestelmänId.flatMap(_.id) == suoritusId.lähdejärjestelmänId &&
      (suoritusId.opiskeluoikeusOid.isEmpty || opiskeluoikeus.oid == suoritusId.opiskeluoikeusOid) &&
      opiskeluoikeus.oppilaitos.map(_.oid) == suoritusId.oppilaitosOid &&
      suoritus.tyyppi.koodiarvo == suoritusId.suorituksenTyyppi &&
      checkKoulutusmoduulinTunniste(suoritusId)
  }

  private def withSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritukset: List[Suoritus]): Opiskeluoikeus = {
    import mojave._
    shapeless.lens[Opiskeluoikeus].field[List[Suoritus]]("suoritukset").set(opiskeluoikeus)(suoritukset)
  }
}
