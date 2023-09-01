package fi.oph.koski.raportit

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData.{helsinki, opiskeluoikeusValmistunut, opiskeluoikeusEronnut}
import fi.oph.koski.documentation.{AmmatillinenExampleData, TutkinnonOsaaPienempiKokonaisuusExample}
import fi.oph.koski.documentation.MuunAmmatillisenKoulutuksenExample.muunAmmatillisenKoulutuksenSuoritus
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema._

trait AmmatillinenRaporttiTestMethods extends OpiskeluoikeusTestMethodsAmmatillinen {

  def insertSisällytettyOpiskeluoikeusSuorituksilla(oppija: LaajatOppijaHenkilöTiedot, innerSuoritukset: List[AmmatillinenPäätasonSuoritus], outerSuoritukset: List[AmmatillinenPäätasonSuoritus]) = {
    val omnia = MockOrganisaatioRepository.findByOppilaitosnumero("10054").get
    val stadinAmmattiopisto = MockOrganisaatioRepository.findByOppilaitosnumero("10105").get

    val innerOpiskeluoikeus = makeOpiskeluoikeus(LocalDate.of(2016, 1, 1), omnia, omnia.oid).copy(suoritukset = innerSuoritukset)
    val outerOpiskeluoikeus = makeOpiskeluoikeus(LocalDate.of(2016, 1, 1), stadinAmmattiopisto, stadinAmmattiopisto.oid).copy(suoritukset = outerSuoritukset)

    putOpiskeluoikeus(outerOpiskeluoikeus, oppija) {
      verifyResponseStatusOk()
      val outerOpiskeluoikeusOid = lastOpiskeluoikeus(oppija.oid).oid.get
      putOpiskeluoikeus(sisällytäOpiskeluoikeus(innerOpiskeluoikeus, SisältäväOpiskeluoikeus(stadinAmmattiopisto, outerOpiskeluoikeusOid)), oppija) {
        verifyResponseStatusOk()
      }
    }
  }

  def insertMuuAmmatillisenSuorituksenOpiskeluoikeusPäivämäärillä(oppija: LaajatOppijaHenkilöTiedot, alkanut: LocalDate, päättynyt: LocalDate) = {
    val valmistunutOpiskeluoikeus = lisääTila(
      makeOpiskeluoikeus(alkanut).copy(suoritukset = List(muunAmmatillisenKoulutuksenSuoritus.copy(vahvistus = vahvistus(päättynyt), osasuoritukset = None))),
      päättynyt,
      opiskeluoikeusValmistunut
    )

    putOpiskeluoikeus(valmistunutOpiskeluoikeus, oppija) {
      verifyResponseStatusOk()
    }
  }

  def insertTOPKSOpiskeluoikeusPäivämäärillä(oppija: LaajatOppijaHenkilöTiedot, alkanut: LocalDate, päättynyt: LocalDate) = {
    val valmistunutOpiskeluoikeus = lisääTila(
      makeOpiskeluoikeus(alkanut).copy(suoritukset = List(TutkinnonOsaaPienempiKokonaisuusExample.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus.copy(osasuoritukset = None))),
      päättynyt,
      opiskeluoikeusEronnut
    )

    putOpiskeluoikeus(valmistunutOpiskeluoikeus, oppija) {
      verifyResponseStatusOk()
    }
  }

  private def vahvistus(päivä: LocalDate) = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(päivä, Some(helsinki), AmmatillinenExampleData.stadinAmmattiopisto, List(Organisaatiohenkilö("Reijo Reksi", LocalizedString.finnish("rehtori"), AmmatillinenExampleData.stadinAmmattiopisto))))
}
