import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../util/http'
import { showError, navigateTo } from '../util/location'
import delays from '../util/delays'
import Link from '../components/Link'
import Highlight from 'react-highlighter'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { userP } from '../util/user.js'

export const searchStringAtom = Atom('')
const oppijaHakuE = searchStringAtom.changes()
const acceptableQuery = (q) => q.length >= 3
const capitalizeHetu = (h) =>
  /\d{6}[+\-A]\d{3}[0-9A-Z]/i.test(h) ? h.toUpperCase() : h

const hakuTulosE = oppijaHakuE
  .debounce(delays().delay(500))
  .flatMapLatest((query) =>
    (acceptableQuery(query)
      ? Http.post(
          '/koski/api/henkilo/search',
          { query: capitalizeHetu(query).trim() },
          { willHandleErrors: true }
        )
      : Bacon.once({ henkilöt: [] })
    ).map((response) => ({ response, query }))
  )

hakuTulosE.onError(showError)

const oppijatP = Bacon.update(
  { query: '', response: { henkilöt: [] } },
  hakuTulosE.skipErrors(),
  (current, hakutulos) => hakutulos
)

const searchInProgressP = oppijaHakuE
  .filter(acceptableQuery)
  .awaiting(hakuTulosE.mapError())
  .throttle(delays().delay(200))

export const OppijaHaku = () => {
  const selectedIndexAtom = Atom(-1)

  const onKeyDown = (options) => (e) => {
    const keyHandlers = {
      ArrowUp: () => {
        selectedIndexAtom.modify((i) => Math.max(i - 1, -1))
      },
      ArrowDown: () => {
        selectedIndexAtom.modify((i) => Math.min(i + 1, options.length - 1))
      },
      Enter: () => {
        const option = options[selectedIndexAtom.get()]
        option && navigateTo(option.href)
      }
    }
    if (
      e.getModifierState('Shift') ||
      e.getModifierState('Alt') ||
      e.getModifierState('Meta') ||
      e.getModifierState('Control')
    )
      return
    const handler = keyHandlers[e.key]
    if (handler) {
      e.preventDefault()
      handler()
    }
  }

  const optionsP = Bacon.combineWith(
    oppijatP,
    selectedIndexAtom,
    ({ response, query }, selectedIndex) => {
      if (response.henkilöt.length > 0) {
        return response.henkilöt.map((o, i) => {
          const href = `/koski/oppija/${o.oid}`
          const oppija =
            o.sukunimi + ', ' + o.etunimet + (o.hetu ? ' (' + o.hetu + ')' : '')
          return {
            href,
            element: (
              <li
                className={i === selectedIndex ? 'selected' : ''}
                role="listitem"
                aria-label={`${o.sukunimi}, ${o.etunimet}`}
                key={i}
              >
                <Link href={href}>
                  <Highlight
                    ignoreDiacritics={true}
                    diacriticsBlacklist={'åäöÅÄÖ'}
                    search={query}
                  >
                    {oppija}
                  </Highlight>
                </Link>
              </li>
            )
          }
        })
      } else if (response.canAddNew) {
        const hetuQuery = response.hetu
          ? encodeURIComponent('hetu') + '=' + encodeURIComponent(response.hetu)
          : ''
        const oidQuery = response.oid
          ? encodeURIComponent('oid') + '=' + encodeURIComponent(response.oid)
          : ''
        const href =
          '/koski/uusioppija#' +
          hetuQuery +
          (hetuQuery && oidQuery ? '&' : '') +
          (oidQuery || '')
        return [
          {
            href,
            element: (
              <Link
                key={selectedIndexAtom.map((i) => i)}
                baret-lift
                className={selectedIndexAtom.map(
                  (i) => 'lisaa-oppija' + (i === 0 ? ' selected' : '')
                )}
                href={href}
              >
                <Text name="Lisää uusi opiskelija" />
              </Link>
            )
          }
        ]
      } else {
        return []
      }
    }
  )

  return (
    <div
      className={searchInProgressP.map((searching) =>
        searching ? 'oppija-haku searching' : 'oppija-haku'
      )}
    >
      <div>
        <label>
          <h3>
            {userP.map((user) =>
              user.hasWriteAccess ? (
                <Text name="Hae tai lisää opiskelija" />
              ) : (
                <Text name="Hae opiskelija" />
              )
            )}
          </h3>
          <input
            type="text"
            role="search"
            value={searchStringAtom}
            id="search-query"
            placeholder={t('henkilötunnus, nimi tai oppijanumero')}
            onChange={(e) => searchStringAtom.set(e.target.value)}
            onKeyDown={optionsP.map(onKeyDown)}
            autoFocus
            data-testid="oppijahaku"
          ></input>
        </label>
      </div>
      <div className="hakutulokset">
        {Bacon.combineWith(
          oppijatP,
          optionsP,
          ({ response, query }, options) => {
            if (response.henkilöt.length > 0) {
              return (
                <ul role="list" aria-label="Hakutulokset">
                  {options.map((option) => option.element)}
                </ul>
              )
            }
            if (query.length > 2) {
              return (
                <div
                  className="no-results"
                  role="status"
                  aria-label="Ei hakutuloksia"
                >
                  <Text name="Ei hakutuloksia" />
                  {options.map((option) => option.element)}
                  {response.error && (
                    <div className="error" role="error" aria-live="polite">
                      {response.error}
                    </div>
                  )}
                </div>
              )
            }
          }
        )}
      </div>
    </div>
  )
}
