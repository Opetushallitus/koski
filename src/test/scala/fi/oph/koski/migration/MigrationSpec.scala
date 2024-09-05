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
      new File("./src/main/resources/db/migration").listFiles.length should equal (99)
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
        "AikajaksoRowBuilder.scala"                                 -> "581190491d31f3b0d4a40c2e62579ffa",
        "HenkiloLoader.scala"                                       -> "265255880a228c9b0e47e80064e8276",
        "KoodistoLoader.scala"                                      -> "86c90ec069d1c5ec5ee9faa65fb1399e",
        "KoskiEventBridgeClient.scala"                              -> "f8a09d358ebb3fe2ed4d8875ccccef12",
        "LoaderUtils.scala"                                         -> "38d31b4d1cfa5e3892083bb39f7f0047",
        "MuuAmmatillinenRaporttiRowBuilder.scala"                   -> "31774fb0fbd06a775a07325e867a951f",
        "OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.scala" -> "6ff94ec559730f377c3972cf1a0b4122",
        "OpiskeluoikeusLoader.scala"                                -> "facf5375f6810f49851861690553e97e",
        "OppivelvollisuudenVapautusLoader.scala"                    -> "2870707413fff5719b7cb7063dd424c4",
        "OrganisaatioHistoriaRowBuilder.scala"                      -> "7e586d9e273a5a4ee7beae257f22c7f4",
        "OrganisaatioLoader.scala"                                  -> "4f7ce51cb922dbc11e694ebe85ab3472",
        "PäivitettyOpiskeluoikeusLoader.scala"                      -> "500545bbe7ef47dedcfdc49580b536d2",
        "RaportointiDatabase.scala"                                 -> "de5931130d7a8c036110b0f63e67f632",
        "RaportointiDatabaseCustomFunctions.scala"                  -> "956f101d1219c49ac9134b72a30caf3a",
        "RaportointiDatabaseSchema.scala"                           -> "7c95e730f735c95df3d71dad9ecf9e67",
        "RaportointikantaService.scala"                             -> "5b9fb17cf7759c509d0a7e3e4d4d2a8c",
        "RaportointikantaStatusServlet.scala"                       -> "9fd6f796adfb2034cce0151b7330cd1a",
        "RaportointikantaTestServlet.scala"                         -> "d457be86e60dd84545378ae415236d26",
        "RaportointikantaTableQueries.scala"                        -> "b97f971fa7a5896ec3c4d69882ca705d",
        "TOPKSAmmatillinenRaporttiRowBuilder.scala"                 -> "a9c26a13385ff576810f3ef831240437",
        "OpiskeluoikeusLoaderRowBuilder.scala"                      -> "e3a98a9b54db53d331c19289de89b99",
        "IncrementalUpdateOpiskeluoikeusLoader.scala"               -> "60c3fe0ffb2e2cde5dfc013954c491be",
        "FullReloadOpiskeluoikeusLoader.scala"                      -> "319c856f45120917695493852bfda577",
        "VipunenExport.scala"                                       -> "9563ec86b4214f871fbd085e44c048cb",
      )

      val errors = getListOfFiles(dir).flatMap(file => {
        val source = Source.fromFile(file)
        val actualChecksum = md5(source.getLines.mkString)
        source.close

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
