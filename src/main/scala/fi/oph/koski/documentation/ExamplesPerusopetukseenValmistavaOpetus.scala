package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString.finnish
import fi.oph.koski.schema._

object ExamplesPerusopetukseenValmistavaOpetus {
  val opiskeluoikeus = PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    alkamispäivä = Some(date(2007, 8, 15)),
    päättymispäivä = Some(date(2008, 6, 1)),
    tila = PerusopetuksenOpiskeluoikeudenTila(List(
      PerusopetuksenOpiskeluoikeusjakso(date(2007, 8, 15), opiskeluoikeusLäsnä),
      PerusopetuksenOpiskeluoikeusjakso(date(2008, 6, 1), opiskeluoikeusValmistunut)
    )),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetukseenValmistavanOpetuksenSuoritus(
        tila = tilaValmis,
        toimipiste = jyväskylänNormaalikoulu,
        vahvistus = vahvistusPaikkakunnalla(),
        suorituskieli = suomenKieli,
        osasuoritukset = Some(List(
          PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
            koulutusmoduuli = PerusopetukseenValmistavanOpetuksenOppiaine(
              tunniste = PaikallinenKoodi("ai", finnish("Äidinkieli")),
              laajuus = Some(PerusopetukseenValmistavanKoulutuksenLaajuus(10, Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko"))),
              opetuksenSisältö = Some(finnish("Suullinen ilmaisu ja kuullun ymmärtäminen"))
            ),
            tila = tilaValmis,
            arviointi = Some(List(SanallinenPerusopetuksenOppiaineenArviointi(kuvaus = Some(finnish("Keskustelee sujuvasti suomeksi")))))
          )
        ))
      )
    )
  )

  val examples = List(Example("perusopetukseen valmistava opetus", "Oppija on suorittanut perusopetukseen valmistavan opetuksen", Oppija(MockOppijat.koululainen.vainHenkilötiedot, List(opiskeluoikeus))))
}
