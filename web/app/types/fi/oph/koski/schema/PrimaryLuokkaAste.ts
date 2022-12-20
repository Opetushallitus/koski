import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PrimaryLuokkaAste
 *
 * @see `fi.oph.koski.schema.PrimaryLuokkaAste`
 */
export type PrimaryLuokkaAste = {
  $class: 'fi.oph.koski.schema.PrimaryLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'P1' | 'P2' | 'P3' | 'P4' | 'P5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export const PrimaryLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'P1' | 'P2' | 'P3' | 'P4' | 'P5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}): PrimaryLuokkaAste => ({
  $class: 'fi.oph.koski.schema.PrimaryLuokkaAste',
  ...o
})

export const isPrimaryLuokkaAste = (a: any): a is PrimaryLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.PrimaryLuokkaAste'
