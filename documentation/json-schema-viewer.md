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
  flag propagation, `.classed("sensitive")`, legend entry, info-title toggle,
  crimson CSS.
- **Lifecycle as strikethrough** — deprecated/redundant fields render struck
  through instead of orange, so a field that is both sensitive and
  deprecated/redundant shows crimson + strikethrough (both signals). The two
  lifecycle legend rows are merged into one "Deprecated / unused".
- **Info-panel clause spacing** — each appended annotation clause (`(Oksa: …)`,
  `(Vanhentunut kenttä: …)`, sensitive label, …) is shown on its own line.

The `deprecated` / `redundantData` / `sensitive` booleans the viewer reads come
from the schema JSON, emitted by the matching annotations in
`src/main/scala/fi/oph/koski/schema/annotation/` (`Deprecated`, `RedundantData`,
`Annotations.scala` → `SensitiveData`).

## Editing / build

- `json-schema-viewer.min.js` is **pretty-printed source** (despite the `.min`
  name) — edit it directly. Terser re-minifies it during the frontend build.
- `json-schema-viewer.min.css` is **un-minified source** (the `.min` name is
  historical) — edit it directly. It is served verbatim (webpack does not minify
  copied static CSS); a ~15 KB dev-tool stylesheet, so size is not a concern.
- Assets are served from `target/webapp/`, produced by webpack's
  `CopyWebpackPlugin` copying `web/static/`. **After editing, run `make front`**
  to regenerate `target/webapp`, then hard-refresh the browser — editing
  `web/static` alone has no effect on the running app until rebuilt.
- Verify what is actually served (bypassing browser cache) with e.g.
  `curl -s http://localhost:7021/koski/json-schema-viewer/styles/json-schema-viewer.min.css`.
