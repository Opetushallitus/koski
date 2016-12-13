package fi.oph.koski.koodisto

import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.log.Logging

import scala.collection.parallel.immutable.ParSeq

object KoodistoCreator extends Logging {
  def createKoodistotFromMockData(koodistot: List[String], config: Config, updateExisting: Boolean = false): Unit = {
    val kp = KoodistoPalvelu.withoutCache(config)
    val kmp = KoodistoMuokkausPalvelu(config)

    val luotavatKoodistot = koodistot.par.filter(kp.getLatestVersion(_).isEmpty).toList

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

    koodistot.par.foreach { koodistoUri =>
      def sortMetadata(k: KoodistoKoodi) = k.copy(metadata = k.metadata.sortBy(_.kieli))
      val koodistoViite: KoodistoViite = kp.getLatestVersion(koodistoUri).getOrElse(throw new Exception("Koodistoa ei löydy: " + koodistoUri))
      val olemassaOlevatKoodit: List[KoodistoKoodi] = kp.getKoodistoKoodit(koodistoViite).toList.flatten.map(sortMetadata)
      val mockKoodit: List[KoodistoKoodi] = MockKoodistoPalvelu().getKoodistoKoodit(koodistoViite).toList.flatten.map(sortMetadata)
      val luotavatKoodit: List[KoodistoKoodi] = mockKoodit.filter { koodi: KoodistoKoodi => !olemassaOlevatKoodit.find(_.koodiArvo == koodi.koodiArvo).isDefined }
      luotavatKoodit.zipWithIndex.foreach { case (koodi, index) =>
        logger.info("Luodaan koodi (" + (index + 1) + "/" + (luotavatKoodit.length) + ") " + koodi.koodiUri)
        kmp.createKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
      }
      if (updateExisting) {
        val päivitettävätKoodit = olemassaOlevatKoodit.flatMap { koodi =>
          mockKoodit.find(_.koodiArvo == koodi.koodiArvo).flatMap { uusiKoodi =>
            val uusiKoodiSamallaKoodiUrilla = uusiKoodi.copy(koodiUri = koodi.koodiUri)
            if (uusiKoodiSamallaKoodiUrilla != koodi) {
              Some(koodi, uusiKoodi)
            } else {
              None
            }
          }
        }

        päivitettävätKoodit.zipWithIndex.foreach { case ((vanhaKoodi, koodi), index) =>
          logger.info("Päivitetään koodi (" + (index + 1) + "/" + (päivitettävätKoodit.length) + ") " + koodi.koodiUri)
          kmp.updateKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
        }
      }
    }
  }
}