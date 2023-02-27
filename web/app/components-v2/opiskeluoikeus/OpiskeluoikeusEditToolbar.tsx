import React, { useState } from 'react'
import { useApiMethod, useOnApiSuccess } from '../../api-fetch'
import { formatDateRange } from '../../date/date'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { invalidateOpiskeluoikeus } from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { RequiresInvalidateAccess } from '../access/RequiresInvalidateAccess'
import { RequiresWriteAccess } from '../access/RequiresWriteAccess'
import { Column, ColumnRow } from '../containers/Columns'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { Trans } from '../texts/Trans'

export type OpiskeluoikeusEditToolbarProps = {
  opiskeluoikeus: Opiskeluoikeus
  editMode: boolean
  invalidatable: boolean
  onStartEdit: () => void
}

export const OpiskeluoikeusEditToolbar = (
  props: OpiskeluoikeusEditToolbarProps
) => {
  const spans = props.editMode ? [12, 12] : [21, 3]
  const opiskeluoikeusOid = getOpiskeluoikeusOid(props.opiskeluoikeus)

  return (
    <ColumnRow>
      <Column
        span={{ default: spans[0], phone: 24 }}
        testId="opiskeluoikeus.voimassaoloaika"
      >
        <Trans>{'Opiskeluoikeuden voimassaoloaika'}</Trans>
        {': '}
        {formatDateRange(
          props.opiskeluoikeus.alkamispäivä,
          props.opiskeluoikeus.päättymispäivä
        )}
      </Column>
      <Column
        span={{ default: spans[1], phone: 24 }}
        align={{ default: 'right', phone: 'left' }}
      >
        {props.editMode ? (
          props.invalidatable &&
          opiskeluoikeusOid && (
            <RequiresInvalidateAccess>
              <MitätöintiButton opiskeluoikeusOid={opiskeluoikeusOid} />
            </RequiresInvalidateAccess>
          )
        ) : (
          <RequiresWriteAccess>
            <RaisedButton
              fullWidth
              onClick={props.onStartEdit}
              testId="opiskeluoikeus.edit"
            >
              {'Muokkaa'}
            </RaisedButton>
          </RequiresWriteAccess>
        )}
      </Column>
    </ColumnRow>
  )
}

type MitätöintiButtonProps = {
  opiskeluoikeusOid: string
}

const MitätöintiButton: React.FC<MitätöintiButtonProps> = (props) => {
  const invalidate = useApiMethod(invalidateOpiskeluoikeus)
  const [confirmationVisible, setConfirmationVisible] = useState(false)
  useOnApiSuccess(invalidate, () => {
    window.location.replace('/koski/virkailija')
  })

  return confirmationVisible ? (
    <>
      <RaisedButton
        type="dangerzone"
        onClick={() => invalidate.call(props.opiskeluoikeusOid)}
        testId="opiskeluoikeus.invalidate.confirm"
      >
        {'Vahvista mitätöinti, operaatiota ei voi peruuttaa'}
      </RaisedButton>
      <FlatButton
        onClick={() => setConfirmationVisible(false)}
        testId="opiskeluoikeus.invalidate.cancel"
      >
        {'Peruuta mitätöinti'}
      </FlatButton>
    </>
  ) : (
    <FlatButton
      onClick={() => setConfirmationVisible(true)}
      testId="opiskeluoikeus.invalidate.button"
    >
      {'Mitätöi opiskeluoikeus'}
    </FlatButton>
  )
}
