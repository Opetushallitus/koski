package fi.oph.koski.eperusteet

import fi.oph.koski.json.{JsonFiles, JsonResources, JsonSerializer}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi
import scala.reflect.runtime.universe.TypeTag

import java.time.LocalDate

object MockEPerusteetRepository extends EPerusteetRepository {
  lazy val rakenteet: List[EPerusteOsaRakenne] = haeRakenteetTiedostoista[EPerusteOsaRakenne]

  lazy val kokoRakenteet: List[EPerusteKokoRakenne] = haeRakenteetTiedostoista[EPerusteKokoRakenne]

  private def haeRakenteetTiedostoista[T: TypeTag]: List[T] = rakennenimet.map { id =>
    try {
      JsonSerializer.extract[T](JsonResources.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get, ignoreExtras = true)
    } catch {
      case e: Exception => throw new RuntimeException(s"Rakenteen $id haku epäonnistui: ${e.getMessage}")
    }
  }

  private val rakennenimet = List(
    "rakenne-autoalan-perustutkinto",
    "rakenne-mock-empty-koulutukset",
    "rakenne-autoalan-perustutkinto2017",
    "rakenne-autoalan-perustutkinto2017-koulutusvientikokeilu",
    "rakenne-ajoneuvoalan-perustutkinto",
    "rakenne-luonto-ja-ymparistoala",
    "rakenne-autoalan-tyonjohto",
    "rakenne-esiopetus-2014",
    "rakenne-perusopetus",
    "rakenne-perusopetuksen-lisäopetus-2014",
    "rakenne-perusopetukseen-valmistava",
    "rakenne-aikuisten-perusopetus2017",
    "rakenne-lukio",
    "rakenne-lukio-2019",
    "rakenne-luva-2015",
    "rakenne-luva-2021",
    "rakenne-aikuisten-lukio-2015",
    "rakenne-aikuisten-lukio-2019",
    "rakenne-hiusalan-perustutkinto",
    "rakenne-puutarhatalouden-perustutkinto",
    "rakenne-automekaanikon-erikoisammattitutkinto-OPH-1886-2017_2017-12-04_2018-12-31_2021-12-31",
    "rakenne-automekaanikon-erikoisammattitutkinto-OPH-1886-2017_2019-01-01_2066-05-12_null",
    "rakenne-liiketalouden-perustutkinto-vanhempi",
    "rakenne-liiketalouden-perustutkinto-uudempi",
    "rakenne-liiketalouden-perustutkinto-1000-011-2014_2016-08-01_2018-07-31_null",
    "rakenne-liiketalouden-perustutkinto-2000-011-2014_2015-08-01_2017-07-31_null",
    "rakenne-liiketalouden-perustutkinto-2000-011-2014_2016-08-01_2018-07-31_null",
    "rakenne-liiketalouden-perustutkinto-3000-011-2014_2016-08-01_2018-07-31_2019-07-31",
    "rakenne-liiketalouden-perustutkinto-4000-011-2014_2066-05-12_null_null",
    "rakenne-puuteollisuuden-perustutkinto",
    "rakenne-virheellinen-puuteollisuuden-perustutkinto",
    "rakenne-telma",
    "rakenne-valma",
    "rakenne-valma-2015",
    "rakenne-valma-ei-voimassa",
    "rakenne-sosiaali-ja-terveysala",
    "rakenne-vst-oppivelvollisille-suunnattu",
    "rakenne-vst-maahanmuuttajien-kotoutumiskoulutus",
    "rakenne-vst-maahanmuuttajien-kotoutumiskoulutus-2022",
    "rakenne-vst-lukutaitokoulutus",
    "rakenne-tieto-ja-viestintätekniikan-perustutkinto",
    "rakenne-tutkintokoulutukseen-valmentava-koulutus",
    "rakenne-tpo-yleinen",
    "rakenne-tpo-laaja"
  )

  private val osaamismerkkiRakenteetNimi = "osaamismerkit"

  private lazy val osaamismerkkiRakenteetTiedostosta: List[EPerusteOsaamismerkkiRakenne] = {
    val id = osaamismerkkiRakenteetNimi
    try {
      JsonSerializer.extract[List[EPerusteOsaamismerkkiRakenne]](JsonResources.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get, ignoreExtras = true)
    } catch {
      case e: Exception => throw new RuntimeException(s"Rakenteen $id haku epäonnistui: ${e.getMessage}")
    }
  }

  def findPerusteet(nimi: String): List[EPerusteRakenne] = {
    // Hakee aina samoilla kriteereillä "auto"
    JsonSerializer.extract[EPerusteOsaRakenteet](JsonFiles.readFile("src/main/resources/mockdata/eperusteet/hakutulokset-auto.json"), ignoreExtras = true)
    .data.filter(_.nimi("fi").toLowerCase.contains(nimi.toLowerCase)).sortBy(_.koulutusvienti)
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPerusteRakenne] = {
    kokoRakenteet
      .filter(r => koulutustyypit.map(k => s"${k.koodistoUri}_${k.koodiarvo}").contains(r.koulutustyyppi))
      .map(_.toEPeruste)
      .sortBy(_.diaarinumero)
  }

  def findTarkatRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTarkkaRakenne] = {
    findPerusteenYksilöintitiedot(diaariNumero, päivä).flatMap(p => kokoRakenteet.find(_.id == p.id))
  }

  def findKaikkiRakenteet(diaarinumero: String): List[EPerusteRakenne] = {
    rakenteet.filter(_.diaarinumero == diaarinumero)
  }

  def findKaikkiPerusteenYksilöintitiedot(diaariNumero: String): List[EPerusteTunniste] = {
    kokoRakenteet
      .filter(_.diaarinumero == diaariNumero)
      .map(_.toEPerusteTunniste)
  }

  override protected def webBaseUrl = "https://eperusteet.opintopolku.fi"

  override def findOsaamismerkkiRakenteet(): List[EPerusteOsaamismerkkiRakenne] =
    osaamismerkkiRakenteetTiedostosta
}
