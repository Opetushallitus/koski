import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'

/**
 * AmmatilliseenTehtäväänValmistavaKoulutus
 *
 * @see `fi.oph.koski.schema.AmmatilliseenTehtäväänValmistavaKoulutus`
 */
export type AmmatilliseenTehtäväänValmistavaKoulutus = {
  $class: 'fi.oph.koski.schema.AmmatilliseenTehtäväänValmistavaKoulutus'
  tunniste: Koodistokoodiviite<
    'ammatilliseentehtavaanvalmistavakoulutus',
    string
  >
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus?: LocalizedString
}

export const AmmatilliseenTehtäväänValmistavaKoulutus = (o: {
  tunniste: Koodistokoodiviite<
    'ammatilliseentehtavaanvalmistavakoulutus',
    string
  >
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus?: LocalizedString
}): AmmatilliseenTehtäväänValmistavaKoulutus => ({
  $class: 'fi.oph.koski.schema.AmmatilliseenTehtäväänValmistavaKoulutus',
  ...o
})

AmmatilliseenTehtäväänValmistavaKoulutus.className =
  'fi.oph.koski.schema.AmmatilliseenTehtäväänValmistavaKoulutus' as const

export const isAmmatilliseenTehtäväänValmistavaKoulutus = (
  a: any
): a is AmmatilliseenTehtäväänValmistavaKoulutus =>
  a?.$class === 'fi.oph.koski.schema.AmmatilliseenTehtäväänValmistavaKoulutus'
