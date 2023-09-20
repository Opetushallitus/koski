import React, { useCallback, useMemo } from 'react'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { FormField } from '../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import {
  ArvosanaView,
  ArvosanaEdit
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusView,
  LaajuusOpintopisteissäEdit
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  OsasuoritusTable,
  osasuoritusTestId,
  OsasuoritusRowData
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import {
  TaitotasoView,
  TaitotasoEdit
} from '../components-v2/opiskeluoikeus/TaitotasoField'
import {
  isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus,
  VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { lastElement } from '../util/optics'
import {
  createVstArviointi,
  defaultFinnishKuvaus,
  defaultLaajuusOpintopisteissa
} from './resolvers'
import {
  VSTOsasuoritus,
  VSTOsasuoritusOsasuorituksilla,
  isVSTOsasuoritusArvioinnilla,
  isVSTOsasuoritusJollaOsasuorituksia,
  isVSTKoulutusmoduuliKuvauksella,
  isTunnustettuVSTOsasuoritus,
  hasPäiväInArviointi
} from './typeguards'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import {
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022,
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { t } from '../i18n/i18n'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import {
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022,
  VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import {
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022,
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import {
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import {
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus,
  OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import {
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus,
  OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus,
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { isVapaanSivistystyönJotpaKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import {
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
  VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import {
  KuvausEdit,
  KuvausView
} from '../components-v2/opiskeluoikeus/KuvausField'
import {
  TunnustusEdit,
  TunnustusView
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataidot'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenTyoelamaJakso'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import { append } from 'ramda'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import {
  KehittyvänKielenTaitotasoEdit,
  KehittyvänKielenTaitotasoView
} from '../components-v2/opiskeluoikeus/KehittyvänKielenTaitotasoField'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import {
  OsasuorituksetExpandedState,
  SetOsasuoritusOpen
} from './../osasuoritus/hooks'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { MuuallaSuoritetutVapaanSivistystyönOpinnot } from '../types/fi/oph/koski/schema/MuuallaSuoritetutVapaanSivistystyonOpinnot'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOsaamiskokonaisuus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenKoulutusmoduuli'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenAlaosasuoritus'
import { VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaamisenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
import { VapaanSivistystyönLukutaidonKokonaisuus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaidonKokonaisuus'
import { VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenKoulutusmoduuli2022'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { usePreferences } from '../appstate/preferences'

type AddNewVSTOsasuoritusViewProps = {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönOpiskeluoikeus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  pathWithOsasuoritukset: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    | VSTOsasuoritus
    | VSTOsasuoritusOsasuorituksilla
    | VapaanSivistystyönPäätasonSuoritus
  >
  organisaatio: Oppilaitos | Koulutustoimija | undefined
}

export const AddNewVSTOsasuoritusView: React.FC<
  AddNewVSTOsasuoritusViewProps
> = (props) => {
  const data = useMemo(
    () => getValue(props.pathWithOsasuoritukset)(props.form.state),
    [props.form.state, props.pathWithOsasuoritukset]
  )
  const { pathWithOsasuoritukset, createOsasuoritus } = props

  const preferencesType =
    isVapaanSivistystyönJotpaKoulutuksenSuoritus(data) ||
    isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(data)
      ? 'vapaansivistystyonjotpakoulutuksenosasuoritus'
      : undefined

  const osasuoritukset =
    usePreferences<VapaanSivistystyönJotpaKoulutuksenOsasuoritus>(
      props.organisaatio?.oid,
      preferencesType
    )

  const storedOsasuoritustunnisteet = useMemo(
    () => osasuoritukset.preferences.map((p) => p.tunniste),
    [osasuoritukset.preferences]
  )

  const onKoodistoSelect = useCallback(
    (osasuoritus: VSTOsasuoritus, isNew = false) => {
      if (pathWithOsasuoritukset !== undefined) {
        createOsasuoritus(pathWithOsasuoritukset, osasuoritus)
        if (
          isNew &&
          isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
            osasuoritus
          )
        ) {
          osasuoritukset.store(
            osasuoritus.koulutusmoduuli.tunniste.koodiarvo,
            osasuoritus.koulutusmoduuli
          )
        }
      } else {
        console.warn('pathWithOsasuoritukset is undefined')
      }
    },
    [createOsasuoritus, pathWithOsasuoritukset, osasuoritukset]
  )
  const onRemoveKoodisto = useCallback(
    (_tunniste: Koodistokoodiviite<string, string>) => {
      console.log('TODO: onRemove')
    },
    []
  )
  const onRemovePaikallinenKoodisto = useCallback(
    (tunniste: PaikallinenKoodi) => {
      osasuoritukset.remove(tunniste.koodiarvo)
    },
    [osasuoritukset]
  )

  if (data === undefined) {
    console.warn('Data is undefined')
    return null
  }
  return (
    <ColumnRow indent={props.level + 1}>
      <Column span={10}>
        {isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(data) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste) =>
              onKoodistoSelect(
                VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
                  {
                    koulutusmoduuli:
                      VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(
                        {
                          tunniste,
                          kuvaus: defaultFinnishKuvaus,
                          laajuus: defaultLaajuusOpintopisteissa
                        }
                      )
                  }
                )
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
          />
        )}
        {isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää paikallinen opintokokonaisuus')}
            onSelect={(tunniste, isNew) =>
              onKoodistoSelect(
                OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
                  {
                    koulutusmoduuli:
                      OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
                        {
                          kuvaus: defaultFinnishKuvaus,
                          laajuus: defaultLaajuusOpintopisteissa,
                          tunniste
                        }
                      )
                  }
                )
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
          />
        )}
        {isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
          data
        ) && (
          <>
            <KoodistoSelect
              koodistoUri="vstmuuallasuoritetutopinnot"
              addNewText={t('Lisää muualla suoritettu opinto')}
              onSelect={(tunniste) =>
                onKoodistoSelect(
                  MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
                    {
                      koulutusmoduuli:
                        MuuallaSuoritetutVapaanSivistystyönOpinnot({
                          tunniste,
                          kuvaus: defaultFinnishKuvaus,
                          laajuus: defaultLaajuusOpintopisteissa
                        })
                    }
                  )
                )
              }
              onRemove={onRemoveKoodisto}
            />
            <PaikallinenOsasuoritusSelect
              addNewText={t('Lisää paikallinen opintokokonaisuus')}
              onSelect={(tunniste) =>
                onKoodistoSelect(
                  OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
                    {
                      koulutusmoduuli:
                        OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
                          {
                            kuvaus: defaultFinnishKuvaus,
                            laajuus: defaultLaajuusOpintopisteissa,
                            tunniste
                          }
                        )
                    }
                  )
                )
              }
              onRemove={onRemovePaikallinenKoodisto}
            />
          </>
        )}
        {isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
          data
        ) && (
          <>
            <PaikallinenOsasuoritusSelect
              addNewText={t('Lisää työelämäjakso')}
              labelText={t('Lisää työelämäjakso')}
              modalTitle={t('Lisää työelämäjakso')}
              namePlaceholder={t('Työelämäjakson nimi')}
              onSelect={(tunniste) =>
                onKoodistoSelect(
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
                )
              }
              onRemove={onRemovePaikallinenKoodisto}
            />
            <PaikallinenOsasuoritusSelect
              addNewText={t(
                'Lisää työelämä- ja yhteiskuntataidon opintokokonaisuus'
              )}
              labelText={t(
                'Lisää työelämä- ja yhteiskuntataidon opintokokonaisuus'
              )}
              modalTitle={t(
                'Lisää työelämä- ja yhteiskuntataidon opintokokonaisuus'
              )}
              namePlaceholder={t('Opintokokonaisuuden nimi')}
              onSelect={(tunniste) =>
                onKoodistoSelect(
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
                )
              }
              onRemove={onRemovePaikallinenKoodisto}
            />
          </>
        )}
        {isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää valinnaiset')}
            onSelect={(p) =>
              onKoodistoSelect(
                VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
                  {
                    koulutusmoduuli:
                      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
                        {
                          tunniste: p,
                          kuvaus: defaultFinnishKuvaus
                        }
                      )
                  }
                )
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
          />
        )}
        {(isVapaanSivistystyönJotpaKoulutuksenSuoritus(data) ||
          isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(data)) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste, isNew) =>
              onKoodistoSelect(
                VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
                  koulutusmoduuli:
                    VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
                      tunniste,
                      laajuus: LaajuusOpintopisteissä({ arvo: 1 })
                    })
                }),
                isNew
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
            tunnisteet={storedOsasuoritustunnisteet}
          />
        )}
        {isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
          data
        ) && (
          <>
            <KoodistoSelect
              koodistoUri="vstosaamiskokonaisuus"
              addNewText={t('Lisää osaamiskokonaisuus')}
              onSelect={(tunniste) => {
                onKoodistoSelect(
                  OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
                    {
                      koulutusmoduuli:
                        OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
                          {
                            tunniste,
                            laajuus: defaultLaajuusOpintopisteissa
                          }
                        )
                    }
                  )
                )
              }}
              onRemove={onRemoveKoodisto}
            />
            <KoodistoSelect
              koodistoUri="vstmuutopinnot"
              addNewText={t('Lisää suuntautumisopinto')}
              onSelect={(tunniste) => {
                onKoodistoSelect(
                  OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
                    {
                      koulutusmoduuli: {
                        $class:
                          'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot',
                        // @ts-expect-error Tyyppi kavennettu jo
                        tunniste
                      }
                    }
                  )
                )
              }}
              onRemove={onRemoveKoodisto}
            />
          </>
        )}
        {isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
          data
        ) && (
          <>
            {!(data.osasuoritukset || []).find(
              isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
            ) && (
              <RaisedButton
                onClick={() =>
                  props.createOsasuoritus(
                    props.pathWithOsasuoritukset,
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
                  )
                }
              >
                {t('Lisää suomen/ruotsin kielen ja viestintätaitojen osa-alue')}
              </RaisedButton>
            )}
            <Spacer />
            {!(data.osasuoritukset || []).find(
              isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
            ) && (
              <RaisedButton
                onClick={() =>
                  props.createOsasuoritus(
                    props.pathWithOsasuoritukset,
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
                  )
                }
              >
                {t('Lisää työelämän ja yhteiskuntataitojen opintojen osa-alue')}
              </RaisedButton>
            )}
            <Spacer />
            {!(data.osasuoritukset || []).find(
              isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus
            ) && (
              <RaisedButton
                onClick={() =>
                  props.createOsasuoritus(
                    props.pathWithOsasuoritukset,
                    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
                      {
                        koulutusmoduuli:
                          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli(
                            {
                              tunniste: Koodistokoodiviite({
                                koodiarvo:
                                  'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
                                koodistoUri:
                                  'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'
                              })
                            }
                          )
                      }
                    )
                  )
                }
              >
                {t('Lisää kotoutumiskoulutuksen ohjauksen osa-alue')}
              </RaisedButton>
            )}
            <Spacer />
            {!(data.osasuoritukset || []).find(
              isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus
            ) && (
              <RaisedButton
                onClick={() =>
                  props.createOsasuoritus(
                    props.pathWithOsasuoritukset,
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
                  )
                }
              >
                {t('Lisää valinnaisten opintojen osa-alue')}
              </RaisedButton>
            )}
          </>
        )}
        {isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste) =>
              onKoodistoSelect(
                VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
                  {
                    koulutusmoduuli:
                      VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(
                        {
                          kuvaus: defaultFinnishKuvaus,
                          laajuus: defaultLaajuusOpintopisteissa,
                          tunniste
                        }
                      )
                  }
                )
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
          />
        )}
        {isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            koodistoUri="vstkoto2022kielijaviestintakoulutus"
            addNewText={t(
              'Lisää kieli- ja viestintäkoulutuksen alaosasuoritus'
            )}
            onSelect={(tunniste, _isNew) => {
              onKoodistoSelect(
                VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus({
                  koulutusmoduuli:
                    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022(
                      {
                        tunniste,
                        laajuus: defaultLaajuusOpintopisteissa
                      }
                    )
                })
              )
            }}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isVapaanSivistystyönLukutaitokoulutuksenSuoritus(data) && (
          <KoodistoSelect
            koodistoUri="vstlukutaitokoulutuksenkokonaisuus"
            addNewText={t('Lisää kokonaisuus')}
            onSelect={(tunniste, _isNew) => {
              onKoodistoSelect(
                VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
                  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
                    tunniste,
                    laajuus: defaultLaajuusOpintopisteissa
                  })
                })
              )
            }}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            koodistoUri="vstkoto2022kokonaisuus"
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste, _isNew) => {
              switch (tunniste.koodiarvo) {
                case 'kielijaviestintaosaaminen':
                  onKoodistoSelect(
                    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
                      {
                        koulutusmoduuli:
                          VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
                            {
                              // @ts-expect-error Tyyppi on jo tarkennettu
                              tunniste,
                              laajuus: defaultLaajuusOpintopisteissa
                            }
                          )
                      }
                    )
                  )
                  break
                case 'ohjaus':
                  onKoodistoSelect(
                    VSTKotoutumiskoulutuksenOhjauksenSuoritus2022({
                      koulutusmoduuli:
                        VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022({
                          // @ts-expect-error Tyyppi on jo tarkennettu
                          tunniste,
                          laajuus: defaultLaajuusOpintopisteissa
                        })
                    })
                  )
                  break
                case 'valinnaisetopinnot':
                  onKoodistoSelect(
                    VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
                      {
                        koulutusmoduuli:
                          VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022(
                            {
                              // @ts-expect-error Tyyppi on jo tarkennettu
                              tunniste,
                              laajuus: defaultLaajuusOpintopisteissa
                            }
                          )
                      }
                    )
                  )
                  break
                case 'yhteiskuntajatyoelamaosaaminen':
                  onKoodistoSelect(
                    VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
                      {
                        koulutusmoduuli:
                          VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022(
                            {
                              // @ts-expect-error Tyyppi on jo tarkennettu
                              tunniste,
                              laajuus: defaultLaajuusOpintopisteissa
                            }
                          )
                      }
                    )
                  )
                  break
                default:
                  console.error(`Unknown koodiarvo: ${tunniste.koodiarvo}`)
                  break
              }
            }}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            koodistoUri="vstkoto2022yhteiskuntajatyoosaamiskoulutus"
            addNewText={t(
              'Lisää yhteiskunta- ja työosaamiskoulutuksen alaosasuoritus'
            )}
            onSelect={(tunniste, _isNew) => {
              onKoodistoSelect(
                VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus(
                  {
                    koulutusmoduuli:
                      VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022(
                        {
                          tunniste,
                          laajuus: defaultLaajuusOpintopisteissa
                        }
                      )
                  }
                )
              )
            }}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste, _isNew) => {
              onKoodistoSelect(
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
              )
            }}
            onRemove={onRemovePaikallinenKoodisto}
          />
        )}
      </Column>
    </ColumnRow>
  )
}

type VSTOsasuoritusPropertiesProps = {
  osasuoritusIndex: number
  level: number
  setOpenStateHandler: SetOsasuoritusOpen
  osasuoritusOpenState: OsasuorituksetExpandedState
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<VapaanSivistystyönOpiskeluoikeus, VSTOsasuoritus>
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
  allOpen: boolean
}

interface OsasuoritusToTableRowParams {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönPäätasonSuoritus
  >
  suoritusIndex: number
  osasuoritusIndex: number
  // Tilanhallintaa modaalien availuun ja osasuoritusikkunan toimintaan
  osasuorituksetExpandedState: OsasuorituksetExpandedState
  setOsasuoritusOpen: SetOsasuoritusOpen
  allOsasuorituksetOpen: boolean
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTOsasuoritus
  ) => void
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form,
  level,
  createOsasuoritus,
  allOsasuorituksetOpen,
  osasuorituksetExpandedState,
  setOsasuoritusOpen
}: OsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Arvosana' | 'Taitotaso'
> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  const osasuoritusValue = getValue(osasuoritus)(form.state)
  const suoritusValue = getValue(suoritusPath.prop('osasuoritukset'))(
    form.state
  )
  const hasArvioinnit =
    suoritusValue &&
    (suoritusValue as any[]).some((v) => {
      return isVSTOsasuoritusArvioinnilla(v)
    })

  const columns = {
    Osasuoritus: (
      <FormField
        form={form}
        path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
        view={LocalizedTextView}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'nimi'
        )}
      />
    ),
    Laajuus: (
      <FormField
        form={form}
        path={osasuoritus.path('koulutusmoduuli.laajuus')}
        view={LaajuusView}
        edit={LaajuusOpintopisteissäEdit}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'laajuus'
        )}
      />
    ),
    Arvosana: (
      <FormField
        form={form}
        path={osasuoritus.path('arviointi')}
        view={ArvosanaView}
        edit={(arvosanaProps) => {
          if (osasuoritusValue === undefined) {
            return null
          }
          if (!isVSTOsasuoritusArvioinnilla(osasuoritusValue)) {
            return null
          }
          return (
            <ArvosanaEdit
              {...arvosanaProps}
              createArviointi={(arvosana) => {
                return createVstArviointi(osasuoritusValue)(arvosana)
              }}
            />
          )
        }}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'arvosana'
        )}
      />
    ),
    Taitotaso: (
      <FormField
        form={form}
        path={osasuoritus
          .guard(isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus)
          .prop('arviointi')}
        view={TaitotasoView}
        edit={(taitotasoProps) => {
          if (osasuoritusValue === undefined) {
            return null
          }
          return <TaitotasoEdit {...taitotasoProps} />
        }}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'arvosana'
        )}
      />
    )
  }

  const { Osasuoritus, Laajuus, Arvosana, Taitotaso } = columns

  const isExpandable =
    osasuoritusValue?.$class !==
    VSTKotoutumiskoulutuksenOhjauksenSuoritus2022.className

  const isLukutaitokoulutus =
    isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
      osasuoritusValue
    )

  const defaultColumns = { Osasuoritus, Laajuus }
  const columnsWithArvosana = { Osasuoritus, Laajuus, Arvosana }
  const columnsWithTaitotaso = { Osasuoritus, Laajuus, Arvosana, Taitotaso }

  // Tässä määritellään näytettävät sarakkeet
  const cols = hasArvioinnit
    ? isLukutaitokoulutus
      ? columnsWithTaitotaso
      : columnsWithArvosana
    : defaultColumns

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset').optional(),
    expandable: isExpandable,
    columns: cols,
    content: (
      <VSTOsasuoritusProperties
        level={level}
        osasuoritusIndex={osasuoritusIndex}
        osasuoritusOpenState={osasuorituksetExpandedState}
        setOpenStateHandler={setOsasuoritusOpen}
        toggleOsasuoritusOpen={setOsasuoritusOpen}
        allOpen={allOsasuorituksetOpen}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritus}
        createOsasuoritus={createOsasuoritus}
      />
    )
  }
}

export const VSTOsasuoritusProperties: React.FC<
  VSTOsasuoritusPropertiesProps
> = (props) => {
  const osasuoritusArvioinnillaPath = props.osasuoritusPath.guard(
    isVSTOsasuoritusArvioinnilla
  )
  const osasuoritusKuvauksellaPath = props.osasuoritusPath
    .prop('koulutusmoduuli')
    .guard(isVSTKoulutusmoduuliKuvauksella)
  const osasuoritusTunnustuksellaPath = props.osasuoritusPath.guard(
    isTunnustettuVSTOsasuoritus
  )
  const viimeisinArviointiPath = osasuoritusArvioinnillaPath
    .prop('arviointi')
    .optional()
    .compose(lastElement())

  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  const arvioitu =
    osasuoritus !== undefined &&
    isVSTOsasuoritusArvioinnilla(osasuoritus) &&
    osasuoritus?.arviointi !== undefined &&
    osasuoritus?.arviointi.length > 0

  return (
    <div>
      {arvioitu && (
        <OsasuoritusProperty label="Arviointi">
          <OsasuoritusSubproperty label="Arvosana">
            <FormField
              form={props.form}
              path={osasuoritusArvioinnillaPath.prop('arviointi').optional()}
              view={ArvosanaView}
              edit={(arvosanaProps) => {
                if (!isVSTOsasuoritusArvioinnilla(osasuoritus)) {
                  return <div>{'Ei arviointia'}</div>
                }
                return (
                  <ArvosanaEdit
                    {...arvosanaProps}
                    createArviointi={(arvosana) => {
                      return createVstArviointi(osasuoritus)(arvosana)
                    }}
                  />
                )
              }}
              testId={`vst.arvosana`}
            />
          </OsasuoritusSubproperty>
          <OsasuoritusSubproperty rowNumber={1} label="Päivämäärä">
            <FormField
              form={props.form}
              path={viimeisinArviointiPath
                .guard(hasPäiväInArviointi)
                .prop('päivä')}
              view={DateView}
              edit={DateEdit}
              testId={`vst.arvostelunPvm`}
            />
          </OsasuoritusSubproperty>
        </OsasuoritusProperty>
      )}
      {osasuoritus?.koulutusmoduuli !== undefined &&
        isVSTKoulutusmoduuliKuvauksella(osasuoritus?.koulutusmoduuli) && (
          <OsasuoritusProperty label="">
            <OsasuoritusSubproperty label="Kuvaus">
              <FormField
                form={props.form}
                path={osasuoritusKuvauksellaPath.prop('kuvaus')}
                view={KuvausView}
                edit={(kuvausProps) => {
                  return <KuvausEdit {...kuvausProps} />
                }}
                testId={`vst.kuvaus`}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
        )}
      {osasuoritus?.koulutusmoduuli !== undefined &&
        isTunnustettuVSTOsasuoritus(osasuoritus) && (
          <OsasuoritusProperty label="">
            <OsasuoritusSubproperty label="Tunnustettu">
              <FormField
                form={props.form}
                path={osasuoritusTunnustuksellaPath.prop('tunnustettu')}
                view={TunnustusView}
                edit={TunnustusEdit}
                editProps={{
                  tunnustusClass:
                    VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen.className
                }}
                testId={`vst.tunnustettu`}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
        )}
      {isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
        osasuoritus
      ) && (
        <>
          <OsasuoritusProperty label="">
            <OsasuoritusSubproperty label="Kuullun ymmärtämisen taitotaso">
              <FormField
                form={props.form}
                path={osasuoritusArvioinnillaPath
                  .guard(
                    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
                  )
                  .prop('arviointi')
                  .optional()
                  .compose(lastElement())
                  .prop('kuullunYmmärtämisenTaitotaso')}
                view={KehittyvänKielenTaitotasoView}
                edit={KehittyvänKielenTaitotasoEdit}
                testId={`vst.kuullunYmmartaminen`}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
          <OsasuoritusProperty label="">
            <OsasuoritusSubproperty label="Puhumisen taitotaso">
              <FormField
                form={props.form}
                path={osasuoritusArvioinnillaPath
                  .guard(
                    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
                  )
                  .prop('arviointi')
                  .optional()
                  .compose(lastElement())
                  .prop('puhumisenTaitotaso')}
                view={KehittyvänKielenTaitotasoView}
                edit={KehittyvänKielenTaitotasoEdit}
                testId={`vst.kuullunYmmartaminen`}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
          <OsasuoritusProperty label="">
            <OsasuoritusSubproperty label="Luetun ymmärtämisen taitotaso">
              <FormField
                form={props.form}
                path={osasuoritusArvioinnillaPath
                  .guard(
                    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
                  )
                  .prop('arviointi')
                  .optional()
                  .compose(lastElement())
                  .prop('luetunYmmärtämisenTaitotaso')}
                view={KehittyvänKielenTaitotasoView}
                edit={KehittyvänKielenTaitotasoEdit}
                testId={`vst.luetunYmmartaminen`}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
          <OsasuoritusProperty label="">
            <OsasuoritusSubproperty label="Kirjoittamisen taitotaso">
              <FormField
                form={props.form}
                path={osasuoritusArvioinnillaPath
                  .guard(
                    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
                  )
                  .prop('arviointi')
                  .optional()
                  .compose(lastElement())
                  .prop('kirjoittamisenTaitotaso')}
                view={KehittyvänKielenTaitotasoView}
                edit={KehittyvänKielenTaitotasoEdit}
                testId={`vst.kirjoittaminen`}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
        </>
      )}
      {isVSTOsasuoritusJollaOsasuorituksia(osasuoritus) && (
        <OsasuoritusTable
          editMode={props.form.editMode}
          level={props.level + 1}
          openState={props.osasuoritusOpenState}
          setOsasuoritusOpen={props.setOpenStateHandler}
          // Näkymä, jolla mahdollistetaan rekursiivinen osasuorituksen lisäys
          addNewOsasuoritusView={AddNewVSTOsasuoritusView}
          addNewOsasuoritusViewProps={{
            form: props.form,
            level: props.level + 1,
            createOsasuoritus: props.createOsasuoritus,
            // @ts-expect-error TODO: Tyypitä fiksusti
            pathWithOsasuoritukset: props.osasuoritusPath
          }}
          onRemove={(i) => {
            props.form.updateAt(
              props.osasuoritusPath
                .guard(isVSTOsasuoritusJollaOsasuorituksia)
                .prop('osasuoritukset')
                .optional(),
              // TypeScript-kääntäjän rajoitusten takia pitää olla näin.
              (os) => (os as any[]).filter((_, index) => i !== index)
            )
          }}
          rows={(osasuoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) => {
              return osasuoritusToTableRow({
                level: props.level + 1,
                form: props.form,
                osasuoritusPath: props.osasuoritusPath,
                allOsasuorituksetOpen: props.allOpen,
                createOsasuoritus: props.createOsasuoritus,
                // @ts-expect-error
                suoritusPath: props.osasuoritusPath,
                osasuoritusIndex: osasuoritusIndex,
                setOsasuoritusOpen: props.setOpenStateHandler,
                osasuorituksetExpandedState: props.osasuoritusOpenState,
                suoritusIndex: props.osasuoritusIndex
              })
            }
          )}
        />
      )}
    </div>
  )
}
