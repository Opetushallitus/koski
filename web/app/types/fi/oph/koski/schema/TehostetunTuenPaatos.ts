import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * TehostetunTuenPäätös
 *
 * @see `fi.oph.koski.schema.TehostetunTuenPäätös`
 */
export type TehostetunTuenPäätös = {
  $class: 'fi.oph.koski.schema.TehostetunTuenPäätös'
  alku: string
  loppu?: string
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
}

export const TehostetunTuenPäätös = (o: {
  alku: string
  loppu?: string
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
}): TehostetunTuenPäätös => ({
  $class: 'fi.oph.koski.schema.TehostetunTuenPäätös',
  ...o
})

export const isTehostetunTuenPäätös = (a: any): a is TehostetunTuenPäätös =>
  a?.$class === 'TehostetunTuenPäätös'
