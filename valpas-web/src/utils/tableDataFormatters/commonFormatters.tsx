import React from "react"
import { Link } from "react-router-dom"
import { Value } from "../../components/tables/DataTable"
import { HenkilöTiedot } from "../../state/apitypes/henkilo"
import {
  Suorituksentyyppi,
  suorituksenTyyppiToKoulutustyyppi,
} from "../../state/apitypes/suorituksentyyppi"
import { ISODate, Oid } from "../../state/common"
import { oppijaPath } from "../../state/paths"
import { OppijaViewBackNavProps } from "../../views/oppija/OppijaView"
import { FilterableValue } from "../conversions"
import { formatNullableDate } from "../date"

export const nullableValue = <T extends FilterableValue>(
  value: T | undefined
): Value => ({
  value,
  display: value || "–",
})

export const nullableDateValue = (date: ISODate | undefined): Value => ({
  value: date || "0000-00-00",
  display: formatNullableDate(date),
})

export const nullableKoulutustyyppiValue = (
  tyyppi: Suorituksentyyppi | undefined
): Value => nullableValue(tyyppi && suorituksenTyyppiToKoulutustyyppi(tyyppi))

export const oppijanNimiValue = (urlBackRef: keyof OppijaViewBackNavProps) => (
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
