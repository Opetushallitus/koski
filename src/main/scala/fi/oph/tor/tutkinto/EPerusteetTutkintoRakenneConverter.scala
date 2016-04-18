package fi.oph.tor.tutkinto
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.koodisto.{KoodistoViite, KoodistoViitePalvelu}
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.log.Logging
import fi.oph.tor.schema.Koodistokoodiviite
import fi.oph.tor.tutkinto
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

object EPerusteetTutkintoRakenneConverter extends Logging {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiasteikkoRepository: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu): TutkintoRakenne = {
    var arviointiasteikkoViittaukset: Set[KoodistoViite] = Set.empty

    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.flatMap { (suoritustapa: ESuoritustapa) =>
      val koulutustyyppi: Koulutustyyppi = convertKoulutusTyyppi(rakenne.koulutustyyppi, suoritustapa.suoritustapakoodi)
      val arviointiasteikkoViittaus: Option[KoodistoViite] = arviointiasteikkoRepository.getArviointiasteikkoViittaus(koulutustyyppi)

      val laajuusYksikkö: Option[Koodistokoodiviite] = suoritustapa.laajuusYksikko.flatMap(_ match {
        case "OSAAMISPISTE" => koodistoPalvelu.validate(Koodistokoodiviite("6", None, "opintojenlaajuusyksikko", None))
        case x: String => {
          logger.warn("Opintojenlaajuusyksikkö not found for laajuusYksikko " + x)
          None
        }
      })

      def convertRakenneOsa(rakenneOsa: ERakenneOsa, suoritustapa: ESuoritustapa): RakenneOsa = {
        rakenneOsa match {
          case x: ERakenneModuuli => RakenneModuuli(
            LocalizedString(x.nimi.getOrElse(Map.empty)),
            x.osat.map(osa => convertRakenneOsa(osa, suoritustapa)),
            x.osaamisala.map(_.osaamisalakoodiArvo)
          )
          case x: ERakenneTutkinnonOsa => suoritustapa.tutkinnonOsaViitteet.toList.flatten.find(v => v.id.toString == x._tutkinnonOsaViite) match {
            case Some(tutkinnonOsaViite) =>
              val eTutkinnonOsa: ETutkinnonOsa = rakenne.tutkinnonOsat.find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa).get
              arviointiasteikkoViittaukset ++= arviointiasteikkoViittaus.toList
              if (arviointiasteikkoViittaus.isEmpty) {
                logger.warn("Arviointiasteikko not found for Koulutustyyppi " + koulutustyyppi)
              }

              val laajuus = laajuusYksikkö.flatMap(yksikkö => tutkinnonOsaViite.laajuus.map(laajuus => laajuus))

              koodistoPalvelu.validate(Koodistokoodiviite(eTutkinnonOsa.koodiArvo, None, "tutkinnonosat", None)) match {
                case Some(tutkinnonosaKoodi) => TutkinnonOsa(tutkinnonosaKoodi, LocalizedString(eTutkinnonOsa.nimi), arviointiasteikkoViittaus, tutkinnonOsaViite.laajuus, x.pakollinen)
                case None => throw new RuntimeException("Tutkinnon osaa ei löydy koodistosta: " + eTutkinnonOsa.koodiArvo)
              }

            case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
          }
        }
      }


      val suoritustapaKoodistoViite: Option[Koodistokoodiviite] = koodistoPalvelu.validate(Koodistokoodiviite(suoritustapa.suoritustapakoodi, None, "suoritustapa", None))
      suoritustapaKoodistoViite.map(SuoritustapaJaRakenne(_, suoritustapa.rakenne.map(convertRakenneOsa(_, suoritustapa)), laajuusYksikkö))
    }

    val osaamisalat: List[Koodistokoodiviite] = rakenne.osaamisalat.map(o => Koodistokoodiviite(o.arvo, Some(LocalizedString(o.nimi)), None, "osaamisala", None))

    TutkintoRakenne(rakenne.diaarinumero, suoritustavat, osaamisalat, arviointiasteikkoViittaukset.toList.flatMap(arviointiasteikkoRepository.getArviointiasteikko(_)))
  }

  private def convertKoulutusTyyppi(ePerusteetKoulutustyyppi: String, suoritustapa: String): Koulutustyyppi = {
    val tyyppi: Koulutustyyppi = ePerusteetKoulutustyyppi.substring(15).toInt
    if (ePerusteetKoulutustyyppi == 1 && suoritustapa == "naytto") {
      13 // <- Ammatillinen perustutkinto näyttötutkintona
    } else {
      tyyppi
    }
  }
}
