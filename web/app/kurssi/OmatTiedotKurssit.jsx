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
import {eiLasketaKokonaispistemäärään} from '../dia/DIA'

export const KurssitListMobile = ({oppiaine, oppiaineenKeskiarvo, customTitle, customKurssitSortFn}) => {
  const osasuoritukset = modelLookup(oppiaine, 'osasuoritukset')
  if (!osasuoritukset) return null
  let kurssit = modelItems(osasuoritukset)
  if (typeof customKurssitSortFn === 'function') {
    kurssit = kurssit.sort(customKurssitSortFn)
  }

  return (
    <td colSpan='2'>
      <table className='kurssilista-mobile'>
        <thead>
        <tr>
          <th className='nimi'><Text name={customTitle || 'Kurssi'}/></th>
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

KurssitListMobile.displayName = 'KurssitListMobile'

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
    const diaSuoritus = kurssi.value.classes.includes('diasuoritus')
    const title = diaSuoritus ? modelTitle(kurssi, 'koulutusmoduuli') : koulutusmoduuli.tunniste.koodiarvo

    const footnoteHint = hasFootnoteHint(koulutusmoduuliModel)
      ? <FootnoteHint title={'Paikallinen kurssi'}/>
      : diaSuoritus && eiLasketaKokonaispistemäärään(kurssi)
        ? <FootnoteHint title={'Ei lasketa kokonaispistemäärään'}/>
        : null

    return [
      <tr key='kurssi-row' className={`kurssi ${even ? 'even' : ''}`}>
        <td className='nimi'>{title} {footnoteHint}</td>
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
