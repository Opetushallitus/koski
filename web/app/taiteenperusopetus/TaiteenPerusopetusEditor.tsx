import * as E from 'fp-ts/Either'
import { isNonEmpty } from 'fp-ts/lib/Array'
import { pipe } from 'fp-ts/lib/function'
import React, { useCallback, useMemo, useState } from 'react'
import { isSuccess, useApiMethod } from '../api-fetch'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { assortedPreferenceType, usePreferences } from '../appstate/preferences'
import { KansalainenOnly } from '../components-v2/access/KansalainenOnly'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { RemoveArrayItemField } from '../components-v2/controls/RemoveArrayItemField'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic, useForm } from '../components-v2/forms/FormModel'
import { useRemovePäätasonSuoritus } from '../components-v2/forms/useRemovePaatasonSuoritus'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { Trans } from '../components-v2/texts/Trans'
import { localize, t } from '../i18n/i18n'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeusjakso'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TaiteenPerusopetuksenPaikallinenOpintokokonaisuus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { append, deleteAt, isSingularArray } from '../util/fp/arrays'
import { deletePäätasonSuoritus } from '../util/koskiApi'
import {
  getOpiskeluoikeusOid,
  mergeOpiskeluoikeusVersionumero
} from '../util/opiskeluoikeus'
import { TaiteenPerusopetuksenTiedot } from './TaiteenPerusopetuksenTiedot'
import {
  createTpoArviointi,
  minimimääräArvioitujaOsasuorituksia,
  TaiteenPerusopetuksenPäätasonSuoritusEq,
  taiteenPerusopetuksenSuorituksenNimi
} from './tpoCommon'
import { TpoOsasuoritusProperties } from './TpoOsasuoritusProperties'
import {
  createCompanionSuoritus,
  UusiTaiteenPerusopetuksenPäätasonSuoritusModal
} from './UusiTaiteenPerusopetuksenPäätasonSuoritus'

export type TaiteenPerusopetusEditorProps =
  AdaptedOpiskeluoikeusEditorProps<TaiteenPerusopetuksenOpiskeluoikeus>

export const TaiteenPerusopetusEditor = (
  props: TaiteenPerusopetusEditorProps
) => {
  const fillKoodistot = useKoodistoFiller()

  // Opiskeluoikeus

  const opiskeluoikeusSchema = useSchema('TaiteenPerusopetuksenOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  // Oppilaitos

  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  // Päätason suoritus

  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)
  const companionPäätasonSuoritus = useMemo(() => {
    const suoritukset = form.state.suoritukset
    return isSingularArray(suoritukset)
      ? createCompanionSuoritus(suoritukset[0])
      : null
  }, [form.state.suoritukset])

  const [newSuoritusModalVisible, setNewSuoritusModalVisible] = useState(false)

  const createPäätasonSuoritus = useCallback(() => {
    if (companionPäätasonSuoritus) {
      form.updateAt(
        form.root.prop('suoritukset'),
        append(companionPäätasonSuoritus)
      )
    }
    setNewSuoritusModalVisible(false)
  }, [companionPäätasonSuoritus, form])

  const removePäätasonSuoritus = useRemovePäätasonSuoritus(
    form,
    päätasonSuoritus.suoritus,
    TaiteenPerusopetuksenPäätasonSuoritusEq,
    () => setPäätasonSuoritus(0)
  )

  // Osasuoritukset

  const osasuoritukset =
    usePreferences<TaiteenPerusopetuksenPaikallinenOpintokokonaisuus>(
      organisaatio?.oid,
      // Ladataan ja tallennetaan osasuoritukset oppimäärän ja taiteenalan perusteella omiin lokeroihin
      assortedPreferenceType(
        'taiteenperusopetus',
        form.state.oppimäärä.koodiarvo,
        päätasonSuoritus.suoritus.koulutusmoduuli.taiteenala.koodiarvo
      )
    )

  const storedOsasuoritustunnisteet = useMemo(
    () => osasuoritukset.preferences.map((p) => p.tunniste),
    [osasuoritukset.preferences]
  )

  const onAddOsasuoritus = useCallback(
    async (tunniste: PaikallinenKoodi, isNew: boolean) => {
      const newOsasuoritus = await fillKoodistot(
        TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus({
          koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus({
            tunniste,
            laajuus: LaajuusOpintopisteissä({ arvo: 1 })
          })
        })
      )

      form.updateAt(päätasonSuoritus.path, (a) => ({
        ...a,
        osasuoritukset: append(newOsasuoritus)(a.osasuoritukset)
      }))

      if (isNew) {
        osasuoritukset.store(tunniste.koodiarvo, newOsasuoritus.koulutusmoduuli)
      }
    },
    [fillKoodistot, form, osasuoritukset, päätasonSuoritus.path]
  )

  const onRemoveOsasuoritus = useCallback(
    (osasuoritusIndex: number) => {
      form.updateAt(päätasonSuoritus.path, (a) =>
        a.osasuoritukset
          ? {
              ...a,
              osasuoritukset: deleteAt(a.osasuoritukset, osasuoritusIndex)
            }
          : a
      )
    },
    [form, päätasonSuoritus.path]
  )

  const onRemoveStoredOsasuoritus = useCallback(
    (tunniste: PaikallinenKoodi) => {
      osasuoritukset.remove(tunniste.koodiarvo)
    },
    [osasuoritukset]
  )

  // Render

  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={tpoKoulutuksenNimi(form.state)}
      />

      <EditorContainer
        form={form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        onChangeSuoritus={setPäätasonSuoritus}
        createOpiskeluoikeusjakso={TaiteenPerusopetuksenOpiskeluoikeusjakso}
        suorituksenNimi={taiteenPerusopetuksenSuorituksenNimi}
        suorituksenLisäys={
          companionPäätasonSuoritus
            ? localize(
                t('Lisää') +
                  ' ' +
                  t(
                    taiteenPerusopetuksenSuorituksenNimi(
                      companionPäätasonSuoritus
                    )
                  ).toLowerCase()
              )
            : undefined
        }
        onCreateSuoritus={() => setNewSuoritusModalVisible(true)}
      >
        {companionPäätasonSuoritus && newSuoritusModalVisible && (
          <UusiTaiteenPerusopetuksenPäätasonSuoritusModal
            opiskeluoikeus={form.state}
            suoritus={companionPäätasonSuoritus}
            onCreate={createPäätasonSuoritus}
            onDismiss={() => setNewSuoritusModalVisible(false)}
          />
        )}

        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>

        {form.state.suoritukset.length > 1 && (
          <ColumnRow>
            <Column span={24} align="right">
              <RemoveArrayItemField
                form={form}
                path={form.root.prop('suoritukset')}
                removeAt={päätasonSuoritus.index}
                label="Poista suoritus"
                onRemove={removePäätasonSuoritus}
                confirmation={{
                  confirm: 'Vahvista poisto, operaatiota ei voi peruuttaa',
                  cancel: 'Peruuta poisto'
                }}
              />
            </Column>
          </ColumnRow>
        )}

        <TaiteenPerusopetuksenTiedot
          form={form}
          päätasonSuoritus={päätasonSuoritus}
        />
        <Spacer />

        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={
            !minimimääräArvioitujaOsasuorituksia(päätasonSuoritus.suoritus)
          }
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset &&
          isNonEmpty(päätasonSuoritus.suoritus.osasuoritukset) && (
            <>
              <OsasuoritusTable
                editMode={form.editMode}
                rows={päätasonSuoritus.suoritus.osasuoritukset.map(
                  (_, osasuoritusIndex) =>
                    osasuoritusToTableRow(
                      form,
                      päätasonSuoritus.path,
                      osasuoritusIndex
                    )
                )}
                onRemove={onRemoveOsasuoritus}
              />
              <Spacer />
            </>
          )}

        {form.editMode && (
          <ColumnRow>
            <Column span={{ default: 1, phone: 0 }} />
            <Column span={{ default: 14, small: 10, phone: 24 }}>
              <PaikallinenOsasuoritusSelect
                tunnisteet={storedOsasuoritustunnisteet}
                onSelect={onAddOsasuoritus}
                onRemove={onRemoveStoredOsasuoritus}
              />
            </Column>
          </ColumnRow>
        )}
      </EditorContainer>
    </>
  )
}

const osasuoritusToTableRow = (
  form: FormModel<TaiteenPerusopetuksenOpiskeluoikeus>,
  suoritusPath: FormOptic<
    TaiteenPerusopetuksenOpiskeluoikeus,
    TaiteenPerusopetuksenPäätasonSuoritus
  >,
  osasuoritusIndex: number
): OsasuoritusRowData<'Kurssi' | 'Laajuus' | 'Arviointi'> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  return {
    columns: {
      Kurssi: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
          view={(props) => <Trans>{props.value}</Trans>}
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
        />
      ),
      Arviointi: (
        <FormField
          form={form}
          path={osasuoritus.prop('arviointi')}
          view={(props) => <ArvosanaView {...props} />}
          edit={(props) => (
            <ArvosanaEdit {...props} createArviointi={createTpoArviointi} />
          )}
        />
      )
    },
    content: (
      <TpoOsasuoritusProperties
        key="lol"
        form={form}
        osasuoritusPath={osasuoritus}
      />
    )
  }
}

const tpoKoulutuksenNimi = (
  opiskeluoikeus: TaiteenPerusopetuksenOpiskeluoikeus
): string => {
  return `${t(opiskeluoikeus.oppimäärä.nimi)}, ${t(
    opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.taiteenala.nimi
  )}`.toLowerCase()
}
