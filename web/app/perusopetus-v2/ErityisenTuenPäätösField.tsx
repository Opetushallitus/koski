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

const emptyPäätös = ErityisenTuenPäätös({ opiskeleeToimintaAlueittain: false })

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
      {value.opiskeleeToimintaAlueittain && (
        <span>
          {' ('}
          {t('opiskelee toiminta-alueittain')}
          {')'}
        </span>
      )}
      {value.erityisryhmässä && (
        <span>
          {' ('}
          {t('erityisryhmässä')}
          {')'}
        </span>
      )}
    </div>
  )
}

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
        label={t('Opiskelee toiminta-alueittain')}
        testId="opiskeleeToimintaAlueittain"
      />
      {current.erityisryhmässä && (
        <Checkbox
          checked={current.erityisryhmässä}
          onChange={(erityisryhmässä) =>
            onChange({ ...current, erityisryhmässä })
          }
          label={t('Erityisryhmässä')}
          testId="erityisryhmässä"
        />
      )}
    </div>
  )
}
