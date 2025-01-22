package fi.oph.koski.massaluovutus.luokallejaaneet

import fi.oph.koski.organisaatio.MockOrganisaatiot

object MassaluovutusQueryLuokalleJaaneetExamples {
  val query: MassaluovutusQueryLuokalleJaaneet = MassaluovutusQueryLuokalleJaaneet(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki)
  )
}
