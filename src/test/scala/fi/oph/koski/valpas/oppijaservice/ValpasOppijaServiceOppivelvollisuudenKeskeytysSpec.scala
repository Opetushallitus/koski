package fi.oph.koski.valpas.oppijaservice

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasrepository.{UusiOppivelvollisuudenKeskeytys, ValpasOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUsers, ValpasRooli}

import java.time.LocalDate.{of => date}

class ValpasOppijaServiceOppivelvollisuudenKeskeytysSpec extends ValpasOppijaServiceTestBase {

  protected val oppivelvollisuudenKeskeytysService = KoskiApplicationForTests.valpasOppivelvollisuudenKeskeytysService

  "Oppivelvollisuuden keskeytys" - {
    "Oppivelvollisuutta ei pysty keskeyttämään ilman kunnan valvontaoikeuksia" in {
      val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
      val tekijäOrganisaatioOid = MockOrganisaatiot.jyväskylänNormaalikoulu
      val alku = rajapäivätService.tarkastelupäivä

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = None,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(defaultSession)

      result.left.map(_.statusCode) shouldBe Left(403)
    }

    "Oppivelvollisuutta ei pysty keskeyttämään organisaation nimissä, jos siihen ei ole oikeuksia" in {
      val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
      val tekijäOrganisaatioOid = MockOrganisaatiot.jyväskylänNormaalikoulu
      val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
      val alku = rajapäivätService.tarkastelupäivä

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = None,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(kuntaSession)

      result.left.map(_.statusCode) shouldBe Left(403)
    }

    "Oppivelvollisuuden pystyy keskeyttämään toistaiseksi kunnan valvontaoikeuksilla" in {
      val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
      val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
      val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
      val alku = rajapäivätService.tarkastelupäivä

      val keskeytykset = oppijaLaajatTiedotService
        .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid)(kuntaSession)
        .map(_.oppivelvollisuudenKeskeytykset)

      keskeytykset shouldBe Right(Seq.empty)

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = None,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(kuntaSession)

      val expectedKeskeytys = ValpasOppivelvollisuudenKeskeytys(
        id = result.toOption.get.id,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
        alku = alku,
        loppu = None,
        voimassa = true,
        tulevaisuudessa = false,
      )

      result shouldBe Right(expectedKeskeytys)

      val keskeytykset2 = oppijaLaajatTiedotService
        .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid)(kuntaSession)
        .map(_.oppivelvollisuudenKeskeytykset)

      keskeytykset2 shouldBe Right(List(expectedKeskeytys))
    }

    "Oppivelvollisuuden pystyy keskeyttämään määräaikaisesti kunnan valvontaoikeuksilla" in {
      val oppija = ValpasMockOppijat.valmistunutYsiluokkalainen
      val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
      val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
      val alku = rajapäivätService.tarkastelupäivä
      val loppu = alku.plusMonths(3)

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = Some(loppu),
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(kuntaSession)

      val expectedKeskeytys = ValpasOppivelvollisuudenKeskeytys(
        id = result.toOption.get.id,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
        alku = alku,
        loppu = Some(loppu),
        voimassa = true,
        tulevaisuudessa = false,
      )

      result shouldBe Right(expectedKeskeytys)

      val keskeytykset = oppijaLaajatTiedotService
        .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid)(kuntaSession)
        .map(_.oppivelvollisuudenKeskeytykset)

      keskeytykset shouldBe Right(List(expectedKeskeytys))
    }

    "Oppivelvollisuuden pystyy keskeyttämään Koskesta puuttuvalle oppijalle kunnan valvontaoikeuksilla" in {
      val oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen
      val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
      val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
      val alku = rajapäivätService.tarkastelupäivä
      val loppu = alku.plusMonths(3)

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = Some(loppu),
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(kuntaSession)

      val expectedKeskeytys = ValpasOppivelvollisuudenKeskeytys(
        id = result.toOption.get.id,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
        alku = alku,
        loppu = Some(loppu),
        voimassa = true,
        tulevaisuudessa = false,
      )

      result shouldBe Right(expectedKeskeytys)

      val keskeytykset = oppijaLaajatTiedotService
        .getOppijaLaajatTiedotHakuJaYhteystiedoilla(oppija.oid, rooli = Some(ValpasRooli.KUNTA), haeMyösVainOppijanumerorekisterissäOleva = true)(kuntaSession)
        .map(_.oppivelvollisuudenKeskeytykset)

      keskeytykset shouldBe Right(List(expectedKeskeytys))
    }

    "Oppivelvollisuutta ei pysty keskeyttämään hetuttomalta Koskesta puuttuvalta oppijalta" in {
      val oppija = ValpasMockOppijat.eiKoskessaHetuton
      val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
      val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
      val alku = rajapäivätService.tarkastelupäivä
      val loppu = alku.plusMonths(3)

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = Some(loppu),
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(kuntaSession)

      result.left.map(_.statusCode) shouldBe Left(403)
    }

    "Oppivelvollisuutta ei voi keskeyttää ellei oppija ole ovl-lain alainen" in {
      val oppija = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004
      val tekijäOrganisaatioOid = MockOrganisaatiot.helsinginKaupunki
      val kuntaSession = session(ValpasMockUsers.valpasUseitaKuntia)
      val alku = rajapäivätService.tarkastelupäivä

      val result = oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys(UusiOppivelvollisuudenKeskeytys(
        oppijaOid = oppija.oid,
        alku = alku,
        loppu = None,
        tekijäOrganisaatioOid = tekijäOrganisaatioOid,
      ))(kuntaSession)

      result.left.map(_.statusCode) shouldBe Left(403)
    }
  }

}
