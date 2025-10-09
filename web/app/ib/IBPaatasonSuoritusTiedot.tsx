import React, { useCallback, useMemo, useState } from 'react'
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
  koodiarvoAndNimi,
  ParasArvosanaEdit,
  ParasArvosanaView
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
import {
  Details,
  OppiaineenKurssit,
  SuorituksenTilaIcon
} from '../components-v2/opiskeluoikeus/OppiaineTable'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import { emptyLocalizedString, t } from '../i18n/i18n'
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
import { config } from '../util/config'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import {
  isPreIBSuoritus2019,
  PreIBSuoritus2019
} from '../types/fi/oph/koski/schema/PreIBSuoritus2019'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import {
  LaajuusEdit,
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { Spacer } from '../components-v2/layout/Spacer'
import { Removable } from '../components-v2/controls/Removable'
import { pipe } from 'fp-ts/function'
import * as A from 'fp-ts/Array'
import * as Ord from 'fp-ts/Ord'
import * as S from 'fp-ts/string'
import * as O from 'fp-ts/Option'
import { FlatButton } from '../components-v2/controls/FlatButton'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { DateInput } from '../components-v2/controls/DateInput'
import { SuullisenKielitaidonKoe2019 } from '../types/fi/oph/koski/schema/SuullisenKielitaidonKoe2019'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LukionOmanÄidinkielenOpinnot } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnot'
import { PuhviKoe2019 } from '../types/fi/oph/koski/schema/PuhviKoe2019'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpintojenOsasuoritus'
import { parasArviointi } from '../util/arvioinnit'
import { ISO2FinnishDate } from '../date/date'
import { OsaamisenTunnustusView } from '../components-v2/opiskeluoikeus/TunnustusField'
import { PathToken } from '../util/laxModify'
import { OsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { useKoodistoOfConstraint } from '../appstate/koodisto'
import { useChildSchema } from '../appstate/constraints'
import { LukionOmanÄidinkielenOpinto } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinto'
import { LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnonOsasuorituksenArviointi'

const preIB2019SuullisenKielitaidonTaitotasot: string[] = [
  'alle_A1.1',
  'A1.1',
  'A1.2',
  'A1.3',
  'A2.1',
  'A2.2',
  'B1.1',
  'B1.2',
  'B2.1',
  'B2.2',
  'C1.1',
  'yli_C1.1'
]

type PreIBOmanÄidinkielenOpinto = LukionOmanÄidinkielenOpinto['tunniste']
type PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana =
  LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi['arvosana']

type PreIBSuullisenKielitaidonKoe2019Arvosana = Koodistokoodiviite<
  'arviointiasteikkoyleissivistava',
  '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
>

type PreIBPuhviKoe2019Arvosana = PreIBSuullisenKielitaidonKoe2019Arvosana

type PreIBSuullisenKielitaidonKoe2019Taitotaso = Koodistokoodiviite<
  'arviointiasteikkokehittyvankielitaidontasot',
  | 'alle_A1.1'
  | 'A1.1'
  | 'A1.2'
  | 'A1.3'
  | 'A2.1'
  | 'A2.2'
  | 'B1.1'
  | 'B1.2'
  | 'B2.1'
  | 'B2.2'
  | 'C1.1'
  | 'yli_C1.1'
>

type PreIBOmanÄidinkielenOpinnot2019Arvosana = Koodistokoodiviite<
  'arviointiasteikkoyleissivistava',
  'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
>

export type IBTutkintTiedotProps = {
  form: FormModel<IBOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<IBOpiskeluoikeus>
  alkamispäivä?: string
}

export const IBPäätasonSuoritusTiedot: React.FC<IBTutkintTiedotProps> = ({
  form,
  päätasonSuoritus,
  alkamispäivä
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
      <KeyValueRow localizableLabel="Ryhmä">
        <FormField
          form={form}
          path={path.prop('ryhmä')}
          view={TextView}
          edit={TextEdit}
          testId="ryhmä"
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
          alkamispäivä={alkamispäivä}
        />
      )}
      {hasPäätasonsuoritusOf(isPreIBSuoritus2019, päätasonSuoritus) && (
        <PreIB2019TiedotRows form={form} päätasonSuoritus={päätasonSuoritus} />
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
  alkamispäivä?: string
}

const IBTutkinnonTiedotRows: React.FC<IBTutkinnonTiedotRowsProps> = ({
  form,
  päätasonSuoritus,
  alkamispäivä
}) => {
  const path = päätasonSuoritus.path

  return (
    <>
      <TheoryOfKnowledgeRows
        form={form}
        päätasonSuoritus={päätasonSuoritus}
        alkamispäivä={alkamispäivä}
      />
      <ExtendedEssayFieldRows
        form={form}
        päätasonSuoritus={päätasonSuoritus}
        alkamispäivä={alkamispäivä}
      />
      <KeyValueRow localizableLabel="Creativity action service">
        <CreativityActionServiceField
          form={form}
          päätasonSuoritus={päätasonSuoritus}
          alkamispäivä={alkamispäivä}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Lisäpisteet">
        {((form.editMode && !dpOppiaineetOsasuorituksina(alkamispäivä)) ||
          päätasonSuoritus.suoritus.lisäpisteet) && (
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
  päätasonSuoritus,
  alkamispäivä
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

  return theoryOfKnowledge ||
    (form.editMode && !dpOppiaineetOsasuorituksina(alkamispäivä)) ? (
    <TestIdLayer id="theoryOfKnowledge">
      <KeyValueRow localizableLabel="Theory of knowledge">
        <KeyValueTable>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            <FormField
              form={form}
              path={theoryOfKnowledgePath
                .valueOr(emptyTheoryOfKnowledge)
                .prop('arviointi')
                .valueOr([])}
              view={ParasArvosanaView}
              edit={ParasArvosanaEdit}
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
            {A.isNonEmpty(kurssit) || form.editMode ? (
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
  ) : null
}

const emptyTheoryOfKnowledge = IBTheoryOfKnowledgeSuoritus({
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge({ pakollinen: false })
})

const ExtendedEssayFieldRows: React.FC<IBTutkinnonTiedotRowsProps> = ({
  form,
  päätasonSuoritus,
  alkamispäivä
}) => {
  const state = useExtendedEssayState(form, päätasonSuoritus)
  const tunnisteet = useKoodistoOptions('oppiaineetib')
  const kielet = useKielivalikoimaOptions(true)
  const ryhmät = useAineryhmäOptions(true)
  const tasot = useOppiaineTasoOptions(true)

  if (
    !(form.editMode && !dpOppiaineetOsasuorituksina(alkamispäivä)) &&
    !päätasonSuoritus.suoritus.extendedEssay
  ) {
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
                  {state.pakollinen.value ? t('Pakollinen') : t('Valinnainen')}
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
  päätasonSuoritus,
  alkamispäivä
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

  if (
    !(form.editMode && !dpOppiaineetOsasuorituksina(alkamispäivä)) &&
    !päätasonSuoritus.suoritus.creativityActionService
  ) {
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

type PreIB2019TiedotRowsProps = {
  form: FormModel<IBOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<IBOpiskeluoikeus, PreIBSuoritus2019>
}

const PreIB2019TiedotRows: React.FC<PreIB2019TiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  return (
    <>
      <PreIB2019OmanÄidinkielenOpinnotRows
        form={form}
        päätasonSuoritus={päätasonSuoritus}
      />
      <PreIB2019PuhviKoeRows form={form} päätasonSuoritus={päätasonSuoritus} />
      <PreIB2019SuullisenKielitaidonKokeetRows
        form={form}
        päätasonSuoritus={päätasonSuoritus}
      />
    </>
  )
}

const PreIB2019OmanÄidinkielenOpinnotRows: React.FC<
  PreIB2019TiedotRowsProps
> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path
  const omanÄidinkielenOpinnotEiSyötetty =
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot === undefined
  const onOsasuorituksia =
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset &&
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset.length > 0
  const [
    showLisääOmanÄidinkielenOpinnotModal,
    setShowLisääOmanÄidinkielenOpinnotModal
  ] = useState(false)
  const [showLisääKurssiModal, setShowLisääKurssiModal] = useState(false)
  const removeOmanÄidinkielenOpinnot = () => {
    form.set(
      ...päätasonSuoritus.pathTokens,
      ...['omanÄidinkielenOpinnot']
    )(undefined)
  }

  if (!form.editMode && omanÄidinkielenOpinnotEiSyötetty) {
    return null
  }

  return form.editMode && omanÄidinkielenOpinnotEiSyötetty ? (
    <KeyValueRow localizableLabel="Oman äidinkielen opinnot">
      <FlatButton onClick={() => setShowLisääOmanÄidinkielenOpinnotModal(true)}>
        {t('Lisää täydentävät oman äidinkielen opinnot')}
      </FlatButton>
      {showLisääOmanÄidinkielenOpinnotModal && (
        <NewOmanÄidinkielenOpinnotModal
          onClose={() => setShowLisääOmanÄidinkielenOpinnotModal(false)}
          onSubmit={(arvosana, kieli, laajuus, arviointipäivä) =>
            form.set(
              ...päätasonSuoritus.pathTokens,
              ...['omanÄidinkielenOpinnot']
            )(
              createLukionOmanÄidinkielenOpinnot(
                arvosana,
                kieli,
                laajuus,
                arviointipäivä
              )
            )
          }
        />
      )}
    </KeyValueRow>
  ) : (
    <KeyValueRow localizableLabel="Oman äidinkielen opinnot">
      <Removable
        isRemovable={form.editMode}
        onClick={removeOmanÄidinkielenOpinnot}
      >
        <KeyValueTable>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            {form.editMode ? (
              <KoodistoSelect
                koodistoUri={'arviointiasteikkoyleissivistava'}
                format={(koodiviite) =>
                  koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
                }
                onSelect={(koodiviite) => {
                  koodiviite &&
                    form.set(
                      ...päätasonSuoritus.pathTokens,
                      ...['omanÄidinkielenOpinnot', 'arvosana']
                    )(koodiviite)
                }}
                value={
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.arvosana
                    .koodiarvo
                }
                testId="omanÄidinkielenOpinnot.arvosana"
              />
            ) : (
              <TestIdText id="omanÄidinkielenOpinnot.arvosana">
                {
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.arvosana
                    .koodiarvo
                }{' '}
                {t(
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.arvosana
                    .nimi
                )}
              </TestIdText>
            )}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
            <FormField
              form={form}
              testId="omanÄidinkielenOpinnot.arviointipäivä"
              view={DateView}
              edit={DateEdit}
              editProps={{ align: 'right' }}
              path={path
                .prop('omanÄidinkielenOpinnot')
                .optional()
                .prop('arviointipäivä')}
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
            {form.editMode ? (
              <KoodistoSelect
                koodistoUri={'kielivalikoima'}
                onSelect={(koodiviite) => {
                  koodiviite &&
                    form.set(
                      ...päätasonSuoritus.pathTokens,
                      ...['omanÄidinkielenOpinnot', 'kieli']
                    )(koodiviite)
                }}
                value={
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.kieli
                    .koodiarvo
                }
                testId="omanÄidinkielenOpinnot.kieli"
              />
            ) : (
              <TestIdText id="omanÄidinkielenOpinnot.kieli">
                {t(
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.kieli.nimi
                )}
              </TestIdText>
            )}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
            <FormField
              form={form}
              path={path
                .prop('omanÄidinkielenOpinnot')
                .optional()
                .prop('laajuus')}
              view={LaajuusView}
              edit={LaajuusOpintopisteissäEdit}
              testId="omanÄidinkielenOpinnot.laajuus"
            />
          </KeyValueRow>
          {onOsasuorituksia && (
            <KeyValueRow localizableLabel={'Osasuoritukset'} innerKeyValueTable>
              <OmanÄidinkielenOpintojenKurssit
                form={form}
                päätasonSuoritus={päätasonSuoritus}
              />
            </KeyValueRow>
          )}
          {form.editMode && (
            <KeyValueRow innerKeyValueTable>
              <FlatButton onClick={() => setShowLisääKurssiModal(true)}>
                {t('Lisää osasuoritus')}
              </FlatButton>
              <Spacer />
            </KeyValueRow>
          )}
          {showLisääKurssiModal && (
            <NewOmanÄidinkielenOpintojenKurssiModal
              olemassaOlevatModuulit={
                päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset?.map(
                  (os) => os.koulutusmoduuli.tunniste.koodiarvo
                ) || []
              }
              onClose={() => setShowLisääKurssiModal(false)}
              onSubmit={(koulutusmoduuli, arvosana, arviointipäivä, kieli) =>
                pipe(
                  päätasonSuoritus.suoritus.omanÄidinkielenOpinnot
                    ?.osasuoritukset || [],
                  A.append(
                    createLukionOmanÄidinkielenOpinnotOsasuoritus(
                      koulutusmoduuli,
                      arvosana,
                      arviointipäivä,
                      kieli
                    )
                  ),
                  (osasuoritukset) =>
                    form.set(
                      ...päätasonSuoritus.pathTokens,
                      ...['omanÄidinkielenOpinnot', 'osasuoritukset']
                    )(osasuoritukset)
                )
              }
            />
          )}
        </KeyValueTable>
      </Removable>
    </KeyValueRow>
  )
}

const OmanÄidinkielenOpintojenKurssit: React.FC<PreIB2019TiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const osasuoritukset =
    päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset || []
  const osasuorituksetSorted = useMemo(() => {
    return A.sort(osasuoritusOrd)(osasuoritukset)
  }, [osasuoritukset])

  return (
    <table className="OppiaineTable">
      <thead>
        <tr>
          <th></th>
          <th>{t('Kurssit')}</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td className="OppiaineRow__icon">
            {päätasonSuoritus.suoritus.omanÄidinkielenOpinnot && (
              <SuorituksenTilaIcon
                suoritus={päätasonSuoritus.suoritus.omanÄidinkielenOpinnot}
              />
            )}
          </td>
          <td className="OppiaineRow__oppiaine">
            <div className="OppiaineRow__kurssit">
              {osasuorituksetSorted.map((os) => {
                const index = osasuoritukset.indexOf(os)
                const osasuoritusId = `omanÄidinkielenOpinnot-${os.koulutusmoduuli.tunniste.koodiarvo}-${index}`
                return (
                  <OmanÄidinkielenOpintojenKurssi
                    form={form}
                    päätasonSuoritus={päätasonSuoritus}
                    osasuoritus={os}
                    osasuoritusPath={[
                      ...päätasonSuoritus.pathTokens,
                      'omanÄidinkielenOpinnot',
                      'osasuoritukset',
                      index
                    ]}
                    index={index}
                    tooltipId={osasuoritusId}
                    key={osasuoritusId}
                  />
                )
              })}
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  )
}

type OmanÄidinkielenOpintojenKurssiProps = PreIB2019TiedotRowsProps & {
  osasuoritus: LukionOmanÄidinkielenOpintojenOsasuoritus
  osasuoritusPath: PathToken[]
  index: number
  tooltipId: string
}

const OmanÄidinkielenOpintojenKurssi: React.FC<
  OmanÄidinkielenOpintojenKurssiProps
> = ({
  form,
  päätasonSuoritus,
  osasuoritus,
  osasuoritusPath,
  index,
  tooltipId
}) => {
  const [tooltipVisible, openTooltip, closeTooltip] = useBooleanState(false)
  const [editModalVisible, openEditModal, closeEditModal] =
    useBooleanState(false)

  return (
    <div className="Kurssi">
      <button
        className={`Kurssi__tunniste${form.editMode ? ' Kurssi__clickable' : ''}`}
        onClick={form.editMode ? openEditModal : undefined}
        onTouchStart={openTooltip}
        onMouseEnter={openTooltip}
        onMouseLeave={closeTooltip}
        onFocus={openTooltip}
        onBlur={closeTooltip}
        aria-describedby={tooltipId}
      >
        <TestIdText id={`omanÄidinkielenOpinnot.${index}.osasuoritus`}>
          {osasuoritus.koulutusmoduuli.tunniste.koodiarvo}
        </TestIdText>
      </button>
      {form.editMode && (
        <IconButton
          charCode={CHARCODE_REMOVE}
          label={t('Poista')}
          size="input"
          onClick={removeAt(
            päätasonSuoritus.suoritus.omanÄidinkielenOpinnot?.osasuoritukset,
            form,
            [
              ...päätasonSuoritus.pathTokens,
              ...['omanÄidinkielenOpinnot', 'osasuoritukset']
            ],
            index
          )}
          testId="delete"
        />
      )}
      <div className="Kurssi__arvosana">
        <TestIdText id={`omanÄidinkielenOpinnot.${index}.arvosana`}>
          {osasuoritus.arviointi
            ? parasArviointi(osasuoritus.arviointi as Arviointi[])?.arvosana
                .koodiarvo
            : '–'}
        </TestIdText>
      </div>
      {tooltipVisible && (
        <Details id={tooltipId}>
          <KeyValueTable>
            <KeyValueRow localizableLabel="Nimi">
              {t(osasuoritus.koulutusmoduuli.tunniste.nimi)}
            </KeyValueRow>
            <KeyValueRow localizableLabel="Laajuus">
              {osasuoritus.koulutusmoduuli.laajuus?.arvo}{' '}
              {t(osasuoritus.koulutusmoduuli.laajuus?.yksikkö.nimi)}
            </KeyValueRow>
            <KeyValueRow localizableLabel="Suorituskieli">
              {t(osasuoritus.suorituskieli?.nimi)}
            </KeyValueRow>
            {osasuoritus.arviointi && (
              <KeyValueRow localizableLabel="Arviointi">
                {osasuoritus.arviointi.map((arviointi, arviointiIndex) => (
                  <KeyValueTable key={arviointiIndex}>
                    <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                      {`${arviointi.arvosana.koodiarvo} (${t(arviointi.arvosana.nimi)})`}
                    </KeyValueRow>
                    <KeyValueRow
                      localizableLabel="Arviointipäivä"
                      innerKeyValueTable
                    >
                      {ISO2FinnishDate(arviointi.päivä)}
                    </KeyValueRow>
                  </KeyValueTable>
                ))}
              </KeyValueRow>
            )}
            {osasuoritus.tunnustettu && (
              <KeyValueRow localizableLabel="Tunnustettu">
                <OsaamisenTunnustusView
                  value={osasuoritus.tunnustettu}
                  index={index}
                />
              </KeyValueRow>
            )}
          </KeyValueTable>
        </Details>
      )}
      {editModalVisible && (
        <EditOmanÄidinkielenOpintojenOsasuoritusModal
          form={form}
          osasuoritus={osasuoritus}
          osasuoritusPath={osasuoritusPath}
          onClose={closeEditModal}
        />
      )}
    </div>
  )
}

const PreIB2019PuhviKoeRows: React.FC<PreIB2019TiedotRowsProps> = ({
  form,
  päätasonSuoritus
}) => {
  const path = päätasonSuoritus.path
  const puhviKoeEiSyötetty = päätasonSuoritus.suoritus.puhviKoe === undefined
  const [showModal, setShowModal] = useState(false)
  const removePuhviKoe = () => {
    form.set(...päätasonSuoritus.pathTokens, ...['puhviKoe'])(undefined)
  }

  if (!form.editMode && puhviKoeEiSyötetty) {
    return null
  }
  return form.editMode && puhviKoeEiSyötetty ? (
    <KeyValueRow localizableLabel="Toisen asteen puheviestintätaitojen päättökoe">
      <FlatButton onClick={() => setShowModal(true)}>
        {t('Lisää toisen asteen puheviestintätaitojen päättökoe')}
      </FlatButton>
      {showModal && (
        <NewPuhviKoeModal
          onClose={() => setShowModal(false)}
          onSubmit={(arvosana, päivä) =>
            form.set(
              ...päätasonSuoritus.pathTokens,
              ...['puhviKoe']
            )(createPuhviKoe2019(arvosana, päivä))
          }
        />
      )}
    </KeyValueRow>
  ) : (
    <KeyValueRow localizableLabel="Toisen asteen puheviestintätaitojen päättökoe">
      <Removable isRemovable={form.editMode} onClick={removePuhviKoe}>
        <KeyValueTable>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            {form.editMode ? (
              <KoodistoSelect
                koodistoUri={'arviointiasteikkoyleissivistava'}
                format={(koodiviite) =>
                  koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
                }
                filter={(koodiviite) => koodiviite.koodiarvo !== 'O'}
                onSelect={(koodiviite) => {
                  koodiviite &&
                    form.set(
                      ...päätasonSuoritus.pathTokens,
                      ...['puhviKoe', 'arvosana']
                    )(koodiviite)
                }}
                value={päätasonSuoritus.suoritus.puhviKoe?.arvosana.koodiarvo}
                testId="puhviKoe.arvosana"
              />
            ) : (
              <TestIdText id="puhviKoe.arvosana">
                {päätasonSuoritus.suoritus.puhviKoe?.arvosana.koodiarvo}{' '}
                {t(päätasonSuoritus.suoritus.puhviKoe?.arvosana.nimi)}
              </TestIdText>
            )}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
            <FormField
              form={form}
              view={LocalizedTextView}
              edit={LocalizedTextEdit}
              testId="puhviKoe.kuvaus"
              path={path.prop('puhviKoe').optional().prop('kuvaus')}
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Päivä" innerKeyValueTable>
            <FormField
              form={form}
              testId="puhviKoe.päivä"
              view={DateView}
              edit={DateEdit}
              editProps={{ align: 'right' }}
              path={path.prop('puhviKoe').optional().prop('päivä')}
            />
          </KeyValueRow>
        </KeyValueTable>
      </Removable>
    </KeyValueRow>
  )
}

const PreIB2019SuullisenKielitaidonKokeetRows: React.FC<
  PreIB2019TiedotRowsProps
> = ({ form, päätasonSuoritus }) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <KeyValueRow localizableLabel="Suullisen kielitaidon kokeet">
      {päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.map(
        (koe, index) => {
          return (
            <Removable
              isRemovable={form.editMode}
              onClick={removeAt(
                päätasonSuoritus.suoritus.suullisenKielitaidonKokeet,
                form,
                [
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet']
                ],
                index
              )}
              key={`suullisenkielitaidonkoe.${index}`}
            >
              <PreIB2019SuullisenKielitaidonKoeRows
                form={form}
                päätasonSuoritus={päätasonSuoritus}
                index={index}
              />
              <Spacer />
            </Removable>
          )
        }
      )}
      {form.editMode && (
        <>
          <FlatButton onClick={() => setShowModal(true)}>
            {t('Lisää suullisen kielitaidon koe')}
          </FlatButton>
          <Spacer />
        </>
      )}
      {showModal && (
        <NewSuullisenKielitaidonKoeModal
          onClose={() => setShowModal(false)}
          onSubmit={(kieli, arvosana, taitotaso, päivä) =>
            pipe(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet || [],
              A.append(
                createSuullisenKielitaidonKoe2019(
                  kieli,
                  arvosana,
                  taitotaso,
                  päivä
                )
              ),
              (kokeet) =>
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet']
                )(kokeet)
            )
          }
        />
      )}
    </KeyValueRow>
  )
}

type PreIB2019SuullisenKielitaidonKoeRowsProps = PreIB2019TiedotRowsProps & {
  index: number
}

const PreIB2019SuullisenKielitaidonKoeRows: React.FC<
  PreIB2019SuullisenKielitaidonKoeRowsProps
> = ({ form, päätasonSuoritus, index }) => {
  const path = päätasonSuoritus.path

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri={'kielivalikoima'}
            onSelect={(koodiviite) => {
              koodiviite &&
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet', index, 'kieli']
                )(koodiviite)
            }}
            value={
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.kieli.koodiarvo
            }
            testId={`suullisenKielitaidonKokeet.${index}.kieli`}
          />
        ) : (
          <TestIdText id={`suullisenKielitaidonKokeet.${index}.kieli`}>
            {t(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.kieli.nimi
            )}
          </TestIdText>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            filter={(koodiviite) => koodiviite.koodiarvo !== 'O'}
            onSelect={(koodiviite) => {
              koodiviite &&
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet', index, 'arvosana']
                )(koodiviite)
            }}
            value={
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.arvosana.koodiarvo
            }
            testId={`suullisenKielitaidonKokeet.${index}.arvosana`}
          />
        ) : (
          <TestIdText id={`suullisenKielitaidonKokeet.${index}.arvosana`}>
            {
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.arvosana.koodiarvo
            }{' '}
            {t(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.arvosana.nimi
            )}
          </TestIdText>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Taitotaso" innerKeyValueTable>
        {form.editMode ? (
          <KoodistoSelect
            koodistoUri={'arviointiasteikkokehittyvankielitaidontasot'}
            filter={(koodiviite) =>
              preIB2019SuullisenKielitaidonTaitotasot.includes(
                koodiviite.koodiarvo
              )
            }
            onSelect={(koodiviite) => {
              koodiviite &&
                form.set(
                  ...päätasonSuoritus.pathTokens,
                  ...['suullisenKielitaidonKokeet', index, 'taitotaso']
                )(koodiviite)
            }}
            value={
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.taitotaso.koodiarvo
            }
            testId={`suullisenKielitaidonKokeet.${index}.taitotaso`}
          />
        ) : (
          <TestIdText id={`suullisenKielitaidonKokeet.${index}.taitotaso`}>
            {t(
              päätasonSuoritus.suoritus.suullisenKielitaidonKokeet?.at(index)
                ?.taitotaso.nimi
            )}
          </TestIdText>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Kuvaus" innerKeyValueTable>
        <FormField
          form={form}
          view={LocalizedTextView}
          edit={LocalizedTextEdit}
          testId={`suullisenKielitaidonKokeet.${index}.kuvaus`}
          path={path
            .prop('suullisenKielitaidonKokeet')
            .optional()
            .at(index)
            .prop('kuvaus')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Päivä" innerKeyValueTable>
        <FormField
          form={form}
          testId={`suullisenKielitaidonKokeet.${index}.päivä`}
          view={DateView}
          edit={DateEdit}
          editProps={{ align: 'right' }}
          path={path
            .prop('suullisenKielitaidonKokeet')
            .optional()
            .at(index)
            .prop('päivä')}
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}

type NewOmanÄidinkielenOpinnotModalProps = {
  onClose: () => void
  onSubmit: (
    arvosana: PreIBOmanÄidinkielenOpinnot2019Arvosana,
    kieli: Koodistokoodiviite<'kielivalikoima'>,
    laajuus: LaajuusOpintopisteissä,
    arviointipäivä?: string
  ) => void
}

const NewOmanÄidinkielenOpinnotModal = ({
  onClose,
  onSubmit
}: NewOmanÄidinkielenOpinnotModalProps) => {
  const [arvosana, setArvosana] = useState<
    PreIBOmanÄidinkielenOpinnot2019Arvosana | undefined
  >(undefined)
  const [arviointipäivä, setArviointipäivä] = useState<string | undefined>(
    undefined
  )
  const [kieli, setKieli] = useState<
    Koodistokoodiviite<'kielivalikoima'> | undefined
  >(undefined)
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {t('Täydentävien oman äidinkielen opintojen lisäys')}
      </ModalTitle>
      <ModalBody>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            onSelect={(koodiviite) => {
              koodiviite &&
                setArvosana(
                  koodiviite as PreIBOmanÄidinkielenOpinnot2019Arvosana
                )
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'omanÄidinkielenOpinnot.modal.arvosana'}
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={arviointipäivä}
            onChange={(date) => setArviointipäivä(date)}
            testId={'omanÄidinkielenOpinnot.modal.päivä'}
          />
        </label>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri={'kielivalikoima'}
            onSelect={(koodiviite) => {
              koodiviite && setKieli(koodiviite)
            }}
            value={kieli ? kieli.koodiarvo : undefined}
            testId={'omanÄidinkielenOpinnot.modal.kieli'}
          />
        </label>
        <label>
          {t('Laajuus')}
          <LaajuusEdit
            value={laajuus}
            onChange={(value) => setLaajuus(value)}
            createLaajuus={(value) => LaajuusOpintopisteissä({ arvo: value })}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={[arvosana, kieli, laajuus].includes(undefined)}
          onClick={() => {
            if (
              arvosana !== undefined &&
              kieli !== undefined &&
              laajuus !== undefined
            ) {
              onSubmit(arvosana, kieli, laajuus, arviointipäivä)
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää täydentävät oman äidinkielen opinnot')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type NewOmanÄidinkielenOpintojenKurssiModalProps = {
  olemassaOlevatModuulit: string[]
  onClose: () => void
  onSubmit: (
    koulutusmoduuli: LukionOmanÄidinkielenOpinto,
    arvosana?: PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana,
    arviointipäivä?: string,
    kieli?: Koodistokoodiviite<'kieli'>
  ) => void
}

const NewOmanÄidinkielenOpintojenKurssiModal = ({
  olemassaOlevatModuulit,
  onClose,
  onSubmit
}: NewOmanÄidinkielenOpintojenKurssiModalProps) => {
  const omanÄidinkielenKurssit =
    useKoodistoOfConstraint(
      useChildSchema(LukionOmanÄidinkielenOpinto.className, 'tunniste')
    ) || []
  const omanÄidinkielenKurssinArvosanat =
    useKoodistoOfConstraint(
      useChildSchema(
        LukionOmanÄidinkielenOpintojenOsasuoritus.className,
        'arviointi.[].arvosana'
      )
    ) || []

  const [kurssi, setKurssi] = useState<PreIBOmanÄidinkielenOpinto | undefined>(
    undefined
  )
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )
  const [kieli, setKieli] = useState<Koodistokoodiviite<'kieli'> | undefined>(
    undefined
  )
  const [arvosana, setArvosana] = useState<
    PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana | undefined
  >(undefined)
  const [arviointipäivä, setArviointipäivä] = useState<string | undefined>(
    undefined
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Kurssi')}
          <KoodistoSelect
            koodistoUri={'moduulikoodistolops2021'}
            koodiarvot={omanÄidinkielenKurssit.map(
              (m) => m.koodiviite.koodiarvo
            )}
            filter={(koodiviite) =>
              !olemassaOlevatModuulit.includes(koodiviite.koodiarvo)
            }
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            onSelect={(koodiviite) => {
              koodiviite && setKurssi(koodiviite as PreIBOmanÄidinkielenOpinto)
            }}
            value={kurssi ? kurssi.koodiarvo : undefined}
            testId={'omanÄidinkielenOpinnot.kurssimodal.moduuli'}
          />
        </label>
        <label>
          {t('Laajuus')}
          <LaajuusEdit
            value={laajuus}
            onChange={(value) => setLaajuus(value)}
            createLaajuus={(value) => LaajuusOpintopisteissä({ arvo: value })}
          />
        </label>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            koodiarvot={omanÄidinkielenKurssinArvosanat.map(
              (m) => m.koodiviite.koodiarvo
            )}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            onSelect={(koodiviite) => {
              koodiviite &&
                setArvosana(
                  koodiviite as PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana
                )
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'omanÄidinkielenOpinnot.kurssimodal.arvosana'}
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={arviointipäivä}
            onChange={(date) => setArviointipäivä(date)}
            testId={'omanÄidinkielenOpinnot.kurssimodal.päivä'}
          />
        </label>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri={'kieli'}
            onSelect={(koodiviite) => {
              koodiviite && setKieli(koodiviite)
            }}
            value={kieli ? kieli.koodiarvo : undefined}
            testId={'omanÄidinkielenOpinnot.kurssimodal.kieli'}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={
            [kurssi, laajuus].includes(undefined) ||
            (!!arvosana && !arviointipäivä) ||
            (!arvosana && !!arviointipäivä)
          }
          onClick={() => {
            if (kurssi !== undefined && laajuus !== undefined) {
              onSubmit(
                LukionOmanÄidinkielenOpinto({ tunniste: kurssi, laajuus }),
                arvosana,
                arviointipäivä,
                kieli
              )
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type EditOmanÄidinkielenOpintojenOsasuoritusModalProps = {
  form: FormModel<IBOpiskeluoikeus>
  osasuoritus: LukionOmanÄidinkielenOpintojenOsasuoritus
  osasuoritusPath: PathToken[]
  onClose: () => void
}

const EditOmanÄidinkielenOpintojenOsasuoritusModal = ({
  form,
  osasuoritus,
  osasuoritusPath,
  onClose
}: EditOmanÄidinkielenOpintojenOsasuoritusModalProps) => {
  return (
    <Modal onClose={onClose}>
      <ModalTitle>{`${osasuoritus.koulutusmoduuli.tunniste.koodiarvo} ${t(osasuoritus.koulutusmoduuli.tunniste.nimi)}`}</ModalTitle>
      <ModalBody>
        <KeyValueRow localizableLabel="Laajuus">
          <LaajuusEdit
            value={osasuoritus.koulutusmoduuli.laajuus}
            onChange={form.set(
              ...osasuoritusPath,
              ...['koulutusmoduuli', 'laajuus']
            )}
            createLaajuus={(value) => LaajuusOpintopisteissä({ arvo: value })}
          />
        </KeyValueRow>
        {(osasuoritus.arviointi || []).map((arviointi, index) => (
          <div
            key={`${osasuoritus.koulutusmoduuli.tunniste.koodiarvo}.arviointi.${index}`}
          >
            <KeyValueRow
              localizableLabel="Arvosana"
              innerKeyValueTable={(osasuoritus.arviointi || []).length > 1}
            >
              <ArvosanaEdit
                suoritusClassName={osasuoritus.$class}
                value={arviointi}
                onChange={form.set(...osasuoritusPath, ...['arviointi', index])}
              />
            </KeyValueRow>
            <KeyValueRow
              localizableLabel="Arviointipäivä"
              innerKeyValueTable={(osasuoritus.arviointi || []).length > 1}
            >
              <DateEdit
                value={arviointi.päivä}
                onChange={form.set(
                  ...osasuoritusPath,
                  ...['arviointi', index, 'päivä']
                )}
                align="right"
              />
            </KeyValueRow>
          </div>
        ))}
        <KeyValueRow localizableLabel="Suorituskieli">
          <KoodistoSelect
            inlineOptions
            koodistoUri="kieli"
            onSelect={form.set(...osasuoritusPath, 'suorituskieli')}
            value={osasuoritus.suorituskieli?.koodiarvo}
            testId="suorituskieli"
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel="Osaamisen tunnustaminen">
          {osasuoritus.tunnustettu ? (
            <fieldset>
              <LocalizedTextEdit
                large
                value={osasuoritus.tunnustettu.selite}
                onChange={form.set(...osasuoritusPath, 'tunnustettu', 'selite')}
              />
              <Checkbox
                checked={osasuoritus.tunnustettu.rahoituksenPiirissä}
                onChange={form.set(
                  ...osasuoritusPath,
                  'tunnustettu',
                  'rahoituksenPiirissä'
                )}
                label="Rahoituksen piirissä"
                testId="rahoituksenPiirissä"
              />
            </fieldset>
          ) : (
            <FlatButton
              onClick={() =>
                form.set(
                  ...osasuoritusPath,
                  'tunnustettu'
                )(
                  OsaamisenTunnustaminen({
                    selite: emptyLocalizedString
                  })
                )
              }
            >
              {t('Lisää osaamisen tunnustaminen')}
            </FlatButton>
          )}
        </KeyValueRow>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <RaisedButton onClick={onClose} testId="confirm">
          {t('Sulje')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type NewPuhviKoeModalProps = {
  onClose: () => void
  onSubmit: (arvosana: PreIBPuhviKoe2019Arvosana, päivä: string) => void
}

const NewPuhviKoeModal = ({ onClose, onSubmit }: NewPuhviKoeModalProps) => {
  const [arvosana, setArvosana] = useState<
    PreIBPuhviKoe2019Arvosana | undefined
  >(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {t('Toisen asteen puheviestintätaitojen päättökokeen lisäys')}
      </ModalTitle>
      <ModalBody>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            filter={(koodiviite) => koodiviite.koodiarvo !== 'O'}
            onSelect={(koodiviite) => {
              koodiviite && setArvosana(koodiviite as PreIBPuhviKoe2019Arvosana)
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'puhviKoe.modal.arvosana'}
          />
        </label>
        <label>
          {t('Päivä')}
          <DateInput
            value={päivä}
            onChange={(date) => setPäivä(date)}
            testId={'puhviKoe.modal.päivä'}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={[arvosana, päivä].includes(undefined)}
          onClick={() => {
            if (arvosana !== undefined && päivä !== undefined) {
              onSubmit(arvosana, päivä)
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää toisen asteen puheviestintätaitojen päättökoe')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

type NewSuullisenKielitaidonKoeModalProps = {
  onClose: () => void
  onSubmit: (
    kieli: Koodistokoodiviite<'kielivalikoima'>,
    arvosana: PreIBSuullisenKielitaidonKoe2019Arvosana,
    taitotaso: PreIBSuullisenKielitaidonKoe2019Taitotaso,
    päivä: string
  ) => void
}

const NewSuullisenKielitaidonKoeModal = ({
  onClose,
  onSubmit
}: NewSuullisenKielitaidonKoeModalProps) => {
  const [kieli, setKieli] = useState<
    Koodistokoodiviite<'kielivalikoima'> | undefined
  >(undefined)
  const [arvosana, setArvosana] = useState<
    PreIBSuullisenKielitaidonKoe2019Arvosana | undefined
  >(undefined)
  const [taitotaso, setTaitotaso] = useState<
    PreIBSuullisenKielitaidonKoe2019Taitotaso | undefined
  >(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Suullisen kielitaidon kokeen lisäys')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri={'kielivalikoima'}
            onSelect={(koodiviite) => {
              koodiviite && setKieli(koodiviite)
            }}
            value={kieli ? kieli.koodiarvo : undefined}
            testId={'suullisenKielitaidonKokeet.modal.kieli'}
          />
        </label>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            filter={(koodiviite) => koodiviite.koodiarvo !== 'O'}
            onSelect={(koodiviite) => {
              koodiviite &&
                setArvosana(
                  koodiviite as PreIBSuullisenKielitaidonKoe2019Arvosana
                )
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'suullisenKielitaidonKokeet.modal.arvosana'}
          />
        </label>
        <label>
          {t('Taitotaso')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkokehittyvankielitaidontasot'}
            filter={(koodiviite) =>
              preIB2019SuullisenKielitaidonTaitotasot.includes(
                koodiviite.koodiarvo
              )
            }
            onSelect={(koodiviite) => {
              koodiviite &&
                setTaitotaso(
                  koodiviite as PreIBSuullisenKielitaidonKoe2019Taitotaso
                )
            }}
            value={taitotaso ? taitotaso.koodiarvo : undefined}
            testId={'suullisenKielitaidonKokeet.modal.taitotaso'}
          />
        </label>
        <label>
          {t('Päivä')}
          <DateInput
            value={päivä}
            onChange={(date) => setPäivä(date)}
            testId={'suullisenKielitaidonKokeet.modal.päivä'}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={[kieli, arvosana, taitotaso, päivä].includes(undefined)}
          onClick={() => {
            if (
              kieli !== undefined &&
              arvosana !== undefined &&
              taitotaso !== undefined &&
              päivä !== undefined
            ) {
              onSubmit(kieli, arvosana, taitotaso, päivä)
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää suullisen kielitaidon koe')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

const createSuullisenKielitaidonKoe2019 = (
  kieli: Koodistokoodiviite<'kielivalikoima'>,
  arvosana: PreIBSuullisenKielitaidonKoe2019Arvosana,
  taitotaso: PreIBSuullisenKielitaidonKoe2019Taitotaso,
  päivä: string
): SuullisenKielitaidonKoe2019 => {
  return SuullisenKielitaidonKoe2019({ päivä, arvosana, taitotaso, kieli })
}

const createLukionOmanÄidinkielenOpinnot = (
  arvosana: PreIBOmanÄidinkielenOpinnot2019Arvosana,
  kieli: Koodistokoodiviite<'kielivalikoima'>,
  laajuus: LaajuusOpintopisteissä,
  arviointipäivä?: string
): LukionOmanÄidinkielenOpinnot => {
  return LukionOmanÄidinkielenOpinnot({
    arvosana,
    arviointipäivä: arviointipäivä,
    laajuus,
    kieli
  })
}

const createLukionOmanÄidinkielenOpinnotOsasuoritus = (
  koulutusmoduuli: LukionOmanÄidinkielenOpinto,
  arvosana?: PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana,
  arviointipäivä?: string,
  kieli?: Koodistokoodiviite<'kieli'>
): LukionOmanÄidinkielenOpintojenOsasuoritus => {
  return LukionOmanÄidinkielenOpintojenOsasuoritus({
    koulutusmoduuli,
    arviointi:
      arvosana && arviointipäivä
        ? [
            LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi({
              arvosana,
              päivä: arviointipäivä
            })
          ]
        : undefined,
    suorituskieli: kieli
  })
}

const createPuhviKoe2019 = (
  arvosana: PreIBPuhviKoe2019Arvosana,
  päivä: string
): PuhviKoe2019 => {
  return PuhviKoe2019({
    arvosana,
    päivä
  })
}

const removeAt =
  (
    arr: any[] | undefined,
    form: FormModel<IBOpiskeluoikeus>,
    path: PathToken[],
    index: number
  ) =>
  () => {
    pipe(
      arr || [],
      A.deleteAt(index),
      O.fold(
        () =>
          console.error(`Could not remove at ${index}, original array:`, arr),
        (newArray) => form.set(...path)(newArray)
      )
    )
  }

const osasuoritusOrd = Ord.contramap<
  string,
  LukionOmanÄidinkielenOpintojenOsasuoritus
>((osasuoritus: LukionOmanÄidinkielenOpintojenOsasuoritus) => {
  return osasuoritus.koulutusmoduuli.tunniste.koodiarvo
})(S.Ord)

const coreOppiaineidenTietomallinMuuttumisenRajapäivä =
  config().rajapäivät.ibLaajuusOpintopisteinäAlkaen

const dpOppiaineetOsasuorituksina = (alkupäivä?: string): boolean =>
  (alkupäivä || '') >= coreOppiaineidenTietomallinMuuttumisenRajapäivä
