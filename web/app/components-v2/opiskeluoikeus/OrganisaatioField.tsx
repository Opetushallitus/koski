import React, { useCallback, useMemo } from 'react'
import { useOrganisaatioHierarkia } from '../../appstate/organisaatioHierarkia'
import { t } from '../../i18n/i18n'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { isTutkintotoimikunta } from '../../types/fi/oph/koski/schema/Tutkintotoimikunta'
import { isYritys } from '../../types/fi/oph/koski/schema/Yritys'
import { getOrganisaatioId, toOrganisaatio } from '../../util/organisaatiot'
import { common, CommonProps } from '../CommonProps'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'

export type OrganisaatioViewProps<T extends Organisaatio> = CommonProps<
  FieldViewBaseProps<T>
>

export const OrganisaatioView = <T extends Organisaatio>(
  props: OrganisaatioViewProps<T>
): React.ReactElement => (
  <div {...common(props, ['OrganisaatioView'])}>
    {t(props.value?.nimi) || '–'}
  </div>
)

export type OrganisaatioEditProps<T extends Organisaatio> = CommonProps<
  FieldEditBaseProps<T>
>

export const OrganisaatioEdit = <T extends Organisaatio>(
  props: OrganisaatioEditProps<T>
): React.ReactElement => {
  const organisaatiot = useOrganisaatioHierarkia()
  const options: OptionList<T> = useMemo(
    () => organisaatioHierarkiaToOptions(organisaatiot),
    [organisaatiot]
  )

  const selected = useMemo(
    () => props.value && getOrganisaatioId(props.value),
    [props.value]
  )

  const onChange = useCallback(
    (option?: SelectOption<T>) => {
      props.onChange(option?.value)
    },
    [props.onChange]
  )

  return <Select options={options} value={selected} onChange={onChange} />
}

const organisaatioHierarkiaToOptions = <T extends Organisaatio>(
  orgs: OrganisaatioHierarkia[]
): OptionList<T> =>
  orgs.map((organisaatiohierarkia) => {
    const org = toOrganisaatio(organisaatiohierarkia)
    return {
      key: getOrganisaatioId(org),
      label: t(org.nimi),
      value: org as T
    }
  })
