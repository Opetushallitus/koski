import React from 'baret'
import {tutkinnonOsanRyhmät} from '../koodisto/koodistot'
import {t} from '../i18n/i18n'
import {modelData, modelLookup, modelProperties, modelProperty, optionalPrototypeModel} from '../editor/EditorModel'
import * as R from 'ramda'
import {createTutkinnonOsanSuoritusPrototype, placeholderForNonGrouped} from '../ammatillinen/TutkinnonOsa'
import {hasArvosana, suorituksenTyyppi} from './Suoritus'
import Text from '../i18n/Text'
import {ArvosanaEditor} from './ArvosanaEditor'
import {Editor} from '../editor/Editor'
import {shouldShowProperty} from '../editor/PropertiesEditor'


export const isAmmatillinentutkinto = suoritus => suoritus.value.classes.includes('ammatillisentutkinnonsuoritus')
export const isMuunAmmatillisenKoulutuksenSuoritus = suoritus => suoritus.value.classes.includes('muunammatillisenkoulutuksensuoritus')
export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = suoritus => suoritus.value.classes.includes('tutkinnonosaapienemmistakokonaisuuksistakoostuvasuoritus')
export const isNäyttötutkintoonValmistava = suoritus => suoritus.value.classes.includes('nayttotutkintoonvalmistavankoulutuksensuoritus')
export const isYlioppilastutkinto = suoritus => suoritus.value.classes.includes('ylioppilastutkinnonsuoritus')

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
      grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || placeholderForNonGrouped)(suoritukset)
      groupTitles = R.merge(ammatillisentutkinnonosanryhmaKoodisto, { [placeholderForNonGrouped] : t('Muut suoritukset')})
      groupIds = R.keys(grouped).sort()
      if (context.edit) {
        // Show the empty groups too
        groupIds = R.uniq(R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds))
      }
    } else {
      // Osasuorituksia voi olla monta tasoa (osasuorituksen osasuorituksia), jolloin on suoraviivaisempaa
      // tarkistaa ylimmän tason suorituksesta, onko kyseessä muun ammatillisen koulutksen tai tutkinnon
      // osaa pienemmistä kokonaisuuksista koostuva suoritus.
      const topLevelSuoritus = suoritusProto.context.suoritus
      if (isMuunAmmatillisenKoulutuksenSuoritus(topLevelSuoritus) || isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(topLevelSuoritus)) {
        grouped = { [placeholderForNonGrouped] : suoritukset }
        groupTitles = { [placeholderForNonGrouped] : t('Osasuoritus') }
        groupIds = [placeholderForNonGrouped]
      } else {
        grouped = { [placeholderForNonGrouped] : suoritukset }
        groupTitles = { [placeholderForNonGrouped] : t(modelProperty(suoritukset[0] || suoritusProto, 'koulutusmoduuli').title) }
        groupIds = [placeholderForNonGrouped]
      }
    }

    return {
      grouped: grouped,
      groupTitles: groupTitles,
      groupIds: groupIds
    }
  })
}

export const suoritusProperties = suoritus => {
  const filterProperties = filter => modelProperties(suoritus, filter)
  const includeProperties = (...properties) => filterProperties(p => properties.includes(p.key))
  const excludeProperties = (...properties) => filterProperties(p => !properties.includes(p.key))

  const propertiesForSuoritustyyppi = (tyyppi, isEdit) => {
    const simplifiedArviointi = modelProperties(modelLookup(suoritus, 'arviointi.-1'),
      p => !(['arvosana', 'päivä', 'arvioitsijat', 'pisteet']).includes(p.key)
    )

    const arviointipäivä = modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => p.key === 'päivä')
    const showPakollinen = (tyyppi !== 'nayttotutkintoonvalmistavakoulutus') && modelData(suoritus, 'koulutusmoduuli.pakollinen') !== undefined
    const pakollinen = showPakollinen ? modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'pakollinen') : []

    const defaultsForEdit = pakollinen
      .concat(arviointipäivä)
      .concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot'))

    const defaultsForView = pakollinen
      .concat(excludeProperties('koulutusmoduuli', 'arviointi', 'tutkinnonOsanRyhmä', 'tutkintokerta'))
      .concat(simplifiedArviointi)

    switch (tyyppi) {
      case 'valma':
      case 'telma':
        return isEdit
          ? pakollinen.concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot')).concat(arviointipäivä).concat(simplifiedArviointi)
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

/*
 *
 * Shared column types
 *
 */

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
  shouldShow: ({parentSuoritus, suoritukset, suorituksetModel, context}) => (!isNäyttötutkintoonValmistava(parentSuoritus) && (context.edit
    ? modelProperty(createTutkinnonOsanSuoritusPrototype(suorituksetModel), 'koulutusmoduuli.laajuus') !== null
    : suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined)),
  renderHeader: ({laajuusYksikkö}) => {
    return <th key='laajuus' className='laajuus' scope='col'><Text name='Laajuus'/>{((laajuusYksikkö && ' (' + laajuusYksikkö + ')') || '')}</th>
  },
  renderData: ({model, showScope}) => <td key='laajuus' className='laajuus'><Editor model={model} path='koulutusmoduuli.laajuus' compact='true' showReadonlyScope={showScope}/></td>
}

export const ArvosanaColumn = {
  shouldShow: ({parentSuoritus, suoritukset, context}) => !isNäyttötutkintoonValmistava(parentSuoritus) && (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => <th key='arvosana' className='arvosana' scope='col'><Text name='Arvosana'/></th>,
  renderData: ({model, ylioppilastutkinto}) => <td key='arvosana' className={`arvosana ${ylioppilastutkinto ? 'ylioppilas' : ''}`}><ArvosanaEditor model={model} /></td>
}
