import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * NurseryLuokkaAste
 *
 * @see `fi.oph.koski.schema.NurseryLuokkaAste`
 */
export type NurseryLuokkaAste = {
  $class: 'fi.oph.koski.schema.NurseryLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'N1' | 'N2'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}

export const NurseryLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'N1' | 'N2'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}): NurseryLuokkaAste => ({
  $class: 'fi.oph.koski.schema.NurseryLuokkaAste',
  ...o
})

NurseryLuokkaAste.className = 'fi.oph.koski.schema.NurseryLuokkaAste' as const

export const isNurseryLuokkaAste = (a: any): a is NurseryLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.NurseryLuokkaAste'
