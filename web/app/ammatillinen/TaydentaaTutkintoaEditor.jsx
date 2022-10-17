import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import TutkintoAutocomplete from '../virkailija/TutkintoAutocomplete'
import {
  modelData,
  modelEmpty,
  modelSetData,
  pushModel,
  resetOptionalModel
} from '../editor/EditorModel'
import { OptionalEditor } from '../editor/OptionalEditor'
import * as R from 'ramda'

const TäydentääTutkintoaEditor = ({ model }) =>
  model.context.edit && !modelEmpty(model) ? (
    <TutkintoPicker model={model} />
  ) : (
    <OptionalEditor model={model} />
  )

const TutkintoPicker = ({ model }) => {
  const data = modelData(model)
  const tutkinto = data.tunniste && {
    diaarinumero: data.perusteenDiaarinumero,
    tutkintoKoodi: data.tunniste.koodiarvo,
    nimi: data.tunniste.nimi
  }
  const tutkintoAtom = Atom(tutkinto)
  tutkintoAtom
    .filter((x) => !!x && !R.equals(tutkinto, x))
    .onValue((tut) => {
      pushModel(
        modelSetData(model, {
          tunniste: {
            koodiarvo: tut.tutkintoKoodi,
            nimi: tut.nimi,
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: tut.diaarinumero
        })
      )
    })

  return (
    <div>
      <TutkintoAutocomplete
        autoFocus="true"
        tutkintoAtom={tutkintoAtom}
        oppilaitosP={Bacon.constant(
          modelData(model.context.suoritus, 'toimipiste')
        )}
      />
      <a className="remove-value" onClick={() => resetOptionalModel(model)} />
    </div>
  )
}

export default TäydentääTutkintoaEditor
