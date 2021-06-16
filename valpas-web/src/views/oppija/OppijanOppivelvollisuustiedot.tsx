import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import React, { useState } from "react"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { t } from "../../i18n/i18n"
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

      <RaisedButton
        hierarchy="secondary"
        onClick={() => setKeskeytysModalVisible(true)}
      >
        Muokkaa oppivelvollisuuden keskeytystä
      </RaisedButton>

      {keskeytysModalVisible && (
        <OppivelvollisuudenKeskeytysModal
          oppija={props.oppija.oppija}
          onClose={() => setKeskeytysModalVisible(false)}
        />
      )}
    </>
  )
}

const oppivelvollisuusValue = (
  oppija: OppijaHakutilanteillaLaajatTiedot
): string =>
  pipe(
    oppija.oppivelvollisuudenKeskeytykset,
    A.filter((ovk) => ovk.voimassa),
    A.head,
    O.map((ovk) =>
      ovk.loppu !== undefined
        ? t("oppija__oppivelvollisuus_keskeytetty_value", {
            alkuPvm: formatDate(ovk.alku),
            loppuPvm: formatDate(ovk.loppu),
          })
        : t("oppija__oppivelvollisuus_keskeytetty_toistaiseksi_value", {
            alkuPvm: formatDate(ovk.alku),
          })
    ),
    O.getOrElse(() =>
      t("oppija__oppivelvollisuus_voimassa_value", {
        date: formatNullableDate(oppija.oppija.oppivelvollisuusVoimassaAsti),
      })
    )
  )
