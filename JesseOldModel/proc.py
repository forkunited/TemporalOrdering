import json,sys,operator,readingTimeBankXML
from pprint import pprint

#open annotated data:
anno = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.anno.json", "r").readlines()
parse = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.jdoc", "r").readlines()

def get_events_and_times():
	docs = {}
	for i,throwaway in enumerate(anno):
		docid, annodoc = anno[i].split('\t')
		annojdoc = json.loads(annodoc)
		docid, parsedoc = parse[i].split('\t')
		parsejdoc = json.loads(parsedoc)

		events = {}
		times = {}
		for a in annojdoc['annos']:
			if a[1] == "EVENT":
				events[a[0][0]]=a
			#extracting the times
			else:
				times[a[0][0]]=a
		docs[docid] = {}
		docs[docid]['events'] = events
		docs[docid]['timexs'] = times
	return docs



