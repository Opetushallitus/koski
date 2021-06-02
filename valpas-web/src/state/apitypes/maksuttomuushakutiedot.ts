import { Oid } from "../common"

export type HenkilöMaksuttomuushakuResult =
  | HenkilöMaksuttomuushakutulos
  | HenkilöHakutiedotMaksuttomuusEiPääteltävissä

export type HenkilöMaksuttomuushakutulos = {
  ok: true
  oid: Oid
  hetu?: string
  etunimet: string
  sukunimi: string
}

export type HenkilöHakutiedotMaksuttomuusEiPääteltävissä = {
  ok: false
}

export const isMaksuttomuushakutulos = (
  hakutieto: HenkilöMaksuttomuushakuResult
): hakutieto is HenkilöMaksuttomuushakutulos => hakutieto.ok === true

export const isEiPääteltävissäOleva = (
  hakutieto: HenkilöMaksuttomuushakuResult
): hakutieto is HenkilöHakutiedotMaksuttomuusEiPääteltävissä =>
  hakutieto.ok === false
