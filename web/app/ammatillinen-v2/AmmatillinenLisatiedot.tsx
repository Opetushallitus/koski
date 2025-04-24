import { FormModel, getValue } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import React from 'react'
import { AmmatillisenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AmmatillisenOpiskeluoikeudenLisatiedot'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  AikajaksoEdit,
  AikajaksoView
} from '../components-v2/opiskeluoikeus/AikajaksoField'
import { Aikajakso } from '../types/fi/oph/koski/schema/Aikajakso'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { t } from '../i18n/i18n'
import { todayISODate } from '../date/date'
import { append } from '../util/fp/arrays'

interface AmmatillinenLisatiedotProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
}

export const AmmatillinenLisatiedot: React.FC<AmmatillinenLisatiedotProps> = ({
  form
}) => {
  const emptyLisatiedot = AmmatillisenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)

  return (
    <KeyValueTable>
      <KeyValueRow
        localizableLabel="Oikeus maksuttomaan asuntolapaikkaan"
        largeLabel
      >
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={lisatiedotPath.prop('oikeusMaksuttomaanAsuntolapaikkaan')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel={'Majoitus'}>
        <FormListField
          form={form}
          view={AikajaksoView}
          edit={AikajaksoEdit}
          path={lisatiedotPath.prop('majoitus')}
          editProps={{
            createAikajakso: Aikajakso
          }}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  lisatiedotPath.prop('majoitus').valueOr([]),
                  append(Aikajakso({ alku: todayISODate() }))
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
    </KeyValueTable>
  )
}
