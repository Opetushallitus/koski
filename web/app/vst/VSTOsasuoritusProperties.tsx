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
import { isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
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
import { isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { t } from '../i18n/i18n'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import {
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
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

type AddNewVSTOsasuoritusViewProps = {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  createOsasuoritusV2: (
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
  const { pathWithOsasuoritukset, createOsasuoritusV2 } = props
  /*
  const onKoodistoSelect = useCallback(
    (koodi: Koodistokoodiviite<string, string>) => {
      if (pathWithOsasuoritukset !== undefined) {
        createOsasuoritus(pathWithOsasuoritukset, koodi)
      } else {
        console.warn('pathWithOsasuoritukset is undefined')
      }
    },
    [createOsasuoritus, pathWithOsasuoritukset]
  )
  */
  const onPaikallinenKoodistoSelectV2 = useCallback(
    (osasuoritus: VSTOsasuoritus) => {
      if (pathWithOsasuoritukset !== undefined) {
        createOsasuoritusV2(pathWithOsasuoritukset, osasuoritus)
      } else {
        console.warn('pathWithOsasuoritukset is undefined')
      }
    },
    [createOsasuoritusV2, pathWithOsasuoritukset]
  )
  /*
  const onPaikallinenKoodistoSelect = useCallback(
    (koodi: PaikallinenKoodi) => {
      if (pathWithOsasuoritukset !== undefined) {
        createOsasuoritus(pathWithOsasuoritukset, koodi)
      } else {
        console.warn('pathWithOsasuoritukset is undefined')
      }
    },
    [createOsasuoritus, pathWithOsasuoritukset]
  )
  */
  const onRemoveKoodisto = useCallback(
    (_tunniste: Koodistokoodiviite<string, string>) => {
      console.log('TODO: onRemove')
    },
    []
  )
  const onRemovePaikallinenKoodisto = useCallback(
    (paikallinen: PaikallinenKoodi) => {
      console.log('TODO: onRemovePaikallinenKoodisto')
    },
    []
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
              onPaikallinenKoodistoSelectV2(
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
            onSelect={onPaikallinenKoodistoSelect}
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
              onSelect={onKoodistoSelect}
              onRemove={onRemoveKoodisto}
            />
            <PaikallinenOsasuoritusSelect
              addNewText={t('Lisää paikallinen opintokokonaisuus')}
              onSelect={onPaikallinenKoodistoSelect}
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
                onPaikallinenKoodistoSelectV2(
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
                onPaikallinenKoodistoSelectV2(
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
              onPaikallinenKoodistoSelectV2(
                VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
                  {
                    koulutusmoduuli: {
                      tunniste: p,
                      $class:
                        'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus',
                      kuvaus: defaultFinnishKuvaus
                    }
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
            onSelect={onPaikallinenKoodistoSelect}
            onRemove={onRemovePaikallinenKoodisto}
          />
        )}
        {isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
          data
        ) && (
          <>
            <KoodistoSelect
              koodistoUri="vstosaamiskokonaisuus"
              addNewText={t('Lisää osaamiskokonaisuus')}
              onSelect={onKoodistoSelect}
              onRemove={onRemoveKoodisto}
            />
            <KoodistoSelect
              koodistoUri="vstmuutopinnot"
              addNewText={t('Lisää suuntautumisopinto')}
              onSelect={onKoodistoSelect}
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
                  props.form.updateAt(props.pathWithOsasuoritukset, (os) => ({
                    ...os,
                    // @ts-expect-error
                    osasuoritukset: append(
                      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
                        {
                          koulutusmoduuli:
                            VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
                              {
                                tunniste: Koodistokoodiviite({
                                  koodiarvo:
                                    'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus',
                                  koodistoUri:
                                    'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
                                  nimi: Finnish({
                                    fi: 'Suomen kieli ja viestintätaidot'
                                  })
                                })
                              }
                            )
                        }
                      ),
                      // @ts-expect-error
                      os.osasuoritukset
                    )
                  }))
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
                  props.form.updateAt(props.pathWithOsasuoritukset, (os) => ({
                    ...os,
                    // @ts-expect-error
                    osasuoritukset: append(
                      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
                        {
                          koulutusmoduuli: {
                            $class:
                              'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli',
                            tunniste: {
                              $class: 'fi.oph.koski.schema.Koodistokoodiviite',
                              koodiarvo:
                                'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus',
                              koodistoUri:
                                'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
                              nimi: Finnish({
                                fi: 'Työelämä- ja yhteiskuntataidot'
                              })
                            }
                          }
                        }
                      ),
                      // @ts-expect-error
                      os.osasuoritukset
                    )
                  }))
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
                  props.form.updateAt(props.pathWithOsasuoritukset, (os) => ({
                    ...os,
                    // @ts-expect-error
                    osasuoritukset: append(
                      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
                        {
                          koulutusmoduuli: {
                            $class:
                              'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli',
                            tunniste: {
                              $class: 'fi.oph.koski.schema.Koodistokoodiviite',
                              koodiarvo:
                                'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus',
                              koodistoUri:
                                'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
                              nimi: Finnish({
                                fi: 'Ohjaus'
                              })
                            }
                          }
                        }
                      ),
                      // @ts-expect-error
                      os.osasuoritukset
                    )
                  }))
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
                  props.form.updateAt(props.pathWithOsasuoritukset, (os) => ({
                    ...os,
                    // @ts-expect-error
                    osasuoritukset: append(
                      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
                        {
                          koulutusmoduuli: {
                            $class:
                              'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli',
                            tunniste: {
                              $class: 'fi.oph.koski.schema.Koodistokoodiviite',
                              koodiarvo:
                                'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus',
                              koodistoUri:
                                'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
                              nimi: Finnish({
                                fi: 'Valinnaiset opinnot'
                              })
                            }
                          }
                        }
                      ),
                      // @ts-expect-error
                      os.osasuoritukset
                    )
                  }))
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
            onSelect={onPaikallinenKoodistoSelect}
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
            onSelect={onKoodistoSelect}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isVapaanSivistystyönLukutaitokoulutuksenSuoritus(data) && (
          <KoodistoSelect
            koodistoUri="vstlukutaitokoulutuksenkokonaisuus"
            addNewText={t('Lisää kokonaisuus')}
            onSelect={onKoodistoSelect}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
          data
        ) && (
          <KoodistoSelect
            koodistoUri="vstkoto2022kokonaisuus"
            addNewText={t('Lisää osasuoritus')}
            onSelect={onKoodistoSelect}
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
            onSelect={onKoodistoSelect}
            onRemove={onRemoveKoodisto}
          />
        )}
        {isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
          data
        ) && (
          <PaikallinenOsasuoritusSelect
            addNewText={t('Lisää osasuoritus')}
            onSelect={onPaikallinenKoodistoSelect}
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
  createOsasuoritusV2: (
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
  createOsasuoritusV2: (
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
  createOsasuoritusV2,
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
        createOsasuoritusV2={createOsasuoritusV2}
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
            createOsasuoritusV2: props.createOsasuoritusV2,
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
                createOsasuoritusV2: props.createOsasuoritusV2,
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
