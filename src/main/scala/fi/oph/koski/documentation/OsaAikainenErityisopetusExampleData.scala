package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object OsaAikainenErityisopetusExampleData {
  private def alkuPvm = date(2008, 8, 15)
  private def loppuPvm = date(2016, 6, 4)

  private def tukimuodotIlmanOsaAikaistaErityisopetusta = Some(List(
    Koodistokoodiviite("3", "perusopetuksentukimuoto"),
    Koodistokoodiviite("2", "perusopetuksentukimuoto")
  ))

  private def tukimuodotJoissaOsaAikainenErityisopetus =  Some(List(
    Koodistokoodiviite("3", "perusopetuksentukimuoto"),
    Koodistokoodiviite("4", "perusopetuksentukimuoto"),
    Koodistokoodiviite("1", "perusopetuksentukimuoto")
  ))

  def erityisenTuenPäätösIlmanOsaAikaistaErityisopetusta = ErityisenTuenPäätös(
    alku = Some(alkuPvm),
    loppu = Some(loppuPvm),
    erityisryhmässä = Some(false),
    tukimuodot = tukimuodotIlmanOsaAikaistaErityisopetusta
  )

  def tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta = TehostetunTuenPäätös(
    alku = alkuPvm,
    loppu = Some(loppuPvm),
    tukimuodot = tukimuodotIlmanOsaAikaistaErityisopetusta
  )

  def erityisenTuenPäätösJossaOsaAikainenErityisopetus =
    erityisenTuenPäätösIlmanOsaAikaistaErityisopetusta.copy(tukimuodot = tukimuodotJoissaOsaAikainenErityisopetus)

  def tehostetunTuenPäätösJossaOsaAikainenErityisopetus =
    tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta.copy(tukimuodot = tukimuodotJoissaOsaAikainenErityisopetus)

  def perusopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä =
    Some(PerusopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.erityisenTuenPäätösJossaOsaAikainenErityisopetus))
    ))

  def perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä =
    Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.erityisenTuenPäätösJossaOsaAikainenErityisopetus))
    ))

  def perusopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusTehostetunTuenPäätöksessä =
    Some(PerusopetuksenOpiskeluoikeudenLisätiedot(tehostetunTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.tehostetunTuenPäätösJossaOsaAikainenErityisopetus))
    ))

  def perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusTehostetunTuenPäätöksessä =
    Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(tehostetunTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.tehostetunTuenPäätösJossaOsaAikainenErityisopetus))
    ))

  def perusopetuksenOpiskeluoikeudenLisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta =
    Some(PerusopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.erityisenTuenPäätösIlmanOsaAikaistaErityisopetusta))))

  def perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta =
    Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.erityisenTuenPäätösIlmanOsaAikaistaErityisopetusta))))

  def perusopetuksenOpiskeluoikeudenLisätiedotJoissaTehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta =
    Some(PerusopetuksenOpiskeluoikeudenLisätiedot(tehostetunTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta))))

  def perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaTehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta =
    Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(tehostetunTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta))))
}
