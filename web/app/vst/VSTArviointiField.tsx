import React from 'react'
import { CommonProps, subTestId } from '../components-v2/CommonProps'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import {
  ParasArvosanaEdit,
  ArvosanaView,
  ArvosanaEdit
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import { OsasuoritusSubproperty } from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  VSTArviointi,
  VSTOsasuoritus,
  VSTOsasuoritusArvioinnilla,
  hasPäiväInArviointi,
  isVSTOsasuoritusArvioinnilla
} from './typeguards'
import { createVstArviointi } from './resolvers'

export type VSTArviointiViewProps = CommonProps<
  FieldViewerProps<VSTArviointi, {}>
>

export const VSTArviointiView = (props: VSTArviointiViewProps) => {
  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Arvosana">
        <ArvosanaView
          value={props.value}
          testId={subTestId(props, 'arvosana')}
        />
      </OsasuoritusSubproperty>
      {hasPäiväInArviointi(props.value) && (
        <OsasuoritusSubproperty rowNumber={startRow + 1} label="Päivämäärä">
          <DateView
            value={props.value.päivä}
            testId={subTestId(props, 'päivä')}
          />
        </OsasuoritusSubproperty>
      )}
    </>
  ) : null
}

export type VSTArviointiEditProps = CommonProps<
  FieldEditorProps<
    VSTArviointi,
    {
      osasuoritus: VSTOsasuoritus
    }
  >
>

export const VSTArviointiEdit = (props: VSTArviointiEditProps) => {
  if (!isVSTOsasuoritusArvioinnilla(props.osasuoritus)) {
    return <div>{'Ei arviointia'}</div>
  }

  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Arvosana">
        <ArvosanaEdit
          {...props}
          createArviointi={createVstArviointi(props.osasuoritus)}
          testId={subTestId(props, 'arvosana')}
        />
      </OsasuoritusSubproperty>
      {hasPäiväInArviointi(props.value) && (
        <OsasuoritusSubproperty rowNumber={startRow + 1} label="Päivämäärä">
          <DateEdit
            value={props.value.päivä}
            onChange={(päivä) =>
              päivä && props.onChange({ ...props.value, päivä } as VSTArviointi)
            }
            testId={subTestId(props, 'päivä')}
          />
        </OsasuoritusSubproperty>
      )}
    </>
  ) : null
}
