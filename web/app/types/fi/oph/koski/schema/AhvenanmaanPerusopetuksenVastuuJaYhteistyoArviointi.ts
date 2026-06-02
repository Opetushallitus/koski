import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Vastuu ja yhteistyö (Ansvar och samarbete) -arviointi. Sallittu arvo G (godkänd).
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi`
 */
export type AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi'
  arvosana: Koodistokoodiviite<
    'ahvenanmaanarviointiasteikkoyleissivistava',
    'G'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'ahvenanmaanarviointiasteikkoyleissivistava',
    'G'
  >
  päivä?: string
  hyväksytty?: boolean
}): AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi => ({
  $class:
    'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi',
  ...o
})

AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi' as const

export const isAhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi = (
  a: any
): a is AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi'
