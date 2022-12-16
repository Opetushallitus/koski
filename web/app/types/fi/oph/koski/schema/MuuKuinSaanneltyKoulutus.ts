import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusTunneissa } from './LaajuusTunneissa'

/**
 * MuuKuinSäänneltyKoulutus
 *
 * @see `fi.oph.koski.schema.MuuKuinSäänneltyKoulutus`
 */
export type MuuKuinSäänneltyKoulutus = {
  $class: 'fi.oph.koski.schema.MuuKuinSäänneltyKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999951'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusTunneissa
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export const MuuKuinSäänneltyKoulutus = (o: {
  tunniste?: Koodistokoodiviite<'koulutus', '999951'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusTunneissa
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}): MuuKuinSäänneltyKoulutus => ({
  $class: 'fi.oph.koski.schema.MuuKuinSäänneltyKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999951',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isMuuKuinSäänneltyKoulutus = (
  a: any
): a is MuuKuinSäänneltyKoulutus => a?.$class === 'MuuKuinSäänneltyKoulutus'
