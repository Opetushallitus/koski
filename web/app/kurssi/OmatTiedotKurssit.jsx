import React from 'baret'
import {modelData, modelItems, modelLookup, modelTitle} from '../editor/EditorModel'
import {t} from '../i18n/i18n'
import {isIBKurssi} from './kurssi'
import {isIBKurssinArviointi} from './KurssiPopup'
import {
  isLukionKurssi,
  isLukioonValmistavanKoulutuksenKurssi,
  isPaikallinen,
  isPreIBKurssi
} from '../suoritus/Koulutusmoduuli'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import IBKurssinArviointiEditor from '../ib/IBKurssinArviointiEditor'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {FootnoteHint} from '../components/footnote'
import Text from '../i18n/Text'

export const KurssitListMobile = ({oppiaine, oppiaineenKeskiarvo}) => {
  const osasuoritukset = modelLookup(oppiaine, 'osasuoritukset')
  if (!osasuoritukset) return null
  const kurssit = modelItems(osasuoritukset)

  return (
    <td colSpan='2'>
      <table className='kurssilista-mobile'>
        <thead>
        <tr>
          <th className='nimi'><Text name='Kurssi'/></th>
          <th className='arvosana'><Text name='Arvosana' /></th>
          <th className='lisatiedot'><Text name='Lisätiedot' /></th>
        </tr>
        </thead>
        <tbody>
        {kurssit.map((kurssi, index) =>
          <MobileKurssi kurssi={kurssi} even={(index + 1) % 2 === 0} key={index}/>
        )}
        {oppiaineenKeskiarvo && <tr>
          <td aria-hidden={true}/>
          <td className='arvosana keskiarvo'>
            {`(${t('Keskiarvo')} ${oppiaineenKeskiarvo})`}
          </td>
        </tr>}
        </tbody>
      </table>
    </td>
  )
}

class MobileKurssi extends React.Component {
  constructor(props) {
    super(props)
    this.state = { expanded: false }

    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand() {
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {kurssi, even} = this.props
    const {expanded} = this.state
    const koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
    const koulutusmoduuliModel = modelLookup(kurssi, 'koulutusmoduuli')
    const title = kurssi.value.classes.includes('diasuoritus') ? modelTitle(kurssi, 'koulutusmoduuli') : koulutusmoduuli.tunniste.koodiarvo

    return [
      <tr key='kurssi-row' className={`kurssi ${even ? 'even' : ''}`}>
        <td className='nimi'>{title} {hasFootnoteHint(koulutusmoduuliModel) && <FootnoteHint title={'Paikallinen kurssi'}/>}</td>
        <td className='arvosana'><ArvosanaEditor model={kurssi}/></td>
        <td className='lisatiedot'>
          <button className='inline-link-button' onClick={this.toggleExpand} aria-pressed={expanded}>
            <Text name={expanded ? 'Sulje' : 'Avaa'}/>
          </button>
        </td>
      </tr>,
      expanded && <tr key='kurssi-details'>
        <td colSpan='3'>
          <PropertiesEditor
            model={kurssi}
            propertyFilter={p => !['arviointi', 'koodistoUri'].includes(p.key) || isIBKurssinArviointi(kurssi)(p)}
            propertyEditable={p => !['tunniste', 'koodiarvo', 'nimi'].includes(p.key)}
            className='kansalainen'
            getValueEditor={(prop, getDefault) => isIBKurssi(kurssi) && prop.key === 'arviointi'
              ? <IBKurssinArviointiEditor model={kurssi}/>
              : getDefault()
            }
          />
        </td>
      </tr>
    ]
  }
}

const hasFootnoteHint = koulutusmoduuliModel => {
  return (
      isLukionKurssi(koulutusmoduuliModel) ||
      isPreIBKurssi(koulutusmoduuliModel) ||
      isLukioonValmistavanKoulutuksenKurssi(koulutusmoduuliModel)
    ) &&
    isPaikallinen(koulutusmoduuliModel)
}
