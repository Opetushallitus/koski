import React from 'baret'
import {tutkinnonOsanRyhmät} from '../koodisto/koodistot'
import {t} from '../i18n/i18n'
import {modelData, modelLookup, modelProperties, modelProperty, modelTitle, optionalPrototypeModel} from '../editor/EditorModel'
import * as R from 'ramda'
import {createTutkinnonOsanSuoritusPrototype, NON_GROUPED} from '../ammatillinen/TutkinnonOsa'
import {hasArvosana, suorituksenTyyppi, tilaText} from './Suoritus'
import Text from '../i18n/Text'
import {ArvosanaEditor} from './ArvosanaEditor'
import {Editor} from '../editor/Editor'
import {shouldShowProperty} from '../editor/PropertiesEditor'
import {osanOsa} from '../ammatillinen/TutkinnonOsa'
import {sortLanguages} from '../util/sorting'
import {suorituksenTilaSymbol} from './Suoritustaulukko'
import {isKieliaine} from './Koulutusmoduuli'

export const isAmmatillinentutkinto = suoritus => suoritus.value.classes.includes('ammatillisentutkinnonsuoritus')
export const isMuunAmmatillisenKoulutuksenSuoritus = suoritus => suoritus && suoritus.value.classes.includes('muunammatillisenkoulutuksensuoritus')
export const isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = suoritus => suoritus.value.classes.includes('muunammatillisenkoulutuksenosasuorituksensuoritus')
export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = suoritus => suoritus.value.classes.includes('tutkinnonosaapienemmistakokonaisuuksistakoostuvasuoritus')
export const isNäyttötutkintoonValmistava = suoritus => suoritus.value.classes.includes('nayttotutkintoonvalmistavankoulutuksensuoritus')
export const isYlioppilastutkinto = suoritus => suoritus.value.classes.includes('ylioppilastutkinnonsuoritus')
export const isVapaanSivistystyönOppivelvollistenSuoritus = suoritus => suoritus.value.classes.includes('oppivelvollisillesuunnattuvapaansivistystyonkoulutuksensuoritus')
export const isMaahanmuuttajienKotoutumiskoulutuksenSuoritus = suoritus => suoritus.value.classes.includes('oppivelvollisillesuunnattumaahanmuuttajienkotoutumiskoulutuksensuoritus')
export const isLukutaitokoulutuksenSuoritus = suoritus => suoritus.value.classes.includes('vapaansivistystyonlukutaitokoulutuksensuoritus')

export const getLaajuusYksikkö = (suoritus) => {
  const laajuusModel = modelLookup(suoritus, 'koulutusmoduuli.laajuus')
  return laajuusModel && laajuusModel.optional && !modelData(laajuusModel)
    ? t(modelData(optionalPrototypeModel(laajuusModel), 'yksikkö.lyhytNimi'))
    : t(modelData(laajuusModel, 'yksikkö.lyhytNimi'))
}

// Can be called without suoritusProto if suoritukset has always at least one member
export const groupSuoritukset = (parentSuoritus, suoritukset, context, suoritusProto) => {
  const diaarinumero = modelData(parentSuoritus, 'koulutusmoduuli.perusteenDiaarinumero')
  const suoritustapa = modelData(parentSuoritus, 'suoritustapa')

  return tutkinnonOsanRyhmät(diaarinumero, suoritustapa).map(ammatillisentutkinnonosanryhmaKoodisto => {
    let grouped, groupIds, groupTitles
    if (isAmmatillinentutkinto(parentSuoritus) && R.keys(ammatillisentutkinnonosanryhmaKoodisto).length > 1) {
      grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || NON_GROUPED)(suoritukset)
      groupTitles = R.mergeRight(ammatillisentutkinnonosanryhmaKoodisto, { [NON_GROUPED] : t('Muut suoritukset')})
      groupIds = R.keys(grouped).sort()
      if (context.edit) {
        // Show the empty groups too
        groupIds = R.uniq(R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds))
      }
    } else {
      // Osasuorituksia voi olla monta tasoa (osasuorituksen osasuorituksia), jolloin on suoraviivaisempaa
      // tarkistaa ylimmän tason suorituksesta, onko kyseessä muun ammatillisen koulutksen tai tutkinnon
      // osaa pienemmistä kokonaisuuksista koostuva suoritus.
      const topLevelSuoritus = R.path(['context', 'suoritus'], suoritusProto)
      if (topLevelSuoritus && (isMuunAmmatillisenKoulutuksenSuoritus(topLevelSuoritus) || isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(topLevelSuoritus))) {
        grouped = { [NON_GROUPED] : suoritukset }
        groupTitles = { [NON_GROUPED] : t('Osasuoritus') }
        groupIds = [NON_GROUPED]
      } else {
        grouped = { [NON_GROUPED] : suoritukset }
        groupTitles = { [NON_GROUPED] : t(modelProperty(suoritukset[0] || suoritusProto, 'koulutusmoduuli').title) }
        groupIds = [NON_GROUPED]
      }
    }

    return {
      grouped: grouped,
      groupTitles: groupTitles,
      groupIds: groupIds
    }
  })
}

export const suoritusProperties = (suoritus, complexArviointi) => {
  const filterProperties = filter => modelProperties(suoritus, filter)
  const includeProperties = (...properties) => filterProperties(p => properties.includes(p.key))
  const excludeProperties = (...properties) => filterProperties(p => !properties.includes(p.key))

  const propertiesForSuoritustyyppi = (tyyppi, isEdit) => {
    const simplifiedArviointi = modelProperties(modelLookup(suoritus, 'arviointi.-1'),
      p => !(['arvosana', 'arvioitsijat', 'pisteet', 'kuvaus']).includes(p.key)
    )

    const arvioinninKuvaus = modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => p.key === 'kuvaus')
    const arviointipäivä = modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => p.key === 'päivä')
    const showPakollinen = (tyyppi !== 'nayttotutkintoonvalmistavakoulutus') && modelData(suoritus, 'koulutusmoduuli.pakollinen') !== undefined
    const pakollinen = showPakollinen ? modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'pakollinen') : []
    const arviointi = modelProperties(suoritus).filter(p => p.key === 'arviointi')

    const taitotasot = modelProperties(modelLookup(suoritus, 'arviointi.-1'),
      p => isEdit && ['kuullunYmmärtämisenTaitotaso', 'puhumisenTaitotaso', 'luetunYmmärtämisenTaitotaso', 'kirjoittamisenTaitotaso'].includes(p.key))

    const defaultsForEdit = pakollinen
      .concat(complexArviointi ? arviointi : arviointipäivä)
      .concat(taitotasot)
      .concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot', 'liittyyTutkinnonOsaan'))

    const defaultsForView = pakollinen
      .concat(excludeProperties('koulutusmoduuli', 'arviointi', 'tutkinnonOsanRyhmä', 'tutkintokerta'))
      .concat(taitotasot)
      .concat(complexArviointi ? arviointi : simplifiedArviointi)

    switch (tyyppi) {
      case 'valma':
      case 'telma':
        return isEdit
          ? pakollinen.concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot')).concat(arviointipäivä).concat(arvioinninKuvaus).concat(simplifiedArviointi)
          : defaultsForView

      default: return isEdit ? defaultsForEdit : defaultsForView
    }
  }

  const kuvaus = modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'kuvaus')
  const parentSuorituksenTyyppi = suorituksenTyyppi(suoritus.context.suoritus)

  return kuvaus
    .concat(propertiesForSuoritustyyppi(parentSuorituksenTyyppi, suoritus.context.edit))
    .filter(shouldShowProperty(suoritus.context))
}

export const ExpandAllRows = ({allExpandedP, toggleExpandAll}) => (
  <thead>
  <tr>
    <th className="suoritus">
      <div>
        {allExpandedP.map(allExpanded => (
          <button className={'expand-all koski-button' + (allExpanded ? ' expanded' : '')} onClick={toggleExpandAll}>
            <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}/>
          </button>
        ))}
      </div>
    </th>
  </tr>
  </thead>
)

/*
 *
 * Shared column types
 *
 */

export const SuoritusColumn = {
  shouldShow : () => true,
  renderHeader: ({suoritusTitle}) => <td key="suoritus" className="suoritus"><Text name={suoritusTitle}/></td>,
  renderData: ({model, showTila, onExpand, hasProperties, expanded}) => {
    let koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')
    let titleAsExpandLink = hasProperties && (!osanOsa(koulutusmoduuli) || !model.context.edit)
    let kieliaine = isKieliaine(koulutusmoduuli)

    return (<td key="suoritus" className="suoritus">
      <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
         onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
      {showTila && <span className="tila" title={tilaText(model)}>{suorituksenTilaSymbol(model)}</span>}
      {
        titleAsExpandLink
          ? <button className='nimi inline-link-button' onClick={() => onExpand(!expanded)}>{modelTitle(model, 'koulutusmoduuli')}</button>
          : <span className="nimi">
            {t(modelData(koulutusmoduuli, 'tunniste.nimi')) + (kieliaine ? ', ' : '')}
            {kieliaine && <span className="value kieli"><Editor model={koulutusmoduuli} inline={true} path="kieli" sortBy={sortLanguages}/></span>}
          </span>
      }
    </td>)
  }
}

export const TutkintokertaColumn = {
  shouldShow: ({parentSuoritus}) => isYlioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return <th key='tutkintokerta' className='tutkintokerta' scope='col'><Text name='Tutkintokerta'/></th>
  },
  renderData: ({model}) => (<td key='tutkintokerta' className='tutkintokerta'>
    <Editor model={model} path='tutkintokerta.vuosi' compact='true'/>
    {' '}
    <Editor model={model} path='tutkintokerta.vuodenaika' compact='true'/>
  </td>)
}

export const KoepisteetColumn = {
  shouldShow: ({parentSuoritus}) => isYlioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return <th key='koepisteet' className='koepisteet' scope='col'><Text name='Pisteet'/></th>
  },
  renderData: ({model, ylioppilastutkinto}) => (<td key='koepisteet' className={`koepisteet ${ylioppilastutkinto ? 'ylioppilas' : ''}`}><Editor model={modelLookup(model, 'arviointi.-1.pisteet')}/></td>)
}

export const LaajuusColumn = {
  shouldShow: ({parentSuoritus, suoritukset, suorituksetModel, context}) => {
    if (isNäyttötutkintoonValmistava(parentSuoritus)) {
      return false
    } else if (isVapaanSivistystyönOppivelvollistenSuoritus(parentSuoritus) && context.edit) {
      return false
    } else {
      return context.edit
        ? modelProperty(createTutkinnonOsanSuoritusPrototype(suorituksetModel), 'koulutusmoduuli.laajuus') !== null
        : suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined
    }
  },
  renderHeader: ({laajuusYksikkö}) => {
    return <th key='laajuus' className='laajuus' scope='col'><Text name='Laajuus'/>{((laajuusYksikkö && ' (' + laajuusYksikkö + ')') || '')}</th>
  },
  renderData: ({model, showScope, disabled}) => <td key='laajuus' className='laajuus'><Editor disabled={disabled} model={model} path='koulutusmoduuli.laajuus' compact='true' showReadonlyScope={showScope}/></td>
}

export const ArvosanaColumn = {
  shouldShow: ({parentSuoritus, suoritukset, context}) => (
    !isNäyttötutkintoonValmistava(parentSuoritus)
    && !isVapaanSivistystyönOppivelvollistenSuoritus(parentSuoritus)
    && (context.edit || suoritukset.find(hasArvosana) !== undefined)
  ),
  renderHeader: () => <th key='arvosana' className='arvosana' scope='col'><Text name='Arvosana'/></th>,
  renderData: ({model, ylioppilastutkinto}) => <td key='arvosana' className={`arvosana ${ylioppilastutkinto ? 'ylioppilas' : ''}`}><ArvosanaEditor model={model} /></td>
}

export const TaitotasoColmn = {
  shouldShow: ({parentSuoritus}) => isLukutaitokoulutuksenSuoritus(parentSuoritus),
  renderHeader: () => <th key='taitotaso'><Text name={'Taitotaso'}/></th>,
  renderData: ({model}) => <td key='taitotaso' className={'taitotaso'}><Editor model={model} path={'arviointi.-1.taitotaso'}/></td>
}
