import React, { useCallback, useContext, useMemo } from 'react'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { usePreferences } from '../appstate/preferences'
import { CommonProps, subTestId } from '../components-v2/CommonProps'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  KehittyvänKielenTaitotasoEdit,
  KehittyvänKielenTaitotasoView
} from '../components-v2/opiskeluoikeus/KehittyvänKielenTaitotasoField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import {
  KuvausEdit,
  KuvausView
} from '../components-v2/opiskeluoikeus/KuvausField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import {
  TaitotasoEdit,
  TaitotasoView
} from '../components-v2/opiskeluoikeus/TaitotasoField'
import {
  TunnustusEdit,
  TunnustusView
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { t } from '../i18n/i18n'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import {
  OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus,
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import {
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022,
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import {
  VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022,
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
import {
  VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022,
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenKoulutusmoduuli2022'
import {
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022,
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { isVapaanSivistystyönJotpaKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenKoulutusmoduuli'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import { lastElement, parasArviointiElement } from '../util/optics'
import { VSTArviointiEdit, VSTArviointiView } from './VSTArviointiField'
import {
  createMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus,
  createOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus,
  createOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus,
  createVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus,
  createVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus,
  createVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus,
  createVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
  createVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus,
  createVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus,
  createVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
} from './VSTOsasuoritukset'
import { createVstArviointi, defaultLaajuusOpintopisteissa } from './resolvers'
import {
  VSTOsasuoritus,
  VSTOsasuoritusOsasuorituksilla,
  isTunnustettuVSTOsasuoritus,
  isVSTKoulutusmoduuliKuvauksella,
  isVSTOsasuoritusArvioinnilla,
  isVSTOsasuoritusJollaOsasuorituksia
} from './typeguards'
import { VSTJotpaProperties } from './jotpa/VSTJotpaProperties'
import { VSTVapaatavoitteinenProperties } from './vapaatavoitteinen/VSTVapaatavoitteinenProperties'

type AddNewVSTOsasuoritusViewProps = CommonProps<{
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
}>

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

  const isVstKoto2012 =
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
      data
    ) ||
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
      data
    )

  const isVstKoto2022 =
    isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(data)

  const preferencesType = isVstJotpa
    ? 'vapaansivistystyonjotpakoulutuksenosasuoritus'
    : isVstVapaatavoitteinen
    ? 'vapaansivistystyonvapaatavoitteisenkoulutuksenosasuoritus'
    : isVstOppivelvollisille
    ? 'oppivelvollisillesuunnattuvapaansivistystyonopintokokonaisuus'
    : isVstKoto2012
    ? 'vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenopintojenosasuoritus'
    : isVstKoto2022
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
            testId={props.testId}
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
            testId={props.testId}
          />
        )}
        {isVapaanSivistystyönLukutaitokoulutuksenSuoritus(data) && (
          <KoodistoSelect
            testId={props.testId}
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
              testId={props.testId}
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
              testId={subTestId(props, 'suuntautumisopinto')}
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
            testId={subTestId(props, 'paikallinen')}
          />
        )}
        {isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
          data
        ) && (
          <>
            <KoodistoSelect
              testId={props.testId}
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
              testId={subTestId(props, 'paikallinen')}
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
            testId={props.testId}
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
        {isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            testId={props.testId}
            koodistoUri="vstkoto2022kielijaviestintakoulutus"
            addNewText={t(
              'Lisää kieli- ja viestintäkoulutuksen alaosasuoritus'
            )}
            onSelect={(tunniste, _isNew) => {
              onKoodistoSelect(
                createVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus(
                  tunniste
                )
              )
            }}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            testId={props.testId}
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
            testId={props.testId}
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
        view={ParasArvosanaView}
        edit={(arvosanaProps) => {
          if (osasuoritusValue === undefined) {
            return null
          }
          if (!isVSTOsasuoritusArvioinnilla(osasuoritusValue)) {
            return null
          }
          return (
            <ParasArvosanaEdit
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
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  // Refaktorointisiirtymän aikaiset "adaptaatiot":
  if (osasuoritus) {
    if (
      isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(osasuoritus)
    ) {
      // @ts-expect-error TODO: Tää on väliaikaista koodia, joten ei edes yritetä tyypittää pathia kuntoon
      return <VSTJotpaProperties {...props} />
    }
    if (isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(osasuoritus)) {
      // @ts-expect-error TODO: Tää on väliaikaista koodia, joten ei edes yritetä tyypittää pathia kuntoon
      return <VSTVapaatavoitteinenProperties {...props} />
    }
  }

  // Vanha komponentti:

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
    .compose(parasArviointiElement())

  const arvioitu =
    osasuoritus !== undefined &&
    isVSTOsasuoritusArvioinnilla(osasuoritus) &&
    osasuoritus?.arviointi !== undefined &&
    osasuoritus?.arviointi.length > 0

  return (
    <div>
      {arvioitu && (
        <OsasuoritusProperty label="Arviointi">
          <FormListField
            form={props.form}
            path={osasuoritusArvioinnillaPath.prop('arviointi')}
            view={VSTArviointiView}
            edit={VSTArviointiEdit}
            editProps={{ osasuoritus }}
            testId={subTestId(props, 'arviointi')}
          />
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
                suoritusIndex: props.osasuoritusIndex,
                testId: String(
                  subTestId(props, `osasuoritukset.${osasuoritusIndex}`)
                )
              })
            }
          )}
        />
      )}
    </div>
  )
}
