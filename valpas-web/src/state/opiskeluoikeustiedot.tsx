import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/function"
import React from "react"
import { FutureSuccessIcon, SuccessIcon } from "../components/icons/Icon"
import { Value } from "../components/tables/DataTable"
import { getLocalizedMaybe, t } from "../i18n/i18n"
import { nonNull } from "../utils/arrays"
import { formatDate, formatNullableDate } from "../utils/date"
import {
  OpintotasonTiedot,
  OpiskeluoikeusSuppeatTiedot,
  voimassaolevaTaiTulevaPeruskoulunJälkeinenMuunaOpintonaNäytettäväOpiskeluoikeus,
} from "./apitypes/opiskeluoikeus"
import { organisaatioNimi } from "./apitypes/organisaatiot"
import {
  isVoimassa,
  isVoimassaTulevaisuudessa,
} from "./apitypes/valpasopiskeluoikeudentila"

export const perusopetuksenJälkeisetOpiskeluoikeustiedot = (
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[]
): Value | null => {
  const oos = opiskeluoikeudet.filter(
    voimassaolevaTaiTulevaPeruskoulunJälkeinenMuunaOpintonaNäytettäväOpiskeluoikeus
  )

  const toValue = (oo: OpiskeluoikeusSuppeatTiedot) => {
    const kohde = [
      organisaatioNimi(oo.oppilaitos),
      getLocalizedMaybe(oo.tyyppi.nimi),
    ]
      .filter(nonNull)
      .join(", ")

    return isVoimassa(oo.perusopetuksenJälkeinenTiedot!.tarkastelupäivänTila) ||
      oo.perusopetuksenJälkeinenTiedot!.alkamispäivä === undefined
      ? kohde
      : t("opiskeluoikeudet__pvm_alkaen_kohde", {
          päivämäärä: formatNullableDate(
            oo.perusopetuksenJälkeinenTiedot!.alkamispäivä
          ),
          kohde,
        })
  }

  const icon = oos.some(
    (oo) =>
      isVoimassa(oo.perusopetuksenJälkeinenTiedot!.tarkastelupäivänTila) &&
      oo.perusopetuksenJälkeinenTiedot!.alkamispäivä !== undefined
  ) ? (
    <SuccessIcon />
  ) : oos.some(
      (oo) =>
        isVoimassaTulevaisuudessa(
          oo.perusopetuksenJälkeinenTiedot!.tarkastelupäivänTila
        ) && oo.perusopetuksenJälkeinenTiedot!.alkamispäivä !== undefined
    ) ? (
    <FutureSuccessIcon />
  ) : undefined

  switch (oos.length) {
    case 0:
      return null
    case 1:
      return { value: toValue(oos[0]!!), icon }
    default:
      const filterValues = oos.map(toValue).filter(nonNull)
      return {
        value: t("opiskeluoikeudet__n_opiskeluoikeutta", {
          lukumäärä: oos.length,
        }),
        filterValues,
        tooltip: filterValues.join("\n"),
        icon,
      }
  }
}

export const perusopetuksenJälkeistäPreferoivatOpiskeluoikeustiedot = (
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[],
  käsiteltäväOpiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): Value | null => {
  const ooTiedots: Array<[OpiskeluoikeusSuppeatTiedot, OpintotasonTiedot]> =
    pipe(
      opiskeluoikeudet,
      A.filter((oo) => oo.oid != käsiteltäväOpiskeluoikeus.oid),
      A.chain(perusopetuksenJälkeistäPreferoivaNäytettäväOpiskeluoikeusTieto)
    )

  const toValue = (
    ooTiedot: [OpiskeluoikeusSuppeatTiedot, OpintotasonTiedot]
  ) => {
    const kohde = [
      organisaatioNimi(ooTiedot[0].oppilaitos),
      getLocalizedMaybe(ooTiedot[0].tyyppi.nimi),
    ]
      .filter(nonNull)
      .join(", ")

    return isVoimassa(ooTiedot[1].tarkastelupäivänTila) ||
      ooTiedot[1].alkamispäivä === undefined
      ? kohde
      : t("opiskeluoikeudet__pvm_alkaen_kohde", {
          päivämäärä: formatDate(ooTiedot[1].alkamispäivä),
          kohde,
        })
  }

  const icon = ooTiedots.some((ooTiedot) =>
    isVoimassaTulevaisuudessa(ooTiedot[1].tarkastelupäivänTila)
  ) ? (
    <FutureSuccessIcon />
  ) : undefined

  switch (ooTiedots.length) {
    case 0:
      return null
    case 1:
      return { value: toValue(ooTiedots[0]!!), icon }
    default:
      const filterValues = ooTiedots.map(toValue).filter(nonNull)
      return {
        value: t("opiskeluoikeudet__n_opiskeluoikeutta", {
          lukumäärä: ooTiedots.length,
        }),
        filterValues,
        tooltip: filterValues.join("\n"),
        icon,
      }
  }
}

const perusopetuksenJälkeistäPreferoivaNäytettäväOpiskeluoikeusTieto = (
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): Array<[OpiskeluoikeusSuppeatTiedot, OpintotasonTiedot]> => {
  const näytettävä = (
    tiedot: OpintotasonTiedot | undefined
  ): Array<[OpiskeluoikeusSuppeatTiedot, OpintotasonTiedot]> => {
    const tila = tiedot?.tarkastelupäivänTila

    if (
      tila !== undefined &&
      (isVoimassa(tila) || isVoimassaTulevaisuudessa(tila))
    ) {
      return [[opiskeluoikeus, tiedot!]]
    } else {
      return []
    }
  }

  // Näytetään oletuksena perusopetuksen jälkeiset opinnot, mutta jos niitä ei ole tai ne eivät ole voimassa, niin
  // näytetään perusopetuksen tiedot
  const perusopetuksenJälkeinenResult = näytettävä(
    opiskeluoikeus.perusopetuksenJälkeinenTiedot
  )

  if (perusopetuksenJälkeinenResult.length) {
    return perusopetuksenJälkeinenResult
  } else {
    return näytettävä(opiskeluoikeus.perusopetusTiedot)
  }
}
