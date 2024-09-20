import React from 'react'
import { useTestId } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { OpiskeluoikeudenOrganisaatiohistoria } from '../../types/fi/oph/koski/schema/OpiskeluoikeudenOrganisaatiohistoria'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import { KeyValueRow, KeyValueTable } from '../containers/KeyValueTable'
import { FieldEditorProps } from '../forms/FormField'
import { Spacer } from '../layout/Spacer'

export type OrganisaatiohistoriaViewProps = CommonProps<
  FieldEditorProps<OpiskeluoikeudenOrganisaatiohistoria[], EmptyObject>
>

export const OrganisaatiohistoriaView: React.FC<
  OrganisaatiohistoriaViewProps
> = (props) => {
  const testId = useTestId('organisaatiohistoria.value')
  if (!props.value) {
    return null
  }
  return (
    <KeyValueTable data-testid={testId}>
      <KeyValueRow localizableLabel={'Organisaatiohistoria'}>
        {props.value.map((row, i) => (
          <>
            <KeyValueTable key={i}>
              <KeyValueRow localizableLabel="Muutospäivä">
                {row.muutospäivä}
              </KeyValueRow>
              <KeyValueRow localizableLabel="Aikaisempi oppilaitos">
                {t(row.oppilaitos?.nimi)}
              </KeyValueRow>
              <KeyValueRow localizableLabel="Aikaisempi koulutustoimija">
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
