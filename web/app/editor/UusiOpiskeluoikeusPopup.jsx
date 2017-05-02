import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import ModalDialog from './ModalDialog.jsx'
import CreateOpiskeluoikeus from '../CreateOpiskeluoikeus.jsx'

export const UusiOpiskeluoikeusPopup = ({resultCallback}) => {
  let submitBus = Bacon.Bus()
  let opiskeluoikeusAtom = Atom()
  let validP = opiskeluoikeusAtom.not().not()
  opiskeluoikeusAtom.sampledBy(submitBus).onValue(resultCallback)
  return (<form className="uusi-oppija">
    <ModalDialog className="lisaa-opiskeluoikeus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
      <h2>Opiskeluoikeuden lisäys</h2>
      <CreateOpiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
      <button disabled={validP.not()} onClick={(e) => {e.preventDefault(); submitBus.push()}}>Lisää opiskeluoikeus</button>
    </ModalDialog>
  </form>)
}