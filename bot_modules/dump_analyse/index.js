const GitHub = require('github-api')

const Utils = require('../../utils')

const { configEditor: config } = require('../config_manager/index.js')

/**
 * Get the status of a given commit hash, will return if commit is behind or ahead of master
 *
 * @param {String} commitId The SHA-1 hash of the commit to get the status for
 * @returns {String} The status of the commit hash
 */
async function compareToLatest (commitId) {
  try {
    const gh = new GitHub()
    const repo = gh.getRepo('GeyserMC', 'Geyser')
    const latestCommit = (await repo.listCommits()).data[0].commit.url.split('/').slice(-1)[0]
    const comparison = (await repo.compareBranches(latestCommit, commitId)).data
    let result = ''
    if (comparison.ahead_by !== 0) {
      result += 'Ahead by ' + comparison.ahead_by + ' commit' + (comparison.ahead_by === 1 ? '' : 's') + '\n'
    }
    if (comparison.behind_by !== 0) {
      result += 'Behind by ' + comparison.behind_by + ' commit' + (comparison.behind_by === 1 ? '' : 's') + '\n'
    }
    return result
  } catch (err) {
    console.error(err)
    return 'Error fetching commit data\n'
  }
}

exports.init = (client) => {
  client.on('message', async (msg) => {
    // Check if the message has a dump link in it
    const match = msg.content.match(/dump\.geysermc\.org\/([0-9a-zA-Z]{32})/)
    if (match === null || !match[1]) {
      return
    }

    // Reconstruct the URLs based on the matched string
    const url = 'https://dump.geysermc.org/' + match[1]
    const rawUrl = 'https://dump.geysermc.org/raw/' + match[1]

    // Fetch the raw response data

    const { status, data: response } = await Utils.getContents(rawUrl)

    // Make sure we got a response
    if (response === '' || status !== 200) {
      return
    }

    const problems = []
    let getVersionFromPing = true
    const supportedMinecraft = response.versionInfo.mcInfo.javaVersion

    if (!(response.bootstrapInfo.platform === 'STANDALONE')) {
      getVersionFromPing = false

      // Check if we are running an old server version
      let isOldVersion = false
      if (!response.bootstrapInfo.platformVersion.includes(supportedMinecraft) &&
        !(response.bootstrapInfo.platform === 'BUNGEECORD' || response.bootstrapInfo.platform === 'VELOCITY')) {
        isOldVersion = true
      }

      // Check plugins
      if (response.bootstrapInfo.plugins) {
        let needsFloodgate = response.config.remote['auth-type'] === 'floodgate'
        let needsFloodgateAuthType = false

        response.bootstrapInfo.plugins.forEach((item) => {
          // Check for any problematic plugins and add the problem to the list
          config.get().problematicPlugins.forEach((problemPlugin) => {
            if (item.name === problemPlugin.name && item.enabled) {
              problems.push(problemPlugin.message)
            }
          })

          if (item.name === 'ViaVersion' && item.enabled) { // Check if VV is installed
            isOldVersion = false
          } else if (item.name.toLowerCase().includes('floodgate') && item.enabled) { // Check if floodgate is installed
            needsFloodgate = false
            needsFloodgateAuthType = true
          }
        })

        // Add any problem messages relates to floodgate
        if (needsFloodgate) {
          problems.push('- `auth-type` is set to `floodgate`, but you don\'t have Floodgate installed! Download it [here](https://ci.nukkitx.com/job/GeyserMC/job/Floodgate/job/development/).')
        } else if (needsFloodgateAuthType && response.config.remote['auth-type'] !== 'floodgate') {
          problems.push(`- You have Floodgate installed, but \`auth-type\` is set to \`${response.config.remote['auth-type']}\`! Set it to \`floodgate\` if you want to use Floodgate,`)
        }
      }

      // If they are on an old version and VV isnt installed add a problem
      if (isOldVersion) {
        problems.push('- Your server needs to be on Minecraft ' + supportedMinecraft + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).")
      }
    }

    let gitInfo = ''
    const gitUrl = response.gitInfo['git.remote.origin.url'].replace(/\.git$/, '')

    // Get the git commit status
    const comparison = await compareToLatest(response.gitInfo['git.commit.id'])

    // Set the latest info based on the returned comparison
    if (comparison.includes('Behind by ')) {
      gitInfo += '**Latest:** No\n'
      problems.push("- You aren't on the latest Geyser version! Please [download](https://ci.nukkitx.com/job/GeyserMC/job/Geyser/job/master/) the latest version.")
    } else if (comparison.includes('Error')) {
      gitInfo += '**Latest:** Unknown\n'
    } else {
      gitInfo += '**Latest:** Yes\n'
    }

    // Check if they are using a custom fork
    if (!gitUrl.startsWith('https://github.com/GeyserMC/Geyser')) {
      gitInfo += '**Is fork:** Yes ' +
      `([${gitUrl.replace('https://github.com/', '')}]` +
      `(${gitUrl}))\n`
    }

    gitInfo += `**Commit:** [\`${response.gitInfo['git.commit.id.abbrev']}\`](${gitUrl}/commit/${response.gitInfo['git.commit.id']})\n`
    gitInfo += comparison

    let versionString = 'Unknown'
    const addr = response.config.remote.address + ':' + response.config.remote.port
    let addrText = addr
    const statusUrl = 'https://mcsrvstat.us/server/' + addr

    // Check if the server is listening on an internal ip and ping it if not
    if (Utils.isInternalIP(response.config.remote.address)) {
      addrText += ' (internal IP)'
    } else if (response.config.remote.address === '***') { // Censored dump
      addrText = '\\*\\*\\*' + ':' + response.config.remote.port // Discord formatting
    } else {
      let didPing = false
      try {
        // Ping the server with a timeout of 1.5s
        const pingResponse = await Utils.pingJavaServer(response.config.remote.address, response.config.remote.port, 1500, response.versionInfo.mcInfo.javaProtocol)

        // Mark the server as pinged and add the status to the address field
        didPing = true
        addrText += ' [(server online)](' + statusUrl + ')'
        versionString = pingResponse.version.name.replace(/§[a-z0-9]/g, '') // Strip formatting

        // Compare the protocol version from the ping and the dump
        // If they are different add a problem
        if (pingResponse.version.protocol !== response.versionInfo.mcInfo.javaProtocol) {
          if (getVersionFromPing) {
            problems.push('- Your server needs to be on Minecraft ' + supportedMinecraft + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).")
          }
        }
      } catch (err) {
        // Add the relevent error status to the server address
        if (didPing) {
          console.log(err)
          addrText += ' (error while pinging)'
        } else {
          addrText += ' [(server offline)](' + statusUrl + ')'
        }
      }
    }

    // If Bedrock address is censored, account for its formatting
    let bedrockAddrText = response.config.bedrock.address
    if (bedrockAddrText === '***') {
      bedrockAddrText = '\\*\\*\\*'
    }

    // Get the version string from the dump if it exists
    if (response.bootstrapInfo.platformVersion) {
      versionString = response.bootstrapInfo.platformVersion
    }

    // Get the platform name and format it to title case (Xxxxxx)
    const platformNamePretty = response.bootstrapInfo.platform.charAt(0).toUpperCase() +
                               response.bootstrapInfo.platform.slice(1).toLowerCase()

    // Build the fields for the embed
    const fields = [
      {
        name: 'Git info',
        value: gitInfo
      },
      {
        name: 'Platform',
        value: platformNamePretty,
        inline: true
      },
      {
        name: 'Remote address',
        value: addrText,
        inline: true
      },
      {
        name: 'Listen address',
        value: bedrockAddrText + ':' + response.config.bedrock.port,
        inline: true
      },
      {
        name: 'Auth type',
        value: response.config.remote['auth-type'],
        inline: true
      },
      {
        name: 'Server version',
        value: versionString,
        inline: true
      }
    ]

    // If we are not running the spigot version add the chunk cache config option
    if (response.bootstrapInfo.platform !== 'SPIGOT') {
      fields.push({
        name: 'Cache chunks?',
        value: response.config['cache-chunks'],
        inline: true
      })
    }

    // Build the final embed and send it
    const embed = {
      title: '<:geyser:723981877773598771> Geyser ' + platformNamePretty,
      url: url,
      color: 0x00ff00,
      fields: fields
    }

    if (problems.length !== 0) {
      embed.description = '**Possible problems:**\n' + problems.join('\n')
    }

    msg.channel.send({ embed })
  })
}
