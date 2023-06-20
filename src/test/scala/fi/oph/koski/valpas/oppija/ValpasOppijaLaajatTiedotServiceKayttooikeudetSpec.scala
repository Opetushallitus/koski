package fi.oph.koski.valpas.oppija

import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}

import java.time.LocalDate.{of => date}

class ValpasOppijaLaajatTiedotServiceKäyttöoikeudetSpec extends ValpasOppijaTestBase {

  "Käyttöoikeudet" - {
    "Peruskoulun hakeutumisen valvoja saa haettua oman oppilaitoksen oppijan tiedot" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
        ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
      ) shouldBe true
    }

    "Peruskoulun hakeutumisen valvoja saa haettua 17 vuotta tänä vuonna täyttävän oman oppilaitoksen oppijan tiedot rajapäivään asti" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(
          rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
        )

      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.turvakieltoOppija,
        ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
      ) shouldBe true
    }

    "Peruskoulun hakeutumisen valvoja saa haettua 17 vuotta tänä vuonna täyttävän oman oppilaitoksen oppijan tiedot rajapäivän jälkeen" in {
      rajapäivätService.asInstanceOf[MockValpasRajapäivätService]
        .asetaMockTarkastelupäivä(
          rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä.plusDays(1)
        )

      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.turvakieltoOppija,
        ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
      ) shouldBe true
    }

    "Peruskoulun hakeutumisen valvoja saa haettua 18 vuotta tänä vuonna täyttävän oman oppilaitoksen oppijan tiedot" in {
      val päivä2022 = date(2022, 1, 15)

      rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(päivä2022)

      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.turvakieltoOppija,
        ValpasMockUsers.valpasJklNormaalikouluPelkkäPeruskoulu
      ) shouldBe true
    }
    "Peruskoulun hakeutumisen valvoja ei saa haettua toisen oppilaitoksen oppijan tietoja" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
        ValpasMockUsers.valpasHelsinkiPeruskoulu
      ) shouldBe false
    }

    "Käyttäjä, jolla hakeutumisen valvontaoikeudet koulutustoimijatasolla, näkee oppilaitoksen oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
        ValpasMockUsers.valpasJklYliopisto
      ) shouldBe true
    }

    "Käyttäjä, jolla globaalit oikeudet, näkee oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
        ValpasMockUsers.valpasOphPääkäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla maksuttomuusoikeudet, näkee peruskoulusta valmistuneen oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
        ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla kunnan oikeudet, näkee peruskoulusta valmistuneen oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
        ValpasMockUsers.valpasHelsinki
      ) shouldBe true
    }

    "Käyttäjä, jolla globaalit oikeudet, ei näe liian vanhaa oppijaa" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004,
        ValpasMockUsers.valpasOphPääkäyttäjä
      ) shouldBe false
    }

    "Käyttäjä, jolla globaalit oikeudet, ei näe oppijaa, joka on valmistunut peruskoulusta ennen lain rajapäivää" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.ennenLainRajapäivääPeruskoulustaValmistunut,
        ValpasMockUsers.valpasOphPääkäyttäjä
      ) shouldBe false
    }

    "Käyttäjä, jolla OPPILAITOS_HAKEUTUMINEN globaalit oikeudet, ei näe oppijaa, joka on valmistunut peruskoulusta yli 2 kk aiemmin" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.yli2kkAiemminPeruskoulustaValmistunut,
        ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä
      ) shouldBe false
    }

    "Käyttäjä, jolla vain globaalit OPPILAITOS_HAKEUTUMINEN oikeudet, ei näe lukio-oppijaa" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.lukioOpiskelija,
        ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä
      ) shouldBe false
    }

    "Käyttäjä, jolla globaalit oikeudet näkee lukio-oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.lukioOpiskelija,
        ValpasMockUsers.valpasOphPääkäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla maksuttomuusoikeudet näkee lukio-oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.lukioOpiskelija,
        ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla kunnan oikeudet näkee lukio-oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.lukioOpiskelija,
        ValpasMockUsers.valpasHelsinki
      ) shouldBe true
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee lukio-oppijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.lukioOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee lukio-oppijan vielä valmistumisen jälkeenkin, koska YO-tutkinto oletetaan olevan suorittamatta" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.lukiostaValmistunutOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee ammattiopiskelijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.ammattikouluOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu
      ) shouldBe true
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ei näe ammattiopiskelijaa valmistumisen jälkeen" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.ammattikoulustaValmistunutOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu
      ) shouldBe false
    }


    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet näkee nivelvaiheen opiskelijan" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
      ) shouldBe true
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ei näe nivelvaiheen opiskelijaa valmistumisen jälkeen" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.nivelvaiheestaValmistunutOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
      ) shouldBe false
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ammattikouluun ei näe kaksoistutkinnon opiskelijaa valmistumisen jälkeen." in {
      // Näkyy ainoastaan lukiolle, päätetty niin.
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.kaksoistutkinnostaValmistunutOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu
      ) shouldBe false
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet lukioon ei näe yhteistutkinnon opiskelijaa ammatillisen tutkinnon valmistumisen jälkeen, vaikka YO-tutkinto oletetaan olevan suorittamatta" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.kaksoistutkinnostaValmistunutOpiskelija,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
      ) shouldBe false
    }

    "Käyttäjä, jolla pelkät suorittamisen valvonnan oikeudet ei näe peruskoulun oppijaa" in {
      canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(
        ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
        ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjä
      ) shouldBe false
    }
  }

  private def canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oppija.oid)(session(user)).isRight
}
