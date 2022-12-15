import {
  LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa,
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa
} from './LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa'
import {
  LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019,
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019
} from './LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019'
import {
  LukioonValmistavanKoulutuksenOppiaineenSuoritus,
  isLukioonValmistavanKoulutuksenOppiaineenSuoritus
} from './LukioonValmistavanKoulutuksenOppiaineenSuoritus'

/**
 * LukioonValmistavanKoulutuksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenOsasuoritus`
 */
export type LukioonValmistavanKoulutuksenOsasuoritus =
  | LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa
  | LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019
  | LukioonValmistavanKoulutuksenOppiaineenSuoritus

export const isLukioonValmistavanKoulutuksenOsasuoritus = (
  a: any
): a is LukioonValmistavanKoulutuksenOsasuoritus =>
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(a) ||
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019(a) ||
  isLukioonValmistavanKoulutuksenOppiaineenSuoritus(a)
