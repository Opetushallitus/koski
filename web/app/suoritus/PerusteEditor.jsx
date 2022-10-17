import React, { fromBacon } from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import * as R from 'ramda'
import { modelData, pushModelValue, wrapOptional } from '../editor/EditorModel'
import { StringEditor } from '../editor/StringEditor'
import { PerusteDropdown } from './PerusteDropdown'
import Http from '../util/http'
import { lang } from '../i18n/i18n'

export const PerusteEditor = ({ model }) => {
  if (!model.context.edit) {
    const peruste = modelData(model)
    const paattymispaiva = modelData(
      model.context.opiskeluoikeus
    ).päättymispäivä
    return peruste ? (
      fromBacon(
        perusteLinkki(peruste, <StringEditor model={model} />, paattymispaiva)
      )
    ) : (
      <StringEditor model={model} />
    )
  }
  model = wrapOptional(model)
  const perusteAtom = Atom(modelData(model))
  perusteAtom
    .filter(R.identity)
    .changes()
    .onValue((diaarinumero) => pushModelValue(model, { data: diaarinumero }))
  return (
    <PerusteDropdown
      {...{
        perusteAtom,
        suoritusTyyppiP: Bacon.constant(
          modelData(model.context.suoritus, 'tyyppi')
        )
      }}
    />
  )
}

PerusteEditor.handlesOptional = () => true

const perusteLinkki = (peruste, perusteEditor, paattymispaiva) => {
  const map404 = {
    errorMapper: (e) => (e.httpStatus === 404 ? Bacon.never() : Bacon.Error(e))
  }
  const paattymisquery = paattymispaiva
    ? '&päättymispäivä=' + paattymispaiva
    : ''
  return Http.cachedGet(
    `/koski/api/tutkinnonperusteet/peruste/${encodeURIComponent(
      peruste
    )}/linkki?lang=${encodeURIComponent(lang)}${paattymisquery}`,
    map404
  )
    .map('.url')
    .map((linkki) => (
      <a target="_blank" href={linkki} rel="noopener noreferrer">
        {perusteEditor}
      </a>
    ))
    .startWith(perusteEditor)
}
