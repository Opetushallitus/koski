import React, { useState } from 'react'
import { TestIdLayer, useTestId } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { OpiskeluoikeudenOrganisaatiohistoria } from '../../types/fi/oph/koski/schema/OpiskeluoikeudenOrganisaatiohistoria'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import { KeyValueRow, KeyValueTable } from '../containers/KeyValueTable'
import { FieldEditorProps } from '../forms/FormField'
import { Spacer } from '../layout/Spacer'
import { Trans } from '../texts/Trans'

export type OrganisaatiohistoriaViewProps = CommonProps<
  FieldEditorProps<OpiskeluoikeudenOrganisaatiohistoria[], EmptyObject>
>

export const OrganisaatiohistoriaView: React.FC<
  OrganisaatiohistoriaViewProps
> = (props) => {
  const toggleTestId = useTestId('organisaatiohistoria.toggle')
  const valueTestId = useTestId('organisaatiohistoria.value')
  const [open, setOpen] = useState(false)
  if (!props.value || props.value.length === 0) {
    return null
  }
  return (
    <TestIdLayer id="organisaatiohistoria">
      <a
        className={`expandable${open ? ' open' : ''}`}
        role="button"
        data-testid={toggleTestId}
        onClick={(e) => {
          e.preventDefault()
          setOpen((prev) => !prev)
        }}
      >
        <Trans>{'Opiskeluoikeuden organisaatiohistoria'}</Trans>
      </a>
      {open && (
        <KeyValueTable data-testid={valueTestId}>
          {props.value.map((row, i) => (
            <KeyValueRow key={i} localizableLabel="Muutos">
              <KeyValueTable>
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
            </KeyValueRow>
          ))}
        </KeyValueTable>
      )}
    </TestIdLayer>
  )
}
