const axios = require('axios')

const minecraftVersionsCache = []

exports.populateInitialMinecraftVersions = async function () {
  const versions = await getMinecraftVersions()

  versions.forEach(version => {
    minecraftVersionsCache.push(version.id)
  })

  console.log(`Loaded ${minecraftVersionsCache.length} initial java minecraft versions`)
}

exports.minecraftUpdateCheck = async function (callback) {
  const versions = await getMinecraftVersions()

  versions.forEach(version => {
    if (!minecraftVersionsCache.includes(version.id)) {
      minecraftVersionsCache.push(version.id)
      callback(minecraftVersionAsString(version))
    }
  })
}

async function getMinecraftVersions () {
  // Fetch the raw response data
  let response
  try {
    response = await axios.get('https://launchermeta.mojang.com/mc/game/version_manifest.json')
  } catch (error) {
    console.error(error)
    return
  }

  return response.data.versions
}

function minecraftVersionAsString (version) {
  return `A new ${version.type} version of java minecraft was just released! : ${version.id}`
}
