import { CommonProps } from '../components-v2/CommonProps'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../components-v2/forms/FormField'
import { AmmatillinenArviointi } from '../types/fi/oph/koski/schema/AmmatillinenArviointi'
import { EmptyObject } from '../util/objects'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { finnish, t } from '../i18n/i18n'
import { ISO2FinnishDate, todayISODate } from '../date/date'
import React from 'react'
import { DateInput } from '../components-v2/controls/DateInput'
import { TextEdit } from '../components-v2/controls/TextField'
import { Arvioitsija } from '../types/fi/oph/koski/schema/Arvioitsija'
import { IconButton } from '../components-v2/controls/IconButton'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { LocalizedTextEdit } from '../components-v2/controls/LocalizedTestField'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Select, useKoodistoOptions } from '../components-v2/controls/Select'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'

export const ArviointiView = ({
  value
}: CommonProps<FieldViewerProps<AmmatillinenArviointi, EmptyObject>>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        <TestIdText id="arvosana">{t(value?.arvosana.nimi)}</TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        {ISO2FinnishDate(value?.päivä)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioijat">
        <TestIdLayer id="arvioijat">
          {value?.arvioitsijat?.map((a) => (
            <>
              <TestIdText id="nimi">{a.nimi}</TestIdText>
              <br />
            </>
          ))}
        </TestIdLayer>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Kuvaus">{t(value?.kuvaus)}</KeyValueRow>
    </>
  )
}
export const emptyArviointi: AmmatillinenArviointi = AmmatillinenArviointi({
  päivä: todayISODate(),
  arvosana: Koodistokoodiviite({
    koodistoUri: 'arviointiasteikkoammatillinenhyvaksyttyhylatty',
    koodiarvo: 'Hyväksytty',
    nimi: finnish('Hyväksytty')
  })
})
export const ArviointiEdit = ({
  value,
  onChange
}: FieldEditorProps<AmmatillinenArviointi, EmptyObject>) => {
  return (
    <>
      <KeyValueRow localizableLabel="Arvosana">
        <AmisArvosanaSelect
          value={value?.arvosana}
          onChange={(arvosana) =>
            arvosana && onChange({ ...emptyArviointi, ...value, arvosana })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arviointipäivä">
        <DateInput
          value={value?.päivä}
          onChange={(päivä) =>
            päivä && onChange({ ...emptyArviointi, ...value, päivä })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Arvioijat">
        {value?.arvioitsijat?.map((a, index) => (
          <KeyValueTable>
            <KeyValueRow localizableLabel="Nimi">
              <TextEdit
                value={a.nimi}
                onChange={(nimi) =>
                  nimi &&
                  onChange({
                    ...emptyArviointi,
                    ...value,
                    arvioitsijat: value.arvioitsijat
                      ? [
                          ...value.arvioitsijat.slice(0, index),
                          Arvioitsija({ nimi }),
                          ...value.arvioitsijat.slice(index + 1)
                        ]
                      : [Arvioitsija({ nimi })]
                  })
                }
              />
            </KeyValueRow>
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() =>
                value.arvioitsijat &&
                onChange({
                  ...emptyArviointi,
                  ...value,
                  arvioitsijat: [
                    ...value.arvioitsijat.slice(0, index),
                    ...value.arvioitsijat.slice(index + 1)
                  ]
                })
              }
              testId="delete"
            />
          </KeyValueTable>
        ))}
        <ButtonGroup>
          <FlatButton
            onClick={() =>
              onChange({
                ...emptyArviointi,
                ...value,
                arvioitsijat: [
                  ...(value?.arvioitsijat || []),
                  Arvioitsija({ nimi: '' })
                ]
              })
            }
          >
            {t('Lisää uusi')}
          </FlatButton>
        </ButtonGroup>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Kuvaus">
        <LocalizedTextEdit
          value={value?.kuvaus}
          onChange={(kuvaus) =>
            onChange({ ...emptyArviointi, ...value, kuvaus })
          }
        />
      </KeyValueRow>
    </>
  )
}
type AmisArvosanaSelectProps = {
  value?: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  onChange?: (
    value?: Koodistokoodiviite<
      | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
      | 'arviointiasteikkoammatillinent1k3'
      | 'arviointiasteikkoammatillinen15',
      string
    >
  ) => void
}
export const AmisArvosanaSelect = ({
  value,
  onChange
}: AmisArvosanaSelectProps) => {
  const options = useKoodistoOptions(
    'arviointiasteikkoammatillinenhyvaksyttyhylatty',
    'arviointiasteikkoammatillinent1k3',
    'arviointiasteikkoammatillinen15'
  )
  return (
    <Select
      options={options}
      value={value && value.koodistoUri + '_' + value.koodiarvo}
      onChange={(a) => onChange && onChange(a?.value)}
      testId="arvosana"
    />
  )
}
