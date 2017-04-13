import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import {Editor} from './Editor.jsx'
import Dropdown from '../Dropdown.jsx'
import {pushModelValue, modelData, modelSetData, modelSetValue} from './EditorModel'
import Http from '../http'

export const OrganisaatioHenkilöEditor = ({model}) => {
  let query = Atom('')

  let myöntäjäOrganisaatio = model.context.myöntäjäOrganisaatio
  let organisaatioOid = modelData(myöntäjäOrganisaatio).oid

  let nimi = h => modelData(h, 'nimi')
  let nimiJaTitteli = h => nimi(h) && (modelData(h, 'nimi') + ', ' + modelData(h, 'titteli.fi'))
  let queryFilter = q => o => nimi(o).toLowerCase().indexOf(q.toLowerCase()) >= 0
  let setData = (data, isNew) => L.set(newItemLens, isNew, modelSetData(modelSetData(model, data.titteli, 'titteli.fi'), data.nimi, 'nimi'))
  var newItemLens = L.compose('value', 'newItem')
  let newItem = modelSetValue(setData({}, true), myöntäjäOrganisaatio.value, 'organisaatio')
  let isNewItem = (o) => L.get(newItemLens, o)
  let kaikkiMyöntäjätP = Http.cachedGet(`/koski/api/preferences/${organisaatioOid}/myöntäjät`)
    .map(myöntäjät => myöntäjät.map(d => setData({ nimi: d.nimi, titteli: d.titteli.fi}, false)))
  let myöntäjätP = Bacon.combineWith(kaikkiMyöntäjätP, query, (xs, q) => !q ? xs : xs.filter(queryFilter(q)))
    .startWith([])

  return (<span className="organisaatiohenkilo">
    <Dropdown baret-lift
      displayValue={o => isNewItem(o) ? 'Lisää henkilö' : nimiJaTitteli(o)}
      options={myöntäjätP}
      onFilter={q => query.set(q)}
      selected={model}
      onSelectionChanged={h => pushModelValue(model, h.value)}
      newItem={newItem}
    />
    {
      isNewItem(model) && (<span className="uusi-henkilo">
        <span className="nimi"><Editor model={model} path="nimi" placeholder="nimi"/></span>
        <span className="titteli"><Editor model={model} path="titteli" placeholder="titteli"/></span></span>
      )
    }
  </span>)
}