package fi.oph.koski.cache

import fi.oph.koski.util.Invocation

class Counter {
  private var counts: Map[String, Int] = Map.empty

  private val incFunc = this.inc _

  def inc(key: String): String = synchronized {
    val current = counts.getOrElse(key, 0)
    val v = current + 1
    counts += (key -> v)
    Thread.sleep(10)
    //println("Generated " + key + "=" + v)
    if (key == "666") {
      throw new Exception("Test exception")
    }
    if ((key == "777") && (v >= 2)) {
      throw new Exception("Test exception")
    }
    if ((key == "888") && Set(2, 3).contains(v)) {
      throw new Exception("Test exception")
    }
    key + v
  }

  def get(key: String) = synchronized {
    counts.getOrElse(key, 0)
  }

  def incInvocation(key: String) = Invocation(incFunc, key)
}
