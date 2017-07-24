import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Highlight from 'react-highlight'
import Http from './http'
import Dropdown from './Dropdown.jsx'
import { contentWithLoadingIndicator } from './AjaxLoadingIndicator.jsx'


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
  const curlValueA = Atom('')
  const postDataA = Atom()
  const resultA = Atom('')
  const queryCollectorBus = Bacon.Bus()
  const postCollectorBus = Bacon.Bus()

  const tryRequest = () => {
    loadingA.set(true)

    let options = {credentials: 'include', method: operation.method, headers: {'Content-Type': 'application/json'}}

    const pd = postDataA.get()
    console.log('pd', pd)
    if (pd !== undefined) {
      options.body = JSON.stringify(pd)
    }

    fetch(makeApiUrl(operation.path, parametersA.get()), options).then(response => {
      return response.text().then(function(text) {
        loadingA.set(false)
        if (response.status == 401) {
          resultA.set(<div>{response.status + ' ' + response.statusText + ' '}<a href="/koski" target="_new">{'Login'}</a></div>)
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

  queryCollectorBus.onValue(v => {
    parametersA.set(R.map(x => x.get(), v))
  })

  postCollectorBus.onValue(v => postDataA.set(v))

  parametersA.changes().onValue(v => {
    console.log('v', v)
    curlValueA.set(curlCommand(operation.method, makeApiUrl(operation.path, v)))
  })

  return (
    <div className="api-tester">
      <div className="buttons">
        <button disabled={loadingA} className="try button blue" onClick={tryRequest}>{'Kokeile'}</button>
        <button disabled={loadingA} className="try-newwindow button blue">{'Uuteen ikkunaan'}</button>
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

  return (
    <div className='content content-area'>
      <section>
        <h1>{'Koski-tiedonsiirtoprotokolla'}</h1>
        <p>{'Tässä dokumentissa kuvataan Koski-järjestelmän tiedonsiirrossa käytettävä protokolla. Lisätietoja Koski-järjestelmästä löydät '}<a href="https://confluence.csc.fi/display/OPHPALV/Koski">{'Opetushallituksen wiki-sivustolta'}</a>{'. Järjestelmän lähdekoodit ja kehitysdokumentaatio '}<a href="https://github.com/Opetushallitus/koski">{'Githubissa'}</a>{'.'}</p>
        <p>{'Protokolla, kuten Koski-järjestelmäkin, on työn alla, joten kaikki voi vielä muuttua.'}</p>
        <p>{'Muutama perusasia tullee kuitenkin säilymään:'}</p>

        <ul>
          <li>{'Rajapinnan avulla järjestelmään voi tallentaa tietoja oppijoiden opinto-oikeuksista, opintosuorituksista ja läsnäolosta oppilaitoksissa'}</li>
          <li>{'Rajapinnan avulla tietoja voi myös hakea ja muokata'}</li>
          <li>{'Rajapinnan käyttö vaatii autentikoinnin ja pääsy tietoihin rajataan käyttöoikeusryhmillä. Näin ollen esimerkiksi oikeus oppilaan tietyssä oppilaitoksessa suorittamien opintojen päivittämiseen voidaan antaa kyseisen oppilaitoksen henkilöstölle'}</li>
          <li>{'Rajapinta mahdollistaa myös automaattiset tiedonsiirrot tietojärjstelmien välillä. Näin esimerkiksi tietyt viranomaiset voivat saada tietoja Koskesta. Samoin oppilaitoksen tietojärjestelmät voivat päivittää tietoja Koskeen.'}</li>
          <li>{'Järjestelmä tarjoaa REST-tyyppisen tiedonsiirtorajapinnan, jossa dataformaattina on JSON'}</li>
          <li>{'Samaa tiedonsiirtoprotokollaa ja dataformaattia pyritään soveltuvilta osin käyttämään sekä käyttöliittymille, jotka näyttävät tietoa loppukäyttäjille, että järjestelmien väliseen kommunikaatioon'}</li>
        </ul>

        <h2>{'JSON-dataformaatti'}</h2>

        <p>{'Käytettävästä JSON-formaatista on laadittu työversio, jonka toivotaan vastaavan ammatillisen koulutuksen tarpeisiin. Tällä formaatilla siis tulisi voida siirtää tietoja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä Koskeen ja eteenpäin tietoja tarvitsevien viramomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin. Formaattia on tarkoitus laajentaa soveltumaan myös muiden koulutustyyppien tarpeisiin, mutta näitä tarpeita ei ole vielä riittävällä tasolla kartoitettu, jotta konkreettista dataformaattia voitaisiin suunnitella. Yksi formaatin suunnittelukriteereistä on toki ollut sovellettavuus muihinkin koulutustyyppeihin.'}</p>

        <h3>{'JSON Schema'}</h3>

        <p>{'Käytettävä JSON-dataformaatti on kuvattu '}<a href="http://json-schema.org/">{'JSON-schemalla'}</a>{', jota vasten siirretyt tiedot voidaan myös automaattisesti validoida.'}</p>

        <div className="preview-image-links">
          <a href="${schemaViewerUrl}">
            <div className="img-wrapper">
              <img src="/koski/images/koski-schema-preview.png"/>
            </div>
            <div className="caption">{'Visualisoitu JSON-schema'}</div>
            <p>{'Voi tarkastella schemaa visualisointityökalun avulla. Tällä työkalulla voi myös validoida JSON-viestejä schemaa vasten. Klikkaamalla kenttiä saat näkyviin niiden tarkemmat kuvaukset.'}</p>
          </a>
          <a href="${schemaDocumentUrl} FIXME">
            <div className="img-wrapper">
              <img src="/koski/images/koski-schema-html-preview.png"/>
            </div>
            <div className="caption">{'Printattava dokumentti'}</div>
            <p>{'Printattava versio schemasta'}</p>
          </a>
          <a href="${schemaFileUrl} FIXME">
            <div className="img-wrapper">
              <img src="/koski/images/koski-schema-json-preview.png"/>
            </div>
            <div className="caption">{'Lataa JSON-tiedostona'}</div>
            <p>{'Voit myös ladata scheman tiedostona'}</p>
          </a>
        </div>

        <p>{'Tietokentät, joissa validit arvot on lueteltavissa, on kooditettu käyttäen hyväksi Opintopolku-järjestelmään kuuluvaa '}<a href="https://github.com/Opetushallitus/koodisto">{'Koodistopalvelua'}</a>{'. Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen '}<a href="/koski/documentation/koodisto/koulutus/latest">{'koulutuskoodi'}</a>{'.'}</p>
        <p>{'Scalaa osaaville ehkä nopein tapa tutkia tietomallia on kuitenkin sen lähdekoodi. Githubista löytyy sekä '}<a href="https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/schema/Oppija.scala">{'scheman'}</a>{', että '}<a href="https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/documentation/Examples.scala">{'esimerkkien'}</a>{' lähdekoodit.'}</p>
      </section>

      <section>
        <h2>{'REST-rajapinnat'}</h2>

        <p>{'Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.'}</p>
        <p>{'Rajapinnat on lueteltu ja kuvattu alla. Voit myös testata rajapintojen toimintaa tällä sivulla, kunhan käyt ensin '}<a href="/koski">{'kirjautumassa sisään'}</a>{' järjestelmään. Saat tarvittavat tunnukset Koski-kehitystiimiltä pyydettäessä.'}</p>
        <p>{'Rajapintojen käyttämät virhekoodit on myös kuvattu alla. Virhetapauksissa rajapinnat käyttävät alla kuvattuja HTTP-statuskoodeja ja sisällyttävät tarkemmat virhekoodit ja selitteineen JSON-tyyppiseen paluuviestiin. Samaan virhevastaukseen voi liittyä useampi virhekoodi/selite.'}</p>

        <ApiOperations operations={apiOperations}/>
      </section>

      <section>
        <h2>{'Esimerkkidata annotoituna'}</h2>
        <p>{"Toinen hyvä tapa tutustua tiedonsiirtoprotokollaan on tutkia esimerkkiviestejä. Alla joukko viestejä, joissa oppijan opinnot ovat eri vaiheissa. Kussakin esimerkissa on varsinaisen JSON-sisällön lisäksi schemaan pohjautuva annotointi ja linkitykset koodistoon ja OKSA-sanastoon."}</p>

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

export const dokumentaatioContentP = () => contentWithLoadingIndicator(infoP).map(info => {
  return ({
    content: <DokumentaatioSivu info={info}/>,
    title: 'Dokumentaatio'
  })
})


const infoP = Bacon.zipAsArray(
  Http.cachedGet('/koski/api/documentation/categoryNames.json').startWith([]),
  Http.cachedGet('/koski/api/documentation/categoryExampleMetadata.json').startWith({}),
  Http.cachedGet('/koski/api/documentation/apiOperations.json').startWith([])
)

//<script src='/koski/js/codemirror/codemirror.js'></script>
//<script src='/koski/js/codemirror/javascript.js'></script>
