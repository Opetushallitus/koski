import React from 'react'
import Text from '../../i18n/Text'

const copyToClipboard = message => () => {
  try {
    const el = document.createElement('textarea')
    el.value = message
    document.body.appendChild(el)
    el.select()
    document.execCommand('copy')
    document.body.removeChild(el)
  } catch(e) {
    console.log(e)
  }
}

export const CopyableText = ({heading, message}) => (
  <div className='copyable-text'>
    <div className='copyable-text__heading'>
      <Text name={heading}/>
    </div>
    <textarea readOnly cols='30' rows='10' defaultValue={message}/>
    <button className='button--success' onClick={copyToClipboard(message)}>
      <Text name='Kopioi'/>
    </button>
  </div>
)
