import React from 'react'
import { PäivämääräväliView } from '../components-v2/opiskeluoikeus/PaivamaaravaliView'
import { TestIdText } from '../appstate/useTestId'
import { todayISODate } from '../date/date'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { TehostetunTuenPäätös } from '../types/fi/oph/koski/schema/TehostetunTuenPaatos'
import { DateInput } from '../components-v2/controls/DateInput'

export const TehostetunTuenPäätösView: React.FC<
  FieldViewerProps<TehostetunTuenPäätös | undefined, EmptyObject>
> = ({ value }) => {
  if (!value) return null
  return (
    <div>
      <PäivämääräväliView alku={value.alku} loppu={value.loppu} />
    </div>
  )
}

export const TehostetunTuenPäätösEdit: React.FC<
  FieldEditorProps<TehostetunTuenPäätös | undefined, EmptyObject>
> = ({ value, onChange }) => {
  const emptyPäätös = TehostetunTuenPäätös({ alku: todayISODate() })
  const current = value || emptyPäätös
  return (
    <div className="AikajaksoEdit">
      <DateInput
        value={current.alku}
        onChange={(alku?: string) => alku && onChange({ ...current, alku })}
        testId="alku"
      />
      <span className="AikajaksoEdit__separator"> {' — '}</span>
      <DateInput
        value={current.loppu}
        onChange={(loppu?: string) => onChange({ ...current, loppu })}
        testId="loppu"
      />
    </div>
  )
}
