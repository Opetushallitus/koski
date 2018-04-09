import React from 'react'

export const Popup = ({showStateAtom, dismissedStateValue = false, inline, children}) => {
  const dismiss = () => showStateAtom.set(dismissedStateValue)

  return (
    <div className={`popup${inline ? ' popup--inline' : ''}`}>
      <div className='popup__content'>
        <a className='popup__close-button' onClick={dismiss}/>
        {children}
      </div>
    </div>
  )
}
