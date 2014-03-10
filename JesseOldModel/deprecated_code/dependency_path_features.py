import json,sys,operator,readingTimeBankXML,proc
from pprint import pprint

anno = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.anno.json", "r").readlines()
parse = open("/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/timebank.jdoc", "r").readlines()
dep_paths = {}
pos_paths = {}

pos_pos_ex = []
pos_dep_ex = []
neg_pos_ex = []
neg_dep_ex = []
doc_info = readingTimeBankXML.get_doc_info()
num_offset_not_match = {"c":0}

def print_examples():
	pos_pos = open("pos_pos_paths.txt", "w")
	
	for item in pos_pos_ex:
		
		thefile.write("%s\n" % item)
	pos_pos.write(sorted(dep_paths.iteritems(), key=operator.itemgetter(1)))
	pos_pos.close()

def is_included(event_offset, time_offset, doc_id, pos_path, dep_path):
	if event_offset not in doc_info[doc_id]["offset"].keys() or time_offset not in doc_info[doc_id]["offset"].keys():
		num_offset_not_match["c"] += 1
		return
	time_id = doc_info[doc_id]["offset"][time_offset]
	event_id = doc_info[doc_id]["offset"][event_offset]
	if event_id not in doc_info[doc_id]["makeinstance"].keys():
		return
	eiid = doc_info[doc_id]["makeinstance"][event_id]["eiid"]
	relation_is_included = False
	for tlink in doc_info[doc_id]["tlinks"]:
		if tlink["relType"] == "IS_INCLUDED":
			if "eventInstanceID" in tlink.keys() and "relatedToTime" in tlink.keys():
				if tlink["eventInstanceID"] == eiid and tlink["relatedToTime"] == time_id:
					pos_pos_ex.append(pos_path)
					pos_dep_ex.append(dep_path)
					relation_is_included = True
			elif "timeID" in tlink.keys() and "relatedToEventInstance" in tlink.keys():
				if tlink["timeID"] == time_id and tlink["relatedToEventInstance"] == eiid:
					pos_pos_ex.append(pos_path)
					pos_dep_ex.append(dep_path)
					relation_is_included = True
	if not relation_is_included:
		neg_pos_ex.append(pos_path)
		neg_dep_ex.append(dep_path)


#i is the char_offset, not deps[i]
def add_paths(i, deps, offsets, events, pos_path, dep_path, visited, tokens, start_offset, doc_id):
	visited.add(i)
	#pos_path = pos_path + deps[i][0]
	if offsets[i][0] in events.keys():
		increment_set(pos_path, pos_paths)
		increment_set(dep_path, dep_paths)
		is_included(offsets[i][0], start_offset, doc_id, pos_path, dep_path)
	for k in range(len(deps)):
		if deps[k][2] == i and deps[k][1] not in visited:
			add_paths(deps[k][1], deps, offsets, events, pos_path + deps[k][0] + " ^ ", dep_path + " ^ ", visited, tokens, start_offset, doc_id)
		elif deps[k][1] == i and deps[k][2] not in visited:
			add_paths(deps[k][2], deps, offsets, events, pos_path + deps[k][0] + " v ", dep_path + " v ", visited, tokens, start_offset, doc_id)
	visited.remove(i)

def increment_set(key, s):
	if key in s.keys():
		s[key] = s[key] + 1
	else:
		s[key] = 1


def get_train_data():
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
			elif a[2]['type'] == 'DATE':
				times[a[0][0]]=a
		#looping over the times
		for time in times.keys():
			start = times[time][0][0]
			end = times[time][0][1]
			for sent in parsejdoc['sentences']:
				if sent['char_offsets'][0][0] <= start and end <= sent['char_offsets'][len(sent['char_offsets'])-1][1]:
					for i in range(len(sent['char_offsets'])):
						if sent['char_offsets'][i][0] == start:
							add_paths(i, sent['deps_cc'], sent['char_offsets'], events, "", "", set(), sent['tokens'], start, docid)
	#print_examples()
	return [[pos_pos_ex, pos_dep_ex], [neg_pos_ex, neg_dep_ex]]
	#print "pos pos: ", len(pos_pos_ex)
	#print "pos dep: ", len(pos_dep_ex)
	#print "neg pos: ", len(neg_pos_ex)
	#print "neg dep: ", len(neg_dep_ex)
