package fi.oph.koski.util

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, IOException, InputStream}
import java.nio.charset.StandardCharsets.UTF_8

class BuildVersionSpec extends AnyFreeSpec with Matchers {
  "BuildVersion" - {
    "Lukee eri metadatakentät ja sulkee resurssin" in {
      val stream = new TrackedStream("version=release\nvcsRevision=commit\nbuildDate=2026-09-18")
      var acquisitions = 0
      BuildVersion.read {
        acquisitions += 1
        stream
      } should be(Some(BuildVersion(Some("release"), Some("commit"), Some("2026-09-18"))))
      acquisitions should be(1)
      stream.closed should be(true)
    }

    "Puuttuva resurssi palauttaa None" in {
      BuildVersion.read(null) should be(None)
    }

    "Puuttuvat kentät palauttavat None" in {
      BuildVersion.read(new TrackedStream("")) should be(Some(BuildVersion(None, None, None)))
      BuildVersion.read(new TrackedStream("version=local")) should be(Some(BuildVersion(Some("local"), None, None)))
      BuildVersion.read(new TrackedStream("vcsRevision=commit")) should be(Some(BuildVersion(None, Some("commit"), None)))
    }

    "Säilyttää tyhjät arvot ja properties-jäsennyksen jälkeiset välilyönnit" in {
      BuildVersion.read(new TrackedStream("version=\nvcsRevision=\nbuildDate=")) should be(
        Some(BuildVersion(Some(""), Some(""), Some("")))
      )
      BuildVersion.read(new TrackedStream("version=release  \nvcsRevision=\\ commit \\ \nbuildDate=date  ")) should be(
        Some(BuildVersion(Some("release  "), Some(" commit  "), Some("date  ")))
      )
    }

    "Välittää resurssin hankintavirheen" in {
      val error = new IOException("unavailable")
      intercept[IOException](BuildVersion.read(throw error)) should be theSameInstanceAs error
    }

    "Välittää lukuvirheen ja sulkee resurssin" in {
      val error = new IOException("unreadable")
      var closed = false
      val stream = new InputStream {
        override def read(): Int = throw error
        override def close(): Unit = { closed = true }
      }
      intercept[IOException](BuildVersion.read(stream)) should be theSameInstanceAs error
      closed should be(true)
    }

    "Välittää jäsennysvirheen ja sulkee resurssin" in {
      val stream = new TrackedStream("vcsRevision=\\uZZZZ")
      intercept[IllegalArgumentException](BuildVersion.read(stream))
      stream.closed should be(true)
    }

    "Välittää sulkemisvirheen" in {
      val error = new IOException("close failed")
      val stream = new ByteArrayInputStream("version=release".getBytes(UTF_8)) {
        override def close(): Unit = throw error
      }
      intercept[IOException](BuildVersion.read(stream)) should be theSameInstanceAs error
    }
  }

  private class TrackedStream(content: String) extends ByteArrayInputStream(content.getBytes(UTF_8)) {
    var closed = false
    override def close(): Unit = { closed = true }
  }
}
