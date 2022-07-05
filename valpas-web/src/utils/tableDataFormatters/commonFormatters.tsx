import React from "react"
import { Link } from "react-router-dom"
import { Spinner } from "../../components/icons/Spinner"
import { Value } from "../../components/tables/DataTable"
import { t } from "../../i18n/i18n"
import { HenkilöTiedot } from "../../state/apitypes/henkilo"
import {
  Suorituksentyyppi,
  suorituksenTyyppiToKoulutustyyppi,
} from "../../state/apitypes/suorituksentyyppi"
import { ISODate, Oid } from "../../state/common"
import { oppijaPath, OppijaPathBackRefs } from "../../state/paths"
import { FilterableNonNullValue, FilterableValue } from "../conversions"
import { formatDate, formatNullableDate } from "../date"

export const nonNullableValue = <T extends FilterableNonNullValue>(
  value: T
): Value => ({
  value,
})

export const nullableValue = <T extends FilterableValue>(value: T): Value => ({
  value,
  display: value || "–",
})

export const dateValue = (date: ISODate): Value => ({
  value: date,
  display: formatDate(date),
})

export const nullableDateValue = (date: ISODate | undefined): Value => ({
  value: date || "0000-00-00",
  display: formatNullableDate(date),
})

export const nullableKoulutustyyppiValue = (
  tyyppi: Suorituksentyyppi | undefined
): Value => nullableValue(tyyppi && suorituksenTyyppiToKoulutustyyppi(tyyppi))

export const oppijanNimiValue = (urlBackRef: keyof OppijaPathBackRefs) => (
  henkilö: HenkilöTiedot,
  organisaatioOid: Oid,
  basePath: string
): Value => {
  const value = `${henkilö.sukunimi} ${henkilö.etunimet}`
  const linkTo = oppijaPath.href(basePath, {
    oppijaOid: henkilö.oid,
    [urlBackRef]: organisaatioOid,
  })

  return {
    value,
    display: <Link to={linkTo}>{value}</Link>,
  }
}

export const loadingValue = (showSpinner: boolean): Value => ({
  value: t("Ladataan"),
  display: showSpinner ? <Spinner /> : " ",
})
