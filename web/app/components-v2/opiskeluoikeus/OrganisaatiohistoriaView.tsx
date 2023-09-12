import React from 'react'
import { t } from '../../i18n/i18n'
import { OpiskeluoikeudenOrganisaatiohistoria } from '../../types/fi/oph/koski/schema/OpiskeluoikeudenOrganisaatiohistoria'
import { CommonProps } from '../CommonProps'
import { KeyValueTable, KeyValueRow } from '../containers/KeyValueTable'
import { FieldEditorProps } from '../forms/FormField'
import { Spacer } from '../layout/Spacer'

export type OrganisaatiohistoriaViewProps = CommonProps<
  FieldEditorProps<OpiskeluoikeudenOrganisaatiohistoria[], {}>
>

export const OrganisaatiohistoriaView: React.FC<
  OrganisaatiohistoriaViewProps
> = (props) => {
  if (!props.value) {
    return null
  }
  return (
    <KeyValueTable testId={props.testId}>
      <KeyValueRow label={'Organisaatiohistoria'}>
        {props.value.map((row, i) => (
          <>
            <KeyValueTable key={i}>
              <KeyValueRow label="Muutospäivä">{row.muutospäivä}</KeyValueRow>
              <KeyValueRow label="Aikaisempi oppilaitos">
                {t(row.oppilaitos?.nimi)}
              </KeyValueRow>
              <KeyValueRow label="Aikaisempi koulutustoimija">
                {t(row.koulutustoimija?.nimi)}
              </KeyValueRow>
            </KeyValueTable>
            <Spacer />
          </>
        ))}
      </KeyValueRow>
    </KeyValueTable>
  )
}
