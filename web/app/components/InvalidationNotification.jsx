import React from 'baret'
import Bacon from 'baconjs'
import Text from '../i18n/Text'

// use session storage for notification status to persist over page reload
const NOTIFICATION_STORAGE_KEY = 'invalidationCompleted'

export const setInvalidationNotification = messageKey => sessionStorage.setItem(NOTIFICATION_STORAGE_KEY, messageKey)
export const resetInvalidationNotification = () => sessionStorage.removeItem(NOTIFICATION_STORAGE_KEY)

export const InvalidationNotification = ({location}) => {
  if (!location || location.path !== '/koski/virkailija' || !sessionStorage.getItem(NOTIFICATION_STORAGE_KEY)) {
    return null
  }
  let hideBus = Bacon.Bus()
  let later = Bacon.later(5000)
  let hideP = hideBus.merge(later).map('hide').toProperty('')
  hideP.filter(e => e === 'hide').onValue(resetInvalidationNotification)
  return (<div id="invalidated" className={hideP.map(hideClass => 'opiskeluoikeus-invalidated ' + hideClass)}>
    <Text name="Opiskeluoikeus mitätöity"/><a onClick={() => hideBus.push()} className="hide-invalidated-message"/>
  </div>)
}

export default InvalidationNotification
