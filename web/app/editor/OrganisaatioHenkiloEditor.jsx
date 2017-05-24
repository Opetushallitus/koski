import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as L from 'partial.lenses'
import {Editor} from './Editor.jsx'
import Dropdown from '../Dropdown.jsx'
import {pushModelValue, modelData, modelSetValue} from './EditorModel'
import {getOrganizationalPreferences} from '../organizationalPreferences'
import {t} from '../i18n'
export const OrganisaatioHenkilöEditor = ({model}) => {
  let query = Atom('')

  let myöntäjäOrganisaatio = model.context.myöntäjäOrganisaatio
  let organisaatioOid = modelData(myöntäjäOrganisaatio).oid

  let nimi = h => modelData(h, 'nimi')
  let nimiJaTitteli = h => nimi(h) && (modelData(h, 'nimi') + ', ' + t(modelData(h, 'titteli')))
  let queryFilter = q => o => nimi(o).toLowerCase().indexOf(q.toLowerCase()) >= 0
  var newItemLens = L.compose('value', 'newItem')
  let newItem = modelSetValue(L.set(newItemLens, true, model), myöntäjäOrganisaatio.value, 'organisaatio')
  let isNewItem = (o) => L.get(newItemLens, o)
  let kaikkiMyöntäjätP = getOrganizationalPreferences(organisaatioOid, 'myöntäjät')
    .map(myöntäjät => myöntäjät.map(m => modelSetValue(model, m.value)))

  let myöntäjätP = Bacon.combineWith(kaikkiMyöntäjätP, query, (xs, q) => !q ? xs : xs.filter(queryFilter(q)))
    .startWith([])

  return (<span className="organisaatiohenkilo">
    <Dropdown
      displayValue={o => isNewItem(o) ? 'Lisää henkilö' : nimiJaTitteli(o)}
      keyValue={o => isNewItem(o) ? '_new' : nimi(o)}
      options={myöntäjätP}
      enableFilter={true}
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