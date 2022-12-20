import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) tunnistetiedot
 *
 * @see `fi.oph.koski.schema.TelmaKoulutus`
 */
export type TelmaKoulutus = {
  $class: 'fi.oph.koski.schema.TelmaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999903'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOsaamispisteissä
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const TelmaKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999903'>
    perusteenDiaarinumero?: string
    laajuus?: LaajuusOsaamispisteissä
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): TelmaKoulutus => ({
  $class: 'fi.oph.koski.schema.TelmaKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999903',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isTelmaKoulutus = (a: any): a is TelmaKoulutus =>
  a?.$class === 'fi.oph.koski.schema.TelmaKoulutus'
