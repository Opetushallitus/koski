import { VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 } from './VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen ohjauksen osasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenSuoritus2022`
 */
export type VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
  koulutusmoduuli: VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 = (
  o: {
    koulutusmoduuli?: VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  } = {}
): VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 => ({
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenSuoritus2022',
  koulutusmoduuli: VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'ohjaus',
      koodistoUri: 'vstkoto2022kokonaisuus'
    })
  }),
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
