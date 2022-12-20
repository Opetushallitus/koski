import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetukseen valmistavan opetuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavaOpetus`
 */
export type PerusopetukseenValmistavaOpetus = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavaOpetus'
  tunniste: Koodistokoodiviite<'koulutus', '999905'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const PerusopetukseenValmistavaOpetus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999905'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): PerusopetukseenValmistavaOpetus => ({
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavaOpetus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999905',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isPerusopetukseenValmistavaOpetus = (
  a: any
): a is PerusopetukseenValmistavaOpetus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetukseenValmistavaOpetus'
