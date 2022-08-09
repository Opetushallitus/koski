package fi.oph.koski.valpas.kuntailmoitus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.valpas.kuntavalvonta.ValpasKuntavalvontaService
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.oppija.OppijaHakutilanteillaLaajatTiedot
import fi.oph.koski.valpas.oppija.ValpasOppijaTestBase
import fi.oph.koski.valpas.valpasrepository.{ValpasExampleData, ValpasKuntailmoituksenTekijäHenkilö, ValpasKuntailmoituksenTekijäLaajatTiedot, ValpasKuntailmoitusLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}

import java.time.LocalDate.{of => date}
import java.time.LocalDateTime

class ValpasOppijaServiceKuntailmoitusSpec extends ValpasOppijaTestBase {
  private val kuntavalvontaService = new ValpasKuntavalvontaService(KoskiApplicationForTests)

  "Kuntailmoitukset getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla" - {
    "palauttaa kuntailmoituksettoman oppijan ilman kuntailmoituksia" in {
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa.oid)(defaultSession)
        .toOption.get

      oppija.kuntailmoitukset should equal(Seq.empty)
    }

    "palauttaa oppijasta tehdyn kuntailmoituksen kaikki tiedot ilmoituksen tekijälle" in {
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
      val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "palauttaa oppijasta tehdyn kuntailmoituksen kaikki tiedot ilmoituksen kohdekunnalle" in {
      // Tässä testissä pitää toistaiseksi temppuilla oppijalla, jolla on monta opiskeluoikeutta, koska pelkällä kuntakäyttäjällä ei vielä ole oikeuksia
      // oppijan tietoihin. Oppijalla on siis ilmoitus Jyväskylä normaalikoulusta Pyhtäälle, ja lisäksi oppija opiskelee Aapajoen peruskoulussa.
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid)(session(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu))
        .toOption.get

      val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
      val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "palauttaa oppijasta tehdystä kuntailmoituksesta vain perustiedot muulle kuin tekijälle tai kunnalle" in {
      // Tässä testissä pitää toistaiseksi temppuilla oppijalla, jolla on monta opiskeluoikeutta, koska pelkällä kuntakäyttäjällä ei vielä ole oikeuksia
      // oppijan tietoihin. Oppijalla on siis ilmoitus Jyväskylän normaalikoulusta Pyhtäälle, ja lisäksi oppija opiskelee Aapajoen peruskoulussa.
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid)(session(ValpasMockUsers.valpasHelsinkiJaAapajoenPeruskoulu))
        .toOption.get

      val expectedIlmoitusKaikkiTiedot = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
      val expectedIlmoitus: ValpasKuntailmoitusLaajatTiedot = karsiPerustietoihin(expectedIlmoitusKaikkiTiedot)

      val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "palauttaa kaikki master- ja slave-oideille tehdyt ilmoitukset pyydettäessä master-oidilla" in {
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitukset = Seq(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(karsiPerustietoihin(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta)).withAktiivinen(true),
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla).withAktiivinen(false)
      )

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "palauttaa kaikki master- ja slave-oideille tehdyt ilmoitukset pyydettäessä slave-oidilla" in {
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas.oid)(session(ValpasMockUsers.valpasHelsinkiJaAapajoenPeruskoulu))
        .toOption.get

      val expectedIlmoitukset = Seq(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta).withAktiivinen(true),
        täydennäAikaleimallaJaOrganisaatiotiedoilla(karsiPerustietoihin(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)).withAktiivinen(false),
      )

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "palauttaa ilmoitukset aikajärjestyksessä ja vain uusin on aktiivinen" in {
      val ilmoituksenTekopäivät = (1 to 3).map(date(2021, 8, _))
      val tarkastelupäivä = date(2021, 8, 30)

      ilmoituksenTekopäivät.map(
        tekopäivä => {
          rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tekopäivä)
          val ilmoitus =
            oppijanPuhelinnumerolla(
              tekopäivä.toString, // Tehdään varmuuden vuoksi ilmoituksista erilaisia myös muuten kuin aikaleiman osalta
              ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
            ).withOppijaOid(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)
          kuntailmoitusRepository.create(ilmoitus, Seq.empty)
        }
      )

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitukset = Seq(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-03", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 3).atStartOfDay)
          .withAktiivinen(true),
        täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-02", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 2).atStartOfDay)
          .withAktiivinen(false),
        täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-01", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 1).atStartOfDay)
          .withAktiivinen(false)
      )

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "Oppijalle, jonka kuntailmoituksista on poistettu lisätiedot, palautuu kuntailmoitukset vajailla tiedoilla" in {
      val oppija = ValpasMockOppijat.ilmoituksenLisätiedotPoistettu
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oppija.oid)(defaultSession)

      result.map(_.kuntailmoitukset.map(_.tekijä)) shouldBe Right(Seq(
        ValpasKuntailmoituksenTekijäLaajatTiedot(
          organisaatio = OidOrganisaatio(MockOrganisaatiot.jyväskylänNormaalikoulu),
          henkilö = Some(ValpasKuntailmoituksenTekijäHenkilö(
            oid = Some(ValpasMockUsers.valpasJklNormaalikoulu.oid),
            etunimet = None,
            sukunimi = None,
            kutsumanimi = None,
            email = None,
            puhelinnumero = None,
          )),
        ))
      )

      result.map(_.kuntailmoitukset.map(_.oppijanYhteystiedot)) shouldBe Right(Seq(None))
    }
  }

  "Kuntailmoitukset aktiivisuus" - {
    "aktiivinen jos on ilmoituksen tekemisen jälkeen vasta tulevaisuudessa alkava ov-suorittamiseen kelpaava opiskeluoikeus" in {
      val ilmoituksenTekopäivä = date(2021, 8, 1)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
      val ilmoitus =
        ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.lukionAloittanut.oid)
      kuntailmoitusRepository.create(ilmoitus, Seq.empty)

      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitus =
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay)
          .withAktiivinen(true)

      validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
    }

    "passiivinen jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaava opiskeluoikeus ja on kulunut 2 kk tai alle" in {
      val ilmoituksenTekopäivä = date(2021, 7, 15)
      val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
      val ilmoitus = ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.lukionAloittanut.oid)
      kuntailmoitusRepository.create(ilmoitus, Seq.empty)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitus =
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(false)

      validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
    }

    "ei-aktiivinen jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaava opiskeluoikeus ja on kulunut yli 2 kk" in {
      val ilmoituksenTekopäivä = date(2021, 7, 15)
      val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(1)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
      val ilmoitus =
        ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(
          ValpasMockOppijat.lukionAloittanut.oid
        )
      kuntailmoitusRepository.create(ilmoitus, Seq.empty)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitus =
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(false)

      validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
    }

    "aktiivinen, vaikka on yli 2 kk ilmoituksesta, mutta ei ole voimassaolevaa opiskeluoikeutta" in {
      val ilmoituksenTekopäivä = date(2021, 6, 10)
      val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(10)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
      val ilmoitus =
        ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta.withOppijaOid(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)
      kuntailmoitusRepository.create(ilmoitus, Seq.empty)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
        .toOption.get

      val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta, ilmoituksenTekopäivä.atStartOfDay)
      val expectedIlmoitukset = Seq(expectedIlmoitus.withAktiivinen(true))

      validateKuntailmoitukset(oppija, expectedIlmoitukset)
    }

    "aktiivinen, vaikka yli 2 kk ilmoituksesta, jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaamaton opiskeluoikeus" in {
      val ilmoituksenTekopäivä = date(2021, 6, 10)
      val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(10)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
      val ilmoitus =
        ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)
      kuntailmoitusRepository.create(ilmoitus, Seq.empty)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitus =
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(true)

      validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
    }

    "Passiivinen jos on ilmoituksen tekemisen jälkeen alkanut ja päättynyt ov-suorittamiseen kelpaava opiskeluoikeus" in {
      val ilmoituksenTekopäivä = date(2021, 7, 15)
      val tarkastelupäivä = date(2022, 6, 1)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
      val ilmoitus = ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla.withOppijaOid(ValpasMockOppijat.valmistunutNivelvaiheenOpiskelija2022.oid)
      kuntailmoitusRepository.create(ilmoitus, Seq.empty)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
      val oppija = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.valmistunutNivelvaiheenOpiskelija2022.oid)(defaultSession)
        .toOption.get

      val expectedIlmoitus =
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay).withAktiivinen(false)

      validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
    }
  }

  "Kuntailmoitukset hakeminen kunnalle" - {
    "palauttaa oikeat oppijat, case #1" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 8, 30))

      validateKunnanIlmoitetutOppijat(
        organisaatioOid = MockOrganisaatiot.helsinginKaupunki,
        user = ValpasMockUsers.valpasHelsinki
      )(Seq(
        ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia,
        ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele
      ))
    }

    "palauttaa oikeat oppijat, case #2" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2021, 8, 30))

      validateKunnanIlmoitetutOppijat(
        organisaatioOid = MockOrganisaatiot.pyhtäänKunta,
        user = ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu
      )(Seq(
        ValpasMockOppijat.turvakieltoOppija,
        ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia,
        ValpasMockOppijat.lukionAloittanutJollaVanhaIlmoitus,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster,
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2,
        ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus,
        ValpasMockOppijat.kasiinAstiToisessaKoulussaOllutJollaIlmoitus,
        ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus,
        ValpasMockOppijat.ilmoituksenLisätiedotPoistettu,
        ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele
      ))
    }
  }

  private def validateKunnanIlmoitetutOppijat(
    organisaatioOid: Oid,
    user: ValpasMockUser
  )(expectedOppijat: Seq[LaajatOppijaHenkilöTiedot]) = {
    val result = getKunnanIlmoitetutOppijat(organisaatioOid, user)
    result.map(_.map(_.oppija.henkilö.oid).sorted) shouldBe Right(expectedOppijat.map(_.oid).sorted)
  }

  private def getKunnanIlmoitetutOppijat(organisaatioOid: Oid, user: ValpasMockUser) = {
    kuntavalvontaService.getOppijatSuppeatTiedot(organisaatioOid)(session(user))
  }

  private def täydennäAikaleimallaJaOrganisaatiotiedoilla(
    kuntailmoitus: ValpasKuntailmoitusLaajatTiedot,
    aikaleima: LocalDateTime = rajapäivätService.tarkastelupäivä.atStartOfDay
  ): ValpasKuntailmoitusLaajatTiedot  = {
    // Yksinkertaista vertailukoodia testissä tekemällä samat aikaleiman ja organisaatiodatan täydennykset mitkä tehdään tuotantokoodissa.
    kuntailmoitus.copy(
      aikaleima = Some(aikaleima),
      tekijä = kuntailmoitus.tekijä.copy(
        organisaatio = organisaatioRepository.getOrganisaatio(kuntailmoitus.tekijä.organisaatio.oid).get
      ),
      kunta = organisaatioRepository.getOrganisaatio(kuntailmoitus.kunta.oid).get
    )
  }

  private def karsiPerustietoihin(kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusLaajatTiedot = {
    kuntailmoitus.copy(
      tekijä = kuntailmoitus.tekijä.copy(
        henkilö = None
      ),
      yhteydenottokieli = None,
      oppijanYhteystiedot = None,
      hakenutMuualle = None
    )
  }

  private def oppijanPuhelinnumerolla(puhelinnumero: String, kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusLaajatTiedot =
    kuntailmoitus.copy(
      oppijanYhteystiedot = Some(kuntailmoitus.oppijanYhteystiedot.get.copy(
        puhelinnumero = Some(puhelinnumero)
      ))
    )

  private def validateKuntailmoitukset(oppija: OppijaHakutilanteillaLaajatTiedot, expectedIlmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot]) = {
    def clueMerkkijono(kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): String =
      s"${kuntailmoitus.tekijä.organisaatio.nimi.get.get("fi")}=>${kuntailmoitus.kunta.kotipaikka.get.nimi.get.get("fi")}"

    val maybeIlmoitukset = oppija.kuntailmoitukset.map(o => Some(o))
    val maybeExpectedData = expectedIlmoitukset.map(o => Some(o))

    maybeIlmoitukset.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
      case (element, index) => {
        withClue(s"index ${index}: ") {
          element match {
            case (Some(kuntailmoitusLisätiedoilla), Some(expectedData)) => {
              val clue = makeClue("ValpasKuntailmoitusLaajatTiedot", Seq(
                s"${kuntailmoitusLisätiedoilla.id}",
                clueMerkkijono(kuntailmoitusLisätiedoilla)
              ))

              withClue(clue) {
                withClue("aktiivinen") {
                  kuntailmoitusLisätiedoilla.aktiivinen should equal(expectedData.aktiivinen)
                }
                withClue("kunta") {
                  kuntailmoitusLisätiedoilla.kunta should equal(expectedData.kunta)
                }
                withClue("aikaleiman päivämäärä") {
                  kuntailmoitusLisätiedoilla.aikaleima.map(_.toLocalDate) should equal(expectedData.aikaleima.map(_.toLocalDate))
                }
                withClue("tekijä") {
                  kuntailmoitusLisätiedoilla.tekijä should equal(expectedData.tekijä)
                }
                withClue("yhteydenottokieli") {
                  kuntailmoitusLisätiedoilla.yhteydenottokieli should equal(expectedData.yhteydenottokieli)
                }
                withClue("oppijanYhteystiedot") {
                  kuntailmoitusLisätiedoilla.oppijanYhteystiedot should equal(expectedData.oppijanYhteystiedot)
                }
                withClue("hakenutMuualle") {
                  kuntailmoitusLisätiedoilla.hakenutMuualle should equal(expectedData.hakenutMuualle)
                }
              }
            }
            case (None, Some(expectedData)) =>
              fail(s"Ilmoitus puuttuu: oppija.oid:${oppija.oppija.henkilö.oid} oppija.hetu:${oppija.oppija.henkilö.hetu} ilmoitus:${clueMerkkijono(expectedData)}")
            case (Some(kuntailmoitusLisätiedoilla), None) =>
              fail(s"Saatiin ylimääräinen ilmoitus: oppija.oid:${oppija.oppija.henkilö.oid} oppija.hetu:${oppija.oppija.henkilö.hetu} ilmoitus:${clueMerkkijono(kuntailmoitusLisätiedoilla)}")
            case _ =>
              fail("Internal error")
          }
        }
      }
    }
  }
}
