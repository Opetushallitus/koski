package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.common.json.JsonSerializer
import fi.oph.koski.validation.{ValidatingAndResolvingExtractor, ValidationAndResolvingContext}
import org.scalatest.{FreeSpec, Matchers}

class ArviointiSpec extends FreeSpec with Matchers {
  "Yleissivistävä" - {
    "H" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"H"}}""")
      arviointi.hyväksytty should equal(false)
    }
    "S" in {
      val arviointi = read[PerusopetuksenOppiaineenArviointi]("""{"arvosana":{"koodistoUri":"arviointiasteikkoyleissivistava","koodiarvo":"S"}}""")
      arviointi.hyväksytty should equal(true)
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

  private lazy val app = KoskiApplicationForTests
  import org.json4s.jackson.JsonMethods.parse
  private def read[T](s: String)(implicit mf : Manifest[T]) = ValidatingAndResolvingExtractor.extract[T](parse(s), ValidationAndResolvingContext(app.koodistoViitePalvelu, app.organisaatioRepository)).toOption.get
}
