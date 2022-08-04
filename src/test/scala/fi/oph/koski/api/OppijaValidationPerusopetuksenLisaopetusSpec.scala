package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä}
import fi.oph.koski.documentation.ExamplesEsiopetus.osaAikainenErityisopetus
import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus
import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus
import fi.oph.koski.documentation.OsaAikainenErityisopetusExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetuksenLisäopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenLisäopetuksenOpiskeluoikeus] with KoskiHttpSpec {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    lisäopetuksenSuoritus.copy(koulutusmoduuli = lisäopetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
  override def tag: TypeTag[PerusopetuksenLisäopetuksenOpiskeluoikeus] = implicitly[TypeTag[PerusopetuksenLisäopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus
  def defaultLisäopetuksenSuoritus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus

  "Osa-aikainen erityisopetus" - {
    "Opiskeluoikeudella on erityisen tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on tehostetun tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaTehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta
      )) {
        verifyResponseStatusOk()
      }
    }
  }

  "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusEronnut)
      )))
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
    }
  }

  "Opiskeluoikeuden tilan alkupäivämäärä ei voi olla päiväys 31.8.2022 jälkeen" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2022, 9, 1), opiskeluoikeusLäsnä)
      ))).withSuoritukset(
      List(defaultLisäopetuksenSuoritus)
    )
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.perusopetuksenLisäopetuksenTilaEiSallittu())
    }
  }

  "Deprekoituja kenttiä, jotka tiputetaan siirrossa pois" - {
    "Lisätiedon kenttiä perusopetuksenAloittamistaLykatty, aloittanut ennen oppivelvollisuutta, tukimuodot," +
      "tehostetun tuen päätös, vuosiluokkiin sitoutumaton opetus ja joustava perusopetus ei oteta vastaan siirrossa" in {
      val oo = defaultOpiskeluoikeus.withLisätiedot(
        Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
          perusopetuksenAloittamistaLykätty = Some(true),
          aloittanutEnnenOppivelvollisuutta = Some(true),
          tukimuodot = Some(List(osaAikainenErityisopetus)),
          tehostetunTuenPäätös = Some(tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta),
          tehostetunTuenPäätökset = Some(List(tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta)),
          vuosiluokkiinSitoutumatonOpetus = Some(true),
          joustavaPerusopetus = Some(Aikajakso(LocalDate.of(2010, 8, 14), None))
        )
        ))

      val tallennettuna = putAndGetOpiskeluoikeus(oo)

      tallennettuna.lisätiedot.get.perusopetuksenAloittamistaLykätty should equal (None)
      tallennettuna.lisätiedot.get.aloittanutEnnenOppivelvollisuutta should equal (None)
      tallennettuna.lisätiedot.get.tukimuodot should equal (None)
      tallennettuna.lisätiedot.get.tehostetunTuenPäätös should equal (None)
      tallennettuna.lisätiedot.get.tehostetunTuenPäätökset should equal (None)
      tallennettuna.lisätiedot.get.vuosiluokkiinSitoutumatonOpetus should equal (None)
      tallennettuna.lisätiedot.get.joustavaPerusopetus should equal (None)
    }

    "Suorituksen kenttää osaAikainenErityisopetus ei oteta vastaan siirrossa - kenttä riippuu tehostetunTuenPäätöksestä" in {
      val oo = defaultOpiskeluoikeus.withSuoritukset(
        List(defaultLisäopetuksenSuoritus.copy(
          osaAikainenErityisopetus = Some(true)
        ))
      )

      val tallennettuna = putAndGetOpiskeluoikeus(oo)

      tallennettuna.suoritukset.head.osaAikainenErityisopetus should equal (None)
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): PerusopetuksenLisäopetuksenOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[PerusopetuksenLisäopetuksenOpiskeluoikeus]
}
