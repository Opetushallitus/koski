import { LocalizedString } from './LocalizedString'

/**
 * Tutkintotoimikunta
 *
 * @see `fi.oph.koski.schema.Tutkintotoimikunta`
 */
export type Tutkintotoimikunta = {
  $class: 'fi.oph.koski.schema.Tutkintotoimikunta'
  nimi: LocalizedString
  tutkintotoimikunnanNumero: string
}

export const Tutkintotoimikunta = (o: {
  nimi: LocalizedString
  tutkintotoimikunnanNumero: string
}): Tutkintotoimikunta => ({
  $class: 'fi.oph.koski.schema.Tutkintotoimikunta',
  ...o
})

export const isTutkintotoimikunta = (a: any): a is Tutkintotoimikunta =>
  a?.$class === 'fi.oph.koski.schema.Tutkintotoimikunta'
