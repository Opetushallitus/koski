import React from 'react'
import { TestIdText } from '../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { ErityisenTuenPäätös } from '../types/fi/oph/koski/schema/ErityisenTuenPaatos'
import { DateInput } from '../components-v2/controls/DateInput'
import { Checkbox } from '../components-v2/controls/Checkbox'
import { t } from '../i18n/i18n'
import { BooleanView } from '../components-v2/opiskeluoikeus/BooleanField'

const emptyPäätös = ErityisenTuenPäätös({ opiskeleeToimintaAlueittain: false })

const hasDeprecatedBooleanValue = (value?: boolean | null): value is boolean =>
  value !== undefined && value !== null

export const ErityisenTuenPäätösView: React.FC<
  FieldViewerProps<ErityisenTuenPäätös | undefined, EmptyObject>
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
      <BooleanValue
        label="Opiskelee toiminta-alueittain"
        value={value.opiskeleeToimintaAlueittain}
        testId="opiskeleeToimintaAlueittain"
      />
      {hasDeprecatedBooleanValue(value.erityisryhmässä) && (
        <BooleanValue
          label="Opiskelee erityisryhmässä"
          value={value.erityisryhmässä}
          testId="erityisryhmässä"
        />
      )}
    </div>
  )
}

const BooleanValue: React.FC<{
  label: string
  value: boolean
  testId: string
}> = ({ label, value, testId }) => (
  <div>
    <span>{t(label)} </span>
    <BooleanView
      value={value}
      testId={testId}
      trueText={t('kyllä')}
      falseText={t('ei')}
    />
  </div>
)

export const ErityisenTuenPäätösEdit: React.FC<
  FieldEditorProps<ErityisenTuenPäätös | undefined, EmptyObject>
> = ({ value, onChange }) => {
  const current = value || emptyPäätös
  return (
    <div className="ErityisenTuenPaatos">
      <div className="AikajaksoEdit">
        <DateInput
          value={current.alku}
          onChange={(alku?: string) => onChange({ ...current, alku })}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' — '}</span>
        <DateInput
          value={current.loppu}
          onChange={(loppu?: string) => onChange({ ...current, loppu })}
          testId="loppu"
        />
      </div>
      <Checkbox
        checked={current.opiskeleeToimintaAlueittain}
        onChange={(opiskeleeToimintaAlueittain) =>
          onChange({ ...current, opiskeleeToimintaAlueittain })
        }
        label="Opiskelee toiminta-alueittain"
        testId="opiskeleeToimintaAlueittain"
      />
      {hasDeprecatedBooleanValue(current.erityisryhmässä) && (
        <Checkbox
          checked={current.erityisryhmässä}
          onChange={(erityisryhmässä) =>
            onChange({ ...current, erityisryhmässä })
          }
          label="Opiskelee erityisryhmässä"
          testId="erityisryhmässä"
        />
      )}
    </div>
  )
}
