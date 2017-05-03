import React from 'baret'
import Bacon from 'baconjs'
import {doActionWhileMounted, parseBool, toObservable} from '../util'

export default ({className, onDismiss, onSubmit, children, submitOnEnterKey}) => {
  className = toObservable(className)
  submitOnEnterKey = parseBool(submitOnEnterKey, true)
  let keyE = Bacon.fromEvent(document, 'keyup')

  function handleKeys(e) {
    if (e.keyCode == 27) onDismiss()
    if (e.keyCode == 13 && submitOnEnterKey) onSubmit()
  }

  return (<div className={className.map(cn => cn + ' modal')}>
    <div className="modal-shield" onClick={() => onDismiss()}/>
    <div className="modal-content">
      <a className="close-modal" onClick={() => onDismiss()}>&#10005;</a>
      { children }
      <a onClick={() => onDismiss()}>Peruuta</a>
    </div>
    { doActionWhileMounted(keyE, handleKeys) }
  </div>)
}