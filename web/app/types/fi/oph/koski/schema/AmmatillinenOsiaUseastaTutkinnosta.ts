import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ammatillisen tutkinnon osia useasta tutkinnosta
 *
 * @see `fi.oph.koski.schema.AmmatillinenOsiaUseastaTutkinnosta`
 */
export type AmmatillinenOsiaUseastaTutkinnosta = {
  $class: 'fi.oph.koski.schema.AmmatillinenOsiaUseastaTutkinnosta'
  tunniste: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainenuseastatutkinnosta'
  >
}

export const AmmatillinenOsiaUseastaTutkinnosta = (
  o: {
    tunniste?: Koodistokoodiviite<
      'suorituksentyyppi',
      'ammatillinentutkintoosittainenuseastatutkinnosta'
    >
  } = {}
): AmmatillinenOsiaUseastaTutkinnosta => ({
  $class: 'fi.oph.koski.schema.AmmatillinenOsiaUseastaTutkinnosta',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'ammatillinentutkintoosittainenuseastatutkinnosta',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

AmmatillinenOsiaUseastaTutkinnosta.className =
  'fi.oph.koski.schema.AmmatillinenOsiaUseastaTutkinnosta' as const

export const isAmmatillinenOsiaUseastaTutkinnosta = (
  a: any
): a is AmmatillinenOsiaUseastaTutkinnosta =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenOsiaUseastaTutkinnosta'
