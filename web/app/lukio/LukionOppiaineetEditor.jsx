import React from 'react'
import * as R from 'ramda'
import { LukionOppiaineEditor } from './LukionOppiaineEditor'
import { UusiLukionOppiaineDropdown } from './UusiLukionOppiaineDropdown'
import {
  modelErrorMessages,
  modelItems,
  modelLookup,
  modelData
} from '../editor/EditorModel'
import { LukionOppiaineetTableHead } from './fragments/LukionOppiaineetTableHead'
import { t } from '../i18n/i18n'
import { flatMapArray } from '../util/util'
import {
  arvioidutOsasuoritukset,
  hyväksytystiArvioidutOsasuoritukset,
  hylkäämättömätOsasuoritukset,
  isLukioOps2019,
  isLuvaOps2019,
  isPreIbLukioOps2019,
  laajuudet
} from './lukio'
import { numberToString } from '../util/format.js'
import { isPaikallinen } from '../suoritus/Koulutusmoduuli'
import { FootnoteDescriptions } from '../components/footnote'
import Text from '../i18n/Text'

export const LukionOppiaineetEditor = ({
  suorituksetModel,
  classesForUusiOppiaineenSuoritus,
  suoritusFilter,
  additionalOnlyEditableProperties,
  additionalEditableKoulutusmoduuliProperties,
  showKeskiarvo = true,
  laajuusHeaderText = 'Laajuus',
  useHylkäämättömätLaajuus = true,
  showHyväksytystiArvioitujenLaajuus = false,
  forceLaajuusOpintopisteinä = false
}) => {
  const { edit, suoritus: päätasonSuoritusModel } = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel).filter(
    suoritusFilter || R.identity
  )
  if (!edit && R.isEmpty(oppiaineet)) return null

  const oppiaineRows = oppiaineet.map((oppiaine, oppiaineIndex) => (
    <LukionOppiaineEditor
      key={oppiaineIndex}
      oppiaine={oppiaine}
      additionalOnlyEditableProperties={additionalOnlyEditableProperties}
      additionalEditableKoulutusmoduuliProperties={
        additionalEditableKoulutusmoduuliProperties
      }
      customOsasuoritusTitle="osasuoritus"
      showArviointiEditor={!oppiaine.value.classes.includes('arvioinniton')}
      showKeskiarvo={showKeskiarvo}
      useHylkäämättömätLaajuus={useHylkäämättömätLaajuus}
      showHyväksytystiArvioitujenLaajuus={showHyväksytystiArvioitujenLaajuus}
    />
  ))
  const errorRows = oppiaineet.map((oppiaine) =>
    modelErrorMessages(oppiaine).map((error, i) => (
      <tr key={'error-' + i} className="error">
        <td colSpan="42" className="error">
          {error}
        </td>
      </tr>
    ))
  )
  const oppiaineetWithErrorRows = R.zip(oppiaineRows, errorRows)
  const laajuusyksikkö =
    (forceLaajuusOpintopisteinä && 'opintopistettä') ||
    modelData(oppiaineet[0], 'koulutusmoduuli.laajuus.yksikkö.nimi.fi') ||
    'kurssia'

  const arvosanaHeaderText = showKeskiarvo ? 'Arvosana (keskiarvo)' : 'Arvosana'

  const arvosanaHeader = <Text name={arvosanaHeaderText} />

  return (
    <section>
      <table className="suoritukset oppiaineet">
        {!R.isEmpty(oppiaineet) && (
          <LukionOppiaineetTableHead
            laajuusyksikkö={laajuusyksikkö}
            laajuusHeaderText={laajuusHeaderText}
            arvosanaHeader={arvosanaHeader}
            showHyväksytystiArvioitujenLaajuus={
              showHyväksytystiArvioitujenLaajuus
            }
          />
        )}
        <tbody>{oppiaineetWithErrorRows}</tbody>
      </table>
      <OsasuorituksetYhteensa
        suorituksetModel={suorituksetModel}
        oppiaineet={oppiaineet}
      />
      {paikallisiaLukionOppiaineitaTaiOsasuorituksia(oppiaineet) && (
        <FootnoteDescriptions
          data={[
            {
              title: paikallinenOsasuoritusTaiOppiaineText(
                suorituksetModel.context.suoritus
              ),
              hint: '*'
            }
          ]}
        />
      )}
      <UusiLukionOppiaineDropdown
        model={päätasonSuoritusModel}
        oppiaineenSuoritusClasses={classesForUusiOppiaineenSuoritus}
      />
    </section>
  )
}

export const paikallisiaLukionOppiaineitaTaiOsasuorituksia = (oppiaineet) =>
  oppiaineet.some(
    (aine) =>
      isPaikallinen(modelLookup(aine, 'koulutusmoduuli')) ||
      paikallisiaOsasuorituksia(aine)
  )
export const paikallisiaOsasuorituksia = (oppiaine) =>
  modelItems(oppiaine, 'osasuoritukset').some((osasuoritus) =>
    isPaikallinen(modelLookup(osasuoritus, 'koulutusmoduuli'))
  )

export const OsasuorituksetYhteensa = ({ suorituksetModel, oppiaineet }) => {
  const isLukio2019 = isLukioOps2019(suorituksetModel.context.suoritus)
  const isPreIB2019 = isPreIbLukioOps2019(suorituksetModel.context.suoritus)
  const isLuva2019 = isLuvaOps2019(suorituksetModel.context.suoritus)

  return isLukio2019 || isPreIB2019 || isLuva2019 ? (
    <div className="kurssit-yhteensä">
      {t('Arvioitujen osasuoritusten laajuus yhteensä') +
        ': ' +
        numberToString(
          laajuudet(
            flatMapArray(oppiaineet, (oppiaine) =>
              arvioidutOsasuoritukset(modelItems(oppiaine, 'osasuoritukset'))
            )
          ),
          1
        )}
      <br />
      {t('Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä') +
        ': ' +
        numberToString(
          laajuudet(
            flatMapArray(oppiaineet, (oppiaine) =>
              hyväksytystiArvioidutOsasuoritukset(
                modelItems(oppiaine, 'osasuoritukset')
              )
            )
          ),
          1
        )}
    </div>
  ) : (
    <div className="kurssit-yhteensä">
      {t('Suoritettujen kurssien laajuus yhteensä') +
        ': ' +
        numberToString(
          laajuudet(
            flatMapArray(oppiaineet, (oppiaine) =>
              hylkäämättömätOsasuoritukset(
                modelItems(oppiaine, 'osasuoritukset')
              )
            )
          )
        )}
    </div>
  )
}

export const paikallinenOsasuoritusTaiOppiaineText = (päätasonSuoritus) =>
  isLukioOps2019(päätasonSuoritus) ||
  isPreIbLukioOps2019(päätasonSuoritus) ||
  isLuvaOps2019(päätasonSuoritus)
    ? 'Paikallinen opintojakso tai oppiaine'
    : 'Paikallinen kurssi tai oppiaine'
