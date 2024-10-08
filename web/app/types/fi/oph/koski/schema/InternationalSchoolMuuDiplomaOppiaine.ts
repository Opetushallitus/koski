import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * InternationalSchoolMuuDiplomaOppiaine
 *
 * @see `fi.oph.koski.schema.InternationalSchoolMuuDiplomaOppiaine`
 */
export type InternationalSchoolMuuDiplomaOppiaine = {
  $class: 'fi.oph.koski.schema.InternationalSchoolMuuDiplomaOppiaine'
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    'F' | 'HSCM' | 'ITGS' | 'MAA' | 'MAI' | 'INS'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export const InternationalSchoolMuuDiplomaOppiaine = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    'F' | 'HSCM' | 'ITGS' | 'MAA' | 'MAI' | 'INS'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}): InternationalSchoolMuuDiplomaOppiaine => ({
  $class: 'fi.oph.koski.schema.InternationalSchoolMuuDiplomaOppiaine',
  ...o
})

InternationalSchoolMuuDiplomaOppiaine.className =
  'fi.oph.koski.schema.InternationalSchoolMuuDiplomaOppiaine' as const

export const isInternationalSchoolMuuDiplomaOppiaine = (
  a: any
): a is InternationalSchoolMuuDiplomaOppiaine =>
  a?.$class === 'fi.oph.koski.schema.InternationalSchoolMuuDiplomaOppiaine'
