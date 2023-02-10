import { LocalizedString } from './LocalizedString'

/**
 * TaiteenPerusopetuksenOsasuorituksenTunnustus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenOsasuorituksenTunnustus`
 */
export type TaiteenPerusopetuksenOsasuorituksenTunnustus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOsasuorituksenTunnustus'
  selite: LocalizedString
}

export const TaiteenPerusopetuksenOsasuorituksenTunnustus = (o: {
  selite: LocalizedString
}): TaiteenPerusopetuksenOsasuorituksenTunnustus => ({
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOsasuorituksenTunnustus',
  ...o
})

TaiteenPerusopetuksenOsasuorituksenTunnustus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenOsasuorituksenTunnustus' as const

export const isTaiteenPerusopetuksenOsasuorituksenTunnustus = (
  a: any
): a is TaiteenPerusopetuksenOsasuorituksenTunnustus =>
  a?.$class ===
  'fi.oph.koski.schema.TaiteenPerusopetuksenOsasuorituksenTunnustus'
