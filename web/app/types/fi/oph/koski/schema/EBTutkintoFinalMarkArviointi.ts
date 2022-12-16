import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * EBTutkintoFinalMarkArviointi
 *
 * @see `fi.oph.koski.schema.EBTutkintoFinalMarkArviointi`
 */
export type EBTutkintoFinalMarkArviointi = {
  $class: 'fi.oph.koski.schema.EBTutkintoFinalMarkArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkifinalmark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export const EBTutkintoFinalMarkArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkifinalmark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}): EBTutkintoFinalMarkArviointi => ({
  $class: 'fi.oph.koski.schema.EBTutkintoFinalMarkArviointi',
  ...o
})

export const isEBTutkintoFinalMarkArviointi = (
  a: any
): a is EBTutkintoFinalMarkArviointi =>
  a?.$class === 'EBTutkintoFinalMarkArviointi'
