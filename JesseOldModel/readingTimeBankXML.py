import xml.dom.minidom
import glob, sys, errno
import xml.etree.ElementTree as ET


#possible_links = set()
#event_to_time_rels = set()
#time_to_event_rels = set()
#event_to_time_counter = {"c":0}
#time_to_event_counter = {"c":0}
def read_docs(tree, doc_name):
	#print len(tree.findall("*"))
	#print tree.findall("*")[4].tag
	#tree.write("tmpoutput.txt")
	#print tree.findall("TEXT")[0].text,
	s = ""

	offset = {}
	for doc_text in tree.findall("TEXT"):
		s += doc_text.text
		#sys.stdout.write(doc_text.text)
		for e_and_t in doc_text.findall("*"):
			if "tid" in e_and_t.attrib.keys():
				offset[len(s)] = e_and_t.attrib["tid"]
			else:
				offset[len(s)] = e_and_t.attrib["eid"]
			#sys.stdout.write("[" + str(len(s)) + "]" + e_and_t.text + e_and_t.tail)
			s += e_and_t.text.lstrip(' ') + e_and_t.tail
	#print s
	tlinks = []
	makeinstance = {}
	for tl in tree.findall("TLINK"):
		#print tl.attrib.keys()
		#if "eventInstanceID" in tl.attrib.keys() and "relatedToTime" in tl.attrib.keys():
		#	event_to_time_rels.add(tl.attrib["relType"])
		#	if tl.attrib["relType"] == "IS_INCLUDED":
		#		event_to_time_counter["c"] += 1
		#if "timeID" in tl.attrib.keys() and "relatedToEventInstance" in tl.attrib.keys():
		#	time_to_event_rels.add(tl.attrib["relType"])
		#	if tl.attrib["relType"] == "IS_INCLUDED":
		#		time_to_event_counter["c"] += 1
		#for k in tl.attrib.keys():
		#	possible_links.add(k)
		tlinks.append(tl.attrib)
	for mi in tree.findall("MAKEINSTANCE"):
		makeinstance[mi.attrib["eventID"]] = mi.attrib

	
	return {"offset":offset, "tlinks":tlinks, "makeinstance":makeinstance}

def get_doc_info():
	read_test_XML = False
	documents = {}
	
	if not read_test_XML:
		path = '/home/jesse/Dropbox/CMU/exploratory_research/tempeval3/TBAQ-cleaned/TimeBank/*.tml'
	else:
		path = '/home/jesse/Dropbox/CMU/exploratory_research/internationalRelationsEvents/timerel/tmpoutput.txt'
	files = glob.glob(path)
	for name in files:
		try:
			#pretty_print(xml.dom.minidom.parse(name))
			documents[name.split("/")[len(name.split("/"))-1][:-4]] = read_docs(ET.parse(name), name)
		except IOError as exc:
			if exc.errno != errno.EISDIR: # Do not fail if a directory is found, just ignore it.
				raise # Propagate other kinds of IOError.
	#print possible_links
	#print "Number of event to time relations: ", event_to_time_counter
	#print event_to_time_rels
	#print "Number of time to event relations: ", time_to_event_counter
	#print time_to_event_rels
	return documents
