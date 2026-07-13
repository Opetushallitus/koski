# JSON Schema Viewer (vendored, Koski-patched)

The interactive JSON Schema viewer served at `/koski/json-schema-viewer/`
(rendered by `JsonSchemaViewerHtmlServlet`). It powers the schema browsers linked
from the API documentation, including the omadata packages. The vendored assets
live in `web/static/json-schema-viewer/`.

## Provenance

This is a vendored copy of the **`dist` output** (v0.3.4) of
[Opetushallitus/json-schema-viewer](https://github.com/Opetushallitus/json-schema-viewer),
a fork of the upstream [ADIwg/json-schema-viewer](https://github.com/adiwg/json-schema-viewer)
("mdJSON" viewer).

**The upstream fork is effectively dormant** — its `master` has not moved since
2016 (only automated dependency-bump branches since). Only the built artifacts
were imported; the fork's actual source (`json-schema-viewer.js`, LESS, Handlebars
templates, Gruntfile) is **not** in this repo. In practice we own and maintain
this copy directly.

## Koski-local patches (NOT present upstream)

These are applied directly to the vendored `js`/`styles` files in
`web/static/json-schema-viewer/`. A future re-vendor from upstream **must
re-apply them**:

- **`redundantData`** — highlights fields tagged `@RedundantData`.
- **`sensitive`** (TOR-2621) — highlights fields tagged `@SensitiveData`: node
  flag propagation, `.classed("sensitive")`, legend entry, and darksalmon tree
  text (`fill`).
- **Lifecycle de-emphasis** — deprecated/redundant fields render dimmed to gray
  (`fill: #777`) with a strikethrough, instead of the previous orange. Colour
  and strikethrough are independent channels, so a field that is both sensitive
  and deprecated/redundant keeps its darksalmon colour and gains the strike
  (both signals; sensitive wins the `fill`). Note: on SVG text the strike line
  follows `fill` and ignores `text-decoration-color`. The two lifecycle legend
  rows are merged into one "Deprecated / unused".
- **Info-panel clause spacing** — each appended annotation clause (`(Oksa: …)`,
  `(Vanhentunut kenttä: …)`, sensitive label, …) is shown on its own line.
- **Info-panel title readability** — the title is not given field styling, the
  inherited jQuery Mobile letterpress `text-shadow` is removed (it blurred the
  tree/info text), and the title shows long property names in full (`margin: 0`,
  `overflow: visible`, `white-space: normal`).
- **Selected-node highlight** — each node gets a `.focus-box` `<rect>` (sized
  from the label length, since the tree font is monospace) shown only on the
  focused node as a rounded green outline around the label. Replaces the plain
  bold, which was hard to see.
- **Translation panel rendering** (TOR-2464) — the info panel's "Translation"
  tab reads `node.translation` and renders one block per language (quiet
  language label, bold title, plain description), using `.text()` so the
  strings are escaped. See "Translation tab" below for how the data is
  produced.

The `deprecated` / `redundantData` / `sensitive` booleans the viewer reads come
from the schema JSON, emitted by the matching annotations in
`src/main/scala/fi/oph/koski/schema/annotation/` (`Deprecated`, `RedundantData`,
`Annotations.scala` → `SensitiveData`).

## Translation tab (TOR-2464)

The info panel's "Translation" tab is populated server-side. Each viewer
schema is served through `LocalizedSchemas` (`fi.oph.koski.documentation`),
which:

- builds the same `ClassSchema` as the corresponding `*Schema` object
  (`KoskiSchema.createSchema`),
- runs `SchemaLocalizationEnricher` (`fi.oph.koski.localization`) to attach a
  `translation` field to each property and class node, resolved from
  `koskiLocalizationRepository` using the same keys as
  `KoskiSpecificSchemaLocalization` — **title + description only**,
- caches the enriched JSON ~1 min (matching the localization cache refresh),
  so translation edits appear without a restart.

The emitted shape is language-keyed and **excludes Finnish** (the Finnish
title is the node name and the Finnish description is already in the
Definition box):

```json
"translation": { "sv": { "title": "…", "description": "…" }, "en": { … } }
```

Notes:

- Tooltip/info-link annotations aren't emitted into the schema JSON, so they
  aren't shown; missing keys/languages are skipped, and a node with no sv/en
  translation gets no `translation` field.
- The `translation` keyword is non-standard and ignored by JSON Schema
  validators, so it is a safe additive change to the documentation schemas
  (which external integrators may also consume).
- To cover a new schema, register it in `LocalizedSchemas`.

## Editing / build

- `json-schema-viewer.js` is **pretty-printed source** — edit it directly.
  Terser minifies it during the frontend build.
- `json-schema-viewer.css` is **un-minified source** — edit it directly. It is
  served verbatim (webpack does not minify copied static CSS); a ~15 KB dev-tool
  stylesheet, so size is not a concern.
- Assets are served from `target/webapp/`, produced by webpack's
  `CopyWebpackPlugin` copying `web/static/`. **After editing, run `make front`**
  to regenerate `target/webapp`, then hard-refresh the browser — editing
  `web/static` alone has no effect on the running app until rebuilt.
- Verify what is actually served (bypassing browser cache) with e.g.
  `curl -s http://localhost:7021/koski/json-schema-viewer/styles/json-schema-viewer.css`.
