import React from 'baret'
import Bacon from 'baconjs'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import TutkintoAutocomplete from '../virkailija/TutkintoAutocomplete'
import { fetchLisättävätTutkinnonOsat } from './TutkinnonOsa'
import LisaaTutkinnonOsaDropdown from './LisaaTutkinnonOsaDropdown'

const TutkinnonOsaToisestaTutkinnostaPicker = ({
  tutkintoAtom,
  tutkinnonOsaAtom,
  oppilaitos,
  autoFocus = true,
  tutkintoTitle = 'Tutkinto',
  tutkinnonOsaTitle = 'Tutkinnon osa',
  tutkintoPlaceholder = ''
}) => (
  <div className="valinnat">
    <TutkintoAutocomplete
      autoFocus={autoFocus}
      tutkintoAtom={tutkintoAtom}
      oppilaitosP={Bacon.constant(oppilaitos)}
      title={<Text name={tutkintoTitle} />}
      placeholder={tutkintoPlaceholder}
    />
    {tutkintoAtom.flatMapLatest((tutkinto) => {
      const osatP = tutkinto
        ? fetchLisättävätTutkinnonOsat(tutkinto.diaarinumero).map('.osat')
        : Bacon.constant([])
      return (
        <LisaaTutkinnonOsaDropdown
          selectedAtom={tutkinnonOsaAtom}
          title={tutkinnonOsaTitle}
          osat={osatP}
          placeholder={osatP
            .map('.length')
            .map((len) =>
              len === 0 ? 'Valitse ensin tutkinto' : 'Valitse tutkinnon osa'
            )
            .map(t)}
        />
      )
    })}
  </div>
)

export default TutkinnonOsaToisestaTutkinnostaPicker
