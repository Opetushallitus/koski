import React, { useState } from 'react'
import { useApiMethod, useOnApiSuccess } from '../../api-fetch'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { formatDateRange } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import {
  invalidateOpiskeluoikeus,
  puraLähdejärjestelmäkytkentä
} from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { RequiresWriteAccess } from '../access/RequiresWriteAccess'
import { Column, ColumnRow } from '../containers/Columns'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { Trans } from '../texts/Trans'
import { useVirkailijaUser } from '../../appstate/user'
import { useVersionumero } from '../../appstate/useSearchParam'
import { PoistuVersiohistoriastaButton } from './VersiohistoriaButton'
import { RequiresLahdejarjestelmakytkennanPurkaminenAccess } from '../access/RequiresLahdejarjestelmakytkennanPurkaminenAccess'
import { setInvalidationNotification } from '../../components/InvalidationNotification'

export type OpiskeluoikeusEditToolbarProps = {
  opiskeluoikeus: Opiskeluoikeus
  showEditButton: boolean
  invalidatable: boolean
  onStartEdit: () => void
}

export const OpiskeluoikeusEditToolbar = (
  props: OpiskeluoikeusEditToolbarProps
) => {
  const spans = props.showEditButton ? [12, 12] : [16, 8]
  const opiskeluoikeusOid = getOpiskeluoikeusOid(props.opiskeluoikeus)
  const hasAnyInvalidateAccess = useVirkailijaUser()?.hasAnyInvalidateAccess
  const inVersiohistoria = useVersionumero() !== null

  return (
    <ColumnRow>
      <Column span={{ default: spans[0], phone: 24 }}>
        <TestIdText id="voimassaoloaika">
          <Trans>{'Opiskeluoikeuden voimassaoloaika'}</Trans>
          {': '}
          {'arvioituPäättymispäivä' in props.opiskeluoikeus &&
          !props.opiskeluoikeus.päättymispäivä
            ? `${formatDateRange(
                props.opiskeluoikeus.alkamispäivä,
                props.opiskeluoikeus.arvioituPäättymispäivä
              )} (${t('Arvioitu päättymispäivä')})`
            : formatDateRange(
                props.opiskeluoikeus.alkamispäivä,
                props.opiskeluoikeus.päättymispäivä
              )}
        </TestIdText>
      </Column>
      <Column
        span={{ default: spans[1], phone: 24 }}
        align={{ default: 'right', phone: 'left' }}
      >
        {hasAnyInvalidateAccess && props.invalidatable && opiskeluoikeusOid && (
          <MitätöintiButton opiskeluoikeusOid={opiskeluoikeusOid} />
        )}
        <RequiresWriteAccess opiskeluoikeus={props.opiskeluoikeus}>
          {inVersiohistoria ? (
            <PoistuVersiohistoriastaButton
              opiskeluoikeusOid={opiskeluoikeusOid}
            />
          ) : props.showEditButton ? (
            <RaisedButton onClick={props.onStartEdit} testId="edit">
              {t('Muokkaa')}
            </RaisedButton>
          ) : (
            !hasAnyInvalidateAccess &&
            opiskeluoikeusOid && (
              <MitätöintiButton opiskeluoikeusOid={opiskeluoikeusOid} />
            )
          )}
        </RequiresWriteAccess>
        <RequiresLahdejarjestelmakytkennanPurkaminenAccess
          opiskeluoikeus={props.opiskeluoikeus}
        >
          {opiskeluoikeusOid && (
            <LähdejärjestelmäkytkennänPurkaminenButton
              opiskeluoikeusOid={opiskeluoikeusOid}
            />
          )}
        </RequiresLahdejarjestelmakytkennanPurkaminenAccess>
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

  return (
    <TestIdLayer id="invalidate">
      {confirmationVisible ? (
        <>
          <RaisedButton
            type="dangerzone"
            onClick={() => invalidate.call(props.opiskeluoikeusOid)}
            testId="confirm"
          >
            {t('Vahvista mitätöinti, operaatiota ei voi peruuttaa')}
          </RaisedButton>
          <FlatButton
            onClick={() => setConfirmationVisible(false)}
            testId="cancel"
          >
            {t('Peruuta mitätöinti')}
          </FlatButton>
        </>
      ) : (
        <FlatButton
          onClick={() => setConfirmationVisible(true)}
          testId="button"
        >
          {t('Mitätöi opiskeluoikeus')}
        </FlatButton>
      )}
    </TestIdLayer>
  )
}

type LähdejärjestelmäkytkennänPurkaminenButtonProps = {
  opiskeluoikeusOid: string
}

const LähdejärjestelmäkytkennänPurkaminenButton: React.FC<
  LähdejärjestelmäkytkennänPurkaminenButtonProps
> = (props) => {
  const puraKytkentä = useApiMethod(puraLähdejärjestelmäkytkentä)
  const [confirmationVisible, setConfirmationVisible] = useState(false)

  useOnApiSuccess(puraKytkentä, () => {
    setInvalidationNotification('Lähdejärjestelmäkytkentä purettu')
    location.reload()
  })

  return (
    <TestIdLayer id="puraKytkenta">
      {confirmationVisible ? (
        <>
          <RaisedButton
            type="dangerzone"
            onClick={() => puraKytkentä.call(props.opiskeluoikeusOid)}
            testId="confirm"
          >
            {t(
              'Vahvista lähdejärjestelmäkytkennän purkaminen, operaatiota ei voi peruuttaa'
            )}
          </RaisedButton>
          <FlatButton
            onClick={() => setConfirmationVisible(false)}
            testId="cancel"
          >
            {t('Peruuta lähdejärjestelmäkytkennän purkaminen')}
          </FlatButton>
        </>
      ) : (
        <FlatButton
          onClick={() => setConfirmationVisible(true)}
          testId="button"
        >
          {t('Pura lähdejärjestelmäkytkentä')}
        </FlatButton>
      )}
    </TestIdLayer>
  )
}
