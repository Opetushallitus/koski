package fi.oph.koski.perftest

import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import org.scalatest.matchers.should.Matchers

import java.io.{File, PrintWriter}
import java.time.LocalDate
import scala.util.Random

// Dokumentaatio: Katso /documentation/todistus.md
object YleinenKielitutkintoOppijatGenerator extends App with KoskidevHttpSpecification with Matchers with Logging {
  implicit val formats: org.json4s.Formats = org.json4s.DefaultFormats

  private val outputFile = new File("src/test/resources/yki_perftest_opiskeluoikeus_oids.txt")
  private val oppijaCount = env("YKI_PERFTEST_OPPIJA_COUNT", "100").toInt

  // Sallitut kielet YKI-tutkinnossa
  private val kielet = List("FI", "EN", "ES", "IT", "SE", "FR", "SV", "RU", "DE")

  // Tutkintotasot
  private val tutkintotasot = List("pt", "kt", "yt") // perustaso, keskitaso, ylin taso

  // Osasuoritusten tyypit (4 pakollista ennen 2026-07-01)
  private val osasuoritusTyypit = List("tekstinymmartaminen", "kirjoittaminen", "puheenymmartaminen", "puhuminen")

  private val hetu = new RandomHetu(1970)

  if (outputFile.exists()) {
    logger.info(s"File ${outputFile.getAbsolutePath} already exists. Skipping oppija creation.")
    logger.info("Delete the file if you want to regenerate test data.")
  } else {
    logger.info(s"Creating $oppijaCount oppijas with yleinen kielitutkinto data...")
    val opiskeluoikeusOids = createOppijat()
    saveOidsToFile(opiskeluoikeusOids)
    logger.info(s"Created ${opiskeluoikeusOids.length} oppijas. OIDs saved to ${outputFile.getAbsolutePath}")
  }

  private def createOppijat(): List[String] = {
    (1 to oppijaCount).flatMap { i =>
      createOppija(i)
    }.toList
  }

  private def createOppija(index: Int): Option[String] = {
    val kieli = kielet(Random.nextInt(kielet.length))
    val tutkintotaso = tutkintotasot(Random.nextInt(tutkintotasot.length))
    val tutkintopäivä = LocalDate.now().minusDays(Random.nextInt(365))
    val arviointipäivä = tutkintopäivä.plusDays(1)

    val opiskeluoikeus = createYkiOpiskeluoikeus(tutkintotaso, kieli, tutkintopäivä, arviointipäivä)
    val henkilö = UusiHenkilö(
      hetu.nextHetu,
      s"YKI-Testi-${index}",
      Some(s"YKI-Testi-${index}"),
      s"Perftest-${index}"
    )

    val oppija = Oppija(henkilö, List(opiskeluoikeus))
    val requestBody = JsonSerializer.writeWithRoot(oppija).getBytes("utf-8")

    try {
      put("api/oppija", body = requestBody, headers = authHeaders() ++ jsonContent) {
        status should equal(200)
        // Parse response as JSON to extract opiskeluoikeus OID
        val json = org.json4s.jackson.JsonMethods.parse(body)
        val opiskeluoikeudet = (json \ "opiskeluoikeudet").children
        val oid = opiskeluoikeudet.headOption.flatMap(oo => (oo \ "oid").extractOpt[String])
        if (oid.isEmpty) {
          logger.warn(s"Failed to get opiskeluoikeus OID for oppija $index from response")
        }
        oid
      }
    } catch {
      case e: Exception =>
        logger.error(s"Failed to create oppija $index: ${e.getMessage}")
        None
    }
  }

  private def createYkiOpiskeluoikeus(
    tutkintotaso: String,
    kieli: String,
    tutkintopäivä: LocalDate,
    arviointipäivä: LocalDate
  ): KielitutkinnonOpiskeluoikeus = {
    // Sallitut arvosanat per tutkintotaso
    val sallitutArvosanat = tutkintotaso match {
      case "pt" => List("alle1", "1", "2")       // perustaso
      case "kt" => List("alle3", "3", "4")       // keskitaso
      case "yt" => List("alle5", "5", "6")       // ylin taso
    }

    // Luo osasuoritukset satunnaisilla arvosanoilla (valitaan sallituista)
    val osasuoritukset = osasuoritusTyypit.map { tyyppi =>
      val osakokeenArvosana = sallitutArvosanat(Random.nextInt(sallitutArvosanat.length))
      createOsakoe(tyyppi, osakokeenArvosana, arviointipäivä)
    }

    KielitutkinnonOpiskeluoikeus(
      tila = KielitutkinnonOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
            alku = tutkintopäivä,
            tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
          ),
          KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
            alku = arviointipäivä,
            tila = Koodistokoodiviite("hyvaksytystisuoritettu", "koskiopiskeluoikeudentila")
          )
        )
      ),
      suoritukset = List(
        YleisenKielitutkinnonSuoritus(
          koulutusmoduuli = YleinenKielitutkinto(
            tunniste = Koodistokoodiviite(tutkintotaso, "ykitutkintotaso"),
            kieli = Koodistokoodiviite(kieli, "kieli")
          ),
          toimipiste = OidOrganisaatio(MockOrganisaatiot.YleinenKielitutkintoOrg.organisaatio),
          järjestäjä = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
          vahvistus = Some(Päivämäärävahvistus(
            päivä = arviointipäivä,
            myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki)
          )),
          osasuoritukset = Some(osasuoritukset),
          yleisarvosana = None
        )
      )
    )
  }

  private def createOsakoe(tyyppi: String, arvosana: String, arviointipäivä: LocalDate): YleisenKielitutkinnonOsakokeenSuoritus = {
    YleisenKielitutkinnonOsakokeenSuoritus(
      koulutusmoduuli = YleisenKielitutkinnonOsakoe(
        tunniste = Koodistokoodiviite(tyyppi, "ykisuorituksenosa")
      ),
      arviointi = Some(List(
        YleisenKielitutkinnonOsakokeenArviointi(
          arvosana = Koodistokoodiviite(arvosana, "ykiarvosana"),
          päivä = arviointipäivä
        )
      ))
    )
  }

  private def saveOidsToFile(oids: List[String]): Unit = {
    val writer = new PrintWriter(outputFile)
    try {
      oids.foreach(writer.println)
    } finally {
      writer.close()
    }
  }
}
