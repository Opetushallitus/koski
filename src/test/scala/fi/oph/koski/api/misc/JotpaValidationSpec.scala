package fi.oph.koski.api.misc

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.AmmattitutkintoExample
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeus, AmmatillinenOpiskeluoikeusjakso, AmmatillisenOpiskeluoikeudenLisätiedot, Koodistokoodiviite, LähdejärjestelmäId}
import fi.oph.koski.validation.JotpaValidation
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import java.time.LocalDate
import com.typesafe.config.ConfigValueFactory.fromAnyRef

class JotpaValidationSpec extends AnyFreeSpec with Matchers {
  "JOTPA-rahoituksen yhtenäisyys" - {
    val config = KoskiApplicationForTests.config.withValue("validaatiot.jatkuvaanOppimiseenSuunnatutKoulutusmuodotAstuvatVoimaan", fromAnyRef("2000-01-01"))

    "Hyväksyy erilaisia ei-jotpa-rahoitusmuotoja sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen, muutaKauttaRahoitettu)).copy(lisätiedot = None)
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config) should equal(HttpStatus.ok)
    }

    "Hyväksyy pelkän jatkuvan oppimisen rahoituksen" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus, jatkuvanOppimisenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config) should equal(HttpStatus.ok)
    }

    "Hyväksyy pelkän jatkuvan oppimisen uudistuksen rahoituksen" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenUudistuksenRahoitus, jatkuvanOppimisenUudistuksenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config) should equal(HttpStatus.ok)
    }

    "Ei hyväksy jotpa- ja ei-jotpa-rahoitusta sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen, jatkuvanOppimisenRahoitus))
      val error = KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys()
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config) should equal(error)
    }

    "Ei hyväksy jotpa-rahoitusmuotoja sekaisin" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenUudistuksenRahoitus, jatkuvanOppimisenRahoitus))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config) should equal(KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys())
    }
  }

  "JOTPA-rahoituksen alkamispäivä" in {
    val config = KoskiApplicationForTests.config.withValue("validaatiot.jatkuvaanOppimiseenSuunnatutKoulutusmuodotAstuvatVoimaan", fromAnyRef("2300-01-01"))
    val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus))
    JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config) should equal(
      KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Jatkuvaan oppimiseen suunnatun koulutuksen opiskeluoikeuden jakso ei voi alkaa ennen 1.1.2300")
    )
  }

  "JOTPA asianumero" - {
    val configVoimassa = KoskiApplicationForTests.config.withValue("validaatiot.jatkuvaanOppimiseenSuunnatutKoulutusmuodotAstuvatVoimaan", fromAnyRef("2000-01-01"))
    val configEiVoimassa = configVoimassa.withValue("validaatiot.jotpaAsianumeroVaatimusAlkaa", fromAnyRef("2999-01-01"))

    "Lähdejärjestelmästä tuleva JOTPA-rahoitteinen opiskeluoikeus vaatii JOTPA asianumeron kun validaatio on voimassa" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus)).copy(
        lisätiedot = None,
        lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("12345"), Koodistokoodiviite("primus", "lahdejarjestelma")))
      )
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, configVoimassa) should equal(
        KoskiErrorCategory.badRequest.validation.tila.vaatiiJotpaAsianumeron()
      )
    }

    "Lähdejärjestelmästä tuleva JOTPA-rahoitteinen opiskeluoikeus ei vaadi JOTPA asianumeroa ennen kuin validaatio on voimassa" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus)).copy(
        lisätiedot = None,
        lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("12345"), Koodistokoodiviite("primus", "lahdejarjestelma")))
      )
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, configEiVoimassa) should equal(
        HttpStatus.ok
      )
    }

    "Käyttöliittymästä tuleva JOTPA-rahoitteinen opiskeluoikeus vaatii JOTPA asianumeron kun validaatio on voimassa" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus)).copy(
        lisätiedot = None,
        lähdejärjestelmänId = None
      )
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, configVoimassa) should equal(
        KoskiErrorCategory.badRequest.validation.tila.vaatiiJotpaAsianumeron())
    }

    "Käyttöliittymästä tuleva JOTPA-rahoitteinen opiskeluoikeus vaatii JOTPA asianumeron ennen kuin validaatio on voimassa" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(jatkuvanOppimisenRahoitus)).copy(
        lisätiedot = None,
        lähdejärjestelmänId = None
      )
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, configEiVoimassa) should equal(
        KoskiErrorCategory.badRequest.validation.tila.vaatiiJotpaAsianumeron())
    }

    "Ei-JOTPA-rahoitteinen opiskeluoikeus ei saa sisältää JOTPA asianumeroa" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, configVoimassa) should equal(
        KoskiErrorCategory.badRequest.validation.tila.jotpaAsianumeroAnnettuVaikkeiJotpaRahoitteinen()
      )
    }

    "Ei-JOTPA-rahoitteinen opiskeluoikeus ei saa sisältää JOTPA asianumeroa, myös kun validaatio ei ole voimassa" in {
      val opiskeluoikeus = getOpiskeluoikeus(List(valtionosuusRahoitteinen))
      JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, configEiVoimassa) should equal(
        KoskiErrorCategory.badRequest.validation.tila.jotpaAsianumeroAnnettuVaikkeiJotpaRahoitteinen()
      )
    }
  }

  def getOpiskeluoikeus(rahoitukset: List[Koodistokoodiviite]): AmmatillinenOpiskeluoikeus = {
    AmmattitutkintoExample.opiskeluoikeus.copy(
      tila = AmmatillinenOpiskeluoikeudenTila(rahoitukset.zipWithIndex.map {
        case (rahoitus, index) => AmmatillinenOpiskeluoikeusjakso(
          LocalDate.of(2012, 9, 1).plusYears(index),
          if (rahoitus == rahoitukset.last) opiskeluoikeusValmistunut else opiskeluoikeusLäsnä,
          Some(rahoitus),
        )
      }),
      lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(jotpaAsianumero = Some(Koodistokoodiviite("01/5848/2023", "jotpaasianumero")), hojks = None))
    )
  }
}
