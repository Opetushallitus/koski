package fi.oph.koski.tutkinto

import fi.oph.koski.eperusteet._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi


object EPerusteetTutkintoRakenneConverter extends Logging {
  def convertRakenne(rakenne: EPerusteTarkkaRakenne)(implicit koodistoPalvelu: KoodistoViitePalvelu): TutkintoRakenne = {
    val suoritustavat = rakenne.suoritustavat.toList.flatten
      .filter(onAmmatillisenSuoritustapa(rakenne))
      .flatMap(suoritustapa =>
        koodistoPalvelu
          .validate(Koodistokoodiviite(suoritustapa.suoritustapakoodi, None, "ammatillisentutkinnonsuoritustapa", None))
          .map(SuoritustapaJaRakenne(_, suoritustapa.rakenne.map(convertRakenneOsa(rakenne, suoritustapa))))
      )

    val osaamisalat = rakenne.osaamisalat.map(o => Koodistokoodiviite(
      o.arvo,
      LocalizedString.sanitize(o.nimi), None, "osaamisala", None
    ))

    val koulutukset = rakenne.koulutukset.map(k => Koodistokoodiviite(
      k.koulutuskoodiArvo,
      LocalizedString.sanitize(k.nimi), None, "koulutus", None
    ))

    TutkintoRakenne(
      rakenne.id,
      rakenne.diaarinumero,
      parseKoulutustyyppi(rakenne.koulutustyyppi),
      suoritustavat,
      osaamisalat,
      koulutukset
    )
  }

  private def onAmmatillisenSuoritustapa(rakenne: EPerusteTarkkaRakenne)(suoritustapa: ETarkkaSuoritustapa): Boolean = {
    Koulutustyyppi.ammatillisetKoulutustyypit.contains(
      convertKoulutusTyyppi(rakenne.koulutustyyppi, suoritustapa.suoritustapakoodi)
    )
  }

  private def convertRakenneOsa
    (rakenne: EPerusteTarkkaRakenne, suoritustapa: ETarkkaSuoritustapa)
    (rakenneOsa: ERakenneOsa)
    (implicit koodistoPalvelu: KoodistoViitePalvelu)
  : RakenneOsa = rakenneOsa match {
    case m: ERakenneModuuli => makeRakenneModuuli(rakenne, suoritustapa, m)
    case o: ERakenneTutkinnonOsa => makeTutkinnonOsa(rakenne, suoritustapa, koodistoPalvelu, o)
    case _: ERakenneLukio => throw new RuntimeException("Lukion rakenteita ei vielä tueta")
  }

  private def makeRakenneModuuli
    (rakenne: EPerusteTarkkaRakenne, suoritustapa: ETarkkaSuoritustapa, moduuli: ERakenneModuuli)
    (implicit koodistoPalvelu: KoodistoViitePalvelu)
  : RakenneModuuli = RakenneModuuli(
    nimi = LocalizedString.sanitizeRequired(moduuli.nimi.getOrElse(Map.empty), LocalizedString.missingString),
    osat = moduuli.osat.map(convertRakenneOsa(rakenne, suoritustapa)),
    määrittelemätön = !moduuli.rooli.contains("määritelty"),
    laajuus = moduuli.muodostumisSaanto.flatMap(_.laajuus.map(l => TutkinnonOsanLaajuus(l.minimi, l.maksimi)))
  )

  private def makeTutkinnonOsa
    (rakenne: EPerusteTarkkaRakenne, suoritustapa: ETarkkaSuoritustapa, koodistoPalvelu: KoodistoViitePalvelu, o: ERakenneTutkinnonOsa)
  : TutkinnonOsa =
    suoritustapa.tutkinnonOsaViitteet.toList.flatten.find(v => v.id.toString == o._tutkinnonOsaViite) match {
      case None => throw new RuntimeException("Tutkinnonosaviitettä ei löydy: " + o._tutkinnonOsaViite)
      case Some(tutkinnonOsaViite: ETutkinnonOsaViite) =>
        val eTutkinnonOsa: ETutkinnonOsa = rakenne.tutkinnonOsat
          .toList.flatten
          .find(o => o.id.toString == tutkinnonOsaViite._tutkinnonOsa)
          .get

        koodistoPalvelu.validate(Koodistokoodiviite(eTutkinnonOsa.koodiArvo, None, "tutkinnonosat", None)) match {
          case None => throw new RuntimeException("Tutkinnon osaa ei löydy koodistosta: " + eTutkinnonOsa.koodiArvo)
          case Some(tutkinnonosaKoodi) => TutkinnonOsa(
            tutkinnonosaKoodi,
            LocalizedString.sanitizeRequired(eTutkinnonOsa.nimi, eTutkinnonOsa.koodiArvo)
          )
        }
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
