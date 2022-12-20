import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * TeatteritaiteenOpintotaso
 *
 * @see `fi.oph.koski.schema.TeatteritaiteenOpintotaso`
 */
export type TeatteritaiteenOpintotaso = {
  $class: 'fi.oph.koski.schema.TeatteritaiteenOpintotaso'
  taiteenala: Koodistokoodiviite<
    'taiteenperusopetustaiteenala',
    'teatteritaide'
  >
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const TeatteritaiteenOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<
      'taiteenperusopetustaiteenala',
      'teatteritaide'
    >
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): TeatteritaiteenOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'teatteritaide',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.TeatteritaiteenOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isTeatteritaiteenOpintotaso = (
  a: any
): a is TeatteritaiteenOpintotaso =>
  a?.$class === 'fi.oph.koski.schema.TeatteritaiteenOpintotaso'
