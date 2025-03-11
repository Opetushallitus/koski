import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * YleisenKielitutkinnonOsakokeenArviointi
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenArviointi`
 */
export type YleisenKielitutkinnonOsakokeenArviointi = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenArviointi'
  arvosana: Koodistokoodiviite<'ykiarvosana', string>
  päivä: string
  hyväksytty?: boolean
}

export const YleisenKielitutkinnonOsakokeenArviointi = (o: {
  arvosana: Koodistokoodiviite<'ykiarvosana', string>
  päivä: string
  hyväksytty?: boolean
}): YleisenKielitutkinnonOsakokeenArviointi => ({
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenArviointi',
  ...o
})

YleisenKielitutkinnonOsakokeenArviointi.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenArviointi' as const

export const isYleisenKielitutkinnonOsakokeenArviointi = (
  a: any
): a is YleisenKielitutkinnonOsakokeenArviointi =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenArviointi'
