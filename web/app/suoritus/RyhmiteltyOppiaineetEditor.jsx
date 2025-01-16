import React from 'baret'
import * as R from 'ramda'
import { modelData, modelItems } from '../editor/EditorModel'
import { t } from '../i18n/i18n'
import { LukionOppiaineetTableHead } from '../lukio/fragments/LukionOppiaineetTableHead'
import { LukionOppiaineEditor } from '../lukio/LukionOppiaineEditor'
import { arvosanaFootnote, ibRyhmät } from '../ib/IB'
import { UusiRyhmiteltyOppiaineDropdown } from '../oppiaine/UusiRyhmiteltyOppiaineDropdown'
import { FootnoteDescriptions } from '../components/footnote'
import {
  diaKurssitSortFn,
  diaLukukausiAlternativesCompletionFn,
  diaRyhmät
} from '../dia/DIA'
import Text from '../i18n/Text'

const diaCustomizations = {
  groupAineet: diaRyhmät,
  laajuusyksikkö: 'vuosiviikkotuntia',
  useOppiaineLaajuus: true,
  showArviointi: false,
  showRyhmättömät: true,
  additionalEditableProperties: ['suorituskieli'],
  customOsasuoritusTitle: 'osasuoritus',
  customOsasuoritusTitleOmatTiedot: 'Suoritus',
  customOsasuoritusAlternativesFn: diaLukukausiAlternativesCompletionFn,
  oppiaineOptionsFilter: (m) => m.value.classes.includes('diaosaalueoppiaine'),
  getFootnote: R.identity,
  customKurssitSortFn: diaKurssitSortFn
}

const typeDependentCustomizations = {
  ibtutkinto: {
    groupAineet: ibRyhmät,
    laajuusyksikkö: 'kurssia',
    useOppiaineLaajuus: false,
    showArviointi: true,
    showRyhmättömät: false,
    oppiaineOptionsFilter: R.identity,
    getFootnote: (oppiaine) =>
      modelData(oppiaine, 'arviointi.-1.predicted') && arvosanaFootnote
  },
  diavalmistavavaihe: diaCustomizations,
  diatutkintovaihe: R.mergeDeepWith(R.concat, diaCustomizations, {
    additionalEditableProperties: [
      'keskiarvo',
      'koetuloksenNelinkertainenPistemäärä',
      'vastaavuustodistuksenTiedot'
    ]
  })
}

export const resolvePropertiesByType = (päätasonSuorituksenTyyppi) => {
  const customizations = typeDependentCustomizations[päätasonSuorituksenTyyppi]
  if (!customizations)
    console.error(
      `Oppiaineiden ryhmittely ei onnistu päätason suoritukselle ${päätasonSuorituksenTyyppi}.`
    )
  return customizations
}

const RyhmättömätAineet = ({
  aineet,
  edit,
  additionalEditableProperties,
  additionalEditableKoulutusmoduuliProperties,
  useOppiaineLaajuus,
  showArviointi,
  päätasonSuoritusModel,
  oppiaineOptionsFilter,
  customOsasuoritusTitle,
  customOsasuoritusAlternativesCompletionFn,
  customKurssitSortFn
}) => (
  <React.Fragment>
    {aineet && (!R.isEmpty(aineet) || edit) && (
      <tr className="aineryhmä" key="lisäaineet">
        <th colSpan="4">{t('Lisäaineet')}</th>
      </tr>
    )}
    {aineet &&
      aineet.map((aine) => (
        <LukionOppiaineEditor
          key={modelData(aine, 'koulutusmoduuli.tunniste.koodiarvo')}
          oppiaine={aine}
          additionalEditableProperties={additionalEditableProperties}
          additionalEditableKoulutusmoduuliProperties={
            additionalEditableKoulutusmoduuliProperties
          }
          useOppiaineLaajuus={useOppiaineLaajuus}
          showArviointi={showArviointi}
          customOsasuoritusTitle={customOsasuoritusTitle}
          customOsasuoritusAlternativesCompletionFn={
            customOsasuoritusAlternativesCompletionFn
          }
          customKurssitSortFn={customKurssitSortFn}
        />
      ))}
    <tr className="uusi-oppiaine" key="uusi-oppiaine-lisäaineet">
      <td colSpan="4">
        <UusiRyhmiteltyOppiaineDropdown
          model={päätasonSuoritusModel}
          optionsFilter={oppiaineOptionsFilter}
        />
      </td>
    </tr>
  </React.Fragment>
)

export default ({
  suorituksetModel,
  päätasonSuorituksenTyyppi,
  additionalEditableKoulutusmoduuliProperties
}) => {
  const { edit, suoritus: päätasonSuoritusModel } = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {
    groupAineet,
    laajuusyksikkö,
    useOppiaineLaajuus,
    showArviointi,
    showRyhmättömät,
    additionalEditableProperties,
    customOsasuoritusTitle,
    customOsasuoritusAlternativesFn,
    oppiaineOptionsFilter,
    getFootnote,
    customKurssitSortFn
  } = resolvePropertiesByType(päätasonSuorituksenTyyppi)

  const { aineryhmät, muutAineet, footnotes } = groupAineet(
    oppiaineet,
    päätasonSuoritusModel,
    edit
  )

  const isIbTutkinto = suorituksetModel.parent.value.classes.includes(
    'ibtutkinnonsuoritus'
  )

  const commonOppiaineProps = {
    additionalEditableProperties,
    additionalEditableKoulutusmoduuliProperties,
    useOppiaineLaajuus,
    showArviointi,
    showKeskiarvo: !isIbTutkinto,
    showPredictedArviointi: isIbTutkinto,
    customOsasuoritusTitle,
    customOsasuoritusAlternativesCompletionFn: customOsasuoritusAlternativesFn,
    customKurssitSortFn
  }

  return aineryhmät ? (
    <div>
      <table className="suoritukset oppiaineet">
        <LukionOppiaineetTableHead
          laajuusyksikkö={laajuusyksikkö}
          showArviointi={showArviointi}
          showPredictedArviointi={isIbTutkinto}
          arvosanaHeader={
            isIbTutkinto ? <Text name="Päättöarvosana" /> : undefined
          }
        />
        <tbody>
          {aineryhmät.map((ryhmät) =>
            ryhmät.map((r) => [
              <tr className="aineryhmä" key={r.ryhmä.koodiarvo}>
                <th colSpan="4">{t(r.ryhmä.nimi)}</th>
              </tr>,
              r.aineet &&
                r.aineet.map((oppiaine, oppiaineIndex) => (
                  <LukionOppiaineEditor
                    key={oppiaineIndex}
                    oppiaine={oppiaine}
                    footnote={getFootnote(oppiaine)}
                    {...commonOppiaineProps}
                  />
                )),
              <tr
                className="uusi-oppiaine"
                key={`uusi-oppiaine-${r.ryhmä.koodiarvo}`}
              >
                <td colSpan="4">
                  <UusiRyhmiteltyOppiaineDropdown
                    model={päätasonSuoritusModel}
                    aineryhmä={r.ryhmä}
                    optionsFilter={oppiaineOptionsFilter}
                  />
                </td>
              </tr>
            ])
          )}
          {showRyhmättömät && (
            <RyhmättömätAineet
              aineet={muutAineet}
              edit={edit}
              päätasonSuoritusModel={päätasonSuoritusModel}
              oppiaineOptionsFilter={R.complement(oppiaineOptionsFilter)}
              {...commonOppiaineProps}
            />
          )}
        </tbody>
      </table>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes} />}
    </div>
  ) : null
}
