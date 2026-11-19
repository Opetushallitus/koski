import React, { useCallback, useEffect, useState } from 'react'
import ReactDOM from 'react-dom'
import { enrichJsonRequest } from '../api-fetch'

const pollIntervalMs = 3000

type RawResponse = {
  status: number
  statusText: string
  body: any
}

type State =
  | { phase: 'idle' }
  | { phase: 'sending' }
  | { phase: 'polling'; response: RawResponse }
  | { phase: 'done'; response: RawResponse }

const isInProgress = (body: any): boolean =>
  body && (body.status === 'pending' || body.status === 'running')

const readResponse = async (response: Response): Promise<RawResponse> => {
  const text = await response.text()
  let body: any = text
  try {
    body = text ? JSON.parse(text) : null
  } catch (_e) {
    // vastaus ei ollut JSONia, näytetään sellaisenaan
  }
  return { status: response.status, statusText: response.statusText, body }
}

const fetchQuery = (apiPath: string, queryId: string): Promise<RawResponse> =>
  fetch(
    `${apiPath}/${encodeURIComponent(queryId)}`,
    enrichJsonRequest('GET', 'application/json')
  ).then(readResponse)

export type MassaluovutusKokeileProps = {
  apiPath: string
  example: string
}

export const MassaluovutusKokeile: React.FC<MassaluovutusKokeileProps> = ({
  apiPath,
  example
}) => {
  const [query, setQuery] = useState(example)
  const [state, setState] = useState<State>({ phase: 'idle' })

  const send = useCallback(async () => {
    let body: unknown
    try {
      body = JSON.parse(query)
    } catch (e) {
      setState({
        phase: 'done',
        response: {
          status: 0,
          statusText: 'Virheellinen JSON',
          body: String(e)
        }
      })
      return
    }
    setState({ phase: 'sending' })
    const response = await fetch(
      apiPath,
      enrichJsonRequest('POST', 'application/json', { body })
    ).then(readResponse)
    setState(
      response.status === 200 && isInProgress(response.body)
        ? { phase: 'polling', response }
        : { phase: 'done', response }
    )
  }, [apiPath, query])

  useEffect(() => {
    if (state.phase !== 'polling') return
    const queryId = state.response.body.queryId
    const timer = setTimeout(async () => {
      const response = await fetchQuery(apiPath, queryId)
      setState(
        response.status === 200 && isInProgress(response.body)
          ? { phase: 'polling', response }
          : { phase: 'done', response }
      )
    }, pollIntervalMs)
    return () => clearTimeout(timer)
  }, [apiPath, state])

  const busy = state.phase === 'sending' || state.phase === 'polling'

  return (
    <div className="massaluovutus-kokeile">
      <p>
        {'Kysely tehdään kirjautuneen käyttäjän oikeuksilla, ja tuloksia pollataan, kunnes kysely on valmis. ' +
          'Esimerkki on mallipohja: sen organisaatio- ja oppijatunnisteet ovat testidataa, jota ei välttämättä ole tässä ympäristössä, joten korvaa ne ennen lähettämistä.'}
      </p>
      <textarea
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        spellCheck={false}
        disabled={busy}
      />
      <div className="buttons">
        <button className="koski-button blue" disabled={busy} onClick={send}>
          {'Lähetä kysely'}
        </button>
        <button
          className="koski-button"
          disabled={busy}
          onClick={() => {
            setQuery(example)
            setState({ phase: 'idle' })
          }}
        >
          {'Palauta esimerkki'}
        </button>
      </div>
      <Result state={state} />
    </div>
  )
}

const Result: React.FC<{ state: State }> = ({ state }) => {
  if (state.phase === 'idle') return null
  if (state.phase === 'sending') {
    return <div className="result">{'Lähetetään…'}</div>
  }
  const { response } = state
  const body = response.body
  const files: string[] = body && Array.isArray(body.files) ? body.files : []
  return (
    <div className="result">
      <div className="status">
        {response.status === 401 ? (
          <span>
            {'401 Kirjaudu sisään ensin: '}
            <a href="/koski/virkailija" target="_blank" rel="noopener">
              {'Kirjaudu'}
            </a>
          </span>
        ) : (
          `${response.status} ${response.statusText}` +
          (body && body.status ? ` – kyselyn tila: ${body.status}` : '')
        )}
        {state.phase === 'polling' && ' (päivitetään…)'}
      </div>
      {files.length > 0 && (
        <ul className="files">
          {files.map((url) => (
            <li key={url}>
              <a href={url} target="_blank" rel="noopener">
                {url.substring(url.lastIndexOf('/') + 1)}
              </a>
            </li>
          ))}
        </ul>
      )}
      {body && body.password && (
        <div className="password">{`Salasana: ${body.password}`}</div>
      )}
      <pre>
        {typeof body === 'string' ? body : JSON.stringify(body, null, 2)}
      </pre>
    </div>
  )
}

const mounted: Element[] = []

// Dokumentaation HTML tulee palvelimelta valmiina; lomake liitetään sen query-tester-paikkamerkkeihin
export const mountQueryTesters = (root: HTMLElement | null) => {
  if (!root) {
    mounted.splice(0).forEach((el) => ReactDOM.unmountComponentAtNode(el))
    return
  }
  root.querySelectorAll<HTMLElement>('.query-tester').forEach((el) => {
    const apiPath = el.dataset.apiPath
    const example = el.dataset.example
    if (!apiPath || !example) return
    ReactDOM.render(
      <MassaluovutusKokeile apiPath={apiPath} example={example} />,
      el
    )
    mounted.push(el)
  })
}
