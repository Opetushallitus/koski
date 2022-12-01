package fi.oph.koski.api

import com.typesafe.config.Config
import fi.oph.koski.documentation.ExamplesTaiteenPerusopetus
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationTaiteenPerusopetusSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[TaiteenPerusopetuksenOpiskeluoikeus] {
  override def tag = implicitly[reflect.runtime.universe.TypeTag[TaiteenPerusopetuksenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesTaiteenPerusopetus.Opiskeluoikeus.aloitettuYleinenOppimäärä

  override val defaultUser = MockUsers.paakayttaja

  val oppija = KoskiSpecificMockOppijat.taiteenPerusopetusAloitettu

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = oppija) {
      verifyResponseStatusOk()
    }
  }


  def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      KoskiApplicationForTests.ePerusteetValidator,
      KoskiApplicationForTests.ePerusteetFiller,
      KoskiApplicationForTests.validatingAndResolvingExtractor,
      KoskiApplicationForTests.suostumuksenPeruutusService,
      KoskiApplicationForTests.koodistoViitePalvelu,
      config
    )
  }

  private def putAndGetOpiskeluoikeus(oo: TaiteenPerusopetuksenOpiskeluoikeus): TaiteenPerusopetuksenOpiskeluoikeus = putOpiskeluoikeus(
    oo
  ) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[TaiteenPerusopetuksenOpiskeluoikeus]
}
