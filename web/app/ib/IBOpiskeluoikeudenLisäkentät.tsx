import React from 'react'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel } from '../components-v2/forms/FormModel'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'

interface IBOpiskeluoikeudenLisäkentätProps {
  form: FormModel<IBOpiskeluoikeus>
}

export const IBOpiskeluoikeudenLisäkentät: React.FC<
  IBOpiskeluoikeudenLisäkentätProps
> = ({ form }) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="IBO-tunniste">
        <FormField
          form={form}
          path={form.root.prop('iboTunniste')}
          view={TextView}
          edit={TextEdit}
          testId="iboTunniste"
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}
