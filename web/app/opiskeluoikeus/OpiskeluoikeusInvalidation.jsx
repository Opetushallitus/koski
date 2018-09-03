import React from 'baret'
import Bacon from 'baconjs'
import Text from '../i18n/Text'
import {modelData} from '../editor/EditorModel'
import {invalidateOpiskeluoikeus} from '../virkailija/VirkailijaOppijaView'
import ButtonWithConfirmation from '../components/ButtonWithConfirmation'

export const setOpiskeluoikeusInvalidated = () => sessionStorage.setItem('opiskeluoikeusInvalidated', true)
export const resetOpiskeluoikeusInvalidated = () => sessionStorage.removeItem('opiskeluoikeusInvalidated')

export const OpiskeluoikeusInvalidatedMessage = ({location}) => {
  if (!location || location.path !== '/koski/virkailija' || !sessionStorage.getItem('opiskeluoikeusInvalidated')) {
    return null
  }
  let hideBus = Bacon.Bus()
  let later = Bacon.later(5000)
  let hideP = hideBus.merge(later).map('hide').toProperty('')
  hideP.filter(e => e === 'hide').onValue(resetOpiskeluoikeusInvalidated)
  return (<div id="invalidated" className={hideP.map(hideClass => 'opiskeluoikeus-invalidated ' + hideClass)}>
    <Text name="Opiskeluoikeus mitätöity"/><a onClick={() => hideBus.push()} className="hide-invalidated-message"/>
  </div>)
}

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

