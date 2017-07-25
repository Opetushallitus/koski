import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Highlight from 'react-highlight'
import Http from './http'
import Dropdown from './Dropdown.jsx'


function selectElementContents(el) {
  var range = document.createRange()
  range.selectNodeContents(el)
  var sel = window.getSelection()
  sel.removeAllRanges()
  sel.addRange(range)
}

const apiBaseUrl = () => document.location.protocol + '//' + document.location.host

const queryParameters = inputs => inputs.reduce((query, v) => {
  return v.value ? query + (query ? '&' : '?') + encodeURIComponent(v.name) + '=' + encodeURIComponent(v.value) : query
},'')

const makeApiUrl = (basePath, params) => {
  let path = basePath
  R.filter(p => p.type === 'path', params).forEach(function(input) {
    path = path.replace('{' + input.name + '}', encodeURIComponent(input.value))
  })
  return apiBaseUrl() + path + queryParameters(R.filter(p => p.type === 'query', params))
}

const curlCommand = (method, url) => {
  var curl = 'curl "' + url + '" --user kalle:kalle'
  if (method != 'GET') {
    curl += ' -X ' + method
  }
  if (method == 'POST' || method == 'PUT') {
    curl += ' -H "content-type: application/json" -d @curltestdata.json'
  }
  return curl
}

const QueryParameters = ({operation, collectorBus}) => {
  const valueAList = R.map(p => Atom({name: p.name, value: p.examples[0], type: p.type}), operation.parameters)

  collectorBus.push(valueAList)

  R.forEach(v => {
    v.changes().onValue(() => collectorBus.push(valueAList))
  }, valueAList)


  return (
    <div className="parameters">
      <h4>{'Parametrit'}</h4>
      <table>
        <thead>
        <tr>
          <th>{'Nimi'}</th>
          <th>{'Merkitys'}</th>
          <th>{'Arvo'}</th>
        </tr>
        </thead>
        <tbody>
        {R.map(([parameter, selectedValueA]) => (
          <tr>
            <td>
              {parameter.name}
            </td>
            <td>
              {parameter.description}
            </td>
            <td>
              {parameter.examples.length > 1
                ? (
                  <Dropdown options={parameter.examples} keyValue={R.identity} displayValue={R.identity} selected={selectedValueA} onSelectionChanged={v => selectedValueA.set(v)}/>
                )
                : <input value={selectedValueA} onChange={e => selectedValueA.set(e.target.value)}/>
              }
            </td>
          </tr>
        ), R.zip(operation.parameters, R.map(v => v.view('value'), valueAList)))}
        </tbody>
      </table>
    </div>
  )
}


const PostDataExamples = ({operation, collectorBus}) => {
  const selectedValueA = Atom(operation.examples[0])
  const codeA = Atom(JSON.stringify(selectedValueA.get().data, null, 2))

  collectorBus.push(selectedValueA.get().data)

  selectedValueA.changes().onValue(v => {
    codeA.set(JSON.stringify(v.data, null, 2))
    collectorBus.push(v.data)
  })

  return (
    <div className="postdata">
      <h4>{'Syötedata'}</h4>
      <div className="examples">
        <label>{'Esimerkkejä'}
          <Dropdown options={operation.examples} keyValue={v => v.name} displayValue={v => v.name} selected={selectedValueA} onSelectionChanged={v => selectedValueA.set(v)}/>
        </label>
      </div>
      <textarea cols="80" rows="50" value={codeA} onChange={c => {codeA.set(c.target.value)}} style={{'font-family': 'monospace'}}></textarea>
    </div>
  )
}

const ApiOperationTesterParameters = ({operation, queryCollectorBus, postCollectorBus}) => {
  if (operation.examples.length > 0) {
    return <PostDataExamples operation={operation} collectorBus={postCollectorBus}/>
  } else if (operation.parameters.length > 0) {
    return <QueryParameters operation={operation} collectorBus={queryCollectorBus}/>
  } else {
    return <div></div>
  }
}

const ApiOperationTester = ({operation}) => {
  const parametersA = Atom([])
  const loadingA = Atom(false)
  const curlVisibleA = Atom(false)
  const curlValueA = Atom(curlCommand(operation.method, makeApiUrl(operation.path, [])))
  const postDataA = Atom()
  const resultA = Atom('')
  const queryCollectorBus = Bacon.Bus()
  const postCollectorBus = Bacon.Bus()

  queryCollectorBus.onValue(v => {
    parametersA.set(R.map(x => x.get(), v))
  })

  postCollectorBus.onValue(v => postDataA.set(v))

  parametersA.changes().onValue(v => {
    curlValueA.set(curlCommand(operation.method, makeApiUrl(operation.path, v)))
  })

  const tryRequest = () => {
    loadingA.set(true)

    let options = {credentials: 'include', method: operation.method, headers: {'Content-Type': 'application/json'}}

    const pd = postDataA.get()
    if (pd !== undefined) {
      options.body = JSON.stringify(pd)
    }

    fetch(makeApiUrl(operation.path, parametersA.get()), options).then(response => {
      return response.text().then(function(text) {
        loadingA.set(false)
        if (response.status == 401) {
          resultA.set(<div>{response.status + ' ' + response.statusText + ' (Kirjaudu sisään esin: '}<a href="/koski" target="_new">{'Login'}</a>{')'}</div>)
        } else if (text) {
          resultA.set(<div>{response.status + ' ' + response.statusText}<Highlight className="json">{JSON.stringify(JSON.parse(text), null, 2)}</Highlight></div>)
        } else {
          resultA.set(<div>{response.status + ' ' + response.statusText}</div>)
        }
      }).catch(function(error) {
        console.error(error)
      })
    })
  }

  const tryRequestNewWindow = () => {
    window.open(makeApiUrl(operation.path, parametersA.get()))
  }

  return (
    <div className="api-tester">
      <div className="buttons">
        <button disabled={loadingA} className="try button blue" onClick={tryRequest}>{'Kokeile'}</button>
        {operation.method === 'GET' &&
          <button disabled={loadingA} className="try-newwindow button blue" onClick={tryRequestNewWindow}>{'Uuteen ikkunaan'}</button>
        }
        <button className="curl button" onClick={() => curlVisibleA.modify(v => !v)}>{curlVisibleA.map(v => v ? 'Piilota curl' : 'Näytä curl')}</button>
      </div>
      <div>{curlVisibleA.map(v => v ? <code ref={e => e && selectElementContents(e)} className="curlcmd" onClick={e => selectElementContents(e.target)}>{curlValueA}</code> : '')}</div>
      <div className="result">{resultA}</div>
      <ApiOperationTesterParameters operation={operation} queryCollectorBus={queryCollectorBus} postCollectorBus={postCollectorBus}/>
    </div>
  )
}

const ApiOperationStatusCodeRow = ({errorCategory}) => {
  const expandedA = Atom(false)

  return (
    <tr>
      <td>
        {errorCategory.statusCode}
      </td>
      <td>
        {errorCategory.statusCode != 200 ? errorCategory.key : ''}
      </td>
      <td>
        {errorCategory.message}
      </td>
      <td>
        <span className={expandedA.map(v => (v ? 'expanded' : '') + ' example-response')}>
          <a className="show-json" onClick={() => expandedA.modify(v => !v)}>{'Näytä JSON'}</a>
          <span className="json-popup">
            <a className="close" onClick={() => expandedA.set(false)}>{'Sulje'}</a>
            <Highlight className="json">{JSON.stringify(errorCategory.exampleResponse, null, 2)}</Highlight>
          </span>
        </span>
      </td>
    </tr>
  )
}
const ApiOperationStatusCodes = ({errorCategories}) => {
  return (
    <table>
      <thead>
      <tr>
        <th>{'HTTP-status'}</th>
        <th>{'Virhekoodi'}
          <small>{'(JSON-vastauksen sisällä)'}</small>
        </th>
        <th>{'Tilanne'}</th>
        <th>{'Esimerkkivastaus'}</th>
      </tr>
      </thead>
      <tbody>
      {R.map(ec => <ApiOperationStatusCodeRow errorCategory={ec}/>, errorCategories)}
      </tbody>
    </table>
  )
}

const ApiOperation = ({operation}) => {
  const expandedA = Atom(false)
  const statusCodesExpandedA = Atom(false)

  return (
    <div className={expandedA.map(v => (v ? 'expanded' : '') + ' api-operation')}>
      <h3 onClick={() => expandedA.modify(v => !v)}>
        <span className="api-method">{operation.method}</span>{operation.path}
      </h3>
      <div className="summary">{operation.summary}</div>
      <div className="api-details">
        <div dangerouslySetInnerHTML={{__html: operation.doc}}></div>
        <div className={statusCodesExpandedA.map(v => (v ? 'expanded' : '') + ' status-codes')}>
          <h4 onClick={() => statusCodesExpandedA.modify(v => !v)}><a>{'Vastaukset ja paluukoodit'}</a></h4>
          <ApiOperationStatusCodes errorCategories={operation.errorCategories}/>
        </div>
        <h4>{'Kokeile heti'}</h4>
        <ApiOperationTester operation={operation}/>
      </div>
    </div>
  )
}

const ApiOperations = ({operations}) => {
  return <div>{R.map(operation => <ApiOperation operation={operation}/>, operations)}</div>
}

const JsonExampleTable = ({contents}) => {
  return <table className="json" dangerouslySetInnerHTML={{__html: (contents)}}></table>
}

const JsonExample = ({category, example}) => {
  const expandedA = Atom(false)
  const contentsA = Atom('...')


  const contentsP = Http.cachedGet('/koski/api/documentation/categoryExamples/'+category+'/'+example.name+'/table.html')
  contentsP.onValue(v => {
    contentsA.set(v)
  })

  return (
    <li className={expandedA.map(v => (v ? 'expanded' : '') + ' example-item')}>
      <a className="example-link" onClick={() => expandedA.modify(v => !v)}>{example.description}</a>
      <a className="example-as-json" href={example.link} target="_blank">{'lataa JSON'}</a>
      {contentsA.map(c => <JsonExampleTable contents={c}/>)}
    </li>
  )
}

const DokumentaatioSivu = ({info}) => {
  const categories = info[0]
  const examples = info[1]
  const apiOperations = info[2]
  const htmlSections = info[3]

  // Sections having any content are considered loaded, so the loading message is hidden when page is useful
  const stillLoading = R.reduce(R.or, false, [categories, apiOperations].concat(htmlSections).map(c => c.length === 0))

  return (
    <div className='content content-area'>
      {stillLoading &&
        <section><h1>{'Ladataan...'}</h1></section>
      }

      <section dangerouslySetInnerHTML={{__html: htmlSections[0]}}></section>

      <section>
        <div dangerouslySetInnerHTML={{__html: htmlSections[1]}}></div>

        <ApiOperations operations={apiOperations}/>
      </section>

      <section>
        <div dangerouslySetInnerHTML={{__html: htmlSections[2]}}></div>

        {R.map(c => (
          <div>
            <h1>{c}</h1>
            <ul className="example-list">
            {
              R.map(e => <JsonExample category={c} example={e}/>, examples[c])
            }
            </ul>
          </div>
        ), categories)}
      </section>

    </div>
  )
}

export const dokumentaatioContentP = () => {
  const infoP = Bacon.zipAsArray(
    Http.cachedGet('/koski/api/documentation/categoryNames.json').startWith([]),
    Http.cachedGet('/koski/api/documentation/categoryExampleMetadata.json').startWith({}),
    Http.cachedGet('/koski/api/documentation/apiOperations.json').startWith([]),
    Http.cachedGet('/koski/api/documentation/sections.html').startWith(['', '', ''])
  )

  return ({
    content: <DokumentaatioSivu baret-lift info={infoP}/>,
    title: 'Dokumentaatio'
  })
}
