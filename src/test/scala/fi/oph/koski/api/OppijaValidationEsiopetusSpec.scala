package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä}
import fi.oph.koski.documentation.ExamplesPerusopetus.erityisenTuenPäätös
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.kulosaarenAlaAste
import fi.oph.koski.documentation.{ExamplesEsiopetus, OsaAikainenErityisopetusExampleData}
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

    "Validointi onnistuu, kun opiskeluoikeus ei sisällä pidennettyä oppivelvollisuuden eikä vammaisuuden aikajaksoja" in {
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

    "Validointi ei onnistu, kun opiskeluoikeus sisältää pidennetyn oppivelvollisuuden mutta ei vammaisuusjaksoja" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, None)),
            vammainen = None,
            vaikeastiVammainen = None
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
        )
      }
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää vammaisuusjaksoja mutta ei pidennettyä oppivelvollisuutta" in {
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
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
        )
      }
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää vaikeasti vammaisuuden mutta ei vammaisuuden jakson ja jaksojen loppua ei ole määritelty" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, None)),
            vammainen = None,
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää pidennetyn oppivelvollisuuden jakson joka alkaa vammaisuusjaksojen jälkeen" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(2), None)),
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = None
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää pidennetyn oppivelvollisuuden jakson joka alkaa ennen vammaisuusjaksoa" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.minusDays(1), None)),
            vammainen = None,
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
        )
      }
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää rajatun pidennetyn oppivelvollisuuden jakson joka jatkuu rajatun vammaisuusjakson jälkeen" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(2)))),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1))))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1)))))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
        )
      }
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää rajatun pidennetyn oppivelvollisuuden jakson joka on sama kuin vammaisuuden jaksot" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(1)))),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1))))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1)))))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää rajatun pidennetyn oppivelvollisuuden jakson joka loppuu ennen rajattua vammaisuusjaksoa" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(1)))),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1))))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(2)))))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää toisiinsa lomittuvat vammaisuuden jaksot" in {
      val alkujakso = Aikajakso(alku, Some(alku.plusDays(2)))
      val välijakso = Aikajakso(alku.plusDays(3), Some(alku.plusDays(4)))
      val loppujakso = Aikajakso(alku.plusDays(4), Some(alku.plusDays(6)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alkujakso.alku, loppujakso.loppu)),
            vammainen = Some(List(
              alkujakso, loppujakso
            )),
            vaikeastiVammainen = Some(List(
              välijakso
            ))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää toisiinsa lomittumattomat vammaisuuden jaksot" in {
      val alkujakso = Aikajakso(alku, Some(alku.plusDays(1)))
      val välijakso = Aikajakso(alku.plusDays(3), Some(alku.plusDays(4)))
      val loppujakso = Aikajakso(alku.plusDays(4), Some(alku.plusDays(6)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alkujakso.alku, loppujakso.loppu)),
            vammainen = Some(List(
              alkujakso, loppujakso
            )),
            vaikeastiVammainen = Some(List(
              välijakso
            ))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(
          expectedStatus = 400,
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
        )
      }
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää osittain päällekäiset vammaisuuden jaksot" in {
      val jakso1 = Aikajakso(alku, None)
      val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(8)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(10), Some(alku.plusDays(12)))),
            vammainen = Some(List(
              jakso1
            )),
            vaikeastiVammainen = Some(List(
              jakso2
            ))
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi onnistuu, vaikka pidennetyn oppivelvollisuuden jakso alkaa ennen opiskeluoikeuden ja vammaisuusjakson yhteistä alkupäivää" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(alku, opiskeluoikeusLäsnä)
        )),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.minusDays(1), None)),
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = None
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Validointi onnistuu, vaikka pidennetyn oppivelvollisuuden jakso loppuu jo ennen opiskeluoikeuden alkamista" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2022, 8, 9), opiskeluoikeusLäsnä)
          )
        ),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(LocalDate.of(2021, 5, 19), Some(LocalDate.of(2022, 2, 3)))),
            vammainen = Some(List(Aikajakso(LocalDate.of(2021, 5, 19), Some(LocalDate.of(2022, 2, 3))))),
            vaikeastiVammainen = None
          )
        )
      )
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): EsiopetuksenOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EsiopetuksenOpiskeluoikeus]
}
