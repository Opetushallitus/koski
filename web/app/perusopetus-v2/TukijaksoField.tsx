import React from 'react'
import { PäivämääräväliView } from '../components-v2/opiskeluoikeus/PaivamaaravaliView'
import { TestIdText } from '../appstate/useTestId'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { Tukijakso } from '../types/fi/oph/koski/schema/Tukijakso'
import { DateInput } from '../components-v2/controls/DateInput'
import { hasErrorInField } from '../components-v2/forms/validator'

export const TukijaksoView: React.FC<
  FieldViewerProps<Tukijakso | undefined, EmptyObject>
> = ({ value }) => (
  <div>
    <PäivämääräväliView alku={value?.alku} loppu={value?.loppu} />
  </div>
)

export const TukijaksoEdit: React.FC<
  FieldEditorProps<Tukijakso | undefined, EmptyObject>
> = ({ value, onChange, errors }) => (
  <div className="AikajaksoEdit">
    <DateInput
      value={value?.alku}
      onChange={(alku?: string) => {
        onChange(Tukijakso({ ...value, alku }))
      }}
      hasErrors={hasErrorInField(errors, 'alku')}
      testId="alku"
    />
    <span className="AikajaksoEdit__separator"> {' — '}</span>
    <DateInput
      value={value?.loppu}
      onChange={(loppu?: string) => {
        // Tukijakso.alku on valinnainen, joten sitä ei täydennetä tässä.
        onChange(Tukijakso({ ...value, loppu }))
      }}
      hasErrors={hasErrorInField(errors, 'loppu')}
      testId="loppu"
    />
  </div>
)
