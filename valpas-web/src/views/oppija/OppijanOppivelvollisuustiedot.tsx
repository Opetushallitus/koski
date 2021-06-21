import * as A from "fp-ts/Array"
import React, { useState } from "react"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { VisibleForKäyttöoikeusrooli } from "../../components/containers/VisibleForKäyttöoikeusrooli"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { T, t } from "../../i18n/i18n"
import { kuntavalvontaAllowed } from "../../state/accessRights"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import { formatDate, formatNullableDate } from "../../utils/date"
import { OppivelvollisuudenKeskeytysModal } from "./OppivelvollisuudenKeskeytysModal"

export type OppijanOppivelvollisuustiedotProps = {
  oppija: OppijaHakutilanteillaLaajatTiedot
}

export const OppijanOppivelvollisuustiedot = (
  props: OppijanOppivelvollisuustiedotProps
) => {
  const [keskeytysModalVisible, setKeskeytysModalVisible] = useState(false)

  return (
    <>
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
          value={oppivelvollisuusValue(props.oppija)}
        />
        <InfoTableRow
          label={t("oppija__maksuttomuus_voimassa")}
          value={t("oppija__maksuttomuus_voimassa_value", {
            date: formatNullableDate(
              props.oppija.oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
            ),
          })}
        />
      </InfoTable>

      <VisibleForKäyttöoikeusrooli rooli={kuntavalvontaAllowed}>
        <RaisedButton
          id="ovkeskeytys-btn"
          hierarchy="secondary"
          onClick={() => setKeskeytysModalVisible(true)}
        >
          <T id="oppija__keskeytä_oppivelvollisuus" />
        </RaisedButton>

        {keskeytysModalVisible && (
          <OppivelvollisuudenKeskeytysModal
            oppija={props.oppija.oppija}
            onClose={() => setKeskeytysModalVisible(false)}
            onSubmit={() => window.location.reload()}
          />
        )}
      </VisibleForKäyttöoikeusrooli>
    </>
  )
}

const oppivelvollisuusValue = (
  oppija: OppijaHakutilanteillaLaajatTiedot
): React.ReactNode => {
  const keskeytykset = oppija.oppivelvollisuudenKeskeytykset
    .filter((ovk) => ovk.voimassa)
    .map((ovk) =>
      ovk.loppu !== undefined
        ? t("oppija__oppivelvollisuus_keskeytetty_value", {
            alkuPvm: formatDate(ovk.alku),
            loppuPvm: formatDate(ovk.loppu),
          })
        : t("oppija__oppivelvollisuus_keskeytetty_toistaiseksi_value", {
            alkuPvm: formatDate(ovk.alku),
          })
    )

  const strs = A.isEmpty(keskeytykset)
    ? [
        t("oppija__oppivelvollisuus_voimassa_value", {
          date: formatNullableDate(oppija.oppija.oppivelvollisuusVoimassaAsti),
        }),
      ]
    : keskeytykset

  return strs.length > 1 ? (
    <ul>
      {strs.map((str, index) => (
        <li key={index}>{str}</li>
      ))}
    </ul>
  ) : (
    <>{strs.join("")}</>
  )
}
