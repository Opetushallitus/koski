import React, { useCallback, useContext, useMemo } from 'react'
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
import { isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { lastElement, päätasonSuoritusPath } from '../util/optics'
import { createVstArviointi, defaultLaajuusOpintopisteissa } from './resolvers'
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
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
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
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import {
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus,
  OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Spacer } from '../components-v2/layout/Spacer'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { isVapaanSivistystyönJotpaKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import {
  KuvausEdit,
  KuvausView
} from '../components-v2/opiskeluoikeus/KuvausField'
import {
  TunnustusEdit,
  TunnustusView
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import {
  KehittyvänKielenTaitotasoEdit,
  KehittyvänKielenTaitotasoView
} from '../components-v2/opiskeluoikeus/KehittyvänKielenTaitotasoField'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import {
  OsasuorituksetExpandedState,
  SetOsasuoritusOpen
} from './../osasuoritus/hooks'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenKoulutusmoduuli'
import {
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022,
  VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenKoulutusmoduuli2022'
import { usePreferences } from '../appstate/preferences'
import {
  createMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus,
  createOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus,
  createOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus,
  createVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
  createVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus,
  createVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
  createVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus,
  createVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus
} from './VSTOsasuoritukset'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { subTestId } from '../components-v2/CommonProps'

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
}

export const AddNewVSTOsasuoritusView: React.FC<
  AddNewVSTOsasuoritusViewProps
> = (props) => {
  const data = useMemo(
    () => getValue(props.pathWithOsasuoritukset)(props.form.state),
    [props.form.state, props.pathWithOsasuoritukset]
  )
  const { pathWithOsasuoritukset, createOsasuoritus } = props

  const { organisaatio } = useContext(OpiskeluoikeusContext)

  const isVstJotpa =
    isVapaanSivistystyönJotpaKoulutuksenSuoritus(data) ||
    isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(data)

  const isVstVapaatavoitteinen =
    isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(data) ||
    isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(data)

  const isVstOppivelvollisille =
    isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(data) ||
    isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
      data
    ) ||
    isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
      data
    )

  const isVstMamu =
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
      data
    ) ||
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
      data
    )

  const isVstKoto =
    isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(data)

  const preferencesType = isVstJotpa
    ? 'vapaansivistystyonjotpakoulutuksenosasuoritus'
    : isVstVapaatavoitteinen
    ? 'vapaansivistystyonvapaatavoitteisenkoulutuksenosasuoritus'
    : isVstOppivelvollisille
    ? 'oppivelvollisillesuunnattuvapaansivistystyonopintokokonaisuus'
    : isVstMamu
    ? 'vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenopintojenosasuoritus'
    : isVstKoto
    ? 'vstkotoutumiskoulutuksenvalinnaistenopintojenalasuorituksenkoulutusmoduuli2022'
    : undefined

  type StorableVST =
    | VapaanSivistystyönJotpaKoulutuksenOsasuoritus
    | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
    | VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
    | OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus

  const osasuoritukset = usePreferences<StorableVST>(
    organisaatio?.oid,
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
          (isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
            osasuoritus
          ) ||
            isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
              osasuoritus
            ) ||
            isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli(
              osasuoritus
            ) ||
            isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
              osasuoritus
            ) ||
            isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
              osasuoritus
            ) ||
            isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
              osasuoritus
            ))
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
        {isVstVapaatavoitteinen && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste, isNew) =>
              onKoodistoSelect(
                createVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
                  tunniste
                ),
                isNew
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
            tunnisteet={storedOsasuoritustunnisteet}
            testId={'vapaatavoitteinen-osasuoritus'}
          />
        )}
        {isVstJotpa && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={(tunniste, isNew) =>
              onKoodistoSelect(
                createVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
                  tunniste
                ),
                isNew
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
            tunnisteet={storedOsasuoritustunnisteet}
            testId={'jotpa-osasuoritus'}
          />
        )}
        {isVapaanSivistystyönLukutaitokoulutuksenSuoritus(data) && (
          <KoodistoSelect
            testId="vstlukutaitokoulutuksenkokonaisuus"
            koodistoUri="vstlukutaitokoulutuksenkokonaisuus"
            addNewText={t('Lisää kokonaisuus')}
            onSelect={(tunniste, _isNew) => {
              onKoodistoSelect(
                createVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
                  tunniste
                )
              )
            }}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
          data
        ) && (
          <>
            <KoodistoSelect
              testId="vstosaamiskokonaisuus"
              koodistoUri="vstosaamiskokonaisuus"
              addNewText={t('Lisää osaamiskokonaisuus')}
              onSelect={(tunniste) => {
                onKoodistoSelect(
                  createOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
                    tunniste
                  )
                )
              }}
              onRemove={onRemoveKoodisto}
            />
            <KoodistoSelect
              testId="vstmuutopinnot"
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
        {isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää paikallinen opintokokonaisuus')}
            onSelect={(tunniste, isNew) =>
              onKoodistoSelect(
                createOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
                  tunniste
                ),
                isNew
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
            tunnisteet={storedOsasuoritustunnisteet}
            testId={'oppivelvollisille-paikallinen-opintokokonaisuus'}
          />
        )}
        {isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
          data
        ) && (
          <>
            <KoodistoSelect
              testId="vstmuuallasuoritetutopinnot"
              koodistoUri="vstmuuallasuoritetutopinnot"
              addNewText={t('Lisää muualla suoritettu opinto')}
              onSelect={(tunniste) =>
                onKoodistoSelect(
                  createMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
                    tunniste
                  )
                )
              }
              onRemove={onRemoveKoodisto}
            />
            <PaikallinenOsasuoritusSelect
              addNewText={t('Lisää paikallinen opintokokonaisuus')}
              onSelect={(tunniste, isNew) =>
                onKoodistoSelect(
                  createOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
                    tunniste
                  ),
                  isNew
                )
              }
              onRemove={onRemovePaikallinenKoodisto}
              tunnisteet={storedOsasuoritustunnisteet}
              testId={'oppivelvollisille-paikallinen-opintokokonaisuus'}
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
                    createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus()
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
                    createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus()
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
                    createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus()
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
                    createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus()
                  )
                }
              >
                {t('Lisää valinnaisten opintojen osa-alue')}
              </RaisedButton>
            )}
          </>
        )}
        {isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            testId="vstkoto2022kokonaisuus"
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
        {isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
          data
        ) && (
          <>
            <PaikallinenOsasuoritusSelect
              addNewText={t('Lisää työelämäjakso')}
              labelText={t('Lisää työelämäjakso')}
              modalTitle={t('Lisää työelämäjakso')}
              namePlaceholder={t('Työelämäjakson nimi')}
              onSelect={(tunniste, isNew) =>
                onKoodistoSelect(
                  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
                    tunniste
                  ),
                  isNew
                )
              }
              onRemove={onRemovePaikallinenKoodisto}
              tunnisteet={storedOsasuoritustunnisteet}
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
              onSelect={(tunniste, isNew) =>
                onKoodistoSelect(
                  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
                    tunniste
                  ),
                  isNew
                )
              }
              onRemove={onRemovePaikallinenKoodisto}
              tunnisteet={storedOsasuoritustunnisteet}
            />
          </>
        )}
        {isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää valinnaiset')}
            onSelect={(tunniste, isNew) =>
              onKoodistoSelect(
                createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
                  tunniste
                ),
                isNew
              )
            }
            onRemove={onRemovePaikallinenKoodisto}
            tunnisteet={storedOsasuoritustunnisteet}
            testId={'koto-valinnaisten-paikallinen-osasuoritus'}
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
                createVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus(
                  tunniste
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
            onSelect={(tunniste, isNew) => {
              onKoodistoSelect(
                createVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus(
                  tunniste
                ),
                isNew
              )
            }}
            onRemove={onRemovePaikallinenKoodisto}
            tunnisteet={storedOsasuoritustunnisteet}
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
  testId: string
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
  testId: string
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
  setOsasuoritusOpen,
  testId
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
        testId={`${testId}.nimi`}
      />
    ),
    Laajuus: (
      <FormField
        form={form}
        path={osasuoritus.path('koulutusmoduuli.laajuus')}
        view={LaajuusView}
        edit={LaajuusOpintopisteissäEdit}
        testId={`${testId}.laajuus`}
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
        testId={`${testId}.arvosana`}
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
        testId={`${testId}.taitotaso`}
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
        testId={testId}
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
              testId={subTestId(props, 'arviointi.arvosana')}
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
              testId={subTestId(props, 'arviointi.päivä')}
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
                testId={subTestId(props, 'kuvaus')}
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
                testId={subTestId(props, 'tunnustettu')}
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
                testId={subTestId(props, 'kuullunYmmartaminen')}
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
                testId={subTestId(props, 'puhuminen')}
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
                testId={subTestId(props, 'luetunYmmartaminen')}
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
                testId={subTestId(props, 'kirjoittaminen')}
              />
            </OsasuoritusSubproperty>
          </OsasuoritusProperty>
        </>
      )}
      {isVSTOsasuoritusJollaOsasuorituksia(osasuoritus) && (
        <OsasuoritusTable
          testId={props.testId}
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
                suoritusIndex: props.osasuoritusIndex,
                testId: String(
                  subTestId(props, `osasuoritus.${osasuoritusIndex}`)
                )
              })
            }
          )}
        />
      )}
    </div>
  )
}
