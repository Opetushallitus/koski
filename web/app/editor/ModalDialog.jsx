import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {doActionWhileMounted, parseBool} from '../util/util'
import Text from '../i18n/Text'

export default ({className, onDismiss, onSubmit, children, submitOnEnterKey, okTextKey = 'Ok', cancelTextKey = 'Peruuta', validP = Bacon.constant(true), fullscreen = false}) => {
  submitOnEnterKey = parseBool(submitOnEnterKey, true)
  let submittedAtom = Atom(false)
  let keyE = Bacon.fromEvent(document, 'keyup')
  function handleKeys(e) {
    if (e.keyCode == 27) onDismiss()
    if (e.keyCode == 13 && submitOnEnterKey) onSubmit()
  }
  let classNameP = submittedAtom.map(submitted => className + ' modal' + (submitted ? ' submitted' : '') + (fullscreen ? ' fullscreen' : ''))
  return (<div className={classNameP}>
    <div className='modal-shield' onClick={() => onDismiss()}/>
    <div className='modal-content'>
      <a className='close-modal' onClick={() => onDismiss()}/>
      { children }
      <div className='actions'>
        <a className='peruuta' onClick={() => onDismiss()}><Text name={cancelTextKey}/></a>
        <button className='koski-button' disabled={validP.not().or(submittedAtom)} onClick={(e) => {e.preventDefault(); submittedAtom.set(true); onSubmit()}}><Text name={okTextKey}/></button>
      </div>
    </div>
    { doActionWhileMounted(keyE, handleKeys) }
  </div>)
}
