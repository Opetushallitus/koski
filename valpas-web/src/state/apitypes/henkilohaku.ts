import { Oid } from "../common"

export type HenkilöhakuResult =
  | LöytyiHenkilöhakuResult
  | EiLöytynytHenkilöhakuResult

export type LöytyiHenkilöhakuResult = {
  ok: true
  oid: Oid
  hetu?: string
  etunimet: string
  sukunimi: string
}

export type EiLöytynytHenkilöhakuResult = {
  ok: false
}

export const isLöytyiHenkilöhakuResult = (
  hakutieto: HenkilöhakuResult
): hakutieto is LöytyiHenkilöhakuResult => hakutieto.ok === true

export const isEiLöytynytHenkilöhakuResult = (
  hakutieto: HenkilöhakuResult
): hakutieto is EiLöytynytHenkilöhakuResult => hakutieto.ok === false
