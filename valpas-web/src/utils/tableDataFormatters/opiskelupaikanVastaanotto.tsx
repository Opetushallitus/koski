import React from "react"
import { SuccessIcon } from "../../components/icons/Icon"
import { Value } from "../../components/tables/DataTable"
import { getLocalizedMaybe, t } from "../../i18n/i18n"
import { HakuSuppeatTiedot, selectByHakutoive } from "../../state/apitypes/haku"
import { isVastaanotettu } from "../../state/apitypes/hakutoive"
import { nullableValue } from "./commonFormatters"

export const opiskelupaikanVastaanottotietoValue = (
  hakutilanteet: HakuSuppeatTiedot[],
): Value => {
  const vastaanotetut = selectByHakutoive(hakutilanteet, isVastaanotettu)
  switch (vastaanotetut.length) {
    case 0:
      return nullableValue(null)
    case 1:
      return {
        value: getLocalizedMaybe(vastaanotetut[0]?.organisaatioNimi),
        icon: <SuccessIcon />,
      }
    default:
      return {
        value: t("vastaanotettu__n_paikkaa", {
          lukumäärä: vastaanotetut.length,
        }),
        tooltip: vastaanotetut
          .map((vo) => getLocalizedMaybe(vo.organisaatioNimi))
          .join("\n"),
        icon: <SuccessIcon />,
      }
  }
}
