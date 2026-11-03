import { LocalizedString } from './LocalizedString'

/**
 * IB-tutkintokerta (examination session), joita on vuosittain kaksi: toukokuu ja marraskuu
 *
 * @see `fi.oph.koski.schema.IBTutkintokerta`
 */
export type IBTutkintokerta = {
  $class: 'fi.oph.koski.schema.IBTutkintokerta'
  koodiarvo: string
  vuosi: number
  kuukausi: LocalizedString
}

export const IBTutkintokerta = (o: {
  koodiarvo: string
  vuosi: number
  kuukausi: LocalizedString
}): IBTutkintokerta => ({ $class: 'fi.oph.koski.schema.IBTutkintokerta', ...o })

IBTutkintokerta.className = 'fi.oph.koski.schema.IBTutkintokerta' as const

export const isIBTutkintokerta = (a: any): a is IBTutkintokerta =>
  a?.$class === 'fi.oph.koski.schema.IBTutkintokerta'
