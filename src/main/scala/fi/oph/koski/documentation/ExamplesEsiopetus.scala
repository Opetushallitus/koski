package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{helsinki, _}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesEsiopetus {
  val peruskoulunEsiopetuksenTunniste = "001101"
  val päiväkodinEsiopetuksenTunniste = "001102"
  lazy val osaAikainenErityisopetus = Koodistokoodiviite("1", Some("Osa-aikainen erityisopetus"), "perusopetuksentukimuoto")

  lazy val lisätiedot = EsiopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(Aikajakso(date(2014, 8, 15), Some(date(2016, 6, 4)))),
    vammainen = Some(List(Aikajakso(date(2010, 8, 14), None))),
    vaikeastiVammainen = Some(List(Aikajakso(date(2014, 6, 6), None))),
    majoitusetu = Some(Aikajakso(date(2011, 8, 14), Some(date(2012, 8, 14)))),
    kuljetusetu = Some(Aikajakso(date(2011, 8, 14), Some(date(2012, 8, 14)))),
    sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    koulukoti = Some(List(Aikajakso(date(2011, 8, 14), None)))
  )

  lazy val esiopetusaikaisetLisätiedot = EsiopetuksenOpiskeluoikeudenLisätiedot(
    tukimuodot = Some(List(osaAikainenErityisopetus)),
    pidennettyOppivelvollisuus = Some(Aikajakso(date(2006, 8, 15), Some(date(2016, 6, 4)))),
    vammainen = Some(List(Aikajakso(date(2006, 8, 14), None))),
    vaikeastiVammainen = Some(List(Aikajakso(date(2006, 6, 6), None))),
    majoitusetu = Some(Aikajakso(date(2006, 8, 14), Some(date(2012, 8, 14)))),
    kuljetusetu = Some(Aikajakso(date(2006, 8, 14), Some(date(2012, 8, 14)))),
    sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2006, 9, 1), Some(date(2013, 9, 1))))),
    koulukoti = Some(List(Aikajakso(date(2006, 8, 14), None)))
  )

  lazy val opiskeluoikeus = EsiopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, toimipiste = jyväskylänNormaalikoulu)),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2006, 8, 13), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2007, 6, 3), opiskeluoikeusValmistunut)
      )
    ),
    lisätiedot = Some(lisätiedot)
  )

  lazy val opiskeluoikeusAikaisillaLisätiedoilla = EsiopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, toimipiste = jyväskylänNormaalikoulu)),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2006, 8, 13), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2007, 6, 3), opiskeluoikeusValmistunut)
      )
    ),
    lisätiedot = Some(esiopetusaikaisetLisätiedot)
  )

  lazy val ostopalveluOpiskeluoikeus = EsiopetuksenOpiskeluoikeus(
    oppilaitos = None,
    suoritukset = List(päiväkotisuoritus(päiväkotiTouhula).copy(vahvistus = None, muutSuorituskielet = None)),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2006, 8, 12), opiskeluoikeusLäsnä)
      )
    ),
    järjestämismuoto = ostopalvelu
  )

  lazy val ostopalvelu = Some(Koodistokoodiviite("JM02", "vardajarjestamismuoto"))

  lazy val opiskeluoikeusHelsingissä: EsiopetuksenOpiskeluoikeus = opiskeluoikeus.copy(
    oppilaitos = Some(kulosaarenAlaAste),
    suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, toimipiste = kulosaarenAlaAste))
  )

  val esioppilas = Oppija(
    exampleHenkilö,
    List(opiskeluoikeus)
  )

  val esioppilasAikaisillaLisätiedoilla = Oppija(
    exampleHenkilö,
    List(opiskeluoikeusAikaisillaLisätiedoilla)
  )

  val examples = List(
    Example("esiopetus valmis", "Oppija on suorittanut peruskoulun esiopetuksen", esioppilas),
    Example("esiopetus - ostopalvelu", "Oppija on suorittanut päiväkodin esiopetuksen ostopalveluna", esioppilas.copy(opiskeluoikeudet = List(ostopalveluOpiskeluoikeus.copy(koulutustoimija = Some(helsinki)))), statusCode = 403)
  )

  def päiväkotisuoritus(toimipiste: OrganisaatioWithOid): EsiopetuksenSuoritus =
    suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = päiväkodinEsiopetuksenTunniste, toimipiste = toimipiste)

  lazy val osaAikainenErityisopetusLukuvuodenAikanaLV1 =
    Koodistokoodiviite("LV1", Some("Osa-aikainen erityisopetus lukuvuoden aikana"), "osaaikainenerityisopetuslukuvuodenaikana")

  def suoritus(perusteenDiaarinumero: String, tunniste: String, toimipiste: OrganisaatioWithOid) = EsiopetuksenSuoritus(
    koulutusmoduuli = Esiopetus(
      kuvaus = Some("Kaksikielinen esiopetus (suomi-portugali)"),
      perusteenDiaarinumero = Some(perusteenDiaarinumero),
      tunniste = Koodistokoodiviite(tunniste, koodistoUri = "koulutus")
    ),
    toimipiste = toimipiste,
    suorituskieli = suomenKieli,
    muutSuorituskielet = Some(List(ruotsinKieli)),
    vahvistus = vahvistusPaikkakunnalla(date(2007, 6, 3)),
    osaAikainenErityisopetus = Some(List(osaAikainenErityisopetusLukuvuodenAikanaLV1))
  )
}

