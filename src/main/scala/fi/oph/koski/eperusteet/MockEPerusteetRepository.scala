package fi.oph.koski.eperusteet

import fi.oph.koski.json.{JsonFiles, JsonResources, JsonSerializer}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi
import scala.reflect.runtime.universe.TypeTag

import java.time.LocalDate

object MockEPerusteetRepository extends EPerusteetRepository {
  lazy val rakenteet: List[EPerusteOsaRakenne] = haeRakenteetTiedostoista[EPerusteOsaRakenne]

  lazy val kokoRakenteet: List[EPerusteKokoRakenne] = haeRakenteetTiedostoista[EPerusteKokoRakenne]

  private def haeRakenteetTiedostoista[T: TypeTag]: List[T] = (customRakennenimet ++ rakennenimet).map { id =>
    JsonSerializer.extract[T](JsonResources.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get, ignoreExtras = true)
  }

  private val customRakennenimet = List(
    "rakenne-autoalan-perustutkinto2017-koulutusvientikokeilu",
    "rakenne-automekaanikon-erikoisammattitutkinto-OPH-1886-2017_2017-12-04_2018-12-31_2021-12-31",
    "rakenne-automekaanikon-erikoisammattitutkinto-OPH-1886-2017_2019-01-01_2066-05-12_null",
    "rakenne-liiketalouden-perustutkinto-1000-011-2014_2016-08-01_2018-07-31_null",
    "rakenne-liiketalouden-perustutkinto-2000-011-2014_2015-08-01_2017-07-31_null",
    "rakenne-liiketalouden-perustutkinto-2000-011-2014_2016-08-01_2018-07-31_null",
    "rakenne-liiketalouden-perustutkinto-3000-011-2014_2016-08-01_2018-07-31_2019-07-31",
    "rakenne-liiketalouden-perustutkinto-4000-011-2014_2066-05-12_null_null",
    "rakenne-liiketalouden-perustutkinto-uudempi",
    "rakenne-liiketalouden-perustutkinto-vanhempi",
    "rakenne-luonto-ja-ymparistoala",
    "rakenne-mock-empty-koulutukset",
    "rakenne-valma-ei-voimassa",
    "rakenne-virheellinen-puuteollisuuden-perustutkinto"
  )

  private val rakennenimet = List(
    "rakenne-aikuisten-lukiokoulutuksen-opetussuunnitelman-perusteet-2015-1573363",
      "rakenne-aikuisten-lukiokoulutuksen-opetussuunnitelman-perusteet-2019-6840291",
      "rakenne-aikuisten-maahanmuuttajien-kotoutumiskoulutuksen-opetussuunnitelman-perusteet-2012-7675672",
      "rakenne-aikuisten-perusopetuksen-opetussuunnitelman-perusteet-2499640",
      "rakenne-ajoneuvoalan-perustutkinto-7614470",
      "rakenne-ammatilliseen-koulutukseen-valmentava-koulutus-(valma)-2910073",
      "rakenne-ammatilliseen-peruskoulutukseen-valmentava-koulutus-1376251",
      "rakenne-autoalan-perustutkinto-3397336",
      "rakenne-autoalan-työnjohdon-erikoisammattitutkinto-1013059",
      "rakenne-automekaanikon-erikoisammattitutkinto-2434073",
      "rakenne-esiopetuksen-opetussuunnitelman-perusteet-2014-419551",
      "rakenne-hiusalan-perustutkinto-1571584",
      "rakenne-kansanopistojen-oppivelvollisille-suunnatun-vapaan-sivistystyön-koulutuksen-opetussuunnitelman-perusteet-2021-7512390",
      "rakenne-koulunkäynnin-ja-aamu--ja-iltapäivätoiminnan-ohjauksen-ammattitutkinto,-koulutusvientikokeilu-2434074",
      "rakenne-liiketalouden-perustutkinto-1718932",
      "rakenne-lisäopetuksen-opetussuunnitelman-perusteet-2014-718900",
      "rakenne-lukiokoulutukseen-valmistavan-koulutuksen-opetussuunnitelman-perusteet-2021-7823340",
      "rakenne-lukion-opetussuunnitelman-perusteet-2015-1372910",
      "rakenne-lukion-opetussuunnitelman-perusteet-2019-6828810",
      "rakenne-lukioon-valmistavan-koulutuksen-opetussuunnitelman-perusteet-1667890",
      "rakenne-perusopetukseen-valmistavan-opetuksen-opetussuunnitelman-perusteet-2015-1541511",
      "rakenne-perusopetuksen-opetussuunnitelman-perusteet-2014-419550",
      "rakenne-puutarhatalouden-perustutkinto-1721199",
      "rakenne-puuteollisuuden-perustutkinto-3932281",
      "rakenne-sosiaali--ja-terveysalan-perustutkinto-1724172",
      "rakenne-taiteen-perusopetuksen-laajan-oppimäärän-opetussuunnitelman-perusteet-2017-3689874",
      "rakenne-taiteen-perusopetuksen-yleisen-oppimäärän-opetussuunnitelman-perusteet-2017-3689873",
      "rakenne-tieto--ja-viestintätekniikan-perustutkinto,-koulutusvientikokeilu-6542660",
      "rakenne-tutkintokoulutukseen-valmentava-koulutus-7534950",
      "rakenne-työhön-ja-itsenäiseen-elämään-valmentava-koulutus-(telma)-2910079",
      "rakenne-vapaan-sivistystyön-lukutaitokoulutuksen-opetussuunnitelma-suositus-2017-7675670"
  )

  def findPerusteet(nimi: String): List[EPerusteRakenne] = {
    // Hakee aina samoilla kriteereillä "auto"
    JsonSerializer.extract[EPerusteOsaRakenteet](JsonFiles.readFile("src/main/resources/mockdata/eperusteet/hakutulokset-auto.json"), ignoreExtras = true)
    .data.filter(_.nimi("fi").toLowerCase.contains(nimi.toLowerCase)).sortBy(_.koulutusvienti)
  }

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPerusteRakenne] = {
    kokoRakenteet
      .filter(r => koulutustyypit.map(k => s"${k.koodistoUri}_${k.koodiarvo}").contains(r.koulutustyyppi))
      .map(_.toEPeruste)
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
}
