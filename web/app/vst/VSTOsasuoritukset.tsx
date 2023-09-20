import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import {
  defaultFinnishKuvaus,
  defaultLaajuusOpintopisteissa
} from './resolvers'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { MuuallaSuoritetutVapaanSivistystyönOpinnot } from '../types/fi/oph/koski/schema/MuuallaSuoritetutVapaanSivistystyonOpinnot'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenTyoelamaJakso'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataidot'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOsaamiskokonaisuus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönLukutaidonKokonaisuus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaidonKokonaisuus'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenAlaosasuoritus'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaamisenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'

export const createVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  (tunniste: PaikallinenKoodi) =>
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus({
      koulutusmoduuli:
        VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
          tunniste,
          kuvaus: defaultFinnishKuvaus,
          laajuus: defaultLaajuusOpintopisteissa
        })
    })

export const createOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  (tunniste: PaikallinenKoodi) =>
    OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus({
      koulutusmoduuli:
        OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus({
          kuvaus: defaultFinnishKuvaus,
          laajuus: defaultLaajuusOpintopisteissa,
          tunniste
        })
    })

export const createMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
  (tunniste: Koodistokoodiviite<'vstmuuallasuoritetutopinnot', string>) =>
    MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
      {
        koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot({
          tunniste,
          kuvaus: defaultFinnishKuvaus,
          laajuus: defaultLaajuusOpintopisteissa
        })
      }
    )

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso =
  (tunniste: PaikallinenKoodi) =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
      {
        koulutusmoduuli:
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
            {
              kuvaus: defaultFinnishKuvaus,
              tunniste
            }
          )
      }
    )

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot =
  (tunniste: PaikallinenKoodi) =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
      {
        koulutusmoduuli:
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
            {
              kuvaus: defaultFinnishKuvaus,
              tunniste
            }
          )
      }
    )

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  (tunniste: PaikallinenKoodi) =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
      {
        koulutusmoduuli:
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
            {
              tunniste,
              kuvaus: defaultFinnishKuvaus
            }
          )
      }
    )

export const createVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = (
  tunniste: PaikallinenKoodi
) =>
  VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
    koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
      tunniste,
      laajuus: LaajuusOpintopisteissä({ arvo: 1 })
    })
  })

export const createOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  (tunniste: Koodistokoodiviite<'vstosaamiskokonaisuus', string>) =>
    OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus({
      koulutusmoduuli:
        OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus({
          tunniste,
          laajuus: defaultLaajuusOpintopisteissa
        })
    })

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  () =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
      {
        koulutusmoduuli:
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
            {
              tunniste: Koodistokoodiviite({
                koodiarvo:
                  'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus',
                koodistoUri:
                  'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
              })
            }
          )
      }
    )

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  () =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
      {
        koulutusmoduuli:
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli(
            {
              tunniste: Koodistokoodiviite({
                koodiarvo:
                  'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus',
                koodistoUri:
                  'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
              })
            }
          )
      }
    )

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  () =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus({
      koulutusmoduuli:
        VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli(
          {
            tunniste: Koodistokoodiviite({
              koodiarvo:
                'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
              koodistoUri: 'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
            })
          }
        )
    })

export const createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =
  () =>
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
      {
        koulutusmoduuli:
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli(
            {
              tunniste: Koodistokoodiviite({
                koodiarvo:
                  'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus',
                koodistoUri:
                  'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
              })
            }
          )
      }
    )

export const createVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =
  (
    tunniste: Koodistokoodiviite<'vstkoto2022kielijaviestintakoulutus', string>
  ) =>
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus({
      koulutusmoduuli:
        VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022(
          {
            tunniste,
            laajuus: defaultLaajuusOpintopisteissa
          }
        )
    })

export const createVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus =
  (
    tunniste: Koodistokoodiviite<'vstlukutaitokoulutuksenkokonaisuus', string>
  ) =>
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
      koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
        tunniste,
        laajuus: defaultLaajuusOpintopisteissa
      })
    })

export const createVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
  (
    tunniste: Koodistokoodiviite<
      'vstkoto2022yhteiskuntajatyoosaamiskoulutus',
      string
    >
  ) =>
    VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus({
      koulutusmoduuli:
        VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022(
          {
            tunniste,
            laajuus: defaultLaajuusOpintopisteissa
          }
        )
    })

export const createVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = (
  tunniste: PaikallinenKoodi
) =>
  VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus({
    koulutusmoduuli:
      VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
        {
          tunniste,
          kuvaus: defaultFinnishKuvaus,
          laajuus: defaultLaajuusOpintopisteissa
        }
      )
  })
