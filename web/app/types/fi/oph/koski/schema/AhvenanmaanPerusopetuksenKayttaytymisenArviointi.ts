import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Käyttäytymisen (Ansvar och samarbete) arviointi.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenKäyttäytymisenArviointi`
 */
export type AhvenanmaanPerusopetuksenKäyttäytymisenArviointi = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenKäyttäytymisenArviointi'
  arvosana: Koodistokoodiviite<
    'ahvenanmaanarviointiasteikkoyleissivistava',
    'G' | 'D' | 'U'
  >
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}

export const AhvenanmaanPerusopetuksenKäyttäytymisenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'ahvenanmaanarviointiasteikkoyleissivistava',
    'G' | 'D' | 'U'
  >
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}): AhvenanmaanPerusopetuksenKäyttäytymisenArviointi => ({
  $class:
    'fi.oph.koski.schema.AhvenanmaanPerusopetuksenKäyttäytymisenArviointi',
  ...o
})

AhvenanmaanPerusopetuksenKäyttäytymisenArviointi.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenKäyttäytymisenArviointi' as const

export const isAhvenanmaanPerusopetuksenKäyttäytymisenArviointi = (
  a: any
): a is AhvenanmaanPerusopetuksenKäyttäytymisenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenKäyttäytymisenArviointi'
