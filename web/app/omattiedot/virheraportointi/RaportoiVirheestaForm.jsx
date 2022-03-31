import React from 'baret'
import * as R from 'ramda'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../../util/http'
import {PuuttuvatTiedot} from './PuuttuvatTiedot'
import Text from '../../i18n/Text'
import {t} from '../../i18n/i18n'
import {modelData, modelItems, modelTitle} from '../../editor/EditorModel'
import {flatMapArray, ift} from '../../util/util'
import {Yhteystiedot} from './Yhteystiedot'
import OrganisaatioPicker from '../../virkailija/OrganisaatioPicker'
import {MuuOppilaitosOptions, OppilaitosOption, OtherOppilaitosValue} from './RadioOption'
import {trackEvent} from '../../tracking/piwikTracking'
import Checkbox from '../../components/Checkbox'

const resolveResponsibleOrganization = opiskeluoikeus =>
  modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'ylioppilastutkinto'
    ? R.assoc('suoritus', modelTitle(opiskeluoikeus, 'suoritukset.0.tyyppi'), modelData(opiskeluoikeus, 'koulutustoimija'))
    : modelData(opiskeluoikeus, 'oppilaitos')

const OppilaitosPicker = ({oppilaitosAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE']
  const pickerSelection = Atom({})

  return (
    <div className='oppilaitos-picker form-section' data-indent={1}>
      {
        Bacon.combineWith(oppilaitosAtom, pickerSelection,
          (oid, selected) => (
            <OrganisaatioPicker
              preselectSingleOption={true}
              selectedOrg={{ oid: selected && selected.oid, nimi: selected && selected.nimi && t(selected.nimi) }}
              onSelectionChanged={org => {
                oppilaitosAtom.set(org ? org.oid : OtherOppilaitosValue)
                pickerSelection.set(org)
              }}
              shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
              canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
              clearText={t('tyhjennä')}
              noSelectionText={t('Valitse oppilaitos')}
              showAll={true}
            />
          )
        )
      }
    </div>
  )
}

OppilaitosPicker.displayName = 'OppilaitosPicker'

export const RaportoiVirheestäForm = ({henkilö, opiskeluoikeudet}) => {
  const hasAcceptedDisclaimer = Atom(false)
  const selectedOppilaitosA = Atom()
  const isLoadingA = Atom(false)

  const oppilaitokset = R.uniqBy(oppilaitos => oppilaitos.oid,
    flatMapArray(opiskeluoikeudet, o => modelItems(o, 'opiskeluoikeudet').map(resolveResponsibleOrganization))
  )

  const yhteystietoP = selectedOppilaitosA
    .map(oid => oid === OtherOppilaitosValue ? null : oid)
    .doAction(oid => !!oid && isLoadingA.set(true))
    .flatMapLatest(oid => oid
      ? Http.cachedGet(
        `/koski/api/organisaatio/sahkoposti-virheiden-raportointiin?organisaatio=${oid}`, {
          errorMapper: e => e.httpStatus === 404 ? ({email: null}) : new Bacon.Error(e)
        }
      )
      : Bacon.once(null)
    )
    .toProperty()

  yhteystietoP.onValue(() => isLoadingA.set(false))

  yhteystietoP.filter(R.identity).skipDuplicates(R.equals).onValue(v => {
    trackEvent('virheraportointi', (v.organisaationNimi && v.organisaationNimi.fi) || '-')
  })

  const isOtherOptionSelectedA = selectedOppilaitosA.map(
    selectedOption => selectedOption ? !oppilaitokset.map(o => o.oid).includes(selectedOption) : false
  )

  return (
    <div className='raportoi-virheestä-form textstyle-body'>
      <div className='puuttuvat-tiedot form-section' data-indent={0}>
        <PuuttuvatTiedot/>
        <Checkbox
          key={`${modelData(henkilö, 'oid')}-virheet-checkbox`}
          id='puuttuvat-tiedot-checkbox'
          label='Asiani koskee tietoa, joka näkyy, tai kuuluisi yllämainitun perusteella näkyä Koski-palvelussa.'
          onChange={event => hasAcceptedDisclaimer.set(event.target.checked)}
        />
      </div>

      {ift(hasAcceptedDisclaimer, (
        <div className='oppilaitos-options form-section' data-indent={1}>
          <p><b><Text name='Voit tiedustella asiaa oppilaitokseltasi.'/></b></p>
          <ul>
            {oppilaitokset.map(o => (
              <OppilaitosOption
                oppilaitos={o}
                selectedOppilaitosA={selectedOppilaitosA}
                key={o.oid}
              />
            ))}

            <MuuOppilaitosOptions
              selectedOppilaitosA={selectedOppilaitosA}
              isSelected={isOtherOptionSelectedA}
            />
          </ul>

          {ift(isOtherOptionSelectedA, <OppilaitosPicker oppilaitosAtom={selectedOppilaitosA}/>)}

          <Yhteystiedot henkilö={henkilö} yhteystietoP={yhteystietoP} isLoadingA={isLoadingA}/>
        </div>
      ))}
    </div>
  )
}

RaportoiVirheestäForm.displayName = 'RaportoiVirheestäForm'
