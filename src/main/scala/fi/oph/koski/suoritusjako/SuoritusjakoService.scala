package fi.oph.koski.suoritusjako


import fi.oph.koski.db.KoskiTables

import java.time.LocalDate
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.log.KoskiOperation.KANSALAINEN_SUORITUSJAKO_LISAYS
import fi.oph.koski.log.KoskiAuditLogMessageField.oppijaHenkiloOid
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema._
import fi.oph.koski.util.WithWarnings

class SuoritusjakoService(suoritusjakoRepository: SuoritusjakoRepository, oppijaFacade: KoskiOppijaFacade) extends Logging {
  def put(oppijaOid: String, suoritusIds: List[SuoritusIdentifier])(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, Suoritusjako] = {
    getOpiskeluoikeudetSuoritusIdentifierinMukaan(oppijaOid, suoritusIds) match {
      case Left(status) => Left(status)
      case Right(opiskeluoikeudet) =>
        val secret = SuoritusjakoSecret.generateNew
        val suoritusjako = suoritusjakoRepository.create(secret, oppijaOid, suoritusIds)
        AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_LISAYS, koskiSession, Map(oppijaHenkiloOid -> oppijaOid)))
        // Kaikkia opiskeluoikeuksia ei talleteta Koskeen, jolloin niillä ei välttämättä ole oidia.
        // Ei yritetä merkata sellaisille opiskeluoikeuksille suoritusjakoa tehdyksi.
        opiskeluoikeudet.map(_.oid match {
          case Some(oid) if suoritusjako.isRight => oppijaFacade.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(oid)
          case _ =>
        })
        suoritusjako
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

  def get(secret: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    suoritusjakoRepository.get(secret).flatMap { suoritusjako =>
      oppijaFacade.findOppija(suoritusjako.oppijaOid)(koskiSession).map(_.map { oppija =>
        val suoritusIdentifiers = JsonSerializer.extract[List[SuoritusIdentifier]](suoritusjako.suoritusIds)
        val filtered = filterOpiskeluoikeudet(oppija.opiskeluoikeudet, suoritusIdentifiers)
        oppija.copy(opiskeluoikeudet = filtered)
      })
    }
  }

  def getAll(oppijaOid: String): Seq[Suoritusjako] = {
    suoritusjakoRepository.getAll(oppijaOid).map(jako => Suoritusjako(jako.secret, jako.voimassaAsti.toLocalDate, jako.aikaleima))
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
