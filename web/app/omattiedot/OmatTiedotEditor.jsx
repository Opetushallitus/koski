import React from 'baret'
import {addContext, modelData} from '../editor/EditorModel.js'
import {currentLocation} from '../util/location.js'
import {näytettävätPäätasonSuoritukset, OpiskeluoikeusEditor} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems, modelTitle} from '../editor/EditorModel'
import Link from '../components/Link'
import {OpiskeluoikeudenTila} from './fragments/OpiskeluoikeudenTila'


export const OmatTiedotEditor = ({model}) => {
  let oppijaOid = modelData(model, 'henkilö.oid')
  let selectedOppilaitos = currentLocation().params.oppilaitos
  let oppilaitokset = modelItems(model, 'opiskeluoikeudet')
  return (
    <div className="oppilaitokset-nav">
      {oppilaitokset.map((oppilaitos, oppilaitosIndex) => (
        <OppilaitoksenOpiskeluoikeudet
          key={oppilaitosIndex}
          oppijaOid={oppijaOid}
          oppilaitos={oppilaitos}
          selected={selectedOppilaitos === modelData(oppilaitos, 'oppilaitos').oid}
        />))}
    </div>)
}

const OppilaitoksenOpiskeluoikeudet = ({oppijaOid, oppilaitos, selected}) => (
  <div className="oppilaitos-nav">
    <OppilaitosOtsikkotiedot oppilaitos={oppilaitos} selected={selected}/>
    {selected &&
      <ul className="opiskeluoikeuksientiedot">
        {modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) => (
          <li key={opiskeluoikeusIndex}>
            <OpiskeluoikeusEditor model={ addContext(opiskeluoikeus, { oppijaOid: oppijaOid, opiskeluoikeusIndex }) }/>
          </li>)
        )}
      </ul>}
  </div>
)

const OppilaitosOtsikkotiedot = ({oppilaitos, selected}) => {
  // FIXME Refaktoroi tätä?
  return (
    <Link className="oppilaitos-nav-otsikkotiedot" href={selected ? '?' : '?oppilaitos=' + modelData(oppilaitos, 'oppilaitos.oid')}>
      <div>
        <h3>{modelTitle(oppilaitos, 'oppilaitos')}</h3>
        <ul className="opiskeluoikeudet">
          {modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) => {
            const hasAlkamispäivä = !!modelData(opiskeluoikeus, 'alkamispäivä')
            return (
              <li className="opiskeluoikeus" key={opiskeluoikeusIndex}>
                <span className="koulutus">{(näytettävätPäätasonSuoritukset(opiskeluoikeus)[0] || {}).title}</span>
                {hasAlkamispäivä && <OpiskeluoikeudenTila opiskeluoikeus={opiskeluoikeus}/>}
              </li>
            )
          })}
        </ul>
      </div>
      <div>
        <span className={selected ? 'open' : ''}/>
      </div>
    </Link>
  )
}
