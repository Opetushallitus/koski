import React from 'baret'
import { Editor } from '../editor/Editor'
import Atom from 'bacon.atom'
import { modelData, modelSetData, pushModel } from '../editor/EditorModel'
import * as R from 'ramda'
import TutkintoAutocomplete from '../virkailija/TutkintoAutocomplete'
import Bacon from 'baconjs'
import Text from '../i18n/Text'

const LiittyyTutkintoonEditor = ({ model }) => {
  return model.context.edit ? (
    <LiittyyTutkintoonPicker model={model} />
  ) : (
    <Editor model={model} />
  )
}

class LiittyyTutkintoonPicker extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      liittyyTutkintoonAtom: Atom()
    }
  }

  render() {
    const { model } = this.props
    const { liittyyTutkintoonAtom } = this.state
    return (
      <TutkintoPicker
        tutkintoAtom={liittyyTutkintoonAtom}
        oppilaitos={modelData(model.context.suoritus, 'toimipiste')}
        tutkintoTitle=""
        tutkintoPlaceholder="Valitse tutkinto"
        autoFocus={false}
      />
    )
  }

  componentDidMount() {
    const { model } = this.props
    const { liittyyTutkintoonAtom } = this.state
    const liittyyTutkintoonData = modelData(model)
    liittyyTutkintoonData &&
      liittyyTutkintoonAtom.set({
        diaarinumero: liittyyTutkintoonData.perusteenDiaarinumero,
        tutkintoKoodi: liittyyTutkintoonData.tunniste.koodiarvo,
        nimi: liittyyTutkintoonData.tunniste.nimi
      })
    liittyyTutkintoonAtom
      .filter((x) => !!x && !R.equals(liittyyTutkintoonData, x.data))
      .onValue((liittyyTutkintoon) => {
        pushModel(
          modelSetData(model, {
            tunniste: {
              koodiarvo: liittyyTutkintoon.tutkintoKoodi,
              nimi: liittyyTutkintoon.nimi,
              koodistoUri: 'koulutus'
            },
            perusteenDiaarinumero: liittyyTutkintoon.diaarinumero
          })
        )
      })
  }
}

const TutkintoPicker = ({
  tutkintoAtom,
  oppilaitos,
  autoFocus = true,
  tutkintoTitle = 'Tutkinto',
  tutkintoPlaceholder = ''
}) => (
  <div className="valinnat">
    <TutkintoAutocomplete
      autoFocus={autoFocus}
      tutkintoAtom={tutkintoAtom}
      oppilaitosP={Bacon.constant(oppilaitos)}
      title={<Text name={tutkintoTitle} />}
      placeholder={tutkintoPlaceholder}
    />
  </div>
)

export default LiittyyTutkintoonEditor
