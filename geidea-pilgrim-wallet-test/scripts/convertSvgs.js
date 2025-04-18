/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable no-undef */
/* eslint-disable no-console */
const svgr = require("@svgr/core")
const fs = require("fs")
const camelCase = require("camelcase")

const readFileAsync = async (filepath) => {
  return new Promise((resolve, reject) => {
    fs.readFile(filepath, (err, data) => {
      if (err) {
        console.error(err)
        reject(err)
      } else {
        resolve(data)
      }
    })
  })
}

const convert = (filename) => async (file) => {
  const componentName = filename.substring(0, filename.lastIndexOf("."))
  return new Promise((resolve) => {
    svgr
      .default(
        file,
        {
          native: true,
          typescript: true,
          plugins: [
            "@svgr/plugin-svgo",
            "@svgr/plugin-jsx",
            "@svgr/plugin-prettier"
          ],
          svgoConfig: {
            plugins: [{ removeViewBox: false }, { removeXMLNS: true }]
          },
          replaceAttrValues: { "#BFC3C9": "{props.color}" }
        },
        { componentName }
      )
      .then((jsCode) => {
        resolve(jsCode)
      })
  })
}

const writeFileAsync = (filepath) => async (file) => {
  return new Promise((resolve, reject) => {
    fs.writeFile(filepath, file, (err) => {
      if (err) {
        console.error(err)
        reject(err)
      } else {
        console.log(`"${filepath}" converted and saved`)
        resolve()
      }
    })
  })
}

const pascalCase = (filename) =>
  camelCase(filename.substring(0, filename.lastIndexOf(".")), {
    pascalCase: true,
    preserveConsecutiveUppercase: false
  }) + ".tsx"

;(async () => {
  const SVG_PATH = "./assets/icons"
  const COMPONENTS_PATH = "./src/icons"
  const srcFilenames = fs.readdirSync(SVG_PATH)
  const dstFilenames = srcFilenames.map(pascalCase)

  await Promise.all(
    srcFilenames.map((filename, index) =>
      readFileAsync(`${SVG_PATH}/${filename}`)
        .then(convert(dstFilenames[index]))
        .then(writeFileAsync(`${COMPONENTS_PATH}/${dstFilenames[index]}`))
    )
  )
})()
