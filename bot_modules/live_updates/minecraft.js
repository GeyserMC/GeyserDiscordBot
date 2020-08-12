const axios = require('axios')

const minecraftVersionsCache = []

/**
 * Get all the current versions and cache them for comparing later
 */
exports.populateInitialMinecraftVersions = async function () {
  const versions = await getMinecraftVersions()

  // Add each version id to the cache
  versions.forEach(version => {
    minecraftVersionsCache.push(version.id)
  })

  console.log(`Loaded ${minecraftVersionsCache.length} initial java minecraft versions`)
}

/**
 * Fetch the latest versions and compare them with the cached list
 *
 * @param {Function} callback The function to call when a new version is found, takes a message as a string
 */
exports.minecraftUpdateCheck = async function (callback) {
  const versions = await getMinecraftVersions()

  versions.forEach(version => {
    if (!minecraftVersionsCache.includes(version.id)) {
      minecraftVersionsCache.push(version.id)
      callback(minecraftVersionAsString(version))
    }
  })
}

/**
 * Get the version data from the official version_manifest.json
 *
 * @returns {Array} Each version data as an object
 */
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

/**
 * Take a version object and fomat it into a nice message for discord
 *
 * @param {Object} version The version data to insert into the message
 */
function minecraftVersionAsString (version) {
  return `A new ${version.type} version of java minecraft was just released! : ${version.id}`
}
