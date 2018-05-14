import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import Http from '../util/http'
import {elementWithLoadingIndicator} from '../components/AjaxLoadingIndicator'
import Text from '../i18n/Text'
import Dropdown from '../components/Dropdown'

export default ({ hetu, oid, henkilöAtom, henkilöValidAtom }) => {
  const etunimetAtom = henkilöAtom.view('etunimet')
  const kutsumanimiAtom = henkilöAtom.view('kutsumanimi')
  const sukunimiAtom = henkilöAtom.view('sukunimi')
  const kutsumanimiChoices = Atom([])
  const kutsumanimiManuallySetAtom = Atom(false)

  const henkilöValidP = etunimetAtom.and(sukunimiAtom).and(kutsumanimiAtom)
  henkilöValidP.changes().onValue((valid) => henkilöValidAtom.set(valid))

  const existingHenkilöP = hetu ? Http.post('/koski/api/henkilo/hetu', {hetu}).map('.0') : Http.cachedGet('/koski/api/henkilo/oid/' + oid).map('.0')
  existingHenkilöP.filter(R.identity).onValue((henkilö) => henkilöAtom.set(henkilö))

  const kutsumanimiChoicesP = etunimetAtom.skipErrors().skipDuplicates().map(sanitizeFirstnames).map(splitName)
  kutsumanimiChoicesP.changes().onValue(x => kutsumanimiChoices.set(x))

  Bacon.combineAsArray(etunimetAtom, kutsumanimiAtom, kutsumanimiManuallySetAtom).changes().onValue(v => {
    let nameParts = splitName(sanitizeFirstnames(v[0]))
    let defaultName = nameParts[0]
    if (!v[2] || (v[1] && !nameParts.includes(sanitizeFirstnames(v[1])))) {
      kutsumanimiAtom.set(defaultName)
      kutsumanimiManuallySetAtom.set(false)
    }
  })

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
                <ValueSelect existing={existing} atom={kutsumanimiAtom} items={kutsumanimiChoices} manuallySetAtom={kutsumanimiManuallySetAtom}/>
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
  : <input type="text" value={ atom.or('') } onChange={ e => atom.set(e.target.value)} onBlur={ e => atom.set(sanitizeFirstnames(e.target.value))}></input>

const ValueSelect = ({ existing, atom, items, manuallySetAtom}) => existing
  ? <input type="text" disabled value={ atom.or('') }></input>
  : <Dropdown options={items} keyValue={R.identity} displayValue={R.identity} selected={atom} onSelectionChanged={value => {manuallySetAtom && manuallySetAtom.set(true); atom.set(value)}}/>

const splitName = (name) => {
  let n = name.trim().split().join('')
  return R.filter(R.identity, R.uniq(n.split(/\s/g).concat(n.split(/[\s-]/g))))
}

const sanitizeFirstnames = (name) => {
  return name.trim().replace(/\s+/g, ' ').replace(/\s*-\s*/g, '-')
}
