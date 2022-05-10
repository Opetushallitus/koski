package fi.oph.koski.jsonschemaviewer

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.servlet.VirkailijaHtmlServlet

class JsonSchemaViewerHtmlServlet(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  override val unsafeAllowBaseUri: Boolean = true
  override val unsafeAllowInlineStyles: Boolean = true

  val code = scala.xml.Unparsed("""
    (function($) {
      $('body').one('pagecontainershow', function(event, ui) {
      var schema = '/koski/api/documentation/' + schemaName()
      var path = window.location.hash.match(/v=([^\&]+)/);
      var selectedNode = path ? path[1] : null

      JSV.init({
        schema: schema,
        maxDepth: 15
      }, function() {
        JSV.setVersion(tv4.getSchema(JSV.treeData.schema).version);
        if (selectedNode) {
          var node = JSV.expandNodePath(selectedNode.split('-'));
          JSV.flashNode(node);
          JSV.clickTitle(node);
        } else {
          JSV.resetViewer();
        }
      });
    });
    })(jQuery);

    function schemaName() {
      var nameInParams = window.location.search.match(new RegExp('(?:[?&]schema=)([^&]+)'))
      var schemaName
      if (nameInParams) {
        schemaName = nameInParams[1]
      }
      return schemaName || 'koski-oppija-schema.json'
    }
    """)

  get("/")(nonce => {

    <html>
      <head>
        <style nonce={nonce}>
          #sharelink {{
            min-width: 300 px;
          }}
        </style>
        <meta charset="utf-8"/>
        <title>JSON Schema Viewer 0.3.4</title>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <link rel="icon" type="image/gif" href="data:image/gif;base64,R0lGODlhEAAQAIAAAAAAAAAAACH5BAkAAAEALAAAAAAQABAAAAIgjI+py+0PEQiT1lkNpppnz4HfdoEH2W1nCJRfBMfyfBQAOw==" />
        <link nonce={nonce} rel="stylesheet" href="/koski/json-schema-viewer/jquery/1.4.5/jquery.mobile.min.css" />
        <link nonce={nonce} rel="stylesheet" href="/koski/json-schema-viewer/styles/json-schema-viewer.min.css" />
      </head>
      <body>
        <div id="loading">
          <div id="preload"><img src="/koski/json-schema-viewer/images/loader.gif" alt="Loading..."/>
            <br />
            Loading...
          </div>
        </div>
        <div class="ui-responsive-panel" data-role="page" id="viewer-page"  data-title="JSON Schema Viewer">
          <div data-role="header" class="md-navbar">
            <div data-role="controlgroup" data-type="horizontal" class="ui-btn-left">
              <a class="ui-btn ui-corner-all ui-icon-bars ui-btn-icon-notext" href="#info-panel" title="Toggle Info">Menu</a>
              <a class="ui-btn md-flex-btn ui-corner-all ui-icon-check ui-btn-icon-left" href="#validator-page" title="Go to Validator">Validator</a>
            </div>
            <h1 id="viewer-title">JSON Schema Viewer<span class="schema-version"></span></h1>
          </div><!-- /header -->
          <div  role="main" class="ui-content" id="main-body">
            <div id="zoom-controls">
              <a id="zoom_in" href="#" class="ui-btn ui-icon-plus ui-btn-icon-notext ui-corner-all" title="Zoom In">Zoom-in</a>
              <a id="zoom_out" href="#" class="ui-btn ui-icon-minus ui-btn-icon-notext ui-corner-all" title="Zoom Out">Zoom-out</a>
              <div id="tree-controls">
                <a id="reset-tree" href="#" class="ui-btn ui-icon-refresh ui-btn-icon-notext ui-corner-all" title="Reset">Reset</a>
              </div>
            </div>
            <div id="legend-container">
              <div id="legend" data-role="collapsible" data-collapsed="false">
                <h3 class="ui-mini">Legend</h3>
                <div id="legend-items"></div>
              </div>
              <input type="text" data-type="search" placeholder="Search..." id="search-input"/>
              <ul id="search-result" data-role="listview" data-filter="true" data-filter-reveal="true" data-input="#search-input" data-inset="true"></ul>
            </div>
          </div>
          <div data-role="footer">
            <div id="schema-path" class="ui-content">
              <a href="#permalink-popup" id="permalink" data-rel="popup">Select a Node</a>
              <div data-role="popup" id="permalink-popup" class="ui-content" data-arrow="true">
                <label for="sharelink">Share:</label>
                <input type="text" id="sharelink" placeholder="Select a Node to create a permalink." value="" data-theme="a"/>
              </div>
            </div>
          </div><!-- /footer -->
          <div data-display="push" data-role="panel" id="info-panel" data-swipe-close={"false"}>
            <div data-role="header" data-theme="b" class="ui-mini">
              <h1 id="info-title">Info</h1>
            </div>
            <div data-role="tabs" id="info-tabs">
              <div data-role="navbar" id="info-tabs-navbar">
                <ul>
                  <li>
                    <a data-ajax="false" href="#info-tab-def">Definition</a>
                  </li>
                  <li>
                    <a data-ajax="false" href="#info-tab-example">Example</a>
                  </li>
                  <li>
                    <a data-ajax="false" href="#info-tab-schema">Schema</a>
                  </li>
                </ul>
              </div>
              <div class="ui-body-d ui-content info-tab" id="info-tab-def">
                <h4>Type:</h4>
                <p id="info-type">
                  Select a node to view the type.
                </p>
                <hr />
                <h4>Definition:</h4>
                <p id="info-definition">
                  Select a node to view the definition.
                </p>
                <hr />
                <h4>Translation:</h4>
                <p id="info-translation">
                  Select a node to view the translation.
                </p>
              </div>
              <div class="ui-body-d ui-content info-tab"  id="info-tab-example">
                <p>
                  Select a node to view the example.
                </p>
              </div>
              <div class="ui-body-d ui-content info-tab"  id="info-tab-schema">
                <p>
                  Select a node to view the JSON schema.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div data-role="page" id="validator-page"  data-title="JSON Validator">
          <div data-role="header" class="md-navbar">
            <h1>mdJSON Validator <span class="schema-version"></span></h1>
            <div data-role="controlgroup" data-type="horizontal" class="ui-btn-left">
              <a class="ui-btn md-flex-btn ui-icon-eye ui-btn-icon-left ui-corner-all" href="#viewer-page"  title="Go to Viewer">Viewer</a>
            </div>
            <a data-rel="popup" data-role="none" data-position-to="window" class="ui-btn-right main-logo" href="#popup-welcome"  title="Show Info"></a>
          </div>
          <div role="main" class="ui-content">
            <div class="ui-grid-a ui-responsive">
              <div class="ui-block-a section">
                <label for="textarea-json">JSON to Validate:</label>
                <textarea cols="40" rows="15" name="textarea-json" id="textarea-json" placeholder="Enter your JSON or Drag-n-Drop a file here..."></textarea>
                <a class="load-example" href="schemas/examples/minimum_example.json" data-target="textarea-json" data-prefetch="true">Load Example</a>
              </div>
              <div class="ui-block-b section">
                <label for="file-upload">Upload File:</label>
                <input id="file-upload" type="file" name="file" data-role=""/>
                <fieldset data-role="controlgroup">
                  <legend>
                    Options:
                  </legend>
                  <input type="checkbox" name="checkbox-strict" id="checkbox-strict"/>
                  <label for="checkbox-strict">Strict</label>
                  <input type="checkbox" name="checkbox-stop" id="checkbox-stop"/>
                  <label for="checkbox-stop">Stop at First Error</label>
                </fieldset>
              </div>
            </div>
            <button id="button-validate" class="ui-btn ui-icon-check ui-btn-icon-bottom">
              Validate!
            </button>
            <div id="validation-results" class="ui-responsive"></div>
          </div>
        </div>

        <div id="popup-error" data-history="false" data-theme="a" data-overlay-theme="b">
          <div data-role="header" data-theme="a">
            <h1>Error!</h1>
          </div>
          <p class="ui-content error-message">
            If you see this, something went wrong.
          </p>
        </div>

        <script nonce={nonce} type='text/javascript' src="/koski/json-schema-viewer/jquery/1.11.3/jquery.min.js"></script>
        <script nonce={nonce} type='text/javascript' src="/koski/json-schema-viewer/jquery/1.4.5/jquery.mobile.min.js"></script>
        <script nonce={nonce} type='text/javascript' src="/koski/json-schema-viewer/js/json-schema-viewer.min.js"></script>
        <script nonce={nonce} type='text/javascript'>{code}</script>
      </body>
    </html>
  })
}
