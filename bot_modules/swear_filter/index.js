const Discord = require('discord.js')
const fs = require('fs')

const Utils = require('../../utils')
const { configEditor: config } = require('../config_manager/index.js')

const listFile = 'profanity_filter.wlist'

exports.init = (client) => {
  // Check the list file exists
  if (!fs.existsSync(listFile)) {
    console.log(`Profanity word list file missing, not loading! (${listFile})`)
    return
  }

  // Fetch the log channel by id
  let logChannel
  client.channels.fetch(config.get().logChannel).then(channel => {
    logChannel = channel
  })

  // Load the word list and build regex expressions from it
  const bannedWords = {}
  fs.readFileSync(listFile, { encoding: 'utf-8' }).split('\n').forEach(word => {
    const newWord = word.toLowerCase().trim()
    if (!(newWord in bannedWords)) {
      bannedWords[newWord] = new RegExp(`(^| )${Utils.escapeRegExp(newWord)}( |$)`, 'i')
    }
  })

  // Log the amount of words loaded
  const bannedWordsRegex = Object.values(bannedWords)
  console.log(`Loaded ${bannedWordsRegex.length} banned words`)

  client.on('message', async (msg) => {
    // Loop the list looking for a regex match
    // This isn't super efficient but it is fast enough
    for (const wordRegex of bannedWordsRegex) {
      if (Utils.cleanMarkdown(msg.content).match(wordRegex)) {
        msg.delete().then(msg => {
          msg.reply('your message has been removed because it contains profanity! Please read our rules for more information.')

          const embed = new Discord.MessageEmbed()
          embed.setTitle('Profanity removed')
          embed.setDescription(`Sender: ${msg.author}\nRegex: \`${wordRegex}\`\nMessage: ${msg.content}`)
          embed.setColor(0xff0000)
          logChannel.send(embed)
        })
        return
      }
    }
  })
}
