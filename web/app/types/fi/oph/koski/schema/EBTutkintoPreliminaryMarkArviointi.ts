import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * EBTutkintoPreliminaryMarkArviointi
 *
 * @see `fi.oph.koski.schema.EBTutkintoPreliminaryMarkArviointi`
 */
export type EBTutkintoPreliminaryMarkArviointi = {
  $class: 'fi.oph.koski.schema.EBTutkintoPreliminaryMarkArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export const EBTutkintoPreliminaryMarkArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}): EBTutkintoPreliminaryMarkArviointi => ({
  $class: 'fi.oph.koski.schema.EBTutkintoPreliminaryMarkArviointi',
  ...o
})

export const isEBTutkintoPreliminaryMarkArviointi = (
  a: any
): a is EBTutkintoPreliminaryMarkArviointi =>
  a?.$class === 'fi.oph.koski.schema.EBTutkintoPreliminaryMarkArviointi'
