import React from 'react'
import {
  useKoodisto,
  KoodistokoodiviiteKoodistonNimellä
} from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { CommonProps } from '../CommonProps'
import { OptionList, Select } from '../controls/Select'
import { FieldViewerProps, FieldEditorProps } from '../forms/FormField'

type OpintokokonaisuusViewProps = CommonProps<
  FieldViewerProps<Koodistokoodiviite<'opintokokonaisuudet', string>, {}>
>

export const OpintokokonaisuusView: React.FC<OpintokokonaisuusViewProps> = (
  props
) => {
  return (
    <div>
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
  FieldEditorProps<Koodistokoodiviite<'opintokokonaisuudet', string>, {}>
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
    />
  )
}
