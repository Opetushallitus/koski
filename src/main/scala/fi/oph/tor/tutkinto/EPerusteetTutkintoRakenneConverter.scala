package fi.oph.tor.tutkinto
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.koodisto.{KoodistoViitePalvelu, KoodistoViite}
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.tutkinto
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi
import fi.vm.sade.utils.slf4j.Logging

object EPerusteetTutkintoRakenneConverter extends Logging {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiasteikkoRepository: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu): TutkintoRakenne = {
    var arviointiasteikkoViittaukset: Set[KoodistoViite] = Set.empty

    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.flatMap { (suoritustapa: ESuoritustapa) =>
      val koulutustyyppi: Koulutustyyppi = convertKoulutusTyyppi(rakenne.koulutustyyppi, suoritustapa.suoritustapakoodi)
      val arviointiasteikkoViittaus: Option[KoodistoViite] = arviointiasteikkoRepository.getArviointiasteikkoViittaus(koulutustyyppi)

      val laajuusYksikkö: Option[KoodistoKoodiViite] = suoritustapa.laajuusYksikko.flatMap(_ match {
        case "OSAAMISPISTE" => koodistoPalvelu.validate(KoodistoKoodiViite("6", None, "opintojenlaajuusyksikko", None))
        case x => {
          logger.warn("Opintojenlaajuusyksikkö not found for laajuusYksikko " + x)
          None
        }
      })

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

              koodistoPalvelu.validate(KoodistoKoodiViite(eTutkinnonOsa.koodiArvo, None, "tutkinnonosat", None)) match {
                case Some(tutkinnonosaKoodi) => TutkinnonOsa(tutkinnonosaKoodi, eTutkinnonOsa.nimi.getOrElse("fi", ""), arviointiasteikkoViittaus, tutkinnonOsaViite.laajuus, x.pakollinen)
                case None => throw new RuntimeException("Tutkinnon osaa ei löydy koodistosta: " + eTutkinnonOsa.koodiArvo)
              }

            case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
          }
        }
      }


      val suoritustapaKoodistoViite: Option[KoodistoKoodiViite] = koodistoPalvelu.validate(KoodistoKoodiViite(suoritustapa.suoritustapakoodi, None, "suoritustapa", None))
      suoritustapaKoodistoViite.map(SuoritustapaJaRakenne(_, convertRakenneOsa(suoritustapa.rakenne, suoritustapa), laajuusYksikkö))
    }

    val osaamisalat: List[Osaamisala] = rakenne.osaamisalat.map(o => Osaamisala(o.nimi("fi"), o.arvo))

    TutkintoRakenne(rakenne.diaarinumero, suoritustavat, osaamisalat, arviointiasteikkoViittaukset.toList.flatMap(arviointiasteikkoRepository.getArviointiasteikko(_)))
  }

  private def convertKoulutusTyyppi(ePerusteetKoulutustyyppi: String, suoritustapa: String): Koulutustyyppi = {
    if (ePerusteetKoulutustyyppi == "koulutustyyppi_1" && suoritustapa == "naytto") {
      13
    } else {
      ePerusteetKoulutustyyppi.substring(15).toInt
    }
  }
}
