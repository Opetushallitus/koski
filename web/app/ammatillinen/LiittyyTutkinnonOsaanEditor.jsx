import * as R from 'ramda'
import React from 'baret'
import Atom from 'bacon.atom'
import {
  modelData,
  modelSetData,
  modelTitle,
  pushModel
} from '../editor/EditorModel'
import TutkinnonOsaToisestaTutkinnostaPicker from './TutkinnonOsaToisestaTutkinnostaPicker'
import { Editor } from '../editor/Editor'

const LiittyyTutkinnonOsaanEditor = ({ model }) =>
  model.context.edit ? (
    <LiittyyTutkinnonOsaanPicker model={model} />
  ) : (
    <Editor model={model} />
  )

class LiittyyTutkinnonOsaanPicker extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      liittyyTutkintoonAtom: Atom(),
      liittyyTutkinnonOsaanAtom: Atom()
    }
  }

  render() {
    const { model } = this.props
    const { liittyyTutkintoonAtom, liittyyTutkinnonOsaanAtom } = this.state
    return (
      <TutkinnonOsaToisestaTutkinnostaPicker
        tutkintoAtom={liittyyTutkintoonAtom}
        tutkinnonOsaAtom={liittyyTutkinnonOsaanAtom}
        oppilaitos={modelData(model.context.suoritus, 'toimipiste')}
        tutkintoTitle=""
        tutkinnonOsaTitle=""
        tutkintoPlaceholder="Valitse ensin tutkinto"
        autoFocus={false}
      />
    )
  }

  componentDidMount() {
    const { model } = this.props
    const { liittyyTutkintoonAtom, liittyyTutkinnonOsaanAtom } = this.state
    const liittyyTutkinnonOsaanData = modelData(model)
    liittyyTutkintoonAtom
      .filter((x) => !!x)
      .onValue(() => liittyyTutkinnonOsaanAtom.set(undefined))
    liittyyTutkinnonOsaanData &&
      liittyyTutkinnonOsaanAtom.set({
        data: liittyyTutkinnonOsaanData,
        title: modelTitle(model)
      })
    liittyyTutkinnonOsaanAtom
      .filter((x) => !!x && !R.equals(liittyyTutkinnonOsaanData, x.data))
      .onValue((liittyyTutkinnonOsaan) => {
        pushModel(modelSetData(model, liittyyTutkinnonOsaan.data))
      })
  }
}

export default LiittyyTutkinnonOsaanEditor
