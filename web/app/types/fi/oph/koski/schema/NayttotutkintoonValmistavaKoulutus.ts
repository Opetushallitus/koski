import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Näyttötutkintoon valmistavan koulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NäyttötutkintoonValmistavaKoulutus`
 */
export type NäyttötutkintoonValmistavaKoulutus = {
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999904'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const NäyttötutkintoonValmistavaKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999904'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): NäyttötutkintoonValmistavaKoulutus => ({
  $class: 'fi.oph.koski.schema.NäyttötutkintoonValmistavaKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999904',
    koodistoUri: 'koulutus'
  }),
  ...o
})

NäyttötutkintoonValmistavaKoulutus.className =
  'fi.oph.koski.schema.NäyttötutkintoonValmistavaKoulutus' as const

export const isNäyttötutkintoonValmistavaKoulutus = (
  a: any
): a is NäyttötutkintoonValmistavaKoulutus =>
  a?.$class === 'fi.oph.koski.schema.NäyttötutkintoonValmistavaKoulutus'
