package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{KäyttöoikeusRepository, MockUsers}
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema.annotation.SensitiveData
import org.scalatest.{FreeSpec, Matchers}

class EditorModelBuilderSpec extends FreeSpec with Matchers {
  val application = KoskiApplication.apply
  implicit val koodisto: KoodistoViitePalvelu = application.koodistoViitePalvelu
  implicit val localizations: LocalizationRepository = application.localizationRepository
  implicit val käyttöoikeusRepository: KäyttöoikeusRepository = application.käyttöoikeusRepository


  "Sensitive data" - {
    "with LUOTTAMUKSELLINEN role" - {
      "data is shown" in {
        implicit val user = MockUsers.kalle.toKoskiUser(käyttöoikeusRepository)
        val editorModel = EditorModelBuilder.buildModel(EditorSchema.deserializationContext, TestClass("sensitive", "public"), true).asInstanceOf[ObjectModel]
        editorModel.properties.find(p => p.key == "sensitive") shouldBe defined
      }
    }
    "without LUOTTAMUKSELLINEN role" - {
      "data is hidden" in {
        implicit val user = MockUsers.tallentajaEiLuottamuksellinen.toKoskiUser(käyttöoikeusRepository)
        val editorModel = EditorModelBuilder.buildModel(EditorSchema.deserializationContext, TestClass("sensitive", "public"), true).asInstanceOf[ObjectModel]
        editorModel.properties.find(p => p.key == "sensitive") shouldBe empty
      }
    }
  }
}

case class TestClass(
  @SensitiveData
  sensitive: String,
  public: String
)
