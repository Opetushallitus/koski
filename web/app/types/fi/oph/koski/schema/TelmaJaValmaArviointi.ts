import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * TelmaJaValmaArviointi
 *
 * @see `fi.oph.koski.schema.TelmaJaValmaArviointi`
 */
export type TelmaJaValmaArviointi = {
  $class: 'fi.oph.koski.schema.TelmaJaValmaArviointi'
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

export const TelmaJaValmaArviointi = (o: {
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
}): TelmaJaValmaArviointi => ({
  $class: 'fi.oph.koski.schema.TelmaJaValmaArviointi',
  ...o
})

TelmaJaValmaArviointi.className =
  'fi.oph.koski.schema.TelmaJaValmaArviointi' as const

export const isTelmaJaValmaArviointi = (a: any): a is TelmaJaValmaArviointi =>
  a?.$class === 'fi.oph.koski.schema.TelmaJaValmaArviointi'
