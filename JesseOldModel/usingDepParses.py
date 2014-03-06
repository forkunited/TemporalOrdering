import json,sys
from pprint import pprint

f = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.tuples", "r")
curLine = f.readline()
sentences = {}
#to get rid of initial whitespace
while not curLine.startswith("=="):
	print curLine
	curLine = f.readline()
print "reading files..."
counter = 0
while curLine:
	curSent = []
	#grabs the name of the file as the 
	sentences[curLine.split('\t')[0][4:] + " " + curLine.split('\t')[1]] = curSent
	curLine = f.readline()
	#to make a list of the lines that relate to one sentence
	while (not curLine.startswith("==") and curLine):
		curSent.append(curLine)
		curLine = f.readline()


#to get the dependency parses:
with open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.jdoc", "r") as f:
	content = f.readlines()

parses = {}
for doc in content:
	docid, jdoc = doc.split('\t')
	jdoc = json.loads(jdoc)
	parses[docid] = jdoc


print "done!"
print
print "sifting to only those that have tuples..."

withTuples = {}
for k in sentences.keys():
	for s in sentences[k]:
		if s.startswith("TUPLE"):
			withTuples[k] = sentences[k]

#print parses["WSJ900813-0157"]['sentences'][1]['deps_cc']
#sys.exit()

#to loop over those that have tuples:
counter = 0

#those with temporal phrases: 
#WSJ900813-0157 S38
#WSJ900813-0157 S31
#AP900815-0044 S31
#CNN19980222.1130.0084 S3
#CNN19980213.2130.0155 S11
#AP900815-0044 S47
#APW19980213.1320 S0
tmpPhrases = ["WSJ900813-0157 S38", "WSJ900813-0157 S31", "AP900815-0044 S31", "CNN19980222.1130.0084 S3", "CNN19980213.2130.0155 S11", "AP900815-0044 S47", "APW19980213.1320 S0"]
for k in tmpPhrases: #withTuples.keys():
	print k
	sent = parses[k.split(" ")[0]]['sentences'][int(k.split(" ")[1][1:])]
	print u' '.join( "%s_%d" % (w,i) for i,w in enumerate(sent['tokens']))
	#print parses[k.split(" ")[0]]['sentences'][int(k.split(" ")[1][1:])]['deps_basic']

	print 'parse:'
	print parses[k.split(" ")[0]]['sentences'][int(k.split(" ")[1][1:])]['deps_cc']
	#for i in parses[k.split(" ")[0]]['sentences'][int(k.split(" ")[1][1:])]['deps_basic']
	#	print i
	print
	for s in withTuples[k]:
		if s.startswith("TUPLE"):
			#getting the dependency parses:
			
			jdoc = json.loads(s.split('\t')[1]) #keys: [u'pred', u'rec', u'tupleid', u'src']
			print jdoc
			print "tokens: ", jdoc['pred']['loc']['tokids']

	print
	print

	counter = counter + 1
	#if counter == 3:
	#	sys.exit()
	




#with open('/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.tuples') as f:
#    content = f.readlines()

#looping over the lines, extracting the the tuples
#for line in content:
