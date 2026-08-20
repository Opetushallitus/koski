import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Koulutusala Virran luokituksen mukaan. Virrassa koodiarvo yksilöidään versio-attribuutilla, joten kullekin luokitukselle on oma kenttänsä.
 *
 * @see `fi.oph.koski.schema.KorkeakoulunKoulutusala`
 */
export type KorkeakoulunKoulutusala = {
  $class: 'fi.oph.koski.schema.KorkeakoulunKoulutusala'
  opintoala1995?: Koodistokoodiviite<'opintoalaoph1995', string>
  okmOhjausala?: Koodistokoodiviite<'okmohjauksenala', string>
  koulutusala2002?: Koodistokoodiviite<'koulutusalaoph2002', string>
  osuus?: number
}

export const KorkeakoulunKoulutusala = (
  o: {
    opintoala1995?: Koodistokoodiviite<'opintoalaoph1995', string>
    okmOhjausala?: Koodistokoodiviite<'okmohjauksenala', string>
    koulutusala2002?: Koodistokoodiviite<'koulutusalaoph2002', string>
    osuus?: number
  } = {}
): KorkeakoulunKoulutusala => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunKoulutusala',
  ...o
})

KorkeakoulunKoulutusala.className =
  'fi.oph.koski.schema.KorkeakoulunKoulutusala' as const

export const isKorkeakoulunKoulutusala = (
  a: any
): a is KorkeakoulunKoulutusala =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunKoulutusala'
