exports.init = (client) => {
  client.on('message', async (msg) => {
    if (msg.content.startsWith('!help')) {
      // TODO: Add a command registration system instead of hardcoding
      msg.channel.send({
        embed: {
          title: 'Geyser Bot Help',
          color: 0x00ff00,
          fields: [
            {
              name: '`!help`',
              value: 'I think you already know what this does',
              inline: true
            },
            {
              name: '`!wiki <search>`',
              value: 'Search the Geyser wiki',
              inline: true
            },
            {
              name: '`!provider <provider>`',
              value: 'Search the Supported Providers page on the Geyser wiki to see if a provider is supported',
              inline: true
            }
          ]
        }
      })
    }
  })
}
