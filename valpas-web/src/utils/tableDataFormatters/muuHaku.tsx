import React from "react"
import { ToggleSwitch } from "../../components/buttons/ToggleSwitch"
import { Value } from "../../components/tables/DataTable"
import { t } from "../../i18n/i18n"
import { OpiskeluoikeusSuppeatTiedot } from "../../state/apitypes/opiskeluoikeus"
import {
  lisätietoMatches,
  OppijaHakutilanteillaSuppeatTiedot,
} from "../../state/apitypes/oppija"
import { SetMuuHakuCallback } from "../../views/hakutilanne/HakutilanneTable"

export const muuHakuSwitchValue = (
  oppija: OppijaHakutilanteillaSuppeatTiedot,
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot,
  onSetMuuHaku: SetMuuHakuCallback,
): Value => {
  const lisätiedot = oppija.lisätiedot.find(
    lisätietoMatches(
      oppija.oppija.henkilö.oid,
      opiskeluoikeus.oid,
      opiskeluoikeus.oppilaitos.oid,
    ),
  )
  const muuHaku = lisätiedot?.muuHaku || false

  return {
    value: muuHaku ? t("Kyllä") : t("Ei"),
    display: (
      <ToggleSwitch
        value={muuHaku}
        onChanged={(state) =>
          onSetMuuHaku(oppija.oppija.henkilö.oid, opiskeluoikeus, state)
        }
      />
    ),
  }
}
