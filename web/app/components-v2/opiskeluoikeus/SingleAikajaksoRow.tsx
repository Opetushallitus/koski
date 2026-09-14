import React from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { Aikajakso } from '../../types/fi/oph/koski/schema/Aikajakso'
import { ButtonGroup } from '../containers/ButtonGroup'
import { KeyValueRow } from '../containers/KeyValueTable'
import { FlatButton } from '../controls/FlatButton'
import { Removable } from '../controls/Removable'
import { FormField } from '../forms/FormField'
import { FormModel, FormOptic, getValue } from '../forms/FormModel'
import { AikajaksoEdit, AikajaksoView } from './AikajaksoField'
import { uusiTyhjäAikajakso } from './uusiJakso'

export type SingleAikajaksoRowProps<T extends object> = {
  form: FormModel<T>
  path: FormOptic<T, Aikajakso | undefined>
  label: string
  testId: string
  // Vanhentunut kenttä näytetään vain, jos sillä on jo arvo: uutta jaksoa ei
  // voi lisätä.
  deprecated?: boolean
}

// Lisätietojen yksittäinen, valinnainen aikajakso. Aikajaksojen listalle ks.
// AikajaksoArrayRow.
export const SingleAikajaksoRow = <T extends object>({
  form,
  path,
  label,
  testId,
  deprecated
}: SingleAikajaksoRowProps<T>) => {
  const value = getValue(path)(form.state)
  if (!value && (deprecated || !form.editMode)) return null

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
      <TestIdLayer id={testId}>
        {!value ? (
          <ButtonGroup>
            <FlatButton
              onClick={() => form.updateAt(path, () => uusiTyhjäAikajakso())}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        ) : form.editMode ? (
          <Removable onClick={() => form.updateAt(path, () => undefined)}>
            {field}
          </Removable>
        ) : (
          field
        )}
      </TestIdLayer>
    </KeyValueRow>
  )
}
