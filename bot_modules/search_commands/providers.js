const Discord = require('discord.js')
const axios = require('axios')

/**
 * Handle the provider command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleProviderCommand = async (msg, args) => {
  let embed = new Discord.MessageEmbed()

  // Check to make sure we have a provider
  if (args.length <= 1) {
    embed.setTitle('Invalid usage')
    embed.setDescription('Missing provider to check. `!provider <provider>`')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Remove the first argument (the command)
  args.splice(0, 1)

  // Join the other arguments together and collect the providers
  const query = args.join(' ')
  const providers = await getProviders()
  let foundProvider = false

  providers.forEach((provider) => {
    if (provider.name.toLowerCase().startsWith(query.toLowerCase())) {
      foundProvider = true

      embed = new Discord.MessageEmbed()

      // Build an embed for the provider
      embed.setTitle(provider.name)
      embed.setColor(0x00ff00)
      embed.setURL(provider.url)
      embed.addField('Category', provider.category)
      embed.addField('Instructions', provider.instructions)

      // Discord doesnt support sending multiple embeds
      // per message via the client api
      msg.channel.send(embed)
    }
  })

  // Send a message if we dont know what provider
  if (!foundProvider) {
    embed.setTitle('Unknown provider')
    embed.setDescription('That provider is not on our GitHub so it is untested!')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
  }
}

/**
 * Get the providers from the github page
 * and parse + format the results
 *
 * @returns {Array} The list of provider objects with name, url, instructions and category
 */
async function getProviders () {
  // Fetch the raw response data
  let response
  try {
    response = await axios.get('https://raw.githubusercontent.com/wiki/GeyserMC/Geyser/Supported-Hosting-Providers.md')
  } catch (error) {
    console.error(error)
    return
  }

  let category = ''
  const providers = []

  response.data.split('\n').forEach(line => {
    line = line.trim()

    // Check for a header line
    if (line.startsWith('## ')) {
      category = line.replace('## ', '')
      return
    }

    // Check for a provider line
    if (line.startsWith('* ')) {
      // Get the provider name and url
      const provider = line.match(/\[([A-Za-z \\.]+)\]\(([a-z\\.\\/:-]+)\)/)

      // Get the inline instructions
      const instructions = line.match(/ \((.+)\)/)

      providers.push({
        name: provider[1],
        url: provider[2],
        instructions: instructions === null ? 'None' : instructions[1],
        category: category
      })
    } else if (line.startsWith('- ')) { // Check for indented instructions
      const provider = providers[providers.length - 1]
      provider.instructions = (provider.instructions + '\n' + line.replace('- ', '')).trim()
    }
  })

  return providers
}
