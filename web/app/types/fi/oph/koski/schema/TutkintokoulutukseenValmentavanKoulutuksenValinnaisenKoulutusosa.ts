import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Valinnaiset koulutuksen osat
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '104'>
  laajuus?: LaajuusViikoissa
}

export const TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =
  (
    o: {
      tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '104'>
      laajuus?: LaajuusViikoissa
    } = {}
  ): TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa',
    tunniste: Koodistokoodiviite({
      koodiarvo: '104',
      koodistoUri: 'koulutuksenosattuva'
    }),
    ...o
  })

TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa.className =
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa' as const

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =>
    a?.$class ===
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa'
