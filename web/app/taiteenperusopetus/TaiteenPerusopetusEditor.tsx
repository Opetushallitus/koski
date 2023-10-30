import { isNonEmpty } from 'fp-ts/lib/Array'
import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { useSchema } from '../appstate/constraints'
import { useKoodistoFiller } from '../appstate/koodisto'
import { assortedPreferenceType, usePreferences } from '../appstate/preferences'
import { KansalainenOnly } from '../components-v2/access/KansalainenOnly'
import { subTestId } from '../components-v2/CommonProps'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { RemoveArrayItemField } from '../components-v2/controls/RemoveArrayItemField'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic, useForm } from '../components-v2/forms/FormModel'
import { useRemovePäätasonSuoritus } from '../components-v2/forms/useRemovePaatasonSuoritus'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import {
  osasuoritusTestId,
  OsasuoritusRowData,
  OsasuoritusTable
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { localize, t } from '../i18n/i18n'
import { useOsasuorituksetExpand } from '../osasuoritus/hooks'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeusjakso'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TaiteenPerusopetuksenPaikallinenOpintokokonaisuus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
import { TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
import { append, deleteAt, isSingularArray } from '../util/fp/arrays'
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

export const TaiteenPerusopetusEditor: React.FC<
  TaiteenPerusopetusEditorProps
> = (props) => {
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

  const suorituksetVahvistettu =
    form.state.suoritukset.filter((s) => Boolean(s.vahvistus)).length >= 2

  const {
    closeAllOsasuoritukset,
    openAllOsasuoritukset,
    osasuorituksetOpenState,
    rootLevelOsasuoritusOpen,
    setOsasuorituksetStateHandler
  } = useOsasuorituksetExpand(päätasonSuoritus)

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
        suorituksetVahvistettu={suorituksetVahvistettu}
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
                testId={`${päätasonSuoritus.testId}.invalidate`}
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
          testId={päätasonSuoritus.testId}
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset &&
          isNonEmpty(päätasonSuoritus.suoritus.osasuoritukset) && (
            <>
              <RaisedButton
                data-testid={`suoritukset.${päätasonSuoritus.index}.expand`}
                onClick={(e) => {
                  e.preventDefault()
                  if (rootLevelOsasuoritusOpen) {
                    closeAllOsasuoritukset()
                  } else {
                    openAllOsasuoritukset()
                  }
                }}
              >
                {rootLevelOsasuoritusOpen
                  ? t('Sulje kaikki')
                  : t('Avaa kaikki')}
              </RaisedButton>
              <Spacer />
              <OsasuoritusTable
                testId={päätasonSuoritus.testId}
                editMode={form.editMode}
                level={0}
                openState={osasuorituksetOpenState}
                setOsasuoritusOpen={setOsasuorituksetStateHandler}
                rows={päätasonSuoritus.suoritus.osasuoritukset.map(
                  (_, osasuoritusIndex) =>
                    osasuoritusToTableRow(
                      form,
                      päätasonSuoritus.path,
                      päätasonSuoritus.index,
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
                testId={subTestId(päätasonSuoritus, 'addOsasuoritus')}
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
  suoritusIndex: number,
  osasuoritusIndex: number
): OsasuoritusRowData<'Osasuoritus' | 'Laajuus' | 'Arviointi'> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  return {
    suoritusIndex,
    osasuoritusIndex,
    expandable: true,
    columns: {
      Osasuoritus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId={osasuoritusTestId(suoritusIndex, osasuoritusIndex, 'nimi')}
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
          testId={osasuoritusTestId(suoritusIndex, osasuoritusIndex, 'laajuus')}
        />
      ),
      Arviointi: (
        <FormField
          form={form}
          path={osasuoritus.prop('arviointi')}
          view={(props) => <ParasArvosanaView {...props} />}
          edit={(props) => (
            <ParasArvosanaEdit
              {...props}
              createArviointi={createTpoArviointi}
            />
          )}
          testId={osasuoritusTestId(
            suoritusIndex,
            osasuoritusIndex,
            'arvosana'
          )}
        />
      )
    },
    content: (
      <TpoOsasuoritusProperties
        form={form}
        osasuoritusPath={osasuoritus}
        testId={osasuoritusTestId(
          suoritusIndex,
          osasuoritusIndex,
          'properties'
        )}
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
