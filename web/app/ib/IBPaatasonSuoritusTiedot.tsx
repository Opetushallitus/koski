import React, { useCallback, useMemo } from 'react'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import {
  ActivePäätasonSuoritus,
  hasPäätasonsuoritusOf
} from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { Checkbox } from '../components-v2/controls/Checkbox'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { Select, useKoodistoOptions } from '../components-v2/controls/Select'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel, getValue } from '../components-v2/forms/FormModel'
import {
  ArvosanaEdit,
  ArvosanaView,
  koodiarvoAndNimi
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { OppiaineenKurssit } from '../components-v2/opiskeluoikeus/OppiaineTable'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import { t } from '../i18n/i18n'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { IBKurssinArviointi } from '../types/fi/oph/koski/schema/IBKurssinArviointi'
import { IBKurssinSuoritus } from '../types/fi/oph/koski/schema/IBKurssinSuoritus'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBOppiaineTheoryOfKnowledge } from '../types/fi/oph/koski/schema/IBOppiaineTheoryOfKnowledge'
import { IBTheoryOfKnowledgeSuoritus } from '../types/fi/oph/koski/schema/IBTheoryOfKnowledgeSuoritus'
import {
  IBTutkinnonSuoritus,
  isIBTutkinnonSuoritus
} from '../types/fi/oph/koski/schema/IBTutkinnonSuoritus'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { appendOptional, deleteAt } from '../util/array'
import { koodiviiteId } from '../util/koodisto'
import { lastElement } from '../util/optics'
import { isKoodiarvoOf } from '../util/types'
import { useBooleanState } from '../util/useBooleanState'
import { DialogSelect } from '../uusiopiskeluoikeus/components/DialogSelect'
import { UusiIBTutkintoOsasuoritusDialog } from './dialogs/UusiIBTutkintoOsasuoritusDialog'
import { createIBCASSuoritus } from './oppiaineet/ibTutkintoOppiaine'
import { useExtendedEssayState } from './state/extendedEssay'
import {
  useAineryhmäOptions,
  useKielivalikoimaOptions,
  useOppiaineTasoOptions
} from './state/options'
import { isNonEmpty } from 'fp-ts/lib/Array'

export type IBTutkintTiedotProps = {
  form: FormModel<IBOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<IBOpiskeluoikeus>
}

export const IBPäätasonSuoritusTiedot: React.FC<IBTutkintTiedotProps> = ({
  form,
  päätasonSuoritus
}) => {
  const path = päätasonSuoritus.path

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Koulutus">
        <TestIdText id="koulutus">
          {t(päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        <FormField
          form={form}
          path={path.prop('toimipiste')}
          view={OrganisaatioView}
          edit={OrganisaatioEdit}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suorituskieli">
        <FormField
          form={form}
          path={path.prop('suorituskieli')}
          view={KoodistoView}
          edit={KoodistoEdit}
          editProps={{ koodistoUri: 'kieli' }}
          testId="suorituskieli"
        />
      </KeyValueRow>
      {hasPäätasonsuoritusOf(isIBTutkinnonSuoritus, päätasonSuoritus) && (
        <IBTutkinnonTiedotRows
          form={form}
          päätasonSuoritus={päätasonSuoritus}
        />
      )}
      <KeyValueRow localizableLabel="Todistuksella näkyvät lisätiedot">
        <FormField
          form={form}
          path={path.prop('todistuksellaNäkyvätLisätiedot')}
          view={LocalizedTextView}
          edit={LocalizedTextEdit}
          testId="todistuksellaNäkyvätLisätiedot"
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}

type IBTutkinnonTiedotRowsProps = {
  form: FormModel<IBOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<
    IBOpiskeluoikeus,
    IBTutkinnonSuoritus
  >
}

const IBTutkinnonTiedotRows: React.FC<IBTutkinnonTiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const path = päätasonSuoritus.path

  return (
    <>
      <TheoryOfKnowledgeRows form={form} päätasonSuoritus={päätasonSuoritus} />
      <ExtendedEssayFieldRows form={form} päätasonSuoritus={päätasonSuoritus} />
      <KeyValueRow localizableLabel="Creativity action service">
        <CreativityActionServiceField
          form={form}
          päätasonSuoritus={päätasonSuoritus}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Lisäpisteet">
        {(form.editMode || päätasonSuoritus.suoritus.lisäpisteet) && (
          <FormField
            form={form}
            path={path.prop('lisäpisteet')}
            view={KoodistoView}
            edit={KoodistoEdit}
            editProps={{ koodistoUri: 'arviointiasteikkolisapisteetib' }}
            testId="lisäpisteet"
          />
        )}
      </KeyValueRow>
    </>
  )
}

const TheoryOfKnowledgeRows: React.FC<IBTutkinnonTiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const [newKurssiDialogVisible, showNewKurssiDialog, hideNewKurssiDialog] =
    useBooleanState(false)

  const theoryOfKnowledgePath = päätasonSuoritus.path.prop('theoryOfKnowledge')
  const theoryOfKnowledge = getValue(theoryOfKnowledgePath)(form.state)

  const [kurssitPath, kurssit] = useMemo(() => {
    const path = theoryOfKnowledgePath
      .valueOr(emptyTheoryOfKnowledge)
      .prop('osasuoritukset')
      .valueOr([])
    return [path, getValue(path)(form.state) || []]
  }, [form.state, theoryOfKnowledgePath])

  const onAdd = useCallback(
    (kurssi: IBKurssinSuoritus) => {
      form.updateAt(
        päätasonSuoritus.path
          .prop('theoryOfKnowledge')
          .valueOr(emptyTheoryOfKnowledge)
          .prop('osasuoritukset'),
        appendOptional(kurssi)
      )
      hideNewKurssiDialog()
    },
    [form, hideNewKurssiDialog, päätasonSuoritus.path]
  )

  const onDelete = useCallback(
    (index: number) => form.updateAt(kurssitPath, deleteAt(index)),
    [form, kurssitPath]
  )

  const onKurssinArviointi = useCallback(
    (index: number, arviointi: Arviointi) =>
      form.updateAt(
        kurssitPath.at(index).prop('arviointi'),
        appendOptional(arviointi as IBKurssinArviointi)
      ),
    [form, kurssitPath]
  )

  return (
    <TestIdLayer id="theoryOfKnowledge">
      <KeyValueRow localizableLabel="Theory of knowledge">
        <KeyValueTable>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            <FormField
              form={form}
              path={theoryOfKnowledgePath
                .valueOr(emptyTheoryOfKnowledge)
                .prop('arviointi')
                .valueOr([])
                .compose(lastElement())}
              view={ArvosanaView}
              edit={ArvosanaEdit}
              editProps={{
                suoritusClassName: IBTheoryOfKnowledgeSuoritus.className
              }}
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Pakollinen" innerKeyValueTable>
            <FormField
              form={form}
              path={theoryOfKnowledgePath
                .valueOr(emptyTheoryOfKnowledge)
                .prop('koulutusmoduuli')
                .prop('pakollinen')
                .optional()}
              view={BooleanView}
              edit={BooleanEdit}
              testId="pakollinen"
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Kurssit" innerKeyValueTable>
            {isNonEmpty(kurssit) || form.editMode ? (
              <>
                <OppiaineenKurssit
                  form={form}
                  kurssit={kurssit}
                  oppiaine={theoryOfKnowledge!}
                  oppiainePath={[
                    ...päätasonSuoritus.pathTokens,
                    'theoryOfKnowledge'
                  ]}
                  hidePaikallinenIndicator
                  onArviointi={onKurssinArviointi}
                  onDeleteKurssi={onDelete}
                  onShowAddOsasuoritusDialog={showNewKurssiDialog}
                />
                {newKurssiDialogVisible && (
                  <UusiIBTutkintoOsasuoritusDialog
                    organisaatioOid={päätasonSuoritus.suoritus.toimipiste.oid}
                    oppiaine={theoryOfKnowledge!}
                    onAdd={onAdd}
                    onClose={hideNewKurssiDialog}
                  />
                )}
              </>
            ) : null}
          </KeyValueRow>
        </KeyValueTable>
      </KeyValueRow>
    </TestIdLayer>
  )
}

const emptyTheoryOfKnowledge = IBTheoryOfKnowledgeSuoritus({
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge({ pakollinen: false })
})

const ExtendedEssayFieldRows: React.FC<IBTutkinnonTiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const state = useExtendedEssayState(form, päätasonSuoritus)
  const tunnisteet = useKoodistoOptions('oppiaineetib')
  const kielet = useKielivalikoimaOptions(true)
  const ryhmät = useAineryhmäOptions(true)
  const tasot = useOppiaineTasoOptions(true)

  if (!form.editMode && !päätasonSuoritus.suoritus.extendedEssay) {
    return null
  }

  return (
    <TestIdLayer id="extendedEssay">
      <KeyValueRow localizableLabel="Extended essay">
        <KeyValueTable>
          {state.tunniste.visible && tunnisteet && (
            <KeyValueRow localizableLabel="Oppiaine" innerKeyValueTable>
              {form.editMode ? (
                <Select
                  inlineOptions
                  options={tunnisteet}
                  value={
                    state.tunniste.value && koodiviiteId(state.tunniste.value)
                  }
                  onChange={(opt) => state.tunniste.set(opt?.value)}
                  testId="oppiaine"
                />
              ) : (
                <TestIdText id="oppiaine">
                  {t(state.tunniste.value?.nimi)}
                </TestIdText>
              )}
            </KeyValueRow>
          )}
          {state.kieli.visible && kielet && (
            <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
              {form.editMode ? (
                <DialogSelect
                  options={kielet}
                  value={state.kieli.value && koodiviiteId(state.kieli.value)}
                  onChange={(o) => state.kieli.set(o?.value)}
                  testId="kieli"
                />
              ) : (
                <TestIdText id="kieli">
                  {t(state.kieli?.value?.nimi)}
                </TestIdText>
              )}
            </KeyValueRow>
          )}
          {state.taso.visible && tasot && (
            <KeyValueRow localizableLabel="Taso" innerKeyValueTable>
              {form.editMode ? (
                <DialogSelect
                  options={tasot}
                  value={state.taso.value && koodiviiteId(state.taso.value)}
                  onChange={(o) => state.taso.set(o?.value)}
                  testId="taso"
                />
              ) : (
                <TestIdText id="taso">{t(state.taso.value?.nimi)}</TestIdText>
              )}
            </KeyValueRow>
          )}
          {state.ryhmä.visible && ryhmät && (
            <KeyValueRow localizableLabel="Aineryhmä" innerKeyValueTable>
              {form.editMode ? (
                <DialogSelect
                  options={ryhmät}
                  value={state.ryhmä.value && koodiviiteId(state.ryhmä.value)}
                  onChange={(o) => state.ryhmä.set(o?.value)}
                  testId="ryhmä"
                />
              ) : (
                <TestIdText id="ryhma">{t(state.ryhmä.value?.nimi)}</TestIdText>
              )}
            </KeyValueRow>
          )}
          {state.pakollinen.visible && (
            <KeyValueRow innerKeyValueTable>
              {form.editMode ? (
                <Checkbox
                  label={t('Pakollinen')}
                  checked={!!state.pakollinen.value}
                  onChange={state.pakollinen.set}
                  testId="pakollinen"
                />
              ) : (
                <TestIdText id="pakollinen">
                  {state.pakollinen ? t('Pakollinen') : t('Valinnainen')}
                </TestIdText>
              )}
            </KeyValueRow>
          )}
        </KeyValueTable>
        {state.aihe.visible && (
          <KeyValueRow localizableLabel="Aihe" innerKeyValueTable>
            {form.editMode ? (
              <LocalizedTextEdit
                value={state.aihe.value}
                onChange={state.aihe.set}
                testId="aihe"
              />
            ) : (
              <TestIdText id="aihe">{t(state.aihe.value)}</TestIdText>
            )}
          </KeyValueRow>
        )}
        {state.arvosana.visible && (
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            {form.editMode ? (
              <KoodistoSelect
                koodistoUri="arviointiasteikkocorerequirementsib"
                value={state.arvosana.value?.koodiarvo}
                format={koodiarvoAndNimi}
                onSelect={state.arvosana.set}
                testId="arvosana"
              />
            ) : (
              <TestIdText id="arvosana">
                {state.arvosana.value?.koodiarvo}{' '}
                {t(state.arvosana.value?.nimi)}
              </TestIdText>
            )}
          </KeyValueRow>
        )}
      </KeyValueRow>
    </TestIdLayer>
  )
}

const CreativityActionServiceField: React.FC<IBTutkinnonTiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const props = useMemo(
    () => ({
      koodistoUri: 'arviointiasteikkoib',
      koodiarvot: ['S'],
      value:
        päätasonSuoritus.suoritus.creativityActionService?.arviointi?.[0]
          .arvosana,
      testId: 'ibcas'
    }),
    [päätasonSuoritus.suoritus.creativityActionService?.arviointi]
  )

  const onSelect = useCallback(
    (arvosana?: Koodistokoodiviite) => {
      const suoritus = isKoodiarvoOf('arviointiasteikkoib', ['S'])(arvosana)
        ? createIBCASSuoritus(arvosana)
        : undefined
      form.updateAt(
        päätasonSuoritus.path.prop('creativityActionService'),
        () => suoritus
      )
    },
    [form, päätasonSuoritus.path]
  )

  if (!form.editMode && !päätasonSuoritus.suoritus.creativityActionService) {
    return null
  }

  return form.editMode ? (
    <KoodistoSelect
      {...props}
      value={props.value?.koodiarvo}
      onSelect={onSelect}
      zeroValueOption
      testId="creativityActionService"
    />
  ) : (
    <KoodistoView {...props} testId="creativityActionService" />
  )
}
