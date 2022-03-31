import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'

const CopyState = {
  PENDING: 1,
  SUCCESS: 2,
  ERROR: 3
}

const ConfirmationDuration = 2000

const copyToClipboard = (message, copyState) => () => {
  const el = document.createElement('textarea')

  try {
    el.value = message
    el.setAttribute('readonly', '')
    document.body.appendChild(el)
    el.select()
    el.setSelectionRange(0, el.value.length)
    document.execCommand('copy')
    copyState.set(CopyState.SUCCESS)
    Bacon.later(ConfirmationDuration, CopyState.PENDING).onValue(v => copyState.set(v))
  } catch(e) {
    copyState.set(CopyState.ERROR)
  } finally {
    document.body.contains(el) && document.body.removeChild(el)
  }
}

export const CopyableText = (
  {
    heading,
    message,
    buttonText = 'Kopioi',
    buttonTextSuccess = 'Kopioitu',
    buttonTextFailure = 'Kopiointi epäonnistui',
    className,
    multiline = true,
    width,
    height
  }) => {
  const copyState = Atom(CopyState.PENDING)

  const buttonState = copyState.map(state => {
    switch (state) {
      case CopyState.PENDING: return {isDisabled: false, style: '', text: buttonText}
      case CopyState.SUCCESS: return {isDisabled: true, style: '--success', text: buttonTextSuccess}
      case CopyState.ERROR: return {isDisabled: true, style: '--error', text: buttonTextFailure}
    }
  })

  const textAreaSize = {}
  width && (textAreaSize.width = width)
  height && (textAreaSize.height = height)

  return (
    <div className={`copyable-text${className ? ' ' + className : ''}`}>
      {heading &&
        <div className='copyable-text__heading'>
          <Text name={heading}/>
        </div>}
      {multiline
        ? <textarea readOnly defaultValue={message} style={textAreaSize}/>
        : <input readOnly type='text' value={message} style={width && {width}}/>}
      {buttonState.map(({isDisabled, style, text}) => (
        <button
          className={style ? `koski-button koski-button${style}` : 'koski-button'}
          disabled={isDisabled}
          style={height && {height}}
          onClick={copyToClipboard(message, copyState)}
        >
          <Text name={text}/>
        </button>
      ))}
    </div>
  )
}

CopyableText.displayName = 'CopyableText'
