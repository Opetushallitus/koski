package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.tutkinto
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi
import fi.vm.sade.utils.slf4j.Logging

class TutkintoRepository(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoPalvelu) {
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste] = {
    ePerusteetToTutkinnot(eperusteet.findPerusteet(query))
  }

  def findByEPerusteDiaarinumero(diaarinumero: String): Option[TutkintoPeruste] = {
    ePerusteetToTutkinnot(eperusteet.findPerusteetByDiaarinumero(diaarinumero)).headOption
  }

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne] = {
    eperusteet.findRakenne(diaariNumero)
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(arviointiAsteikot, koodistoPalvelu))
  }

  private def ePerusteetToTutkinnot(perusteet: List[EPeruste]) = {
    perusteet.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => TutkintoPeruste(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }
}

object EPerusteetTutkintoRakenneConverter extends Logging {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiasteikkoRepository: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoPalvelu): TutkintoRakenne = {
    var arviointiasteikkoViittaukset: Set[KoodistoViittaus] = Set.empty

    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.flatMap { (suoritustapa: ESuoritustapa) =>
      val koulutustyyppi: Koulutustyyppi = Koulutustyyppi.fromEPerusteetKoulutustyyppiAndSuoritustapa(rakenne.koulutustyyppi, suoritustapa.suoritustapakoodi)
      val arviointiasteikkoViittaus: Option[KoodistoViittaus] = arviointiasteikkoRepository.getArviointiasteikkoViittaus(koulutustyyppi)

      val laajuusYksikkö: Option[KoodistoKoodiViite] = suoritustapa.laajuusYksikko match {
        case "OSAAMISPISTE" => KoodistoPalvelu.getKoodistoKoodiViite(koodistoPalvelu, "opintojenlaajuusyksikko", "6", None)
        case _ => None
      }

      if(!laajuusYksikkö.isDefined) {
        logger.warn("Opintojenlaajuusyksikkö not found for laajuusYksikko " + suoritustapa.laajuusYksikko)
      }

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
              if (arviointiasteikkoViittaus.isEmpty) {
                logger.warn("Arviointiasteikko not found for Koulutustyyppi " + koulutustyyppi)
              }

              val laajuus = laajuusYksikkö.flatMap(yksikkö => tutkinnonOsaViite.laajuus.map(laajuus => laajuus))

              KoodistoPalvelu.getKoodistoKoodiViite(koodistoPalvelu, "tutkinnonosat", eTutkinnonOsa.koodiArvo, None) match {
                case Some(tutkinnonosaKoodi) => TutkinnonOsa(tutkinnonosaKoodi, eTutkinnonOsa.nimi.getOrElse("fi", ""), arviointiasteikkoViittaus, tutkinnonOsaViite.laajuus, x.pakollinen)
                case None => throw new RuntimeException("Tutkinnon osaa ei löydy koodistosta: " + eTutkinnonOsa.koodiArvo)
              }

            case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
          }
        }
      }


      val suoritustapaKoodistoViite: Option[KoodistoKoodiViite] = KoodistoPalvelu.getKoodistoKoodiViite(koodistoPalvelu, "suoritustapa", suoritustapa.suoritustapakoodi)
      suoritustapaKoodistoViite.map(SuoritustapaJaRakenne(_, convertRakenneOsa(suoritustapa.rakenne, suoritustapa), laajuusYksikkö))
    }

    val osaamisalat: List[Osaamisala] = rakenne.osaamisalat.map(o => Osaamisala(o.nimi("fi"), o.arvo))

    TutkintoRakenne(suoritustavat, osaamisalat, arviointiasteikkoViittaukset.toList.flatMap(arviointiasteikkoRepository.getArviointiasteikko(_)))
  }
}