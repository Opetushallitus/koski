package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{taiteenPerusopetusAloitettu, taiteenPerusopetusValmis}
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
    arvioitsijat = Some(
      List(
        Arvioitsija("Musa Ope")
      )
    )
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
            jaksoHyväksytystiSuoritettu(päivä.plusMonths(6))
          )
        )

    def tilaMitätöity(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            jaksoLäsnä(päivä),
            jaksoMitätöity(päivä.plusMonths(6))
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
      suoritukset = List(
        PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia,
        PäätasonSuoritus.yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia
      ),
      organisaatiohistoria = None,
      arvioituPäättymispäivä = None
    )

    val hyväksytystiSuoritettuLaajaOppimäärä = suoritettuOppimäärä(
      oppimäärä = "laajaoppimaara",
      suoritukset = List(
          PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia,
          PäätasonSuoritus.laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia
        )
    )

    private def suoritettuOppimäärä(oppimäärä: String, suoritukset: List[TaiteenPerusopetuksenPäätasonSuoritus]) =
      TaiteenPerusopetuksenOpiskeluoikeus(
        oid = None,
        versionumero = None,
        aikaleima = None,
        lähdejärjestelmänId = None,
        oppilaitos = Some(varsinaisSuomenKansanopisto),
        koulutustoimija = Some(varsinaisSuomenAikuiskoulutussäätiö),
        tila = tilaHyväksytystiSuoritettu(),
        oppimäärä = Koodistokoodiviite(oppimäärä, "taiteenperusopetusoppimaara"),
        suoritukset = suoritukset,
        organisaatiohistoria = None,
        arvioituPäättymispäivä = Some(alkupäivä.plusYears(1))
      )

  }

  object PäätasonSuoritus {

    val yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia = TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräEiLaajuutta,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      arviointi = None,
      vahvistus = None,
      osasuoritukset = None
    )

    val yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia = TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      arviointi = None,
      vahvistus = None,
      osasuoritukset = None
    )

    val laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia = TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      arviointi = Some(List(arviointiHyväksytty)),
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(
        Osasuoritus.osasuoritusMusiikki("musa1", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa2", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa3", 9.63)
      ))
    )

    val laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia = TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiLaajaOppimääräSyventävätOpinnot,
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      arviointi = Some(List(arviointiHyväksytty)),
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(
        Osasuoritus.osasuoritusMusiikki("musa1", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa2", 8.52)
      ))
    )

    object Koulutusmoduuli {
      val musiikkiYleinenOppimääräEiLaajuutta = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari,
        None
      )
      val musiikkiYleinenOppimääräYhteisetOpinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari,
        Some(11.11)
      )
      val musiikkiYleinenOppimääräTeemaopinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari,
        Some(7.41)
      )
      val musiikkiLaajaOppimääräPerusopinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenLaajanOppimääränPerusteet2017.diaari,
        Some(29.63)
      )
      val musiikkiLaajaOppimääräSyventävätOpinnot = musiikinOpintotaso(
        Perusteet.TaiteenPerusopetuksenLaajanOppimääränPerusteet2017.diaari,
        Some(18.52)
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

  lazy val examples = List(
    Example(
      "taiteen perusopetus - läsnä",
      "Oppijalla on keskeneräinen taiteen perusopetuksen yleisen oppimäärän opintotason suoritus",
      oppijaOpiskeluoikeusAloitettu
    ), Example(
      "taiteen perusopetus - hyväksytystisuoritettu",
      "Oppijalla on hyväksytysti suoritettu taiteen perusopetuksen laajan oppimäärän opintotason suoritus",
      oppijaOpiskeluoikeusValmis
    )
  )

}
