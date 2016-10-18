package fi.oph.koski.koodisto

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.log.Logging

import scala.collection.parallel.immutable.ParSeq

object KoodistoCreator extends Logging {
  def createKoodistotFromMockData(config: Config): Unit = {
    val kp = KoodistoPalvelu.withoutCache(config)
    val kmp = KoodistoMuokkausPalvelu(config)

    val luotavatKoodistot = MockKoodistoPalvelu.koodistot.par.filter(kp.getLatestVersion(_).isEmpty).toList

    luotavatKoodistot.foreach { koodistoUri =>
      MockKoodistoPalvelu().getKoodisto(KoodistoViite(koodistoUri, 1)) match {
        case None =>
          throw new IllegalStateException("Mock not found: " + koodistoUri)
        case Some(koodisto) =>
          logger.info("Luodaan koodisto " + koodisto.koodistoUri)
          kmp.createKoodisto(koodisto)
          koodisto.koodistoViite
      }
    }

    MockKoodistoPalvelu.koodistot.par.foreach { koodistoUri =>
      val koodistoViite: KoodistoViite = kp.getLatestVersion(koodistoUri).getOrElse(throw new Exception("Koodistoa ei löydy: " + koodistoUri))
      val koodit = kp.getKoodistoKoodit(koodistoViite).toList.flatten
      val luotavatKoodit = MockKoodistoPalvelu().getKoodistoKoodit(koodistoViite).toList.flatten.filter { koodi: KoodistoKoodi => !koodit.find(_.koodiArvo == koodi.koodiArvo).isDefined }
      luotavatKoodit.zipWithIndex.foreach { case (koodi, index) =>
        logger.info("Luodaan koodi (" + (index + 1) + "/" + (luotavatKoodit.length) + ") " + koodi.koodiUri)
        kmp.createKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
      }
    }
  }

}


