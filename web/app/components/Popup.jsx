import React from 'react'
import { t } from '../i18n/i18n'
import { focusWithoutScrolling } from '../util/util'

export class Popup extends React.Component {
  componentDidMount() {
    const { showStateAtom, onFocusValue } = this.props
    // FIXME: setTimeout is really nasty, but without it this fires before the button press is finished, and the button gets the focus back
    showStateAtom.onValue(
      (e) =>
        e === onFocusValue &&
        setTimeout(() => focusWithoutScrolling(this.popupContainer), 10)
    )
  }

  render() {
    const {
      showStateAtom,
      dismissedStateValue = false,
      inline,
      children,
      closeTitle
    } = this.props
    const dismiss = () => showStateAtom.set(dismissedStateValue)
    const closeButtonLabel = closeTitle
      ? `${t('Sulje')} ${t(closeTitle)}`
      : t('Sulje')

    return (
      <div
        className={`popup${inline ? ' popup--inline' : ''}`}
        ref={(elem) => (this.popupContainer = elem)}
        tabIndex="-1"
      >
        <div className="popup__content">
          <button
            className="popup__close-button"
            onClick={dismiss}
            aria-label={closeButtonLabel}
          />
          {children}
        </div>
      </div>
    )
  }
}
