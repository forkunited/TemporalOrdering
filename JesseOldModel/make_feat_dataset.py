import json,sys,operator,readingTimeBankXML,proc,sentence_dependency_feature
from pprint import pprint

#outline: 
#one function to loop over the data, one sentence at a time
#one function that takes a single sentence and computes features from it.

#example of how to read in all data:
def full_dataset():
	all_doc_dataset = {}
	anno = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.anno.json", "r").readlines()
	parse = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.jdoc", "r").readlines()
	e_and_t = proc.get_events_and_times()
	docs_info = readingTimeBankXML.get_doc_info()
	for i,throwaway in enumerate(anno):
		doc_dataset = {}
		docid, annodoc = anno[i].split('\t')
		annojdoc = json.loads(annodoc)
		docid, parsedoc = parse[i].split('\t')
		parsejdoc = json.loads(parsedoc)
		for j in range(len(parsejdoc['sentences'])):
			#the parsejdoc['sentences'][j] contains the dependency parse, basic dependency parse, lemmas, deps_cc, POS, tokens, 
			#char_offsets, ner, and normner (whatever that is).
			#e_and_t contains events and times, indexed by character offset
			#doc_info[docid] contains the tlinks, the makeinstances, and the character offsets.
			doc_dataset[j] = compute_sentence_feats(parsejdoc['sentences'][j], e_and_t[docid], docs_info[docid])
		all_doc_dataset[docid] = doc_dataset
	return all_doc_dataset

#compute features for the sentence.
#first, compute the dependency paths between each event / timex
def compute_sentence_feats(sent, doc_e_and_t, doc_info):
	sentence_feats = {}
	sentence_feats['dependency_path'] = sentence_dependency_feature.compute_sentence_path(sent, doc_info, doc_e_and_t)
	
	return sentence_feats

