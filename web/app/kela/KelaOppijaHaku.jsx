import React from 'baret'
import Atom from 'bacon.atom'
import { navigateTo } from '../util/location'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'

const searchStringAtom = Atom('')

searchStringAtom.changes().onValue((str) => {
  if (isValidHetu(str)) {
    navigateTo(`/koski/kela/${capitalizeHetu(str)}`)
  }
})

export const KelaOppijaHaku = () => {
  return (
    <div>
      {searchStringAtom.map((str) => {
        const showAsInvalid = str.length > 4 && !isValidHetu(str)
        return (
          <>
            <div className="kela-oppija-haku">
              <label>
                <h3>
                  <Text name="Hae opiskelija" />
                </h3>
                <input
                  className={showAsInvalid ? 'invalid' : ''}
                  id="kela-search-query"
                  type="text"
                  value={str}
                  placeholder={t('Henkilötunnus')}
                  onChange={(e) => searchStringAtom.set(e.target.value)}
                />
              </label>
            </div>
            {showAsInvalid && (
              <span className="virheellinen-hetu">
                <Text name="Virheellinen henkilötunnus" />
              </span>
            )}
          </>
        )
      })}
    </div>
  )
}

const hetuRegex = new RegExp(
  '^([012][0-9]|3[01])(0[1-9]|1[0-2])([0-9]{2})(A|B|C|D|E|F|X|Y|W|V|U|-|\\+)([0-9]{3})([0-9A-FHJ-NPR-Y])$'
)
const isValidHetu = (str) => hetuRegex.test(str)
const capitalizeHetu = (h) =>
  /\d{6}[+\-ABCDEFXYWVU]\d{3}[0-9A-Z]/i.test(h) ? h.toUpperCase() : h
