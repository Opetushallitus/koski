import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Oppivelvollisen erityisen tuen päätöstiedot
 *
 * @see `fi.oph.koski.schema.ErityisenTuenPäätös`
 */
export type ErityisenTuenPäätös = {
  $class: 'fi.oph.koski.schema.ErityisenTuenPäätös'
  toteutuspaikka?: Koodistokoodiviite<'erityisopetuksentoteutuspaikka', string>
  opiskeleeToimintaAlueittain: boolean
  loppu?: string
  erityisryhmässä?: boolean
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  alku?: string
}

export const ErityisenTuenPäätös = (o: {
  toteutuspaikka?: Koodistokoodiviite<'erityisopetuksentoteutuspaikka', string>
  opiskeleeToimintaAlueittain: boolean
  loppu?: string
  erityisryhmässä?: boolean
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  alku?: string
}): ErityisenTuenPäätös => ({
  $class: 'fi.oph.koski.schema.ErityisenTuenPäätös',
  ...o
})

export const isErityisenTuenPäätös = (a: any): a is ErityisenTuenPäätös =>
  a?.$class === 'fi.oph.koski.schema.ErityisenTuenPäätös'
