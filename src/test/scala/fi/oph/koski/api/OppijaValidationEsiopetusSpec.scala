package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä}
import fi.oph.koski.documentation.ExamplesPerusopetus.erityisenTuenPäätös
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.kulosaarenAlaAste
import fi.oph.koski.documentation.{ExamplesEsiopetus, OsaAikainenErityisopetusExampleData}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
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

  "Pidennetyn oppivelvollisuuden aikajakso" - {
    val alku = LocalDate.of(2016, 4, 1)

    "ei sisällä pidennettyä oppivelvollisuuden eikä vammaisuuden aikajaksoja" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = None,
            vammainen = None,
            vaikeastiVammainen = None
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "sisältää pidennetyn oppivelvollisuuden mutta ei vammaisuusjaksoja" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(
              Aikajakso(alku, None)
            ),
            vammainen = None,
            vaikeastiVammainen = None
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "sisältää vammaisuustiedon aikajaksot kun sekä vammaisuusjakson että pidennetyn oppivelvollisuuden loppua ei ole määritelty" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, None)),
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "sisältää vammaisuustiedon aikajaksot kun pidennetyn oppivelvollisuuden loppu on määritelty mutta vammaisuusjakson ei" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(2)))),
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "sisältää vammaisuustiedon aikajaksot kun pidennetyn oppivelvollisuuden ja vammaisuusjakson loppu on määritelty" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(2)))),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(2))))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(2)))))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "ei ole validi kun vammaisuustiedon aikajaksot ovat olemassa ja pidennetty oppivelvollisuusjakso puuttuu" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = None,
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          HttpStatus(400, KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella().errors ++
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella().errors)
        )
      }
    }
    "ei ole validi kun vammaisuustiedon aikajakso alkaa ennen pidennetyn oppivelvollisuuden jaksoa" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, None)),
            vammainen = Some(List(Aikajakso(alku.minusDays(1), None))),
            vaikeastiVammainen = Some(List(Aikajakso(alku.minusDays(1), None)))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          HttpStatus(400, KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella().errors ++
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella().errors)
        )
      }
    }
    "ei ole validi kun vammaisuustiedon aikajakso loppuu pidennetyn oppivelvollisuuden jakson jälkeen" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(2)))),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(3))))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(3)))))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          HttpStatus(400, KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella().errors ++
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella().errors)
        )
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): EsiopetuksenOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EsiopetuksenOpiskeluoikeus]
}
