package fi.oph.koski.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.ExamplesPerusopetus.erityisenTuenPäätös
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.kulosaarenAlaAste
import fi.oph.koski.documentation.{ExamplesEsiopetus, OsaAikainenErityisopetusExampleData}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator

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
      validate(oo).isRight should equal(true)
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää pidennetyn oppivelvollisuuden mutta ei vammaisuusjaksoja" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = None,
            vaikeastiVammainen = None,
          )
        )
      )

      validate(oo).left.get should equal (KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"))
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

      validate(oo).left.get should equal (KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vammaisuusjaksoja ei voi olla ilman vastaavaa pidennetyn oppivelvollisuuden jaksoa"))
    }

    "Validointi onnistuu ennen rajapäivää, vaikka opiskeluoikeus sisältää vammaisuusjaksoja mutta ei pidennettyä oppivelvollisuutta" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = None,
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )

      validate(oo, 1).isRight should be(true)
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää vaikeasti vammaisuuden mutta ei vammaisuuden jakson ja jaksojen loppua ei ole määritelty" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = None,
            vaikeastiVammainen = Some(List(Aikajakso(alku, None))),
          )
        )
      )

      validate(oo).isRight should be(true)
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää pidennetyn oppivelvollisuuden jakson joka alkaa vammaisuusjaksojen jälkeen" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(2), None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal (KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"))
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää vammaisuusjakson, joka on kokonaan pidennetyn oppivelvollisuuden ulkopuolella" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(4), None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              Aikajakso(alku, Some(alku.plusDays(3))),
              Aikajakso(alku.plusDays(4), None)
            )),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal (KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"))
    }

    "Validointi ei onnistu, kun vammaisuusjakso alkaa ennen pidennetyn oppivelvollisuuden alkua" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(4), None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              Aikajakso(alku, None)
            )),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal (KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"))
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää pidennetyn oppivelvollisuuden jakson joka alkaa ennen vammaisuusjaksoa" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.minusDays(1), None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku.minusDays(1)),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),

            vammainen = None,
            vaikeastiVammainen = Some(List(Aikajakso(alku, None)))
          )
        )
      )

      validate(oo).left.get should equal (KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"))
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää rajatun pidennetyn oppivelvollisuuden jakson joka jatkuu rajatun vammaisuusjakson jälkeen" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(2)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1))))),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal (HttpStatus.append(
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"),
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Viimeisimmän vammaisuusjakson päättymispäivä ei ole sama kuin pidennetyn oppivelvollisuuden määritelty päättymispäivä")
      ))
    }

    "Validointi ei onnistu, kun vaikeasti vammaisuuden ja vammaisuuden jaksoja on samoina päivinä" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(10)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(5))))),
            vaikeastiVammainen = Some(List(Aikajakso(alku.plusDays(5), Some(alku.plusDays(10)))))
          )
        )
      )

      validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vaikeasti vammaisuuden ja muun kuin vaikeasti vammaisuuden aikajaksot eivät voi olla voimassa samana päivänä"))
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää rajatun pidennetyn oppivelvollisuuden jakson joka on sama kuin vammaisuuden jakso" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(1)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = None,
            vaikeastiVammainen = Some(List(Aikajakso(alku, Some(alku.plusDays(1)))))
          )
        )
      )

      validate(oo).isRight should equal(true)
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää rajatun pidennetyn oppivelvollisuuden jakson joka loppuu ennen rajattua vammaisuusjaksoa" in {
      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(1)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = None,
            vaikeastiVammainen = Some(List(Aikajakso(alku.plusDays(2), Some(alku.plusDays(3)))))
          )
        )
      )

      validate(oo).left.get should equal(HttpStatus.fold(
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"),
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"),
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Viimeisimmän vammaisuusjakson päättymispäivä ei ole sama kuin pidennetyn oppivelvollisuuden määritelty päättymispäivä")
      ))
    }

    "Validointi onnistuu, kun opiskeluoikeus sisältää peräkkäisiä eri vammaisuusjaksoja" in {
      val alkujakso = Aikajakso(alku, Some(alku.plusDays(2)))
      val välijakso = Aikajakso(alku.plusDays(3), Some(alku.plusDays(4)))
      val loppujakso = Aikajakso(alku.plusDays(5), Some(alku.plusDays(6)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alkujakso.alku, loppujakso.loppu)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alkujakso.alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              alkujakso, loppujakso
            )),
            vaikeastiVammainen = Some(List(
              välijakso
            ))
          )
        )
      )

      validate(oo).isRight should equal(true)
    }

    "Validointi ei onnistu, kun opiskeluoikeus sisältää toisiinsa lomittumattomat vammaisuuden jaksot" in {
      val alkujakso = Aikajakso(alku, Some(alku.plusDays(1)))
      val välijakso = Aikajakso(alku.plusDays(3), Some(alku.plusDays(4)))
      val loppujakso = Aikajakso(alku.plusDays(5), Some(alku.plusDays(6)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alkujakso.alku, loppujakso.loppu)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alkujakso.alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              alkujakso, loppujakso
            )),
            vaikeastiVammainen = Some(List(
              välijakso
            ))
          )
        )
      )

      validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"))
    }

    "Validointi onnistuu, kun samantyyppisiä vammaisuusjaksoja on päällekäin" in {
      val jakso1 = Aikajakso(alku, None)
      val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(12)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              jakso1,
              jakso2
            )),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).isRight should equal(true)
    }

    "Validointi ei onnistu, kun samantyyppisiä vammaisuusjaksoja on päällekäin, mutta vain aiemmin alkanut jakso jatkuu avoimuuden vuoksi pidennetyn oppivelvollisuuden loppuun" in {
      val jakso1 = Aikajakso(alku, None)
      val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(10)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(alku.plusDays(12)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              jakso1,
              jakso2
            )),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal(HttpStatus.append(
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"),
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Viimeisimmän vammaisuusjakson päättymispäivä ei ole sama kuin pidennetyn oppivelvollisuuden määritelty päättymispäivä")
      ))
    }

    "Validointi ei onnistu, jos viimeisenä alkaneelta vammaisuusjaksolta puuttuu päättymispäivä, kun pidennetyllä oppivelvollisuudella on päättymispäivä" in {
      val jakso1 = Aikajakso(alku, Some(alku.plusDays(12)))
      val jakso2 = Aikajakso(alku.plusDays(5), None)

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(10), Some(alku.plusDays(12)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              jakso1,
              jakso2
            )),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal(HttpStatus.append(
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"),
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Viimeisimmän vammaisuusjakson päättymispäivä ei ole sama kuin pidennetyn oppivelvollisuuden määritelty päättymispäivä")
      ))
    }


    "Validointi ei onnistu, kun opiskeluoikeus sisältää osittain päällekäiset eri vammaisuuden jaksot" in {
      val jakso1 = Aikajakso(alku, None)
      val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

      val oo = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.plusDays(10), Some(alku.plusDays(12)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              jakso1
            )),
            vaikeastiVammainen = Some(List(
              jakso2
            ))
          )
        )
      )

      validate(oo).left.get should equal(HttpStatus.append(
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"),
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vaikeasti vammaisuuden ja muun kuin vaikeasti vammaisuuden aikajaksot eivät voi olla voimassa samana päivänä")
      ))
    }

    "Validointi ei onnistu, jos pidennetyn oppivelvollisuuden jakso alkaa ennen opiskeluoikeuden ja vammaisuusjakson yhteistä alkupäivää" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(alku, opiskeluoikeusLäsnä)
        )),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku.minusDays(1), None)),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku.minusDays(1)),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(Aikajakso(alku, None))),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"))
    }

    "Validointi ei onnistu, jos opiskeluoikeus on päättynyt ja samalla alkupäivämäärällä on useita vammaisuusjaksoja, joista jonkin loppu on avoin" in {
      val loppu = alku.plusMonths(13)
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(alku, opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(loppu, opiskeluoikeusValmistunut)
          )),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(alku, Some(loppu))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(alku),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(
              Aikajakso(alku, Some(alku.plusDays(10))),
              Aikajakso(alku.plusDays(11), None),
              Aikajakso(alku.plusDays(11), Some(loppu)),
            )),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal(HttpStatus.append(
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"),
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Viimeisimmän vammaisuusjakson päättymispäivä ei ole sama kuin pidennetyn oppivelvollisuuden määritelty päättymispäivä")
      ))
    }

    "Validointi ei onnistu, jos pidennetyn oppivelvollisuuden jakso loppuu jo ennen opiskeluoikeuden alkamista" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2022, 8, 9), opiskeluoikeusLäsnä)
          )
        ),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(LocalDate.of(2021, 5, 19), Some(LocalDate.of(2022, 2, 3)))),
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2021, 5, 19)),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            )),
            vammainen = Some(List(Aikajakso(LocalDate.of(2021, 5, 19), Some(LocalDate.of(2022, 2, 3))))),
            vaikeastiVammainen = None
          )
        )
      )

      validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus("Pidennetty oppivelvollisuusjakso ei voi loppua ennen opiskeluoikeuden alkua"))
    }

    "Validointi onnistuu, kun erityisen tuen jaksoja on ilman pidennettyä oppivelvollisuutta" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2022, 8, 9), opiskeluoikeusLäsnä)
          )
        ),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = None,
            vammainen = None,
            vaikeastiVammainen = None,
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2022, 8, 9)),
                loppu = Some(LocalDate.of(2022, 9, 9)),
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2023, 2, 3)),
                loppu = Some(LocalDate.of(2023, 9, 15)),
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            ))
          )
        )
      )

      validate(oo).isRight should equal(true)
    }

    "Validointi onnistuu, kun erityisen tuen jaksoja on kokonaan pidennetyn oppivelvollisuuden ulkopuolella" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2022, 8, 9), opiskeluoikeusLäsnä)
          )
        ),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(LocalDate.of(2023, 5, 19), Some(LocalDate.of(2024, 2, 3)))),
            vammainen = Some(List(Aikajakso(LocalDate.of(2023, 5, 19), Some(LocalDate.of(2024, 2, 3))))),
            vaikeastiVammainen = None,
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2022, 8, 9)),
                loppu = Some(LocalDate.of(2022, 9, 9)),
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2023, 5, 19)),
                loppu = Some(LocalDate.of(2024, 2, 3)),
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            ))
          )
        )
      )

      validate(oo).isRight should equal(true)
    }

    "Validointi ei onnistu, jos pidennetty oppivelvollisuus sisältää päiviä, joina ei ole erityisen tuen jaksoa" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2022, 8, 9), opiskeluoikeusLäsnä)
          )
        ),
        lisätiedot = Some(
          ExamplesEsiopetus.lisätiedot.copy(
            pidennettyOppivelvollisuus = Some(Aikajakso(LocalDate.of(2023, 5, 19), Some(LocalDate.of(2024, 2, 3)))),
            vammainen = Some(List(Aikajakso(LocalDate.of(2023, 5, 19), Some(LocalDate.of(2024, 2, 3))))),
            vaikeastiVammainen = None,
            erityisenTuenPäätökset = Some(List(
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2023, 5, 19)),
                loppu = Some(LocalDate.of(2024, 1, 3)),
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
              ErityisenTuenPäätös(
                alku = Some(LocalDate.of(2024, 1, 7)),
                loppu = None,
                opiskeleeToimintaAlueittain = true,
                erityisryhmässä = Some(true)
              ),
            ))
          )
        )
      )

      validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.erityisenTuenPäätös("Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa erityisen tuen jaksoa"))
    }

    def validate(oo: Opiskeluoikeus, voimaanastumispäivänOffsetTästäPäivästä: Long = 0): Either[HttpStatus, Oppija] = {
      val oppija = Oppija(defaultHenkilö, List(oo))

      implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
      implicit val accessType = AccessType.write

      val config = KoskiApplicationForTests.config.withValue("validaatiot.pidennetynOppivelvollisuudenYmsValidaatiotAstuvatVoimaan", fromAnyRef(LocalDate.now().plusDays(voimaanastumispäivänOffsetTästäPäivästä).toString))

      mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija)
    }

    def mockKoskiValidator(config: Config) = {
      new KoskiValidator(
        KoskiApplicationForTests.tutkintoRepository,
        KoskiApplicationForTests.koodistoViitePalvelu,
        KoskiApplicationForTests.organisaatioRepository,
        KoskiApplicationForTests.possu,
        KoskiApplicationForTests.henkilöRepository,
        KoskiApplicationForTests.ePerusteet,
        KoskiApplicationForTests.validatingAndResolvingExtractor,
        KoskiApplicationForTests.suostumuksenPeruutusService,
        config
      )
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): EsiopetuksenOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EsiopetuksenOpiskeluoikeus]
}
