import React from 'baret'
import {addContext, modelData} from '../editor/EditorModel.js'
import {currentLocation} from '../util/location.js'
import {näytettävätPäätasonSuoritukset, OpiskeluoikeusEditor} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems, modelTitle} from '../editor/EditorModel'
import {yearFromIsoDateString} from '../date/date'
import Link from '../components/Link'


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
    <div className="oppilaitos-nav-otsikkotiedot">
      <div>
        <h3>{modelTitle(oppilaitos, 'oppilaitos')}</h3>
        <ul className="opiskeluoikeudet">
          {modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) => {
            const hasAlkamispäivä = !!modelData(opiskeluoikeus, 'alkamispäivä')
            return (
              <li className="opiskeluoikeus" key={opiskeluoikeusIndex}>
                <span className="koulutus inline-text">{(näytettävätPäätasonSuoritukset(opiskeluoikeus)[0] || {}).title}</span>
                {hasAlkamispäivä && (
                  <span className="inline-text">{'('}
                    <span className="alku pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'alkamispäivä'))}</span>{'—'}
                    <span className="loppu pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'päättymispäivä'))}{', '}</span>
                    <span className="tila">{modelTitle(opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}</span>{')'}
                  </span>
                )}
              </li>
            )
          })}
        </ul>
      </div>
      <div>
        {selected
          ? <Link href="?" className="open"/>
          : <Link href={'?oppilaitos=' + modelData(oppilaitos, 'oppilaitos.oid')}/>}
      </div>
    </div>
  )
}
