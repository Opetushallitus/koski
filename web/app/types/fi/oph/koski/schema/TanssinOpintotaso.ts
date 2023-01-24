import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * TanssinOpintotaso
 *
 * @see `fi.oph.koski.schema.TanssinOpintotaso`
 */
export type TanssinOpintotaso = {
  $class: 'fi.oph.koski.schema.TanssinOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'tanssi'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const TanssinOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'tanssi'>
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): TanssinOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'tanssi',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.TanssinOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

TanssinOpintotaso.className = 'fi.oph.koski.schema.TanssinOpintotaso' as const

export const isTanssinOpintotaso = (a: any): a is TanssinOpintotaso =>
  a?.$class === 'fi.oph.koski.schema.TanssinOpintotaso'
