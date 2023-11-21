import React from 'react'
import { CommonProps } from '../components-v2/CommonProps'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import { OsasuoritusSubproperty } from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { todayISODate } from '../date/date'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'
import { VSTOsasuoritus } from './typeguards'

export type VSTTaitotasoViewProps = CommonProps<
  FieldViewerProps<
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi,
    {}
  >
>

export const VSTTaitotasoView = (props: VSTTaitotasoViewProps) => {
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

export type VSTTaitotasoEditProps = CommonProps<
  FieldEditorProps<
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi,
    {
      osasuoritus: VSTOsasuoritus
    }
  >
>

export const VSTTaitotasoEdit = (props: VSTTaitotasoEditProps) => {
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
