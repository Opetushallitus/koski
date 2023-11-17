import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormListField } from '../../components-v2/forms/FormListField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VSTArviointiEdit, VSTArviointiView } from '../VSTArviointiField'
import {
  VSTSuoritus,
  VSTSuoritusArvioinnilla,
  VSTSuoritusKuvauksella,
  VSTSuoritusTunnustuksella
} from './types'
import {
  KuvausEdit,
  KuvausView
} from '../../components-v2/opiskeluoikeus/KuvausField'
import { FormField } from '../../components-v2/forms/FormField'
import {
  TunnustusEdit,
  TunnustusView
} from '../../components-v2/opiskeluoikeus/TunnustusField'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import { emptyLocalizedString, finnish } from '../../i18n/i18n'
import { TestIdLayer } from '../../appstate/useTestId'

export type VSTPropertyFieldProps<T extends VSTSuoritus = VSTSuoritus> =
  CommonProps<{
    form: FormModel<VapaanSivistystyönOpiskeluoikeus>
    path: FormOptic<VapaanSivistystyönOpiskeluoikeus, T>
  }>

export const ArviointiProperty = <T extends VSTSuoritusArvioinnilla>(
  props: VSTPropertyFieldProps<T>
) => {
  const osasuoritus = getValue(props.path)(props.form.state)
  const arvioitu = (osasuoritus?.arviointi?.length || 0) > 0

  return arvioitu ? (
    <TestIdLayer id="arviointi">
      <OsasuoritusProperty label="Arviointi">
        <FormListField
          form={props.form}
          path={props.path.prop('arviointi')}
          view={VSTArviointiView}
          edit={VSTArviointiEdit}
          editProps={{ osasuoritus }}
        />
      </OsasuoritusProperty>
    </TestIdLayer>
  ) : null
}

export const KuvausProperty = <T extends VSTSuoritusKuvauksella>(
  props: VSTPropertyFieldProps<T>
) => (
  <OsasuoritusProperty label="Kuvaus">
    <OsasuoritusPropertyValue>
      <FormField
        form={props.form}
        path={props.path.prop('koulutusmoduuli').prop('kuvaus')}
        view={KuvausView}
        edit={KuvausEdit}
      />
    </OsasuoritusPropertyValue>
  </OsasuoritusProperty>
)

export const TunnustettuProperty = <T extends VSTSuoritusTunnustuksella>(
  props: VSTPropertyFieldProps<T>
) => {
  const osasuoritus = getValue(props.path)(props.form.state)
  const visible = osasuoritus?.tunnustettu !== undefined || props.form.editMode

  return visible ? (
    <OsasuoritusProperty label="Tunnustettu">
      <OsasuoritusPropertyValue>
        <FormField
          form={props.form}
          path={props.path.prop('tunnustettu')}
          view={TunnustusView}
          edit={TunnustusEdit}
          editProps={{
            createEmptyTunnustus: () =>
              VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen({
                selite: emptyLocalizedString
              })
          }}
        />
      </OsasuoritusPropertyValue>
    </OsasuoritusProperty>
  ) : null
}
