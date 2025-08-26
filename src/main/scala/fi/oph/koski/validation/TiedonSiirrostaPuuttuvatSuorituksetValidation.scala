package fi.oph.koski.validation


import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusChangeMigrator.kopioiValmiitSuorituksetUuteen
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus}

/*
  Osata opiskeluoikeuksista voidaan tiedonsiirroissa jättää siirtämättä jo aikaisemmin Koskeen siirrettyjä päätason suorituksia lähdejärjestelmän rajoitusten takia.
  Tiedonsiirrosta puuttuvat päätason suoritukset kopioidaan osaksi uusinta opiskeluoikeuden versiota päivityksen yhteydessä. kts. OpiskeluoikeusChangeMigrator.scala
  Koska osa lähdejärjestelmistä siirtää vain yhden suorituksen kerrallaan, jo aikaisemmin tallennettuja suorituksia, jotka nyt puuttuvat uusimmasta siirrosta, ei voida
  validoida kaikkien validaatio-sääntöjen läpi, koska tämä voisi potentiaalisesti aiheuttaa tilanteen jossa uuden validaatiosäännön jälkeen lähdejärjestelmä ei enää pystyisi
  päivittämään koko opiskeluoikeutta, koska vanhat suoritukset eivät läpäise viimeisimpiä validaatioita.
*/
object TiedonSiirrostaPuuttuvatSuorituksetValidation {
  def validateEiSamaaAlkamispaivaa(uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, repository: KoskiOpiskeluoikeusRepository)(implicit user: KoskiSpecificSession): HttpStatus = {
    aikaisemminTallennetuillaSuorituksilla(uusiOpiskeluoikeus, repository)
      .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
      .groupBy(_.alkamispäivä)
      .find(_._2.length > 1) match {
      case Some(s) => {
        val tunnisteet = s._2.map(_.koulutusmoduuli.tunniste).mkString(", ")
        KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
          s"Vuosiluokilla ($tunnisteet) on sama alkamispäivä. Kahdella tai useammalla vuosiluokalla ei saa olla sama alkamispäivämäärä."
        )
      }
      case _ => HttpStatus.ok
    }
  }

  private def aikaisemminTallennetuillaSuorituksilla(uusi: KoskeenTallennettavaOpiskeluoikeus, repository: KoskiOpiskeluoikeusRepository)(implicit user: KoskiSpecificSession) = {
    val aikaisemminTallennettuOpiskeluoikeus = uusi.oid
      .map(oid => repository.findByOid(oid))
      .flatMap(_.map(_.toOpiskeluoikeusUnsafe(user)) match {
        case Right(vanha) => Some(vanha)
        case Left(_) => None
      })

    aikaisemminTallennettuOpiskeluoikeus
      .map(vanha => kopioiValmiitSuorituksetUuteen(vanha, uusi))
      .getOrElse(uusi)
      .suoritukset
  }
}
