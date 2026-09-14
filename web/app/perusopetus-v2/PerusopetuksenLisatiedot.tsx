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
import { Tukijakso } from '../types/fi/oph/koski/schema/Tukijakso'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import { AikajaksoArrayRow } from '../components-v2/opiskeluoikeus/AikajaksoArrayRow'
import {
  uusiErityisenTuenPäätös,
  uusiTukijakso
} from '../components-v2/opiskeluoikeus/uusiJakso'
import {
  BooleanView,
  BooleanEdit
} from '../components-v2/opiskeluoikeus/BooleanField'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { SingleAikajaksoRow } from '../components-v2/opiskeluoikeus/SingleAikajaksoRow'
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
        path={lisatiedotPath.prop('pidennettyOppivelvollisuus')}
        label="Pidennetty oppivelvollisuus"
        testId="pidennettyOppivelvollisuus"
      />

      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop(
          'opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella'
        )}
        label="Opetuksen järjestäminen vamman sairauden tai rajoitteen perusteella"
        testId="opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella"
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('toimintaAlueittainOpiskelu')}
        label="Opetus toiminta-alueittain vamman sairauden tai rajoitteen perusteella"
        testId="toimintaAlueittainOpiskelu"
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('tavoitekokonaisuuksittainOpiskelu')}
        label="Tavoitekokonaisuuksittain opiskelu"
        testId="tavoitekokonaisuuksittainOpiskelu"
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('yhdysluokka')}
        label="Yhdysluokka"
        testId="yhdysluokka"
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
        path={lisatiedotPath.prop('joustavaPerusopetus')}
        label="Joustava perusopetus"
        testId="joustavaPerusopetus"
      />
      <SingleAikajaksoRow
        form={form}
        path={lisatiedotPath.prop('kotiopetus')}
        label="Kotiopetus"
        testId="kotiopetus"
        deprecated
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('kotiopetusjaksot')}
        label="Kotiopetusjaksot"
        testId="kotiopetusjaksot"
      />
      <SingleAikajaksoRow
        form={form}
        path={lisatiedotPath.prop('ulkomailla')}
        label="Ulkomailla"
        testId="ulkomailla"
        deprecated
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('ulkomaanjaksot')}
        label="Ulkomaanjaksot"
        testId="ulkomaanjaksot"
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
        path={lisatiedotPath.prop('vammainen')}
        label="Vammainen"
        testId="vammainen"
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('vaikeastiVammainen')}
        label="Vaikeasti vammainen"
        testId="vaikeastiVammainen"
      />

      <SingleAikajaksoRow
        form={form}
        path={lisatiedotPath.prop('majoitusetu')}
        label="Majoitusetu"
        testId="majoitusetu"
      />
      <SingleAikajaksoRow
        form={form}
        path={lisatiedotPath.prop('kuljetusetu')}
        label="Kuljetusetu"
        testId="kuljetusetu"
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('sisäoppilaitosmainenMajoitus')}
        label="Sisäoppilaitosmainen majoitus"
        testId="sisäoppilaitosmainenMajoitus"
      />
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('koulukoti')}
        label="Koulukoti"
        testId="koulukoti"
      />

      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('valmistavanLisäopetus')}
        label="Valmistavan lisäopetus"
        testId="valmistavanLisäopetus"
      />
    </KeyValueTable>
  )
}

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
                  append(uusiErityisenTuenPäätös())
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
              form.updateAt(jaksotPath.valueOr([]), append(uusiTukijakso()))
            }
          >
            {t('Lisää')}
          </FlatButton>
        </ButtonGroup>
      )}
    </KeyValueRow>
  )
}
