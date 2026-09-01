import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Liikkuvuusjakso
 *
 * @see `fi.oph.koski.schema.Liikkuvuusjakso`
 */
export type Liikkuvuusjakso = {
  $class: 'fi.oph.koski.schema.Liikkuvuusjakso'
  tyyppi: Koodistokoodiviite<'virtaliikkuvuudentyyppi', string>
  loppu?: string
  liikkuvuusohjelma: Koodistokoodiviite<'virtaliikkuvuusohjelma', string>
  luokittelu?: Array<Koodistokoodiviite<'liikkuvuudenluokittelu', string>>
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  suunta: Koodistokoodiviite<'virtaliikkuvuudensuunta', string>
  alku: string
}

export const Liikkuvuusjakso = (o: {
  tyyppi: Koodistokoodiviite<'virtaliikkuvuudentyyppi', string>
  loppu?: string
  liikkuvuusohjelma: Koodistokoodiviite<'virtaliikkuvuusohjelma', string>
  luokittelu?: Array<Koodistokoodiviite<'liikkuvuudenluokittelu', string>>
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  suunta: Koodistokoodiviite<'virtaliikkuvuudensuunta', string>
  alku: string
}): Liikkuvuusjakso => ({ $class: 'fi.oph.koski.schema.Liikkuvuusjakso', ...o })

Liikkuvuusjakso.className = 'fi.oph.koski.schema.Liikkuvuusjakso' as const

export const isLiikkuvuusjakso = (a: any): a is Liikkuvuusjakso =>
  a?.$class === 'fi.oph.koski.schema.Liikkuvuusjakso'
