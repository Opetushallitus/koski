package fi.oph.koski.valpas.oppija

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.{ErrorDetail, HttpStatus}
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import fi.oph.koski.valpas.yhteystiedot.{ValpasYhteystiedot, ValpasYhteystietoHakemukselta, ValpasYhteystietoOppijanumerorekisteristä}

import java.time.LocalDate.{of => date}

class ValpasOppijaLaajatTiedotServiceVainOnrSpec extends ValpasOppijaTestBase {
  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla Koskesta ja Valppaasta löytymättömällä oppijalla" - {
    "palauttaa oppijan tiedot kuntakäyttäjälle" in {
      val expectedOppijaData = ExpectedOppijaData(
        oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen,
        onOikeusValvoaMaksuttomuutta = true,
        onOikeusValvoaKunnalla = true
      )
      val expectedData = List()
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        expectedOppijaData.oppija.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      validateOppijaLaajatTiedot(result.oppija, expectedOppijaData, expectedData)
    }
    "palauttaa oppivelvollisuustiedot" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      result.oppija.oppivelvollisuusVoimassaAsti should equal(date(2023, 1, 23))
      result.oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(date(2025, 12, 31))
      result.onOikeusTehdäKuntailmoitus should equal(Some(false))
    }
    "palauttaa tiedon, ettei kuntailmoituksia saa tehdä, koska se ei vielä ole mahdollista Kosken ulkopuolisille oppijoille" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      result.onOikeusTehdäKuntailmoitus should equal(Some(false))
    }
    "palauttaa yhteystiedot" in {

      def tyhjilläHakuJaHakemusOideilla(yhteystiedot: ValpasYhteystiedot): ValpasYhteystiedot = yhteystiedot match {
        case yt: ValpasYhteystiedot if yt.alkuperä.isInstanceOf[ValpasYhteystietoHakemukselta] =>
          yt.copy(alkuperä = yt.alkuperä.asInstanceOf[ValpasYhteystietoHakemukselta].copy(
            hakuOid = "",
            hakemusOid = ""
          ))
        case yt => yt
      }

      val expectedYhteystiedot = Seq(
        ValpasYhteystiedot(
          alkuperä = ValpasYhteystietoHakemukselta(
            hakuNimi = LocalizedString.finnish("Yhteishaku 2021"),
            haunAlkamispaivämäärä = date(2020, 3, 9).atTime(12, 0, 0),
            hakemuksenMuokkauksenAikaleima = Some(date(2020, 4, 9).atTime(12, 0, 0)),
            hakuOid = "",
            hakemusOid = ""
          ),
          yhteystietoryhmänNimi = Finnish("Yhteystiedot", Some("Kontaktuppgifter")),
          henkilönimi = None,
          sähköposti = Some("Valpas.Kosketon@gmail.com"),
          puhelinnumero = None,
          matkapuhelinnumero = Some("0401234567"),
          lähiosoite = Some("Esimerkkikatu 123"),
          postitoimipaikka = Some("Helsinki"),
          postinumero = Some("99999"),
          maa = None
        ),
        ValpasYhteystiedot(
          alkuperä = ValpasYhteystietoOppijanumerorekisteristä(
            alkuperä = Koodistokoodiviite("alkupera1", "yhteystietojenalkupera"), // VTJ
            tyyppi = Koodistokoodiviite("yhteystietotyyppi1", "yhteystietotyypit"), // Kotiosoite
          ),
          yhteystietoryhmänNimi = Finnish(
            "Viralliset yhteystiedot Digi- ja väestötietovirastossa (DVV)",
            Some("Officiella kontaktuppgifter hos Myndigheten för digitalisering och befolkningsdata (MDB)"),
            Some("Official contact information in the Digital and Population Data Services Agency (DVV)")),
          henkilönimi = None,
          sähköposti = Some("valpas@gmail.com"),
          puhelinnumero = Some("0401122334"),
          matkapuhelinnumero = Some("0401122334"),
          lähiosoite = Some("Esimerkkitie 10"),
          postitoimipaikka = Some("Helsinki"),
          postinumero = Some("99999"),
          maa = Some(LocalizedString.finnish("  Costa rica  "))
        )
      ).map(tyhjilläHakuJaHakemusOideilla)

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      result.yhteystiedot.map(tyhjilläHakuJaHakemusOideilla) should equal(expectedYhteystiedot)
    }
    "palauttaa haku- ja valintatiedot" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      result.hakutilanteet should have length(1)
      result.hakutilanteet(0).hakutoiveet should have length(1)
    }

    "ei palauta yli-ikäistä oppijaa" in {
      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(date(2023, 1, 25))

      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasHelsinki))

      result should be(expectedResult)
    }

    "ei palauta lain piirissä olematonta oppijaa" in {
      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.oid
      )(session(ValpasMockUsers.valpasHelsinki))

      result should be(expectedResult)
    }

    "ei palauta hetutonta oppijaa" in {
      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaHetuton.oid
      )(session(ValpasMockUsers.valpasHelsinki))

      result should be(expectedResult)
    }

    "ei palauta oppijaa ennen elokuuta oppijan 7-vuotisvuotena" in {
      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(date(2021, 7, 31))

      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessa7VuottaTäyttävä.oid
      )(session(ValpasMockUsers.valpasHelsinki))

      result should be(expectedResult)
    }

    "ei palauta oppijaa, jos käyttäjällä ei ole kunta-oikeuksia" in {
      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.oid
      )(session(ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu))

      result should be(expectedResult)
    }
  }

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla Koskesta ja Valppaasta löytymättömällä oppijalla maksuttomuuskäyttäjällä" - {
    "palauttaa oppijan tiedot" in {
      val expectedOppijaData = ExpectedOppijaData(
        oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen,
        onOikeusValvoaMaksuttomuutta = true,
        onOikeusValvoaKunnalla = true
      )
      val expectedData = List()
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        expectedOppijaData.oppija.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä)).toOption.get

      validateOppijaLaajatTiedot(result.oppija, expectedOppijaData, expectedData)
    }
    "palauttaa oppivelvollisuustiedot" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä)).toOption.get

      result.oppija.oppivelvollisuusVoimassaAsti should equal(date(2023, 1, 23))
      result.oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(date(2025, 12, 31))
      result.onOikeusTehdäKuntailmoitus should equal(Some(false))
    }

    "palauttaa oppijan, vaikka hän olisi yli 18-vuotias" in {
      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(date(2025, 5, 9))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä)).toOption.get

      result.oppija.oppivelvollisuusVoimassaAsti should equal(date(2023, 1, 23))
      result.oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti should equal(date(2025, 12, 31))
      result.onOikeusTehdäKuntailmoitus should equal(Some(false))
    }

    "ei palauta oppijaa kun hänen 20-vuotisvuosi on ohi" in {
      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(date(2026, 1, 1))

      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinen.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä))

      result should be(expectedResult)
    }

    "ei palauta lain piirissä olematonta oppijaa" in {
      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä))

      result should be(expectedResult)
    }

    "ei palauta hetutonta oppijaa" in {
      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaHetuton.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä))

      result should be(expectedResult)
    }

    "ei palauta oppijaa ennen elokuuta oppijan 7-vuotisvuotena" in {
      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(date(2021, 7, 31))

      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessa7VuottaTäyttävä.oid
      )(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä))

      result should be(expectedResult)
    }

    "ei palauta oppijaa, jos käyttäjällä ei ole maksuttomuus-oikeuksia" in {
      val expectedResult = Left(HttpStatus(403,List(ErrorDetail(
        ValpasErrorCategory.forbidden.oppija.key, "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"
      ))))

      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.oid
      )(session(ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu))

      result should be(expectedResult)
    }
  }

  "getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla Koskesta löytymättömällä oppijalla, jolla on Valpas-merkintöjä" - {
    "palauttaa keskeytystiedot" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      result.oppivelvollisuudenKeskeytykset should have length(2)
    }
    "palauttaa ilmoitustiedot" in {
      val result = oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia.oid
      )(session(ValpasMockUsers.valpasHelsinki)).toOption.get

      result.kuntailmoitukset should have length(2)
    }
  }
}
