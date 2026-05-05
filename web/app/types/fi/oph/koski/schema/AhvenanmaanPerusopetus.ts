import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ahvenanmaan perusopetuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetus`
 */
export type AhvenanmaanPerusopetus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const AhvenanmaanPerusopetus = (
  o: {
    perusteenDiaarinumero?: string
    tunniste?: Koodistokoodiviite<'koulutus', '201101'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): AhvenanmaanPerusopetus => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '201101',
    koodistoUri: 'koulutus'
  }),
  ...o
})

AhvenanmaanPerusopetus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetus' as const

export const isAhvenanmaanPerusopetus = (a: any): a is AhvenanmaanPerusopetus =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanPerusopetus'
