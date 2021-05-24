package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.LukioExampleData.opiskeluoikeusAktiivinen
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, perusopetuksenOppimääränSuoritus, perusopetuksenOppimääränSuoritusKesken, yhdeksännenLuokanSuoritus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste, oppilaitos}
import fi.oph.koski.documentation.{ExampleData, ExamplesEsiopetus, ExamplesLukio2019, ExamplesPerusopetuksenLisaopetus}
import fi.oph.koski.organisaatio.MockOrganisaatiot.aapajoenKoulu
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

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

  def lukionOpiskeluoikeusAlkaa2021Syksyllä = ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date(2021, 8, 15), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )
    )
  )

  def lukionAineopintojenOpiskeluoikeusAlkaa2021Syksyllä = ExamplesLukio2019.aktiivinenOppiaineenOppimääräOpiskeluoikeus.copy(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date(2021, 8, 15), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )
    )
  )

  def lukionOpiskeluoikeusAlkaa2021Lokakuussa = ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = date(2021, 10, 3), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )
    )
  )

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

  def kymppiluokanOpiskeluoikeus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )
}
