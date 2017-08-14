import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Http from '../http'
import '../../node_modules/codemirror/mode/javascript/javascript.js'
import {contentWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {ApiOperations} from './DokumentaatioApiTester.jsx'

const JsonExampleTable = ({contents}) => {
  return <table className="json" dangerouslySetInnerHTML={{__html: (contents)}}></table>
}

const JsonExample = ({category, example}) => {
  const expandedA = Atom(false)

  return (
    <li className="example-item">
      <a className="example-link" onClick={() => expandedA.modify(v => !v)}>{example.description}</a>
      <a className="example-as-json" href={example.link} target="_blank">{'lataa JSON'}</a>
      {expandedA.flatMap(v => v
        ? Http.cachedGet('/koski/api/documentation/categoryExamples/'+category+'/'+example.name+'/table.html').map(c => <JsonExampleTable contents={c}/>)
        : null
      )}
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
          <div key={c}>
            <h1>{c}</h1>
            <ul className="example-list">
            {
              R.addIndex(R.map)((e, idx) => <JsonExample key={idx} category={c} example={e}/>, examples[c])
            }
            </ul>
          </div>
        ), categories)}
      </section>

    </div>
  )
}

export const dokumentaatioContentP = () => {
  const infoP = Bacon.combineAsArray(
    Http.cachedGet('/koski/api/documentation/categoryNames.json'),
    Http.cachedGet('/koski/api/documentation/categoryExampleMetadata.json'),
    Http.cachedGet('/koski/api/documentation/apiOperations.json'),
    Http.cachedGet('/koski/api/documentation/sections.html')
  )

  return contentWithLoadingIndicator(infoP.map(info => ({
    content: <DokumentaatioSivu info={info}/>,
    title: 'Dokumentaatio'
  })))
}
