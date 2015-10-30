package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.koodisto.KoodistoViittaus
import fi.oph.tor.tutkinto
import fi.oph.tor.util.Timed

class TutkintoRepository(eperusteet: EPerusteetRepository) extends Timed {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = timed("findTutkinnot") {
    ePerusteetToTutkinnot(eperusteet.findPerusteet(query))
  }

  def findByEPerusteDiaarinumero(diaarinumero: String) = timed("findByEPerusteDiaarinumero") {
    ePerusteetToTutkinnot(eperusteet.findPerusteetByDiaarinumero(diaarinumero)).headOption
  }

  private def ePerusteetToTutkinnot(perusteet: List[EPeruste]) = {
    perusteet.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }

  def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository) = timed("findPerusteRakenne") {
    eperusteet.findRakenne(diaariNumero)
      .map(EPerusteetTutkintoRakenneConverter.convertRakenne)
  }
}

object EPerusteetTutkintoRakenneConverter {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiasteikkoRepository: ArviointiasteikkoRepository): TutkintoRakenne = {
    var arviointiasteikkoViittaukset: Set[KoodistoViittaus] = Set.empty

    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.map { (suoritustapa: ESuoritustapa) =>
      val arviointiasteikkoViittaus: Option[KoodistoViittaus] = arviointiasteikkoRepository.getArviointiasteikkoViittaus(Koulutustyyppi.fromEPerusteetKoulutustyyppiAndSuoritustapa(rakenne.koulutustyyppi, Suoritustapa(suoritustapa)))

      def convertRakenneOsa(rakenneOsa: ERakenneOsa, suoritustapa: ESuoritustapa): RakenneOsa = {
        rakenneOsa match {
          case x: ERakenneModuuli => RakenneModuuli(
            x.nimi.getOrElse(Map.empty).getOrElse("fi", ""),
            x.osat.map(osa => convertRakenneOsa(osa, suoritustapa)),
            x.osaamisala.map(_.osaamisalakoodiArvo)
          )
          case x: ERakenneTutkinnonOsa => suoritustapa.tutkinnonOsaViitteet.find(v => v.id.toString == x._tutkinnonOsaViite) match {
            case Some(tutkinnonOsaViite) =>
              val eTutkinnonOsa: ETutkinnonOsa = rakenne.tutkinnonOsat.find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa).get
              arviointiasteikkoViittaukset ++= arviointiasteikkoViittaus.toList
              TutkinnonOsa(KoulutusModuuliTunniste.tutkinnonOsa(eTutkinnonOsa.koodiArvo), eTutkinnonOsa.nimi.getOrElse("fi", ""), arviointiasteikkoViittaus)
            case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
          }
        }
      }


      SuoritustapaJaRakenne(Suoritustapa(suoritustapa), convertRakenneOsa(suoritustapa.rakenne, suoritustapa))
    }

    val osaamisalat: List[Osaamisala] = rakenne.osaamisalat.map(o => Osaamisala(o.nimi("fi"), o.arvo))

    TutkintoRakenne(suoritustavat, osaamisalat, arviointiasteikkoViittaukset.toList.flatMap(arviointiasteikkoRepository.getArviointiasteikko(_)))
  }
}