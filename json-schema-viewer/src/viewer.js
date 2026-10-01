/*! JSON Schema Viewer v0.3.4 | https://github.com/jlblcc/json-schema-viewer */
const $ = require('jquery')
const URI = require('urijs')
const d3 = require('d3')
const jsonpointer = require('jsonpointer.js')
const hljs = require('./highlight')
const tv4 = require('./tv4-async-load')
require('jquery.scrollto')
require('../vendor/filereader')

if (!window.location.origin) {
  window.location.origin =
    window.location.protocol +
    '//' +
    window.location.hostname +
    (window.location.port ? ':' + window.location.port : '')
}

if (typeof window.JSV === 'undefined') {
  var JSV = {
    schema: '',
    plain: false,
    version: '',
    focusNode: false,
    example: false,
    treeData: null,
    viewerInit: false,
    viewerHeight: 0,
    viewerWidth: 0,
    duration: 750,
    counter: 0,
    maxLabelLength: {},
    maxDepth: 20,
    labels: {
      allOf: true,
      anyOf: true,
      oneOf: true,
      'object{ }': true
    },
    baseSvg: null,
    svgGroup: null,
    init: function (config, callback) {
      var i
      for (i in config) {
        if (JSV.hasOwnProperty(i)) {
          JSV[i] = config[i]
        }
      }
      if (JSV.plain) {
        JSV.createDiagram(callback)
        d3.selectAll('#zoom-controls>a').on('click', JSV.zoomClick)
        d3.select('#tree-controls>a#reset-tree').on('click', JSV.resetViewer)
        JSV.viewerInit = true
        return
      }
      JSV.contentHeight()
      JSV.resizeViewer()
      $(document).on('pagecontainertransition', this.contentHeight)
      $(window).on('throttledresize orientationchange', this.contentHeight)
      $(window).on('resize', this.contentHeight)
      JSV.resizeBtn()
      $(document).on('pagecontainershow', JSV.resizeBtn)
      $(window).on('throttledresize', JSV.resizeBtn)
      var cb = function () {
        callback()
        var items = []
        JSV.visit(
          JSV.treeData,
          function (me) {
            if (me.isReal) {
              items.push(me.plainName + '|' + JSV.getNodePath(me).join('-'))
            }
          },
          function (me) {
            return me.children || me._children
          }
        )
        items.sort()
        JSV.buildSearchList(items, true)
        $('#loading').fadeOut('slow')
      }
      JSV.createDiagram(cb)
      $(window).on('hashchange', function () {
        if (JSV.treeData && /^#viewer-page/.test(window.location.hash)) {
          JSV.resetDeepLinks()
          JSV.applyDeepLinks(window.location.hash)
        }
      })
      JSV.initValidator()
      $('#popup-error').enhanceWithin().popup()
      $.fn.highlight = function (str, className, quote) {
        var string = quote ? '\\"\\b' + str + '\\b\\"' : '\\b' + str + '\\b',
          regex = new RegExp(string, 'g')
        return this.each(function () {
          this.innerHTML = this.innerHTML.replace(regex, function (matched) {
            return '<span class="' + className + '">' + matched + '</span>'
          })
        })
      }
      $('body').on('pagecontainershow', function (event, ui) {
        var page = ui.toPage
        if (page.attr('id') === 'viewer-page' && JSV.viewerInit) {
          if (page.jqmData('infoOpen')) {
            $('#info-panel').panel('open')
          }
          JSV.contentHeight()
          if ($('svg#jsv-tree').height() === 0) {
            $('svg#jsv-tree')
              .attr('width', $('#main-body').width())
              .attr('height', $('#main-body').height())
            JSV.resizeViewer()
            JSV.resetViewer()
          }
        }
      })
      $('body').on('pagecontainerbeforehide', function (event, ui) {
        var page = ui.prevPage
        if (page.attr('id') === 'viewer-page') {
          page.jqmData(
            'infoOpen',
            !!page.find('#info-panel.ui-panel-open').length
          )
        }
      })
      $('#info-panel').on('panelopen', function () {
        var focus = JSV.focusNode
        JSV.resizeViewer()
        if (focus) {
          d3.select('#n-' + focus.id).classed('focus', true)
          JSV.setPermalink(focus)
        }
      })
      $('#info-panel').on('panelclose', function () {
        var focus = JSV.focusNode
        JSV.resizeViewer()
        if (focus) {
          d3.select('#n-' + focus.id).classed('focus', false)
          $('#permalink').html('Select a Node...')
          $('#sharelink').val('')
        }
      })
      $('#info-panel').on('tabsactivate', function (event, ui) {
        var id = ui.newPanel.attr('id')
        if (id === 'info-tab-example' || id === 'info-tab-schema') {
          var pre = ui.newPanel.find('pre'),
            highEl = pre.find('span.highlight')[0]
          if (highEl) {
            pre.scrollTo(highEl, 900)
          }
        }
      })
      $('.load-example').each(function (idx, link) {
        var ljq = $(link)
        ljq.on('click', function (evt) {
          evt.preventDefault()
          JSV.loadInputExample(link.href, ljq.data('target'))
        })
      })
      d3.selectAll('#zoom-controls>a').on('click', JSV.zoomClick)
      d3.select('#tree-controls>a#reset-tree').on('click', JSV.resetViewer)
      $('#sharelink').on('click', function () {
        $(this).select()
      })
      $(document).on('keydown', JSV.keyNav)
      $(window).on('throttledresize', JSV.pinPanel)
      JSV.viewerInit = true
    },
    contentHeight: function () {
      var screen = $.mobile.getScreenHeight(),
        header = $('.ui-header').hasClass('ui-header-fixed')
          ? $('.ui-header').outerHeight() - 1
          : $('.ui-header').outerHeight(),
        footer = $('.ui-footer').hasClass('ui-footer-fixed')
          ? $('.ui-footer').outerHeight() - 1
          : $('.ui-footer').outerHeight(),
        contentCurrent =
          $('#main-body.ui-content').outerHeight() -
          $('#main-body.ui-content').height(),
        content = screen - header - footer - contentCurrent
      $('#main-body.ui-content').css('min-height', content + 'px')
    },
    resizeBtn: function (minSize) {
      var bp = typeof minSize === 'number' ? minSize : 800
      var activePage = $.mobile.pageContainer.pagecontainer('getActivePage')
      if ($('.md-navbar', activePage).width() <= bp) {
        $('.md-navbar .md-flex-btn.ui-btn-icon-left').toggleClass(
          'ui-btn-icon-notext ui-btn-icon-left'
        )
      } else {
        $('.md-navbar .md-flex-btn.ui-btn-icon-notext').toggleClass(
          'ui-btn-icon-left ui-btn-icon-notext'
        )
      }
    },
    setVersion: function (version) {
      JSV.version = version
      $('.schema-version').text(version)
    },
    showError: function (msg) {
      $('#popup-error .error-message').html(msg)
      $('#popup-error').popup('open')
    },
    initValidator: function () {
      var opts = {
        readAsDefault: 'Text',
        on: {
          load: function (e, file) {
            var data = e.currentTarget.result
            try {
              $.parseJSON(data)
              $('#textarea-json').val(data)
            } catch (err) {
              JSV.showError(
                'Failed to load ' +
                  file.name +
                  '. The file is not valid JSON. <br/>The error: <i>' +
                  err +
                  '</i>'
              )
            }
          },
          error: function (e, file) {
            var msg =
              'Failed to load ' +
              file.name +
              '. ' +
              e.currentTarget.error.message
            JSV.showError(msg)
          }
        }
      }
      $('#file-upload, #textarea-json').fileReaderJS(opts)
      $('body').fileClipboard(opts)
      $('#button-validate').click(function () {
        var result = JSV.validate()
        if (result) {
          JSV.showValResult(result)
        }
      })
    },
    validate: function () {
      var data
      try {
        data = $.parseJSON($('#textarea-json').val())
      } catch (e) {
        JSV.showError('Unable to parse JSON: <br/>' + e)
      }
      if (data) {
        var stop = $('#checkbox-stop').is(':checked'),
          strict = $('#checkbox-strict').is(':checked'),
          schema = tv4.getSchemaMap()[JSV.schema],
          result
        if (stop) {
          var r = tv4.validate(data, schema, false, strict)
          result = {
            valid: r,
            errors: !r ? [tv4.error] : []
          }
        } else {
          result = tv4.validateMultiple(data, schema, false, strict)
        }
        return result
      }
    },
    showValResult: function (result) {
      var cont = $('#validation-results'),
        ui
      if (cont.children().length) {
        cont.css('opacity', 0)
      }
      if (result.valid) {
        cont.html('<p class=ui-content>JSON is valid!</p>')
      } else {
        ui = cont.html('<div class=ui-content>JSON is <b>NOT</b> valid!</div>')
        $.each(result.errors, function (i, err) {
          var me = JSV.buildValError(err, 'Error ' + (i + 1) + ': ')
          if (err.subErrors) {
            $.each(err.subErrors, function (i, sub) {
              me.append(JSV.buildValError(sub, 'SubError ' + (i + 1) + ': '))
            })
          }
          ui.children('.ui-content').first().append(me).enhanceWithin()
        })
      }
      cont.toggleClass('error', !result.valid)
      $('#validator-page').animate(
        {
          scrollTop: $('#validation-results').offset().top + 20
        },
        1e3
      )
      cont.fadeTo(350, 1)
    },
    buildValError: function (err, title) {
      var main =
        '<div data-role="collapsible" data-collapsed="true" data-mini="true">' +
        '<h4>' +
        (title || 'Error: ') +
        err.message +
        '</h4>' +
        '<ul><li>Message: ' +
        err.message +
        '</li>' +
        '<li>Data Path: ' +
        err.dataPath +
        '</li>' +
        '<li>Schema Path: ' +
        err.schemaPath +
        '</li></ul></div>'
      return $(main)
    },
    setInfo: function (node) {
      var schema = $('#info-tab-schema')
      var def = $('#info-tab-def')
      var ex = $('#info-tab-example')
      var height =
        $('#info-panel').innerHeight() -
        $('#info-panel .ui-panel-inner').outerHeight() +
        $('#info-panel #info-tabs').height() -
        $('#info-panel #info-tabs-navbar').height() -
        (schema.outerHeight(true) - schema.height())
      $.each([schema, def, ex], function (i, e) {
        e.height(height)
      })
      // Technical: definition-list table from structured schema fields
      var mono = function (text) {
        return $('<span class="jsv-mono"></span>').text(text)
      }
      var chip = function (text) {
        return $('<span class="jsv-chip"></span>').text(text)
      }
      var chips = function (values) {
        var el = $('<div class="jsv-chips"></div>')
        $.each(values, function (i, v) {
          el.append(chip(v))
        })
        return el
      }
      var techTable = $('#info-technical').empty()
      var addRow = function (label, valueEl) {
        techTable.append(
          $('<div class="jsv-tech-row"></div>')
            .append($('<dt class="jsv-tech-label"></dt>').text(label))
            .append($('<dd class="jsv-tech-value"></dd>').append(valueEl))
        )
      }

      addRow('Type', mono(node.displayType.toString()))
      if (node.type === 'array') {
        addRow(
          'Cardinality',
          mono(
            (node.minItems || 0) +
              '..' +
              (node.maxItems != null ? node.maxItems : '*')
          )
        )
      } else {
        addRow('Cardinality', mono(node.require ? '1..1' : '0..1'))
      }
      if (node['default'] != null) {
        addRow('Default', mono(node['default']))
      }
      if (node.minimum != null) {
        addRow(
          'Minimum',
          mono(node.minimum + (node.exclusiveMinimum ? ' (exclusive)' : ''))
        )
      }
      if (node.maximum != null) {
        addRow(
          'Maximum',
          mono(node.maximum + (node.exclusiveMaximum ? ' (exclusive)' : ''))
        )
      }
      if (node.pattern) {
        addRow('Format', mono(node.pattern))
      }
      if (node.unit) {
        addRow('Format', mono(node.unit))
      }
      if (node.koodisto) {
        var koodistoLink = $(
          '<a class="jsv-mono jsv-koodisto-link" target="_blank"></a>'
        )
          .attr(
            'href',
            '/koski/dokumentaatio/koodisto/' + node.koodisto + '/latest'
          )
          .text(node.koodisto)
        addRow('Koodisto', koodistoLink)
      }
      if (node.oksa) {
        var oksaLink = $(
          '<a class="jsv-mono jsv-oksa-link" target="_blank"></a>'
        )
          .attr('href', node.oksa.url)
          .text(node.oksa['käsite'] || node.oksa.url)
        addRow('Oksa', oksaLink)
      }
      var allowedValues = (node.enumValues || []).concat(node.koodiarvot || [])
      if (allowedValues.length) {
        addRow('Allowed', chips(allowedValues))
      }
      if (node.synthetic) {
        addRow(
          'Computed',
          $('<span class="jsv-plain"></span>').text(
            'Derived value, not set on input'
          )
        )
      }
      if (node.readOnly) {
        addRow(
          'Read-only',
          $('<span class="jsv-plain"></span>').text(
            node.readOnlyText || 'Vain luettava kenttä.'
          )
        )
      }
      if (node.conditions && node.conditions.length) {
        var condEl = $('<div class="jsv-plain"></div>')
        $.each(node.conditions, function (i, c) {
          condEl.append($('<div></div>').text(c))
        })
        addRow('Condition', condEl)
      }
      if (node.acceptsSingleValue) {
        addRow(
          'Input',
          $('<span class="jsv-plain"></span>').text(
            'Accepts also a single value'
          )
        )
      }
      var annotationTokens = []
      if (node.sensitive) {
        annotationTokens.push('@SensitiveData')
      }
      if (node.redundantData) {
        annotationTokens.push('@RedundantData')
      }
      if (node.deprecated) {
        annotationTokens.push('@Deprecated')
      }
      if (annotationTokens.length) {
        addRow('Annotation', chips(annotationTokens))
      }

      // Behaviour badges: annotations that change how the field behaves
      var esc = function (s) {
        return $('<div></div>')
          .text(s || '')
          .html()
      }
      var badges = $('#info-badges').empty()
      var lockIcon =
        '<svg class="jsv-badge-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#2a2a2a" stroke-width="2"><rect x="4" y="10.5" width="16" height="10.5" rx="2"></rect><path d="M8 10.5V7a4 4 0 0 1 8 0v3.5"></path></svg>'
      var slashIcon =
        '<svg class="jsv-badge-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#2a2a2a" stroke-width="2"><circle cx="12" cy="12" r="9"></circle><path d="M5.6 5.6l12.8 12.8"></path></svg>'
      var clockIcon =
        '<svg class="jsv-badge-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#2a2a2a" stroke-width="2"><circle cx="12" cy="12" r="9"></circle><path d="M12 7v5l3 2"></path></svg>'
      var addBadge = function (iconHtml, textHtml) {
        badges.append(
          $('<div class="jsv-badge"></div>').html(
            iconHtml + '<span class="jsv-badge-text">' + textHtml + '</span>'
          )
        )
      }
      if (node.sensitive) {
        addBadge(
          lockIcon,
          '<strong>Erityinen henkilötieto + salassa pidettävä tieto</strong>'
        )
      }
      if (node.redundantData) {
        addBadge(
          slashIcon,
          '<strong>Kenttä ei ole käytössä.</strong> Koski ei ota vastaan kentässä siirrettyä tietoa.'
        )
      }
      if (node.deprecated) {
        addBadge(
          clockIcon,
          '<strong>Vanhentunut kenttä.</strong> ' +
            esc(node.deprecatedText || node.deprecatedMessage)
        )
      }

      // === Description: yksi lohko per kieli (FI ensin, sitten SV, EN) ===
      var fiTitle =
        node.translation && node.translation.fi && node.translation.fi.title
      // Localized descriptions may contain markdown links [teksti](url). Escape the
      // text first — esc() encodes & < > but NOT quotes — then linkify only http/https
      // URLs. The URL charset excludes whitespace, ) and ", so a crafted URL cannot
      // break out of the double-quoted href attribute (XSS); other schemes and markup
      // stay inert escaped text. A URL or label containing ) or ] is left unlinkified.
      var renderProse = function (text) {
        return esc(text).replace(
          /\[([^\]]+)\]\((https?:\/\/[^\s)"]+)\)/g,
          function (match, label, url) {
            return (
              '<a href="' +
              url +
              '" target="_blank" rel="noopener noreferrer">' +
              label +
              '</a>'
            )
          }
        )
      }
      var languageBlock = function (lang, title, description) {
        var block = $('<div class="jsv-lang-block"></div>')
        block.append(
          $('<span class="jsv-lang-tag jsv-lang-' + lang + '"></span>').text(
            lang.toUpperCase()
          )
        )
        var term = title || fiTitle || node.plainName || node.title || node.name
        if (term) {
          block.append($('<div class="jsv-term"></div>').text(term))
        }
        if (description) {
          block.append(
            $('<div class="jsv-prose"></div>').html(renderProse(description))
          )
        }
        return block
      }
      var blocks = []
      $.each(['fi', 'sv', 'en'], function (i, lang) {
        var t = node.translation && node.translation[lang]
        if (t && (t.title || t.description)) {
          blocks.push(languageBlock(lang, t.title, t.description))
        }
      })
      if (blocks.length === 0 && node.title) {
        blocks.push(languageBlock('fi', node.title, ''))
      }
      var localized = $('#info-localized').empty()
      $.each(blocks, function (i, block) {
        localized.append(block)
      })
      $('#info-description-header').toggle(blocks.length > 0)
      JSV.createPre(schema, tv4.getSchema(node.schema), false, node.plainName)
      var example =
        !node.example &&
        node.parent &&
        node.parent.example &&
        node.parent.type === 'object'
          ? node.parent.example
          : node.example
      if (example) {
        if (example !== JSV.example) {
          $.getJSON(
            node.schema.match(/^(.*?)(?=[^\/]*\.json)/g) + example,
            function (data) {
              var pointer = example.split('#')[1]
              if (pointer) {
                data = jsonpointer.get(data, pointer)
              }
              JSV.createPre(ex, data, false, node.plainName)
              JSV.example = example
            }
          ).fail(function () {
            ex.html('<h3>No example found.</h3>')
            JSV.example = false
          })
        } else {
          var pre = ex.find('pre'),
            highEl
          pre.find('span.highlight').removeClass('highlight')
          if (node.plainName) {
            pre.highlight(node.plainName, 'highlight', true)
          }
          highEl = pre.find('span.highlight')[0]
          if (highEl) {
            pre.scrollTo(highEl, 900)
          }
        }
      } else {
        ex.html('<h3>No example available.</h3>')
        JSV.example = false
      }
    },
    createPre: function (el, obj, title, exp) {
      var pre = $(
        '<pre><code class="language-json">' +
          JSON.stringify(obj, null, '  ') +
          '</code></pre>'
      )
      var btn = $(
        '<a href="#" class="ui-btn ui-mini ui-icon-action ui-btn-icon-right">Open in new window</a>'
      ).click(function () {
        var w = window.open('', 'pre', null, true)
        $(w.document.body).html(
          $('<div>').append(pre.clone().height('95%')).html()
        )
        hljs.highlightBlock($(w.document.body).children('pre')[0])
        $(w.document.body).append(
          '<link rel="stylesheet" href="http://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.1/styles/default.min.css">'
        )
        w.document.title = title || 'JSON Schema Viewer'
        w.document.close()
      })
      el.html(btn)
      if (exp) {
        pre.highlight(exp, 'highlight', true)
      }
      el.append(pre)
      pre.height(
        el.height() -
          btn.outerHeight(true) -
          (pre.outerHeight(true) - pre.height())
      )
      var highEl = pre.find('span.highlight')[0]
      if (highEl) {
        pre.scrollTo(highEl, 900)
      }
    },
    compilePath: function (node, path) {
      var p
      if (node.parent) {
        p = path ? node.name + ' > ' + path : node.name
        return JSV.compilePath(node.parent, p)
      } else {
        p = path ? node.name + ' > ' + path : node.name
      }
      return p
    },
    loadInputExample: function (uri, target) {
      $.getJSON(uri)
        .done(function (fetched) {
          $('#' + target).val(JSON.stringify(fetched, null, '  '))
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
          JSV.showError('Failed to load example: ' + errorThrown)
        })
    },
    setPermalink: function (node) {
      var uri = new URI(),
        path = JSV.getNodeNamePath(node).map(encodeURIComponent).join('.')
      uri.hash($.mobile.activePage.attr('id') + '?open=' + path)
      $('#permalink').html(JSV.compilePath(node))
      $('#sharelink').val(uri.toString())
    },
    getNodePath: function (node, path) {
      var p = path || [],
        parent = node.parent
      if (parent) {
        var children = parent.children || parent._children
        p.unshift(children.indexOf(node))
        return JSV.getNodePath(parent, p)
      } else {
        return p
      }
    },
    // Koski: deep links, see documentation/json-schema-viewer.md
    getNodeNamePath: function (node, path) {
      var p = path || [],
        parent = node.parent
      if (parent) {
        var siblings = parent.children || parent._children
        if (node.isReal || siblings.length > 1) {
          p.unshift(node.isReal ? node.plainName : node.title)
        }
        return JSV.getNodeNamePath(parent, p)
      } else {
        return p
      }
    },
    matchingChildren: function (children, segment) {
      var s = segment.toLowerCase(),
        names = function (node) {
          return [
            (node.plainName || '').toLowerCase(),
            (node.title || '').toLowerCase()
          ]
        },
        exact = children.filter(function (c) {
          return names(c).indexOf(s) >= 0
        })
      return exact.length
        ? exact
        : children.filter(function (c) {
            return names(c).some(function (n) {
              return n.indexOf(s) === 0
            })
          })
    },
    findChildrenByName: function (node, segment) {
      var children = node.children || node._children || [],
        direct = JSV.matchingChildren(children, segment)
      if (direct.length) {
        return direct
      }
      return children
        .filter(function (c) {
          return !c.isReal
        })
        .reduce(function (acc, c) {
          return acc.concat(JSV.findChildrenByName(c, segment))
        }, [])
    },
    // "." separates name segments because jQuery Mobile treats a hash containing "/" as a page path.
    resolveNodePaths: function (path) {
      var isIndexPath = /^[0-9]+(-[0-9]+)*$/.test(path),
        segments = isIndexPath ? path.split('-') : path.split('.'),
        nodes = [JSV.treeData],
        i
      for (i = 0; i < segments.length && nodes.length; i++) {
        nodes = nodes.reduce(function (acc, node) {
          if (isIndexPath) {
            var child = (node.children || node._children || [])[
              parseInt(segments[i], 10)
            ]
            return child ? acc.concat([child]) : acc
          }
          return acc.concat(
            JSV.findChildrenByName(node, decodeURIComponent(segments[i]))
          )
        }, [])
      }
      if (!nodes.length) {
        console.warn('JSV: node path not found: ' + path)
      }
      return nodes
    },
    resolveNodePath: function (path) {
      return JSV.resolveNodePaths(path)[0] || null
    },
    expandAncestors: function (node) {
      var p = node.parent
      while (p) {
        if (p._children) {
          JSV.expand(p)
        }
        p = p.parent
      }
    },
    expandThroughWrappers: function (node) {
      var children
      JSV.expand(node)
      children = node.children || []
      if (children.length === 1 && !children[0].isReal) {
        JSV.expandThroughWrappers(children[0])
      }
    },
    expandNodePath: function (path) {
      var node = JSV.resolveNodePath(
        Array.isArray(path) ? path.join('-') : path
      )
      if (!node) {
        return null
      }
      JSV.expandAncestors(node)
      JSV.update(JSV.treeData)
      JSV.centerNode(node)
      return node
    },
    parseHashParams: function (hash) {
      var params = {},
        query = (hash || '').split('?')[1] || ''
      query.split('&').forEach(function (pair) {
        var eq = pair.indexOf('='),
          key = eq < 0 ? pair : pair.substring(0, eq),
          value = eq < 0 ? '' : pair.substring(eq + 1)
        if (key) {
          params[key] = value
        }
      })
      return params
    },
    resetDeepLinks: function () {
      JSV.visit(
        JSV.treeData,
        function (d) {
          d.marked = false
        },
        function (d) {
          return d.children || d._children
        }
      )
      if (JSV.focusNode) {
        d3.select('#n-' + JSV.focusNode.id).classed('focus', false)
        JSV.focusNode = null
      }
      JSV.panelUnpinned = false
      JSV.deepLinkTarget = null
      JSV.resetTree(JSV.treeData, 1)
    },
    applyDeepLinks: function (hash) {
      var params = JSV.parseHashParams(hash),
        selected = null,
        target = null,
        touched = false,
        marked = false,
        list = function (key) {
          return params[key] ? params[key].split(',').filter(Boolean) : []
        }
      list('open').forEach(function (path) {
        JSV.resolveNodePaths(path).forEach(function (node) {
          JSV.expandAncestors(node)
          JSV.expandThroughWrappers(node)
          target = target || node
          touched = true
        })
      })
      list('mark').forEach(function (path) {
        JSV.resolveNodePaths(path).forEach(function (node) {
          JSV.expandAncestors(node)
          node.marked = true
          target = target || node
          touched = true
          marked = true
        })
      })
      if (params.v) {
        selected = JSV.resolveNodePath(params.v)
        if (selected) {
          JSV.expandAncestors(selected)
          touched = true
        }
      }
      if (!touched) {
        JSV.resetViewer()
        return
      }
      JSV.update(JSV.treeData)
      JSV.deepLinkTarget = selected || target
      if (marked) {
        // Closing the panel deselects (panelclose handler), so v= only centers here.
        JSV.panelUnpinned = true
        $('#info-panel').panel('close')
      }
      if (selected) {
        JSV.centerNode(selected)
        JSV.flashNode(selected)
        if (!marked) {
          JSV.selectNode(selected, true)
        }
      } else {
        JSV.centerNode(target)
      }
    },
    buildSearchList: function (items, init) {
      var ul = $('ul#search-result')
      texts = {}
      $.each(items, function (i, v) {
        var data = v.split('|')
        if (!texts[data[0]]) {
          texts[data[0]] = true
          var li = $('<li/>').attr('data-icon', 'false').appendTo(ul)
          $('<a/>').attr('data-path', data[1]).text(data[0]).appendTo(li)
        }
      })
      if (init) {
        ul.filterable()
      }
      ul.filterable('refresh')
      ul.on('click', function (e) {
        var path = $(e.target).attr('data-path')
        var node = JSV.expandNodePath(path.split('-'))
        JSV.flashNode(node)
      })
    },
    flashNode: function (node, times) {
      var t = times || 4,
        text = $('#n-' + node.id + ' text')
      while (t--) {
        text.fadeTo(350, 0).fadeTo(350, 1)
      }
    },
    visit: function (parent, visitFn, childrenFn) {
      if (!parent) {
        return
      }
      visitFn(parent)
      var children = childrenFn(parent)
      if (children) {
        var count = children.length,
          i
        for (i = 0; i < count; i++) {
          JSV.visit(children[i], visitFn, childrenFn)
        }
      }
    },
    compileData: function (schema, parent, name, real, depth) {
      depth = depth || 0
      if (depth > this.maxDepth) {
        return
      }
      var key,
        node,
        s = schema.$ref ? tv4.getSchema(schema.$ref) : schema,
        props = s.properties,
        items = s.items,
        owns = Object.prototype.hasOwnProperty,
        all = {},
        parentSchema = function (node) {
          var schema = node.id || node.$ref || node.schema
          if (schema) {
            return schema
          } else if (node.parentSchema) {
            return parentSchema(node.parentSchema)
          } else {
            return null
          }
        }
      if (s.allOf) {
        all.allOf = s.allOf
      }
      if (s.oneOf) {
        all.oneOf = s.oneOf
      }
      if (s.anyOf) {
        all.anyOf = s.anyOf
      }
      node = {
        description: schema.description || s.description,
        name: (real ? name : false) || s.title || name || 'schema',
        isReal: real,
        plainName: name,
        type: s.type,
        displayType:
          s.type ||
          (s['enum']
            ? 'enum: ' + s['enum'].join(', ')
            : s.items
              ? 'array'
              : s.properties
                ? 'object'
                : 'ambiguous'),
        translation: schema.translation || s.translation,
        example: schema.example || s.example,
        opacity: real ? 1 : 0.5,
        required: s.required,
        schema: s.id || schema.$ref || parentSchema(parent),
        parentSchema: parent,
        deprecated: schema.deprecated || s.deprecated,
        redundantData: schema.redundantData || s.redundantData,
        sensitive: schema.sensitive || s.sensitive,
        minItems: s.minItems,
        maxItems: s.maxItems,
        minimum: s.minimum,
        maximum: s.maximum,
        exclusiveMinimum: s.exclusiveMinimum,
        exclusiveMaximum: s.exclusiveMaximum,
        pattern: s.pattern,
        enumValues: s['enum'],
        koodisto: schema.koodisto || s.koodisto,
        unit: schema.unit || s.unit,
        deprecatedMessage: schema.deprecatedMessage || s.deprecatedMessage,
        deprecatedText: schema.deprecatedText || s.deprecatedText,
        readOnly: schema.readOnly || s.readOnly,
        readOnlyText: schema.readOnlyText || s.readOnlyText,
        koodiarvot: schema.koodiarvot || s.koodiarvot,
        oksa: schema.oksa || s.oksa,
        default: schema['default'] || s['default'],
        conditions: schema.conditions || s.conditions,
        acceptsSingleValue: schema.acceptsSingleValue || s.acceptsSingleValue,
        synthetic: schema.synthetic || s.synthetic
      }
      node.require =
        parent && parent.required
          ? parent.required.indexOf(node.name) > -1
          : false
      if (parent) {
        if (node.name === 'item') {
          node.parent = parent
          if (node.type) {
            node.name = node.type
            parent.children.push(node)
          }
        } else if (parent.name === 'item') {
          parent.parent.children.push(node)
        } else {
          parent.children.push(node)
        }
      } else {
        JSV.treeData = node
      }
      node.title = node.name
      if (node.type === 'array') {
        node.name += '[' + (s.minItems || ' ') + ']'
        node.minItems = s.minItems
      }
      if (node.type === 'object' && node.name !== 'item') {
        node.name += '{ }'
      }
      if (props || items || all) {
        node.children = []
      }
      for (key in props) {
        if (!owns.call(props, key)) {
          continue
        }
        JSV.compileData(props[key], node, key, true, depth + 1)
      }
      for (key in all) {
        if (!owns.call(all, key)) {
          continue
        }
        if (!all[key]) {
          continue
        }
        var allNode = {
          name: key,
          children: [],
          opacity: 0.5,
          parentSchema: parent,
          schema: schema.$ref || parentSchema(parent)
        }
        if (node.name === 'item') {
          node.parent.children.push(allNode)
        } else {
          node.children.push(allNode)
        }
        for (var i = 0; i < all[key].length; i++) {
          JSV.compileData(
            all[key][i],
            allNode,
            s.title || all[key][i].type,
            false,
            depth + 1
          )
        }
      }
      if (Object.prototype.toString.call(items) === '[object Object]') {
        JSV.compileData(items, node, 'item', false, depth + 1)
      } else if (Object.prototype.toString.call(items) === '[object Array]') {
        items.forEach(function (itm, idx, arr) {
          JSV.compileData(itm, node, idx.toString(), false, depth + 1)
        })
      }
    },
    resizeViewer: function () {
      JSV.viewerWidth = $('#main-body').width()
      JSV.viewerHeight = $('#main-body').height()
      if (JSV.focusNode || JSV.deepLinkTarget) {
        JSV.centerNode(JSV.focusNode || JSV.deepLinkTarget)
      }
    },
    resetTree: function (source, level) {
      JSV.visit(
        source,
        function (d) {
          if (
            d.children &&
            d.children.length > 0 &&
            d.depth > level &&
            !JSV.labels[d.name]
          ) {
            JSV.collapse(d)
          } else if (JSV.labels[d.name]) {
            JSV.expand(d)
          }
        },
        function (d) {
          if (d.children && d.children.length > 0) {
            return d.children
          } else if (d._children && d._children.length > 0) {
            return d._children
          } else {
            return null
          }
        }
      )
    },
    resetViewer: function () {
      var page = $('#viewer-page')
      page.css('display', 'block')
      var root = JSV.treeData
      root.x0 = JSV.viewerHeight / 2
      root.y0 = 0
      JSV.tree.nodes(root)
      JSV.resetTree(root, 1)
      JSV.update(root)
      page.css('display', '')
      JSV.centerNode(root, 4)
    },
    centerNode: function (source, ratioX) {
      var rX = ratioX ? ratioX : 2,
        zl = JSV.zoomListener,
        scale = zl.scale(),
        x = -source.y0 * scale + JSV.viewerWidth / rX,
        y = -source.x0 * scale + JSV.viewerHeight / 2
      d3.select('g#node-group')
        .transition()
        .duration(JSV.duration)
        .attr('transform', 'translate(' + x + ',' + y + ')scale(' + scale + ')')
      zl.scale(scale)
      zl.translate([x, y])
    },
    collapse: function (d) {
      if (d.children) {
        d._children = d.children
        d.children = null
      }
    },
    expand: function (d) {
      if (d._children) {
        d.children = d._children
        d._children = null
      }
      if (d.children) {
        var count = d.children.length,
          i
        for (i = 0; i < count; i++) {
          if (JSV.labels[d.children[i].name]) {
            JSV.expand(d.children[i])
          }
        }
      }
    },
    toggleChildren: function (d) {
      if (d.children) {
        JSV.collapse(d)
      } else if (d._children) {
        JSV.expand(d)
      }
      return d
    },
    click: function (d) {
      if (!JSV.labels[d.name]) {
        if (d3.event && d3.event.defaultPrevented) {
          return
        }
        d = JSV.toggleChildren(d)
        JSV.update(d)
        JSV.centerNode(d)
      }
    },
    clickTitle: function (d) {
      if (!JSV.labels[d.name]) {
        if (d3.event && d3.event.defaultPrevented) {
          return
        }
        JSV.selectNode(d, true)
      }
    },
    selectNode: function (d, openPanel) {
      if (JSV.focusNode) {
        d3.select('#n-' + JSV.focusNode.id).classed('focus', false)
      }
      JSV.focusNode = d
      JSV.centerNode(d)
      d3.select('#n-' + d.id).classed('focus', true)
      if (!JSV.plain) {
        JSV.setPermalink(d)
        var titleText = 'Info: ' + d.name
        // Shrink the font for long field names so the whole name stays visible.
        var titleSize = Math.max(
          10,
          15 - Math.max(0, titleText.length - 24) * 0.15
        )
        $('#info-title')
          .text(titleText)
          .css('font-size', titleSize + 'px')
        JSV.setInfo(d)
        if (openPanel) {
          $('#info-panel').panel('open')
        }
      }
    },
    visibleNodes: function () {
      var out = []
      ;(function walk(node) {
        if (!node) {
          return
        }
        if (!JSV.labels[node.name]) {
          out.push(node)
        }
        if (node.children) {
          node.children.forEach(walk)
        }
      })(JSV.treeData)
      return out
    },
    firstSelectableChild: function (d) {
      var kids = d.children || [],
        i,
        deep
      for (i = 0; i < kids.length; i++) {
        if (!JSV.labels[kids[i].name]) {
          return kids[i]
        }
        deep = JSV.firstSelectableChild(kids[i])
        if (deep) {
          return deep
        }
      }
      return null
    },
    keyNav: function (e) {
      var keys = {
        ArrowUp: 1,
        ArrowDown: 1,
        ArrowLeft: 1,
        ArrowRight: 1,
        Enter: 1
      }
      if (!keys[e.key]) {
        return
      }
      var tag = document.activeElement && document.activeElement.tagName
      if (tag === 'INPUT' || tag === 'TEXTAREA') {
        return
      }
      if (
        !$.mobile.activePage ||
        $.mobile.activePage.attr('id') !== 'viewer-page'
      ) {
        return
      }
      if (!JSV.treeData) {
        return
      }
      e.preventDefault()
      var current = JSV.focusNode || JSV.treeData
      if (e.key === 'Enter') {
        JSV.clickTitle(current)
      } else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
        var list = JSV.visibleNodes(),
          i = list.indexOf(current)
        if (i < 0) {
          JSV.clickTitle(list[0])
        } else {
          var next =
            e.key === 'ArrowDown'
              ? list[Math.min(i + 1, list.length - 1)]
              : list[Math.max(i - 1, 0)]
          JSV.clickTitle(next)
        }
      } else if (e.key === 'ArrowRight') {
        if (current._children) {
          JSV.click(current)
        } else if (current.children && current.children.length) {
          var child = JSV.firstSelectableChild(current)
          if (child) {
            JSV.clickTitle(child)
          }
        }
      } else if (e.key === 'ArrowLeft') {
        if (current.children && current.children.length) {
          JSV.click(current)
        } else if (current.parent) {
          JSV.clickTitle(current.parent)
        }
      }
    },
    pinPanel: function () {
      if (JSV.plain) {
        return
      }
      if (
        !$.mobile.activePage ||
        $.mobile.activePage.attr('id') !== 'viewer-page'
      ) {
        return
      }
      if (JSV.panelUnpinned) {
        return
      }
      if (window.matchMedia && window.matchMedia('(min-width: 48em)').matches) {
        $('#info-panel').panel('open')
      }
    },
    zoom: function () {
      JSV.svgGroup.attr(
        'transform',
        'translate(' +
          JSV.zoomListener.translate() +
          ')' +
          'scale(' +
          JSV.zoomListener.scale() +
          ')'
      )
    },
    interpolateZoom: function (translate, scale) {
      return d3
        .transition()
        .duration(350)
        .tween('zoom', function () {
          var iTranslate = d3.interpolate(
              JSV.zoomListener.translate(),
              translate
            ),
            iScale = d3.interpolate(JSV.zoomListener.scale(), scale)
          return function (t) {
            JSV.zoomListener.scale(iScale(t)).translate(iTranslate(t))
            JSV.zoom()
          }
        })
    },
    zoomClick: function () {
      var clicked = d3.event.target,
        direction = 1,
        factor = 0.2,
        target_zoom = 1,
        center = [JSV.viewerWidth / 2, JSV.viewerHeight / 2],
        zl = JSV.zoomListener,
        extent = zl.scaleExtent(),
        translate = zl.translate(),
        translate0 = [],
        l = [],
        view = {
          x: translate[0],
          y: translate[1],
          k: zl.scale()
        }
      d3.event.preventDefault()
      direction = this.id === 'zoom_in' ? 1 : -1
      target_zoom = zl.scale() * (1 + factor * direction)
      if (target_zoom < extent[0] || target_zoom > extent[1]) {
        return false
      }
      translate0 = [
        (center[0] - view.x) / view.k,
        (center[1] - view.y) / view.k
      ]
      view.k = target_zoom
      l = [translate0[0] * view.k + view.x, translate0[1] * view.k + view.y]
      view.x += center[0] - l[0]
      view.y += center[1] - l[1]
      JSV.interpolateZoom([view.x, view.y], view.k)
    },
    zoomListener: null,
    sortTree: function (tree) {
      tree.sort(function (a, b) {
        return b.name.toLowerCase() < a.name.toLowerCase() ? 1 : -1
      })
    },
    diagonal1: function (d) {
      var src = d.source,
        node = d3.select('#n-' + src.id)[0][0],
        dia,
        width = 0
      if (node) {
        width = node.getBBox().width
      }
      dia =
        'M' +
        (src.y + width) +
        ',' +
        src.x +
        'H' +
        (d.target.y - 30) +
        'V' +
        d.target.x +
        ('h' + 30)
      return dia
    },
    update: function (source) {
      var duration = JSV.duration
      var root = JSV.treeData
      var levelWidth = [1]
      var childCount = function (level, n) {
        if (n.children && n.children.length > 0) {
          if (levelWidth.length <= level + 1) {
            levelWidth.push(0)
          }
          levelWidth[level + 1] += n.children.length
          n.children.forEach(function (d) {
            childCount(level + 1, d)
          })
        }
      }
      childCount(0, root)
      var newHeight = d3.max(levelWidth) * 45
      JSV.tree.size([newHeight, JSV.viewerWidth])
      var nodes = JSV.tree.nodes(root),
        links = JSV.tree.links(nodes)
      JSV.visit(
        JSV.treeData,
        function (d) {
          JSV.maxLabelLength[d.depth] = Math.max(
            d.name.length,
            JSV.maxLabelLength[d.depth] ? JSV.maxLabelLength[d.depth] : 0
          )
        },
        function (d) {
          return d.children && d.children.length > 0 ? d.children : null
        }
      )
      nodes.forEach(function (d) {
        d.y = d.parent
          ? d.parent.y + JSV.maxLabelLength[d.parent.depth] * 8 + 100
          : 0
      })
      var node = JSV.svgGroup.selectAll('g.node').data(nodes, function (d) {
        return d.id || (d.id = ++JSV.counter)
      })
      var nodeEnter = node
        .enter()
        .append('g')
        .attr('class', function (d) {
          return JSV.labels[d.name] ? 'node label' : 'node'
        })
        .classed('deprecated', function (d) {
          return d.deprecated
        })
        .classed('redundantData', function (d) {
          return d.redundantData
        })
        .classed('sensitive', function (d) {
          return d.sensitive
        })
        .attr('id', function (d, i) {
          return 'n-' + d.id
        })
        .attr('transform', function (d) {
          return 'translate(' + source.y0 + ',' + source.x0 + ')'
        })
      nodeEnter
        .append('circle')
        .attr('r', 0)
        .classed('collapsed', function (d) {
          return d._children ? true : false
        })
        .on('click', JSV.click)
      nodeEnter
        .append('rect')
        .attr('class', 'focus-box')
        .attr('x', 6)
        .attr('y', -9)
        .attr('height', 18)
        .attr('rx', 3)
        .attr('width', function (d) {
          return (d.name.length + (d.require ? 1 : 0)) * 8.4 + 8
        })
      node.classed('marked', function (d) {
        return !!d.marked
      })
      nodeEnter
        .append('text')
        .attr('x', function (d) {
          return 10
        })
        .attr('dy', '.35em')
        .attr('class', function (d) {
          return d.children || d._children
            ? 'node-text node-branch'
            : 'node-text'
        })
        .classed('abstract', function (d) {
          return d.opacity < 1
        })
        .attr('text-anchor', function (d) {
          return 'start'
        })
        .text(function (d) {
          return d.name + (d.require ? '*' : '')
        })
        .style('fill-opacity', 0)
        .on('click', JSV.clickTitle)
        .on('dblclick', function (d) {
          JSV.click(d)
          JSV.clickTitle(d)
          d3.event.stopPropagation()
        })
      node
        .select('.node circle')
        .attr('r', 6.5)
        .classed('collapsed', function (d) {
          return d._children ? true : false
        })
      var nodeUpdate = node
        .transition()
        .duration(duration)
        .attr('transform', function (d) {
          return 'translate(' + d.y + ',' + d.x + ')'
        })
      nodeUpdate.select('text').style('fill-opacity', function (d) {
        return d.opacity || 1
      })
      var nodeExit = node
        .exit()
        .transition()
        .duration(duration)
        .attr('transform', function (d) {
          return 'translate(' + source.y + ',' + source.x + ')'
        })
        .remove()
      nodeExit.select('circle').attr('r', 0)
      nodeExit.select('text').style('fill-opacity', 0)
      var link = JSV.svgGroup.selectAll('path.link').data(links, function (d) {
        return d.target.id
      })
      link
        .enter()
        .insert('path', 'g')
        .attr('class', 'link')
        .attr('d', function (d) {
          var o = {
            x: source.x0,
            y: source.y0
          }
          return JSV.diagonal1({
            source: o,
            target: o
          })
        })
      link.transition().duration(duration).attr('d', JSV.diagonal1)
      link
        .exit()
        .transition()
        .duration(duration)
        .attr('d', function (d) {
          var o = {
            x: source.x,
            y: source.y
          }
          return JSV.diagonal1({
            source: o,
            target: o
          })
        })
        .remove()
      nodes.forEach(function (d) {
        d.x0 = d.x
        d.y0 = d.y
      })
    },
    createDiagram: function (callback) {
      tv4.asyncLoad([JSV.schema], function () {
        JSV.compileData(tv4.getSchema(JSV.schema), false, 'schema')
        var viewerWidth = JSV.viewerWidth
        var viewerHeight = JSV.viewerHeight
        JSV.zoomListener = d3.behavior
          .zoom()
          .scaleExtent([0.1, 3])
          .on('zoom', JSV.zoom)
        JSV.baseSvg = d3
          .select('#main-body')
          .append('svg')
          .attr('id', 'jsv-tree')
          .attr('class', 'overlay')
          .attr('width', viewerWidth)
          .attr('height', viewerHeight)
          .call(JSV.zoomListener)
        JSV.tree = d3.layout.tree().size([viewerHeight, viewerWidth])
        JSV.svgGroup = JSV.baseSvg.append('g').attr('id', 'node-group')
        JSV.resetViewer()
        JSV.centerNode(JSV.treeData, 4)
        JSV.pinPanel()
        var legendData = [
          {
            text: 'Expanded',
            y: 20
          },
          {
            text: 'Collapsed',
            iconCls: 'collapsed',
            y: 40
          },
          {
            text: 'Selected',
            itemCls: 'focus',
            y: 60
          },
          {
            text: 'Required*',
            y: 80
          },
          {
            text: 'Object{ }',
            iconCls: 'collapsed',
            y: 100
          },
          {
            text: 'Array[minimum #]',
            iconCls: 'collapsed',
            y: 120
          },
          {
            text: 'Abstract Property',
            itemCls: 'abstract',
            y: 140,
            opacity: 0.5
          },
          {
            text: 'Deprecated / unused',
            itemCls: 'deprecated',
            y: 160
          },
          {
            text: 'Sensitive',
            itemCls: 'sensitive',
            y: 180
          },
          {
            text: 'Highlighted',
            itemCls: 'marked',
            y: 200
          }
        ]
        var legendSvg = d3
          .select('#legend-items')
          .append('svg')
          .attr('width', 170)
          .attr('height', 220)
        var legendItem = legendSvg
          .selectAll('g.item-group')
          .data(legendData)
          .enter()
          .append('g')
          .attr('class', function (d) {
            var cls = 'item-group '
            cls += d.itemCls || ''
            return cls
          })
          .attr('transform', function (d) {
            return 'translate(10, ' + d.y + ')'
          })
        legendItem
          .append('circle')
          .attr('r', 6.5)
          .attr('class', function (d) {
            return d.iconCls
          })
        legendItem
          .append('text')
          .attr('x', 15)
          .attr('dy', '.35em')
          .attr('class', 'item-text')
          .attr('text-anchor', 'start')
          .style('fill-opacity', function (d) {
            return d.opacity || 1
          })
          .text(function (d) {
            return d.text
          })
        if (typeof callback === 'function') {
          callback()
        }
      })
    }
  }
}

module.exports = JSV
