import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * KäsityönOpintotaso
 *
 * @see `fi.oph.koski.schema.KäsityönOpintotaso`
 */
export type KäsityönOpintotaso = {
  $class: 'fi.oph.koski.schema.KäsityönOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'kasityo'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const KäsityönOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'kasityo'>
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): KäsityönOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'kasityo',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.KäsityönOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isKäsityönOpintotaso = (a: any): a is KäsityönOpintotaso =>
  a?.$class === 'KäsityönOpintotaso'
