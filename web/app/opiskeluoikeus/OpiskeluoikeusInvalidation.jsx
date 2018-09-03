import React from 'react'
import {modelData} from '../editor/EditorModel'
import {invalidateOpiskeluoikeus} from '../virkailija/VirkailijaOppijaView'
import ButtonWithConfirmation from '../components/ButtonWithConfirmation'

export const InvalidateOpiskeluoikeusButton = ({opiskeluoikeus}) => (
  <ButtonWithConfirmation
    text='Mitätöi opiskeluoikeus'
    confirmationText='Vahvista mitätöinti, operaatiota ei voi peruuttaa'
    cancelText='Peruuta mitätöinti'
    action={() => invalidateOpiskeluoikeus(modelData(opiskeluoikeus, 'oid'))}
    className='invalidate'
    confirmationClassName='confirm-invalidate'
  />
)
