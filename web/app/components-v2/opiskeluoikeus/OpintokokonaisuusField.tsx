import React from 'react'
import {
  KoodistokoodiviiteKoodistonNimellä,
  useKoodisto
} from '../../appstate/koodisto'
import { useTestId } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import { OptionList, Select } from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

type OpintokokonaisuusViewProps = CommonProps<
  FieldViewerProps<
    Koodistokoodiviite<'opintokokonaisuudet', string>,
    EmptyObject
  >
>

export const OpintokokonaisuusView: React.FC<OpintokokonaisuusViewProps> = (
  props
) => {
  const testId = useTestId('opintokokonaisuus.value')
  return (
    <div data-testid={testId}>
      <a
        href={`${window.ePerusteetBaseUrl}${t(
          'eperusteet_opintopolku_url_fragment'
        )}${props.value?.koodiarvo}`}
        target="_blank"
        rel="noopener noreferrer"
      >
        {props.value?.koodiarvo} {t(props.value?.nimi)}
      </a>
    </div>
  )
}

type OpintokokonaisuusEditProps = CommonProps<
  FieldEditorProps<
    Koodistokoodiviite<'opintokokonaisuudet', string>,
    EmptyObject
  >
>

export const OpintokokonaisuusEdit: React.FC<OpintokokonaisuusEditProps> = (
  props
) => {
  const opintokokonaisuudet = useKoodisto('opintokokonaisuudet')
  const mappedOpintokokonaisuudet: OptionList<
    KoodistokoodiviiteKoodistonNimellä<'opintokokonaisuudet'>
  > = opintokokonaisuudet
    ? opintokokonaisuudet.map((o) => ({
        key: o.koodiviite.koodiarvo,
        label: `${o.koodiviite.koodiarvo} ${t(o.koodiviite.nimi)}`,
        value: o
      }))
    : []
  return (
    <Select
      onChange={(opt) => {
        props.onChange(opt?.value?.koodiviite)
      }}
      initialValue={props.value?.koodiarvo}
      value={props.value?.koodiarvo || ''}
      options={mappedOpintokokonaisuudet}
      testId="opintokokonaisuus.edit"
    />
  )
}
