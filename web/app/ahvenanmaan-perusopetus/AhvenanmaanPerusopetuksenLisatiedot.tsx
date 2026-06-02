import React from 'react'
import { TestIdLayer } from '../appstate/useTestId'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
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
import { todayISODate } from '../date/date'
import { t } from '../i18n/i18n'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import { append } from '../util/fp/arrays'

interface AhvenanmaanPerusopetuksenLisatiedotProps {
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
}

// Ahvenanmaan perusopetuksen lisätiedoissa on vain kotiopetusjaksot.
export const AhvenanmaanPerusopetuksenLisatiedot: React.FC<
  AhvenanmaanPerusopetuksenLisatiedotProps
> = ({ form }) => {
  const emptyLisatiedot = AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)

  if (!lisätiedot) return null

  return (
    <KeyValueTable>
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
