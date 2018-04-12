import React, {fromBacon} from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import R from 'ramda'
import {modelData} from '../editor/EditorModel.js'
import {pushModelValue} from '../editor/EditorModel'
import {wrapOptional} from '../editor/EditorModel'
import {StringEditor} from '../editor/StringEditor'
import {PerusteDropdown} from './PerusteDropdown'
import Http from '../util/http'

export const PerusteEditor = ({model}) => {
  if (!model.context.edit) {
    const peruste = modelData(model)
    return peruste ? fromBacon(perusteLinkki(peruste, <StringEditor model={model}/>)) : <StringEditor model={model}/>
  }
  model = wrapOptional(model)
  let perusteAtom = Atom(modelData(model))
  perusteAtom.filter(R.identity).changes().onValue(diaarinumero => pushModelValue(model, { data: diaarinumero }))
  return <PerusteDropdown {...{perusteAtom, suoritusTyyppiP: Bacon.constant(modelData(model.context.suoritus, 'tyyppi'))}}/>
}

PerusteEditor.handlesOptional=() => true

const perusteLinkki = (peruste, perusteEditor) => {
  const map404 = { errorMapper: (e) => e.httpStatus === 404 ? Bacon.never() : Bacon.Error(e) }
  return Http.cachedGet(`/koski/api/tutkinnonperusteet/peruste/${encodeURIComponent(peruste)}/linkki`, map404).map('.url').map(linkki =>
    <a target="top" href={linkki}>{perusteEditor}</a>
  ).startWith(perusteEditor)
}
