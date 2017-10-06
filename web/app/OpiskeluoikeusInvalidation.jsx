import React from 'baret'
import Bacon from 'baconjs'
import Text from './Text.jsx'
import {modelData, modelItems} from './editor/EditorModel'
import {suoritusValmis} from './editor/Suoritus'
import {invalidateOpiskeluoikeus} from './Oppija.jsx'

export const setOpiskeluoikeusInvalidated = () => sessionStorage.setItem('opiskeluoikeusInvalidated', true)
export const resetOpiskeluoikeusInvalidated = () => sessionStorage.removeItem('opiskeluoikeusInvalidated')

export const OpiskeluoikeusInvalidatedMessage = () => {
  if (!sessionStorage.getItem('opiskeluoikeusInvalidated')) {
    return null
  }
  let hideBus = Bacon.Bus()
  let later = Bacon.later(10000)
  let hideP = hideBus.merge(later).map('hide').toProperty('')
  hideP.filter(e => e === 'hide').onValue(resetOpiskeluoikeusInvalidated)
  return (<div id="invalidated" className={hideP.map(hideClass => 'opiskeluoikeus-invalidated ' + hideClass)}>
    <Text name="Opiskeluoikeus mitätöity"/><a onClick={() => hideBus.push()} className="hide-message"/>
  </div>)
}

export class InvalidateOpiskeluoikeusButton extends React.Component {
  render() {
    let { opiskeluoikeus } = this.props
    let deleteRequested = this.state && this.state.deleteRequested

    return suorituksiaTehty(opiskeluoikeus)
      ? null
      : deleteRequested
        ? (<div className="invalidate">
          <a onClick={() => this.setState({deleteRequested: false})}><Text name="Peruuta mitätöinti" /></a>
          <button className="confirm-invalidate" onClick={() => invalidateOpiskeluoikeus(modelData(opiskeluoikeus, 'oid'))}><Text name="Vahvista mitätöinti, operaatiota ei voi peruuttaa" /></button>
        </div>)
        : <a className="invalidate" onClick={() => this.setState({deleteRequested: true})}><Text name="Mitätöi opiskeluoikeus" /></a>
  }
}

const suorituksiaTehty = opiskeluoikeus => {
  let suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  let osasuoritukset = suoritukset.flatMap(s => modelItems(s, 'osasuoritukset'))
  return osasuoritukset.find(suoritusValmis) !== undefined
}
