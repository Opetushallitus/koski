import React from 'react'
import { TestIdLayer } from '../../../appstate/useTestId'
import { CommonProps } from '../../../components-v2/CommonProps'
import { DateEdit, DateView } from '../../../components-v2/controls/DateField'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../../../components-v2/forms/FormField'
import { FormListField } from '../../../components-v2/forms/FormListField'
import { getValue } from '../../../components-v2/forms/FormModel'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { todayISODate } from '../../../date/date'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'
import { EmptyObject } from '../../../util/objects'
import { VSTPropertyFieldProps } from '../../common/propertyFields'
import { VSTSuoritusArvioinnilla } from '../../common/types'

export const VSTKoto2022KielitaitotasoProperty = <
  T extends VSTSuoritusArvioinnilla
>(
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
          view={VSTKoto2022KielitaitotasoView}
          edit={VSTKoto2022KielitaitotasoEdit}
          editProps={{ osasuoritus }}
        />
      </OsasuoritusProperty>
    </TestIdLayer>
  ) : null
}

export type VSTKoto2022KielitaitotasoViewProps = CommonProps<
  FieldViewerProps<
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi,
    EmptyObject
  >
>

export const VSTKoto2022KielitaitotasoView = (
  props: VSTKoto2022KielitaitotasoViewProps
) => {
  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Taitotaso">
        <ArvosanaView value={props.value} />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={startRow + 1} label="Päivämäärä">
        <DateView value={props.value.arviointipäivä} />
      </OsasuoritusSubproperty>
    </>
  ) : null
}

export type VSTKoto2022KielitaitotasoEditProps = CommonProps<
  FieldEditorProps<
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi,
    {
      osasuoritus: VSTSuoritusArvioinnilla
    }
  >
>

export const VSTKoto2022KielitaitotasoEdit = (
  props: VSTKoto2022KielitaitotasoEditProps
) => {
  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Taitotaso">
        <ArvosanaEdit
          {...props}
          createArviointi={(arvosana) =>
            VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi({
              arvosana,
              arviointipäivä: todayISODate()
            })
          }
        />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={startRow + 1} label="Päivämäärä">
        <DateEdit
          value={props.value.arviointipäivä}
          onChange={(arviointipäivä) =>
            props.value?.arvosana &&
            props.onChange(
              VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi({
                arvosana: props.value?.arvosana,
                arviointipäivä
              })
            )
          }
        />
      </OsasuoritusSubproperty>
    </>
  ) : null
}
