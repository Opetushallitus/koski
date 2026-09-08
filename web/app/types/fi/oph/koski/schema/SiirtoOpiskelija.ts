import { Oppilaitos } from './Oppilaitos'

/**
 * SiirtoOpiskelija
 *
 * @see `fi.oph.koski.schema.SiirtoOpiskelija`
 */
export type SiirtoOpiskelija = {
  $class: 'fi.oph.koski.schema.SiirtoOpiskelija'
  siirtoPäivä: string
  lähdeOrganisaatio?: Oppilaitos
}

export const SiirtoOpiskelija = (o: {
  siirtoPäivä: string
  lähdeOrganisaatio?: Oppilaitos
}): SiirtoOpiskelija => ({
  $class: 'fi.oph.koski.schema.SiirtoOpiskelija',
  ...o
})

SiirtoOpiskelija.className = 'fi.oph.koski.schema.SiirtoOpiskelija' as const

export const isSiirtoOpiskelija = (a: any): a is SiirtoOpiskelija =>
  a?.$class === 'fi.oph.koski.schema.SiirtoOpiskelija'
