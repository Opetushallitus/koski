package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.documentation.ExampleData.{helsinki, opiskeluoikeusEronnut, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, suomenKieli, vahvistus, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.LukioExampleData.{opiskeluoikeusAktiivinen, opiskeluoikeusPäättynyt}
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, perusopetuksenOppimääränSuoritus, perusopetuksenOppimääränSuoritusKesken, yhdeksännenLuokanSuoritus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste, oppilaitos}
import fi.oph.koski.documentation.{AmmattitutkintoExample, ExampleData, ExamplesEsiopetus, ExamplesLukio2019, ExamplesPerusopetuksenLisaopetus, ExamplesTelma, ExamplesValma, VapaaSivistystyöExample}
import fi.oph.koski.organisaatio.MockOrganisaatiot.aapajoenKoulu
import fi.oph.koski.schema._
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData.{hyväksytty, järjestämismuotoOppilaitos, järjestämismuotoOppisopimus, stadinAmmattiopisto, stadinToimipiste, suoritustapaNäyttö, tutkinnonOsanSuoritus}
import fi.oph.koski.documentation.AmmattitutkintoExample.tutkinto
import fi.oph.koski.organisaatio.MockOrganisaatiot

object ValpasOpiskeluoikeusExampleData {
  def oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def ysiluokkaKeskenVsop = {
    val edellisetLisätiedot = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus
      .lisätiedot.getOrElse(PerusopetuksenOpiskeluoikeudenLisätiedot())

    oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        vuosiluokkiinSitoutumatonOpetus = true
      ))
    )
  }

  def valmistunutYsiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30))
      ),
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30)),
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 30), opiskeluoikeusValmistunut)
      )
    )
  )

  def alkaaYsiluokkalainenSaksalainenKouluSyys2021 =
    PerusopetuksenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos(MockOrganisaatiot.saksalainenKoulu)),
      koulutustoimija = None,
      suoritukset = List(
        perusopetuksenOppimääränSuoritusKesken,
        yhdeksännenLuokanSuoritus.copy(
          alkamispäivä = Some(date(2021, 9, 1)),
          vahvistus = None
        )
      ),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
        )
      )
    )

  def valmistunutYsiluokkalainenSaksalainenKoulu = valmistunutYsiluokkalainen.copy(
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.saksalainenKoulu))
  )

  def valmistunutKasiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30))
      ),
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30)),
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 30), opiskeluoikeusValmistunut)
      )
    )
  )

  def valmistunutYsiluokkalainenVsop = {
    val edellisetLisätiedot = valmistunutYsiluokkalainen
      .lisätiedot.getOrElse(PerusopetuksenOpiskeluoikeudenLisätiedot())

    valmistunutYsiluokkalainen.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        vuosiluokkiinSitoutumatonOpetus = true
      ))
    )
  }

  def ennenLainRajapäivääToisestaKoulustaValmistunutYsiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(oppilaitos(aapajoenKoulu)),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2020, 12, 31))
      ),
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 12, 31)),
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 12, 31), opiskeluoikeusValmistunut)
      )
    )
  )

  def yli2kkAiemminPeruskoulustaValmistunut = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 7, 4))
      ),
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2021, 7, 4)),
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 7, 4), opiskeluoikeusValmistunut)
      )
    )
  )

  def valmistunutYsiluokkalainenToinenKoulu =
    valmistunutYsiluokkalainen.copy(
      oppilaitos = Some(oppilaitos(aapajoenKoulu)),
      suoritukset = List(
        perusopetuksenOppimääränSuoritus.copy(
          vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 29))
        ),
        kahdeksannenLuokanSuoritus.copy(
          alkamispäivä = Some(date(2019, 8, 15)),
          vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 29)),
        ),
        yhdeksännenLuokanSuoritus.copy(
          alkamispäivä = Some(date(2020, 8, 15)),
          vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 29)),
        )
      ),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 14), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 29), opiskeluoikeusValmistunut)
        )
      )
    )

  def luokallejäänytYsiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        luokka = "9A",
        jääLuokalle = true,
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30))
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def luokallejäänytYsiluokkalainenJollaUusiYsiluokka = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      // Tarkoituksella väärässä aikajärjestyksessä, jotta tulee testattua paremmin
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2021, 8, 15)),
        vahvistus = None,
        luokka = "9B"
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        luokka = "9A",
        jääLuokalle = true,
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30))
      ),
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        luokka = "9A",
        jääLuokalle = true,
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30))
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 30), opiskeluoikeusEronnut)
      )
    )
  )

  def luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(oppilaitos(aapajoenKoulu)),
    koulutustoimija = None,
    suoritukset = List(
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2021, 8, 15)),
        vahvistus = None,
        luokka = "9B"
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2 = luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen.copy(
    oppilaitos = Some(oppilaitos(aapajoenKoulu))
  )
  def luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2 = luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen.copy(
    oppilaitos = Some(jyväskylänNormaalikoulu)
  )

  def kasiluokkaEronnutKeväällä2020Opiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(kulosaarenAlaAste),
    koulutustoimija = None,
    suoritukset = List(
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30))
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 5, 30), opiskeluoikeusEronnut),
      )
    )
  )

  def pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  // Päällekkäiset opiskeluoikeudet esimerkit

  def oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1 = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(kulosaarenAlaAste),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken.copy(
        toimipiste = kulosaarenAlaAste,
      ),
      kahdeksannenLuokanSuoritus.copy(
        toimipiste = kulosaarenAlaAste,
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
        luokka = "8A"
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2 = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None,
        luokka = "9B"
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def kesäYsiluokkaKesken = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2021, 6, 2)),
        vahvistus = None,
        luokka = "9D"
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 6, 2), opiskeluoikeusLäsnä)
      )
    )
  )

  def lukionOpiskeluoikeus = ExamplesLukio2019.aktiivinenOpiskeluoikeus

  def lukionOpiskeluoikeusValmistunut = ExamplesLukio2019.opiskeluoikeus.copy(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(alku = date(2021, 9, 2), tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )
    ),
  )

  def ammattikouluOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus,
      AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(
        vahvistus = None
      )
    )
  )

  def ammattikouluValmaOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesValma.valmaKoulutuksenSuoritus.copy(
        vahvistus = None
      )
    )
  )

  def ammattikouluTelmaOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesTelma.telmaKoulutuksenSuoritus.copy(
        vahvistus = None
      )
    )
  )

  def ammattikouluValmistunutOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2021, 9, 2), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(
      AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus,
      AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(
        vahvistus = vahvistus(date(2021, 9, 2), stadinAmmattiopisto, Some(helsinki))
      )
    )
  )

  def ammattikouluEronnutOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 8, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2021, 9, 2), opiskeluoikeusEronnut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 8, 1) , loppu = None, maksuton = true)))
    )),
    suoritukset = List(
      ammatillisenTutkinnonSuoritus2021.copy(
        vahvistus = None
      )
    )
  )

  def ammattikouluAlkaaOmniaSyys2021 = ammattikouluValmistunutOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.omnia)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true)))
    )),
    suoritukset = List(
      ammatillisenTutkinnonSuoritus2021.copy(
        toimipiste = Toimipiste(MockOrganisaatiot.omniaArbetarInstitutToimipiste),
        vahvistus = None
      )
    )
  )

  def ammattikouluAlkaaOmniaLoka2021 = ammattikouluValmistunutOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.omnia)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 10, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 10, 1) , loppu = None, maksuton = true)))
    )),
    suoritukset = List(
      ammatillisenTutkinnonSuoritus2021.copy(
        toimipiste = Toimipiste(MockOrganisaatiot.omniaArbetarInstitutToimipiste),
        vahvistus = None
      )
    )
  )

  lazy val ammatillisenTutkinnonSuoritus2021 = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = tutkinto,
    suoritustapa = suoritustapaNäyttö,
    järjestämismuodot = Some(List(
      Järjestämismuotojakso(date(2021, 8, 1), None, järjestämismuotoOppilaitos),
      Järjestämismuotojakso(date(2021, 8, 2), None, järjestämismuotoOppisopimus),
      Järjestämismuotojakso(date(2021, 8, 3), None, järjestämismuotoOppilaitos)
    )),
    suorituskieli = suomenKieli,
    alkamispäivä = None,
    toimipiste = stadinToimipiste,
    vahvistus = vahvistus(date(2021, 8, 5), stadinAmmattiopisto, Some(helsinki)),
    osasuoritukset = Some(List(
      tutkinnonOsanSuoritus("104052", "Johtaminen ja henkilöstön kehittäminen", None, hyväksytty),
      tutkinnonOsanSuoritus("104053", "Asiakaspalvelu ja korjaamopalvelujen markkinointi", None, hyväksytty),
      tutkinnonOsanSuoritus("104054", "Työnsuunnittelu ja organisointi", None, hyväksytty),
      tutkinnonOsanSuoritus("104055", "Taloudellinen toiminta", None, hyväksytty),
      tutkinnonOsanSuoritus("104059", "Yrittäjyys", None, hyväksytty)
    ))
  )


  def lukionOpiskeluoikeusAlkaa2021Syksyllä(
    maksuttomuus: Option[List[Maksuttomuus]] = Some(List(Maksuttomuus(alku = date(2021, 8, 15) , loppu = None, maksuton = true)))
  ) = {
    val oo = ExamplesLukio2019.aktiivinenOpiskeluoikeus
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        maksuttomuus = maksuttomuus
      )),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 8, 15), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      )
    )
  }

  def lukionOpiskeluoikeusAlkaaJaLoppuu2021Syksyllä(
    maksuttomuus: Option[List[Maksuttomuus]] = Some(List(Maksuttomuus(alku = date(2021, 8, 15) , loppu = Some(date(2021, 9, 19)), maksuton = true)))
  ) = {
    val oo = ExamplesLukio2019.aktiivinenOpiskeluoikeus
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        maksuttomuus = maksuttomuus
      )),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 8, 15), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(alku = date(2021, 9, 19), tila = opiskeluoikeusEronnut, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      )
    )
  }

  def lukionAineopintojenOpiskeluoikeusAlkaa2021Syksyllä(
    maksuttomuus: Option[List[Maksuttomuus]] = Some(List(Maksuttomuus(alku = date(2021, 10, 3) , loppu = None, maksuton = true)))
  ) = {
    val oo = ExamplesLukio2019.aktiivinenOppiaineenOppimääräOpiskeluoikeus
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    ExamplesLukio2019.aktiivinenOppiaineenOppimääräOpiskeluoikeus.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        maksuttomuus = maksuttomuus
      )),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 8, 15), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      )
    )
  }

  def lukionOpiskeluoikeusAlkaa2021Lokakuussa(
    maksuttomuus: Option[List[Maksuttomuus]] = Some(List(Maksuttomuus(alku = date(2021, 10, 3) , loppu = None, maksuton = true)))
  ) = {
    val oo = ExamplesLukio2019.aktiivinenOpiskeluoikeus
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        maksuttomuus = maksuttomuus
      )),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 10, 3), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      )
    )
  }

  def kasiluokkaKeskenKeväällä2021Opiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None,
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def kotiopetusMeneilläänOpiskeluoikeus = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      kotiopetusjaksot = Some(List(
        Aikajakso(alku = date(2020, 1, 1), loppu = None)
      ))
    ))
  )

  def kotiopetusMenneisyydessäOpiskeluoikeus = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      kotiopetusjaksot = Some(List(
        Aikajakso(alku = date(2020, 1, 1), loppu = Some(date(2020, 2, 1)))
      ))
    ))
  )

  def eronnutOpiskeluoikeusTarkastelupäivääEnnen = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(

    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 1, 1), opiskeluoikeusEronnut)
      )
    )
  )

  def eronnutOpiskeluoikeusTarkastelupäivänä = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 9, 5), opiskeluoikeusEronnut)
      )
    )
  )

  def eronnutOpiskeluoikeusTarkastelupäivänJälkeen = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 10, 5), opiskeluoikeusEronnut)
      )
    )
  )

  def eronnutOpiskeluoikeusEiYsiluokkaaKeväänAlussa = kasiluokkaEronnutKeväällä2020Opiskeluoikeus.copy(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 3, 3), opiskeluoikeusEronnut),
      )
    )
  )

  def eronnutOpiskeluoikeusEiYsiluokkaaKeväänJaksolla = kasiluokkaEronnutKeväällä2020Opiskeluoikeus.copy(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 30), opiskeluoikeusEronnut),
      )
    )
  )

  def eronnutOpiskeluoikeusEiYsiluokkaaElokuussa = kasiluokkaEronnutKeväällä2020Opiskeluoikeus.copy(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 8, 5), opiskeluoikeusEronnut),
      )
    )
  )

  def oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeenOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2021, 9, 15)),
        vahvistus = None
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 9, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 11, 5), opiskeluoikeusEronnut)
      )
    )
  )

  def kulosaarelainenYsiluokkalainenOpiskeluoikeus = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    oppilaitos = Some(kulosaarenAlaAste)
  )

  def esiopetuksenOpiskeluoikeus = ExamplesEsiopetus.opiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def valmistunutKymppiluokkalainen = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 31), opiskeluoikeusValmistunut)
      )
    )
  )

  def kymppiluokanOpiskeluoikeus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def kymppiluokkaAlkaaSyys2021 = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.saksalainenKoulu)),
    koulutustoimija = None,
    lisätiedot = Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true)))
    )),
    suoritukset = List(
      ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus.copy(vahvistus = None)
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä),
      )
    )
  )

  def valmaOpiskeluoikeusAlkaaOmniassaSyys2021 = ammattikouluValmistunutOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.omnia)),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true))),
      hojks = None
    )),
    suoritukset = List(
      ExamplesValma.valmaKoulutuksenSuoritus.copy(vahvistus = None),
      AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(
        vahvistus = None
      )
    )
  )

  def vstAlkaaSyys2021 = VapaaSivistystyöExample.opiskeluoikeusKOPS.copy(
    lisätiedot = Some(VapaanSivistystyönOpiskeluoikeudenLisätiedot(
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true))),
    ))
  )
}
