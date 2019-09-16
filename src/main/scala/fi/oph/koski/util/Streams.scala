package fi.oph.koski.util

import java.io.{InputStream, OutputStream}

object Streams {
  def pipeTo(from: InputStream, to: OutputStream): Unit = {
    Iterator.continually (from.read).takeWhile(_ != -1).foreach(to.write)
    from.close()
  }
}
