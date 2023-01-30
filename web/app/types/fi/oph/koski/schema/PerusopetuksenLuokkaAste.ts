import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetuksen luokka-asteen (1-9) tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLuokkaAste`
 */
export type PerusopetuksenLuokkaAste = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLuokkaAste'
  tunniste: Koodistokoodiviite<'perusopetuksenluokkaaste' | 'koulutus', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const PerusopetuksenLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<'perusopetuksenluokkaaste' | 'koulutus', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}): PerusopetuksenLuokkaAste => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenLuokkaAste',
  ...o
})

PerusopetuksenLuokkaAste.className =
  'fi.oph.koski.schema.PerusopetuksenLuokkaAste' as const

export const isPerusopetuksenLuokkaAste = (
  a: any
): a is PerusopetuksenLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenLuokkaAste'
