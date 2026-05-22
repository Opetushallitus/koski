package fi.oph.koski.turvakielto

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.documentation.ExamplesEsiopetus
import fi.oph.koski.documentation.ExamplesPerusopetus
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TurvakieltoServiceSpec extends AnyFreeSpec with Matchers {
  "poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot" - {
    "toimii päätason suorituksilla, joilla ei ole osasuoritukset-kenttää (esim. EsiopetuksenSuoritus)" in {
      val oo = ExamplesEsiopetus.opiskeluoikeus
      val siivottu = TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(oo)
        .asInstanceOf[EsiopetuksenOpiskeluoikeus]

      siivottu.oppilaitos.map(_.oid) should equal(Some(Opetushallitus.organisaatioOid))
      siivottu.koulutustoimija.map(_.oid) should equal(Some(Opetushallitus.organisaatioOid))
      siivottu.suoritukset.head.toimipiste.oid should equal(Opetushallitus.organisaatioOid)
    }

    "korvaa päätason VahvistusPaikkakunnalla-paikkakunnan turvakieltopaikkakunnalla" in {
      val oo = ExamplesPerusopetus.ysiluokkalaisenOpiskeluoikeus
      val siivottu = TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(oo)
        .asInstanceOf[PerusopetuksenOpiskeluoikeus]

      val luokanSuoritus = siivottu.suoritukset.collectFirst {
        case s: PerusopetuksenVuosiluokanSuoritus => s
      }.get
      luokanSuoritus.toimipiste.oid should equal(Opetushallitus.organisaatioOid)
      val vahvistus = luokanSuoritus.vahvistus.get
      vahvistus.paikkakunta should equal(TurvakieltoService.turvakieltopaikkakunta)
    }

    "ei muuta Vahvistuksettoman osasuorituksen vahvistus-tilaa (oppiainesuoritus)" in {
      val oo = ExamplesPerusopetus.ysiluokkalaisenOpiskeluoikeus
      val alkuperäisetOppiaineet = oo.suoritukset.collectFirst {
        case s: PerusopetuksenVuosiluokanSuoritus => s.osasuoritukset.toList.flatten
      }.get
      alkuperäisetOppiaineet should not be empty

      val siivottu = TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(oo)
        .asInstanceOf[PerusopetuksenOpiskeluoikeus]
      val siivotutOppiaineet = siivottu.suoritukset.collectFirst {
        case s: PerusopetuksenVuosiluokanSuoritus => s.osasuoritukset.toList.flatten
      }.get

      siivotutOppiaineet should equal(alkuperäisetOppiaineet)
      all(siivotutOppiaineet) shouldBe a[Vahvistukseton]
    }

    "siivoaa osasuoritusten toimipisteet ja valinnaiset vahvistuspaikkakunnat (MahdollisestiToimipisteellinen + VahvistusValinnaisellaPaikkakunnalla)" in {
      val oo = AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis()
      val siivottu = TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(oo)
        .asInstanceOf[AmmatillinenOpiskeluoikeus]

      val päätasonSuoritus = siivottu.suoritukset.head
      päätasonSuoritus.toimipiste.oid should equal(Opetushallitus.organisaatioOid)
      päätasonSuoritus.vahvistus.get.asInstanceOf[VahvistusValinnaisellaPaikkakunnalla].paikkakunta should equal(None)

      val osasuoritukset = päätasonSuoritus.osasuoritukset.toList.flatten
      osasuoritukset should not be empty
      val mahdollisestiToimipisteelliset = osasuoritukset.collect { case s: MahdollisestiToimipisteellinen => s }
      mahdollisestiToimipisteelliset should not be empty
      all(mahdollisestiToimipisteelliset.map(_.toimipiste)) should equal(None)

      val osasuoritustenPaikkakunnat = osasuoritukset.flatMap(_.vahvistus.collect {
        case v: VahvistusValinnaisellaPaikkakunnalla => v.paikkakunta
      })
      all(osasuoritustenPaikkakunnat) should equal(None)
    }
  }
}
