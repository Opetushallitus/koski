import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Työelämätaidot ja työpaikalla tapahtuva oppiminen
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen`
 */
export type TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen'
    tunniste: Koodistokoodiviite<'koulutuksenosattuva', '102'>
    laajuus?: LaajuusViikoissa
  }

export const TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =
  (
    o: {
      tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '102'>
      laajuus?: LaajuusViikoissa
    } = {}
  ): TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen',
    tunniste: Koodistokoodiviite({
      koodiarvo: '102',
      koodistoUri: 'koulutuksenosattuva'
    }),
    ...o
  })

export const isTutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =>
    a?.$class ===
    'TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen'
