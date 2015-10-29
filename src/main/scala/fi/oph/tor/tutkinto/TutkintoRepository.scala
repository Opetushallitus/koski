package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.tutkinto

class TutkintoRepository(eperusteet: EPerusteetRepository) {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = {
    ePerusteetToTutkinnot(eperusteet.findPerusteet(query))
  }

  def findByEPerusteDiaarinumero(diaarinumero: String) = {
    ePerusteetToTutkinnot(eperusteet.findPerusteetByDiaarinumero(diaarinumero)).headOption
  }

  def ePerusteetToTutkinnot(perusteet: EPerusteet) = {
    perusteet.data.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }

  def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository) = {
    eperusteet.findRakenne(diaariNumero)
      .map(EPerusteetTutkintoRakenneConverter.convertRakenne)
  }
}

object EPerusteetTutkintoRakenneConverter {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiAsteikot: ArviointiasteikkoRepository): TutkintoRakenne = {
    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.map { (suoritustapa: ESuoritustapa) =>
      SuoritustapaJaRakenne(Suoritustapa(suoritustapa.suoritustapakoodi).get, convertRakenneOsa(rakenne.tutkinnonOsat, suoritustapa.rakenne, suoritustapa.tutkinnonOsaViitteet))
    }

    val osaamisalat: List[Osaamisala] = rakenne.osaamisalat.map(o => Osaamisala(o.nimi("fi"), o.arvo))

    TutkintoRakenne(suoritustavat, osaamisalat, arviointiAsteikot.getAll)
  }

  private def convertRakenneOsa(tutkinnonOsat: List[ETutkinnonOsa], rakenneOsa: ERakenneOsa, tutkinnonOsaViitteet: List[ETutkinnonOsaViite]): RakenneOsa = {
    rakenneOsa match {
      case x: ERakenneModuuli => RakenneModuuli(
        x.nimi.getOrElse(Map.empty).getOrElse("fi", ""),
        x.osat.map(osa => convertRakenneOsa(tutkinnonOsat, osa, tutkinnonOsaViitteet)),
        x.osaamisala.map(_.osaamisalakoodiArvo)
      )
      case x: ERakenneTutkinnonOsa => tutkinnonOsaViitteet.find(v => v.id.toString == x._tutkinnonOsaViite) match {
        case Some(tutkinnonOsaViite) =>
          val eTutkinnonOsa: ETutkinnonOsa = tutkinnonOsat.find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa).get
          TutkinnonOsa(KoulutusModuuliTunniste.tutkinnonOsa(eTutkinnonOsa.koodiArvo), eTutkinnonOsa.nimi.getOrElse("fi", ""), ArviointiasteikkoRepository.example)
        case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
      }
    }
  }
}