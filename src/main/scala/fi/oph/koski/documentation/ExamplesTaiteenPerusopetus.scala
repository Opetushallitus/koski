package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{taiteenPerusopetusAloitettu, taiteenPerusopetusValmis}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object ExamplesTaiteenPerusopetus {

  val alkupäivä = date(2021, 1, 1)

  val taiteenPerusopetusYleinenOppimääräDiaari = "OPH-2069-2017"
  val taiteenPerusopetusLaajaOppimääräDiaari = "OPH-2068-2017"

  val musiikinTaiteenala = Koodistokoodiviite("musiikki", "taiteenperusopetustaiteenala")

  val helsinginKaupunkiKoulutustoimija = Koulutustoimija(
    oid = "1.2.246.562.10.346830761110",
    nimi = Some(Finnish("Helsingin kaupunki"))
  )
  val helsinginTyöväenopistoOppilaitos = Oppilaitos(
    oid = "1.2.246.562.10.97620158317",
    nimi = Some(Finnish("Helsingin kaupungin suomenkielinen työväenopisto"))
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
    myöntäjäOrganisaatio = helsinginTyöväenopistoOppilaitos,
    myöntäjäHenkilöt = List(
      OrganisaatiohenkilöValinnaisellaTittelillä(
        nimi = "Musa Ope",
        titteli = Some(Finnish("Opettaja")),
        organisaatio = helsinginTyöväenopistoOppilaitos
      )
    )
  )

  object Opiskeluoikeus {

    def tilaLäsnä(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeusjakso(
      alku = päivä,
      tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
    )

    def tilaHyväksytystiSuoritettu(päivä: LocalDate = alkupäivä) = TaiteenPerusopetuksenOpiskeluoikeusjakso(
      alku = päivä,
      tila = Koodistokoodiviite("hyvaksytystisuoritettu", "koskiopiskeluoikeudentila")
    )

    val aloitettuYleinenOppimäärä = TaiteenPerusopetuksenOpiskeluoikeus(
      oid = None,
      versionumero = None,
      aikaleima = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(helsinginTyöväenopistoOppilaitos),
      koulutustoimija = Some(helsinginKaupunkiKoulutustoimija),
      tila = TaiteenPerusopetuksenOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          tilaLäsnä()
        )
      ),
      oppimäärä = Koodistokoodiviite("yleinenoppimaara", "taiteenperusopetusoppimäärä"),
      suoritukset = List(PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia),
      organisaatiohistoria = None,
      arvioituPäättymispäivä = None
    )

    val hyväksytystiSuoritettuLaajaOppimäärä = TaiteenPerusopetuksenOpiskeluoikeus(
      oid = None,
      versionumero = None,
      aikaleima = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(helsinginTyöväenopistoOppilaitos),
      koulutustoimija = Some(helsinginKaupunkiKoulutustoimija),
      tila = TaiteenPerusopetuksenOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          tilaLäsnä(),
          tilaHyväksytystiSuoritettu(alkupäivä.plusMonths(6))
        )
      ),
      oppimäärä = Koodistokoodiviite("laajaoppimaara", "taiteenperusopetusoppimäärä"),
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
      koulutusmoduuli = Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot,
      toimipiste = helsinginTyöväenopistoOppilaitos,
      arviointi = None,
      vahvistus = None,
      osasuoritukset = None
    )

    val laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia = TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot,
      toimipiste = helsinginTyöväenopistoOppilaitos,
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
      toimipiste = helsinginTyöväenopistoOppilaitos,
      arviointi = Some(List(arviointiHyväksytty)),
      vahvistus = Some(vahvistus),
      osasuoritukset = Some(List(
        Osasuoritus.osasuoritusMusiikki("musa1", 10.0),
        Osasuoritus.osasuoritusMusiikki("musa2", 8.52)
      ))
    )

    object Koulutusmoduuli {
      val musiikkiYleinenOppimääräYhteisetOpinnot = musiikinOppimäärä(
        taiteenPerusopetusYleinenOppimääräDiaari,
        "yleisenoppimaaranyhteisetopinnot",
        None // 11.11 op
      )
      val musiikkiLaajaOppimääräPerusopinnot = musiikinOppimäärä(
        taiteenPerusopetusLaajaOppimääräDiaari,
        "laajanoppimaaranperusopinnot",
        Some(29.63)
      )
      val musiikkiLaajaOppimääräSyventävätOpinnot = musiikinOppimäärä(
        taiteenPerusopetusLaajaOppimääräDiaari,
        "laajanoppimaaransyventavatopinnot",
        Some(18.52)
      )

      private def musiikinOppimäärä(
        diaari: String,
        opintotaso: String,
        laajuus: Option[Double]
      ) = MusiikinOpintotaso(
        opintotaso = Koodistokoodiviite(opintotaso, "taiteenperusopetusopintotaso"),
        taiteenala = musiikinTaiteenala,
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
