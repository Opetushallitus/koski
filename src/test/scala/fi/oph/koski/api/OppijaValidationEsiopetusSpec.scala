package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä}
import fi.oph.koski.documentation.ExamplesEsiopetus.osaAikainenErityisopetus
import fi.oph.koski.documentation.ExamplesPerusopetus.{erityisenTuenPäätös, osaAikainenErityisopetus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.kulosaarenAlaAste
import fi.oph.koski.documentation.{ExamplesEsiopetus, OsaAikainenErityisopetusExampleData, YleissivistavakoulutusExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate

class OppijaValidationEsiopetusSpec extends TutkinnonPerusteetTest[EsiopetuksenOpiskeluoikeus] with EsiopetusSpecification {
  "Peruskoulun esiopetus -> HTTP 200" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus) {
      verifyResponseStatusOk()
    }
  }

  "Päiväkodin esiopetus -> HTTP 200" in {
    putOpiskeluoikeus(päiväkodinEsiopetuksenOpiskeluoikeus) {
      verifyResponseStatusOk()
    }
  }

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    peruskoulunEsiopetuksenSuoritus.copy(koulutusmoduuli = peruskoulunEsiopetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))

  def defaultEsiopetuksenSuoritus = peruskoulunEsiopetuksenSuoritus

  def lisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä =
    Some(EsiopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.erityisenTuenPäätösJossaOsaAikainenErityisopetus))
    ))

  def lisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta =
    Some(EsiopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätökset =
      Some(List(OsaAikainenErityisopetusExampleData.erityisenTuenPäätösIlmanOsaAikaistaErityisopetusta))))

  "Osa-aikainen erityisopetus" - {
    "Opiskeluoikeudella on erityisen tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(lisätiedot = lisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta)) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on erityisen tuen päätös osa-aikaisesta erityisopetuksesta ja tieto suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = lisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä,
        suoritukset = List(defaultEsiopetuksenSuoritus.copy(
          osaAikainenErityisopetus = Some(List(Koodistokoodiviite("LV1","osaaikainenerityisopetuslukuvuodenaikana")))
        ))
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on erityisen tuen päätös osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        suoritukset = List(defaultEsiopetuksenSuoritus.copy(osaAikainenErityisopetus = None)),
        lisätiedot = lisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä)) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.osaAikainenErityisopetus.kirjausPuuttuuSuorituksesta(
            "Jos osa-aikaisesta erityisopetuksesta on päätös opiskeluoikeuden lisätiedoissa, se pitää kirjata myös suoritukseen"
          )
        )
      }
    }

    "Opiskeluoikeudella on erityisen tuen päätös osa-aikaisesta erityisopetuksesta ja tyhjä lista suorituksessa -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        suoritukset = List(defaultEsiopetuksenSuoritus.copy(osaAikainenErityisopetus = Some(List()))),
        lisätiedot = lisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä)) {
        verifyResponseStatus(
          400,
          KoskiErrorCategory.badRequest.validation.osaAikainenErityisopetus.kirjausPuuttuuSuorituksesta(
            "Jos osa-aikaisesta erityisopetuksesta on päätös opiskeluoikeuden lisätiedoissa, se pitää kirjata myös suoritukseen"
          )
        )
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
  }

  "Deprekoituja kenttiä, jotka tiputetaan siirrossa pois" - {
    "Lisätiedon kenttiä tukimuodot ja erityisenTuenPäätökseen kenttiä tukimuodot ja toteutuspaikka ei oteta vastaan siirrossa" in {
      val oo = defaultOpiskeluoikeus.withLisätiedot(
        Some(EsiopetuksenOpiskeluoikeudenLisätiedot(
          tukimuodot = Some(List(ExamplesEsiopetus.osaAikainenErityisopetus)),
          erityisenTuenPäätökset = Some(List(erityisenTuenPäätös.copy(
            tukimuodot = Some(List(ExamplesEsiopetus.osaAikainenErityisopetus)),
            toteutuspaikka = kulosaarenAlaAste.oppilaitosnumero
          )))
        )))

      val tallennettuna = putAndGetOpiskeluoikeus(oo)

      tallennettuna.lisätiedot.get.tukimuodot should equal (None)
      tallennettuna.lisätiedot.get.erityisenTuenPäätökset.head.head.tukimuodot should equal (None)
      tallennettuna.lisätiedot.get.erityisenTuenPäätökset.head.head.toteutuspaikka should equal (None)
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): EsiopetuksenOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EsiopetuksenOpiskeluoikeus]
}
