import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Aikuisten perusopetuksen alkuvaiheen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaihe`
 */
export type AikuistenPerusopetuksenAlkuvaihe = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaihe'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
}

export const AikuistenPerusopetuksenAlkuvaihe = (
  o: {
    perusteenDiaarinumero?: string
    tunniste?: Koodistokoodiviite<
      'suorituksentyyppi',
      'aikuistenperusopetuksenoppimaaranalkuvaihe'
    >
  } = {}
): AikuistenPerusopetuksenAlkuvaihe => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaihe',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenoppimaaranalkuvaihe',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isAikuistenPerusopetuksenAlkuvaihe = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaihe =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaihe'
