package fi.oph.koski.schema

import fi.oph.koski.TestEnvironment

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class JaksoSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Jaksojen konjunktio" - {
    "Kun jakson 1 alku on ennen jakson 2 alkua" in {
      val jakso1 = J(date(2018, 5, 20), Some(date(2019, 6, 20)))
      val jakso2 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakson 1 alku on sama kuin jakson 2 alku" in {
      val jakso1 = J(date(2018, 6, 20), Some(date(2019, 6, 20)))
      val jakso2 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakson 1 alku on jakson 2 sisällä ja loppua ei määritelty" in {
      val jakso1 = J(date(2018, 6, 20))
      val jakso2 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakson 1 loppu on jakson 2 sisällä" in {
      val jakso1 = J(date(2017, 6, 20), Some(date(2018, 6, 20)))
      val jakso2 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakson 2 alku on ennen jakson 1 alkua" in {
      val jakso1 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      val jakso2 = J(date(2018, 5, 20), Some(date(2019, 7, 20)))
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakson 2 alku on ennen jakson 1 alkua ja loppua ei määritelty" in {
      val jakso1 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      val jakso2 = J(date(2018, 5, 20), None)
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakson 2 loppu on jakson 1 sisällä" in {
      val jakso1 = J(date(2018, 6, 20), Some(date(2018, 7, 20)))
      val jakso2 = J(date(2017, 5, 20), Some(date(2018, 6, 20)))
      jakso1.overlaps(jakso2) should equal(true)
    }

    "Kun jakso 1 päättyy ennen jakson 2 alkua" in {
      val jakso1 = J(date(2015, 6, 20), Some(date(2016, 7, 20)))
      val jakso2 = J(date(2017, 5, 20), Some(date(2018, 6, 20)))
      jakso1.overlaps(jakso2) should equal(false)
    }

    "Kun jakso 2 päättyy ennen jakson 1 alkua" in {
      val jakso1 = J(date(2017, 5, 20), Some(date(2018, 6, 20)))
      val jakso2 = J(date(2015, 6, 20), Some(date(2016, 7, 20)))
      jakso1.overlaps(jakso2) should equal(false)
    }
  }

  "Aikajaksolistojen päällekkäisyys" - {
    "Osittainen päällekkäisyys avoimen jakson kanssa" in {
      val as =
        List(Aikajakso(date(2026, 1, 1), Some(date(2026, 1, 31))))
      val bs =
        List(Aikajakso(date(2026, 1, 15), None))

      Aikajakso.overlaps(as, bs) should equal(true)
      Aikajakso.overlaps(bs, as) should equal(true)
    }

    "Yksi yhteinen rajapäivä" in {
      val as =
        List(Aikajakso(date(2026, 1, 1), Some(date(2026, 1, 15))))
      val bs =
        List(Aikajakso(date(2026, 1, 15), Some(date(2026, 1, 31))))

      Aikajakso.overlaps(as, bs) should equal(true)
      Aikajakso.overlaps(bs, as) should equal(true)
    }

    "Jaksot ovat peräkkäisiä ilman päällekkäisyyttä" in {
      val as =
        List(Aikajakso(date(2026, 1, 1), Some(date(2026, 1, 15))))
      val bs =
        List(Aikajakso(date(2026, 1, 16), None))

      Aikajakso.overlaps(as, bs) should equal(false)
      Aikajakso.overlaps(bs, as) should equal(false)
    }

    "Päällekkäisyys löytyy vasta listojen jälkimmäisistä jaksoista" in {
      val as =
        List(
          Aikajakso(date(2026, 1, 1), Some(date(2026, 1, 10))),
          Aikajakso(date(2026, 4, 1), Some(date(2026, 4, 10))),
        )
      val bs =
        List(
          Aikajakso(date(2026, 2, 1), Some(date(2026, 2, 10))),
          Aikajakso(date(2026, 4, 5), Some(date(2026, 4, 15))),
        )

      Aikajakso.overlaps(as, bs) should equal(true)
      Aikajakso.overlaps(bs, as) should equal(true)
    }
  }

}

case class J(
  alku: LocalDate,
  loppu: Option[LocalDate] = None
) extends Jakso
