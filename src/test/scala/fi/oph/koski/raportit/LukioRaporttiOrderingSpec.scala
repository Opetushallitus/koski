package fi.oph.koski.raportit

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LukioRaporttiOrderingSpec extends AnyFreeSpec with TestEnvironment with Matchers {

  "Oppiaineiden järjestys" - {
    val paikallinenPitkäMatematiikka = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Matematiikka, pitkä oppimäärä", "MA", true), Nil)
    val valtakunnallinenPitkäMatematiikka = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Matematiikka, pitkä oppimäärä", "MA", false), Nil)
    val paikallinenLyhytMatematiikka = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Matematiikka, lyhyt oppimäärä", "MA", true), Nil)
    val valtakunnallinenLyhytMatematiikka = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Matematiikka, lyhyt oppimäärä", "MA", false), Nil)
    val paikallinenBiologia = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Biologia", "BI", true), Nil)
    val paikallinenRuotsiB1= YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Ruotsi", "B1", true), Nil)
    val valtakunnallinenBiologia = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Biologia", "BI", false), Nil)
    val valtakunnallinenRuotsiB2 = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Ruotsi", "B2", false), Nil)
    val valtakunnallinenRuotsiB1 = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Ruotsi", "B1", false), Nil)
    val paikallinenAine1 = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Autokoulu", "AAA", true), Nil)
    val paikallinenAine2 = YleissivistäväRaporttiOppiaineJaKurssit(YleissivistäväRaporttiOppiaine("Autokoulu", "ÖÖÖ", true), Nil)

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
      oppiaineet.sorted(YleissivistäväOppiaineetOrdering) should equal(List(
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
