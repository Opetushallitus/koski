package fi.oph.koski.config

// scalafix:off DisableSyntax.println

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigRenderOptions, ConfigUtil, ConfigValue}

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

// Täydentää AppConfig-tiedoston reference.confin avaimilla ja järjestää avaimet reference.confin järjestykseen
// {}-lohkoihin. AppConfigin arvot ja kommentit säilyvät, ja puuttuva avain saa reference.confin oletuksen.
// reference.confin "Ei käytössä pilvessä:" -kommentit kopioidaan AppConfigiin avaimen yläpuolelle.
object AppConfigYhdistys {
  case class Tulos(tiedosto: String, lisätyt: List[String], paikallisetOletukset: List[String])

  private val PilviMerkintä = "Ei käytössä pilvessä:"
  private val PaikallinenArvo = """mock|localhost|localstack|127\.0\.0\.1|0:0:0:0:0:0:0:1""".r
  // JSON-muodossa merkkijonot ovat aina lainausmerkeissä, ja viittaukset ${...} säilyvät. HOCON-muodossa sama
  // merkkijono renderöityisi eri tavoin sen mukaan, oliko se lainausmerkeissä.
  private val Lyhyt = ConfigRenderOptions.concise.setJson(true)
  private val Pitkä = ConfigRenderOptions.defaults.setOriginComments(false).setComments(false).setJson(true)
  private val PitkänArvonRaja = 100
  private val Sisennys = "  "

  // Tulos kirjoitetaan tiedostoon, koska exec:java ohjaa myös stderrin stdoutiin.
  def main(args: Array[String]): Unit = {
    val Array(syötePolku, tulosPolku) = args
    val syöte = Files.readString(Paths.get(syötePolku), UTF_8)
    val tulos = yhdistä(AppConfigAvaintarkistus.koskenReferenceConf, ConfigFactory.parseString(syöte))
    Files.writeString(Paths.get(tulosPolku), tulos.tiedosto, UTF_8)

    val kadonneet = kadonneetKommentit(syöte, tulos.tiedosto)
    if (kadonneet.nonEmpty) {
      println(s"${kadonneet.size} kommenttia jäi pois, koska tyhjä rivi erotti ne avaimesta tai avain määriteltiin myöhemmin uudelleen:")
      kadonneet.foreach(kommentti => println(s"  $kommentti"))
    }
    if (tulos.lisätyt.nonEmpty) {
      println(s"Lisättiin ${tulos.lisätyt.size} avainta reference.confin oletusarvoilla:")
      tulos.lisätyt.foreach(avain => println(s"  $avain"))
    }
    if (tulos.paikallisetOletukset.nonEmpty) {
      println(s"VAROITUS: ${tulos.paikallisetOletukset.size} lisätyn avaimen oletus näyttää paikalliselta arvolta, tarkista ne:")
      tulos.paikallisetOletukset.foreach(avain => println(s"  $avain"))
    }
    println(s"Kirjoitettiin $tulosPolku")
  }

  def yhdistä(reference: Config, appConfig: Config): Tulos = {
    val appConfigArvot = AppConfigAvaintarkistus.lehdet(appConfig)
    val appConfigArvo = appConfigArvot.toMap
    val referenceArvot = AppConfigAvaintarkistus.lehdet(reference).sortBy { case (avain, arvo) => (arvo.origin.lineNumber, avain) }
    val referenceAvaimet = referenceArvot.map(_._1).toSet
    val lisätyt = referenceArvot.filterNot { case (avain, _) => appConfigArvo.contains(avain) }

    val juuri = new Haara(Nil)
    val objektienKommentit = kommentitPoluittain(appConfig.root, Nil)
    def lisää(avain: String, arvo: ConfigValue, kommentit: List[String]): Unit = {
      val polku = ConfigUtil.splitPath(avain).asScala.toList
      val haara = polku.init.inits.toList.reverse.drop(1).foldLeft(juuri) { (vanhempi, objektinPolku) =>
        vanhempi.haara(objektinPolku.last, objektienKommentit.getOrElse(objektinPolku, Nil))
      }
      haara.lapset(polku.last) = Lehti(arvo, kommentit)
    }
    referenceArvot.foreach { case (avain, oletus) =>
      val pilviKommentit = eiKäytössäPilvessä(oletus)
      appConfigArvo.get(avain) match {
        case Some(arvo) =>
          val omat = arvo.origin.comments.asScala.toList
          lisää(avain, arvo, omat ++ pilviKommentit.filterNot(kommentti => omat.exists(_.trim == kommentti.trim)))
        case None => lisää(avain, oletus, pilviKommentit)
      }
    }
    appConfigArvot
      .filterNot { case (avain, _) => referenceAvaimet.contains(avain) }
      .sortBy(_._2.origin.lineNumber)
      .foreach { case (avain, arvo) => lisää(avain, arvo, arvo.origin.comments.asScala.toList) }

    val tiedosto = juuri.lapset.toList.map { case (nimi, solmu) => rivit(List(nimi), solmu, "").mkString("\n") }.mkString("", "\n\n", "\n")
    varmistaArvojenSäilyminen(appConfigArvot, tiedosto)

    Tulos(
      tiedosto,
      lisätyt.map(_._1),
      lisätyt.collect {
        case (avain, oletus) if PaikallinenArvo.findFirstIn(oletus.render(Lyhyt)).isDefined && eiKäytössäPilvessä(oletus).isEmpty => avain
      }
    )
  }

  private def eiKäytössäPilvessä(oletus: ConfigValue): List[String] =
    oletus.origin.comments.asScala.toList.filter(_.trim.startsWith(PilviMerkintä))

  private sealed trait Solmu
  private case class Lehti(arvo: ConfigValue, kommentit: List[String]) extends Solmu
  private class Haara(val kommentit: List[String]) extends Solmu {
    val lapset: mutable.LinkedHashMap[String, Solmu] = mutable.LinkedHashMap.empty
    def haara(nimi: String, kommentit: => List[String]): Haara = lapset.getOrElseUpdate(nimi, new Haara(kommentit)) match {
      case haara: Haara => haara
      case _: Lehti => throw new IllegalStateException(s"$nimi on sekä arvo että objekti")
    }
  }

  // Objekti, jolla on vain yksi lapsi, kirjoitetaan pisteellä erotettuna polkuna eikä omana lohkonaan.
  private def rivit(polku: List[String], solmu: Solmu, sisennys: String): List[String] = solmu match {
    case haara: Haara if haara.lapset.size == 1 =>
      val (nimi, lapsi) = haara.lapset.head
      val lapsenRivit = rivit(polku :+ nimi, lapsi, sisennys)
      kommenttirivit(haara.kommentit, sisennys) ++ lapsenRivit
    case haara: Haara =>
      kommenttirivit(haara.kommentit, sisennys) ++
        List(s"$sisennys${ConfigUtil.joinPath(polku.asJava)} {") ++
        haara.lapset.toList.flatMap { case (nimi, lapsi) => rivit(List(nimi), lapsi, sisennys + Sisennys) } ++
        List(s"$sisennys}")
    case Lehti(arvo, kommentit) =>
      val arvonRivit = arvoRiveinä(arvo)
      kommenttirivit(kommentit, sisennys) ++
        List(s"$sisennys${ConfigUtil.joinPath(polku.asJava)} = ${arvonRivit.head}") ++
        arvonRivit.tail.map(sisennys + _)
  }

  private def kommenttirivit(kommentit: List[String], sisennys: String): List[String] =
    kommentit.map(kommentti => s"$sisennys#$kommentti")

  // Pitkä lista tai objekti jaetaan riveille. Typesafe sisentää neljällä välilyönnillä, tiedosto kahdella.
  private def arvoRiveinä(arvo: ConfigValue): List[String] = {
    val lyhyt = arvo.render(Lyhyt)
    if (lyhyt.length <= PitkänArvonRaja) {
      List(lyhyt)
    } else {
      arvo.render(Pitkä).linesIterator.toList.map { rivi =>
        val välilyönnit = rivi.takeWhile(_ == ' ').length
        Sisennys * (välilyönnit / 4) + rivi.drop(välilyönnit)
      }
    }
  }

  private val Kommenttirivi = """^\s*(?:#|//)(.*)$""".r

  // HOCON-jäsennin jättää pois kommentit, joita ei ole kiinnitetty avaimeen.
  private[koski] def kadonneetKommentit(syöte: String, tiedosto: String): List[String] = {
    def kommentit(teksti: String) = teksti.linesIterator.collect { case Kommenttirivi(kommentti) => kommentti.trim }.filter(_.nonEmpty).toList
    val säilyneet = kommentit(tiedosto).toSet
    kommentit(syöte).filterNot(säilyneet.contains)
  }

  private def kommentitPoluittain(objekti: ConfigObject, polku: List[String]): Map[List[String], List[String]] =
    objekti.asScala.toList.collect { case (avain, lapsi: ConfigObject) =>
      val lapsenPolku = polku :+ avain
      kommentitPoluittain(lapsi, lapsenPolku) + (lapsenPolku -> lapsi.origin.comments.asScala.toList)
    }.foldLeft(Map.empty[List[String], List[String]])(_ ++ _)

  private def varmistaArvojenSäilyminen(alkuperäiset: List[(String, ConfigValue)], tiedosto: String): Unit = {
    val uudet = AppConfigAvaintarkistus.lehdet(ConfigFactory.parseString(tiedosto)).toMap
    val muuttuneet = alkuperäiset.collect {
      case (avain, arvo) if !uudet.get(avain).map(_.render(Lyhyt)).contains(arvo.render(Lyhyt)) => avain
    }
    if (muuttuneet.nonEmpty) {
      throw new IllegalStateException(s"Yhdistäminen muutti AppConfigin arvoja: ${muuttuneet.mkString(", ")}")
    }
  }
}
