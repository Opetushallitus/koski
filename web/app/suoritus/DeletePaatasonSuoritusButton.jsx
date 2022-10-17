import React from 'react'
import { deletePäätasonSuoritus } from '../virkailija/VirkailijaOppijaView'
import ButtonWithConfirmation from '../components/ButtonWithConfirmation'

export default ({ opiskeluoikeus, päätasonSuoritus }) => (
  <div className="delete-paatason-suoritus-container">
    <ButtonWithConfirmation
      text="Poista suoritus"
      confirmationText="Vahvista poisto, operaatiota ei voi peruuttaa"
      cancelText="Peruuta poisto"
      action={() => deletePäätasonSuoritus(opiskeluoikeus, päätasonSuoritus)}
      className="invalidate delete-paatason-suoritus"
      confirmationClassName="confirm-invalidate delete-paatason-suoritus__confirm"
    />
  </div>
)
