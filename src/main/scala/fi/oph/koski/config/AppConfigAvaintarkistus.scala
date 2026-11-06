package fi.oph.koski.config

// scalafix:off DisableSyntax.println

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigUtil, ConfigValue}

import java.nio.charset.StandardCharsets.UTF_8
import scala.io.Source
import scala.jdk.CollectionConverters._

// CI ajaa tämän ennen asennusta: ympäristön AppConfigissa on oltava jokainen reference.confin avain,
// jotta pilvessä ei käytetä paikallisen kehityksen oletusarvoja. AppConfig luetaan stdinistä, ja
// tulosteessa on vain avainten nimiä, ei arvoja.
object AppConfigAvaintarkistus {
  case class Tulos(puuttuvat: List[String], ylimääräiset: List[String])

  def main(args: Array[String]): Unit = {
    val appConfig = ConfigFactory.parseString(Source.fromInputStream(System.in, UTF_8.name).mkString)
    val tulos = tarkista(koskenReferenceConf, appConfig)

    if (tulos.ylimääräiset.nonEmpty) {
      println(s"AppConfigissa on ${tulos.ylimääräiset.size} avainta, joita reference.confissa ei ole:")
      tulos.ylimääräiset.foreach(avain => println(s"  $avain"))
    }

    if (tulos.puuttuvat.nonEmpty) {
      println(s"AppConfigista puuttuu ${tulos.puuttuvat.size} reference.confin avainta:")
      tulos.puuttuvat.foreach(avain => println(s"  $avain"))
      System.exit(1)
    }

    println("AppConfigissa on kaikki reference.confin avaimet.")
  }

  def tarkista(reference: Config, appConfig: Config): Tulos = {
    val referenceAvaimet = lehdet(reference).map(_._1).toSet
    val appConfigAvaimet = lehdet(appConfig).map(_._1).toSet
    Tulos(
      puuttuvat = (referenceAvaimet -- appConfigAvaimet).toList.sorted,
      ylimääräiset = (appConfigAvaimet -- referenceAvaimet).toList.sorted
    )
  }

  // Puu käydään läpi ratkaisematta viittauksia, jotta ${...}-arvoja ei tarvitse ratkaista tarkistuksessa.
  private[config] def lehdet(config: Config): List[(String, ConfigValue)] = lehdet(config.root, Nil)

  private def lehdet(objekti: ConfigObject, polku: List[String]): List[(String, ConfigValue)] =
    objekti.asScala.toList.flatMap { (avainJaArvo: (String, ConfigValue)) =>
      avainJaArvo match {
        case (avain, lapsi: ConfigObject) => lehdet(lapsi, polku :+ avain)
        case (avain, arvo) => List(ConfigUtil.joinPath((polku :+ avain).asJava) -> arvo)
      }
    }

  // Luokkapolulla on myös kirjastojen reference.conf-tiedostoja (esim. Slick), joiden avaimia AppConfigissa ei tarvita.
  private[koski] def koskenReferenceConf: Config = {
    val koodinSijainti = getClass.getProtectionDomain.getCodeSource.getLocation.toString
    val tiedosto = getClass.getClassLoader.getResources("reference.conf").asScala
      .find(_.toString.contains(koodinSijainti))
      .getOrElse(throw new IllegalStateException(s"Kosken reference.conf ei löydy sijainnista $koodinSijainti"))
    ConfigFactory.parseURL(tiedosto)
  }
}
