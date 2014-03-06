#sent contains [u'parse', u'deps_basic', u'lemmas', u'deps_cc', u'pos', u'tokens', u'char_offsets', u'normner', u'ner']
#e_and_t contains events and times, indexed by character offset
#doc_info contains the tlinks, the makeinstances, and the character offsets.
def compute_sentence_path(sent, doc_info, doc_e_and_t):
	paths = {}
	pairs_with_edge = []
	for i in range(len(doc_info['tlinks'])):
		tlink = doc_info['tlinks'][i]
		#goal: find the token numbers of each of the elements in the tlink
		first_token_num = -1
		second_token_num = -1
		if 'eventInstanceID' in tlink.keys() and 'relatedToTime' in tlink.keys():
			first_token_num = get_event_token(sent, doc_info, tlink['eventInstanceID'])
			second_token_num = get_timex_token(sent, doc_info, tlink['relatedToTime'])
		elif "timeID" in tlink.keys() and "relatedToEventInstance" in tlink.keys():
			first_token_num = get_event_token(sent, doc_info, tlink['relatedToEventInstance'])
			second_token_num = get_timex_token(sent, doc_info, tlink['timeID'])
		if first_token_num == -1 or second_token_num == -1:
			continue
		pairs_with_edge.append([first_token_num, second_token_num])
		path = {}
		path['path'] = ''
		find_unlabeled_path(first_token_num, second_token_num, sent['deps_cc'], set(), '', path)
		paths[tlink['lid']] = [path['path'], tlink['relType']]
	add_no_edge(sent, doc_info, doc_e_and_t, paths, pairs_with_edge)
	return paths
	#loop over the events and timexs
	#need: set of [label, features] for each relation
	#loop over tlinks
		#find path for each tlink

def add_no_edge(sent, doc_info, doc_e_and_t, paths, pairs_with_edge):
	counter = 0
	for t in doc_e_and_t['timexs'].keys():
		for e in doc_e_and_t['events'].keys():
			t_token = get_token_from_char(sent, t)
			e_token = get_token_from_char(sent, e)
			if t_token == -1 or e_token == -1:
				continue
			if not ([t_token, e_token] in pairs_with_edge or [e_token, t_token] in pairs_with_edge):
				counter += 1
				path = {}
				path['path'] = ''
				find_unlabeled_path(e_token, t_token, sent['deps_cc'], set(), '', path)
				paths[counter] = [path['path'], 'no_edge']
				
	#loop over the events
		#loop over the times
	
def find_unlabeled_path(cur, end, deps, visited, cur_path, path):
	if cur == end:
		path['path'] = cur_path
		return
	visited.add(cur)
	for k in range(len(deps)):
		if deps[k][2] == cur and deps[k][1] not in visited:
			find_unlabeled_path(deps[k][1], end, deps, visited, cur_path + " ^ ", path)
		elif deps[k][1] == cur and deps[k][2] not in visited:
			find_unlabeled_path(deps[k][2], end, deps, visited, cur_path + " v ", path)
	visited.remove(cur)


def get_timex_token(sent, doc_info, tid):
	return get_token_from_id(doc_info, sent, tid)

def get_event_token(sent, doc_info, eiid):
	#loop over the makeinstances to find the eid
	eid = ""
	for m_instance in doc_info['makeinstance'].keys():
		if 'eiid' in doc_info['makeinstance'][m_instance].keys():
			if doc_info['makeinstance'][m_instance]['eiid'] == eiid:
				eid = m_instance
				break
	return get_token_from_id(doc_info, sent, eid)

#actualy calls get_token_from_char to get the token number
def get_token_from_id(doc_info, sent, e_or_t_id):
	char_offset = ""
	#now loop over the offsets, using the eid
	for offset in doc_info['offset'].keys():
		if doc_info['offset'][offset] == e_or_t_id:
			char_offset = offset
			break
	return get_token_from_char(sent, char_offset)

def get_token_from_char(sent, char_offset):
	#finally loop over the char_offsets
	token_num = -1
	for i in range(len(sent['char_offsets'])):
		#to get the character offset of the first character of token i
		if sent['char_offsets'][i][0] == char_offset:
			return i
	return -1
