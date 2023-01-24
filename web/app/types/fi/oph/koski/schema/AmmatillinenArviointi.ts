import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * AmmatillinenArviointi
 *
 * @see `fi.oph.koski.schema.AmmatillinenArviointi`
 */
export type AmmatillinenArviointi = {
  $class: 'fi.oph.koski.schema.AmmatillinenArviointi'
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export const AmmatillinenArviointi = (o: {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}): AmmatillinenArviointi => ({
  $class: 'fi.oph.koski.schema.AmmatillinenArviointi',
  ...o
})

AmmatillinenArviointi.className =
  'fi.oph.koski.schema.AmmatillinenArviointi' as const

export const isAmmatillinenArviointi = (a: any): a is AmmatillinenArviointi =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenArviointi'
