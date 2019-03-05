import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelSetData, modelTitle, pushModel} from '../editor/EditorModel'
import TutkinnonOsaToisestaTutkinnostaPicker from './TutkinnonOsaToisestaTutkinnostaPicker'
import {Editor} from '../editor/Editor'

const LiittyyTutkinnonOsaanEditor = ({model}) => {
  const liittyyTutkintoon = Atom()
  const liittyyTutkinnonOsaanData = modelData(model)
  const liittyyTutkinnonOsaanAtom = Atom(liittyyTutkinnonOsaanData && {data: liittyyTutkinnonOsaanData, title: modelTitle(model)})
  liittyyTutkinnonOsaanAtom.filter(x => !!x).onValue(liittyyTutkinnonOsaan => {
    pushModel(modelSetData(model, liittyyTutkinnonOsaan.data))
  })

  return model.context.edit
    ? <TutkinnonOsaToisestaTutkinnostaPicker
        tutkintoAtom={liittyyTutkintoon}
        tutkinnonOsaAtom={liittyyTutkinnonOsaanAtom}
        oppilaitos={modelData(model.context.suoritus, 'toimipiste')}
        tutkintoTitle=''
        tutkinnonOsaTitle=''
        tutkintoPlaceholder='Valitse ensin tutkinto'
        autoFocus={false}
      />
    : <Editor model={model} />
}

export default LiittyyTutkinnonOsaanEditor
