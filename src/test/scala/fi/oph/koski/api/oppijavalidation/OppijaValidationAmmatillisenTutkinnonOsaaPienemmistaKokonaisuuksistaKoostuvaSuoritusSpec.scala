package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.TutkinnonOsaaPienempiKokonaisuusExample
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.LocalizedString.finnish

import java.time.LocalDate.{of => date}

class OppijaValidationAmmatillisenTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritusSpec extends MuuAmmatillinenSpecification[TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus] {
  "Osaamisen hankkimistapa, koulutussopimus, ryhmä" - {
    val suoritus = defaultPäätasonSuoritus.copy(
      osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))),
      koulutussopimukset = Some(List(koulutussopimusjakso)),
      ryhmä = Some("XY")
    )
    "palautetaan HTTP 200" in setupAmmatillinenPäätasonSuoritus(suoritus)(verifyResponseStatusOk())
  }

  "liittyyTutkintoon kentässä määritelty diaarinumero" - {
    def makeSuoritus(liittyyTutkintoon: AmmatillinenTutkintoKoulutus = autoalanPerustutkinto): TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =
      TutkinnonOsaaPienempiKokonaisuusExample.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus.copy(
        osasuoritukset = Some(List(
          PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
            TutkinnonOsaaPienempiKokonaisuus(
              PaikallinenKoodi("Ö", "Öljynvaihto kuorma-autoihin"),
              None,
              finnish("Öljynvaihdon suorittaminen erilaisissa kuorma-autoissa synteettisiä öljyjä käyttäen")
            ),
            alkamispäivä = None,
            arviointi = None,
            näyttö = None,
            liittyyTutkintoon = liittyyTutkintoon,
            lisätiedot = None,
            suorituskieli = None
          ),
        ))
      )

    "täydentää perusteen nimen diaarinumeron perusteella" in {
      def perusteenNimet(oo: AmmatillinenOpiskeluoikeus): Seq[Option[LocalizedString]] =
        oo.suoritukset.flatMap(_.rekursiivisetOsasuoritukset.collect { case s: PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus => s }.map(_.liittyyTutkintoon.perusteenNimi))

      val opiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(makeSuoritus()))
      perusteenNimet(opiskeluoikeus) should be(List(None))
      val luotuOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus)

      perusteenNimet(luotuOpiskeluoikeus) should be(List(Some(Finnish("Autoalan perustutkinto", Some("Grundexamen inom bilbranschen")))))
    }

    "voi viitata perusteeseen, josta löytyy koulutuskoodi" in {
      setupAmmatillinenPäätasonSuoritus(makeSuoritus()) {
        verifyResponseStatusOk()
      }
    }

    "ei voi viitata perusteeseen, josta ei löydy annettua koulutuskoodia" in {
      setupAmmatillinenPäätasonSuoritus(makeSuoritus(autoalanPerustutkinto.copy(tunniste = autoalanPerustutkinto.tunniste.copy(koodiarvo = "361902")))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tunnisteenKoodiarvoaEiLöydyRakenteesta("Tunnisteen koodiarvoa 361902 ei löytynyt opiskeluoikeuden voimassaoloaikana voimassaolleen rakenteen 39/011/2014 mahdollisista koulutuksista. Tarkista tutkintokoodit ePerusteista."))
      }
    }

    "voi viitata perusteeseen, jossa ei ole listattu mitään koulutuskoodeja" in {
      setupAmmatillinenPäätasonSuoritus(makeSuoritus(autoalanPerustutkinto.copy(perusteenDiaarinumero = Some("mock-empty-koulutukset")))) {
        verifyResponseStatusOk()
      }
    }

    "voi olla kokonaan määrittelemättä" in {
      setupAmmatillinenPäätasonSuoritus(makeSuoritus(autoalanPerustutkinto.copy(perusteenDiaarinumero = None))) {
        verifyResponseStatusOk()
      }
    }
  }

  override def defaultPäätasonSuoritus: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = kiinteistösihteerinTutkinnonOsaaPienempiMuuAmmatillinenKokonaisuus()
}
