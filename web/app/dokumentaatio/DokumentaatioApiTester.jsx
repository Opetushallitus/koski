import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Highlight from 'react-highlight'
import CodeMirror from '@skidding/react-codemirror'
import Dropdown from '../Dropdown.jsx'

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
  const codeA = Atom(JSON.stringify(operation.examples[0].data, null, 2))

  selectedValueA.onValue(v => {
    codeA.set(JSON.stringify(v.data, null, 2))
  })

  codeA.onValue(code => collectorBus.push(code))

  return (
    <div className="postdata">
      <h4>{'Syötedata'}</h4>
      <div className="examples">
        <label>{'Esimerkkejä'}
          <Dropdown options={operation.examples} keyValue={v => v.name} displayValue={v => v.name} selected={selectedValueA} onSelectionChanged={v => selectedValueA.set(v)}/>
        </label>
      </div>
      <CodeMirror baret-lift value={codeA} onChange={c => codeA.set(c)} options={{mode: {name: 'javascript', json: true}}}/>
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
      options.body = pd
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
      <ApiOperationTesterParameters operation={operation} queryCollectorBus={queryCollectorBus} postCollectorBus={postCollectorBus}/>
      <div className="buttons">
        <button disabled={loadingA} className="try button blue" onClick={tryRequest}>{'Kokeile'}</button>
        {operation.method === 'GET' &&
        <button disabled={loadingA} className="try-newwindow button blue" onClick={tryRequestNewWindow}>{'Uuteen ikkunaan'}</button>
        }
        <button className="curl button" onClick={() => curlVisibleA.modify(v => !v)}>{curlVisibleA.map(v => v ? 'Piilota curl' : 'Näytä curl')}</button>
      </div>
      <div>{curlVisibleA.map(v => v ? <code ref={e => e && selectElementContents(e)} className="curlcmd" onClick={e => selectElementContents(e.target)}>{curlValueA}</code> : null)}</div>
      <div className="result">{resultA}</div>
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
    <div className="api-operation">
      <h3 onClick={() => expandedA.modify(v => !v)}>
        <span className="api-method">{operation.method}</span>{operation.path}
      </h3>
      <div className="summary">{operation.summary}</div>
      {expandedA.map(exp => exp ? (
        <div className="api-details">
          <div dangerouslySetInnerHTML={{__html: operation.doc}}></div>
          <div className={statusCodesExpandedA.map(v => (v ? 'expanded' : '') + ' status-codes')}>
            <h4 onClick={() => statusCodesExpandedA.modify(v => !v)}><a>{'Vastaukset ja paluukoodit'}</a></h4>
            <ApiOperationStatusCodes errorCategories={operation.errorCategories}/>
          </div>
          <h4>{'Kokeile heti'}</h4>
          <ApiOperationTester operation={operation}/>
        </div>
      ) : null)}
    </div>
  )
}

export const ApiOperations = ({operations}) => {
  return <div>{R.map(operation => <ApiOperation operation={operation}/>, operations)}</div>
}
