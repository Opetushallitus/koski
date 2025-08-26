import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ammatillinen tutkinnon osa/osia useasta tutkinnosta
 *
 * @see `fi.oph.koski.schema.AmmatillinenOsaTaiOsiaUseastaTutkinnosta`
 */
export type AmmatillinenOsaTaiOsiaUseastaTutkinnosta = {
  $class: 'fi.oph.koski.schema.AmmatillinenOsaTaiOsiaUseastaTutkinnosta'
  tunniste: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainenuseastatutkinnosta'
  >
}

export const AmmatillinenOsaTaiOsiaUseastaTutkinnosta = (
  o: {
    tunniste?: Koodistokoodiviite<
      'suorituksentyyppi',
      'ammatillinentutkintoosittainenuseastatutkinnosta'
    >
  } = {}
): AmmatillinenOsaTaiOsiaUseastaTutkinnosta => ({
  $class: 'fi.oph.koski.schema.AmmatillinenOsaTaiOsiaUseastaTutkinnosta',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'ammatillinentutkintoosittainenuseastatutkinnosta',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

AmmatillinenOsaTaiOsiaUseastaTutkinnosta.className =
  'fi.oph.koski.schema.AmmatillinenOsaTaiOsiaUseastaTutkinnosta' as const

export const isAmmatillinenOsaTaiOsiaUseastaTutkinnosta = (
  a: any
): a is AmmatillinenOsaTaiOsiaUseastaTutkinnosta =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenOsaTaiOsiaUseastaTutkinnosta'
