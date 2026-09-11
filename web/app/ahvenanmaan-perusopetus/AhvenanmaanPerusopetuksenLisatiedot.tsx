import React from 'react'
import { TestIdLayer } from '../appstate/useTestId'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { Removable } from '../components-v2/controls/Removable'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../components-v2/forms/FormModel'
import {
  AikajaksoEdit,
  AikajaksoView
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import { uusiTyhjäAikajakso } from '../components-v2/opiskeluoikeus/uusiJakso'
import { t } from '../i18n/i18n'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanAikuistenPerusopetuksenOppimaaranSuoritus'
import { AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import { append } from '../util/fp/arrays'

interface AhvenanmaanPerusopetuksenLisatiedotProps {
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
}

// Ahvenanmaan perusopetuksen lisätiedoissa ovat kotiopetusjaksot ja muille kuin
// oppivelvollisille alkuvaihe.
export const AhvenanmaanPerusopetuksenLisatiedot: React.FC<
  AhvenanmaanPerusopetuksenLisatiedotProps
> = ({ form }) => {
  const emptyLisatiedot = AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)
  // Backend hylkää alkuvaiheen oppivelvollisen opiskeluoikeudelta.
  const muuKuinOppivelvollinen = form.state.suoritukset.some(
    isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
  )

  if (!lisätiedot) return null

  return (
    <KeyValueTable>
      {muuKuinOppivelvollinen && (
        <SingleAikajaksoRow
          form={form}
          lisatiedotPath={lisatiedotPath}
          fieldName="alkuvaihe"
          label="Alkuvaihe"
          value={lisätiedot.alkuvaihe}
        />
      )}
      <AikajaksoArrayRow
        form={form}
        lisatiedotPath={lisatiedotPath}
        fieldName="kotiopetusjaksot"
        label="Kotiopetusjaksot"
      />
    </KeyValueTable>
  )
}

type LisätiedotPath = FormOptic<
  AhvenanmaanPerusopetuksenOpiskeluoikeus,
  AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot
>

const SingleAikajaksoRow: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  fieldName: keyof AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot
  label: string
  value: Aikajakso | undefined
}> = ({ form, lisatiedotPath, fieldName, label, value }) => {
  if (!form.editMode && !value) return null
  const path = lisatiedotPath.prop(fieldName) as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    Aikajakso | undefined
  >
  const field = (
    <FormField
      form={form}
      path={path}
      view={AikajaksoView}
      edit={AikajaksoEdit}
      editProps={{ createAikajakso: Aikajakso }}
    />
  )
  return (
    <KeyValueRow localizableLabel={label} largeLabel>
      <TestIdLayer id={fieldName}>
        {value ? (
          form.editMode ? (
            <Removable onClick={() => form.updateAt(path, () => undefined)}>
              {field}
            </Removable>
          ) : (
            field
          )
        ) : form.editMode ? (
          <ButtonGroup>
            <FlatButton
              onClick={() => form.updateAt(path, () => uusiTyhjäAikajakso())}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        ) : null}
      </TestIdLayer>
    </KeyValueRow>
  )
}

const AikajaksoArrayRow: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  lisatiedotPath: LisätiedotPath
  fieldName: keyof AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot
  label: string
}> = ({ form, lisatiedotPath, fieldName, label }) => {
  const path = lisatiedotPath.prop(fieldName) as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
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
                form.updateAt(path.valueOr([]), append(uusiTyhjäAikajakso()))
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
