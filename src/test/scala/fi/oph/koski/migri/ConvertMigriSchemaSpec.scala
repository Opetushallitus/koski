package fi.oph.koski.migri

import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class ConvertMigriSchemaSpec extends AnyFreeSpec with Matchers {
  private val henkilö = {
    val o = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
    TäydellisetHenkilötiedot(
      oid = o.oid,
      hetu = o.hetu,
      syntymäaika = o.syntymäaika,
      etunimet = o.etunimet,
      kutsumanimi = o.kutsumanimi,
      sukunimi = o.sukunimi,
      äidinkieli = None,
      kansalaisuus = None,
    )
  }
  private val yki = ExamplesKielitutkinto.YleisetKielitutkinnot.lähdejärjestelmällinenOpiskeluoikeus
  private val vkt = ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Opiskeluoikeus
    .valmis(LocalDate.of(2020, 9, 10), "FI", List("kirjallinen"), "erinomainen")

  "convertYki" - {
    "palauttaa vain yleisen kielitutkinnon opiskeluoikeudet, ei valtionhallinnon kielitutkintoja eikä muita opiskeluoikeuksia" in {
      val result = ConvertMigriSchema.convertYki(henkilö, List(yki, vkt)).getOrElse(fail("odotettiin YKI-tietoja"))

      result.henkilö.oid shouldBe henkilö.oid
      result.opiskeluoikeudet shouldBe Nil

      val ykitiedot = result.ykitiedot.getOrElse(fail("ykitiedot puuttuu"))
      ykitiedot should have length 1
      ykitiedot.head.tila shouldBe yki.tila
      ykitiedot.head.tyyppi.koodiarvo shouldBe "kielitutkinto"
      ykitiedot.head.suoritukset.map(_.tyyppi.koodiarvo) shouldBe List("yleinenkielitutkinto")
      ykitiedot.head.suoritukset.head.koulutusmoduuli.kieli.koodiarvo shouldBe "FI"
    }

    "palauttaa None, jos oppijalla ei ole yleisen kielitutkinnon opiskeluoikeuksia" in {
      ConvertMigriSchema.convertYki(henkilö, List(vkt)) shouldBe None
      ConvertMigriSchema.convertYki(henkilö, Nil) shouldBe None
    }
  }
}
