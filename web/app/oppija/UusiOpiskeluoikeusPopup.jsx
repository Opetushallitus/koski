import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import ModalDialog from '../editor/ModalDialog'
import UusiOpiskeluoikeus from '../uusioppija/UusiOpiskeluoikeus'
import Text from '../i18n/Text'

export const UusiOpiskeluoikeusPopup = ({ resultCallback }) => {
  const submitBus = Bacon.Bus()
  const opiskeluoikeusAtom = Atom()
  const validP = opiskeluoikeusAtom.not().not()
  opiskeluoikeusAtom.sampledBy(submitBus).onValue((oo) => {
    resultCallback(oo)
  })
  return (
    <form className="uusi-oppija">
      <ModalDialog
        className="lisaa-opiskeluoikeus-modal"
        onDismiss={resultCallback}
        onSubmit={() => submitBus.push()}
        okTextKey="Lisää opiskeluoikeus"
        validP={validP}
      >
        <h2>
          <Text name="Opiskeluoikeuden lisäys" />
        </h2>
        <UusiOpiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom} />
      </ModalDialog>
    </form>
  )
}
