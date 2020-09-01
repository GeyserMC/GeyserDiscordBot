exports.init = (client) => {
  client.on('message', async (msg) => {
    if (msg.content.startsWith('!help')) {
      // TODO: Add a command registration system instead of hardcoding
      
      const helpEmbed = new Discord.MessageEmbed()
        .setColor('#00FF00')
        .setTitle('Geyser Bot Help')
      helpEmbed.addField('`!help`', 'I think you already know what this does', true)
      helpEmbed.addField('`!wiki <search>`', 'Search the Geyser wiki', true)
      helpEmbed.addField('`!provider <provider>`', 'Search the Supported Providers page on the Geyser wiki to see if a provider is supported', true)

      msg.channel.send(helpEmbed)
    }
  })
}
