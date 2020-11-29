const Discord = require('discord.js')

const configEditingUsers = JSON.parse(process.env.CONFIG_EDITING_USERS)
const configEditingGroups = JSON.parse(process.env.CONFIG_EDITING_GROUPS)

const channelRegex = /<#[0-9]{18}>/
const userRegex = /<@!?&?[0-9]{18}>/

exports.init = (client) => {
  /**
   * Handle the say command
   *
   * @param {Discord.Message} msg The original message sent by the user
   */
  client.on('message', async (msg) => {
    if (!configEditingUsers.includes(msg.author.id) && !(msg.member !== null && msg.member.roles.cache.some(r => configEditingGroups.includes(r.id)))) {
      return
    }

    if (msg.content.startsWith('!say ')) {
      // replace only replaces the first instance in JS
      msg.channel.send(msg.content.replace('!say ', ''))
      msg.delete()
    } else if (msg.content.startsWith('!say-to ')) {
      const args = msg.content.split(' ')
      if (channelRegex.test(args[1])) {
        try {
          const channel = await client.channels.fetch(args[1].replace('<#', '').replace('>', ''))
          await channel.send(msg.content.replace('!say-to ' + args[1] + ' ', ''))
          msg.delete()
        } catch (err) {
          msg.channel.send('Cannot send message: `' + err.message + '`')
        }
      } else if (userRegex.test(args[1])) {
        try {
          const user = await client.users.fetch(args[1].replace('<@', '').replace('!', '').replace('&', '').replace('>', ''))
          await user.send(msg.content.replace('!say-to ' + args[1] + ' ', ''))
          msg.delete()
        } catch (err) {
          msg.channel.send('Cannot send message: `' + err.message + '`')
        }
        msg.delete()
      } else {
        console.log(args[1])
        msg.channel.send('Not a valid channel or user: ' + args[1])
      }
    }
  })
}
