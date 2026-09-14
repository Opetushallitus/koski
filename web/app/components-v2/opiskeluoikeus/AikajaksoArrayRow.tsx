import React from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { Aikajakso } from '../../types/fi/oph/koski/schema/Aikajakso'
import { append } from '../../util/fp/arrays'
import { ButtonGroup } from '../containers/ButtonGroup'
import { KeyValueRow } from '../containers/KeyValueTable'
import { FlatButton } from '../controls/FlatButton'
import { FormListField } from '../forms/FormListField'
import { FormModel, FormOptic, getValue } from '../forms/FormModel'
import { AikajaksoEdit, AikajaksoView } from './AikajaksoField'
import { uusiTyhjäAikajakso } from './uusiJakso'

export type AikajaksoArrayRowProps<T extends object> = {
  form: FormModel<T>
  path: FormOptic<T, Aikajakso[] | undefined>
  label: string
  testId: string
}

// Lisätietojen aikajaksojen lista. Yksittäiselle aikajaksolle ks.
// SingleAikajaksoRow.
export const AikajaksoArrayRow = <T extends object>({
  form,
  path,
  label,
  testId
}: AikajaksoArrayRowProps<T>) => {
  const values = getValue(path)(form.state)
  if (!form.editMode && (!values || values.length === 0)) return null

  return (
    <KeyValueRow localizableLabel={label} largeLabel>
      <TestIdLayer id={testId}>
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
