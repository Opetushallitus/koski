import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissäTaiKursseissa } from './LaajuusOpintopisteissaTaiKursseissa'

/**
 * Lukioon valmistavan koulutuksen (LUVA) tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukioonValmistavaKoulutus`
 */
export type LukioonValmistavaKoulutus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999906'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const LukioonValmistavaKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999906'>
    perusteenDiaarinumero?: string
    laajuus?: LaajuusOpintopisteissäTaiKursseissa
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): LukioonValmistavaKoulutus => ({
  $class: 'fi.oph.koski.schema.LukioonValmistavaKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999906',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isLukioonValmistavaKoulutus = (
  a: any
): a is LukioonValmistavaKoulutus =>
  a?.$class === 'fi.oph.koski.schema.LukioonValmistavaKoulutus'
