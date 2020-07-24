const axios = require('axios')
const GitHub = require('github-api')
const ipRangeCheck = require('ip-range-check')
const mcping = require('mcping-js')

function pingServer (address, port, timeout, protocolVersion) {
  return new Promise((resolve, reject) => {
    const server = new mcping.MinecraftServer(address, port)
    server.ping(timeout, protocolVersion, (err, res) => {
      if (err) reject(err)
      resolve(res)
    })
  })
}
// const SUPPORTED_MINECRAFT = "1.16.1";
const INTERNAL_IP_RANGES = ['0.0.0.0/8', '10.0.0.0/8', '100.64.0.0/10', '127.0.0.0/8', '169.254.0.0/16', '172.16.0.0/12', '192.0.0.0/24', '192.0.2.0/24', '192.88.99.0/24', '192.168.0.0/16', '198.18.0.0/15', '198.51.100.0/24', '203.0.113.0/24', '224.0.0.0/4', '240.0.0.0/4', '255.255.255.255/32', '10.0.0.0/8', '172.16.0.0/12', '192.168.0.0/16']

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

exports.init = function (client) {
  client.on('message', async msg => {
    console.log(msg.content)
    const match = msg.content.match(/dump\.geysermc\.org\/([0-9a-zA-Z]{32})/)
    if (match === null || !match[1]) {
      return
    }
    const url = 'https://dump.geysermc.org/' + match[1]
    // const shortUrl = 'dump.geysermc.org/' + match[1]
    const rawUrl = 'https://dump.geysermc.org/raw/' + match[1]
    let response
    try {
      response = await axios.get(rawUrl)
    } catch (error) {
      console.error(error)
      return
    }
    const problems = []
    const supportedMinecraft = response.data.versionInfo.mcInfo.javaVersion
    if (!(response.data.bootstrapInfo.platform === 'STANDALONE')) {
      let isOldVersion = false
      if (!response.data.bootstrapInfo.platformVersion.includes(supportedMinecraft)) {
        isOldVersion = true
      }
      if (response.data.bootstrapInfo.plugins) {
        let needsFloodgate = response.data.config.remote['auth-type'] === 'floodgate'
        let needsFloodgateAuthType = false
        response.data.bootstrapInfo.plugins.forEach(function (item) {
          if (item.name === 'ProtocolSupport' && item.enabled) {
            problems.push('- You have ProtocolSupport installed, which is known to have problems with Geyser! Try replacing it with [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/) and [ViaBackwards](https://www.spigotmc.org/resources/viabackwards.27448/) it if you have problems.')
          } else if (item.name === 'ViaVersion' && item.enabled) {
            isOldVersion = false
          } else if (item.name.toLowerCase().includes('floodgate') && item.enabled) {
            needsFloodgate = false
            needsFloodgateAuthType = true
          }
        })
        if (needsFloodgate) {
          problems.push('- `auth-type` is set to `floodgate`, but you don\'t have Floodgate installed! Download it [here](https://ci.nukkitx.com/job/GeyserMC/job/Floodgate/job/development/).')
        } else if (needsFloodgateAuthType && response.data.config.remote['auth-type'] !== 'floodgate') {
          problems.push(`- You have Floodgate installed, but \`auth-type\` is set to \`${response.data.config.remote['auth-type']}\`! Set it to \`floodgate\` if you want to use Floodgate,`)
        }
      }
      if (isOldVersion) {
        problems.push('- Your server needs to be on Minecraft ' + supportedMinecraft + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).")
      }
    }

    let gitInfo = ''
    const gitUrl = response.data.gitInfo['git.remote.origin.url']
    const comparison = await compareToLatest(response.data.gitInfo['git.commit.id'])
    if (comparison.includes('Behind by ')) {
      gitInfo += '**Latest:** no\n'
      problems.push("- You aren't on the latest Geyser version! Please [download](https://ci.nukkitx.com/job/GeyserMC/job/Geyser/job/master/) the latest version.")
    } else if (comparison === 'Error fetching commit data\n') {
      gitInfo += '**Latest:** unknown\n'
    } else {
      gitInfo += '**Latest:** yes\n'
    }
    if (gitUrl.startsWith('https://github.com/GeyserMC/Geyser')) {
      // isFork = false
    } else {
      // isFork = true
      gitInfo += '**Is fork:** yes ' +
      `([${gitUrl.replace('https://github.com/', '').replace(/\.git$/, '')}]` +
      `(${gitUrl.replace(/\.git$/, '')}))\n`
    }
    gitInfo += `**Commit:** [\`${response.data.gitInfo['git.commit.id.abbrev']}\`]` +
    `(${gitUrl.replace(/\.git$/, '')}/commit/${response.data.gitInfo['git.commit.id']})\n`
    gitInfo += comparison
    const platformNamePretty = response.data.bootstrapInfo.platform.charAt(0).toUpperCase() +
                               response.data.bootstrapInfo.platform.slice(1).toLowerCase()

    let versionString = 'Unknown'
    const addr = response.data.config.remote.address + ':' + response.data.config.remote.port
    let addrText = addr
    const statusUrl = 'https://mcsrvstat.us/server/' + addr
    if (ipRangeCheck(response.data.config.remote.address, INTERNAL_IP_RANGES)) {
      addrText += ' (internal IP)'
    } else {
      let didPing = false
      try {
        const pingResponse = await pingServer(response.data.config.remote.address,
          response.data.config.remote.port, 1500,
          response.data.versionInfo.mcInfo.javaVersion)
        didPing = true
        addrText += ' [(server online)](' + statusUrl + ')'
        versionString = pingResponse.version.name.replace(/§[a-z0-9]/g, '') // Strip formatting
        if (pingResponse.version.protocol !== response.data.versionInfo.mcInfo.javaVersion) {
          const problemString = problems.push('- Your server needs to be on Minecraft ' + supportedMinecraft + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).")
          if (!problems.includes(problemString)) {
            problems.push(problemString)
          }
        }
      } catch (err) {
        if (didPing) {
          console.log(err)
          addrText += ' (error while pinging)'
        } else {
          addrText += ' [(server offline)](' + statusUrl + ')'
        }
      }
    }
    if (response.data.bootstrapInfo.platformVersion) {
      versionString = response.data.bootstrapInfo.platformVersion
    }

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
        value: response.data.config.bedrock.address + ':' + response.data.config.bedrock.port,
        inline: true
      },
      {
        name: 'Auth type',
        value: response.data.config.remote['auth-type'],
        inline: true
      },
      {
        name: 'Server version',
        value: versionString,
        inline: true
      }
    ]

    if (response.data.bootstrapInfo.platform !== 'SPIGOT') {
      fields.push({
        name: 'Cache chunks?',
        value: response.data.config['cache-chunks'],
        inline: true
      })
    }

    const embed = {
      title: '<:geyser:736173240170446909> Geyser ' + platformNamePretty,
      description: problems.length === 0
        ? undefined
        : '**Possible problems:**\n' + problems.join('\n'),
      url: url,
      color: 0x00ff00,
      fields: fields
    }
    msg.channel.send({ embed })
  })
}
