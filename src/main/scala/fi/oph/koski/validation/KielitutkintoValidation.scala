package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax.symmetricalEitherChainingOps

object KielitutkintoValidation {
  def validateOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case oo: KielitutkinnonOpiskeluoikeus => validateKielitutkinnonOpiskeluoikeus(oo)
      case _ => HttpStatus.ok
    }

  private def validateKielitutkinnonOpiskeluoikeus(opiskeluoikeus: KielitutkinnonOpiskeluoikeus): HttpStatus =
    opiskeluoikeus.suoritukset.head match {
      case pts: YleisenKielitutkinnonSuoritus => HttpStatus.fold(
        validateYleisenKielitutkinnonPäivät(opiskeluoikeus, pts),
        validateYleisenKielitutkinnonArvioinnit(pts),
      )
      case pts: ValtionhallinnonKielitutkinnonSuoritus =>
        validateValtionhallinnonKielitutkinnonArvioinnit(pts)
      case _ => HttpStatus.ok
    }

  private def validateYleisenKielitutkinnonPäivät(opiskeluoikeus: KielitutkinnonOpiskeluoikeus, pts: YleisenKielitutkinnonSuoritus): HttpStatus = {
    val tutkintopäivä = opiskeluoikeus.tila.opiskeluoikeusjaksot.find(_.tila.koodiarvo == "lasna").map(_.alku)
    val arviointipäivä = opiskeluoikeus.tila.opiskeluoikeusjaksot.find(_.tila.koodiarvo == "hyvaksytystisuoritettu").map(_.alku)
    val vahvistuspäivä = pts.vahvistus.map(_.päivä)

    HttpStatus.fold(
      HttpStatus.validate(tutkintopäivä.isDefined)(KoskiErrorCategory.badRequest.validation.tila.tilaPuuttuu("Kielitutkinnolta puuttuu 'lasna'-tilainen opiskeluoikeuden jakso")),
      HttpStatus.validate(arviointipäivä.isDefined)(KoskiErrorCategory.badRequest.validation.tila.tilaPuuttuu("Kielitutkinnolta puuttuu 'hyvaksytystisuoritettu'-tilainen opiskeluoikeuden jakso")),
      HttpStatus.validate(arviointipäivä == vahvistuspäivä)(KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä(s"Arviointipäivä ${arviointipäivä.getOrElse("null")} (hyväksytysti suoritettu -tilainen opiskeluoikeusjakso) on eri kuin vahvistuksen päivämäärä ${vahvistuspäivä.getOrElse("null")}")),
    )
  }

  private def validateYleisenKielitutkinnonArvioinnit(pts: YleisenKielitutkinnonSuoritus): HttpStatus = {
    val osakokeet = pts.osasuoritukset.toList.flatten
    pts.koulutusmoduuli.tunniste.koodiarvo match {
      case "pt" => validateYleisenKielitutkinnonOsakokeidenArvioinnit(List("alle1", "1", "2"), osakokeet)
      case "kt" => validateYleisenKielitutkinnonOsakokeidenArvioinnit(List("alle3", "3", "4"), osakokeet)
      case "yt" => validateYleisenKielitutkinnonOsakokeidenArvioinnit(List("alle5", "5", "6"), osakokeet)
      case tutkintotaso: String => KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkintotaso: $tutkintotaso")
    }
  }

  private def validateYleisenKielitutkinnonOsakokeidenArvioinnit(sallitutArvosanat: List[String], osakokeet: List[YleisenKielitutkinnonOsakokeenSuoritus]): HttpStatus =
    HttpStatus.fold(osakokeet.map(
      osakoe => validateKielitutkinnonOsakokeenArviointi(sallitutArvosanat ++ List("9", "10", "11"), osakoe)
    ))

  private def validateValtionhallinnonKielitutkinnonArvioinnit(pts: ValtionhallinnonKielitutkinnonSuoritus): HttpStatus = {
    val sallitutArvosanat = pts.koulutusmoduuli.tunniste.koodiarvo match {
      case "hyvajatyydyttava" => Right(List("hyva", "tyydyttava", "hylatty"))
      case "erinomainen" => Right(List("erinomainen", "hylatty"))
      case koodi: Any => Left(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Tuntematon tutkintotason tunniste: $koodi"))
    }

    val kielitaidonSuoritukset = pts.osasuoritukset.toList.flatten

    HttpStatus.fold(
      kielitaidonSuoritukset.map { kielitaito =>
        val osakokeidenSuoritukset = kielitaito.osasuoritukset.toList.flatten
        sallitutArvosanat.map(arvosanat => HttpStatus.fold(
          List(validateValtionhallinnonKielitutkinnonKielitaidonArviointi(arvosanat, kielitaito)) ++
          osakokeidenSuoritukset.map(validateKielitutkinnonOsakokeenArviointi(arvosanat, _))
        )).get
      }
    )
  }

  private def validateValtionhallinnonKielitutkinnonKielitaidonArviointi(sallitutArvosanat: List[String], kielitaito: ValtionhallinnonKielitutkinnonKielitaidonSuoritus): HttpStatus =
    if (kielitaito.arviointi.isDefined) {
      validateKielitutkinnonOsakokeenArviointi(sallitutArvosanat, kielitaito)
    } else {
      HttpStatus.ok
    }

  private def validateKielitutkinnonOsakokeenArviointi(sallitutArvosanat: List[String], suoritus: Suoritus): HttpStatus = {
    val arvosanat = suoritus.arviointi.toList.flatten.map(_.arvosana.koodiarvo)
    HttpStatus.fold(
      List(HttpStatus.validate(arvosanat.nonEmpty)(KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu("Osakokeen arviointi puuttuu"))) ++
      arvosanat.map(arvosana =>
        HttpStatus.validate(
          sallitutArvosanat.contains(arvosana)
        )(
          KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(
            s"Suoritus ${suoritus.koulutusmoduuli.tunniste} sisältää virheellisen arvosanan $arvosana (sallitut koodiarvot ovat: ${sallitutArvosanat.mkString(", ")})"
          )
        )
      )
    )
  }
}
