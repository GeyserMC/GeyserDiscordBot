const Discord = require('discord.js')
const axios = require('axios')
const path = require('path')

const { configEditor: config } = require('../config_manager/index.js')

exports.init = (client) => {
  client.on('message', async (msg) => {
    msg.attachments.forEach(async (attachment) => {
      if (config.get().convertExtensions.includes(path.extname(attachment.name))) {
        const contents = await getAttachmentContents(attachment.url)

        const hastebinPost = await postToHastebin(contents)
        const hastebinUrl = 'https://hastebin.com/' + hastebinPost.key

        const embed = new Discord.MessageEmbed()
        embed.setColor(0x00ff00)
        embed.setTitle(hastebinUrl)
        embed.setURL(hastebinUrl)
        embed.setDescription(`Converted \`${attachment.name}\` to a hastebin link!`)

        msg.channel.send(embed)
      }
    })
  })
}

async function getAttachmentContents (url) {
  // Fetch the raw response data
  let response
  try {
    response = await axios.get(url)
  } catch (error) {
    console.error(error)
    return
  }

  return response.data
}

async function postToHastebin (contents) {
  // Fetch the raw response data
  let response
  try {
    response = await axios.post('https://hastebin.com/documents', contents)
  } catch (error) {
    console.error(error)
    return
  }

  return response.data
}
