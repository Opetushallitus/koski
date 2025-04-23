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
    </KeyValueTable>
  )
}
