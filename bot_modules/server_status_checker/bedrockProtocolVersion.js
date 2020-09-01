const Utils = require('../../utils')

const xpath = require('xpath')
const parse5 = require('parse5')
const xmlser = require('xmlserializer')
const Dom = require('xmldom').DOMParser

let wikiDoc
let xPathSelect

async function init () {
  // https://stackoverflow.com/questions/25753368/performant-parsing-of-pages-with-node-js-and-xpath

  const html = (await Utils.getContents('https://minecraft.gamepedia.com/Template:Protocol_version/Table/doc')).data
  const document = parse5.parse(html.toString())
  const xhtml = xmlser.serializeToString(document)
  wikiDoc = new Dom().parseFromString(xhtml)
  xPathSelect = xpath.useNamespaces({ x: 'http://www.w3.org/1999/xhtml' })
}

init()

/**
 * Converts a protocol version number to a name
 *
 * @param {Number|String} protocolVersion The protocol number
 */
exports.versionFromProtocol = async function (protocolVersion) {
  const nodes = xPathSelect(`//*[@id="mw-content-text"]/x:div/x:table[5]//x:td[text()='${protocolVersion}']`, wikiDoc)
  return nodes[0].parentNode.firstChild.textContent.replace('Bedrock Edition ', '')
}
