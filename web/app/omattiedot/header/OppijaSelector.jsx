import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelItems} from '../../editor/EditorModel'
import DropDown from '../../components/Dropdown'
import Text from '../../i18n/Text'

export const OppijaSelector = ({oppija, onOppijaChanged}) => (
  modelData(oppija, 'hasHuollettavia')
    ? <OppijaDropdown henkilö={modelData(oppija, 'henkilö')} huollettavat={modelItems(oppija, 'huollettavat').map(h => modelData(h))} onOppijaChanged={onOppijaChanged} />
    : null
)

const OppijaDropdown = ({henkilö, huollettavat, onOppijaChanged}) => {
  const oppijaAtom = Atom(henkilö)
  oppijaAtom.skip(1).onValue(onOppijaChanged)
  return (<div className='oppija-selector'>
    <Text className='oppija-selector__heading' name='Kenen opintoja haluat tarkastella' />
    <DropDown
      options={[henkilö, ...huollettavat]}
      selected={oppijaAtom}
      keyValue={o => o.oid}
      displayValue={o => `${o.etunimet} ${o.sukunimi}`}
      onSelectionChanged={o => oppijaAtom.set(o)}
    />
  </div>)
}
