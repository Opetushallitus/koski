package fi.oph.koski.suoritusjako


import java.util.UUID.randomUUID

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema._

class SuoritusjakoService(suoritusjakoRepository: SuoritusjakoRepository, oppijaFacade: KoskiOppijaFacade) extends Logging {
  def put(oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Either[HttpStatus, String] = {
    assertSuorituksetExist(oppijaOid, suoritusIds) match {
      case Right(_) =>
        val secret = generateNewSecret()
        suoritusjakoRepository.put(secret, oppijaOid, suoritusIds)
        Right(secret)
      case Left(status) => Left(status)
    }
  }

  def get(secret: String): Either[HttpStatus, Oppija] = {
    suoritusjakoRepository.get(secret).flatMap { suoritusjako =>
      oppijaFacade.findOppija(suoritusjako.oppijaOid)(KoskiSession.systemUser).map { oppija =>
        val suoritusIdentifiers = JsonSerializer.extract[List[SuoritusIdentifier]](suoritusjako.suoritusIds)
        val filtered = filterOpiskeluoikeudet(oppija.opiskeluoikeudet, suoritusIdentifiers)
        oppija.copy(opiskeluoikeudet = filtered)
      }
    }
  }

  def validateSuoritusjakoSecret(secret: String): Either[HttpStatus, String] = {
    if (secret.matches("^[0-9a-f]{32}$")) {
      Right(secret)
    } else {
      Left(KoskiErrorCategory.badRequest.format())
    }
  }

  private def generateNewSecret(): String = {
    randomUUID.toString.replaceAll("-", "")
  }

  private def filterOpiskeluoikeudet(opiskeluoikeus: Seq[Opiskeluoikeus], suoritusIds: List[SuoritusIdentifier]): Seq[Opiskeluoikeus] = {
    opiskeluoikeus.flatMap { oo =>
      filterSuoritukset(oo, suoritusIds) match {
        case Nil => Nil
        case filtered => List(withSuoritukset(oo, filtered))
      }
    }
  }

  private def assertSuorituksetExist(oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Either[HttpStatus, Boolean] = {
    oppijaFacade.findOppija(oppijaOid)(KoskiSession.systemUser).map { oppija =>
      suoritusIds.map(suoritusId =>
        oppija.opiskeluoikeudet.exists(oo =>
          oo.suoritukset.exists(isMatchingSuoritus(oo, _, suoritusId))
        )
      )
    } match {
      case Right(matches) if matches.isEmpty => Left(KoskiErrorCategory.badRequest.format())
      case Right(matches) if matches.exists(!_) => Left(KoskiErrorCategory.notFound.suoritustaEiLöydy())
      case Right(_) => Right(true)
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

  private def isMatchingSuoritus(opiskeluoikeus: Opiskeluoikeus, suoritus: PäätasonSuoritus, suoritusId: SuoritusIdentifier): Boolean =
    opiskeluoikeus.oppilaitos.exists(_.oid == suoritusId.oppilaitosOid) &&
      suoritus.tyyppi.koodiarvo == suoritusId.suorituksenTyyppi &&
      suoritus.koulutusmoduuli.tunniste.koodiarvo == suoritusId.koulutusmoduulinTunniste

  private def withSuoritukset(opiskeluoikeus: Opiskeluoikeus, suoritukset: List[Suoritus]): Opiskeluoikeus = {
    import mojave._
    shapeless.lens[Opiskeluoikeus].field[List[Suoritus]]("suoritukset").set(opiskeluoikeus)(suoritukset)
  }
}
