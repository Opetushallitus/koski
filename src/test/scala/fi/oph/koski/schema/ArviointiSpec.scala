package fi.oph.koski.schema

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.documentation.AmmatillinenExampleData.hyväksytty
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class ArviointiSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Yleissivistävä" - {
    "H" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"H"}}""")
      arviointi.hyväksytty should equal(false)
    }
    "S" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"S"}}""")
      arviointi.hyväksytty should equal(true)
    }
    "O" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"O"}}""")
      arviointi.hyväksytty should equal(false)
    }
    "4" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"4"}}""")
      arviointi.hyväksytty should equal(false)
    }
    "5" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"5"}}""")
      arviointi.hyväksytty should equal(true)
    }
  }
  "Ammatillinen" - {
    "Hylätty" in {
      val arviointi = read[AmmatillinenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoammatillinenhyvaksyttyhylatty","koodiarvo":"Hylätty"},"päivä":"2000-01-01"}""")
      arviointi.hyväksytty should equal(false)
    }
    "Hyväksytty" in {
      val arviointi = read[AmmatillinenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoammatillinenhyvaksyttyhylatty","koodiarvo":"Hyväksytty"},"päivä":"2000-01-01"}""")
      arviointi.hyväksytty should equal(true)
    }
    "H" in {
      val arviointi = read[AmmatillinenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoammatillinent1k3","koodiarvo":"0"},"päivä":"2000-01-01"}""")
      arviointi.hyväksytty should equal(false)
    }
    "T1" in {
      val arviointi = read[AmmatillinenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoammatillinent1k3","koodiarvo":"1"},"päivä":"2000-01-01"}""")
      arviointi.hyväksytty should equal(true)
    }
  }

  "Ylioppilaskoe" - {
    "I" in {
      val arviointi = read[YlioppilaskokeenArviointi]( """{"arvosana":{"koodistoUri":"koskiyoarvosanat","koodiarvo":"I"}}""")
      arviointi.hyväksytty should equal(false)
    }
    "A" in {
      val arviointi = read[YlioppilaskokeenArviointi]( """{"arvosana":{"koodistoUri":"koskiyoarvosanat","koodiarvo":"A"}}""")
      arviointi.hyväksytty should equal(true)
    }
  }

  "Korkeakoulu" - {
    "1" in {
      val arviointi = read[KorkeakoulunKoodistostaLöytyväArviointi]("""{"arvosana":{"koodistoUri":"virtaarvosana","koodiarvo":"1"},"päivä":"2000-01-01"}""")
      arviointi.hyväksytty should equal(true)
    }
  }

  "Hyväksytty-kenttä" - {
    "Arvon generointi" in {
      JsonSerializer.writeWithRoot(PerusopetuksenOppiaineenArviointi(8)) should equal("""{"arvosana":{"koodiarvo":"8","koodistoUri":"arviointiasteikkoyleissivistava"},"hyväksytty":true}""")
      JsonSerializer.writeWithRoot(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("5", "virtaarvosana"), LocalDate.parse("2000-01-01"))) should equal("""{"arvosana":{"koodiarvo":"5","koodistoUri":"virtaarvosana"},"päivä":"2000-01-01","hyväksytty":true}""")
    }
    "Arvon validointi" - {
      "Annettua arvoa ei käytetä, vaan arvo lasketaan arvosanasta" in {
        read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"H"},"hyväksytty": false}""")
          .hyväksytty should equal(false)
        read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"H"},"hyväksytty": true}""")
          .hyväksytty should equal(false)
      }
    }
  }

  "Arviointien vertaus" - {
    val matalaArviointiAikaisempi = numeerinenArviointi(5, LocalDate.of(2016, 6, 4))
    val matalaArviointiMyöhempi = numeerinenArviointi(5, LocalDate.of(2017, 6, 4))
    val korkeaArviointi = numeerinenArviointi(9)

    val hyväksyttyArviointiAikaisempi = hyväksyttyArviointi(LocalDate.of(2016, 6, 4))
    val hyväksyttyArviointiMyöhempi = hyväksyttyArviointi(LocalDate.of(2017, 6, 4))
    val hylättyArviointi = hyväksyttyArviointi(LocalDate.of(2016, 6, 4))

    "Sama numeerinen arvosana palauttaa aikaisemman arvioinnin" in {
      Arviointi.korkeampiArviointi(matalaArviointiAikaisempi, matalaArviointiMyöhempi).arviointipäivä.get should equal (LocalDate.of(2016, 6, 4))
      Arviointi.korkeampiArviointi(matalaArviointiMyöhempi, matalaArviointiAikaisempi).arviointipäivä.get should equal (LocalDate.of(2016, 6, 4))
    }

    "Arvosanan ollessa eri, palautetaan korkeampi arviointi" in {
      Arviointi.korkeampiArviointi(matalaArviointiAikaisempi, korkeaArviointi).arvosana.koodiarvo should equal ("9")
      Arviointi.korkeampiArviointi(korkeaArviointi, matalaArviointiAikaisempi).arvosana.koodiarvo should equal ("9")
    }

    "Sama hyväksytty/hylätty arviointi palauttaa aikaisemman arvioinnin" in {
      Arviointi.korkeampiArviointi(hyväksyttyArviointiAikaisempi, hyväksyttyArviointiMyöhempi).arviointipäivä.get should equal (LocalDate.of(2016, 6, 4))
      Arviointi.korkeampiArviointi(hyväksyttyArviointiMyöhempi, hyväksyttyArviointiAikaisempi).arviointipäivä.get should equal (LocalDate.of(2016, 6, 4))
    }

    "Eri hyväksytty/hylätty arviointi palauttaa hyväksytyn arvioinnin" in {
      Arviointi.korkeampiArviointi(hyväksyttyArviointiAikaisempi, hylättyArviointi).arvosana.koodiarvo should equal ("Hyväksytty")
      Arviointi.korkeampiArviointi(hylättyArviointi, hyväksyttyArviointiAikaisempi).arvosana.koodiarvo should equal ("Hyväksytty")
    }
  }

  private lazy val app = KoskiApplicationForTests

  private def read[T](s: String)(implicit mf : Manifest[T]) = app.validatingAndResolvingExtractor
    .extract[T](strictDeserialization)(parse(s)).toOption.get

  def numeerinenArviointi(arvosana: Int, päivä: LocalDate = LocalDate.of(2016, 6, 4)): LukionArviointi = {
    NumeerinenLukionArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana.toString, koodistoUri = "arviointiasteikkoyleissivistava"), päivä)
  }

  def hyväksyttyArviointi(päivä: LocalDate = LocalDate.of(2016, 6, 4)) = AmmatillinenArviointi(arvosana = hyväksytty, päivä)
}
