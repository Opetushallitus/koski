import { addYears, parseISO } from "date-fns"
import { format } from "date-fns/esm"
import React from "react"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { t } from "../../i18n/i18n"
import { Opiskeluoikeus, Oppija } from "../../state/oppijat"
import { DATE_FORMAT } from "../../utils/date"

export type OppijanOppivelvollisuustiedotProps = {
  oppija: Oppija
}

export const OppijanOppivelvollisuustiedot = (
  props: OppijanOppivelvollisuustiedotProps
) => (
  <InfoTable>
    <InfoTableRow
      label={t("oppija__opiskelutilanne")}
      value={opiskelutilanne(props.oppija)}
    />
    <InfoTableRow
      label={t("oppija__oppivelvollisuus_voimassa")}
      value={t("oppija__oppivelvollisuus_voimassa_value", {
        date: oppivelvollisuusVoimassa(props.oppija),
      })}
    />
  </InfoTable>
)

const opiskelutilanne = (oppija: Oppija) =>
  t(
    oppija.opiskeluoikeudet.some(isOpiskelemassa)
      ? "oppija__opiskelutilanne__opiskelemassa"
      : "oppija__opiskelutilanne__ei_opiskelupaikkaa"
  )

const isOpiskelemassa = (opiskeluoikeus: Opiskeluoikeus): boolean => {
  const tila = opiskeluoikeus.viimeisinTila?.koodiarvo
  return tila === "lasna" || tila === "valiaikaisestikeskeytynyt"
}

const oppivelvollisuusVoimassa = (oppija: Oppija) =>
  format(addYears(parseISO(oppija.henkilö.syntymäaika), 18), DATE_FORMAT)
