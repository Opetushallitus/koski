import React from "react"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { t } from "../../i18n/i18n"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import { formatNullableDate } from "../../utils/date"

export type OppijanOppivelvollisuustiedotProps = {
  oppija: OppijaHakutilanteillaLaajatTiedot
}

export const OppijanOppivelvollisuustiedot = (
  props: OppijanOppivelvollisuustiedotProps
) => (
  <InfoTable>
    <InfoTableRow
      label={t("oppija__opiskelutilanne")}
      value={t(
        props.oppija.oppija.opiskelee
          ? "oppija__opiskelutilanne__opiskelemassa"
          : "oppija__opiskelutilanne__ei_opiskelupaikkaa"
      )}
    />
    <InfoTableRow
      label={t("oppija__oppivelvollisuus_voimassa")}
      value={t("oppija__oppivelvollisuus_voimassa_value", {
        date: formatNullableDate(
          props.oppija.oppija.oppivelvollisuusVoimassaAsti
        ),
      })}
    />
  </InfoTable>
)
