package fi.oph.koski.tutkinto
import fi.oph.koski.arvosana.ArviointiasteikkoRepository
import fi.oph.koski.eperusteet._
import fi.oph.koski.koodisto.{KoodistoViite, KoodistoViitePalvelu}
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.tutkinto
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

object EPerusteetTutkintoRakenneConverter extends Logging {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit arviointiasteikkoRepository: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu): TutkintoRakenne = {
    var arviointiasteikkoViittaukset: Set[KoodistoViite] = Set.empty

    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.flatMap { (suoritustapa: ESuoritustapa) =>
      val koulutustyyppi: Koulutustyyppi = convertKoulutusTyyppi(rakenne.koulutustyyppi, suoritustapa.suoritustapakoodi)
      val arviointiasteikkoViittaus: Option[KoodistoViite] = arviointiasteikkoRepository.getArviointiasteikkoViittaus(koulutustyyppi)

      val laajuusYksikkö: Option[Koodistokoodiviite] = suoritustapa.laajuusYksikko.flatMap(_ match {
        case "OSAAMISPISTE" => koodistoPalvelu.validate(Koodistokoodiviite("6", "opintojenlaajuusyksikko"))
        case "KURSSI" => koodistoPalvelu.validate(Koodistokoodiviite("4", "opintojenlaajuusyksikko"))
        case x: String => {
          logger.warn("Opintojenlaajuusyksikkö not found for laajuusYksikko " + x)
          None
        }
      })

      def convertRakenneOsa(rakenneOsa: ERakenneOsa, suoritustapa: ESuoritustapa): RakenneOsa = {
        rakenneOsa match {
          case x: ERakenneModuuli => RakenneModuuli(
            LocalizedString.sanitizeRequired(x.nimi.getOrElse(Map.empty), LocalizedString.missingString),
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
                case Some(tutkinnonosaKoodi) => TutkinnonOsa(tutkinnonosaKoodi, LocalizedString.sanitizeRequired(eTutkinnonOsa.nimi, eTutkinnonOsa.koodiArvo), arviointiasteikkoViittaus, tutkinnonOsaViite.laajuus, x.pakollinen)
                case None => throw new RuntimeException("Tutkinnon osaa ei löydy koodistosta: " + eTutkinnonOsa.koodiArvo)
              }

            case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
          }
        }
      }


      if (Koulutustyyppi.ammatillisetKoulutustyypit.contains(koulutustyyppi)) {
        koodistoPalvelu.validate(Koodistokoodiviite(suoritustapa.suoritustapakoodi, None, "ammatillisentutkinnonsuoritustapa", None))
          .map(SuoritustapaJaRakenne(_, suoritustapa.rakenne.map(convertRakenneOsa(_, suoritustapa)), laajuusYksikkö))
      } else {
        None
      }
    }

    val osaamisalat: List[Koodistokoodiviite] = rakenne.osaamisalat.map(o => Koodistokoodiviite(o.arvo, LocalizedString.sanitize(o.nimi), None, "osaamisala", None))

    TutkintoRakenne(rakenne.diaarinumero, parseKoulutustyyppi(rakenne.koulutustyyppi), suoritustavat, osaamisalat, arviointiasteikkoViittaukset.toList.flatMap(arviointiasteikkoRepository.getArviointiasteikko(_)))
  }



  private def convertKoulutusTyyppi(ePerusteetKoulutustyyppi: String, suoritustapa: String): Koulutustyyppi = {
    val tyyppi: Koulutustyyppi = parseKoulutustyyppi(ePerusteetKoulutustyyppi)
    if (tyyppi == Koulutustyyppi.ammatillinenPerustutkinto && suoritustapa == "naytto") {
      Koulutustyyppi.ammatillinenPerustutkintoNäyttötutkintona
    } else {
      tyyppi
    }
  }

  def parseKoulutustyyppi(ePerusteetKoulutustyyppi: String): Koulutustyyppi = {
    Koulutustyyppi(ePerusteetKoulutustyyppi.substring(15).toInt)
  }
}
