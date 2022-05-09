package fi.oph.koski.migration

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class MigrationSpec extends AnyFreeSpec with Matchers {
  "Migraatiot" - {
    "Havaittiin uusi tietokannan migraatiotiedosto. Migraatiot, varsinkin jos koskevat Kosken suurimpia tauluja, on hyvä testata tietokantareplikaa vasten.\n" +
      "Korjaa tämän testin odottama tiedostomäärä, kun olet varma että migraatiot voi viedä eteenpäin.\nDokumentaatio: documentation/tietokantamigraatiot.md" in {
      new File("./src/main/resources/db/migration").listFiles.length should equal (82)
    }
  }

  "Raportointikannan skeema" - {
    "Havaittiin mahdollinen muutos raportointikannan skeemassa. Skeemamuutokset saattavat rikkoa inkrementaalisen kantageneroinnin.\n" +
      "Muutoksen vieminen tuotantoon vaatii raportointikannan full-reload-generoinnin ympäristöihin.\n" +
      "Korjaa tämän testin md5-tarkastusluvut vasta kun olet varma siitä että nykyinen toteutus voidaan viedä eteenpäin." in {
      val dir = "./src/main/scala/fi/oph/koski/raportointikanta"
      val expectedChecksums = Map(
        "AikajaksoRowBuilder.scala"                                 -> "4ad00ba7d178ded3ab275d0f3c06f44a",
        "HenkiloLoader.scala"                                       -> "8bb9b09ac2dd771c741dff417b34f79e",
        "KoodistoLoader.scala"                                      -> "86c90ec069d1c5ec5ee9faa65fb1399e",
        "KoskiEventBridgeClient.scala"                              -> "f8a09d358ebb3fe2ed4d8875ccccef12",
        "LoaderUtils.scala"                                         -> "38d31b4d1cfa5e3892083bb39f7f0047",
        "MuuAmmatillinenRaporttiRowBuilder.scala"                   -> "31774fb0fbd06a775a07325e867a951f",
        "OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset.scala" -> "4498412891d88ddd2daf422144664086",
        "OpiskeluoikeusLoader.scala"                                -> "f0be4321ad6636aefb29aaed664dcedf",
        "OrganisaatioHistoriaRowBuilder.scala"                      -> "7e586d9e273a5a4ee7beae257f22c7f4",
        "OrganisaatioLoader.scala"                                  -> "9e2e45da33ed335af4a7b0a31b139a7",
        "RaportointiDatabase.scala"                                 -> "f9b0d909dea4de87d6fadcea95f525ef",
        "RaportointiDatabaseCustomFunctions.scala"                  -> "956f101d1219c49ac9134b72a30caf3a",
        "RaportointiDatabaseSchema.scala"                           -> "f40e7c556d42a721d64479d011c562c5",
        "RaportointikantaService.scala"                             -> "136644dcdfda656708301529e0d068f3",
        "RaportointikantaServlet.scala"                             -> "2eaa7fc778aac5db6aaca76de5745e06",
        "RaportointikantaTableQueries.scala"                        -> "b97f971fa7a5896ec3c4d69882ca705d",
        "TOPKSAmmatillinenRaporttiRowBuilder.scala"                 -> "a9c26a13385ff576810f3ef831240437",
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
