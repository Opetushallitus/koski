package fi.oph.koski.perustiedot

import fi.oph.koski.documentation.{AhvenanmaanPerusopetusExampleData, ExamplesPerusopetus}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.schema.Opiskeluoikeus
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskeluoikeudenPerustiedotSpec extends AnyFreeSpec with Matchers {

  "Opiskelijalistan Koulutus-sarakkeessa näytettävät suoritukset" - {
    "manner-Suomen perusopetuksesta näytetään vain oppimäärän suoritus" in {
      suoritusTyypit(
        ExamplesPerusopetus.ysiluokkalaisenOpiskeluoikeus,
        KoskiSpecificMockOppijat.ysiluokkalainen
      ) should be(List("perusopetuksenoppimaara"))
    }

    "Ahvenanmaan perusopetuksesta näytetään vain oppimäärän suoritus" in {
      suoritusTyypit(
        AhvenanmaanPerusopetusExampleData.opiskeluoikeus,
        KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas
      ) should be(List("ahvenanmaanperusopetuksenoppimaara"))
    }
  }

  "Opiskelijalistan Luokka/ryhmä-sarakkeessa näytetään viimeisimmän vuosiluokan luokka" - {
    "manner-Suomen perusopetus" in {
      perustiedot(
        ExamplesPerusopetus.ysiluokkalaisenOpiskeluoikeus,
        KoskiSpecificMockOppijat.ysiluokkalainen
      ).luokka should be(Some("9C"))
    }

    "Ahvenanmaan perusopetus" in {
      perustiedot(
        AhvenanmaanPerusopetusExampleData.opiskeluoikeus,
        KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas
      ).luokka should be(Some("9A"))
    }
  }

  private def suoritusTyypit(oo: Opiskeluoikeus, oppija: OppijaHenkilö): List[String] =
    perustiedot(oo, oppija).suoritukset.map(_.tyyppi.koodiarvo)

  private def perustiedot(oo: Opiskeluoikeus, oppija: OppijaHenkilö): OpiskeluoikeudenPerustiedot =
    OpiskeluoikeudenPerustiedot.makePerustiedot(1, oo, OppijaHenkilöWithMasterInfo(oppija, None))
}
