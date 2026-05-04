import React from 'react'
import { TestIdText } from '../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../date/date'
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
      <TestIdText id="alku">
        {value.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>
      {' — '}
      <TestIdText id="loppu">
        {value.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
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
