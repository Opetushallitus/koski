package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{taiteenPerusopetusAloitettu, taiteenPerusopetusHankintakoulutus, taiteenPerusopetusValmis}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Perusteet

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object ExamplesTaiteenPerusopetus {

  val alkupäivä = date(2021, 1, 1)

  val musiikinTaiteenala = Koodistokoodiviite("musiikki", "taiteenperusopetustaiteenala")
  val kuvataiteenTaiteenala = Koodistokoodiviite("kuvataide", "taiteenperusopetustaiteenala")

  lazy val hkiKoulutustoimija = Koulutustoimija(
    oid = MockOrganisaatiot.helsinginKaupunki,
    nimi = Some(Finnish(fi = "Helsingin kaupunki"))
  )

  lazy val varsinaisSuomenAikuiskoulutussäätiö: Koulutustoimija = Koulutustoimija(
    oid = MockOrganisaatiot.varsinaisSuomenAikuiskoulutussäätiö,
    nimi = Some(Finnish("Varsinais-Suomen Aikuiskoulutussäätiö sr")),
    Some("0136193-2"),
    Some(Koodistokoodiviite("577", None, "kunta", None))
  )

  lazy val varsinaisSuomenKansanopisto: Oppilaitos = Oppilaitos(
    MockOrganisaatiot.varsinaisSuomenKansanopisto,
    Some(Koodistokoodiviite("01694", None, "oppilaitosnumero", None)),
    Some(Finnish("Varsinais-Suomen kansanopisto"))
  )

  lazy val varsinaisSuomenKansanopistoToimipiste: OidOrganisaatio = OidOrganisaatio(
    MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste
  )

  val arviointiHyväksytty = TaiteenPerusopetuksenArviointi(
    päivä = alkupäivä,
  )

  val vahvistus = HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(
    päivä = alkupäivä,
    paikkakunta = Some(ExampleData.helsinki),
    myöntäjäOrganisaatio = varsinaisSuomenKansanopisto,
    myöntäjäHenkilöt = List(
      OrganisaatiohenkilöValinnaisellaTittelillä(
        nimi = "Musa Ope",
        titteli = Some(Finnish("Opettaja")),
        organisaatio = varsinaisSuomenKansanopisto
      )
    )
  )

  val tunnustus = Some(
    TaiteenPerusopetuksenOsasuorituksenTunnustus(
      selite = Finnish("Tunnustettu paikallinen opintokokonaisuus")
    )
  )

  object Opiskeluoikeus {

    def jaksoLäsnä(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeusjakso(
      alku = päivä,
      tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
    )

    def jaksoHyväksytystiSuoritettu(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeusjakso(
      alku = päivä,
      tila = Koodistokoodiviite("hyvaksytystisuoritettu", "koskiopiskeluoikeudentila")
    )
    def jaksoPäättynyt(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeusjakso(
      alku = päivä,
      tila = Koodistokoodiviite("paattynyt", "koskiopiskeluoikeudentila")
    )

    def jaksoMitätöity(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeusjakso(
      alku = päivä,
      tila = Koodistokoodiviite("mitatoity", "koskiopiskeluoikeudentila")
    )

    def tilaLäsnä(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          jaksoLäsnä(päivä)
        )
      )

    def tilaHyväksytystiSuoritettu(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            jaksoLäsnä(päivä),
            jaksoHyväksytystiSuoritettu(päivä.plusMonths(12))
          )
        )

    def tilaMitätöity(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            jaksoLäsnä(päivä),
            jaksoMitätöity(päivä.plusMonths(12))
          )
        )

    val aloitettuYleinenOppimäärä = TaiteenPerusopetuksenOpiskeluoikeus(
      oid = None,
      versionumero = None,
      aikaleima = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(varsinaisSuomenKansanopisto),
      koulutustoimija = Some(varsinaisSuomenAikuiskoulutussäätiö),
      tila = tilaLäsnä(),
      oppimäärä = Koodistokoodiviite("yleinenoppimaara", "taiteenperusopetusoppimaara"),
      koulutuksenToteutustapa = Koodistokoodiviite("itsejarjestettykoulutus", "taiteenperusopetuskoulutuksentoteutustapa"),
      suoritukset = List(
        PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia,
        PäätasonSuoritus.yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia
      ),
      organisaatiohistoria = None,
      arvioituPäättymispäivä = None
    )

    val hyväksytystiSuoritettuLaajaOppimäärä = suoritettuOppimäärä()

    val hankintakoulutuksenaHyväksytystiSuoritettuLaajaOppimäärä = suoritettuOppimäärä().copy(
      koulutustoimija = Some(hkiKoulutustoimija),
      koulutuksenToteutustapa = Koodistokoodiviite("hankintakoulutus", "taiteenperusopetuskoulutuksentoteutustapa")
    )

    private def suoritettuOppimäärä() = TaiteenPerusopetuksenOpiskeluoikeus(
      oid = None,
      versionumero = None,
      aikaleima = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(varsinaisSuomenKansanopisto),
      koulutustoimija = Some(varsinaisSuomenAikuiskoulutussäätiö),
      tila = tilaHyväksytystiSuoritettu(),
      oppimäärä = Koodistokoodiviite("laajaoppimaara", "taiteenperusopetusoppimaara"),
      koulutuksenToteutustapa = Koodistokoodiviite("itsejarjestettykoulutus", "taiteenperusopetuskoulutuksentoteutustapa"),
      suoritukset = List(
        PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia,
        PäätasonSuoritus.laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia
      ),
      organisaatiohistoria = None,
      arvioituPäättymispäivä = Some(alkupäivä.plusYears(1))
    )

  }

  object PäätasonSuoritus {

    val yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia = TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräEiLaajuutta,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      vahvistus = None,
      osasuoritukset = None
    )

    val yleistenYhteistenOpintojenSuoritusArvioituJaVahvistettu = TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(Osasuoritus.osasuoritusMusiikki("musa1", 11.1)))
    )

    val yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia = TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      vahvistus = None,
      osasuoritukset = None
    )

    val yleistenTeemaopintojenSuoritusArvioituJaVahvistettu = TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(Osasuoritus.osasuoritusMusiikki("musa2", 7.4)))
    )

    val laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia = TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(
        Osasuoritus.tunnustettuOsasuoritusMusiikki("musa1", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa2", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa3", 9.6)
      ))
    )

    val laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia = TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiLaajaOppimääräSyventävätOpinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(
        Osasuoritus.osasuoritusMusiikki("musa1", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa2", 8.5)
      ))
    )

    object Koulutusmoduuli {
      val musiikkiYleinenOppimääräEiLaajuutta = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari,
        None
      )
      val musiikkiYleinenOppimääräYhteisetOpinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari,
        Some(11.1)
      )
      val musiikkiYleinenOppimääräTeemaopinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari,
        Some(7.4)
      )
      val musiikkiLaajaOppimääräPerusopinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenLaajanOppimääränPerusteet2017.diaari,
        Some(29.6)
      )
      val musiikkiLaajaOppimääräSyventävätOpinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenLaajanOppimääränPerusteet2017.diaari,
        Some(18.5)
      )

      private def musiikinOpintotaso(
        diaari: String,
        laajuus: Option[Double]
      ) = MusiikinOpintotaso(
        laajuus = laajuus.map(LaajuusOpintopisteissä(_)),
        perusteenDiaarinumero = Some(diaari)
      )
    }

  }

  object Osasuoritus {

    def osasuoritusMusiikki(
      tunniste: String,
      laajuus: Double
    ) = TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.paikallinenOsasuoritus(tunniste, laajuus),
      arviointi = Some(List(arviointiHyväksytty))
    )

    def tunnustettuOsasuoritusMusiikki(
      tunniste: String,
      laajuus: Double
    ): TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = osasuoritusMusiikki(tunniste, laajuus).copy(
      tunnustettu = tunnustus
    )

    object Koulutusmoduuli {
      def paikallinenOsasuoritus(tunniste: String, laajuus: Double) = TaiteenPerusopetuksenPaikallinenOpintokokonaisuus(
        tunniste = PaikallinenKoodi(tunniste, Finnish("Musiikin kurssi")),
        laajuus = LaajuusOpintopisteissä(laajuus)
      )
    }

  }


  lazy val oppijaOpiskeluoikeusAloitettu = Oppija(
    henkilö = MockOppijat.asUusiOppija(taiteenPerusopetusAloitettu),
    opiskeluoikeudet = List(
      Opiskeluoikeus.aloitettuYleinenOppimäärä
    )
  )

  lazy val oppijaOpiskeluoikeusValmis = Oppija(
    henkilö = MockOppijat.asUusiOppija(taiteenPerusopetusValmis),
    opiskeluoikeudet = List(
      Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä
    )
  )

  lazy val oppijaOpiskeluoikeusValmisHankintakoulutus = Oppija(
    henkilö = MockOppijat.asUusiOppija(taiteenPerusopetusHankintakoulutus),
    opiskeluoikeudet = List(
      Opiskeluoikeus.hankintakoulutuksenaHyväksytystiSuoritettuLaajaOppimäärä
    )
  )

  lazy val examples = List(
    Example(
      "taiteen perusopetus - läsnä",
      "Oppijalla on keskeneräinen taiteen perusopetuksen yleisen oppimäärän opintotason suoritus",
      oppijaOpiskeluoikeusAloitettu
    ), Example(
      "taiteen perusopetus - hyväksytystisuoritettu",
      "Oppijalla on hyväksytysti suoritettu taiteen perusopetuksen laajan oppimäärän opintotason suoritus",
      oppijaOpiskeluoikeusValmis
    ), Example(
      "taiteen perusopetus - hyväksytystisuoritettu hankintakoulutus",
      "Oppijalla on hankintakoulutuksena järjestetty hyväksytysti suoritettu taiteen perusopetuksen laajan oppimäärän opintotason suoritus",
      oppijaOpiskeluoikeusValmisHankintakoulutus
    )
  )

}
