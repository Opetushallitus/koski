package fi.oph.koski.raportit

import org.scalatest.{FreeSpec, Matchers}

class LukioRaporttiOrderingSpec extends FreeSpec with Matchers {

  "Oppiaineiden järjestys" - {
    val paikallinenPitkäMatematiikka = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Matematiikka, pitkä oppimäärä", "MA", true), Nil)
    val valtakunnallinenPitkäMatematiikka = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Matematiikka, pitkä oppimäärä", "MA", false), Nil)
    val paikallinenLyhytMatematiikka = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Matematiikka, lyhyt oppimäärä", "MA", true), Nil)
    val valtakunnallinenLyhytMatematiikka = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Matematiikka, lyhyt oppimäärä", "MA", false), Nil)
    val paikallinenBiologia = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Biologia", "BI", true), Nil)
    val paikallinenRuotsiB1= LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Ruotsi", "B1", true), Nil)
    val valtakunnallinenBiologia = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Biologia", "BI", false), Nil)
    val valtakunnallinenRuotsiB2 = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Ruotsi", "B2", false), Nil)
    val valtakunnallinenRuotsiB1 = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Ruotsi", "B1", false), Nil)
    val paikallinenAine1 = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Autokoulu", "AAA", true), Nil)
    val paikallinenAine2 = LukioRaporttiOppiaineJaKurssit(LukioRaporttiOppiaine("Autokoulu", "ÖÖÖ", true), Nil)

    val oppiaineet = List(
      paikallinenAine1,
      paikallinenAine2,
      paikallinenPitkäMatematiikka,
      valtakunnallinenPitkäMatematiikka,
      paikallinenLyhytMatematiikka,
      valtakunnallinenLyhytMatematiikka,
      paikallinenBiologia,
      paikallinenRuotsiB1,
      valtakunnallinenBiologia,
      valtakunnallinenRuotsiB2,
      valtakunnallinenRuotsiB1
    )

    "Valtakunnalliset aineet järjestetään ensimmäiseksi lukion opetussuunnitelman mukaan. Paikalliset aineet aakkosjärjestykseen nimen perusteella, pitkä matematiikka ennen lyhyttä." in {
      oppiaineet.sorted(LukioRaporttiOppiaineetOrdering) should equal(List(
        valtakunnallinenRuotsiB1,
        valtakunnallinenRuotsiB2,
        valtakunnallinenPitkäMatematiikka,
        valtakunnallinenLyhytMatematiikka,
        valtakunnallinenBiologia,
        paikallinenAine1,
        paikallinenAine2,
        paikallinenBiologia,
        paikallinenPitkäMatematiikka,
        paikallinenLyhytMatematiikka,
        paikallinenRuotsiB1
      ))
    }
  }
}
