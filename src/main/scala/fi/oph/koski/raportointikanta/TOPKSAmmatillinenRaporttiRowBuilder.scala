package fi.oph.koski.raportointikanta

import fi.oph.koski.schema.{Suoritus, YhteisenTutkinnonOsanOsaAlueenSuoritus}

object TOPKSAmmatillinenRaporttiRowBuilder {
  def build(opiskeluoikeudenOid: String, päätasonSuoritusId: Long, os: Suoritus): TOPKSAmmatillinenRaportointiRow = {
    TOPKSAmmatillinenRaportointiRow(
      opiskeluoikeudenOid,
      päätasonSuoritusId,
      toteuttavanLuokanNimi = os.getClass.getSimpleName.toLowerCase,
      rahoituksenPiirissä = os match {
        case s: YhteisenTutkinnonOsanOsaAlueenSuoritus => s.tunnustettu.exists(_.rahoituksenPiirissä)
        case _ => false
      },
      arviointiHyväksytty = os.viimeisinArviointi.exists(_.hyväksytty),
      tunnustettu = os match {
        case s: YhteisenTutkinnonOsanOsaAlueenSuoritus => s.tunnustettu.isDefined
        case _ => false
      },
      koulutusmoduuliLaajuusArvo = os.koulutusmoduuli.getLaajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = os.koulutusmoduuli.getLaajuus.map(_.yksikkö.koodiarvo)
    )
  }
}
