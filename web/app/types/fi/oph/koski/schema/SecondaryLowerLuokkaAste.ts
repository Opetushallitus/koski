import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SecondaryLowerLuokkaAste
 *
 * @see `fi.oph.koski.schema.SecondaryLowerLuokkaAste`
 */
export type SecondaryLowerLuokkaAste = {
  $class: 'fi.oph.koski.schema.SecondaryLowerLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S1' | 'S2' | 'S3' | 'S4' | 'S5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export const SecondaryLowerLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S1' | 'S2' | 'S3' | 'S4' | 'S5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}): SecondaryLowerLuokkaAste => ({
  $class: 'fi.oph.koski.schema.SecondaryLowerLuokkaAste',
  ...o
})

export const isSecondaryLowerLuokkaAste = (
  a: any
): a is SecondaryLowerLuokkaAste => a?.$class === 'SecondaryLowerLuokkaAste'
