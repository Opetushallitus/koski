package fi.oph.koski.documentation

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOppijat}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

object ExamplesTutkintokoulutukseenValmentavaKoulutus {

  lazy val tuvaTila = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = List(
      TutkintokoulutukseenValmentavanOpiskeluoikeusjakso(
        alku = date(2021, 8, 1),
        tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
      )
    )
  )

  lazy val tuvaOpiskeluOikeus = TutkintokoulutukseenValmentavanOpiskeluoikeus(
    oppilaitos = Some(stadinAmmattiopisto),
    tila = tuvaTila,
    lisätiedot = Some(TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
      järjestämislupa = "Ammatillinen",
    )),
    suoritukset = List(
      TutkintokoulutukseenValmentavanKoulutuksenSuoritus(
        toimipiste = stadinAmmattiopisto,
        koulutusmoduuli = TutkintokoulutukseenValmentavanKoulutus(
          perusteenDiaarinumero = Some("OPH-58-2021"),
          tunniste = Koodistokoodiviite("999999", "koulutus"),
        ),
        vahvistus = None,
        suorituskieli = suomenKieli,
        osasuoritukset =
          Some(
            List(
              TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
                koulutusmoduuli = TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(
                  laajuus = Some(LaajuusViikoissa(2))
                ),
                arviointi = Some(
                  List(
                    SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
                      arvosana = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
                      kuvaus = None,
                      päivä = date(2022, 1, 31)
                    )
                  )
                ),
                suorituskieli = Some(suomenKieli),
                tunnustettu = None
              ),
              TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
                koulutusmoduuli = TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(
                  laajuus = Some(LaajuusViikoissa(1))
                ),
                arviointi = None,
                suorituskieli = Some(suomenKieli),
                tunnustettu = None
              ),
              TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
                koulutusmoduuli = TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(
                  laajuus = Some(LaajuusViikoissa(1))
                ),
                arviointi = None,
                suorituskieli = Some(suomenKieli),
                tunnustettu = None
              )
            )
          ),
      )
    )
  )

  lazy val tuvaOppija = Oppija(
    MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.tuva),
    List(
      tuvaOpiskeluOikeus
    )
  )

  lazy val examples = List(
    Example("tutkintokoulutukseen valmentava koulutus", "Oppija on suorittamassa tutkintokoulutukseen valmentavaa koulutusta.", tuvaOppija)
  )
}
