import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Käyttäytymisen arviointi. Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään
 *
 * @see `fi.oph.koski.schema.PerusopetuksenKäyttäytymisenArviointi`
 */
export type PerusopetuksenKäyttäytymisenArviointi = {
  $class: 'fi.oph.koski.schema.PerusopetuksenKäyttäytymisenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}

export const PerusopetuksenKäyttäytymisenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}): PerusopetuksenKäyttäytymisenArviointi => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenKäyttäytymisenArviointi',
  ...o
})

PerusopetuksenKäyttäytymisenArviointi.className =
  'fi.oph.koski.schema.PerusopetuksenKäyttäytymisenArviointi' as const

export const isPerusopetuksenKäyttäytymisenArviointi = (
  a: any
): a is PerusopetuksenKäyttäytymisenArviointi =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenKäyttäytymisenArviointi'
