import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * ArkkitehtuurinOpintotaso
 *
 * @see `fi.oph.koski.schema.ArkkitehtuurinOpintotaso`
 */
export type ArkkitehtuurinOpintotaso = {
  $class: 'fi.oph.koski.schema.ArkkitehtuurinOpintotaso'
  taiteenala: Koodistokoodiviite<
    'taiteenperusopetustaiteenala',
    'arkkitehtuuri'
  >
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const ArkkitehtuurinOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<
      'taiteenperusopetustaiteenala',
      'arkkitehtuuri'
    >
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): ArkkitehtuurinOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'arkkitehtuuri',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.ArkkitehtuurinOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isArkkitehtuurinOpintotaso = (
  a: any
): a is ArkkitehtuurinOpintotaso => a?.$class === 'ArkkitehtuurinOpintotaso'
