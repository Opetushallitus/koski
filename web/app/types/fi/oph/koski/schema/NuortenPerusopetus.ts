import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Nuorten perusopetuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetus`
 */
export type NuortenPerusopetus = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const NuortenPerusopetus = (
  o: {
    perusteenDiaarinumero?: string
    tunniste?: Koodistokoodiviite<'koulutus', '201101'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): NuortenPerusopetus => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '201101',
    koodistoUri: 'koulutus'
  }),
  ...o
})

NuortenPerusopetus.className = 'fi.oph.koski.schema.NuortenPerusopetus' as const

export const isNuortenPerusopetus = (a: any): a is NuortenPerusopetus =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetus'
