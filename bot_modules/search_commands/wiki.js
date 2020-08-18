const Discord = require('discord.js')
const cheerio = require('cheerio')
const Utils = require('../../utils')

/**
 * Handle the wiki command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleWikiCommand = async (msg, args) => {
  const embed = new Discord.MessageEmbed()

  // Check to make sure we have a search term
  if (args.length <= 1) {
    embed.setTitle('Invalid usage')
    embed.setDescription('Missing search term. `!wiki <search>`')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Remove the first argument (the command)
  args.splice(0, 1)

  // Join the other arguments together and collect the search results
  const query = args.join(' ')
  let results = await doSearch(query)

  // Set the title and color for the embed
  embed.setTitle(`Search for '${query}'`)
  embed.setColor(0x00ff00)
  embed.setURL(`https://github.com/GeyserMC/Geyser/search?q=${encodeURIComponent(query)}&type=Wikis`)

  if (results.length >= 1) {
    // Replace the results with the identical title match
    results.forEach((result) => {
      if (result.title.toLowerCase() === query.toLowerCase()) {
        results = [result]
      }
    })

    results.forEach((result) => {
      // Ignore pages starting with_ which are usually meta pages
      if (result.title.startsWith('_')) {
        return
      }

      // Add the result as a field
      embed.addField(result.title, `${result.url}\n${result.desc}`)
    })
  } else {
    // We found no results
    embed.setDescription('No results')
    embed.setColor(0xff0000)
  }

  msg.channel.send(embed)
}

/**
 * Search the wiki on GitHub
 * and parse + format the results
 *
 * @param {String} query The search query
 * @returns {Array} The list of provider objects with title, desc, updated and url
 */
async function doSearch (query) {
  // Fetch the search page
  const { status, data: contents } = await Utils.getContents(`https://github.com/GeyserMC/Geyser/search?q=${encodeURIComponent(query)}&type=Wikis`)

  // Make sure we got a response
  if (contents === '' || status !== 200) {
    return
  }

  // Load the page response into a cheerio object
  const $ = cheerio.load(contents)
  const results = []

  // Loop all search results
  $('#wiki_search_results > div:first-child').children().each((i, child) => {
    const children = $(child).children()

    // Build a result from the entry
    results.push({
      title: $(children[0]).text().trim(),
      desc: $(children[1]).html().replace(/<em>/g, '**').replace(/<\/em>/g, '**').trim(),
      updated: $(children[2]).text().trim(),
      url: 'https://github.com' + $('a', children[0]).attr('href')
    })
  })

  return results
}
