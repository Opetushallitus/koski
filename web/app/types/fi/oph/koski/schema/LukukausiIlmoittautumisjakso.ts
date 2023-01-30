import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu } from './LukuvuosiIlmoittautumisjaksonLukuvuosiMaksu'

/**
 * Lukukausi_Ilmoittautumisjakso
 *
 * @see `fi.oph.koski.schema.Lukukausi_Ilmoittautumisjakso`
 */
export type Lukukausi_Ilmoittautumisjakso = {
  $class: 'fi.oph.koski.schema.Lukukausi_Ilmoittautumisjakso'
  tila: Koodistokoodiviite<'virtalukukausiilmtila', string>
  maksetutLukuvuosimaksut?: Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
  ylioppilaskunnanJäsen?: boolean
  ythsMaksettu?: boolean
  loppu?: string
  alku: string
}

export const Lukukausi_Ilmoittautumisjakso = (o: {
  tila: Koodistokoodiviite<'virtalukukausiilmtila', string>
  maksetutLukuvuosimaksut?: Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
  ylioppilaskunnanJäsen?: boolean
  ythsMaksettu?: boolean
  loppu?: string
  alku: string
}): Lukukausi_Ilmoittautumisjakso => ({
  $class: 'fi.oph.koski.schema.Lukukausi_Ilmoittautumisjakso',
  ...o
})

Lukukausi_Ilmoittautumisjakso.className =
  'fi.oph.koski.schema.Lukukausi_Ilmoittautumisjakso' as const

export const isLukukausi_Ilmoittautumisjakso = (
  a: any
): a is Lukukausi_Ilmoittautumisjakso =>
  a?.$class === 'fi.oph.koski.schema.Lukukausi_Ilmoittautumisjakso'
