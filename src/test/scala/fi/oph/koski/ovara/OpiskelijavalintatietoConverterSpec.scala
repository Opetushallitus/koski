package fi.oph.koski.ovara

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.servlet.InvalidRequestException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskelijavalintatietoConverterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val converter = new OpiskelijavalintatietoConverter(KoskiApplicationForTests.koodistoViitePalvelu)

  private def mockData(oid: String): OvaraOpiskelijavalintatieto =
    MockOvaraClient.fetchOpiskelijavalintatiedot(oid).toOption.flatten.getOrElse(fail(s"Mock-dataa ei löydy oidille $oid"))

  "Muuntaa Ovaran tiedot koodistokoodiviitteiksi ja lokalisoiduiksi nimiksi" in {
    val result = converter.convert(mockData(KoskiSpecificMockOppijat.ammattilainen.oid))

    result.hakemukset should have length 1
    val hakemus = result.hakemukset.head
    hakemus.hakemusOid shouldBe "1.2.246.562.11.00000000000001049800"
    hakemus.haunKohdejoukko.map(k => (k.koodistoUri, k.koodiarvo)) shouldBe Some(("haunkohdejoukko", "12"))
    hakemus.hakutapa.map(k => (k.koodistoUri, k.koodiarvo)) shouldBe Some(("hakutapa", "01"))
    hakemus.haku.oid shouldBe "1.2.246.562.29.00000000000000005467"
    hakemus.haku.nimi.get("fi") shouldBe "Yhteishaku kevät 2024"
    hakemus.haku.nimi.get("sv") shouldBe "Gemensam ansökan våren 2024"
    hakemus.haku.nimi.get("en") shouldBe "Yhteishaku kevät 2024"

    hakemus.hakutoiveet should have length 1
    val hakutoive = hakemus.hakutoiveet.head
    hakutoive.hakukohde.oid shouldBe "1.2.246.562.20.00000000000000005476"
    hakutoive.hakukohde.nimi.get("fi") shouldBe "Tietotekniikan koulutusohjelma"
    hakutoive.tarjoaja.map(_.oid) shouldBe Some("1.2.246.562.10.42160341923")
    hakutoive.tarjoaja.map(_.nimi.get("fi")) shouldBe Some("Esimerkkioppilaitos")
    hakutoive.koulutuksenAlkamiskausi.map(k => (k.koodistoUri, k.koodiarvo)) shouldBe Some(("kausi", "s"))
    hakutoive.koulutuksenAlkamisvuosi shouldBe Some("2024")
    hakutoive.valinnanTila.map(k => (k.koodistoUri, k.koodiarvo)) shouldBe Some(("omadatavalinnantila", "hyvaksytty"))
    hakutoive.vastaanotonTila.map(k => (k.koodistoUri, k.koodiarvo)) shouldBe Some(("omadatavastaanotontila", "vastaanottanutsitovasti"))
    hakutoive.ilmoittautumisenTila.map(k => (k.koodistoUri, k.koodiarvo)) shouldBe Some(("omadatailmoittautumisentila", "lasna"))
    hakutoive.johtaaTutkintoon shouldBe Some(true)
  }

  "Heittää poikkeuksen virheellisestä koodistokoodiviitteestä" in {
    val e = the[IllegalArgumentException] thrownBy converter.convert(mockData(KoskiSpecificMockOppijat.amis.oid))
    e.getMessage should include("INVALID")
  }

  "Heittää poikkeuksen tuntemattomasta tila-arvosta" in {
    val e = the[InvalidRequestException] thrownBy converter.convert(mockData(KoskiSpecificMockOppijat.lukiolainen.oid))
    e.getMessage should include("omadatavalinnantila/tuntematontila")
  }
}
