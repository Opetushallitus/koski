package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData.{arviointi, oppiaine, suoritus, vuosiviikkotuntia}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

object ExamplesPerusopetukseenValmistavaOpetus {
  val perusopetukseenValmistavanOpetuksenSuoritus = PerusopetukseenValmistavanOpetuksenSuoritus(
    koulutusmoduuli = PerusopetukseenValmistavaOpetus(perusteenDiaarinumero = Some("57/011/2015")),
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2018, 6, 1)),
    suorituskieli = suomenKieli,
    osasuoritukset = Some(List(
      PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
        koulutusmoduuli = PerusopetukseenValmistavanOpetuksenOppiaine(
          tunniste = PaikallinenKoodi("ai", finnish("Äidinkieli")),
          laajuus = Some(LaajuusKaikkiYksiköt(10, Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko"))),
          opetuksenSisältö = Some(finnish("Suullinen ilmaisu ja kuullun ymmärtäminen"))
        ),
        arviointi = Some(List(SanallinenPerusopetuksenOppiaineenArviointi(kuvaus = Some(finnish("Keskustelee sujuvasti suomeksi")))))
      ),
      NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1)),
        arviointi = arviointi(9),
        luokkaAste = Some(Koodistokoodiviite("7", "perusopetuksenluokkaaste")),
        suoritustapa = Some(PerusopetusExampleData.suoritustapaErityinenTutkinto),
        suorituskieli = Some(Koodistokoodiviite("FI", "kieli"))
      )
    )),
    kokonaislaajuus = Some(LaajuusTunneissa(11))
  )

  val perusopetukseenValmistavaOpiskeluoikeus = PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2017, 8, 15), opiskeluoikeusLäsnä),
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2017, 12, 20), opiskeluoikeusLoma),
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2018, 1, 6), opiskeluoikeusLäsnä),
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2018, 6, 1), opiskeluoikeusValmistunut)
    )),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      perusopetukseenValmistavanOpetuksenSuoritus
    )
  )

  val examples = List(Example("perusopetukseen valmistava opetus", "Oppija on suorittanut perusopetukseen valmistavan opetuksen", Oppija(asUusiOppija(KoskiSpecificMockOppijat.koululainen), List(perusopetukseenValmistavaOpiskeluoikeus))))
}
