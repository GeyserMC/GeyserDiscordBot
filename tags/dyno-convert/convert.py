import json

template = '''type: text

---

'''

with open('tags.json') as json_file:
  data = json.load(json_file)
  for tag in data['tags']:
    f = open("../" + tag['tag'] + ".tag", "w")
    f.write(template + tag['content'])
    f.close()