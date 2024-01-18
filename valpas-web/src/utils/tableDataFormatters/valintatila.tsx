import * as A from "fp-ts/Array"
import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import React from "react"
import { WarningIcon } from "../../components/icons/Icon"
import { Value } from "../../components/tables/DataTable"
import { getLocalizedMaybe, t } from "../../i18n/i18n"
import { HakuSuppeatTiedot, selectByHakutoive } from "../../state/apitypes/haku"
import {
  isEiPaikkaa,
  isHyväksytty,
  isVarasijalla,
  SuppeaHakutoive,
} from "../../state/apitypes/hakutoive"
import { nonEmptyEvery } from "../arrays"
import { nullableValue } from "./commonFormatters"

export const valintatilaValue = (haut: HakuSuppeatTiedot[]): Value => {
  const hyväksytytHakutoiveet = selectByHakutoive(haut, isHyväksytty)
  if (A.isNonEmpty(hyväksytytHakutoiveet)) {
    return hyväksyttyValintatila(hyväksytytHakutoiveet)
  }

  const [varasija] = selectByHakutoive(haut, isVarasijalla)
  if (varasija) {
    return {
      value: t("valintatieto__varasija"),
      display: t("valintatieto__varasija_hakukohde", {
        hakukohde: getLocalizedMaybe(varasija.organisaatioNimi) || "?",
      }),
    }
  }

  if (
    nonEmptyEvery(haut, (haku) => nonEmptyEvery(haku.hakutoiveet, isEiPaikkaa))
  ) {
    return {
      value: t("valintatieto__ei_opiskelupaikkaa"),
      icon: <WarningIcon />,
    }
  }

  return nullableValue(null)
}

const hyväksyttyValintatila = (
  hyväksytytHakutoiveet: NonEmptyArray<SuppeaHakutoive>,
): Value => {
  const buildHyväksyttyValue = (hakutoive: SuppeaHakutoive) => {
    return {
      value: t("valintatieto__hyväksytty", {
        hakukohde: orderedHakukohde(
          hakutoive.hakutoivenumero,
          t("valintatieto__hakukohde_lc"),
        ),
      }),
      display: orderedHakukohde(
        hakutoive.hakutoivenumero,
        getLocalizedMaybe(hakutoive.organisaatioNimi) || "?",
      ),
    }
  }

  if (hyväksytytHakutoiveet.length === 1) {
    return buildHyväksyttyValue(hyväksytytHakutoiveet[0])
  }

  return {
    value: t("valintatieto__hyväksytty_n_hakutoivetta", {
      lukumäärä: hyväksytytHakutoiveet.length,
    }),
    filterValues: hyväksytytHakutoiveet.map(
      (hakutoive) => buildHyväksyttyValue(hakutoive).value,
    ),
    tooltip: hyväksytytHakutoiveet
      .map((ht) => buildHyväksyttyValue(ht).display)
      .join("\n"),
  }
}

const orderedHakukohde = (
  hakutoivenumero: number | undefined,
  hakukohde: string,
) => (hakutoivenumero ? `${hakutoivenumero}. ${hakukohde}` : hakukohde)
