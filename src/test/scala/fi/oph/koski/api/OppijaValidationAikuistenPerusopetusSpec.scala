package fi.oph.koski.api

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudetWithAccessCheck
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{aikuistenPerusopetuksenAlkuvaiheenSuoritus, oppiaineidenSuoritukset2015, oppiaineidenSuoritukset2017}
import fi.oph.koski.documentation.ExamplesEsiopetus.osaAikainenErityisopetus
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http._
import fi.oph.koski.koskiuser.KoskiSpecificSession.systemUser
import fi.oph.koski.schema._
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiHttpSpec}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationAikuistenPerusopetusSpec
  extends TutkinnonPerusteetTest[AikuistenPerusopetuksenOpiskeluoikeus]
    with KoskiHttpSpec
    with DirtiesFixtures
    with OpiskeluoikeusTestMethodsAikuistenPerusopetus
    with DatabaseTestMethods {

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = AikuistenPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      aikuistenPerusopetuksenOppimääränSuoritus(diaari).copy(osasuoritukset = None, vahvistus = None)
    ),
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(AikuistenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))))
  )

  private def aikuistenPerusopetuksenOppimääränSuoritus(diaari: Option[String] = Some("19/011/2015")) = {
    ExamplesAikuistenPerusopetus.aikuistenPerusopetukseOppimääränSuoritus(
      AikuistenPerusopetus(diaari),
      (if (diaari == Some("OPH-1280-2017")) { oppiaineidenSuoritukset2017 } else { oppiaineidenSuoritukset2015 })
    )
  }

  private def opiskeluoikeusWithValmistunutTila = defaultOpiskeluoikeus.copy(
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(
      AikuistenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
      AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
    ))
  )

  def eperusteistaLöytymätönValidiDiaarinumero: String = "19/011/2015"

  "Kurssisuoritukset" - {
    "OPS 2015, mutta kurssisuorituksissa 2017 koodisto -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus(Some("19/011/2015")).copy(osasuoritukset = oppiaineidenSuoritukset2017)))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*aikuistenperusopetuksenpaattovaiheenkurssit2017.*".r))
      }
    }

    "OPS 2017, mutta kurssisuorituksissa 2015 koodisto -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus(Some("OPH-1280-2017")).copy(osasuoritukset = oppiaineidenSuoritukset2015)))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*aikuistenperusopetuksenkurssit2015.*".r))
      }
    }
  }

  "Alkuvaiheen suoritus" - {
    "Kun yritetään liittää suoritus väärään koulutustyyppiin liittyvään perusteeseen -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(
        suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
          koulutusmoduuli = aikuistenPerusopetuksenAlkuvaiheenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some(vääräntyyppisenPerusteenDiaarinumero))
        ))
      )

      putTodistus(oo) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi, s".*ei voi käyttää opiskeluoikeuden voimassaoloaikana voimassaollutta perustetta $vääräntyyppisenPerusteenDiaarinumero \\($vääräntyyppisenPerusteenId\\), jonka koulutustyyppi on .*. Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat.*".r)))
    }
  }

  "Vahvistetussa alkuvaiheen suorituksessa" - {
    "oppiaineen arviointia" - {
      "ei vaadita" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
            osasuoritukset = aikuistenPerusopetuksenAlkuvaiheenSuoritus.osasuoritukset.map(_.map(_.copy(arviointi = None)))
          ))
        )
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
    }
    "kurssin arviointi" - {
      "vaaditaan" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
            osasuoritukset = aikuistenPerusopetuksenAlkuvaiheenSuoritus.osasuoritukset.map(xs =>
              List(xs.head.copy(
                arviointi = None,
                osasuoritukset = xs.head.osasuoritukset.map(x => List(x.head.copy(arviointi = None))))
              ))
            ))
        )
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, HttpStatus.append(
            KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella suorituksentyyppi/aikuistenperusopetuksenoppimaaranalkuvaihe on keskeneräinen osasuoritus aikuistenperusopetuksenalkuvaiheenkurssit2017/LÄI1"),
            KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella aikuistenperusopetuksenalkuvaiheenoppiaineet/AI on keskeneräinen osasuoritus aikuistenperusopetuksenalkuvaiheenkurssit2017/LÄI1"))
          )
        }
      }
    }
  }

  "Sama oppiaine" - {
    "aikuisten perusopetuksen oppivaiheessa" - {
      "sallitaan" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
            osasuoritukset = aikuistenPerusopetuksenAlkuvaiheenSuoritus.osasuoritukset.map(xs => xs.head :: xs)
          ))
        )
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
    }
    "oppimäärän suorituksessa" - {
      "ei sallita" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus().copy(
            osasuoritukset = oppiaineidenSuoritukset2015.map(xs => xs.head :: xs))
          ))
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran ryhmässä pakolliset"))
        }
      }
    }
  }

  "Opiskeluoikeuden Valmistunut tila" - {
    "Voidaan asettaa kun kaikki on vahvistettu" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(
          aikuistenPerusopetuksenOppimääränSuoritus(),
          aikuistenPerusopetuksenAlkuvaiheenSuoritus
        )
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
    "Voidaan asettaa vaikka alkuvaiheen suorituksella ei olisi vahvistusta" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(
          aikuistenPerusopetuksenOppimääränSuoritus(),
          aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(vahvistus = None)
        )
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
    "Ei voida asettaa kun vahvistamaton oppimäärä on ainut suoritus" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus().copy(vahvistus = None))
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/201101 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
      }
    }
    "Ei voida asettaa kun perusopetuksen oppimäärä on vahvistamatta" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(
          aikuistenPerusopetuksenAlkuvaiheenSuoritus,
          aikuistenPerusopetuksenOppimääränSuoritus().copy(vahvistus = None)
        )
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/201101 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
      }
    }
    "Ei voida asettaa kun vahvistettu alkuvaihe on ainoa suoritus" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus))
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.suoritusPuuttuu("Opiskeluoikeutta aikuistenperusopetus ei voi merkitä valmiiksi kun siitä puuttuu suoritus aikuistenperusopetuksenoppimaara tai perusopetuksenoppiaineenoppimaara"))
      }
    }
    "Ei voida asettaa kun vahvistamaton alkuvaihe on ainut suoritus" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(vahvistus = None))
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.suoritusPuuttuu("Opiskeluoikeutta aikuistenperusopetus ei voi merkitä valmiiksi kun siitä puuttuu suoritus aikuistenperusopetuksenoppimaara tai perusopetuksenoppiaineenoppimaara"))
      }
    }
  }

  "Opintojen rahoitus" - {
    "lasna -tilalta vaaditaan opintojen rahoitus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2008, 1, 1), opiskeluoikeusLäsnä))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
      }
    }
    "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
      val tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(
        AikuistenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusValmistunut)
      ))
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila, suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus()))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta valmistunut puuttuu rahoitusmuoto"))
      }
    }
  }

  "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus()),
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(
        AikuistenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        AikuistenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusEronnut)
      )))
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
    }
  }

  "Deprekoituja kenttiä, jotka tiputetaan siirrossa pois" - {
    "Lisätiedon kenttiä tukimuodot, tehostetunTuenPäätös, tehostetunTuenPäätökset, vuosiluokkiinSitoutumatonOpetus, vammainen, vaikeastiVammainen ja oikeusMaksuttomaanAsuntolapaikkaan ei oteta vastaan siirrossa" in {
      val oo = defaultOpiskeluoikeus.withLisätiedot(
        Some(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
          tukimuodot = Some(List(osaAikainenErityisopetus)),
          tehostetunTuenPäätös = Some(Aikajakso(LocalDate.now(), None)),
          tehostetunTuenPäätökset = Some(List(Aikajakso(LocalDate.now(), None))),
          vuosiluokkiinSitoutumatonOpetus = Some(true),
          vammainen = Some(List(Aikajakso(LocalDate.now(), None))),
          vaikeastiVammainen = Some(List(Aikajakso(LocalDate.now(), None))),
          oikeusMaksuttomaanAsuntolapaikkaan = Some(Aikajakso(LocalDate.now(), None)),
          sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(LocalDate.now(), None)))
      )))

      val tallennettuna = putAndGetOpiskeluoikeus(oo)

      tallennettuna.lisätiedot.get.tukimuodot should equal (None)
      tallennettuna.lisätiedot.get.tehostetunTuenPäätös should equal (None)
      tallennettuna.lisätiedot.get.tehostetunTuenPäätökset should equal (None)
      tallennettuna.lisätiedot.get.vuosiluokkiinSitoutumatonOpetus should equal (None)
      tallennettuna.lisätiedot.get.vammainen should equal (None)
      tallennettuna.lisätiedot.get.vaikeastiVammainen should equal (None)
      tallennettuna.lisätiedot.get.oikeusMaksuttomaanAsuntolapaikkaan should equal (None)

      val tietokannasta = opiskeluoikeusTietokannasta(tallennettuna.oid.get)
      (tietokannasta.data \\ "lisätiedot").toString.contains("sisäoppilaitosmainenMajoitus") should equal (true)
      (tietokannasta.data \\ "lisätiedot").toString.contains("tukimuodot") should equal (false)
      (tietokannasta.data \\ "lisätiedot").toString.contains("tehostetunTuenPäätös") should equal (false)
      (tietokannasta.data \\ "lisätiedot").toString.contains("tehostetunTuenPäätökset") should equal (false)
      (tietokannasta.data \\ "lisätiedot").toString.contains("vuosiluokkiinSitoutumatonOpetus") should equal (false)
      (tietokannasta.data \\ "lisätiedot").toString.contains("vammainen") should equal (false)
      (tietokannasta.data \\ "lisätiedot").toString.contains("vaikeastiVammainen") should equal (false)
      (tietokannasta.data \\ "lisätiedot").toString.contains("oikeusMaksuttomaanAsuntolapaikkaan") should equal (false)
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): AikuistenPerusopetuksenOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[AikuistenPerusopetuksenOpiskeluoikeus]

  private def opiskeluoikeusTietokannasta(opiskeluoikeusOid: String) =
    runDbSync(KoskiOpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oid === opiskeluoikeusOid).map(o => o).result).head
}
