const Discord = require('discord.js')

const commandManager = require('../command_manager/index.js')

exports.commands = [
  {
    name: 'help',
    description: 'I think you already know what this does',
    run: async (msg, args) => {
      const helpEmbed = new Discord.MessageEmbed()
        .setColor('#00FF00')
        .setTitle('Geyser Bot Help')

      Object.values(commandManager.getCommands()).forEach(command => {
        if (!('description' in command) || command.description.trim() === '') {
          return
        }

        let commandName = commandManager.prefix + command.name
        if ('args' in command) {
          commandName += ' ' + command.args
        }
        helpEmbed.addField(`\`${commandName}\``, command.description, true)
      })

      msg.channel.send(helpEmbed)
    }
  }
]
