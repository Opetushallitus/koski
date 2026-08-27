package fi.oph.koski.raportit

import fi.oph.koski.schema.{Aikajakso, OikeuttaMaksuttomuuteenPidennetty}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class RaporttiUtilsSpec extends AnyFreeSpec with Matchers {
  "jaksotMerkkijonona" - {
    "palauttaa puuttuvalle ja tyhjälle jaksolistalle None" in {
      RaporttiUtils.jaksotMerkkijonona[Aikajakso](None) shouldBe None
      RaporttiUtils.jaksotMerkkijonona(Some(Seq.empty[Aikajakso])) shouldBe None
    }

    "säilyttää kaikki jaksot ja niiden järjestyksen ilman aikarajausta" in {
      RaporttiUtils.jaksotMerkkijonona(Some(Seq(
        OikeuttaMaksuttomuuteenPidennetty(date(2027, 4, 1), date(2027, 6, 30)),
        Aikajakso(date(2026, 8, 1), None)
      ))) shouldBe Some("2027-04-01 – 2027-06-30, 2026-08-01 – ")
    }
  }

  "aikavälillä rajattu jaksotMerkkijonona" - {
    val rajaus = Aikajakso(date(2027, 1, 1), Some(date(2027, 12, 31)))

    "palauttaa None, kun mikään jakso ei osu rajaukseen" in {
      RaporttiUtils.jaksotMerkkijonona(
        Some(Seq(Aikajakso(date(2026, 1, 1), Some(date(2026, 12, 31))))),
        rajaus
      ) shouldBe None
    }

    "hyväksyy rajauksen alku- ja loppupäivään osuvat jaksot" in {
      RaporttiUtils.jaksotMerkkijonona(Some(Seq(
        Aikajakso(date(2026, 12, 1), Some(date(2027, 1, 1))),
        Aikajakso(date(2027, 12, 31), Some(date(2028, 1, 31)))
      )), rajaus) shouldBe Some("2026-12-01 – 2027-01-01, 2027-12-31 – 2028-01-31")
    }

    "hyväksyy avoimen jakson" in {
      RaporttiUtils.jaksotMerkkijonona(
        Some(Seq(Aikajakso(date(2026, 1, 1), None))),
        rajaus
      ) shouldBe Some("2026-01-01 – ")
    }
  }
}
