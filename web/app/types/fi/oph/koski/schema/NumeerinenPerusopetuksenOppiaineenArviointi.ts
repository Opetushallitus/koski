import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Numeerinen arviointi asteikolla 4 (hylätty) - 10 (erinomainen)
 *
 * @see `fi.oph.koski.schema.NumeerinenPerusopetuksenOppiaineenArviointi`
 */
export type NumeerinenPerusopetuksenOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.NumeerinenPerusopetuksenOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const NumeerinenPerusopetuksenOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}): NumeerinenPerusopetuksenOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.NumeerinenPerusopetuksenOppiaineenArviointi',
  ...o
})

export const isNumeerinenPerusopetuksenOppiaineenArviointi = (
  a: any
): a is NumeerinenPerusopetuksenOppiaineenArviointi =>
  a?.$class === 'NumeerinenPerusopetuksenOppiaineenArviointi'
