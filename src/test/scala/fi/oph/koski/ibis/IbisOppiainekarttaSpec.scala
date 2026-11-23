package fi.oph.koski.ibis

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class IbisOppiainekarttaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Oppiainekartta" - {
    "tunnistaa kielioppiaineet kielineen, myös IBO:n katkaisemilla nimillä" in {
      IbisOppiainekartta.get("FINNISH A: Literature") should be(Some(IbisKieliOppiaine("A", "FI")))
      IbisOppiainekartta.get("ENGLISH A Language and Literature") should be(Some(IbisKieliOppiaine("A2", "EN")))
      IbisOppiainekartta.get("JAPANESE A: Lang and Literatur") should be(Some(IbisKieliOppiaine("A2", "JA")))
      IbisOppiainekartta.get("SWEDISH B") should be(Some(IbisKieliOppiaine("B", "SV")))
      IbisOppiainekartta.get("CHINESE B") should be(Some(IbisKieliOppiaine("B", "ZH")))
      IbisOppiainekartta.get("CHINESE B - MANDARIN") should be(Some(IbisKieliOppiaine("B", "CMN")))
      IbisOppiainekartta.get("CHINESE B - CANTONESE") should be(Some(IbisKieliOppiaine("B", "YUE")))
      IbisOppiainekartta.get("MANDARIN AB.") should be(Some(IbisKieliOppiaine("AB", "CMN")))
      IbisOppiainekartta.get("LATIN") should be(Some(IbisKieliOppiaine("CLA", "LA")))
    }

    "tunnistaa extended essayn kielioppiaineen" in {
      IbisOppiainekartta.get("ENGLISH A") should be(Some(IbisKieliOppiaine("A", "EN")))
    }

    "tunnistaa muut oppiaineet" in {
      IbisOppiainekartta.get("MATHEMATICS ANALYSIS AND APPROACHES") should be(Some(IbisMuuOppiaine("MATAA")))
      IbisOppiainekartta.get("HISTORY EUROPE") should be(Some(IbisMuuOppiaine("HIS")))
      IbisOppiainekartta.get("WLD. STUDIES ENV/ECO SUSTAINABILTY") should be(Some(IbisMuuOppiaine("WS")))
    }

    "ei välitä kirjainkoosta eikä ylimääräisistä välilyönneistä" in {
      IbisOppiainekartta.get("  english a:  literature ") should be(Some(IbisKieliOppiaine("A", "EN")))
    }

    "tunnistaa IBIS:n 30 merkkiin katkaisemat nimet" in {
      IbisOppiainekartta.get("MATHEMATICS ANALYSIS AND APPRO") should be(Some(IbisMuuOppiaine("MATAA")))
      IbisOppiainekartta.get("MATHEMATICS APPLICATIONS AND I") should be(Some(IbisMuuOppiaine("MATAI")))
    }

    "tunnistaa jokaisen oppiaineen myös 30 merkkiin katkaistulla nimellä" in {
      val eriävät = IbisOppiainekartta.oppiaineet.toList.collect {
        case (nimi, oppiaine) if !IbisOppiainekartta.get(nimi.take(30)).contains(oppiaine) => nimi
      }
      eriävät should be(empty)
    }

    "ei tunnista oppiaineita, joille ei vielä ole vastinetta Koskessa" in {
      List("WELSH A: Literature", "CLASS.GREEK", "ART HISTORY", "REF PROJECT").flatMap(IbisOppiainekartta.get) should be(empty)
    }

    "tuottaa jokaisesta oppiaineesta skeeman ja koodistojen mukaisen IB Diploman oppiaineen" in {
      val converter = new IbisOpiskeluoikeusConverter(
        KoskiApplicationForTests.koodistoViitePalvelu,
        KoskiApplicationForTests.validatingAndResolvingExtractor,
      )
      val virheet = IbisOppiainekartta.oppiaineet.keys.toList.sorted.flatMap { nimi =>
        converter.convert(diplomaOppiaineella(nimi), None, LocalDate.of(2026, 7, 6)).left.toOption.map(virhe => s"$nimi: ${virhe.syyt.mkString(", ")}")
      }
      virheet should be(empty)
    }
  }

  private def diplomaOppiaineella(nimi: String) = IbisRecord(
    year = 2026,
    month = "MAY",
    school = "000001",
    iboTunniste = "abc123",
    dateOfBirth = None,
    gender = "",
    category = IbisCategory.Diploma,
    lastName = "",
    firstName = "",
    studentId = "",
    courseRef = "",
    lastChangeDate = None,
    totalPoints = Some(30),
    resultCode = IbisResultCode.DiplomaAwarded,
    combinedGrade = Some(1),
    subjects = List(IbisSubject(2026, "MAY", nimi, "HL", "5", additional = false, language = "ENGLISH")),
  )
}
