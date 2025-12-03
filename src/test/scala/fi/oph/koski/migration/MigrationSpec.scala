package fi.oph.koski.migration

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.raportointikanta.{DelayedScheduler, RaportointiDatabase, RaportointikantaTestMethods}
import fi.oph.koski.util.Wait
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class MigrationSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods {
  "Migraatiot" - {
    "Havaittiin uusi tietokannan migraatiotiedosto. Migraatiot, varsinkin jos koskevat Kosken suurimpia tauluja, on hyvä testata tietokantareplikaa vasten.\n" +
      "Korjaa tämän testin odottama tiedostomäärä, kun olet varma että migraatiot voi viedä eteenpäin.\nDokumentaatio: documentation/tietokantamigraatiot.md" in {
      new File("./src/main/resources/db/migration").listFiles.length should equal (101)
    }
  }

  "Raportointikannan skeema" - {
    "Skeema ei ole muuttunut tai skeeman versionumero on päivitetty" in {
      reloadRaportointikanta()
      Wait.until(KoskiApplicationForTests.raportointikantaService.isLoadComplete)

      val hash = KoskiApplicationForTests.raportointiDatabase.getSchemaHash("public")

      if (hash != RaportointiDatabase.schemaVersion._2) {
        val newVersion = RaportointiDatabase.schemaVersion._1 + 1
        println(s"""Raportointikannan skeema on muuttunut. Päivitä RaportointiDatabase-objektiin versio seuraavanlaiseksi:\n\n\tdef schemaVersion: (Int, String) = ($newVersion, "$hash")""")
        RaportointiDatabase.schemaVersion should equal((newVersion, hash))
      }
    }

    "Havaittiin mahdollinen muutos raportointikannan skeemassa. Skeemamuutokset saattavat rikkoa inkrementaalisen kantageneroinnin.\n" +
      "Muutoksen vieminen tuotantoon vaatii raportointikannan full-reload-generoinnin ympäristöihin.\n" +
      "Korjaa tämän testin md5-tarkastusluvut vasta kun olet varma siitä että nykyinen toteutus voidaan viedä eteenpäin." in {
      val dir = "./src/main/scala/fi/oph/koski/raportointikanta"
      val expectedChecksums = Map(
        "AikajaksoRowBuilder.scala"                                 -> "a5dffa8b6fb090736db7c999b2b0c119",
        "HenkiloLoader.scala"                                       -> "f4859d357500b77712f02c89b08b6a3c",
        "KoodistoLoader.scala"                                      -> "86c90ec069d1c5ec5ee9faa65fb1399e",
        "KoskiEventBridgeClient.scala"                              -> "2dab293a49a8500a68d52c4d2d510973",
        "LoaderUtils.scala"                                         -> "38d31b4d1cfa5e3892083bb39f7f0047",
        "MuuAmmatillinenRaporttiRowBuilder.scala"                   -> "31774fb0fbd06a775a07325e867a951f",
        "OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.scala" -> "6ff94ec559730f377c3972cf1a0b4122",
        "OpiskeluoikeusLoader.scala"                                -> "90385e1b2915c500e4256ee61a074068",
        "OppivelvollisuudenVapautusLoader.scala"                    -> "2870707413fff5719b7cb7063dd424c4",
        "OrganisaatioHistoriaRowBuilder.scala"                      -> "7e586d9e273a5a4ee7beae257f22c7f4",
        "OrganisaatioLoader.scala"                                  -> "4f7ce51cb922dbc11e694ebe85ab3472",
        "PäivitettyOpiskeluoikeusLoader.scala"                      -> "500545bbe7ef47dedcfdc49580b536d2",
        "RaportointiDatabase.scala"                                 -> "174046c53277f70a1907ec41a04134bb",
        "RaportointiDatabaseCustomFunctions.scala"                  -> "956f101d1219c49ac9134b72a30caf3a",
        "RaportointiDatabaseSchema.scala"                           -> "93bb1adbbf20cbeeb8cd4f2d88832f47",
        "RaportointikantaService.scala"                             -> "2a8e3cfa70e2c5d32115aff434a890a9",
        "RaportointikantaStatusServlet.scala"                       -> "bfb4d4d668ecbff866468ae2dc5c1e0b",
        "RaportointikantaTestServlet.scala"                         -> "ad92e33c2f816ed65c0693f5dc0143b4",
        "RaportointikantaTableQueries.scala"                        -> "f2f26c217992539c1e61dcbd031fc642",
        "TOPKSAmmatillinenRaporttiRowBuilder.scala"                 -> "a9c26a13385ff576810f3ef831240437",
        "OpiskeluoikeusLoaderRowBuilder.scala"                      -> "7736a9e94bfd7ffe5aa98b63cf1bf8d2",
        "IncrementalUpdateOpiskeluoikeusLoader.scala"               -> "7bfac44029f19738ce8a51b8c5e51923",
        "FullReloadOpiskeluoikeusLoader.scala"                      -> "8d516ad76ced8f614c125948a3dcc7e6",
        "VipunenExport.scala"                                       -> "e01d5a8dedee5127212210694624131f",
      )

      val errors = getListOfFiles(dir).flatMap(file => {
        val source = Source.fromFile(file)
        val actualChecksum = md5(source.getLines().mkString)
        source.close()

        val filename = file.getName
        val expectedChecksum = expectedChecksums.get(filename)

        (actualChecksum, expectedChecksum) match {
          case (a, None)                => Some(s"Tiedoston $filename tarkastuslukua ($a) ei ole lisätty expectedChecksums-listaan")
          case (a, Some(e)) if a != e   => Some(s"Tarkastusluku tiedostolle $filename on muuttunut: nykyinen = $a, odotettu = $e")
          case _                        => None
        }
      })

      errors should equal(List.empty)
    }
  }

  private def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    d.exists should equal(true)
    d.isDirectory should equal(true)
    d.listFiles.filter(_.isFile).toList
  }

  private def md5(input: String): String = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("MD5")
    val digest: Array[Byte] = md.digest(input.getBytes)
    val bigInt = new BigInteger(1, digest)
    bigInt.toString(16).trim
  }
}
