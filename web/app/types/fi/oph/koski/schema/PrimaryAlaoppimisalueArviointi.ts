import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * PrimaryAlaoppimisalueArviointi
 *
 * @see `fi.oph.koski.schema.PrimaryAlaoppimisalueArviointi`
 */
export type PrimaryAlaoppimisalueArviointi = {
  $class: 'fi.oph.koski.schema.PrimaryAlaoppimisalueArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiprimarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export const PrimaryAlaoppimisalueArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiprimarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}): PrimaryAlaoppimisalueArviointi => ({
  $class: 'fi.oph.koski.schema.PrimaryAlaoppimisalueArviointi',
  ...o
})

export const isPrimaryAlaoppimisalueArviointi = (
  a: any
): a is PrimaryAlaoppimisalueArviointi =>
  a?.$class === 'PrimaryAlaoppimisalueArviointi'
