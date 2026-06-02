import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Sanallinen arviointi; koodiarvot G (godkänd), D (deltagit), U (underkänd).
 *
 * @see `fi.oph.koski.schema.SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi`
 */
export type SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'ahvenanmaanarviointiasteikkoyleissivistava',
    'G' | 'D' | 'U'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'ahvenanmaanarviointiasteikkoyleissivistava',
    'G' | 'D' | 'U'
  >
  päivä?: string
  hyväksytty?: boolean
}): SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi => ({
  $class:
    'fi.oph.koski.schema.SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi',
  ...o
})

SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi.className =
  'fi.oph.koski.schema.SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi' as const

export const isSanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi = (
  a: any
): a is SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi'
