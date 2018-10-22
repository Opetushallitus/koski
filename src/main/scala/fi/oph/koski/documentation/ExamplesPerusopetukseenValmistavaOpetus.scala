package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData.{arviointi, oppiaine, suoritus, vuosiviikkotuntia}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

object ExamplesPerusopetukseenValmistavaOpetus {
  val perusopetukseenValmistavaOpiskeluoikeus = PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    päättymispäivä = Some(date(2008, 6, 1)),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
      NuortenPerusopetuksenOpiskeluoikeusjakso(date(2007, 8, 15), opiskeluoikeusLäsnä),
      NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 6, 1), opiskeluoikeusValmistunut)
    )),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetukseenValmistavanOpetuksenSuoritus(
        koulutusmoduuli = PerusopetukseenValmistavaOpetus(perusteenDiaarinumero = Some("57/011/2015")),
        toimipiste = jyväskylänNormaalikoulu,
        vahvistus = vahvistusPaikkakunnalla(päivä = date(2008, 6, 1)),
        suorituskieli = suomenKieli,
        osasuoritukset = Some(List(
          PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
            koulutusmoduuli = PerusopetukseenValmistavanOpetuksenOppiaine(
              tunniste = PaikallinenKoodi("ai", finnish("Äidinkieli")),
              laajuus = Some(PerusopetukseenValmistavanKoulutuksenLaajuus(10, Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko"))),
              opetuksenSisältö = Some(finnish("Suullinen ilmaisu ja kuullun ymmärtäminen"))
            ),
            arviointi = Some(List(SanallinenPerusopetuksenOppiaineenArviointi(kuvaus = Some(finnish("Keskustelee sujuvasti suomeksi")))))
          ),
          suoritus(oppiaine("FY").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = arviointi(9))
        ))
      )
    )
  )

  val examples = List(Example("perusopetukseen valmistava opetus", "Oppija on suorittanut perusopetukseen valmistavan opetuksen", Oppija(asUusiOppija(MockOppijat.koululainen), List(perusopetukseenValmistavaOpiskeluoikeus))))
}
