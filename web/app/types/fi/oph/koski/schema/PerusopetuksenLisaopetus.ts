import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetuksen lisäopetuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetus`
 */
export type PerusopetuksenLisäopetus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetus'
  tunniste: Koodistokoodiviite<'koulutus', '020075'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const PerusopetuksenLisäopetus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '020075'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): PerusopetuksenLisäopetus => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '020075',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isPerusopetuksenLisäopetus = (
  a: any
): a is PerusopetuksenLisäopetus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenLisäopetus'
