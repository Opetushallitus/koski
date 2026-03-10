package fi.oph.koski.perftest

import fi.oph.koski.log.Logging

import scala.io.Source
import scala.util.Random

/**
  * Thread-safe iterator over yleinen kielitutkinto opiskeluoikeus OIDs from a file.
  * Similar to RandomOppijaOid but reads from a file instead of fetching from API.
  *
  * Usage:
  *   val oids = new RandomYkiOpiskeluoikeusOid("src/test/resources/yki_perftest_opiskeluoikeus_oids.txt")
  *   val nextOid = oids.next()
  */
class RandomYkiOpiskeluoikeusOid(filename: String = "src/test/resources/yki_perftest_opiskeluoikeus_oids.txt") extends Logging {

  lazy val oids: List[String] = {
    logger.info(s"Loading YKI opiskeluoikeus OIDs from $filename")
    val source = Source.fromFile(filename)
    try {
      val lines = source.getLines().toList.filter(_.nonEmpty)
      logger.info(s"Loaded ${lines.length} OIDs from $filename")
      lines
    } finally {
      source.close()
    }
  }

  lazy val oidIterator: SynchronizedIterator[String] = {
    logger.info(s"Creating iterator for ${oids.length} OIDs")
    SynchronizedIterator(Iterator.continually(Random.shuffle(oids).iterator).flatten)
  }

  def next(): String = oidIterator.next()
}
