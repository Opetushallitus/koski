package fi.oph.koski.localization

import fi.oph.koski.TestEnvironment
import fi.oph.koski.eperusteet.ETutkinnonOsa
import org.json4s.DefaultFormats
import fi.oph.koski.http.Http._
import fi.oph.koski.http.Http
import fi.oph.koski.koodisto.RemoteKoodistoPalvelu
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

private case class EPerusteInfot(data: List[EPerusteInfo])
private case class EPerusteInfo(id: Int)
private case class EPerusteRakenneLocalization(tutkinnonOsat: Option[List[ETutkinnonOsa]])

class EPerusteetLocalizationTest extends AnyFreeSpec with TestEnvironment with Matchers {

  private implicit val formats = DefaultFormats
  private lazy val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
  private lazy val koodistoPalvelu = new RemoteKoodistoPalvelu(root)

  private lazy val eperusteetHttp = Http(root + "/eperusteet-service", "eperusteet-localization-test")

  private def haePerusteidenInfot(startingFromPage: Int = 0): List[EPerusteInfo] = {
    val thisPage = Http.runIO(
      eperusteetHttp.get(uri"/api/perusteet/info?sivukoko=100&sivu=$startingFromPage")(Http.parseJson[EPerusteInfot])
    ).data
    if (thisPage.isEmpty) thisPage else thisPage ++ haePerusteidenInfot(startingFromPage + 1)
  }
  private def haeRakenne(id: Int): EPerusteRakenneLocalization = Http.runIO(
    eperusteetHttp.get(uri"/api/external/peruste/$id")(Http.parseJson[EPerusteRakenneLocalization])
  )

  private def kanoninenNimi(s: String) =
    s.stripSuffix("*").stripSuffix("(*)").replace('\u00a0', ' ').replaceAll("\\s+", " ").trim

  "Tutkinnon osien kielistetyt koodiarvot koodisto vs. ePerusteet" taggedAs(LocalizationTestTag) in {

    println("Ladataan listaa perusteista...")
    val perusteidenInfot = haePerusteidenInfot()
    // Uncomment this for testing
    //  .filter(i => Array(3932281, 1726680, 1314998, 2236610, 2291719, 1715117, 419550, 1314998, 1135883).contains(i.id))
    println(s"Löytyi ${perusteidenInfot.size} perustetta, ladataan perusteiden tiedot...")
    val started = System.currentTimeMillis
    val perusteet = perusteidenInfot.map(_.id).map(haeRakenne)
    val elapsed = System.currentTimeMillis - started
    println(s"Perusteiden tiedot ladattu (${elapsed/1000} s)")

    val nimetEperusteissa: Map[String, List[Map[String, String]]] = perusteet
      .flatMap(_.tutkinnonOsat.getOrElse(List.empty))
      .groupBy(_.koodiArvo)
      .mapValues(_.map(_.nimi.filterKeys(!_.startsWith("_")).mapValues(kanoninenNimi)).distinct)
    val suomenkielisetNimetEperusteissa = nimetEperusteissa
      .collect { case (arvo, nimetE) if nimetE.head.contains("fi") => (arvo, nimetE.head("fi")) }
    val ruotsinkielisetNimetEperusteissa = nimetEperusteissa
      .collect { case (arvo, nimetE) if nimetE.head.contains("sv") => (arvo, nimetE.head("sv")) }

    val nimetKoodistossa: Map[String, Map[String, String]] = koodistoPalvelu
      .getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("tutkinnonosat"))
      .groupBy(_.koodiArvo)
      .mapValues(_.head.nimi.get.values.mapValues(kanoninenNimi))

    println(s"Perusteista löytyi ${nimetEperusteissa.size} tutkinnon osaa, koodistosta ${nimetKoodistossa.size}")

    println("\nTutkinnon osan ruotsinkielinen nimi puuttuu koodistosta, mutta löytyy perusteista:")
    ruotsinkielisetNimetEperusteissa
      .filter { case (arvo, svE) => !nimetKoodistossa(arvo).contains("sv") }
      .foreach { case (arvo, svE) => println(s"$arvo $svE") }

    println("\nTutkinnon osan ruotsinkielinen nimi puuttuu sekä koodistosta että perusteista:")
    nimetKoodistossa
      .filter { case (arvo, nimiK) => !nimiK.contains("sv") && nimetEperusteissa.contains(arvo) && !nimetEperusteissa(arvo).head.contains("sv") }
      .foreach { case (arvo, nimiK) => println(s"$arvo ${nimiK("fi")}") }

    println("\nTutkinnon osan suomenkieliset nimet eivät täsmää:")
    suomenkielisetNimetEperusteissa
      .map { case (arvo, fiE) => (arvo, (fiE, nimetKoodistossa(arvo).get("fi"))) }
      .collect { case (arvo, (fiE, Some(fiK))) if fiE != fiK => (arvo, (fiE, fiK)) }
      .foreach {
        case (arvo, (fiE, fiK)) => println(s"$arvo perusteissa: $fiE\n${" " * arvo.length} koodistossa: $fiK")
      }

    println("\nTutkinnon osan ruotsinkieliset nimet eivät täsmää:")
    ruotsinkielisetNimetEperusteissa
      .map { case (arvo, svE) => (arvo, (svE, nimetKoodistossa(arvo).get("sv"))) }
      .collect { case (arvo, (svE, Some(svK))) if svE != svK => (arvo, (svE, svK)) }
      .foreach {
        case (arvo, (svE, svK)) => println(s"$arvo perusteissa: $svE\n${" " * arvo.length} koodistossa: $svK")
      }

    println("\nTutkinnon osalla useampi kuin yksi nimi perusteissa:")
    nimetEperusteissa
      .filter { case (arvo, nimetE) => nimetE.length > 1 }
      .foreach { case (arvo, nimetE) => println(s"$arvo ${nimetE.mkString("\n" + " " * (arvo.length + 1))}") }
  }

  "Osaamisalojen kielistetyt koodiarvo" taggedAs(LocalizationTestTag) in {
    val koodit = koodistoPalvelu.getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("osaamisala"))
    println("\nOsaamisalan ruotsinkielinen nimi puuttuu koodistosta:")
    koodit
      .filterNot(_.getMetadata("sv").exists(_.nimi.exists(_.trim.nonEmpty)))
      .sortBy(_.koodiArvo)
      .foreach { k => println(s"${k.koodiArvo} ${k.nimi.get.get("fi")}") }
  }

  "Tutkintonimikkeiden kielistetyt koodiarvo" taggedAs(LocalizationTestTag) in {
    val koodit = koodistoPalvelu.getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("tutkintonimikkeet"))
    println("\nTutkintonimikkeen ruotsinkielinen nimi puuttuu koodistosta:")
    koodit
      .filterNot(_.getMetadata("sv").exists(_.nimi.exists(_.trim.nonEmpty)))
      .sortBy(_.koodiArvo)
      .foreach { k => println(s"${k.koodiArvo} ${k.nimi.get.get("fi")}") }
  }
}

