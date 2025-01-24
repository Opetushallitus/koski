import React from 'react'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { ErityisenKoulutustehtävänJakso } from '../../types/fi/oph/koski/schema/ErityisenKoulutustehtavanJakso'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { CommonProps } from '../CommonProps'
import { DateInput } from '../controls/DateInput'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { KoodistoSelect } from './KoodistoSelect'
import { EmptyObject } from '../../util/objects'

export type ErityisenKoulutustehtävänJaksoViewProps = CommonProps<
  FieldViewerProps<ErityisenKoulutustehtävänJakso | undefined, EmptyObject>
>

export const ErityisenKoulutustehtävänJaksoView: React.FC<
  ErityisenKoulutustehtävänJaksoViewProps
> = ({ value }) => {
  return (
    <TestIdLayer id="erityisenKoulutustehtävänJakso">
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>{' '}
      <TestIdText id="tehtävä">{t(value?.tehtävä.nimi)}</TestIdText>
    </TestIdLayer>
  )
}

export type ErityisenKoulutustehtävänJaksoEditProps = CommonProps<
  FieldEditorProps<ErityisenKoulutustehtävänJakso, EmptyObject>
>

export const emptyErityisenKoulutustehtävänJakso =
  ErityisenKoulutustehtävänJakso({
    alku: todayISODate(),
    tehtävä: Koodistokoodiviite({
      koodistoUri: 'erityinenkoulutustehtava',
      koodiarvo: '101'
    })
  })

export const ErityisenKoulutustehtävänJaksoEdit: React.FC<
  ErityisenKoulutustehtävänJaksoEditProps
> = ({ value, onChange }) => {
  const setAlku = (alku?: string) => {
    alku && onChange({ ...emptyErityisenKoulutustehtävänJakso, ...value, alku })
  }

  const setLoppu = (loppu?: string) => {
    onChange({ ...emptyErityisenKoulutustehtävänJakso, ...value, loppu })
  }

  const setTehtävä = (
    tehtävä: Koodistokoodiviite<'erityinenkoulutustehtava'> | undefined
  ) => {
    tehtävä &&
      onChange({ ...emptyErityisenKoulutustehtävänJakso, ...value, tehtävä })
  }

  return (
    <TestIdLayer id="maksuttomuus">
      <div className="MaksuttomuusEdit">
        <DateInput value={value?.alku} onChange={setAlku} testId="alku" />
        <span className="MaksuttomuusEdit__separator"> {' - '}</span>
        <DateInput value={value?.loppu} onChange={setLoppu} testId="loppu" />
        <KoodistoSelect
          koodistoUri="erityinenkoulutustehtava"
          value={value?.tehtävä.koodiarvo}
          onSelect={setTehtävä}
          testId="tehtävä"
        />
      </div>
    </TestIdLayer>
  )
}
