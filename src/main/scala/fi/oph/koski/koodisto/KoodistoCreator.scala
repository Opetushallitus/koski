package fi.oph.koski.koodisto

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonDiff.objectDiff
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import org.json4s.jackson.JsonMethods

case class KoodistoCreator(application: KoskiApplication) extends Logging {
  private val config = application.config
  private lazy val kp = application.koodistoPalvelu
  private lazy val kmp = KoodistoMuokkausPalvelu(config)

  private val createMissingStr = config.getString("koodisto.create")
  private val updateExistingStr = config.getString("koodisto.update")

  private val updateable = Koodistot.koodistot.filter { koodistoUri =>
    updateExistingStr match {
      case "all" => true
      case "koskiKoodistot" => Koodistot.koskiKoodistot.contains(koodistoUri)
      case "muutKoodistot" => Koodistot.muutKoodistot.contains(koodistoUri)
      case _ => updateExistingStr.split(",").contains(koodistoUri)
    }
  }
  private val createable = Koodistot.koodistot.filter { koodistoUri =>
    createMissingStr match {
      case "all" => true
      case "koskiKoodistot" => Koodistot.koskiKoodistot.contains(koodistoUri)
      case "muutKoodistot" => Koodistot.muutKoodistot.contains(koodistoUri)
      case "true" => Koodistot.koskiKoodistot.contains(koodistoUri) // the former default case
      case _ => createMissingStr.split(",").contains(koodistoUri)
    }
  }

  def createAndUpdateCodesBasedOnMockData {
    val codesToCheck = (updateable ++ createable).distinct
    if (codesToCheck.nonEmpty) {
      logger.info(s"Aloitetaan ${codesToCheck.length} koodiston tarkistus")
      luoPuuttuvatKoodistot
      päivitäOlemassaOlevatKoodistot

      val päivitettävätJaLuotavat = codesToCheck.par.map { koodistoUri =>
        def sortListsInside(k: KoodistoKoodi) = k.copy(metadata = k.metadata.sortBy(_.kieli), withinCodeElements = k.withinCodeElements.map(_.sortBy(_.codeElementUri)))

        val koodistoViite: KoodistoViite = kp.getLatestVersion(koodistoUri).getOrElse(throw new Exception("Koodistoa ei löydy: " + koodistoUri))
        val olemassaOlevatKoodit: List[KoodistoKoodi] = kp.getKoodistoKoodit(koodistoViite).toList.flatten.map(sortListsInside)
        val mockKoodit: List[KoodistoKoodi] = MockKoodistoPalvelu().getKoodistoKoodit(koodistoViite).toList.flatten.map(sortListsInside)

        val result = (luotavatKoodit(koodistoUri, olemassaOlevatKoodit, mockKoodit), päivitettävätKoodit(koodistoUri, olemassaOlevatKoodit, mockKoodit))
        result
      }

      logger.info("Koodistot tarkistettu")

      val luotavat = päivitettävätJaLuotavat.flatMap(_._1).toList
      if (luotavat.nonEmpty) {
        logger.info("Aloitetaan puuttuvien koodien lisäys")
        luoPuuttuvatKoodit(luotavat)
        logger.info("Puuttuvat koodit lisätty")
      }
      val päivitettävät = päivitettävätJaLuotavat.flatMap(_._2).toList
      if (päivitettävät.nonEmpty) {
        logger.info("Aloitetaan muuttuneiden koodien päivitys")
        päivitäKoodit(päivitettävät)
        logger.info("Muuttuneet koodit päivitetty")
      }
    }
  }

  private def luotavatKoodit(koodistoUri: String, olemassaOlevatKoodit: List[KoodistoKoodi], mockKoodit: List[KoodistoKoodi]) = {
    if (createable.contains(koodistoUri)) {
      mockKoodit
        .filter { koodi: KoodistoKoodi => !olemassaOlevatKoodit.find(_.koodiArvo == koodi.koodiArvo).isDefined }
        .map(k => (koodistoUri, k))
    } else {
      Nil
    }
  }

  private def luoPuuttuvatKoodit(luotavatKoodit: List[(String, KoodistoKoodi)]) = {
    luotavatKoodit.zipWithIndex.foreach { case ((koodistoUri, koodi), index) =>
      logger.info("Luodaan koodi (" + (index + 1) + "/" + (luotavatKoodit.length) + ") " + koodi.koodiUri)
      kmp.createKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
    }
  }

  private def päivitettävätKoodit(koodistoUri: String, olemassaOlevatKoodit: List[KoodistoKoodi], mockKoodit: List[KoodistoKoodi]) = {
    if (updateable.contains(koodistoUri)) {
      olemassaOlevatKoodit.flatMap { vanhaKoodi =>
        mockKoodit.find(_.koodiArvo == vanhaKoodi.koodiArvo).flatMap { uusiKoodi =>
          val uusiKoodiSamallaKoodiUrilla = uusiKoodi.copy(
            koodiUri = vanhaKoodi.koodiUri
          )

          if (uusiKoodiSamallaKoodiUrilla != vanhaKoodi) {
            Some(koodistoUri, vanhaKoodi, uusiKoodi)
          } else {
            None
          }
        }
      }
    } else {
      Nil
    }
  }

  private def päivitäKoodit(päivitettävätKoodit: List[(String, KoodistoKoodi, KoodistoKoodi)]) = {
    päivitettävätKoodit.zipWithIndex.foreach { case ((koodistoUri, vanhaKoodi, uusiKoodi), index) =>
      logger.info("Päivitetään koodi (" + (index + 1) + "/" + (päivitettävätKoodit.length) + ") " + uusiKoodi.koodiUri + " diff " + JsonMethods.compact(objectDiff(vanhaKoodi, uusiKoodi)) + " original " + JsonSerializer.writeWithRoot(vanhaKoodi))
      kmp.updateKoodi(koodistoUri, uusiKoodi.copy(
        voimassaAlkuPvm = Some(LocalDate.now),
        tila = uusiKoodi.tila.orElse(vanhaKoodi.tila),
        version = uusiKoodi.version.orElse(vanhaKoodi.version)
      ))
    }
  }

  private def päivitäOlemassaOlevatKoodistot = {
    // update existing
    val olemassaOlevatKoodistot = Koodistot.koodistot.filter(updateable.contains(_)).filter(!kp.getLatestVersion(_).isEmpty).toList
    val päivitettävätKoodistot = olemassaOlevatKoodistot.flatMap { koodistoUri =>
      val existing: Koodisto = kp.getLatestVersion(koodistoUri).flatMap(kp.getKoodisto).get
      val mock: Koodisto = MockKoodistoPalvelu().getKoodisto(KoodistoViite(koodistoUri, 1)).get.copy(version = existing.version)

      if (existing.withinCodes.map(_.sortBy(_.codesUri)) != mock.withinCodes.map(_.sortBy(_.codesUri))) {
        logger.info("Päivitetään koodisto " + existing.koodistoUri + " diff " + JsonMethods.compact(objectDiff(existing, mock)) + " original " + JsonSerializer.writeWithRoot(existing))
        Some(mock)
      } else {
        None
      }
    }
    päivitettävätKoodistot.foreach { koodisto =>
      kmp.updateKoodisto(koodisto)
    }
  }

  private def luoPuuttuvatKoodistot {
    // Create missing
    val luotavatKoodistot = Koodistot.koodistot.filter(createable.contains(_)).filter(kp.getLatestVersion(_).isEmpty).toList
    luotavatKoodistot.foreach { koodistoUri =>
      MockKoodistoPalvelu().getKoodisto(KoodistoViite(koodistoUri, 1)) match {
        case None =>
          throw new IllegalStateException("Mock not found: " + koodistoUri)
        case Some(koodisto) =>
          logger.info("Luodaan koodisto " + koodistoUri)
          kmp.createKoodisto(koodisto)
      }
    }
  }
}