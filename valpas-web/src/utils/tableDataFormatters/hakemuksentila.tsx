import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import React from "react"
import { Link } from "react-router-dom"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { Value } from "../../components/tables/DataTable"
import { getLocalizedMaybe, t } from "../../i18n/i18n"
import { Translation } from "../../state/apitypes/appConfiguration"
import { HakuSuppeatTiedot } from "../../state/apitypes/haku"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { Oid } from "../../state/common"
import { oppijaPath } from "../../state/paths"
import { formatNullableDate } from "../date"

export const hakemuksenTilaValue = (
  oppija: OppijaHakutilanteillaSuppeatTiedot,
  basePath: string
): Value => {
  const { hakutilanteet, hakutilanneError, isLoadingHakutilanteet } = oppija
  const oppijaOid = oppija.oppija.henkilö.oid

  const hakemuksenTilaValue = hakemuksenTilaT(
    hakutilanteet.length,
    hakutilanneError,
    isLoadingHakutilanteet
  )

  return {
    value: hakemuksenTilaValue,
    display: hakemuksenTilaDisplay(
      hakutilanteet,
      hakemuksenTilaValue,
      oppijaOid,
      basePath
    ),
    tooltip: hakutilanteet.map(hakuTooltip).join("\n"),
  }
}

const hakemuksenTilaT = (
  hakemusCount: number,
  hakutilanneError?: string,
  isLoadingHakutilanteet?: boolean
): Translation => {
  if (isLoadingHakutilanteet) return t("Ladataan")
  else if (hakutilanneError) return t("oppija__hakuhistoria_virhe")
  else if (hakemusCount == 0) return t("hakemuksentila__ei_hakemusta")
  else if (hakemusCount == 1) return t("hakemuksentila__hakenut")
  else return t("hakemuksentila__n_hakua", { lukumäärä: hakemusCount })
}

const hakemuksenTilaDisplay = (
  hakutilanteet: HakuSuppeatTiedot[],
  hakemuksenTilaValue: Translation,
  oppijaOid: Oid,
  basePath: string
) =>
  pipe(
    A.head(hakutilanteet),
    O.map((hakutilanne) =>
      hakutilanteet.length == 1 ? (
        <ExternalLink to={hakutilanne.hakemusUrl}>
          {hakemuksenTilaValue}
        </ExternalLink>
      ) : (
        <Link to={oppijaPath.href(basePath, { oppijaOid })}>
          {hakemuksenTilaValue}
        </Link>
      )
    ),
    O.toNullable
  )

const hakuTooltip = (haku: HakuSuppeatTiedot): string =>
  t("hakemuksentila__tooltip", {
    haku: getLocalizedMaybe(haku.hakuNimi) || "?",
    muokkausPvm: formatNullableDate(haku.muokattu),
  })
