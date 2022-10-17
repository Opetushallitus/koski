import React from 'react'
import { modelData } from '../editor/EditorModel'
import { invalidateOpiskeluoikeus } from '../virkailija/VirkailijaOppijaView'
import ButtonWithConfirmation from '../components/ButtonWithConfirmation'

export default ({ opiskeluoikeus }) => (
  <ButtonWithConfirmation
    text="Mitätöi opiskeluoikeus"
    confirmationText="Vahvista mitätöinti, operaatiota ei voi peruuttaa"
    cancelText="Peruuta mitätöinti"
    action={() => invalidateOpiskeluoikeus(modelData(opiskeluoikeus, 'oid'))}
    className="invalidate invalidate-opiskeluoikeus"
    confirmationClassName="confirm-invalidate invalidate-opiskeluoikeus__confirm"
  />
)
