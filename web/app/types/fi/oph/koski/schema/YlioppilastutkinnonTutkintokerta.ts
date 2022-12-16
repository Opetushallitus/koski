import { LocalizedString } from './LocalizedString'

/**
 * YlioppilastutkinnonTutkintokerta
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonTutkintokerta`
 */
export type YlioppilastutkinnonTutkintokerta = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonTutkintokerta'
  koodiarvo: string
  vuosi: number
  vuodenaika: LocalizedString
}

export const YlioppilastutkinnonTutkintokerta = (o: {
  koodiarvo: string
  vuosi: number
  vuodenaika: LocalizedString
}): YlioppilastutkinnonTutkintokerta => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonTutkintokerta',
  ...o
})

export const isYlioppilastutkinnonTutkintokerta = (
  a: any
): a is YlioppilastutkinnonTutkintokerta =>
  a?.$class === 'YlioppilastutkinnonTutkintokerta'
