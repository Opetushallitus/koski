package fi.oph.koski.environment

import com.typesafe.config.ConfigFactory
import fi.oph.koski.config.AppConfigAvaintarkistus
import fi.oph.koski.config.AppConfigAvaintarkistus.Tulos
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class AppConfigAvaintarkistusSpec extends AnyFreeSpec with Matchers {
  private def tarkista(reference: String, appConfig: String): Tulos =
    AppConfigAvaintarkistus.tarkista(ConfigFactory.parseString(reference), ConfigFactory.parseString(appConfig))

  "Kaikki avaimet löytyvät" in {
    tarkista("a.b = 1\nc = mock", "a { b = 2 }\nc = \"https://example.com\"") should be(Tulos(Nil, Nil))
  }

  "Puuttuvat avaimet listataan täysinä polkuina" in {
    tarkista("a.b = 1\na.c = 2\nd = 3", "a.b = 1").puuttuvat should be(List("a.c", "d"))
  }

  "Ylimääräiset avaimet listataan erikseen" in {
    tarkista("a = 1", "a = 1\nb.c = 2").ylimääräiset should be(List("b.c"))
  }

  "Lista ja tyhjä merkkijono ovat arvoja" in {
    tarkista("a = [x, y]\nb = salaisuus", "a = []\nb = \"\"") should be(Tulos(Nil, Nil))
  }

  "Viittauksia ei tarvitse ratkaista" in {
    tarkista(s"a = 1\nb = $${a}\nc = [$${a}]", s"a = 1\nb = $${a}\nc = $${?PUUTTUVA_MUUTTUJA}") should be(Tulos(Nil, Nil))
  }

  "Erikoismerkkejä sisältävä avain lainataan" in {
    tarkista("a.\"b.c\" = 1", "").puuttuvat should be(List("a.\"b.c\""))
  }

  "Kosken reference.conf luetaan ilman kirjastojen tiedostoja" in {
    val avaimet = AppConfigAvaintarkistus.tarkista(AppConfigAvaintarkistus.koskenReferenceConf, ConfigFactory.empty()).puuttuvat
    val tiedostonAvaimet = AppConfigAvaintarkistus.tarkista(ConfigFactory.parseFile(new File("src/main/resources/reference.conf")), ConfigFactory.empty()).puuttuvat
    avaimet should be(tiedostonAvaimet)
    avaimet should contain("sessionTimeoutMinutes")
    avaimet.filter(_.startsWith("slick.")) should be(empty)
  }
}
