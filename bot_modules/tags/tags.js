const Discord = require('discord.js')
const loader = require('./loader')

let tags = {}

exports.initTags = () => {
  // Load tags from file
  tags = loader.load()
}

exports.handleTagsCommand = async (msg, args) => {
  // Print all tags
  // Ignore args, unless we want search?
}

exports.handleTagCommand = async (msg, args) => {
  // Print a specific tag
  // Only handle first arg
}
