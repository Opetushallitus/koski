import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBDiplomaLuokkaAste
 *
 * @see `fi.oph.koski.schema.IBDiplomaLuokkaAste`
 */
export type IBDiplomaLuokkaAste = {
  $class: 'fi.oph.koski.schema.IBDiplomaLuokkaAste'
  diplomaType: Koodistokoodiviite<'internationalschooldiplomatype', 'ib'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}

export const IBDiplomaLuokkaAste = (o: {
  diplomaType?: Koodistokoodiviite<'internationalschooldiplomatype', 'ib'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}): IBDiplomaLuokkaAste => ({
  $class: 'fi.oph.koski.schema.IBDiplomaLuokkaAste',
  diplomaType: Koodistokoodiviite({
    koodiarvo: 'ib',
    koodistoUri: 'internationalschooldiplomatype'
  }),
  ...o
})

IBDiplomaLuokkaAste.className =
  'fi.oph.koski.schema.IBDiplomaLuokkaAste' as const

export const isIBDiplomaLuokkaAste = (a: any): a is IBDiplomaLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomaLuokkaAste'
