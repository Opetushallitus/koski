import React from 'react'
import { TestIdText } from '../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { Tukijakso } from '../types/fi/oph/koski/schema/Tukijakso'
import { DateInput } from '../components-v2/controls/DateInput'

export const TukijaksoView: React.FC<
  FieldViewerProps<Tukijakso | undefined, EmptyObject>
> = ({ value }) => (
  <div>
    <TestIdText id="alku">
      {value?.alku && ISO2FinnishDate(value.alku)}
    </TestIdText>
    {' — '}
    <TestIdText id="loppu">
      {value?.loppu && ISO2FinnishDate(value.loppu)}
    </TestIdText>
  </div>
)

export const TukijaksoEdit: React.FC<
  FieldEditorProps<Tukijakso | undefined, EmptyObject>
> = ({ value, onChange }) => (
  <div className="AikajaksoEdit">
    <DateInput
      value={value?.alku}
      onChange={(alku?: string) => {
        onChange(Tukijakso({ ...value, alku }))
      }}
      testId="alku"
    />
    <span className="AikajaksoEdit__separator"> {' — '}</span>
    <DateInput
      value={value?.loppu}
      onChange={(loppu?: string) => {
        onChange(
          Tukijakso({ alku: value?.alku || todayISODate(), ...value, loppu })
        )
      }}
      testId="loppu"
    />
  </div>
)
