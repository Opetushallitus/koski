package fi.oph.koski.migration

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.raportointikanta.{DelayedScheduler, RaportointiDatabase}
import fi.oph.koski.util.Wait
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class MigrationSpec extends AnyFreeSpec with Matchers {
  "Migraatiot" - {
    "Havaittiin uusi tietokannan migraatiotiedosto. Migraatiot, varsinkin jos koskevat Kosken suurimpia tauluja, on hyvä testata tietokantareplikaa vasten.\n" +
      "Korjaa tämän testin odottama tiedostomäärä, kun olet varma että migraatiot voi viedä eteenpäin.\nDokumentaatio: documentation/tietokantamigraatiot.md" in {
      new File("./src/main/resources/db/migration").listFiles.length should equal (92)
    }
  }

  "Raportointikannan skeema" - {
    "Skeema ei ole muuttunut tai skeeman versionumero on päivitetty" in {
      KoskiApplicationForTests.raportointikantaService.loadRaportointikanta(force = false)
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
        "HenkiloLoader.scala"                                       -> "8bb9b09ac2dd771c741dff417b34f79e",
        "KoodistoLoader.scala"                                      -> "86c90ec069d1c5ec5ee9faa65fb1399e",
        "KoskiEventBridgeClient.scala"                              -> "f8a09d358ebb3fe2ed4d8875ccccef12",
        "LoaderUtils.scala"                                         -> "38d31b4d1cfa5e3892083bb39f7f0047",
        "MuuAmmatillinenRaporttiRowBuilder.scala"                   -> "31774fb0fbd06a775a07325e867a951f",
        "OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.scala" -> "47d7ee909d283a47f9240d804d5ddde5",
        "OpiskeluoikeusLoader.scala"                                -> "2a9a7500cb21a6eda52007c55ea8b734",
        "OppivelvollisuudenVapautusLoader.scala"                    -> "2870707413fff5719b7cb7063dd424c4",
        "OrganisaatioHistoriaRowBuilder.scala"                      -> "7e586d9e273a5a4ee7beae257f22c7f4",
        "OrganisaatioLoader.scala"                                  -> "9e2e45da33ed335af4a7b0a31b139a7",
        "PäivitettyOpiskeluoikeusLoader.scala"                      -> "500545bbe7ef47dedcfdc49580b536d2",
        "RaportointiDatabase.scala"                                 -> "89c0d7b5728af32f646dc2250cf5f8cc",
        "RaportointiDatabaseCustomFunctions.scala"                  -> "956f101d1219c49ac9134b72a30caf3a",
        "RaportointiDatabaseSchema.scala"                           -> "3d5bc4b883feb97ef83e1f3d79aa5386",
        "RaportointikantaService.scala"                             -> "7f595703bc78f62b2c1aecb14c0ff63e",
        "RaportointikantaStatusServlet.scala"                       -> "9fd6f796adfb2034cce0151b7330cd1a",
        "RaportointikantaTestServlet.scala"                         -> "d457be86e60dd84545378ae415236d26",
        "RaportointikantaTableQueries.scala"                        -> "b97f971fa7a5896ec3c4d69882ca705d",
        "TOPKSAmmatillinenRaporttiRowBuilder.scala"                 -> "a9c26a13385ff576810f3ef831240437",
        "OpiskeluoikeusLoaderRowBuilder.scala"                      -> "8675b543f25ed37260c6c7843c08eb59",
        "IncrementalUpdateOpiskeluoikeusLoader.scala"               -> "fd0687df6c413b981947246909b1c91d",
        "FullReloadOpiskeluoikeusLoader.scala"                      -> "925831fbb6bc923c49f6f87f8827f8d",
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
