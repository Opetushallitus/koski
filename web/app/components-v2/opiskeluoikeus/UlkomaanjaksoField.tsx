import React from 'react'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../../date/date'
import { emptyLocalizedString, t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Ulkomaanjakso } from '../../types/fi/oph/koski/schema/Ulkomaanjakso'
import { CommonProps } from '../CommonProps'
import { DateInput } from '../controls/DateInput'
import { LocalizedTextEdit } from '../controls/LocalizedTestField'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { KoodistoSelect } from './KoodistoSelect'
import { EmptyObject } from '../../util/objects'

export type UlkomaanjaksoViewProps = CommonProps<
  FieldViewerProps<Ulkomaanjakso | undefined, EmptyObject>
>

export const UlkomaanjaksoView: React.FC<UlkomaanjaksoViewProps> = ({
  value
}) => {
  return (
    <TestIdLayer id="ulkomaanjakso">
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>{' '}
      <TestIdText id="maa">{t(value?.maa.nimi)}</TestIdText>
      {': '}
      <TestIdText id="kuvaus">{t(value?.kuvaus)}</TestIdText>
    </TestIdLayer>
  )
}

export type UlkomaanjaksoEditProps = CommonProps<
  FieldEditorProps<Ulkomaanjakso, EmptyObject>
>

export const emptyUlkomaanjakso = Ulkomaanjakso({
  alku: todayISODate(),
  maa: Koodistokoodiviite({ koodistoUri: 'maatjavaltiot2', koodiarvo: '999' }),
  kuvaus: emptyLocalizedString
})

export const UlkomaanjaksoEdit: React.FC<UlkomaanjaksoEditProps> = ({
  value,
  onChange
}) => {
  const setAlku = (alku?: string) => {
    alku && onChange({ ...emptyUlkomaanjakso, ...value, alku })
  }

  const setLoppu = (loppu?: string) => {
    onChange({ ...emptyUlkomaanjakso, ...value, loppu })
  }

  const setMaa = (maa: Koodistokoodiviite<'maatjavaltiot2'> | undefined) => {
    maa && onChange({ ...emptyUlkomaanjakso, ...value, maa })
  }

  const setKuvaus = (kuvaus: LocalizedString | undefined) => {
    kuvaus && onChange({ ...emptyUlkomaanjakso, ...value, kuvaus })
  }

  return (
    <TestIdLayer id="maksuttomuus">
      <div className="MaksuttomuusEdit">
        <DateInput value={value?.alku} onChange={setAlku} testId="alku" />
        <span className="MaksuttomuusEdit__separator"> {' - '}</span>
        <DateInput value={value?.loppu} onChange={setLoppu} testId="loppu" />
        <KoodistoSelect
          koodistoUri="maatjavaltiot2"
          value={value?.maa.koodiarvo}
          onSelect={setMaa}
          testId="maa"
        />
        <LocalizedTextEdit
          value={value?.kuvaus}
          onChange={setKuvaus}
          testId="kuvaus"
          placeholder={t('Kuvaus')}
        />
      </div>
    </TestIdLayer>
  )
}
