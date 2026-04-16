import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Numeerinen arviointi asteikolla 4 (hylätty) - 10 (erinomainen)
 *
 * @see `fi.oph.koski.schema.NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi`
 */
export type NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}): NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi => ({
  $class:
    'fi.oph.koski.schema.NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi',
  ...o
})

NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi.className =
  'fi.oph.koski.schema.NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi' as const

export const isNumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi = (
  a: any
): a is NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi'
