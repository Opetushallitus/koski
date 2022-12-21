import React from 'react'
import { formatDateRange } from '../../date/date'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { RaisedButton } from '../controls/RaisedButton'
import { ColumnGrid, Column } from '../containers/ColumnGrid'
import { Trans } from '../texts/Trans'

export type OpiskeluoikeusEditToolbarProps = {
  opiskeluoikeus: Opiskeluoikeus
  editMode: boolean
  onStartEdit: () => void
}

export const OpiskeluoikeusEditToolbar = (
  props: OpiskeluoikeusEditToolbarProps
) => (
  <ColumnGrid>
    <Column span={21} spanPhone={24}>
      <Trans>Opiskeluoikeuden voimassaoloaika</Trans>:{' '}
      {formatDateRange(
        props.opiskeluoikeus.alkamispäivä,
        props.opiskeluoikeus.päättymispäivä
      )}
    </Column>
    <Column span={3} spanPhone={24}>
      {!props.editMode && (
        <RaisedButton fullWidth onClick={props.onStartEdit}>
          Muokkaa
        </RaisedButton>
      )}
    </Column>
  </ColumnGrid>
)
