package fi.oph.koski.vtj

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.huoltaja.VtjParser
import fi.oph.koski.util.Files
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.Elem

class VtjParserSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  val mockResponse: Elem = Files.asString("src/main/resources/mockdata/vtj/henkilo.xml")
    .map(scala.xml.XML.loadString)
    .getOrElse(throw new Exception("VTJ mock data not found"))

  "Huollettavat parsitaan oikein" in {
    val huollettavat = VtjParser.parseHuollettavatFromVtjResponse(mockResponse)
    huollettavat should have size (4)

    huollettavat(0).hetu should equal("300996-870E")
    huollettavat(0).etunimet should equal("Essi")
    huollettavat(0).sukunimi should equal("Eskari")

    huollettavat(1).hetu should equal("080698-703Y")
    huollettavat(1).etunimet should equal("Ynjevi")
    huollettavat(1).sukunimi should equal("Ylioppilaslukiolainen")

    huollettavat(2).hetu should equal("060488-681S")
    huollettavat(2).etunimet should equal("Olli")
    huollettavat(2).sukunimi should equal("Oiditon")

    huollettavat(3).hetu should equal("151067-2193")
    huollettavat(3).etunimet should equal("Tero")
    huollettavat(3).sukunimi should equal("Turvakielto")
  }

  "Paluukoodi parsitaan oikein" in {
    val paluuKoodi = VtjParser.parsePaluukoodiFromVtjResponse(mockResponse)
    paluuKoodi.koodi should equal("0000")
    paluuKoodi.arvo should equal("Haku onnistui")
  }
}
