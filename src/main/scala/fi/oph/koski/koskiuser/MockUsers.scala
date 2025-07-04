package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät._
import fi.oph.koski.koskiuser.Rooli.{AMMATILLINENKOULUTUS, KIELITUTKINTO, OPHKATSELIJA}
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.schema.SuorituksenTyyppi.{telma, valtionhallinnonKielitutkinto, yleinenKielitutkinto}
import fi.oph.koski.userdirectory._

import java.net.InetAddress

object MockUsers {
  val kalle = KoskiMockUser(
    "käyttäjä",
    "kalle",
    "1.2.246.562.24.99999999987",
    (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja)
  )

  val pärre = KoskiMockUser(
    "käyttäjä",
    "pärre",
    "1.2.246.562.24.99999999901",
    (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja),
    "sv"
  )

  val omadataOAuth2Palvelukäyttäjä = KoskiMockUser(
    "oauth2client",
    "oauth2client",
    "1.2.246.562.24.99999984728",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.dvv, List(
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
    )))
  )

  val omadataOAuth2KaikkiOikeudetPalvelukäyttäjä = KoskiMockUser(
    "oauth2kaikkiclient",
    "oauth2kaikkiclient",
    "1.2.246.562.24.99999984785",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.dvv, List(
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_HETU),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_OPPIJANUMERO),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_KAIKKI_TIEDOT),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT),
       PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT)
    )))
  )

  val omadataOAuth2IlmanLogoutPalvelukäyttäjä = KoskiMockUser(
    "oauth2clientnologout",
    "oauth2clientnologout",
    "1.2.246.562.24.99999985296",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.dvv, List(
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_HETU),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_OPPIJANUMERO),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT)
    )))
  )

  val omadataOAuth2SampleAppPalvelukäyttäjä = KoskiMockUser(
    "omadataoauth2sample",
    "omadataoauth2sample",
    "1.2.246.562.24.99999984735",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.dvv, List(
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_HETU),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_OPPIJANUMERO),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT)
    )))
  )

  val omadataOAuth2SampleAppPalvelukäyttäjäNoLogout = KoskiMockUser(
    "omadataoauth2samplenologout",
    "omadataoauth2samplenologout",
    "1.2.246.562.24.99999984739",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.dvv, List(
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_HETU),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_OPPIJANUMERO),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT)
    )))
  )

  val omadataOAuth2OphPalvelukäyttäjä = KoskiMockUser(
    "oauth2oph",
    "oauth2oph",
    "1.2.246.562.24.99999988375",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
    )))
  )

  val rekisteröimätönOmadataOAuth2Palvelukäyttäjä = KoskiMockUser(
    "oauth2clienteirek",
    "oauth2clienteirek",
    "1.2.246.562.24.99999984729",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.dvv, List(
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_NIMI),
      PalveluJaOikeus("KOSKI", Rooli.OMADATAOAUTH2_HENKILOTIEDOT_SYNTYMAAIKA),
    )))
  )

  val omniaPalvelukäyttäjä = KoskiMockUser(
    "käyttäjä",
    "omnia-palvelukäyttäjä",
    "1.2.246.562.24.99999999989",
    Seq(oppilaitosPalvelukäyttäjä(omnia))
  )

  val omniaKatselija = KoskiMockUser(
    "käyttäjä",
    "omnia-katselija",
    "1.2.246.562.24.99999999990",
    Seq(oppilaitosKatselija(omnia))
  )

  val omniaTallentaja = KoskiMockUser(
    "käyttäjä",
    "omnia-tallentaja",
    "1.2.246.562.24.99999999991",
    Seq(oppilaitosTallentaja(omnia))
  )

  val omniaPääkäyttäjä = KoskiMockUser(
    "omnia-pää",
    "omnia-pää",
    "1.2.246.562.24.99999977777",
    Seq(oppilaitosPääkäyttäjä(MockOrganisaatiot.omnia))
  )

  val tallentajaEiLuottamuksellinen = KoskiMockUser(
    "epäluotettava-tallentaja",
    "epäluotettava-tallentaja",
    "1.2.246.562.24.99999999997",
    Seq(ilmanLuottamuksellisiaTietoja(omnia), ilmanLuottamuksellisiaTietoja(jyväskylänNormaalikoulu))
  )

  val paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet = KoskiMockUser(
    "käyttäjä",
    "mikko",
    "1.2.246.562.24.99999999987",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophPääkäyttäjä.kayttooikeudet ++
        List(
          PalveluJaOikeus("KOSKI", Rooli.MITATOIDYT_OPISKELUOIKEUDET),
          PalveluJaOikeus("KOSKI", Rooli.POISTETUT_OPISKELUOIKEUDET)
        )
    ))
  )

  val paakayttajaMitatoidytOpiskeluoikeudet = KoskiMockUser(
    "käyttäjä",
    "mikko",
    "1.2.246.562.24.99999999987",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophPääkäyttäjä.kayttooikeudet ++
      List(
        PalveluJaOikeus("KOSKI", Rooli.MITATOIDYT_OPISKELUOIKEUDET)
      )
    ))
  )

  val paakayttaja = KoskiMockUser(
    "käyttäjä",
    "pää",
    "1.2.246.562.24.99999999992",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophPääkäyttäjä.kayttooikeudet ++
      localizationAdmin.kayttooikeudet ++
      List(
        PalveluJaOikeus("LOKALISOINTI", "CRUD"),
        PalveluJaOikeus("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA")
      )
    ))
  )

  val ophkatselija = KoskiMockUser(
    "käyttäjä",
    "oph-katselija",
    "1.2.246.562.24.99999999492",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophKatselija.kayttooikeudet
    ))
  )

  val viranomainen = KoskiMockUser(
    "käyttäjä",
    "viranomais",
    "1.2.246.562.24.99999999993",
    Seq(viranomaisKatselija)
  )

  val helsinginKaupunkiPalvelukäyttäjä = KoskiMockUser(
    "stadin-palvelu",
    "stadin-palvelu",
    "1.2.246.562.24.99999999994",
    Seq(oppilaitosPalvelukäyttäjä(helsinginKaupunki))
  )

  val helsinginKaupunkiEsiopetus = KoskiMockUser(
    "stadin-esiopetus",
    "stadin-esiopetus",
    "1.2.246.562.24.99999999944",
    Seq(oppilaitosEsiopetusKatselija(helsinginKaupunki))
  )

  val stadinAmmattiopistoJaOppisopimuskeskusTallentaja = KoskiMockUser(
    "tallentaja",
    "tallentaja",
    "1.2.246.562.24.99999999995",
    Seq(oppilaitosTallentaja(MockOrganisaatiot.stadinAmmattiopisto), oppilaitosTallentaja(MockOrganisaatiot.stadinOppisopimuskeskus))
  )

  val stadinAmmattiopistoKatselija = KoskiMockUser(
    "katselija",
    "katselija",
    "1.2.246.562.24.99999999985",
    Seq(oppilaitosKatselija(MockOrganisaatiot.stadinAmmattiopisto))
  )

  val stadinAmmattiopistoPalvelukäyttäjä = KoskiMockUser(
    "stadin-palvelukäyttäjä",
    "stadin-palvelukäyttäjä",
    "1.2.246.562.24.9999999989",
    Seq(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.stadinAmmattiopisto))
  )

  val jyväskylänKatselijaEsiopetus = KoskiMockUser(
    "esiopetus",
    "esiopetus",
    "1.2.246.562.24.99999999666",
    Seq(oppilaitosEsiopetusKatselija(MockOrganisaatiot.jyväskylänNormaalikoulu))
  )

  val jyväskylänKatselijaEiLuottamuksellinen = KoskiMockUser(
    "jyvas-eiluottoa",
    "jyvas-eiluottoa",
    "1.2.246.562.24.99999999888",
    Seq(ilmanLuottamuksellisiaTietoja(MockOrganisaatiot.jyväskylänNormaalikoulu))
  )

  val stadinAmmattiopistoPääkäyttäjä = KoskiMockUser(
    "stadinammattiopisto-admin",
    "stadinammattiopisto-admin",
    "1.2.246.562.24.99999999986",
    Seq(oppilaitosPääkäyttäjä(MockOrganisaatiot.stadinAmmattiopisto)),
    "fi",
    List("koski-oppilaitos-pääkäyttäjä_1494486198456")
  )

  val stadinVastuukäyttäjä = KoskiMockUser(
    "stadin-vastuu",
    "stadin-vastuu",
    "1.2.246.562.24.99999999996",
    Seq(vastuukäyttäjä(helsinginKaupunki))
  )

  val stadinPääkäyttäjä = KoskiMockUser(
    "stadin-pää",
    "stadin-pää",
    "1.2.246.562.24.99999999997",
    Seq(oppilaitosPääkäyttäjä(helsinginKaupunki)),
    "fi",
    List("koski-oppilaitos-pääkäyttäjä_1494486198456")
  )


  val varsinaisSuomiPalvelukäyttäjä = KoskiMockUser(
    "varsinaissuomi-tallentaja",
    "varsinaissuomi-tallentaja",
    "1.2.246.562.24.99999966699",
    Seq(oppilaitosPalvelukäyttäjä(varsinaisSuomenKansanopisto))
  )

  val varsinaisSuomiKoulutustoimijaKatselija = KoskiMockUser(
    "varsinaissuomi-koulutustoimija-katselija",
    "varsinaissuomi-koulutustoimija-katselija",
    "1.2.246.562.24.99966699999",
    Seq(oppilaitosKatselija(varsinaisSuomenAikuiskoulutussäätiö))
  )

  val varsinaisSuomiKoulutustoimija = KoskiMockUser(
    "varsinaissuomi-koulutustoimija-tallentaja",
    "varsinaissuomi-koulutustoimija-tallentaja",
    "1.2.246.562.24.99996669999",
    Seq(oppilaitosTallentaja(varsinaisSuomenAikuiskoulutussäätiö))
  )

  val varsinaisSuomiPääkäyttäjä = KoskiMockUser(
    "varsinaissuomi-oppilaitos-pää",
    "varsinaissuomi-oppilaitos-pää",
    "1.2.246.562.24.99999666999",
    Seq(oppilaitosPääkäyttäjä(varsinaisSuomenKansanopisto))
  )

  val varsinaisSuomiOppilaitosTallentaja = KoskiMockUser(
    "varsinaissuomi-oppilaitos-tallentaja",
    "varsinaissuomi-oppilaitos-tallentaja",
    "1.2.246.562.24.99999966669",
    Seq(oppilaitosTallentaja(varsinaisSuomenKansanopisto))
  )

  val varsinaisSuomiHankintakoulutusOppilaitosTallentaja = KoskiMockUser(
    "varsinaissuomi-hankinta-oppilaitos-tallentaja",
    "varsinaissuomi-hankinta-oppilaitos-tallentaja",
    "1.2.246.562.24.99999966666",
    Seq(oppilaitosTallentajaTaiteenPerusopetusHankintakoulutus(varsinaisSuomenKansanopisto))
  )

  val helsinkiTallentaja = KoskiMockUser(
    "hki-tallentaja",
    "hki-tallentaja",
    "1.2.246.562.24.99999999977",
    Seq(oppilaitosTallentaja(helsinginKaupunki))
  )

  val helsinkiKatselija = KoskiMockUser(
    "hki-katselija",
    "hki-katselija",
    "1.2.246.562.24.99999999777",
    Seq(oppilaitosKatselija(helsinginKaupunki))
  )

  val tornioTallentaja = KoskiMockUser(
    "tornio-tallentaja",
    "tornio-tallentaja",
    "1.2.246.562.24.99999999988",
    Seq(oppilaitosTallentaja(tornionKaupunki))
  )

  val helsinkiSekäTornioTallentaja = KoskiMockUser(
    "helsinki-tornio-tallentaja",
    "helsinki-tornio-tallentaja",
    "1.2.246.562.24.99999999922",
    Seq(oppilaitosTallentaja(helsinginKaupunki), oppilaitosTallentaja(tornionKaupunki))
  )

  val pyhtäänTallentaja = KoskiMockUser(
    "pyhtaa-tallentaja",
    "pyhtaa-tallentaja",
    "1.2.246.562.24.99999999966",
    Seq(oppilaitosTallentaja(pyhtäänKunta))
  )

  val jyväskyläTallentaja = KoskiMockUser(
    "jyvaskyla-tallentaja",
    "jyvaskyla-tallentaja",
    "1.2.246.562.24.99999999955",
    Seq(oppilaitosTallentaja(jyväskylänYliopisto))
  )

  val touholaTallentaja = KoskiMockUser(
    "touhola-tallentaja",
    "touhola-tallentaja",
    "1.2.246.562.24.99999999933",
    Seq(oppilaitosTallentaja(päiväkotiTouhula))
  )

  val majakkaTallentaja = KoskiMockUser(
    "majakka-tallentaja",
    "majakka-tallentaja",
    "1.2.246.562.24.99999999911",
    Seq(oppilaitosTallentaja(päiväkotiMajakka))
  )

  val kahdenOrganisaatioPalvelukäyttäjä = KoskiMockUser(
    "palvelu2",
    "palvelu2",
    "1.2.246.562.24.99999999998",
    Seq(oppilaitosPalvelukäyttäjä(helsinginKaupunki), oppilaitosPalvelukäyttäjä(MockOrganisaatiot.omnia))
  )

  val omattiedot = KoskiMockUser(
    "Oppija",
    "Oili",
    "1.2.246.562.24.99999999999",
    Seq(oppilaitosTallentaja(omnia))
  )

  val eiOikkia = KoskiMockUser(
    "EiOikkia",
    "Otto",
    "1.2.246.562.24.99999999902",
    Seq(OrganisaatioJaKäyttöoikeudet(lehtikuusentienToimipiste, List(PalveluJaOikeus("OPPIJANUMEROREKISTERI", "READ"))))
  )

  val viranomainenGlobaaliKatselija = KoskiMockUser(
    "Viranomainen",
    "Eeva",
    "1.2.246.562.24.99999999111",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.evira, List(
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KIELITUTKINTO),
    )))
  )

  val kelaSuppeatOikeudet = KoskiMockUser(
    "Kela",
    "Suppea",
    "1.2.246.562.24.88888888111",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA),
    )))
  )

  val kelaLaajatOikeudet = KoskiMockUser(
    "Kela",
    "Laaja",
    "1.2.246.562.24.88888888222",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA),
    )))
  )

  val perusopetusViranomainen = KoskiMockUser(
    "Perusopetus",
    "Pertti",
    "1.2.246.562.24.99999999222",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS))))
  )

  val toinenAsteViranomainen = KoskiMockUser(
    "Toinenaste",
    "Teuvo",
    "1.2.246.562.24.99999999333",
    Seq(
      OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(PalveluJaOikeus("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA"))),
      OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE)))
    )
  )

  val korkeakouluViranomainen = KoskiMockUser(
    "Korkeakoulu",
    "Kaisa",
    "1.2.246.562.24.99999999444",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU))))
  )

  val jyväskylänNormaalikoulunPalvelukäyttäjä = KoskiMockUser(
    "jyväs-palvelu",
    "jyväs-palvelu",
    "1.2.246.562.24.99999999777",
    Seq(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.jyväskylänNormaalikoulu))
  )

  val jyväskylänYliopistonVastuukäyttäjä = KoskiMockUser(
    "jyväs-vastuu",
    "jyväs-vastuu",
    "1.2.246.562.24.99999997777",
    Seq(vastuukäyttäjä(MockOrganisaatiot.jyväskylänYliopisto)),
    "fi",
    List("Vastuukayttajat")
  )

  val migriKäyttäjä = KoskiMockUser(
    "Luovutus",
    "Migri",
    "1.2.246.562.24.99999988877",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.migri, List(
      PalveluJaOikeus("KOSKI", Rooli.MIGRI),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
    )))
  )

  val hslKäyttäjä = KoskiMockUser(
    "Palveluväylä",
    "HSL",
    "1.2.246.562.24.99999988899",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.hsl, List(
      PalveluJaOikeus("KOSKI", Rooli.HSL),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
    )))
  )

  val suomiFiKäyttäjä = KoskiMockUser(
    "Palveluväylä",
    "SuomiFi",
    "1.2.246.562.24.99999988889",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.suomifi, List(
      PalveluJaOikeus("KOSKI", Rooli.SUOMIFI),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
    )))
  )

  val tilastokeskusKäyttäjä = KoskiMockUser(
    "Tilastokeskus",
    "Teppo",
    "1.2.246.562.24.78787878787",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.tilastokeskus, List(
      PalveluJaOikeus("KOSKI", Rooli.TILASTOKESKUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TAITEENPERUSOPETUS),
    )))
  )

  val valviraKäyttäjä = KoskiMockUser(
    "Valvira",
    "Ville",
    "1.2.246.562.24.42042042040",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.valvira, List(
      PalveluJaOikeus("KOSKI", Rooli.VALVIRA),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE)
    )))
  )

  val esiopetusTallentaja = KoskiMockUser(
    "esiopetus-tallentaja",
    "esiopetus-tallentaja",
    "1.2.246.562.24.42042042041",
    Seq(OrganisaatioJaKäyttöoikeudet(helsinginKaupunki, List(
      PalveluJaOikeus("KOSKI", Rooli.READ_UPDATE_ESIOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
    )))
  )

  val perusopetusTallentaja = KoskiMockUser(
    "perusopetus-tallentaja",
    "perusopetus-tallentaja",
    "1.2.246.562.24.42042042042",
    Seq(OrganisaatioJaKäyttöoikeudet(jyväskylänNormaalikoulu, List(
      PalveluJaOikeus("KOSKI", Rooli.PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.READ_UPDATE),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
    )))
  )

  val oppivelvollisuutietoRajapinta = KoskiMockUser(
    "oppivelvollisuustieto-rajapinnan-kutsuja",
    "oppivelvollisuustieto-rajapinnan-kutsuja",
    "1.2.246.562.24.42042042444",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(PalveluJaOikeus("KOSKI", Rooli.OPPIVELVOLLISUUSTIETO_RAJAPINTA))))
  )

  val ytlKäyttäjä = KoskiMockUser(
    "YTL-virkailija",
    "ylermi",
    "1.2.246.562.24.42042042058",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.ytl, List(
      PalveluJaOikeus("KOSKI", Rooli.YTL),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE)
    )))
  )

  val vktKäyttäjä = KoskiMockUser(
    "VKT-virkailija",
    "valeria",
    "1.2.246.562.24.42042042058",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(
      PalveluJaOikeus("KOSKI", Rooli.VKT)
    )))
  )

  val kielitutkintorekisteriKäyttäjä = KoskiMockUser(
    "Kielitutkintorekisteri",
    "kitu",
    "1.2.246.562.24.69696942058",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(
      PalveluJaOikeus("KOSKI", Rooli.KIELITUTKINTOREKISTERI),
    )))
  )

  val hakemuspalveluKäyttäjä = KoskiMockUser(
    "Hakemuspalvelu-virkailija",
    "virpiina",
    "1.2.246.562.24.42042042069",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(
      PalveluJaOikeus("KOSKI", Rooli.HAKEMUSPALVELU_API),
    )))
  )

  val xssHyökkääjä = KoskiMockUser(
    "Paha Hakkeri</script><script>alert(1);",
    "xss-hakkeri",
    "1.2.246.562.24.42042046666",
    (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja)
  )

  val muuKuinSäänneltyKoulutusYritys = KoskiMockUser(
    "Jatkuva Koulutus Oy",
    "muks",
    "1.2.246.562.10.53455746569",
    List(MuuKuinSäänneltyKoulutusToimija.oppilaitos).map(oppilaitosTallentaja)
  )

  val pohjoiskalotinKoulutussäätiöKäyttäjä = KoskiMockUser(
    "pohjoiskalotti-tallentaja",
    "pohjoiskalotti-tallentaja",
    "1.2.246.562.10.53455745792",
    List(PohjoiskalotinKoulutussäätiö.oppilaitos).map(oppilaitosTallentaja)
  )

  val yleisenKielitutkinnonKäyttäjä = KoskiMockUser(
    "yki",
    "yki",
    "1.2.246.562.10.53400745790",
    List(päätasonSuoritukseenRajoitettuKatselija(KIELITUTKINTO, yleinenKielitutkinto))
  )

  val valtionhallinnonKielitutkinnonKäyttäjä = KoskiMockUser(
    "vkt",
    "vkt",
    "1.2.246.562.10.53400745791",
    List(päätasonSuoritukseenRajoitettuKatselija(KIELITUTKINTO, valtionhallinnonKielitutkinto))
  )

  val ykiJaVktKäyttäjä = KoskiMockUser(
    "yki-vkt",
    "yki-vkt",
    "1.2.246.562.10.53400745792",
    List(
      päätasonSuoritukseenRajoitettuKatselija(KIELITUTKINTO, yleinenKielitutkinto),
      päätasonSuoritukseenRajoitettuKatselija(KIELITUTKINTO, valtionhallinnonKielitutkinto),
    )
  )

  val stadinTelma = KoskiMockUser(
    "stadin-telma",
    "stadin-telma",
    "1.2.246.562.10.53400745793",
    List(
      organisaationPäätasonSuoritukseenRajoitettuPäivittäjä(
        MockOrganisaatiot.stadinAmmattiopisto,
        AMMATILLINENKOULUTUS,
        telma
      )
    )
  )

  val kaksiTallentajaoikeutta = KoskiMockUser(
    "tuplatallari",
    "tuplatallari",
    "1.2.246.562.10.53400745794",
    Seq(OrganisaatioJaKäyttöoikeudet(jyväskylänNormaalikoulu, List(
      PalveluJaOikeus("KOSKI", Rooli.READ_UPDATE),
      PalveluJaOikeus("KOSKI", Rooli.READ_UPDATE_ESIOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.KAIKKI_OPISKELUOIKEUS_TYYPIT),
      PalveluJaOikeus("KOSKI", Rooli.TIEDONSIIRRON_MITATOINTI)
    )))
  )

  val users = List(
    kalle,
    pärre,
    omniaPalvelukäyttäjä,
    omniaKatselija,
    omniaTallentaja,
    omniaPääkäyttäjä,
    paakayttaja,
    ophkatselija,
    paakayttajaMitatoidytOpiskeluoikeudet,
    paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet,
    viranomainen,
    helsinginKaupunkiPalvelukäyttäjä,
    helsinginKaupunkiEsiopetus,
    stadinAmmattiopistoPääkäyttäjä,
    stadinAmmattiopistoJaOppisopimuskeskusTallentaja,
    stadinAmmattiopistoKatselija,
    stadinAmmattiopistoPalvelukäyttäjä,
    jyväskylänKatselijaEsiopetus,
    jyväskylänKatselijaEiLuottamuksellinen,
    kahdenOrganisaatioPalvelukäyttäjä,
    omattiedot,
    stadinVastuukäyttäjä,
    stadinPääkäyttäjä,
    tallentajaEiLuottamuksellinen,
    helsinkiTallentaja,
    helsinkiKatselija,
    tornioTallentaja,
    helsinkiSekäTornioTallentaja,
    pyhtäänTallentaja,
    jyväskyläTallentaja,
    touholaTallentaja,
    majakkaTallentaja,
    eiOikkia,
    jyväskylänNormaalikoulunPalvelukäyttäjä,
    jyväskylänYliopistonVastuukäyttäjä,
    viranomainenGlobaaliKatselija,
    kelaSuppeatOikeudet,
    kelaLaajatOikeudet,
    perusopetusViranomainen,
    toinenAsteViranomainen,
    korkeakouluViranomainen,
    migriKäyttäjä,
    suomiFiKäyttäjä,
    hslKäyttäjä,
    tilastokeskusKäyttäjä,
    valviraKäyttäjä,
    esiopetusTallentaja,
    perusopetusTallentaja,
    oppivelvollisuutietoRajapinta,
    varsinaisSuomiPalvelukäyttäjä,
    varsinaisSuomiKoulutustoimijaKatselija,
    varsinaisSuomiKoulutustoimija,
    varsinaisSuomiPääkäyttäjä,
    varsinaisSuomiOppilaitosTallentaja,
    varsinaisSuomiHankintakoulutusOppilaitosTallentaja,
    ytlKäyttäjä,
    vktKäyttäjä,
    hakemuspalveluKäyttäjä,
    xssHyökkääjä,
    muuKuinSäänneltyKoulutusYritys,
    pohjoiskalotinKoulutussäätiöKäyttäjä,
    omadataOAuth2Palvelukäyttäjä,
    omadataOAuth2KaikkiOikeudetPalvelukäyttäjä,
    omadataOAuth2IlmanLogoutPalvelukäyttäjä,
    rekisteröimätönOmadataOAuth2Palvelukäyttäjä,
    omadataOAuth2SampleAppPalvelukäyttäjä,
    omadataOAuth2SampleAppPalvelukäyttäjäNoLogout,
    omadataOAuth2OphPalvelukäyttäjä,
    kielitutkintorekisteriKäyttäjä,
    yleisenKielitutkinnonKäyttäjä,
    valtionhallinnonKielitutkinnonKäyttäjä,
    ykiJaVktKäyttäjä,
    stadinTelma,
    kaksiTallentajaoikeutta,
  )
}

trait MockUser extends UserWithPassword {
  def lastname: String
  def firstname: String
  def oid: String
  def käyttöoikeudet: Set[Käyttöoikeus]
  def lang: String
  def käyttöoikeusRyhmät: List[String]

  def ldapUser: DirectoryUser

  def username = ldapUser.etunimet
  def password = username
}

case class KoskiMockUser(lastname: String, firstname: String, oid: String, käyttöoikeudetRaw: Seq[OrganisaatioJaKäyttöoikeudet], lang: String = "fi", käyttöoikeusRyhmät: List[String] = Nil) extends MockUser {

  val käyttöoikeudet = DirectoryClient.resolveKäyttöoikeudet(HenkilönKäyttöoikeudet(oid, käyttöoikeudetRaw.toList))._2.toSet

  lazy val ldapUser = DirectoryUser(oid, käyttöoikeudet.toList, firstname, lastname, Some(lang))
  def toKoskiSpecificSession(käyttöoikeudet: KäyttöoikeusRepository): KoskiSpecificSession = {
    val authUser: AuthenticationUser = fromDirectoryUser(username, ldapUser)
    new KoskiSpecificSession(authUser, "fi", InetAddress.getByName("192.168.0.10"), "", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
}
