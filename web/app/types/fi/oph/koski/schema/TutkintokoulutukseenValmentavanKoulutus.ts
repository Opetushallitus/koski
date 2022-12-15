import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Tutkintokoulutukseen valmentavan koulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutus`
 */
export type TutkintokoulutukseenValmentavanKoulutus = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999908'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusViikoissa
}

export const TutkintokoulutukseenValmentavanKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999908'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    laajuus?: LaajuusViikoissa
  } = {}
): TutkintokoulutukseenValmentavanKoulutus => ({
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999908',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isTutkintokoulutukseenValmentavanKoulutus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutus =>
  a?.$class === 'TutkintokoulutukseenValmentavanKoulutus'
