import React from 'react'

export const Popup = ({showStateAtom, inline, children}) => {
  const dismiss = () => showStateAtom.set(false)

  return (
    <div className={`popup${inline ? ' popup--inline' : ''}`}>
      <div className='popup__content'>
        <a className='popup__close-button' onClick={dismiss}/>
        {children}
      </div>
    </div>
  )
}
