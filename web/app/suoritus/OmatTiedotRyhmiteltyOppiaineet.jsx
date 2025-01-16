import React from 'baret'
import * as R from 'ramda'
import { modelData, modelItems } from '../editor/EditorModel'
import { t } from '../i18n/i18n'
import { isMobileAtom } from '../util/isMobileAtom'
import { OmatTiedotLukionOppiaineetTableHead } from '../lukio/fragments/LukionOppiaineetTableHead'
import { arvosanaFootnote } from '../ib/IB'
import { FootnoteDescriptions } from '../components/footnote'
import { OmatTiedotLukionOppiaine } from '../lukio/OmatTiedotLukionOppiaineet'
import { resolveArvosanaModel } from './ArvosanaEditor'
import Text from '../i18n/Text'
import { resolvePropertiesByType } from './RyhmiteltyOppiaineetEditor'

const OmatTiedotOppiaineryhmä = ({
  title,
  aineet,
  useOppiaineLaajuus,
  customOsasuoritusTitle,
  customKurssitSortFn
}) => (
  <React.Fragment>
    <h4 className="aineryhma-title">{t(title)}</h4>
    <table className="omattiedot-suoritukset">
      <OmatTiedotLukionOppiaineetTableHead
        arvosanaHeader={
          aineet.some(
            (aine) =>
              resolveArvosanaModel(aine, 'arviointi') ||
              resolveArvosanaModel(aine, 'predictedArviointi')
          ) ? (
            <Text name="Arvosana" />
          ) : null
        }
      />
      <tbody>
        {aineet &&
          aineet.map((oppiaine, oppiaineIndex) => {
            const predictedArviointi = modelData(
              oppiaine,
              'predictedArviointi.-1'
            )
            const arviointi = modelData(oppiaine, 'arviointi.-1')
            const predictedArviointiVanhassaHaarassa = arviointi?.predicted
            const arviointiField =
              arviointi && !arviointi?.predicted
                ? 'arviointi' // Vanhan tietomallin mukaisesti predicted löytyy arviointi-kentästä
                : predictedArviointi
                  ? 'predictedArviointi' // Predicted grade löytyy, mutta ei päättöarvosanaa
                  : 'arviointi'

            const footnote =
              (predictedArviointiVanhassaHaarassa ||
                (!arviointi && predictedArviointi)) &&
              arvosanaFootnote

            return (
              <OmatTiedotLukionOppiaine
                baret-lift
                key={oppiaineIndex}
                oppiaine={oppiaine}
                isMobile={isMobileAtom}
                footnote={footnote}
                showKeskiarvo={false}
                notFoundText={null}
                useOppiaineLaajuus={useOppiaineLaajuus}
                customOsasuoritusTitle={customOsasuoritusTitle}
                customKurssitSortFn={customKurssitSortFn}
                arviointiField={arviointiField}
              />
            )
          })}
      </tbody>
    </table>
  </React.Fragment>
)

export default ({ suorituksetModel, päätasonSuorituksenTyyppi }) => {
  const { suoritus: päätasonSuoritusModel } = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {
    groupAineet,
    useOppiaineLaajuus,
    customOsasuoritusTitleOmatTiedot,
    customKurssitSortFn
  } = resolvePropertiesByType(päätasonSuorituksenTyyppi)
  const { aineryhmät, muutAineet, footnotes } = groupAineet(
    oppiaineet,
    päätasonSuoritusModel
  )

  return aineryhmät || muutAineet ? (
    <div className="aineryhmat">
      {aineryhmät &&
        aineryhmät.map((ryhmät) =>
          ryhmät.map((r) => (
            <OmatTiedotOppiaineryhmä
              key={r.ryhmä.koodiarvo}
              title={r.ryhmä.nimi}
              aineet={r.aineet}
              useOppiaineLaajuus={useOppiaineLaajuus}
              customOsasuoritusTitle={customOsasuoritusTitleOmatTiedot}
              customKurssitSortFn={customKurssitSortFn}
            />
          ))
        )}

      {muutAineet && !R.isEmpty(muutAineet) && (
        <OmatTiedotOppiaineryhmä
          key="lisäaineet"
          title="Lisäaineet"
          aineet={muutAineet}
          useOppiaineLaajuus={useOppiaineLaajuus}
          customOsasuoritusTitle={customOsasuoritusTitleOmatTiedot}
          customKurssitSortFn={customKurssitSortFn}
        />
      )}

      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes} />}
    </div>
  ) : null
}
