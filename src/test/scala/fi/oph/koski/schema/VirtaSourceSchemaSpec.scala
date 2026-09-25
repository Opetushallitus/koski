package fi.oph.koski.schema

import fi.oph.koski.TestEnvironment
import fi.oph.koski.schema.annotation.{VirtaDerived, VirtaNote, VirtaSource}
import fi.oph.scalaschema.annotation.Description
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JsonAST.{JNothing, JObject, JString}
import org.json4s.JValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class VirtaSourceSchemaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(
    KoskiSchema.createSchema(classOf[VirtaSourceTestClass]).asInstanceOf[ClassSchema]
  )
  private def property(name: String) = schemaJson \ "properties" \ name

  "@VirtaSource" - {
    "emittoi polun ja säännön virta-objektina" in {
      (property("polullinen") \ "virta") should equal(JObject(
        "path" -> JString("Opiskeluoikeus/@avain"),
        "rule" -> JString("vain kun eri kuin Myontaja")
      ))
    }
    "jättää tyhjän säännön pois" in {
      (property("pelkkäPolku") \ "virta") should equal(JObject("path" -> JString("Opiskeluoikeus/Tyyppi")))
      (property("pelkkäPolku") \ "description") should equal(JString("(Virta: Opiskeluoikeus/Tyyppi)"))
    }
    "lisää kuvaukseen Virta-lauseen" in {
      (property("polullinen") \ "description") should equal(JString("(Virta: Opiskeluoikeus/@avain — vain kun eri kuin Myontaja)"))
    }
    "liittyy @Description-kuvauksen perään" in {
      (property("kuvauksellinen") \ "description") should equal(JString("Kuvaus, joka päättyy pisteeseen. (Virta: Opiskeluoikeus/Jakso — sääntö)"))
    }
  }

  "@VirtaDerived" - {
    "emittoi säännön derived-kenttänä" in {
      (property("johdettu") \ "virta") should equal(JObject("derived" -> JString("Aina korkeakoulutus")))
    }
    "lisää kuvaukseen johdettu-lauseen" in {
      (property("johdettu") \ "description") should equal(JString("(Virta: johdettu Koskessa — Aina korkeakoulutus)"))
    }
  }

  "@VirtaNote" - {
    "lisätään virta-objektiin note-kenttänä polun rinnalle" in {
      (property("selitteellinen") \ "virta") should equal(JObject(
        "path" -> JString("Opiskeluoikeus/Tila"),
        "note" -> JString("Pitkä selite.")
      ))
    }
  }

  "Merkitsemätön kenttä ei saa virta-objektia" in {
    (property("merkitsemätön") \ "virta") should be(JNothing)
  }
}

case class VirtaSourceTestClass(
  @VirtaSource("Opiskeluoikeus/@avain", "vain kun eri kuin Myontaja")
  polullinen: String,
  @VirtaSource("Opiskeluoikeus/Tyyppi")
  pelkkäPolku: String,
  @Description("Kuvaus, joka päättyy pisteeseen.")
  @VirtaSource("Opiskeluoikeus/Jakso", "sääntö")
  kuvauksellinen: String,
  @VirtaDerived("Aina korkeakoulutus")
  johdettu: String,
  @VirtaSource("Opiskeluoikeus/Tila")
  @VirtaNote("Pitkä selite.")
  selitteellinen: String,
  merkitsemätön: String
)
