import { LukionOppiaineidenOppimäärätKoodi2019 } from './LukionOppiaineidenOppimaaratKoodi2019'

/**
 * LukionOppiaineidenOppimäärät2019
 *
 * @see `fi.oph.koski.schema.LukionOppiaineidenOppimäärät2019`
 */
export type LukionOppiaineidenOppimäärät2019 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineidenOppimäärät2019'
  tunniste: LukionOppiaineidenOppimäärätKoodi2019
  perusteenDiaarinumero?: string
}

export const LukionOppiaineidenOppimäärät2019 = (
  o: {
    tunniste?: LukionOppiaineidenOppimäärätKoodi2019
    perusteenDiaarinumero?: string
  } = {}
): LukionOppiaineidenOppimäärät2019 => ({
  $class: 'fi.oph.koski.schema.LukionOppiaineidenOppimäärät2019',
  tunniste: LukionOppiaineidenOppimäärätKoodi2019({
    koodiarvo: 'lukionaineopinnot'
  }),
  ...o
})

export const isLukionOppiaineidenOppimäärät2019 = (
  a: any
): a is LukionOppiaineidenOppimäärät2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppiaineidenOppimäärät2019'
