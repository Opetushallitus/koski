import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../../util/http'
import {PuuttuvatTiedot} from './PuuttuvatTiedot'
import Text from '../../i18n/Text'
import {t} from '../../i18n/i18n'
import {modelData} from '../../editor/EditorModel'
import {ift} from '../../util/util'
import {Yhteystiedot} from './Yhteystiedot'
import OrganisaatioPicker from '../../virkailija/OrganisaatioPicker'
import {MuuOppilaitosOptions, OppilaitosOption, OtherOppilaitosValue} from './RadioOption'

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
              selectedOrg={selected}
              onSelectionChanged={org => {
                oppilaitosAtom.set(org.oid)
                pickerSelection.set(org)
              }}
              shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
              canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
              clearText='tyhjennä'
              noSelectionText={t('Valitse oppilaitos')}
              showAll={true}
            />
          )
        )
      }
    </div>
  )
}

export const RaportoiVirheestäForm = ({henkilö, opiskeluoikeudet}) => {
  const hasAcceptedDisclaimer = Atom(false)
  const selectedOppilaitosA = Atom()

  const oppilaitokset = opiskeluoikeudet.map(o => modelData(o, 'oppilaitos'))

  const yhteystietoP = selectedOppilaitosA
    .map(oid => oid === OtherOppilaitosValue ? null : oid)
    .flatMapLatest(oid => oid
      ? Http.cachedGet(
        `/koski/api/organisaatio/sahkoposti-virheiden-raportointiin?organisaatio=${oid}`, {
          errorMapper: e => e.httpStatus === 404 ? ({email: null}) : new Bacon.Error(e)
        }
      )
      : Bacon.once(null)
    )
    .toProperty()

  const isOtherOptionSelectedA = selectedOppilaitosA.map(
    selectedOption => selectedOption ? !oppilaitokset.map(o => o.oid).includes(selectedOption) : false
  )

  const isLoadingP = Bacon.mergeAll(
    yhteystietoP.map(false),
    selectedOppilaitosA.filter(oid => oid !== OtherOppilaitosValue).map(true).changes()
  ).startWith(false).skipDuplicates()

  return (
    <div className='raportoi-virheestä-form'>
      <div className='puuttuvat-tiedot form-section' data-indent={0}>
        <PuuttuvatTiedot/>

        <div className='labeled-option'>
          <input
            type='checkbox'
            id='puuttuvat-tiedot__checkbox'
            onChange={event => hasAcceptedDisclaimer.set(event.target.checked)}
          />
          <label htmlFor='puuttuvat-tiedot__checkbox'>
            <Text name='Asiani koskee tietoa, joka näkyy, tai kuuluisi yllämainitun perusteella näkyä Koski-palvelussa.'/>
          </label>
        </div>
      </div>

      {ift(hasAcceptedDisclaimer, (
        <div className='oppilaitos-options form-section' data-indent={1}>
          <h3><Text name='Voit tiedustella asiaa oppilaitokseltasi.'/></h3>
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

          <Yhteystiedot henkilö={henkilö} yhteystietoP={yhteystietoP} isLoadingP={isLoadingP}/>
        </div>
      ))}
    </div>
  )
}
