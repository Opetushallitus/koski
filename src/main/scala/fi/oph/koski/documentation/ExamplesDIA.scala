package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData.{englanti, helsinki}
import fi.oph.koski.documentation.DIAExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

object ExamplesDIA {
  def osasuorituksetValmistavaVaihe: List[DIAOppiaineenValmistavanVaiheenSuoritus] = List(
    diaValmistavaVaiheAineSuoritus(diaOppiaineÄidinkieli("DE", laajuus(3)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "3"),
      (diaValmistavaLukukausi("2", laajuus(2)), "5")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus(3)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "2"),
      (diaValmistavaLukukausi("2", laajuus(2)), "3")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineKieliaine("A", "EN", laajuus(3)), Some(List(
      (diaValmistavaLukukausi(), "3")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineKieliaine("B1", "SV", laajuus(3)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "2"),
      (diaValmistavaLukukausi("2", laajuus(2)), "2")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineLisäaineKieli("B2", "LA", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "4"),
      (diaValmistavaLukukausi("2", laajuus(1)), "4")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineKieliaine("B3", "RU", laajuus(3)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "4"),
      (diaValmistavaLukukausi("2", laajuus(2)), "3")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("KU", "1",  laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "4"),
      (diaValmistavaLukukausi("2", laajuus(1)), "5")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("MA", "2", laajuus(4)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(2)), "3"),
      (diaValmistavaLukukausi("2", laajuus(2)), "1")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("FY", "2", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "3"),
      (diaValmistavaLukukausi("2", laajuus(1)), "2")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("KE", "2", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "2"),
      (diaValmistavaLukukausi("2", laajuus(1)), "4")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("TI", "2", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "1"),
      (diaValmistavaLukukausi("2", laajuus(1)), "2")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("HI", "3", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "3"),
      (diaValmistavaLukukausi("2", laajuus(1)), "4")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("TA", "3", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "5"),
      (diaValmistavaLukukausi("2", laajuus(1)), "3")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("MAA", "3", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "3"),
      (diaValmistavaLukukausi("2", laajuus(1)), "3")
    ))),
    diaValmistavaVaiheAineSuoritus(diaOppiaineMuu("FI", "3", laajuus(2)), Some(List(
      (diaValmistavaLukukausi("1", laajuus(1)), "1"),
      (diaValmistavaLukukausi("2", laajuus(1)), "1")
    )))
  )

  def osasuorituksetTutkintovaihe: List[DIAOppiaineenTutkintovaiheenSuoritus] = List(
    diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("DE", laajuus(10)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(2)), "3"),
      (diaTutkintoLukukausi("4", laajuus(2)), "5"),
      (diaTutkintoLukukausi("5", laajuus(2)), "4"),
      (diaTutkintoLukukausi("6", laajuus(4)), "3"),
      (diaPäättökoe("kirjallinenkoe"), "5")
    )),
      koetuloksenNelinkertainenPistemäärä = Some(20),
      vastaavuustodistuksenTiedot = Some(DIAVastaavuustodistuksenTiedot(
        4.0f,
        LaajuusOpintopisteissä(2.5f)
      ))
    ),
    diaTutkintoAineSuoritus(diaOppiaineÄidinkieli("FI", laajuus(8)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(2)), "2"),
      (diaTutkintoLukukausi("4", laajuus(2)), "3"),
      (diaTutkintoLukukausi("5", laajuus(2)), "3"),
      (diaTutkintoLukukausi("6", laajuus(2)), "3")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineKieliaine("A", "EN", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3"), "2"),
      (diaTutkintoLukukausi("4"), "3"),
      (diaTutkintoLukukausi("5"), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineKieliaine("B1", "SV", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "2"),
      (diaTutkintoLukukausi("4", laajuus(1)), "2"),
      (diaTutkintoLukukausi("5", laajuus(1)), "4"),
      (diaTutkintoLukukausi("6", laajuus(3)), "3")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineLisäaineKieli("B2", "LA", laajuus(4)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "3"),
      (diaTutkintoLukukausi("4", laajuus(1)), "3"),
      (diaTutkintoLukukausi("5", laajuus(1)), "2"),
      (diaTutkintoLukukausi("6", laajuus(1)), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineKieliaine("B3", "RU", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "4"),
      (diaTutkintoLukukausi("4", laajuus(1)), "3"),
      (diaTutkintoLukukausi("5", laajuus(1)), "4"),
      (diaTutkintoLukukausi("6", laajuus(3)), "3")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("KU", "1", laajuus(4)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "4"),
      (diaTutkintoLukukausi("4", laajuus(1)), "3"),
      (diaTutkintoLukukausi("5", laajuus(1)), "2"),
      (diaTutkintoLukukausi("6", laajuus(1)), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("MA", "2", laajuus(8)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(2)), "3"),
      (diaTutkintoLukukausi("4", laajuus(2)), "1"),
      (diaTutkintoLukukausi("5", laajuus(2)), "1"),
      (diaTutkintoLukukausi("6", laajuus(2)), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("FY", "2", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "2"),
      (diaTutkintoLukukausi("4", laajuus(1)), "2"),
      (diaTutkintoLukukausi("5", laajuus(1)), "1"),
      (diaTutkintoLukukausi("6", laajuus(3)), "1")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("KE", "2", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "3"),
      (diaTutkintoLukukausi("4", laajuus(1)), "2"),
      (diaTutkintoLukukausi("5", laajuus(1)), "1"),
      (diaTutkintoLukukausi("6", laajuus(3)), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("TI", "2", laajuus(4)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "2"),
      (diaTutkintoLukukausi("4", laajuus(1)), "1"),
      (diaTutkintoLukukausi("5", laajuus(1)), "1"),
      (diaTutkintoLukukausi("6", laajuus(1)), "1")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("HI", "3", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "3"),
      (diaTutkintoLukukausi("4", laajuus(1)), "4"),
      (diaTutkintoLukukausi("5", laajuus(1)), "3"),
      (diaTutkintoLukukausi("6", laajuus(3)), "1")
    )), suorituskieli = Some("FI")),
    diaTutkintoAineSuoritus(diaOppiaineMuu("TA", "3", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "4"),
      (diaTutkintoLukukausi("4", laajuus(1)), "3"),
      (diaTutkintoLukukausi("5", laajuus(1)), "2"),
      (diaTutkintoLukukausi("6", laajuus(3)), "3")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("MAA", "3", laajuus(6)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "3"),
      (diaTutkintoLukukausi("4", laajuus(1)), "5"),
      (diaTutkintoLukukausi("5", laajuus(1)), "3"),
      (diaTutkintoLukukausi("6", laajuus(3)), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineMuu("FI", "3", laajuus(4)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(1)), "2"),
      (diaTutkintoLukukausi("4", laajuus(1)), "2"),
      (diaTutkintoLukukausi("5", laajuus(1)), "2"),
      (diaTutkintoLukukausi("6", laajuus(1)), "2")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineLisäaine("CCEA", laajuus(1)), Some(List(
      (diaTutkintoLukukausi("5", laajuus(0.5f)), "3"),
      (diaTutkintoLukukausi("6", laajuus(0.5f)), "1")
    ))),
    diaTutkintoAineSuoritus(diaOppiaineLisäaine("MASY", laajuus(2)), Some(List(
      (diaTutkintoLukukausi("3", laajuus(0.5f)), "2"),
      (diaTutkintoLukukausi("4", laajuus(0.5f)), "1"),
      (diaTutkintoLukukausi("5", laajuus(0.5f)), "2"),
      (diaTutkintoLukukausi("6", laajuus(0.5f)), "4")
    )))
  )

  def diaValmistavanVaiheenSuoritus = DIAValmistavanVaiheenSuoritus(
    toimipiste = saksalainenKoulu,
    suorituskieli = englanti,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
    osasuoritukset = Some(osasuorituksetValmistavaVaihe)
  )

  def diaTutkintovaiheenSuoritus(kokonaispistemäärä: Option[Int] = None,
                                 lukukausisuoritustenKokonaispistemäärä: Option[Int] = None,
                                 tutkintoaineidenKokonaispistemäärä: Option[Int] = None,
                                 kokonaispistemäärästäJohdettuKeskiarvo: Option[Float] = None) = DIATutkinnonSuoritus(
    toimipiste = saksalainenKoulu,
    suorituskieli = englanti,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = saksalainenKoulu, kunta = helsinki),
    kokonaispistemäärä = kokonaispistemäärä,
    lukukausisuoritustenKokonaispistemäärä = lukukausisuoritustenKokonaispistemäärä,
    tutkintoaineidenKokonaispistemäärä = tutkintoaineidenKokonaispistemäärä,
    kokonaispistemäärästäJohdettuKeskiarvo = kokonaispistemäärästäJohdettuKeskiarvo,
    osasuoritukset = Some(osasuorituksetTutkintovaihe)
  )

  val opiskeluoikeus = DIAOpiskeluoikeus(
    oppilaitos = Some(saksalainenKoulu),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2016, 6, 4), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(diaValmistavanVaiheenSuoritus, diaTutkintovaiheenSuoritus(Some(870), Some(590), Some(280), Some(1.2F)))
  )

  val examples = List(
    Example("dia", "dia", Oppija(asUusiOppija(MockOppijat.dia), List(opiskeluoikeus)))
  )
}
