import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelItems} from '../../editor/EditorModel'
import DropDown from '../../components/Dropdown'
import Text from '../../i18n/Text'
import * as R from 'ramda'

export const OppijaSelector = ({oppija, onOppijaChanged}) =>
  modelData(oppija, 'hasHuollettavia') ? <OppijaDropdown oppija={oppija} onOppijaChanged={onOppijaChanged} /> : null

const OppijaDropdown = ({oppija, onOppijaChanged}) => {
  const huoltaja = modelData(oppija, 'userHenkilö')
  const oppijaAtom = Atom(huoltaja)
  oppijaAtom.skip(1).onValue(onOppijaChanged)
  return (<div className='oppija-selector'>
    <Text className='oppija-selector__heading' name='Kenen opintoja haluat tarkastella' />
    <DropDown
      options={modelItems(oppija, 'kaikkiHenkilöt').map(x => modelData(x))}
      selected={oppijaAtom}
      keyValue={o => o.oid}
      displayValue={o => `${o.etunimet} ${o.sukunimi}`}
      onSelectionChanged={o => oppijaAtom.set(R.assoc('isHuollettava', o.oid !== huoltaja.oid, o))}
    />
  </div>)
}
