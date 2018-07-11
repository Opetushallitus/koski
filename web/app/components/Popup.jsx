import React from 'react'
import {t} from '../i18n/i18n'

export const Popup = ({showStateAtom, dismissedStateValue = false, inline, children, closeTitle}) => {
  const dismiss = () => showStateAtom.set(dismissedStateValue)
  const closeButtonLabel = closeTitle ? `${t('Sulje')} ${t(closeTitle)}` : t('Sulje')

  return (
    <div className={`popup${inline ? ' popup--inline' : ''}`}>
      <div className='popup__content'>
        <button className='popup__close-button' onClick={dismiss} aria-label={closeButtonLabel}/>
        {children}
      </div>
    </div>
  )
}
