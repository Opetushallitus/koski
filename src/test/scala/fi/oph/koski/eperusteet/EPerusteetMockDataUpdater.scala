package fi.oph.koski.eperusteet

import fi.oph.koski.log.Logging
import fi.oph.koski.cache.{JMXCacheManager}
import fi.oph.koski.json.JsonFiles

// VIRKAILIJA_ROOT=https://virkailija.opintopolku.fi mvn scala:testCompile exec:java -Dexec.mainClass=fi.oph.koski.eperusteet.EPerusteetMockDataUpdater
object EPerusteetMockDataUpdater extends App with Logging {
  val diaariNumerot = List(
    ("70/011/2015", "1573363"),
    ("OPH-2267-2019", "6840291"),
    ("OPH-1280-2017", "2499640"),
    ("OPH-5410-2021", "7614470"),
    // ("39/011/2014", "612"), Poistunut?
    // ("OPH-4792-2017", "3397363") Poistunut?,
    ("OPH-2762-2017", "3397336"),
    ("40/011/2001", "1013059"),
    ("OPH-1886-2017", "2434073"),
    ("OPH-1886-2017", "2434074"),
    ("102/011/2014", "419551"),
    ("43/011/2014", "1571584"),
    // ("1000/011/2014", "1718939"), Poistunut?
    // ("2000/011/2014", "1718940"), Poistunut?
    // ("2000/011/2014", "1718941"), Poistunut?
    // ("3000/011/2014", "1718942"), Poistunut?
    // ("4000/011/2014", "1718942"), Poistunut?
    ("59/011/2014", "1718932"),
    ("59/011/2014", "1718932"),
    ("OPH-2263-2019", "6828810"),
    ("60/011/2015", "1372910"),
    // ("62/011/2014", "33827"),
    ("56/011/2015", "1667890"),
    ("OPH-4958-2020", "7823340"),
    // ("mock-empty-koulutukset", "613"), Mock-peruste
    ("57/011/2015", "1541511"),
    ("105/011/2014", "718900"),
    ("104/011/2014", "419550"),
    ("75/011/2014", "1721199"),
    ("OPH-2455-2017", "3932281"),
    ("79/011/2014", "1724172"),
    ("OPH-2659-2017", "2910079"),
    ("OPH-1117-2019", "6542660"),
    ("OPH-2068-2017", "3689874"),
    ("OPH-2069-2017", "3689873"),
    ("OPH-1488-2021", "7534950"),
    ("5/011/2015", "1376251"),
    // ("OOO-2658-2017", "2910074"), Poistunut?
    ("OPH-2658-2017", "2910073"),
    // ("OPH-992455-2017", "99932281"), Poistunut?
    ("OPH-2984-2017", "7675670"),
    ("OPH-649-2022", "7675672"),
    ("OPH-123-2021", "7675672"),
    ("OPH-58-2021", "7512390")
  )

  val baseUrl = "https://eperusteet.opintopolku.fi/eperusteet-service"
  private val eperusteetRepository = new RemoteEPerusteetRepository(baseUrl, "")(new JMXCacheManager)

  def paivitaRakenteet(): Unit = {
    diaariNumerot.filter(dn => !dn._2.isEmpty).map(dn => {
      logger.info(s"Päivitetään tutkinnon rakenne ${dn._1}")
      try {
        val res = eperusteetRepository.findPerusteentiedot(dn._2)
        Left(res)
      } catch {
        case _: Throwable => Right(dn)
      }

    }).foreach(tiedot => {
      tiedot match {
        case Left(tiedot) => {
          val tiedostonimi = s"rakenne-${tiedot.nimi.getOrElse("fi", "").toLowerCase().replace(" ", "-")}-${tiedot.id}.json"
          JsonFiles.writeFile(s"src/main/resources/mockdata/eperusteet-v2/${tiedostonimi}", tiedot)
        }
        case Right(dn) => {
          logger.error(s"Virhe päivitettäessä tutkinnon rakenteita diaarinumerolle ${dn._1}, id: ${dn._2}")
        }
      }
    })
  }

  def paivitaHakutulokset(): Unit = {
    // Hakusanat, joille haetaan mock-data
    val hakusanat = List("auto")

    val hakutulokset = hakusanat.map(eperusteetRepository.findPerusteet)
    val hakusanatJaTulokset = hakusanat zip hakutulokset
    hakusanatJaTulokset.foreach(ht => {
      val tiedostonimi = s"hakutulokset-${ht._1}.json"
      JsonFiles.writeFile(s"src/main/resources/mockdata/eperusteet-v2/${tiedostonimi}", ht._2)
    })
  }

  paivitaRakenteet()
  paivitaHakutulokset()
}

