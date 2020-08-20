const Utils = require('../../utils')

const minecraftVersionsCache = []

/**
 * Get all the current versions and cache them for comparing later
 */
exports.populateInitialMinecraftVersions = async () => {
  const { status, data: versions } = await Utils.getContents('https://launchermeta.mojang.com/mc/game/version_manifest.json')

  // Make sure we got a response
  if (versions === '' || status !== 200) {
    console.log('Failed to load initial Java Minecraft versions')
    return
  }

  // Add each version id to the cache
  versions.versions.forEach(version => {
    minecraftVersionsCache.push(version.id)
  })

  console.log(`Loaded ${minecraftVersionsCache.length} initial Java Minecraft versions`)
}

/**
 * Fetch the latest versions and compare them with the cached list
 *
 * @param {Function} callback The function to call when a new version is found, takes a message as a string
 */
exports.minecraftUpdateCheck = async (callback) => {
  const { status, data: versions } = await Utils.getContents('https://launchermeta.mojang.com/mc/game/version_manifest.json')

  // Make sure we got a response
  if (versions === '' || status !== 200) {
    return
  }

  versions.versions.forEach(version => {
    if (!minecraftVersionsCache.includes(version.id)) {
      minecraftVersionsCache.push(version.id)
      callback(minecraftVersionAsString(version))
    }
  })
}

/**
 * Take a version object and fomat it into a nice message for discord
 *
 * @param {Object} version The version data to insert into the message
 */
function minecraftVersionAsString (version) {
  return `A new ${version.type} version of Java Minecraft was just released! : ${version.id}`
}
