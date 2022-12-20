import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Sanallisessa arvioinnissa suorituksen hyväksymisen ilmaisuun käytetään koodiarvoja S (suoritettu), H (hylätty) ja O (osallistunut). Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään
 *
 * @see `fi.oph.koski.schema.SanallinenPerusopetuksenOppiaineenArviointi`
 */
export type SanallinenPerusopetuksenOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.SanallinenPerusopetuksenOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}

export const SanallinenPerusopetuksenOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}): SanallinenPerusopetuksenOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.SanallinenPerusopetuksenOppiaineenArviointi',
  ...o
})

export const isSanallinenPerusopetuksenOppiaineenArviointi = (
  a: any
): a is SanallinenPerusopetuksenOppiaineenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.SanallinenPerusopetuksenOppiaineenArviointi'
