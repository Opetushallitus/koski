import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ahvenanmaan perusopetuksen luokka-asteen (1-9) tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenLuokkaAste`
 */
export type AhvenanmaanPerusopetuksenLuokkaAste = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenLuokkaAste'
  tunniste: Koodistokoodiviite<'perusopetuksenluokkaaste' | 'koulutus', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const AhvenanmaanPerusopetuksenLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<'perusopetuksenluokkaaste' | 'koulutus', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}): AhvenanmaanPerusopetuksenLuokkaAste => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenLuokkaAste',
  ...o
})

AhvenanmaanPerusopetuksenLuokkaAste.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenLuokkaAste' as const

export const isAhvenanmaanPerusopetuksenLuokkaAste = (
  a: any
): a is AhvenanmaanPerusopetuksenLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenLuokkaAste'
