import { Parcel } from "@parcel/core"
import { postbuild } from "./postbuild.mjs"

let bundler = new Parcel({
  entries: "src/index.html",
  defaultConfig: "@parcel/config-default",
  defaultTargetOptions: {
    distDir: "dist-nonce",
    publicUrl: "/koski/valpas/v2/assets",
  },
})

// Tällä tapaa parcel serve -komento on toteutettu ohjelmallisesti CLI:n sijasta.
await bundler.watch(async (err, event) => {
  if (err) {
    // fatal error
    throw err
  }

  if (event.type === "buildSuccess") {
    let bundles = event.bundleGraph.getBundles()
    console.log(`✨ Built ${bundles.length} bundles in ${event.buildTime}ms!`)
    // Postbuild-komento ajetaan aina, kun buildi onnistuu
    await postbuild()
  } else if (event.type === "buildFailure") {
    console.log(event.diagnostics)
  }
})
