import React from 'react'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../../date/date'
import { Maksuttomuus } from '../../types/fi/oph/koski/schema/Maksuttomuus'
import { CommonProps } from '../CommonProps'
import { Checkbox } from '../controls/Checkbox'
import { DateInput } from '../controls/DateInput'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { t } from '../../i18n/i18n'
import { EmptyObject } from '../../util/objects'

export type MaksuttomuusViewProps = CommonProps<
  FieldViewerProps<Maksuttomuus | undefined, EmptyObject>
>

export const MaksuttomuusView: React.FC<MaksuttomuusViewProps> = ({
  value
}) => {
  return (
    <div>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      <TestIdText id="maksuton">
        {value?.maksuton ? t('Maksuton') : t('Maksullinen')}
      </TestIdText>
    </div>
  )
}

export type MaksuttomuusEditProps = CommonProps<
  FieldEditorProps<Maksuttomuus, EmptyObject>
>

export const emptyMaksuttomuuus = Maksuttomuus({
  maksuton: false,
  alku: todayISODate()
})

export const MaksuttomuusEdit: React.FC<MaksuttomuusEditProps> = ({
  value,
  onChange
}) => {
  const setAlku = (alku?: string) => {
    alku && onChange({ ...emptyMaksuttomuuus, ...value, alku })
  }

  const setLoppu = (loppu?: string) => {
    onChange({ ...emptyMaksuttomuuus, ...value, loppu })
  }

  const setMaksuton = (maksuton: boolean) => {
    onChange({ ...emptyMaksuttomuuus, maksuton })
  }

  return (
    <TestIdLayer id="maksuttomuus">
      <div className="MaksuttomuusEdit">
        <DateInput value={value?.alku} onChange={setAlku} testId="alku" />
        <span className="MaksuttomuusEdit__separator"> {' - '}</span>
        <DateInput value={value?.loppu} onChange={setLoppu} testId="loppu" />
        <Checkbox
          checked={value?.maksuton || false}
          onChange={setMaksuton}
          label="Maksuton"
          testId="maksuton"
        />
      </div>
    </TestIdLayer>
  )
}
