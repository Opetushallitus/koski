import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Laajuus } from './Laajuus'

/**
 * Muun korkeakoulun opinnon tunnistetiedot
 *
 * @see `fi.oph.koski.schema.MuuKorkeakoulunOpinto`
 */
export type MuuKorkeakoulunOpinto = {
  $class: 'fi.oph.koski.schema.MuuKorkeakoulunOpinto'
  tunniste: Koodistokoodiviite<'virtaopiskeluoikeudentyyppi', string>
  nimi: LocalizedString
  laajuus?: Laajuus
}

export const MuuKorkeakoulunOpinto = (o: {
  tunniste: Koodistokoodiviite<'virtaopiskeluoikeudentyyppi', string>
  nimi: LocalizedString
  laajuus?: Laajuus
}): MuuKorkeakoulunOpinto => ({
  $class: 'fi.oph.koski.schema.MuuKorkeakoulunOpinto',
  ...o
})

export const isMuuKorkeakoulunOpinto = (a: any): a is MuuKorkeakoulunOpinto =>
  a?.$class === 'MuuKorkeakoulunOpinto'