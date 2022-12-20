import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Aikuisten perusopetuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetus`
 */
export type AikuistenPerusopetus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const AikuistenPerusopetus = (
  o: {
    perusteenDiaarinumero?: string
    tunniste?: Koodistokoodiviite<'koulutus', '201101'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): AikuistenPerusopetus => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '201101',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isAikuistenPerusopetus = (a: any): a is AikuistenPerusopetus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetus'
