import { Oid } from "../common"

export type HenkilöhakuResult =
  | LöytyiHenkilöhakuResult
  | EiLöytynytHenkilöhakuResult
  | EiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult

export type LöytyiHenkilöhakuResult = {
  ok: true
  oid: Oid
  hetu?: string
  etunimet: string
  sukunimi: string
  vainOppijanumerorekisterissä?: boolean
  eiLainTaiMaksuttomuudenPiirissä?: boolean
}

export type EiLöytynytHenkilöhakuResult = {
  eiLainTaiMaksuttomuudenPiirissä?: false
  ok: false
}

export type EiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult = {
  eiLainTaiMaksuttomuudenPiirissä: true
  ok: false
}

export const isLöytyiHenkilöhakuResult = (
  hakutieto: HenkilöhakuResult
): hakutieto is LöytyiHenkilöhakuResult => hakutieto.ok === true

export const isEiLöytynytHenkilöhakuResult = (
  hakutieto: HenkilöhakuResult
): hakutieto is EiLöytynytHenkilöhakuResult =>
  hakutieto.ok === false && !hakutieto.eiLainTaiMaksuttomuudenPiirissä

export const isEiLöytynytEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult = (
  hakutieto: HenkilöhakuResult
): hakutieto is EiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult =>
  hakutieto.ok === false && !!hakutieto.eiLainTaiMaksuttomuudenPiirissä
