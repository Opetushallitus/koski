import { LocalizedString } from './LocalizedString'

/**
 * Paikallinen, koulutustoimijan oma kooditus. Käytetään kansallisen koodiston puuttuessa
 *
 * @see `fi.oph.koski.schema.KorkeakoulunPaikallinenArvosana`
 */
export type KorkeakoulunPaikallinenArvosana = {
  $class: 'fi.oph.koski.schema.KorkeakoulunPaikallinenArvosana'
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}

export const KorkeakoulunPaikallinenArvosana = (o: {
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}): KorkeakoulunPaikallinenArvosana => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunPaikallinenArvosana',
  ...o
})

KorkeakoulunPaikallinenArvosana.className =
  'fi.oph.koski.schema.KorkeakoulunPaikallinenArvosana' as const

export const isKorkeakoulunPaikallinenArvosana = (
  a: any
): a is KorkeakoulunPaikallinenArvosana =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunPaikallinenArvosana'
