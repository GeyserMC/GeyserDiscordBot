require('dotenv').config()
const Discord = require('discord.js')
const client = new Discord.Client()

function initialiseModule (name) {
  require('./bot_modules/' + name + '/index.js').init(client)
}

client.on('ready', () => {
  console.log(`Logged in as ${client.user.tag}!`)
  console.log(`Add me using https://discord.com/oauth2/authorize?client_id=${process.env.BOT_ID}&scope=bot&permissions=${process.env.BOT_PERMISSION}`)
})

initialiseModule('dump_analyse')

client.login(process.env.BOT_TOKEN)
