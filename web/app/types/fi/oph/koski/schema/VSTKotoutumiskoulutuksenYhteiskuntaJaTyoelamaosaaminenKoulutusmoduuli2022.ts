import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022`
 */
export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =
  {
    $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022'
    tunniste: Koodistokoodiviite<
      'vstkoto2022kokonaisuus',
      'yhteiskuntajatyoelamaosaaminen'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export const VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =
  (
    o: {
      tunniste?: Koodistokoodiviite<
        'vstkoto2022kokonaisuus',
        'yhteiskuntajatyoelamaosaaminen'
      >
      laajuus?: LaajuusOpintopisteissä
    } = {}
  ): VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 => ({
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022',
    tunniste: Koodistokoodiviite({
      koodiarvo: 'yhteiskuntajatyoelamaosaaminen',
      koodistoUri: 'vstkoto2022kokonaisuus'
    }),
    ...o
  })

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =>
    a?.$class ===
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022'
