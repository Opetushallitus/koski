import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { DateEdit, DateView } from '../../components-v2/controls/DateField'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../../components-v2/forms/FormField'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import { OsasuoritusSubproperty } from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { ArvosanaOf } from '../../util/schema'
import { isVSTArviointiPäivällä, isVSTSuoritusArvioinnilla } from './arviointi'
import { VSTArviointi, VSTSuoritusArvioinnilla } from './types'
import { EmptyObject } from '../../util/objects'

export type VSTArviointiViewProps = CommonProps<
  FieldViewerProps<VSTArviointi, EmptyObject>
>

export const VSTArviointiView = (props: VSTArviointiViewProps) => {
  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Arvosana">
        <ArvosanaView value={props.value} />
      </OsasuoritusSubproperty>
      {isVSTArviointiPäivällä(props.value) && (
        <OsasuoritusSubproperty rowNumber={startRow + 1} label="Päivämäärä">
          <DateView value={props.value.päivä} />
        </OsasuoritusSubproperty>
      )}
    </>
  ) : null
}

export type VSTArviointiEditProps<T extends VSTArviointi> = CommonProps<
  FieldEditorProps<
    T,
    {
      createArviointi: (arvosana: ArvosanaOf<T>) => T
      osasuoritus: VSTSuoritusArvioinnilla
    }
  >
>

export const VSTArviointiEdit = <T extends VSTArviointi>(
  props: VSTArviointiEditProps<T>
) => {
  if (!isVSTSuoritusArvioinnilla(props.osasuoritus)) {
    return <div>{'Ei arviointia'}</div>
  }

  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Arvosana">
        <ArvosanaEdit {...props} createArviointi={props.createArviointi} />
      </OsasuoritusSubproperty>
      {isVSTArviointiPäivällä(props.value) && (
        <OsasuoritusSubproperty rowNumber={startRow + 1} label="Päivämäärä">
          <DateEdit
            value={props.value.päivä}
            onChange={(päivä) =>
              päivä && props.onChange({ ...props.value, päivä } as T)
            }
            errors={props.errors}
          />
        </OsasuoritusSubproperty>
      )}
    </>
  ) : null
}
