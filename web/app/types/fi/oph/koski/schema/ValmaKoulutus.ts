import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA) tunnistetiedot
 *
 * @see `fi.oph.koski.schema.ValmaKoulutus`
 */
export type ValmaKoulutus = {
  $class: 'fi.oph.koski.schema.ValmaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999901'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOsaamispisteissä
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const ValmaKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999901'>
    perusteenDiaarinumero?: string
    laajuus?: LaajuusOsaamispisteissä
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): ValmaKoulutus => ({
  $class: 'fi.oph.koski.schema.ValmaKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999901',
    koodistoUri: 'koulutus'
  }),
  ...o
})

ValmaKoulutus.className = 'fi.oph.koski.schema.ValmaKoulutus' as const

export const isValmaKoulutus = (a: any): a is ValmaKoulutus =>
  a?.$class === 'fi.oph.koski.schema.ValmaKoulutus'
