import React from 'baret'
import Text from '../../i18n/Text'

export const LukionOppiaineetTableHead = ({
  laajuusyksikkö = 'kurssia',
  showArviointi = true,
  laajuusHeaderText = 'Laajuus',
  arvosanaHeader = <Text name="Arvosana" />,
  showHyväksytystiArvioitujenLaajuus = false,
  showPredictedArviointi = false,
  predictedArvosanaHeader = <Text name="Predicted grade" />
}) => (
  <thead>
    <tr>
      <th className="suorituksentila"></th>
      <th className="oppiaine">
        <Text name="Oppiaine" />
      </th>
      {laajuusyksikkö && (
        <th className="laajuus">
          <Text name={`${laajuusHeaderText} (${laajuusyksikkö})`} />
        </th>
      )}
      {showHyväksytystiArvioitujenLaajuus && laajuusyksikkö && (
        <th className="laajuus arvioitu">
          <Text name={`Hyväksytysti arvioitu (${laajuusyksikkö})`} />
        </th>
      )}
      {showPredictedArviointi && (
        <th className="predicted-arvosana">{predictedArvosanaHeader}</th>
      )}
      {showArviointi && <th className="arvosana">{arvosanaHeader}</th>}
    </tr>
    <tr>
      <th colSpan="5">
        <hr />
      </th>
    </tr>
  </thead>
)

export const OmatTiedotLukionOppiaineetTableHead = ({
  arvosanaHeader = <Text name="Arvosana" />
}) => (
  <thead>
    <tr>
      <th className="oppiaine" scope="col">
        <Text name="Oppiaine" />
      </th>
      <th className="arvosana" scope="col">
        {arvosanaHeader}
      </th>
    </tr>
  </thead>
)
