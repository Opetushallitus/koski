package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.documentation.AmmatillinenExampleData.stadinAmmattiopisto
import fi.oph.koski.documentation.DIAExampleData.saksalainenKoulu

import java.time.LocalDate
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesIB._
import fi.oph.koski.documentation.LukioExampleData.{opiskeluoikeusAktiivinen, opiskeluoikeusPäättynyt}
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, perusopetuksenOppimääränSuoritus, perusopetuksenOppimääränSuoritusKesken, seitsemännenLuokanSuoritus, suoritustapaErityinenTutkinto, yhdeksännenLuokanSuoritus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste, oppilaitos, ressunLukio}
import fi.oph.koski.documentation._
import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmattitutkintoExample, ExampleData, ExamplesEsiopetus, ExamplesInternationalSchool, ExamplesLukio2019, ExamplesPerusopetuksenLisaopetus, ExamplesTelma, ExamplesValma, InternationalSchoolExampleData, LukioExampleData, VapaaSivistystyöExample}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExamplesInternationalSchool.{grade1, grade10, grade11, grade12, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9, gradeExplorer}
import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus

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

  def oppivelvollinenYsiluokkaKeskenKeväällä2021OpiskeluoikeusPuuttuva7LuokanAlkamispäivä = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      seitsemännenLuokanSuoritus.copy(
        alkamispäivä = None,
        vahvistus = vahvistusPaikkakunnalla(date(2019, 5, 30)),
      ),
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

  def valmistunutYsiluokkalainenToukokuun15 = valmistunutYsiluokkalainen.copy(
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 15))
      ),
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 15)),
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 15), opiskeluoikeusValmistunut)
      )
    )
  )

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

  def keväänUlkopuolellaValmistunutYsiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 9, 1))
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2021, 9, 1)),
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusValmistunut)
      )
    )
  )

  def keväänUlkopuolellaEronnutYsiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = None
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusEronnut)
      )
    )
  )

  def valmistunutIlmanYsiluokkaa = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30)),
        suoritustapa = suoritustapaErityinenTutkinto
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 30), opiskeluoikeusValmistunut)
      )
    )
  )

  def valmistunutLokakuussaIlmanYsiluokkaa = {
    val valmistumispäivä = date(2021, 10, 1)

    PerusopetuksenOpiskeluoikeus(
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      suoritukset = List(
        perusopetuksenOppimääränSuoritus.copy(
          vahvistus = vahvistusPaikkakunnalla(valmistumispäivä),
          suoritustapa = suoritustapaErityinenTutkinto
        )
      ),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(valmistumispäivä, opiskeluoikeusValmistunut)
        )
      )
    )
  }

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
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.saksalainenKoulu)),
  )

  def valmistunutYsiluokkalainenSaksalainenKouluVäliaikaisestiKeskeytynytToukokuussa = valmistunutYsiluokkalainenSaksalainenKoulu.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 10), opiskeluoikeusValiaikaisestiKeskeytynyt),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 21), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 30), opiskeluoikeusValmistunut)
      )
    )
  )

  def valmistunutKasiluokkalainen = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(date(2021, 5, 30)),
        suoritustapa = suoritustapaErityinenTutkinto
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
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.aapajoenKoulu)),
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
      oppilaitos = Some(oppilaitos(MockOrganisaatiot.aapajoenKoulu)),
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

  def valmistunutYsiluokkalainenRessunLukio = valmistunutYsiluokkalainenToinenKoulu.copy(
      oppilaitos = Some(oppilaitos(MockOrganisaatiot.ressunLukio))
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
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.aapajoenKoulu)),
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
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.aapajoenKoulu))
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

  def ammattikouluOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus().copy(
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

  def ammattikouluValmaOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus().copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesValma.valmaKoulutuksenSuoritus.copy(
        vahvistus = None
      )
    )
  )

  def ammattikouluValmaOpiskeluoikeusEronnut = ammattikouluValmaOpiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2021, 11, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2022, 1, 9), opiskeluoikeusEronnut, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    arvioituPäättymispäivä = Some(LocalDate.of(2022, 1, 9))
  )

  def valmaRessussa = ammattikouluValmaOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    oppilaitos = Some(ressunLukio),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 8, 8), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesValma.valmaKoulutuksenSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = None
      )
    )
  )

  def valmaRessussaEronnut(alku: LocalDate = date(2021, 8, 8), loppu: LocalDate = date(2022, 5, 31)) = ammattikouluValmaOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(loppu),
    oppilaitos = Some(ressunLukio),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(alku, opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(loppu, opiskeluoikeusEronnut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(
      ExamplesValma.valmaKoulutuksenSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = None
      )
    )
  )

  def valmaRessussaValmistunut = ammattikouluValmistunutOpiskeluoikeus().copy(
    oppilaitos = Some(ressunLukio),
    suoritukset = List(
      ExamplesValma.valmaKoulutuksenSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = vahvistus(date(2021, 9, 2), AmmatillinenExampleData.stadinAmmattiopisto, Some(helsinki))
      )
    )
  )

  def ammattikouluTelmaOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus().copy(
    arvioituPäättymispäivä = None,
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2018, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesTelma.telmaKoulutuksenSuoritus.copy(
        vahvistus = None
      )
    )
  )

  def telmaRessussa = ammattikouluTelmaOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    oppilaitos = Some(ressunLukio),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 8, 9), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesTelma.telmaKoulutuksenSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = None
      )
    )
  )

  def telmaJaAmisRessussa = ammattikouluTelmaOpiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    oppilaitos = Some(ressunLukio),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 8, 9), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      ExamplesTelma.telmaKoulutuksenSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = None
      ),
      AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = None
      )
    )
  )

  def ammattikouluValmistunutOpiskeluoikeus(
    alkamispäivä: LocalDate = date(2012, 9, 1),
    päättymispäivä: LocalDate = date(2021, 9, 2)
  ) = AmmattitutkintoExample.opiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(päättymispäivä, opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    arvioituPäättymispäivä = Some(päättymispäivä),
    suoritukset = List(
      AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(
        alkamispäivä = Some(alkamispäivä),
        vahvistus = vahvistus(päättymispäivä, stadinAmmattiopisto, Some(helsinki)),
      ),
      AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(
        alkamispäivä = Some(alkamispäivä),
        vahvistus = vahvistus(päättymispäivä, stadinAmmattiopisto, Some(helsinki))
      )
    )
  )

  def ammattikouluValmistunutOsittainenOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeusOsittainen.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2021, 9, 2), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(
      AmmattitutkintoExample.ammatillisenTutkinnonOsanSuoritus.copy(
        vahvistus = vahvistus(date(2021, 9, 2), AmmatillinenExampleData.stadinAmmattiopisto, Some(helsinki))
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

  def ammattikouluAlkaaOmniaSyys2021: AmmatillinenOpiskeluoikeus = ammattikouluAlkaaOmnia(date(2021, 9, 1))

  def ammattikouluAlkaaOmniaLoka2021: AmmatillinenOpiskeluoikeus = ammattikouluAlkaaOmnia(date(2021, 10, 1))

  def ammattikouluAlkaaOmnia(alkamispäivä: LocalDate): AmmatillinenOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus().copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.omnia)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      maksuttomuus = Some(List(Maksuttomuus(alku = alkamispäivä , loppu = None, maksuton = true)))
    )),
    suoritukset = List(
      ammatillisenTutkinnonSuoritus2021.copy(
        toimipiste = Toimipiste(MockOrganisaatiot.omniaArbetarInstitutToimipiste),
        vahvistus = None
      )
    )
  )

  def ammattikouluAlkaaJaEroaaOmnia(alkamispäivä: LocalDate, päättymispäivä: LocalDate): AmmatillinenOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus().copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    oppilaitos = Some(oppilaitos(MockOrganisaatiot.omnia)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(päättymispäivä, opiskeluoikeusEronnut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      maksuttomuus = Some(List(Maksuttomuus(alku = alkamispäivä , loppu = None, maksuton = true)))
    )),
    suoritukset = List(
      ammatillisenTutkinnonSuoritus2021.copy(
        toimipiste = Toimipiste(MockOrganisaatiot.omniaArbetarInstitutToimipiste),
        vahvistus = None
      )
    )
  )


  def ammattikouluLomallaOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeus.copy(
    arvioituPäättymispäivä = Some(date(2023, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2021, 8, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2021, 8, 2), opiskeluoikeusLoma, Some(ExampleData.valtionosuusRahoitteinen))
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

  def ammattikouluMaksuttomuuttaPidennetty = ammattikouluAlkaaOmniaSyys2021.copy(
    oppilaitos = Some(stadinAmmattiopisto),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true))),
      oikeuttaMaksuttomuuteenPidennetty = Some(List(
        OikeuttaMaksuttomuuteenPidennetty(alku = date(2021, 9, 1), loppu = date(2023, 12, 31)),
      ))
    )),
  )

  lazy val ammatillisenTutkinnonSuoritus2021 = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = AmmattitutkintoExample.tutkinto,
    suoritustapa = AmmatillinenExampleData.suoritustapaNäyttö,
    järjestämismuodot = Some(List(
      Järjestämismuotojakso(date(2021, 8, 1), None, AmmatillinenExampleData.järjestämismuotoOppilaitos),
      Järjestämismuotojakso(date(2021, 8, 2), None, AmmatillinenExampleData.järjestämismuotoOppisopimus),
      Järjestämismuotojakso(date(2021, 8, 3), None, AmmatillinenExampleData.järjestämismuotoOppilaitos)
    )),
    suorituskieli = suomenKieli,
    alkamispäivä = None,
    toimipiste = AmmatillinenExampleData.stadinToimipiste,
    vahvistus = vahvistus(date(2021, 8, 5), AmmatillinenExampleData.stadinAmmattiopisto, Some(helsinki)),
    osasuoritukset = Some(List(
      AmmatillinenExampleData.tutkinnonOsanSuoritus("104052", "Johtaminen ja henkilöstön kehittäminen", None, AmmatillinenExampleData.hyväksytty),
      AmmatillinenExampleData.tutkinnonOsanSuoritus("104053", "Asiakaspalvelu ja korjaamopalvelujen markkinointi", None, AmmatillinenExampleData.hyväksytty),
      AmmatillinenExampleData.tutkinnonOsanSuoritus("104054", "Työnsuunnittelu ja organisointi", None, AmmatillinenExampleData.hyväksytty),
      AmmatillinenExampleData.tutkinnonOsanSuoritus("104055", "Taloudellinen toiminta", None, AmmatillinenExampleData.hyväksytty),
      AmmatillinenExampleData.tutkinnonOsanSuoritus("104059", "Yrittäjyys", None, AmmatillinenExampleData.hyväksytty)
    ))
  )


  def lukionOpiskeluoikeusAlkaa2021Syksyllä(
    maksuttomuus: Option[List[Maksuttomuus]] = Some(List(
      Maksuttomuus(alku = date(2021, 8, 15), loppu = Some(date(2021, 8, 16)), maksuton = true),
      Maksuttomuus(alku = date(2021, 8, 17), loppu = Some(date(2021, 8, 18)), maksuton = false),
      Maksuttomuus(alku = date(2021, 8, 19), loppu = None, maksuton = true),
    ))
  ) = {
    val oo = ExamplesLukio2019.aktiivinenOpiskeluoikeus
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    oo.copy(
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

    oo.copy(
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

    oo.copy(
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

    oo.copy(
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

  def lukionVanhanOpsinOpiskeluoikeusAlkaa2021Keväällä(
    maksuttomuus: Option[List[Maksuttomuus]] = Some(List(Maksuttomuus(alku = date(2021, 8, 1) , loppu = None, maksuton = true)))
  ) = {
    val oo = ExamplesLukio.lukioKesken
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    oo.copy(
      lisätiedot = Some(edellisetLisätiedot.copy(
        maksuttomuus = maksuttomuus
      )),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 3, 3), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
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

  def kasiluokkaKeskeytetty2021Opiskeluoikeus = kasiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 3, 1), opiskeluoikeusValiaikaisestiKeskeytynyt)
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

  def kotiopetusMeneilläänVanhallaRakenteellaOpiskeluoikeus = oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      kotiopetus = Some(Aikajakso(alku = date(2020, 1, 1), loppu = None))
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

  def valmistunutKymppiluokkalainen = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 31), opiskeluoikeusValmistunut)
      )
    )
  )

  def valmistunutKymppiluokkalainenKeväällä2022 = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusValmistunut)
      )
    ),
    lisätiedot = None
  )

  def alkukesästäEronnutKymppiluokkalainen = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 31), opiskeluoikeusEronnut)
      )
    ),
    suoritukset = List(lisäopetuksenSuoritus.copy(vahvistus = None)),
    lisätiedot = None
  )

  def alkukesästäEronneeksiKatsottuKymppiluokkalainen = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 5, 31), opiskeluoikeusKatsotaanEronneeksi)
      )
    ),
    suoritukset = List(lisäopetuksenSuoritus.copy(vahvistus = None)),
    lisätiedot = None
  )

  def alkuvuodestaEronnutKymppiluokkalainen = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 1, 31), opiskeluoikeusEronnut)
      )
    ),
    suoritukset = List(lisäopetuksenSuoritus.copy(vahvistus = None)),
    lisätiedot = None
  )

  def alkuvuodestaEronneeksiKatsottuKymppiluokkalainen = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2020, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 1, 31), opiskeluoikeusKatsotaanEronneeksi)
      )
    ),
    suoritukset = List(lisäopetuksenSuoritus.copy(vahvistus = None)),
    lisätiedot = None
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

  def kymppiluokkaRessussa = kymppiluokkaAlkaaSyys2021.copy(
    oppilaitos = Some(ressunLukio),
    suoritukset = List(
      ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus.copy(
        toimipiste = ressunLukio,
        vahvistus = None
      )
    )
  )

  def valmaOpiskeluoikeusAlkaaOmniassaSyys2021 = ammattikouluValmistunutOpiskeluoikeus().copy(
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
      ExamplesValma.valmaKoulutuksenSuoritus.copy(vahvistus = None)
    )
  )

  def vstAlkaaSyys2021 = VapaaSivistystyöExample.opiskeluoikeusKOPS.copy(
    lisätiedot = Some(VapaanSivistystyönOpiskeluoikeudenLisätiedot(
      maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true))),
    ))
  )

  def vstKopsRessussa = vstAlkaaSyys2021.copy(
    oppilaitos = Some(ressunLukio),
    suoritukset = List(VapaaSivistystyöExample.suoritusKOPS.copy(
      toimipiste = ressunLukio,
      vahvistus = None
    ))
  )

  def amisAmmatillinenJaNäyttötutkintoonValmistavaOpiskeluoikeus = ammattikouluValmistunutOpiskeluoikeus().copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(
        vahvistus = None,
        toimipiste = AmmatillinenExampleData.stadinToimipiste,
        ryhmä = Some("A")
      ),
      AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(
        vahvistus = None,
        toimipiste = AmmatillinenExampleData.stadinAmmattiopisto,
        ryhmä = Some("B")
      )
    )
  )

  def lukionVäliaikaisestiKeskeytettyOpiskeluoikeus = ExamplesLukio2019.opiskeluoikeus.copy(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date(2021, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(alku = date(2021, 8, 2), tila = opiskeluoikeusValiaikaisestiKeskeytynyt, opintojenRahoitus = None),
      )
    ),
  )

  def internationalSchool9LuokaltaValmistunut2020 = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2020, 5, 30))))
  )

  def internationalSchool9LuokaltaValmistunut2021 = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 5, 30))))
  )

  def oppivelvollinenIntSchoolYsiluokkaKeskenKeväällä2021Opiskeluoikeus = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(
       alkamispäivä = Some(date(2020, 8, 1)),
       vahvistus = None
      )
    )
  )

  def internationalSchool10LuokaltaAloittanut = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 1, 1), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(
      grade10.copy(
        alkamispäivä = Some(date(2021, 1, 1)),
        vahvistus = None
      )
    )
  )

  def internationalSchool11LuokaltaAloittanut = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 1, 1), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(
      grade11.copy(
        alkamispäivä = Some(date(2021, 1, 1)),
        vahvistus = None
      )
    )
  )

  def internationalSchool8LuokanSyksyllä2021Aloittanut = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6,
      grade7.copy(
        vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 5, 30))
      ),
      grade8.copy(
        alkamispäivä = Some(date(2021, 8, 1)),
        vahvistus = None
      )
    )
  )

  def internationalSchool9LuokanSyksyllä2021Aloittanut = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7,
      grade8.copy(
        vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 5, 30))
      ),
      grade9.copy(
        alkamispäivä = Some(date(2021, 8, 1)),
        vahvistus = None
      )
    )
  )

  def intSchoolKasiluokkaKeskenKeväällä2021Opiskeluoikeus = ExamplesInternationalSchool.opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7,
      grade8.copy(
        alkamispäivä = Some(date(2020, 8, 1)),
        vahvistus = None
      )
    )
  )

  def intSchool9LuokaltaKeskenEronnutOpiskeluoikeusTarkastelupäivääEnnen = oppivelvollinenIntSchoolYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 1, 1), opiskeluoikeusEronnut)
      )
    )
  )

  def intSchool9LuokaltaValmistumisenJälkeenEronnutOpiskeluoikeusTarkastelupäivääEnnen = internationalSchool9LuokaltaValmistunut2021.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 1, 1), opiskeluoikeusEronnut)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 1, 1))))
  )

  def intSchool9LuokaltaKeskenEronnutOpiskeluoikeusTarkastelupäivänä = oppivelvollinenIntSchoolYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 9, 5), opiskeluoikeusEronnut)
      )
    )
  )

  def intSchool9LuokaltaValmistumisenJälkeenEronnutOpiskeluoikeusTarkastelupäivänä = internationalSchool9LuokaltaValmistunut2021.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 9, 5), opiskeluoikeusEronnut)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 9, 5))))
  )

  def intSchool9LuokaltaKeskenEronnutOpiskeluoikeusTarkastelupäivänJälkeen = oppivelvollinenIntSchoolYsiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 10, 5), opiskeluoikeusEronnut)
      )
    )
  )

  def intSchool9LuokaltaValmistumisenJälkeenEronnutOpiskeluoikeusTarkastelupäivänJälkeen = internationalSchool9LuokaltaValmistunut2021.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 10, 5), opiskeluoikeusEronnut)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 10, 5))))
  )

  def intSchool9LuokaltaValmistunut2021ja10LuokallaAloittanut = internationalSchool9LuokaltaValmistunut2021.copy(
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 5, 30))),
      grade10.copy(
        alkamispäivä = Some(date(2021, 8, 1)),
        vahvistus = None
      )
    )
  )

  def intSchool9LuokaltaValmistunutLokakuussa2021ja10LuokallaAloittanut = internationalSchool9LuokaltaValmistunut2021.copy(
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 10, 1))),
      grade10.copy(
        alkamispäivä = Some(date(2021, 10, 15)),
        vahvistus = None
      )
    )
  )

  def intSchool9LuokaltaValmistunut2021ja10LuokallaLokakuussaAloittanut = internationalSchool9LuokaltaValmistunut2021.copy(
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 5, 30))),
      grade10.copy(
        alkamispäivä = Some(date(2021, 10, 3)),
        vahvistus = None
      )
    )
  )

  def intSchool9LuokaltaValmistunut2021ja10LuokallaIlmanAlkamispäivää = internationalSchool9LuokaltaValmistunut2021.copy(
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 5, 30))),
      grade10.copy(
        alkamispäivä = None,
        vahvistus = None
      )
    )
  )

  def yli2kkAiemminIntSchoolin9LuokaltaValmistunut  = internationalSchool9LuokaltaValmistunut2021.copy(
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 7, 4)))
    )
  )

  def yli2kkAiemminIntSchoolin9LuokaltaValmistunut10Jatkanut  = internationalSchool9LuokaltaValmistunut2021.copy(
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8,
      grade9.copy(vahvistus = InternationalSchoolExampleData.vahvistus(date(2021, 7, 4))),
      grade10.copy(
        alkamispäivä = Some(date(2021, 8, 1)),
        vahvistus = None
      )
    )
  )

  def intSchoolistaEronnutOpiskeluoikeusEiYsiluokkaaKeväänAlussa = intSchoolKasiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 3, 3), opiskeluoikeusEronnut),
      )
    )
  )

  def intSchoolistaEronnutOpiskeluoikeusEiYsiluokkaaElokuussa = intSchoolKasiluokkaKeskenKeväällä2021Opiskeluoikeus.copy(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), opiskeluoikeusLäsnä),
        InternationalSchoolOpiskeluoikeusjakso(date(2021, 8, 5), opiskeluoikeusEronnut),
      )
    )
  )

  def aikuistenPerusopetuksessa: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(date(2021,8, 15), None)
  def aikuistenPerusopetuksessaSyksynRajapäivänJälkeenAloittava: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(date(2021, 10, 1), None)
  def aikuistenPerusopetuksestaKeväänValmistujaksollaValmistunut: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(date(2021,1, 1), Some(date(2021, 5, 29)))
  def aikuistenPerusopetuksestaEronnut: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa.copy(
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        List(
          AikuistenPerusopetuksenOpiskeluoikeusjakso(
            date(2021, 8, 15), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)
          ),
          AikuistenPerusopetuksenOpiskeluoikeusjakso(
            date(2021, 8, 30), opiskeluoikeusEronnut, Some(valtionosuusRahoitteinen)
          ),
        )
      ),
    )
  def aikuistenPerusopetuksestaYli2kkAiemminValmistunut: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(
      date(2021,1, 1), Some(date(2021, 7, 4))
    )
  def aikuistenPerusopetuksestaAlle2kkAiemminValmistunut: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(
      date(2021,1, 1), Some(date(2021, 8, 10))
    )
  def aikuistenPerusopetuksestaLähitulevaisuudessaValmistuva: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(
      date(2021,1, 1), Some(date(2021, 10, 1))
    )
  def aikuistenPerusopetuksestaTulevaisuudessaValmistuva: AikuistenPerusopetuksenOpiskeluoikeus =
    aikuistenPerusopetuksessa(
      date(2021,1, 1), Some(date(2021, 12, 10))
    )
  def aikuistenPerusopetuksessaAineopiskelija: AikuistenPerusopetuksenOpiskeluoikeus =
    ExamplesAikuistenPerusopetus.oppiaineenOppimääräOpiskeluoikeus.copy(
      oppilaitos = Some(ressunLukio),
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        List(
          AikuistenPerusopetuksenOpiskeluoikeusjakso(
            date(2021, 8, 15), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)
          )
        )
      ),
      suoritukset = List(
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritus(
          ExamplesAikuistenPerusopetus.äidinkieli("AI1", diaarinumero = Some("19/011/2015"))
        ).copy(
          toimipiste = ressunLukio,
          vahvistus = None
        )
      )
    )
  private def aikuistenPerusopetuksessa(
    alkamispäivä: LocalDate,
    valmistumispäivä: Option[LocalDate]
  ): AikuistenPerusopetuksenOpiskeluoikeus =
  {
    val valmistunut: Option[AikuistenPerusopetuksenOpiskeluoikeusjakso] =
      valmistumispäivä.map(vp =>
        AikuistenPerusopetuksenOpiskeluoikeusjakso(vp, opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
      )

    val tilat: List[AikuistenPerusopetuksenOpiskeluoikeusjakso] = List(
      AikuistenPerusopetuksenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))
    ) ++ valmistunut.toList

    ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen.copy(
      oppilaitos = Some(ressunLukio),
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        tilat
      ),
      suoritukset = List(
        ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
          toimipiste = ressunLukio,
          vahvistus = valmistumispäivä.flatMap(vp => vahvistusPaikkakunnalla(vp))
        ),
        ExamplesAikuistenPerusopetus.aikuistenPerusopetukseOppimääränSuoritus(
          ExamplesAikuistenPerusopetus.aikuistenPerusopetus2017,
          ExamplesAikuistenPerusopetus.oppiaineidenSuoritukset2017
        ).copy(
          toimipiste = ressunLukio,
          vahvistus = valmistumispäivä.flatMap(vp => vahvistusPaikkakunnalla(vp))
        )
      )
    )
  }

  def luva: LukioonValmistavanKoulutuksenOpiskeluoikeus = LukioonValmistavanKoulutuksenOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    koulutustoimija = None,
    tila = LukionOpiskeluoikeudenTila(List(
      LukionOpiskeluoikeusjakso(
        date(2021, 8, 15), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)
      )
    )),
    suoritukset = List(ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenSuoritus2019.copy(
      toimipiste = ressunLukio,
      vahvistus = None
    )),
    lisätiedot = None
  )

  def esiopetusLäsnäOpiskeluoikeus = ExamplesEsiopetus.opiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2021, 8, 13), opiskeluoikeusLäsnä),
      )
    ),
    lisätiedot = Some(ExamplesEsiopetus.lisätiedot.copy(
      pidennettyOppivelvollisuus = None,
      vammainen = None,
      vaikeastiVammainen = None
    ))
  )

  def esiopetusValmistunutOpiskeluoikeus = ExamplesEsiopetus.opiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2010, 8, 13), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2015, 1, 1), opiskeluoikeusValmistunut)
      )
    ),
    suoritukset = ExamplesEsiopetus.opiskeluoikeus.suoritukset.map(s => s.copy(vahvistus = vahvistusPaikkakunnalla(date(2007, 6, 3))))
  )

  def perusopetukseenValmistavanOpetuksenOpiskeluoikeusAlkaaSyys2021 = perusopetukseenValmistavanOpetuksenOpiskeluoikeus.copy(
    tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2021, 9, 1), opiskeluoikeusLäsnä),
    )),
  )

  def perusopetukseenValmistavanOpetuksenOpiskeluoikeus = ExamplesPerusopetukseenValmistavaOpetus.perusopetukseenValmistavaOpiskeluoikeus.copy(
    tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2021, 5, 1), opiskeluoikeusLäsnä),
    )),
  )

  def perusopetukseenValmistavanOpetuksenOpiskeluoikeusValmistunut = perusopetukseenValmistavanOpetuksenOpiskeluoikeus.copy(
    tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2019, 9, 4), opiskeluoikeusLäsnä),
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2021, 5, 31), opiskeluoikeusValmistunut)
    )),
  )

  def perusopetukseenValmistavanOpetuksenOpiskeluoikeusEronnut = perusopetukseenValmistavanOpetuksenOpiskeluoikeus.copy(
    tila = PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(List(
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2019, 9, 5), opiskeluoikeusLäsnä),
      PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(date(2021, 8, 27), opiskeluoikeusEronnut)
    )),
  )

  def ibOpiskeluoikeusPreIbSuoritus = {
    val maksuttomuus: Option[List[Maksuttomuus]] = Some(List(
      Maksuttomuus(alku = date(2021, 6, 1), loppu = None, maksuton = true),
    ))
    val oo = ExamplesLukio2019.aktiivinenOpiskeluoikeus
    val edellisetLisätiedot = oo.lisätiedot.getOrElse(LukionOpiskeluoikeudenLisätiedot())

    IBOpiskeluoikeus(
      oppilaitos = Some(jyväskylänNormaalikoulu),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(date(2021, 6, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      suoritukset = List(
        preIBSuoritus.copy(
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistusPaikkakunnalla(päivä = date(2021, 6, 1), org = jyväskylänNormaalikoulu, kunta = jyväskylä)
        ),
      ),
      lisätiedot = Some(edellisetLisätiedot.copy(
        maksuttomuus = maksuttomuus
      ))
    )
  }

  def oppivelvollinenYsiluokkaKeskenKeväällä2021SaksalainenKouluOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(saksalainenKoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken.copy(
        toimipiste = saksalainenKoulu,
      ),
      kahdeksannenLuokanSuoritus.copy(
        toimipiste = saksalainenKoulu,
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(
          päivä = date(2020, 5, 30),
          org = saksalainenKoulu,
          kunta = helsinki,
        ),
      ),
      yhdeksännenLuokanSuoritus.copy(
        toimipiste = saksalainenKoulu,
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

  def tuvaOpiskeluoikeusKesken = ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusEiValmistunut
  def tuvaOpiskeluoikeusValmis = ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusValmistunut.copy(
    tila = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
      opiskeluoikeusjaksot = List(
        ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusjakso(date(2020, 1, 1), "lasna"),
        ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusjakso(date(2022, 8, 1), "valmistunut")
      )
    ),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija =  None,
    lisätiedot = None,
    suoritukset = List(
      ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaPäätasonSuoritus(Some(12.0), jyväskylänNormaalikoulu).copy(
        osasuoritukset = ExamplesTutkintokoulutukseenValmentavaKoulutus
          .tuvaOpiskeluOikeusValmistunut
          .suoritukset.headOption
          .map(t => t.osasuoritusLista.map {
            case t: TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus => t
          })
      )
    )
  )
}
