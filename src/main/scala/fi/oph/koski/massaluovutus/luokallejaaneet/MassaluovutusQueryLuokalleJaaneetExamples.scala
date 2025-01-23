package fi.oph.koski.massaluovutus.luokallejaaneet

import fi.oph.koski.organisaatio.MockOrganisaatiot

object MassaluovutusQueryLuokalleJaaneetExamples {
  val jsonQuery: MassaluovutusQueryLuokalleJaaneet = MassaluovutusQueryLuokalleJaaneetJson(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki)
  )

  val csvQuery: MassaluovutusQueryLuokalleJaaneet = MassaluovutusQueryLuokalleJaaneetCsv(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki)
  )
}
