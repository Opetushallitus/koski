import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from './VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 } from './VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus } from './VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenAlaosasuoritus'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuoritus
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen yhteiskunta- ja työelämäosaamisen osasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022`
 */
export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
  {
    $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022'
    arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022
    osasuoritukset?: Array<VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus>
  }

export const VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
  (
    o: {
      arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
      >
      tila?: Koodistokoodiviite<'suorituksentila', string>
      koulutusmoduuli?: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022
      osasuoritukset?: Array<VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus>
    } = {}
  ): VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022(
        {
          tunniste: Koodistokoodiviite({
            koodiarvo: 'yhteiskuntajatyoelamaosaaminen',
            koodistoUri: 'vstkoto2022kokonaisuus'
          })
        }
      ),
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022',
    ...o
  })

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =>
    a?.$class ===
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022'
