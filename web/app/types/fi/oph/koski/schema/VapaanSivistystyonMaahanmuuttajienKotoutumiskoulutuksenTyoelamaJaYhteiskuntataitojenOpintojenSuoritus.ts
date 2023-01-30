import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus } from './VapaanSivistystyonMaahanmuuttajienKuntoutuskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenOsasuoritus'

/**
 * VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus'
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus>
  }

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  (
    o: {
      arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
      >
      tila?: Koodistokoodiviite<'suorituksentila', string>
      koulutusmoduuli?: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli
      osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus>
    } = {}
  ): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli(
        {
          tunniste: Koodistokoodiviite({
            koodiarvo:
              'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus',
            koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
          })
        }
      ),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus',
    ...o
  })

VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus.className =
  'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus' as const

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus'
