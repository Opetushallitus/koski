import { LocalizedString } from './LocalizedString'

/**
 * Paikallinen, koulutustoimijan oma kooditus. Käytetään kansallisen koodiston puuttuessa
 *
 * @see `fi.oph.koski.schema.PaikallinenKoodi`
 */
export type PaikallinenKoodi = {
  $class: 'fi.oph.koski.schema.PaikallinenKoodi'
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}

export const PaikallinenKoodi = (o: {
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}): PaikallinenKoodi => ({
  $class: 'fi.oph.koski.schema.PaikallinenKoodi',
  ...o
})

PaikallinenKoodi.className = 'fi.oph.koski.schema.PaikallinenKoodi' as const

export const isPaikallinenKoodi = (a: any): a is PaikallinenKoodi =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenKoodi'
