import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Lukiokoulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionOppimäärä`
 */
export type LukionOppimäärä = {
  $class: 'fi.oph.koski.schema.LukionOppimäärä'
  tunniste: Koodistokoodiviite<'koulutus', '309902'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const LukionOppimäärä = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '309902'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): LukionOppimäärä => ({
  $class: 'fi.oph.koski.schema.LukionOppimäärä',
  tunniste: Koodistokoodiviite({
    koodiarvo: '309902',
    koodistoUri: 'koulutus'
  }),
  ...o
})

LukionOppimäärä.className = 'fi.oph.koski.schema.LukionOppimäärä' as const

export const isLukionOppimäärä = (a: any): a is LukionOppimäärä =>
  a?.$class === 'fi.oph.koski.schema.LukionOppimäärä'
