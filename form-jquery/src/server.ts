import serveStatic from "serve-static-bun"
console.log("start server on localhost:8080")
Bun.build({
  entrypoints: ["src/form.ts"],
  outdir: "dist",
  target: "browser"
})
Bun.serve({ fetch: serveStatic("./"), port: 8080 })
