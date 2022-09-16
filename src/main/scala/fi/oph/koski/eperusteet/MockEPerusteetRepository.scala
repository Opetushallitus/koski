package fi.oph.koski.eperusteet

import fi.oph.koski.json.{JsonFiles, JsonResources, JsonSerializer}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

import java.time.LocalDate

object MockEPerusteetRepository extends EPerusteetRepository {
  lazy val rakenteet: List[EPerusteRakenne] = rakennenimet.map { id =>
    JsonSerializer.extract[EPerusteOsaRakenne](JsonResources.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get, ignoreExtras = true)
  }

  lazy val kokoRakenteet: List[EPerusteKokoRakenne] = rakennenimet.map { id =>
    JsonSerializer.extract[EPerusteKokoRakenne](JsonResources.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get, ignoreExtras = true)
  }

  private val rakennenimet = List(
    "rakenne-autoalan-perustutkinto",
    "rakenne-autoalan-perustutkinto2017",
    "rakenne-autoalan-perustutkinto2017-koulutusvientikokeilu",
    "rakenne-ajoneuvoalan-perustutkinto",
    "rakenne-luonto-ja-ymparistoala",
    "rakenne-autoalan-tyonjohto",
    "rakenne-perusopetus",
    "rakenne-aikuisten-perusopetus2017",
    "rakenne-lukio",
    "rakenne-lukio-2019",
    "rakenne-aikuisten-lukio-2019",
    "rakenne-hiusalan-perustutkinto",
    "rakenne-puutarhatalouden-perustutkinto",
    "rakenne-automekaanikon-erikoisammattitutkinto-vanhempi",
    "rakenne-automekaanikon-erikoisammattitutkinto-uudempi",
    "rakenne-liiketalouden-perustutkinto-vanhempi",
    "rakenne-liiketalouden-perustutkinto-uudempi",
    "rakenne-liiketalouden-perustutkinto-1000-011-2014_2016-08-01_2018-07-31_null",
    "rakenne-liiketalouden-perustutkinto-2000-011-2014_2015-08-01_2017-07-31_null",
    "rakenne-liiketalouden-perustutkinto-2000-011-2014_2016-08-01_2018-07-31_null",
    "rakenne-liiketalouden-perustutkinto-3000-011-2014_2016-08-01_2018-07-31_2019-07-31",
    "rakenne-puuteollisuuden-perustutkinto",
    "rakenne-virheellinen-puuteollisuuden-perustutkinto",
    "rakenne-valma",
    "rakenne-valma-ei-voimassa",
    "rakenne-sosiaali-ja-terveysala",
    "rakenne-vst-oppivelvollisille-suunnattu",
    "rakenne-vst-maahanmuuttajien-kotoutumiskoulutus",
    "rakenne-vst-maahanmuuttajien-kotoutumiskoulutus-2022",
    "rakenne-vst-lukutaitokoulutus",
    "rakenne-tieto-ja-viestintätekniikan-perustutkinto",
    "rakenne-tutkintokoulutukseen-valmentava-koulutus"
  )

  def findPerusteet(nimi: String): List[EPerusteRakenne] = {
    // Hakee aina samoilla kriteereillä "auto"
    JsonSerializer.extract[EPerusteOsaRakenteet](JsonFiles.readFile("src/main/resources/mockdata/eperusteet/hakutulokset-auto.json"), ignoreExtras = true)
    .data.filter(_.nimi("fi").toLowerCase.contains(nimi.toLowerCase)).sortBy(_.koulutusvienti)
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPerusteRakenne] = {
    kokoRakenteet.filter(r => koulutustyypit.map(k => s"${k.koodistoUri}_${k.koodiarvo}").contains(r.koulutustyyppi)).map(_.toEPeruste).sortBy(_.koulutusvienti)
  }

  // TODO: Filtteröi päivän mukaan, palauta monta vastausta
  def findTarkatRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTarkkaRakenne] = {
    if (diaariNumero == "mock-empty-koulutukset") {
      kokoRakenteet.find(_.diaarinumero == "39/011/2014").map(_.copy(koulutukset = Nil)).toList
    } else {
      kokoRakenteet.find(_.diaarinumero == diaariNumero).toList
    }
  }

  def findKaikkiRakenteet(diaarinumero: String): List[EPerusteRakenne] = {
    val diaarinMukaan = rakenteet.filter(_.diaarinumero == diaarinumero)
    diaarinMukaan
  }

  def findPerusteenYksilöintitiedot(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTunniste] = {
    kokoRakenteet
      .filter(_.diaarinumero == diaariNumero)
      .filter(perusteVoimassa(päivä))
      .map(_.toEPerusteTunniste)
  }

  override protected def webBaseUrl = "https://eperusteet.opintopolku.fi"
}
