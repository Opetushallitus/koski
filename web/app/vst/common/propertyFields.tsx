import React from 'react'
import { CommonProps, subTestId } from '../../components-v2/CommonProps'
import { FormListField } from '../../components-v2/forms/FormListField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import { OsasuoritusProperty } from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VSTArviointiEdit, VSTArviointiView } from '../VSTArviointiField'
import { VSTSuoritus, VSTSuoritusArvioinnilla } from './types'

export type VSTPropertyFieldProps<T extends VSTSuoritus = VSTSuoritus> =
  CommonProps<{
    form: FormModel<VapaanSivistystyönOpiskeluoikeus>
    path: FormOptic<VapaanSivistystyönOpiskeluoikeus, T>
  }>

export const VSTArviointiField = <T extends VSTSuoritusArvioinnilla>(
  props: VSTPropertyFieldProps<T>
) => {
  const osasuoritus = getValue(props.path)(props.form.state)
  const arvioitu = (osasuoritus?.arviointi?.length || 0) > 0

  return arvioitu ? (
    <OsasuoritusProperty label="Arviointi">
      <FormListField
        form={props.form}
        path={props.path.prop('arviointi')}
        view={VSTArviointiView}
        edit={VSTArviointiEdit}
        editProps={{ osasuoritus }}
        testId={subTestId(props, 'arviointi')}
      />
    </OsasuoritusProperty>
  ) : null
}
