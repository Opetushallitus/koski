import React from 'baret'
import ModalDialog from '../editor/ModalDialog'
import Atom from 'bacon.atom'

export const VirtaVirheetPopup = ({ virheet, onDismiss }) => {
  const checkboxAtom = Atom(false)
  console.log(virheet)

  const virheetTyypeittäin = {}
  for (const virhe of virheet) {
    if (!virheetTyypeittäin[virhe.tyyppi]) virheetTyypeittäin[virhe.tyyppi] = []
    virheetTyypeittäin[virhe.tyyppi].push(virhe.arvo)
  }
  console.log(virheetTyypeittäin)

  return (
    <ModalDialog
      className="peru-suostumus-popup-modal"
      onDismiss={onDismiss}
      submitOnEnterKey="false"
      validP={checkboxAtom}
    >
      {Object.keys(virheetTyypeittäin).map(function (key, i) {
        return (
          <span key={`${key}_${i}`}>
            {"Avaimella '" +
              key +
              "' löytyi virheet: " +
              virheetTyypeittäin[key].join(', ')}
          </span>
        )
      })}
    </ModalDialog>
  )
}
