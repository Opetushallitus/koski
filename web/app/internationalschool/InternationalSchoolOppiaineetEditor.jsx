import React from 'baret'
import { modelItems } from '../editor/EditorModel'
import { LukionOppiaineetTableHead } from '../lukio/fragments/LukionOppiaineetTableHead'
import { LukionOppiaineEditor } from '../lukio/LukionOppiaineEditor'
import Text from '../i18n/Text'
import { UusiInternationalSchoolOppiaineDropdown } from './UusiInternationalSchoolOppiaineDropdown'

export default ({ suorituksetModel }) => {
  return (
    <div>
      <table className="suoritukset oppiaineet">
        <LukionOppiaineetTableHead
          laajuusyksikkö={null}
          arvosanaHeader={<Text name="Arvosana" />}
        />
        <tbody>
          <Oppiaineet
            aineet={modelItems(suorituksetModel)}
            päätasonSuoritusModel={suorituksetModel.context.suoritus}
          />
        </tbody>
      </table>
    </div>
  )
}

const Oppiaineet = ({ aineet, päätasonSuoritusModel }) => (
  <React.Fragment>
    {aineet.map((aine, i) => (
      <LukionOppiaineEditor
        key={i}
        oppiaine={aine}
        showLaajuus={false}
        showArviointi={true}
      />
    ))}
    <tr className="uusi-oppiaine" key="uusi-oppiaine">
      <td colSpan="4">
        <UusiInternationalSchoolOppiaineDropdown
          model={päätasonSuoritusModel}
        />
      </td>
    </tr>
  </React.Fragment>
)
