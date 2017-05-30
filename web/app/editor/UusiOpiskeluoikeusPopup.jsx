import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import ModalDialog from './ModalDialog.jsx'
import UusiOpiskeluoikeus from '../uusioppija/UusiOpiskeluoikeus.jsx'
import Text from '../Text.jsx'

export const UusiOpiskeluoikeusPopup = ({resultCallback}) => {
  let submitBus = Bacon.Bus()
  let opiskeluoikeusAtom = Atom()
  let validP = opiskeluoikeusAtom.not().not()
  opiskeluoikeusAtom.sampledBy(submitBus).onValue((oo) => {
    resultCallback(oo)
  })
  return (<form className="uusi-oppija">
    <ModalDialog className="lisaa-opiskeluoikeus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää opiskeluoikeus" validP={validP} >
      <h2><Text name="Opiskeluoikeuden lisäys"/></h2>
      <UusiOpiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
    </ModalDialog>
  </form>)
}