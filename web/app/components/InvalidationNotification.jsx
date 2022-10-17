import React from 'baret'
import Bacon from 'baconjs'
import Text from '../i18n/Text'

// use session storage for notification status to persist over page reload
const NOTIFICATION_STORAGE_KEY = 'invalidationCompleted'

export const setInvalidationNotification = (messageKey) =>
  sessionStorage.setItem(NOTIFICATION_STORAGE_KEY, messageKey)
export const resetInvalidationNotification = () =>
  sessionStorage.removeItem(NOTIFICATION_STORAGE_KEY)

export class InvalidationNotification extends React.PureComponent {
  render() {
    const notification = sessionStorage.getItem(NOTIFICATION_STORAGE_KEY)
    if (!notification) return null

    const hideBus = Bacon.Bus()
    const later = Bacon.later(5000)
    const hideP = hideBus.merge(later).map('hide').toProperty('')
    hideP.filter((e) => e === 'hide').onValue(resetInvalidationNotification)

    return (
      <div
        id="invalidated"
        className={hideP.map(
          (hideClass) => 'invalidation-notification ' + hideClass
        )}
      >
        <Text name={notification} />
        <a
          onClick={() => hideBus.push()}
          className="hide-invalidation-notification"
        />
      </div>
    )
  }
}

export default InvalidationNotification
