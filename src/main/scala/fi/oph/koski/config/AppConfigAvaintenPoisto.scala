package fi.oph.koski.config

// scalafix:off DisableSyntax.println

import com.typesafe.config.{Config, ConfigFactory}

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

// Poistaa AppConfig-tiedostosta avaimet, joita koodi ei lue. Tiedosto kirjoitetaan samassa muodossa kuin
// AppConfigYhdistys sen kirjoittaa, joten muut avaimet, arvot ja kommentit säilyvät.
object AppConfigAvaintenPoisto {
  case class Tulos(tiedosto: String, poistetut: List[String], puuttuneet: List[String])

  // Tulos kirjoitetaan tiedostoon, koska exec:java ohjaa myös stderrin stdoutiin.
  def main(args: Array[String]): Unit = {
    val (syötePolku, tulosPolku, avaimet) = (args(0), args(1), args.drop(2).toList)
    val syöte = Files.readString(Paths.get(syötePolku), UTF_8)
    val appConfig = ConfigFactory.parseString(syöte)
    val tulos = poista(AppConfigAvaintarkistus.koskenReferenceConf, appConfig, avaimet)
    Files.writeString(Paths.get(tulosPolku), tulos.tiedosto, UTF_8)

    println(s"Poistettiin ${tulos.poistetut.size} avainta:")
    tulos.poistetut.foreach(avain => println(s"  $avain"))
    if (tulos.puuttuneet.nonEmpty) {
      println(s"${tulos.puuttuneet.size} avainta ei ollut tiedostossa:")
      tulos.puuttuneet.foreach(avain => println(s"  $avain"))
    }
    val poistettujenKommentit = tulos.poistetut
      .flatMap(avain => AppConfigAvaintarkistus.lehdet(appConfig.withOnlyPath(avain)))
      .flatMap(_._2.origin.comments.asScala)
      .map(_.trim)
    val kadonneet = AppConfigYhdistys.kadonneetKommentit(syöte, tulos.tiedosto).filterNot(poistettujenKommentit.contains)
    if (kadonneet.nonEmpty) {
      println(s"${kadonneet.size} muuta kommenttia jäi pois, tarkista ne:")
      kadonneet.foreach(kommentti => println(s"  $kommentti"))
    }
    println(s"Kirjoitettiin $tulosPolku")
  }

  def poista(reference: Config, appConfig: Config, avaimet: List[String]): Tulos = {
    val referenceAvaimet = AppConfigAvaintarkistus.lehdet(reference).map(_._1)
    val luettavat = avaimet.filter(avain => referenceAvaimet.exists(r => r == avain || r.startsWith(s"$avain.")))
    if (luettavat.nonEmpty) {
      throw new IllegalArgumentException(
        s"reference.confissa on vielä ${luettavat.mkString(", ")}. Poista avain ensin reference.confista ja vie muutos masteriin."
      )
    }

    val (poistetut, puuttuneet) = avaimet.partition(appConfig.hasPathOrNull)
    val yhdistetty = AppConfigYhdistys.yhdistä(reference, poistetut.foldLeft(appConfig)(_.withoutPath(_)))
    if (yhdistetty.lisätyt.nonEmpty) {
      throw new IllegalStateException(
        s"AppConfigista puuttuu reference.confin avaimia: ${yhdistetty.lisätyt.mkString(", ")}. Lisää ne ennen poistoa."
      )
    }
    Tulos(yhdistetty.tiedosto, poistetut, puuttuneet)
  }
}
