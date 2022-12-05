import React from 'baret'
import Atom from 'bacon.atom'
import { modelData, modelSetTitle, modelSetValues } from '../editor/EditorModel'
import TutkinnonOsaToisestaTutkinnostaPicker from './TutkinnonOsaToisestaTutkinnostaPicker'
import ModalDialog from '../editor/ModalDialog'
import Text from '../i18n/Text'
import { ift } from '../util/util'

export const LisääOsaToisestaTutkinnosta = ({
  lisättävätTutkinnonOsat,
  suoritus,
  koulutusmoduuliProto,
  addTutkinnonOsa,
  diaarinumero
}) => {
  const oppilaitos = modelData(suoritus, 'toimipiste')
  const lisääOsaToisestaTutkinnostaAtom = Atom(false)
  const lisääOsaToisestaTutkinnosta = (tutkinto, osa) => {
    lisääOsaToisestaTutkinnostaAtom.set(false)
    if (osa) {
      addTutkinnonOsa(
        modelSetTitle(
          modelSetValues(koulutusmoduuliProto(), { tunniste: osa }),
          osa.title
        ),
        tutkinto.diaarinumero !== diaarinumero && tutkinto
      )
    }
  }
  const tutkintoAtom = Atom()
  const tutkinnonOsaAtom = Atom()
  tutkintoAtom.onValue(() => tutkinnonOsaAtom.set(undefined))

  return (
    <span className="osa-toisesta-tutkinnosta">
      {lisättävätTutkinnonOsat.osaToisestaTutkinnosta && (
        <a
          className="add-link"
          onClick={() => lisääOsaToisestaTutkinnostaAtom.set(true)}
        >
          <Text name="Lisää tutkinnon osa toisesta tutkinnosta" />
        </a>
      )}
      {ift(
        lisääOsaToisestaTutkinnostaAtom,
        <ModalDialog
          className="lisaa-tutkinnon-osa-toisesta-tutkinnosta-modal"
          onDismiss={lisääOsaToisestaTutkinnosta}
          onSubmit={() =>
            lisääOsaToisestaTutkinnosta(
              tutkintoAtom.get(),
              tutkinnonOsaAtom.get()
            )
          }
          okTextKey="Lisää tutkinnon osa"
          validP={tutkinnonOsaAtom}
          submitOnEnterKey="false"
        >
          <h2>
            <Text name="Tutkinnon osan lisäys toisesta tutkinnosta" />
          </h2>
          <TutkinnonOsaToisestaTutkinnostaPicker
            {...{ tutkintoAtom, tutkinnonOsaAtom, oppilaitos }}
          />
        </ModalDialog>
      )}
    </span>
  )
}
