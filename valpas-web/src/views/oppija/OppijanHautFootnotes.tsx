import bem from "bem-ts"
import * as A from "fp-ts/Array"
import { flow } from "fp-ts/lib/function"
import * as string from "fp-ts/string"
import React from "react"
import { Datum, Value } from "../../components/tables/DataTable"
import { T } from "../../i18n/i18n"
import {
  Hakutoive,
  isLiitteetTarkastamatta,
  isLiitteetTarkastettu,
} from "../../state/apitypes/hakutoive"
import { nonNull } from "../../utils/arrays"

const b = bem("oppijanhaut")

const hakutoiveFootnotes = {
  "1": "oppija__hakenut_harkinnanvaraisesti",
  "2": "oppija__hakenut_harkinnanvaraisesti_liitteet_tarkastettu",
  "3": "oppija__hakenut_harkinnanvaraisesti_liitteet_tarkastamatta",
}

export type HakutoiveFootnoteRefId = keyof typeof hakutoiveFootnotes
export type ValueWithFootnote = Value & {
  footnoteRefId?: HakutoiveFootnoteRefId
}
export type DatumWithFootnotes = Omit<Datum, "values"> & {
  values: ValueWithFootnote[]
}

export const hakutoiveToFootnoteRefId = (
  hakutoive: Hakutoive
): HakutoiveFootnoteRefId | null =>
  !hakutoive.harkinnanvarainen
    ? null
    : isLiitteetTarkastettu(hakutoive)
    ? "2"
    : isLiitteetTarkastamatta(hakutoive)
    ? "3"
    : "1"

export const pickFootnoteRefs = flow(
  A.chain((d: DatumWithFootnotes) => d.values),
  A.map((v) => v.footnoteRefId),
  A.filter(nonNull),
  A.uniq<HakutoiveFootnoteRefId>(string.Eq),
  A.sort(string.Ord)
)

export type FootnoteReferenceProps = {
  refId: HakutoiveFootnoteRefId
}

export const FootnoteReference = (props: FootnoteReferenceProps) => (
  <span className={b("footnotereference")}>{props.refId}</span>
)

export const Footnote = (props: FootnoteReferenceProps) => (
  <div className={b("footnote")}>
    {props.refId}) <T id={hakutoiveFootnotes[props.refId]} />
  </div>
)
