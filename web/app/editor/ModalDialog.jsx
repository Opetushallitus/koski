import React from 'baret'
import Bacon from 'baconjs'
import {doActionWhileMounted} from '../util';

export default ({className, onDismiss, onSubmit, children}) => {
  let keyE = Bacon.fromEvent(document, 'keyup')

  function handleKeys(e) {
    if (e.keyCode == 27) onDismiss()
    if (e.keyCode == 13) onSubmit()
  }

  return (<div className={className + ' modal'}>
    <div className="modal-content">
      <a className="close-modal" onClick={() => onDismiss()}>&#10005;</a>
      { children }
      <a onClick={() => onDismiss()}>Peruuta</a>
    </div>
    { doActionWhileMounted(keyE, handleKeys) }
  </div>)
}