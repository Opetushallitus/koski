import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * YleisenKielitutkinnonOsanArviointi
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonOsanArviointi`
 */
export type YleisenKielitutkinnonOsanArviointi = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsanArviointi'
  arvosana: Koodistokoodiviite<'ykiarvosana', string>
  päivä: string
  hyväksytty?: boolean
}

export const YleisenKielitutkinnonOsanArviointi = (o: {
  arvosana: Koodistokoodiviite<'ykiarvosana', string>
  päivä: string
  hyväksytty?: boolean
}): YleisenKielitutkinnonOsanArviointi => ({
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsanArviointi',
  ...o
})

YleisenKielitutkinnonOsanArviointi.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonOsanArviointi' as const

export const isYleisenKielitutkinnonOsanArviointi = (
  a: any
): a is YleisenKielitutkinnonOsanArviointi =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonOsanArviointi'
