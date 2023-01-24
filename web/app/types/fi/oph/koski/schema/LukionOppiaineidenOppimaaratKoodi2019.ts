/**
 * Koodi, jota käytetään lukion oppiaineiden oppimäärien ryhmittelyssä 2019.
 *
 * @see `fi.oph.koski.schema.LukionOppiaineidenOppimäärätKoodi2019`
 */
export type LukionOppiaineidenOppimäärätKoodi2019 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineidenOppimäärätKoodi2019'
  koodiarvo: string
}

export const LukionOppiaineidenOppimäärätKoodi2019 = (
  o: {
    koodiarvo?: string
  } = {}
): LukionOppiaineidenOppimäärätKoodi2019 => ({
  $class: 'fi.oph.koski.schema.LukionOppiaineidenOppimäärätKoodi2019',
  koodiarvo: 'lukionaineopinnot',
  ...o
})

LukionOppiaineidenOppimäärätKoodi2019.className =
  'fi.oph.koski.schema.LukionOppiaineidenOppimäärätKoodi2019' as const

export const isLukionOppiaineidenOppimäärätKoodi2019 = (
  a: any
): a is LukionOppiaineidenOppimäärätKoodi2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppiaineidenOppimäärätKoodi2019'
