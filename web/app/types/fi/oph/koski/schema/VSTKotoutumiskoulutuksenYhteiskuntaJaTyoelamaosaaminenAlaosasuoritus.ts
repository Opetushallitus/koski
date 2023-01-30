import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 } from './VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaamisenAlasuorituksenKoulutusmoduuli2022'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen osasuorituksen alaosasuoritus
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus`
 */
export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
  {
    $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus'
    >
    koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus'
    >
    koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus => ({
    $class:
      'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus' as const

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus'
