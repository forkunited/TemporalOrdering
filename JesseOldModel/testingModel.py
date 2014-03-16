#to find out how many events have more than one temporal phrase grounding them 
import json,sys,operator,readingTimeBankXML,proc,math


def add_counter(counter, s):
	if s not in counter:
		counter[s] = 1
	else:
		counter[s] = counter[s] + 1


def add_time_and_event(t, e, time_to_events):
	if not t in time_to_events:
		time_to_events[t] = e
	else:
		time_to_events[t] = [time_to_events[t], e]

def get_event_id_from_instance(instance, makeinstance):
	for event in makeinstance.keys():
		if makeinstance[event]['eiid'] == instance:
			return event

def find_offset(e, offset):
	for o in offset.keys():
		if offset[o] == e:
			return o

def find_char_diff(i_one, i_two, offset, makeinstance):
	e_one = get_event_id_from_instance(i_one, makeinstance)
	e_two = get_event_id_from_instance(i_two, makeinstance)
	return math.fabs(find_offset(e_one, offset) - find_offset(e_two, offset))

doc_info = readingTimeBankXML.get_doc_info()
#keys = set()
counters = {}
two_events_docs = {}
for doc in doc_info.keys():
	cur_doc = doc_info[doc]['tlinks']
	e_included_counter = {}
	t_included_counter = {}
	e_total = {}
	time_to_events = {}
	for l in cur_doc:
		if l['relType'] == 'IS_INCLUDED':
			if 'eventInstanceID' in l.keys() and 'relatedToTime' in l.keys():
				add_counter(e_included_counter, l['eventInstanceID'])
				add_counter(t_included_counter, l['relatedToTime'])
				add_time_and_event(l['relatedToTime'], l['eventInstanceID'], time_to_events)
			elif 'relatedToEventInstance' in l.keys() and 'timeID' in l.keys():
				add_counter(e_included_counter, l['relatedToEventInstance'])
				add_counter(t_included_counter, l['timeID'])
				add_time_and_event(l['timeID'], l['relatedToEventInstance'], time_to_events)
	counters[doc] = {}
	counters[doc]['events'] = e_included_counter
	counters[doc]['times'] = t_included_counter
	two_events_docs[doc] = time_to_events

num_e = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
num_t = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
max_e = 0
max_t = 0
distance = {}
#histogram of the distance between two events that link to the same time.
for doc in counters.keys():
	#to compute the number of times and events with more than one IS_INCLUDED tlink:
	for e in counters[doc]['events'].keys():
	#	if max_e < counters[doc]['events'][e]:
	#		max_e = counters[doc]['events'][e]
		num_e[counters[doc]['events'][e]] += 1
		if counters[doc]['events'][e] == 2:
			print 'event with ' + `counters[doc]['events'][e]` + ": ", e + " " + doc
	for t in counters[doc]['times'].keys():
	#	if max_t < counters[doc]['times'][t]:
	#		max_t = counters[doc]['times'][t]
		if not t == 't0':
			num_t[counters[doc]['times'][t]] += 1
			if counters[doc]['times'][t] == 2:
				diff = find_char_diff(two_events_docs[doc][t][0], two_events_docs[doc][t][1], doc_info[doc]['offset'], doc_info[doc]['makeinstance'])
				add_counter(distance, diff)
			if counters[doc]['times'][t] > 3:
				print 'time with ' + `counters[doc]['times'][t]` + ": ", t + " " + doc





