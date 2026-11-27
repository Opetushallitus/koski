package fi.oph.koski.ibis

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class IbisOppiainekarttaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Oppiainekartta" - {
    "tunnistaa kielioppiaineet kielineen" in {
      IbisOppiainekartta.get("112726") should be(Some(IbisKieliOppiaine("A", "FI"))) // FINNISH A: Literature
      IbisOppiainekartta.get("112733") should be(Some(IbisKieliOppiaine("A2", "EN"))) // ENGLISH A Language and Literature
      IbisOppiainekartta.get("128711") should be(Some(IbisKieliOppiaine("A2", "JA"))) // JAPANESE A: Lang and Literatur
      IbisOppiainekartta.get("100559") should be(Some(IbisKieliOppiaine("B", "SV"))) // SWEDISH B
      IbisOppiainekartta.get("100124") should be(Some(IbisKieliOppiaine("B", "ZH"))) // CHINESE B
      IbisOppiainekartta.get("117711") should be(Some(IbisKieliOppiaine("B", "CMN"))) // CHINESE B - MANDARIN
      IbisOppiainekartta.get("117712") should be(Some(IbisKieliOppiaine("B", "YUE"))) // CHINESE B - CANTONESE
      IbisOppiainekartta.get("100375") should be(Some(IbisKieliOppiaine("AB", "CMN"))) // MANDARIN AB.
      IbisOppiainekartta.get("100340") should be(Some(IbisKieliOppiaine("CLA", "LA"))) // LATIN
    }

    "tunnistaa extended essayn kielioppiaineen" in {
      IbisOppiainekartta.get("100176") should be(Some(IbisKieliOppiaine("A", "EN"))) // ENGLISH A
    }

    "tunnistaa muut oppiaineet" in {
      IbisOppiainekartta.get("166711") should be(Some(IbisMuuOppiaine("MATAA"))) // MATHEMATICS ANALYSIS AND APPROACHES
      IbisOppiainekartta.get("149712") should be(Some(IbisMuuOppiaine("HIS"))) // HISTORY EUROPE
      IbisOppiainekartta.get("159711") should be(Some(IbisMuuOppiaine("WS"))) // WLD. STUDIES ENV/ECO SUSTAINABILTY
    }

    "ei tunnista oppiaineita, joille ei vielä ole vastinetta Koskessa" in {
      // WELSH A: Literature, CLASS.GREEK, ART HISTORY ja REF PROJECT
      List("112715", "100129", "100058", "100657").flatMap(IbisOppiainekartta.get) should be(empty)
    }

    "tuottaa jokaisesta oppiaineesta skeeman ja koodistojen mukaisen IB Diploman oppiaineen" in {
      val converter = new IbisOpiskeluoikeusConverter(
        KoskiApplicationForTests.koodistoViitePalvelu,
        KoskiApplicationForTests.validatingAndResolvingExtractor,
      )
      val virheet = IbisOppiainekartta.oppiaineet.keys.toList.sorted.flatMap { koodi =>
        converter.convert(diplomaOppiaineella(koodi), None, LocalDate.of(2026, 7, 6)).left.toOption.map(virhe => s"$koodi: ${virhe.syyt.mkString(", ")}")
      }
      virheet should be(empty)
    }
  }

  private def diplomaOppiaineella(koodi: String) = IbisRecord(
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
    subjects = List(IbisSubject(2026, "MAY", koodi, "HL", "5", additional = false)),
  )
}
