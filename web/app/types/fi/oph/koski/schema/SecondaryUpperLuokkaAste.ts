import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SecondaryUpperLuokkaAste
 *
 * @see `fi.oph.koski.schema.SecondaryUpperLuokkaAste`
 */
export type SecondaryUpperLuokkaAste = {
  $class: 'fi.oph.koski.schema.SecondaryUpperLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S6' | 'S7'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export const SecondaryUpperLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S6' | 'S7'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}): SecondaryUpperLuokkaAste => ({
  $class: 'fi.oph.koski.schema.SecondaryUpperLuokkaAste',
  ...o
})

export const isSecondaryUpperLuokkaAste = (
  a: any
): a is SecondaryUpperLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.SecondaryUpperLuokkaAste'
