import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022`
 */
export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =
  {
    $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022'
    tunniste: Koodistokoodiviite<
      'vstkoto2022yhteiskuntajatyoosaamiskoulutus',
      string
    >
    laajuus?: LaajuusOpintopisteissä
  }

export const VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =
  (o: {
    tunniste: Koodistokoodiviite<
      'vstkoto2022yhteiskuntajatyoosaamiskoulutus',
      string
    >
    laajuus?: LaajuusOpintopisteissä
  }): VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 => ({
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022',
    ...o
  })

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =>
    a?.$class ===
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022'
