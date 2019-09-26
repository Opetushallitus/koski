package fi.oph.koski.tutkinto
import fi.oph.koski.eperusteet._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.tutkinto
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

object EPerusteetTutkintoRakenneConverter extends Logging {
  def convertRakenne(rakenne: EPerusteRakenne)(implicit koodistoPalvelu: KoodistoViitePalvelu): TutkintoRakenne = {
    val suoritustavat: List[tutkinto.SuoritustapaJaRakenne] = rakenne.suoritustavat.toList.flatten.flatMap { (suoritustapa: ESuoritustapa) =>
      val koulutustyyppi: Koulutustyyppi = convertKoulutusTyyppi(rakenne.koulutustyyppi, suoritustapa.suoritustapakoodi)

      def convertRakenneOsa(rakenneOsa: ERakenneOsa, suoritustapa: ESuoritustapa): RakenneOsa = {
        rakenneOsa match {
          case x: ERakenneModuuli => RakenneModuuli(
            nimi = LocalizedString.sanitizeRequired(x.nimi.getOrElse(Map.empty), LocalizedString.missingString),
            osat = x.osat.map(osa => convertRakenneOsa(osa, suoritustapa)),
            määrittelemätön = x.rooli != Some("määritelty"),
            laajuus = x.muodostumisSaanto.flatMap(_.laajuus.map(l => TutkinnonOsanLaajuus(l.minimi, l.maksimi)))
          )
          case x: ERakenneTutkinnonOsa => suoritustapa.tutkinnonOsaViitteet.toList.flatten.find(v => v.id.toString == x._tutkinnonOsaViite) match {
            case Some(tutkinnonOsaViite) =>
              val eTutkinnonOsa: ETutkinnonOsa = rakenne.tutkinnonOsat.toList.flatten.find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa).get

              koodistoPalvelu.validate(Koodistokoodiviite(eTutkinnonOsa.koodiArvo, None, "tutkinnonosat", None)) match {
                case Some(tutkinnonosaKoodi) => TutkinnonOsa(tutkinnonosaKoodi, LocalizedString.sanitizeRequired(eTutkinnonOsa.nimi, eTutkinnonOsa.koodiArvo))
                case None => throw new RuntimeException("Tutkinnon osaa ei löydy koodistosta: " + eTutkinnonOsa.koodiArvo)
              }

            case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + x._tutkinnonOsaViite)
          }
          case x: ERakenneLukio => throw new RuntimeException("Lukion rakenteita ei vielä tueta")
        }
      }


      if (Koulutustyyppi.ammatillisetKoulutustyypit.contains(koulutustyyppi)) {
        koodistoPalvelu.validate(Koodistokoodiviite(suoritustapa.suoritustapakoodi, None, "ammatillisentutkinnonsuoritustapa", None))
          .map(SuoritustapaJaRakenne(_, suoritustapa.rakenne.map(convertRakenneOsa(_, suoritustapa))))
      } else {
        None
      }
    }

    val osaamisalat: List[Koodistokoodiviite] = rakenne.osaamisalat.map(o => Koodistokoodiviite(o.arvo, LocalizedString.sanitize(o.nimi), None, "osaamisala", None))
    val koulutukset: List[Koodistokoodiviite] = rakenne.koulutukset.map(k => Koodistokoodiviite(k.koulutuskoodiArvo, LocalizedString.sanitize(k.nimi), None, "koulutus", None))

    TutkintoRakenne(rakenne.diaarinumero, parseKoulutustyyppi(rakenne.koulutustyyppi), suoritustavat, osaamisalat, koulutukset)
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
