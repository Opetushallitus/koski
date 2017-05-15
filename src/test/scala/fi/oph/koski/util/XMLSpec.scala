package fi.oph.koski.util

import fi.oph.koski.util.XML.CommentedPCData
import org.scalatest.{FreeSpec, Matchers}

class XMLSpec extends FreeSpec with Matchers {
  "CommentedPCData" in {
    val node =
      <script>
        {CommentedPCData(
        """
        console.log("<a href='javascript:'>)"
        """)}
      </script>

    node.toString() should equal(
      """<script>
        |        // <![CDATA[
        |        console.log("<a href='javascript:'>)"
        |        // ]]>
        |      </script>""".stripMargin)
  }
}
