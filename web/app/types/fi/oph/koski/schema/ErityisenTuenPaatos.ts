import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Oppivelvollisen erityisen tuen päätöstiedot
 *
 * @see `fi.oph.koski.schema.ErityisenTuenPäätös`
 */
export type ErityisenTuenPäätös = {
  $class: 'fi.oph.koski.schema.ErityisenTuenPäätös'
  loppu?: string
  erityisryhmässä?: boolean
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  alku?: string
  toteutuspaikka?: Koodistokoodiviite<'erityisopetuksentoteutuspaikka', string>
  opiskeleeToimintaAlueittain: boolean
}

export const ErityisenTuenPäätös = (o: {
  loppu?: string
  erityisryhmässä?: boolean
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  alku?: string
  toteutuspaikka?: Koodistokoodiviite<'erityisopetuksentoteutuspaikka', string>
  opiskeleeToimintaAlueittain: boolean
}): ErityisenTuenPäätös => ({
  $class: 'fi.oph.koski.schema.ErityisenTuenPäätös',
  ...o
})

ErityisenTuenPäätös.className =
  'fi.oph.koski.schema.ErityisenTuenPäätös' as const

export const isErityisenTuenPäätös = (a: any): a is ErityisenTuenPäätös =>
  a?.$class === 'fi.oph.koski.schema.ErityisenTuenPäätös'
