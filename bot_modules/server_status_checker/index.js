const Utils = require('../../utils')
const bedrockProtocolVersion = require('./bedrockProtocolVersion.js')

const ChatMessage = require('prismarine-chat')('1.16')
// TODO: Use this throughout the code
const escapeMarkdown = require('discord.js').Util.escapeMarkdown

const bedrockCharacters = ['\uE000', '\uE001', '\uE002', '\uE003', '\uE004', '\uE005', '\uE006', '\uE007', '\uE008', '\uE009', '\uE00A', '\uE00B', '\uE00C', '\uE00D', '\uE00E', '\uE00F', '\uE020', '\uE021', '\uE022', '\uE023', '\uE024', '\uE025', '\uE026', '\uE027', '\uE028', '\uE029', '\uE02A', '\uE02B', '\uE02C', '\uE02D', '\uE02E', '\uE02F', '\uE040', '\uE041', '\uE042', '\uE043', '\uE044', '\uE045', '\uE046', '\uE047', '\uE048', '\uE049', '\uE04A', '\uE04B', '\uE04C', '\uE04D', '\uE04E', '\uE04F', '\uE060', '\uE061', '\uE062', '\uE080', '\uE081', '\uE082', '\uE083', '\uE084', '\uE085', '\uE086', '\uE087', '\uE0A0', '\uE0A1', '\uE100', '\uE101', '\uE102', '\uE103', '\uE0C0', '\uE0C1', '\uE0C2', '\uE0C3', '\uE0C4', '\uE0C5', '\uE0C6', '\uE0C7', '\uE0C8', '\uE0C9', '\uE0CA', '\uE0CB', '\uE0CC', '\uE0CD', '\uE0E0', '\uE0E1', '\uE0E2', '\uE0E3', '\uE0E4', '\uE0E5', '\uE0E6', '\uE0E7', '\uE0E8', '\uE0E9', '\uE0EA']

exports.init = (client) => {
  client.on('message', async (msg) => {
    if (msg.content.startsWith('!status ')) {
      const args = msg.content.split(' ')
      if (Utils.isInternalIP(Utils.splitAddress(args[1]).ip)) {
        msg.channel.send('That\'s an internal IP, which means it\'s only accessible from your network.')
        return
      }
      if (args.length === 2) {
        pingServer(msg.channel, args[1], 'java')
        pingServer(msg.channel, args[1], 'bedrock')
      } else {
        switch (args[2].toLowerCase()) {
          case 'bedrock':
            pingServer(msg.channel, args[1], 'bedrock')
            break
          case 'java':
            pingServer(msg.channel, args[1], 'java')
            break
          default:
            msg.channel.send('Unknown edition')
        }
      }
    }
  })
}

/**
 * Pings the Java server and sends an embed to the channel specified
 * 
 * @param {Discord.Channel} channel TODO: TODO TODO TODO
 * @param {String} address The address to ping
 * @param {String} edition The edition (Bedrock or Java)
 */
async function pingServer (channel, address, edition) {
  // Ping the server with a timeout of 1.5s
  const addressData = Utils.splitAddress(address, (edition === 'bedrock') ? 19132 : 25565)
  // Pings as 1.16.2 (751), which probably doesn't matter
  try {
    const pingResponse = (edition === 'bedrock')
      ? await Utils.pingBedrockServer(addressData.ip, addressData.port, 5000)
      : await Utils.pingJavaServer(addressData.ip, addressData.port, 1500, 751)
    channel.send({
      embed: {
        title: (edition === 'bedrock') ? 'Bedrock server online' : 'Java server online',
        color: 0x00ff00,
        fields: [{
          name: 'MOTD',
          value: escapeMarkdown((edition === 'bedrock')
            ? pingResponse.advertise.split(';')[1]
              .replace(/§[a-z0-9]/g, '') // Strip formatting
              .split('').filter(character => !bedrockCharacters.includes(character)).join('') // Strip Bedrock Unicode characters
            : new ChatMessage(pingResponse.description)
              .toString()
              .replace(/§[a-z0-9]/g, '')) // Strip formatting
        },
        {
          name: 'Version',
          value: escapeMarkdown((edition === 'bedrock')
            ? await bedrockProtocolVersion.versionFromProtocol(pingResponse.advertise.split(';')[2])
            : pingResponse.version.name.replace(/§[a-z0-9]/g, '')) // Strip formatting
        }]
      }
    })
  } catch (err) {
    if (err.message === undefined || err.message.startsWith('getaddrinfo ENOTFOUND') || err.message.startsWith('connect ECONNREFUSED') || err.message === 'Socket timeout' || err.message === 'read ECONNRESET') {
      channel.send({
        embed: {
          title: (edition === 'bedrock') ? 'Bedrock server offline' : 'Java server offline',
          color: 0xff0000
        }
      })
    } else {
      channel.send({
        embed: {
          title: (edition === 'bedrock') ? 'Error pinging Bedrock server' : 'Error pinging Java server',
          description: err.message,
          color: 0xaaaaaa
        }
      })
    }
  }
}
