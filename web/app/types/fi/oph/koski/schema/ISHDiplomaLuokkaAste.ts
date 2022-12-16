import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ISHDiplomaLuokkaAste
 *
 * @see `fi.oph.koski.schema.ISHDiplomaLuokkaAste`
 */
export type ISHDiplomaLuokkaAste = {
  $class: 'fi.oph.koski.schema.ISHDiplomaLuokkaAste'
  diplomaType: Koodistokoodiviite<'internationalschooldiplomatype', 'ish'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}

export const ISHDiplomaLuokkaAste = (o: {
  diplomaType?: Koodistokoodiviite<'internationalschooldiplomatype', 'ish'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}): ISHDiplomaLuokkaAste => ({
  $class: 'fi.oph.koski.schema.ISHDiplomaLuokkaAste',
  diplomaType: Koodistokoodiviite({
    koodiarvo: 'ish',
    koodistoUri: 'internationalschooldiplomatype'
  }),
  ...o
})

export const isISHDiplomaLuokkaAste = (a: any): a is ISHDiplomaLuokkaAste =>
  a?.$class === 'ISHDiplomaLuokkaAste'
