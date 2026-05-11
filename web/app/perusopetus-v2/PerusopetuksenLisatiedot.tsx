import React from 'react'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import { PerusopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeudenLisatiedot'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { t } from '../i18n/i18n'
import { todayISODate } from '../date/date'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { Tukijakso } from '../types/fi/oph/koski/schema/Tukijakso'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  AikajaksoView,
  AikajaksoEdit
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import {
  BooleanView,
  BooleanEdit
} from '../components-v2/opiskeluoikeus/BooleanField'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { Removable } from '../components-v2/controls/Removable'
import { append } from '../util/fp/arrays'
import { ErityisenTuenPäätös } from '../types/fi/oph/koski/schema/ErityisenTuenPaatos'
import { TukijaksoView, TukijaksoEdit } from './TukijaksoField'
import {
  ErityisenTuenPäätösView,
  ErityisenTuenPäätösEdit
} from './ErityisenTuenPäätösField'
import { TestIdLayer } from '../appstate/useTestId'

interface PerusopetuksenLisatiedotProps {
  form: FormModel<PerusopetuksenOpiskeluoikeus>
}

export const PerusopetuksenLisatiedot: React.FC<
  PerusopetuksenLisatiedotProps
> = ({ form }) => {
  const emptyLisatiedot = PerusopetuksenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)

  if (!lisätiedot) return null

  return (
    <KeyValueTable>
      <BooleanRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="perusopetuksenAloittamistaLykätty"
        label="Perusopetuksen aloittamista lykätty"
        value={lisätiedot.perusopetuksenAloittamistaLykätty}
        deprecated
      />

      <BooleanRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="aloittanutEnnenOppivelvollisuutta"
        label="Aloittanut ennen oppivelvollisuutta"
        value={lisätiedot.aloittanutEnnenOppivelvollisuutta}
      />

      <SingleAikajaksoRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="pidennettyOppivelvollisuus"
        label="Pidennetty oppivelvollisuus"
        value={lisätiedot.pidennettyOppivelvollisuus}
      />

      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella"
        label="Opetuksen järjestäminen vamman, sairauden tai rajoitteen perusteella"
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="toimintaAlueittainOpiskelu"
        label="Toiminta-alueittain opiskelu"
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="tavoitekokonaisuuksittainOpiskelu"
        label="Tavoitekokonaisuuksittain opiskelu"
      />

      <ErityisenTuenPäätöksetRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        lisätiedot={lisätiedot}
      />

      <TuenPäätöksenJaksotRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        lisätiedot={lisätiedot}
      />

      <SingleAikajaksoRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="joustavaPerusopetus"
        label="Joustava perusopetus"
        value={lisätiedot.joustavaPerusopetus}
      />
      <SingleAikajaksoRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="kotiopetus"
        label="Kotiopetus"
        value={lisätiedot.kotiopetus}
        deprecated
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="kotiopetusjaksot"
        label="Kotiopetusjaksot"
      />
      <SingleAikajaksoRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="ulkomailla"
        label="Ulkomailla"
        value={lisätiedot.ulkomailla}
        deprecated
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="ulkomaanjaksot"
        label="Ulkomaanjaksot"
      />

      <BooleanRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="vuosiluokkiinSitoutumatonOpetus"
        label="Vuosiluokkiin sitoutumaton opetus"
        value={lisätiedot.vuosiluokkiinSitoutumatonOpetus}
      />

      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="vammainen"
        label="Vammainen"
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="vaikeastiVammainen"
        label="Vaikeasti vammainen"
      />

      <SingleAikajaksoRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="majoitusetu"
        label="Majoitusetu"
        value={lisätiedot.majoitusetu}
      />
      <SingleAikajaksoRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="kuljetusetu"
        label="Kuljetusetu"
        value={lisätiedot.kuljetusetu}
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="sisäoppilaitosmainenMajoitus"
        label="Sisäoppilaitosmainen majoitus"
      />
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="koulukoti"
        label="Koulukoti"
      />

      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="valmistavanLisäopetus"
        label="Valmistavan lisäopetus"
      />
    </KeyValueTable>
  )
}

// --- Single aikajakso field (optional, not array) ---

type LisätiedotPath = FormOptic<
  PerusopetuksenOpiskeluoikeus,
  PerusopetuksenOpiskeluoikeudenLisätiedot
>

const hasOldUiValue = (value: unknown): boolean =>
  value !== undefined &&
  value !== null &&
  (!Array.isArray(value) || value.length > 0)

const shouldShowDeprecatedBoolean = (
  value: boolean | undefined,
  editMode: boolean
): boolean => hasOldUiValue(value) && (editMode || value !== false)

const BooleanRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  fieldName: keyof PerusopetuksenOpiskeluoikeudenLisätiedot
  label: string
  value: boolean | undefined
  deprecated?: boolean
}> = ({ form, lisatiedotPath, fieldName, label, value, deprecated }) => {
  if (
    deprecated
      ? !shouldShowDeprecatedBoolean(value, form.editMode)
      : !form.editMode && value !== true
  ) {
    return null
  }

  const path = lisatiedotPath.prop(fieldName) as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    boolean | undefined
  >

  return (
    <KeyValueRow localizableLabel={label} largeLabel>
      <FormField
        form={form}
        view={BooleanView}
        viewProps={{ hideFalse: true }}
        edit={BooleanEdit}
        path={path}
        testId={fieldName}
      />
    </KeyValueRow>
  )
}

const SingleAikajaksoRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  fieldName: keyof PerusopetuksenOpiskeluoikeudenLisätiedot
  label: string
  value: Aikajakso | undefined
  deprecated?: boolean
}> = ({ form, lisatiedotPath, fieldName, label, value, deprecated }) => {
  if (deprecated ? !hasOldUiValue(value) : !form.editMode && !value) return null
  const path = lisatiedotPath.prop(fieldName) as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    Aikajakso | undefined
  >
  return (
    <KeyValueRow localizableLabel={label} largeLabel>
      <TestIdLayer id={fieldName}>
        {value ? (
          form.editMode ? (
            <Removable onClick={() => form.updateAt(path, () => undefined)}>
              <FormField
                form={form}
                path={path}
                view={AikajaksoView}
                edit={AikajaksoEdit}
                editProps={{ createAikajakso: Aikajakso }}
              />
            </Removable>
          ) : (
            <FormField
              form={form}
              path={path}
              view={AikajaksoView}
              edit={AikajaksoEdit}
              editProps={{ createAikajakso: Aikajakso }}
            />
          )
        ) : form.editMode ? (
          <ButtonGroup>
            <FlatButton
              onClick={() => form.updateAt(path, () => Aikajakso({ alku: '' }))}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        ) : null}
      </TestIdLayer>
    </KeyValueRow>
  )
}

// --- Aikajakso array field ---

const AikajaksoArrayRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  fieldName: keyof PerusopetuksenOpiskeluoikeudenLisätiedot
  label: string
}> = ({ form, lisatiedotPath, fieldName, label }) => {
  const path = lisatiedotPath.prop(fieldName) as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    Aikajakso[] | undefined
  >
  const values = getValue(path)(form.state)
  if (!form.editMode && (!values || values.length === 0)) return null

  return (
    <KeyValueRow localizableLabel={label} largeLabel>
      <TestIdLayer id={fieldName}>
        <FormListField
          form={form}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          path={path}
          editProps={{ createAikajakso: Aikajakso }}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() =>
                form.updateAt(
                  path.valueOr([]),
                  append(Aikajakso({ alku: todayISODate() }))
                )
              }
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </TestIdLayer>
    </KeyValueRow>
  )
}

// --- Erityisen tuen päätökset ---

const ErityisenTuenPäätöksetRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot
}> = ({ form, lisatiedotPath, lisätiedot }) => {
  const päätöksetPath = lisatiedotPath.prop(
    'erityisenTuenPäätökset'
  ) as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    ErityisenTuenPäätös[] | undefined
  >
  const singlePath = lisatiedotPath.prop('erityisenTuenPäätös') as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    ErityisenTuenPäätös | undefined
  >

  const allPäätökset: ErityisenTuenPäätös[] = [
    ...(lisätiedot.erityisenTuenPäätös ? [lisätiedot.erityisenTuenPäätös] : []),
    ...(lisätiedot.erityisenTuenPäätökset || [])
  ]

  if (!form.editMode && allPäätökset.length === 0) return null

  return (
    <KeyValueRow localizableLabel="Erityisen tuen päätös" largeLabel>
      {form.editMode ? (
        <>
          {/* Legacy single field */}
          {lisätiedot.erityisenTuenPäätös && (
            <TestIdLayer id="erityisenTuenPäätös">
              <FormField
                form={form}
                path={singlePath}
                view={ErityisenTuenPäätösView}
                edit={ErityisenTuenPäätösEdit}
              />
            </TestIdLayer>
          )}
          {/* Array field */}
          <TestIdLayer id="erityisenTuenPäätökset">
            <FormListField
              form={form}
              path={päätöksetPath}
              view={ErityisenTuenPäätösView}
              edit={ErityisenTuenPäätösEdit}
              removable
            />
          </TestIdLayer>
          <ButtonGroup>
            <FlatButton
              onClick={() =>
                form.updateAt(
                  päätöksetPath.valueOr([]),
                  append(
                    ErityisenTuenPäätös({
                      opiskeleeToimintaAlueittain: false
                    })
                  )
                )
              }
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        </>
      ) : (
        <>
          {lisätiedot.erityisenTuenPäätös && (
            <TestIdLayer id="erityisenTuenPäätös">
              <FormField
                form={form}
                path={singlePath}
                view={ErityisenTuenPäätösView}
                edit={ErityisenTuenPäätösEdit}
              />
            </TestIdLayer>
          )}
          <TestIdLayer id="erityisenTuenPäätökset">
            <FormListField
              form={form}
              path={päätöksetPath}
              view={ErityisenTuenPäätösView}
              edit={ErityisenTuenPäätösEdit}
            />
          </TestIdLayer>
        </>
      )}
    </KeyValueRow>
  )
}

// --- Tuen päätöksen jaksot ---

const TuenPäätöksenJaksotRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot
}> = ({ form, lisatiedotPath, lisätiedot }) => {
  const jaksotPath = lisatiedotPath.prop('tuenPäätöksenJaksot') as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    Tukijakso[] | undefined
  >
  const values = lisätiedot.tuenPäätöksenJaksot

  if (!form.editMode && (!values || values.length === 0)) return null

  return (
    <KeyValueRow localizableLabel="Tuen päätöksen jaksot" largeLabel>
      <FormListField
        form={form}
        path={jaksotPath}
        view={TukijaksoView}
        edit={TukijaksoEdit}
        removable
      />
      {form.editMode && (
        <ButtonGroup>
          <FlatButton
            onClick={() =>
              form.updateAt(jaksotPath.valueOr([]), append(Tukijakso()))
            }
          >
            {t('Lisää')}
          </FlatButton>
        </ButtonGroup>
      )}
    </KeyValueRow>
  )
}
