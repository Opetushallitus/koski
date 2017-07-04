import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import Http from '../http'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import Text from '../Text.jsx'
import Dropdown from '../Dropdown.jsx'
import {splitName, capitalizeName} from '../util.js'

export default ({ hetu, oid, henkilöAtom, henkilöValidAtom }) => {
  const etunimetAtom = henkilöAtom.view('etunimet')
  const kutsumanimiAtom = henkilöAtom.view('kutsumanimi')
  const sukunimiAtom = henkilöAtom.view('sukunimi')
  const kutsumanimiChoices = Atom([])

  const henkilöValidP = etunimetAtom.and(sukunimiAtom).and(kutsumanimiAtom)
  henkilöValidP.changes().onValue((valid) => henkilöValidAtom.set(valid))

  const existingHenkilöP = hetu ? Http.cachedGet('/koski/api/henkilo/hetu/' + hetu).map('.0') : Http.cachedGet('/koski/api/henkilo/oid/' + oid).map('.0')
  existingHenkilöP.filter(R.identity).onValue((henkilö) => henkilöAtom.set(henkilö))

  const kutsumanimiChoicesP = etunimetAtom.skipErrors().skipDuplicates().map(splitName)
  kutsumanimiChoicesP.changes().onValue(x => kutsumanimiChoices.set(x))

  return (
    <div className='henkilo'>
      {
        elementWithLoadingIndicator(existingHenkilöP.map(henkilö => {
            let existing = !!(henkilö && henkilö.oid)
            return (<div>
              <label className='hetu'>
                <Text name="Henkilötunnus"/>
                <span className='value'>{hetu}</span>
              </label>
              <label className='etunimet'>
                <Text name="Etunimet"/>
                <NameInputOrValue existing={existing} atom={etunimetAtom}/>
              </label>
              <label className='kutsumanimi'>
                <Text name="Kutsumanimi"/>
                <ValueSelect existing={existing} atom={kutsumanimiAtom} items={kutsumanimiChoices}/>
              </label>
              <label className='sukunimi'>
                <Text name="Sukunimi"/>
                <NameInputOrValue existing={existing} atom={sukunimiAtom}/>
              </label>
            </div>)
          }), <Text name="Ladataan..."/>
        )
      }
    </div>
  )
}

const NameInputOrValue = ({ existing, atom }) => existing
  ? <input type="text" disabled value={ atom.or('') }></input>
  : <input type="text" value={ atom.or('') } onChange={ e => atom.set(e.target.value)} onBlur={ e => atom.set(capitalizeName(e.target.value))}></input>

const ValueSelect = ({ existing, atom, items}) => existing
  ? <input type="text" disabled value={ atom.or('') }></input>
  : <Dropdown options={items} keyValue={R.identity} displayValue={R.identity} selected={atom} onSelectionChanged={value => atom.set(value)}/>
