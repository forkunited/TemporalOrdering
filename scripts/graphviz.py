import sys,json
alldat = json.loads(sys.stdin.read())

times_by_id = {}
times_by_id['t0'] = alldat['creationTime']
for sent in alldat['sentences']:
    for t in sent['times']:
        times_by_id[t['id']] = t

def cleanup(rel):
    return rel.replace("BEFORE","BEF").replace("AFTER","AFT").replace("SIMULTANEOUS","SIM").replace("INCLUDES","INC").replace("IS_INCLUDED","IS_INC")

def normalize_edge(edge):
    if edge['timeMLRelType']=='IS_INCLUDED':
        return {'timeMLRelType': 'INCLUDES', 'sourceId':edge['targetId'], 'targetId':edge['sourceId']}
    if edge['timeMLRelType']=='BEFORE':
        return {'timeMLRelType': 'AFTER', 'sourceId':edge['targetId'], 'targetId':edge['sourceId']}
    return edge

colors = {
        'AFTER': '#700070',
        'SIMULTANEOUS': '#C35617',
        'INCLUDES': '#207020',
}

print "digraph {"

seen_nodes = set()

for edge in alldat['tlinks']:
    if edge['timeMLRelType']=='VAGUE': continue
    edge = normalize_edge(edge)
    seen_nodes.add(edge['sourceId'])
    seen_nodes.add(edge['targetId'])
    print """{src} -> {tgt} [label={label} color="{col}" fontcolor="{col}"];""".format(
            src=edge['sourceId'],
            tgt=edge['targetId'], 
            # dirtype= "--" if edge['timeMLRelType']=='SIMULTANEOUS' else "->",
            label=cleanup(edge['timeMLRelType']),
            col=colors[edge['timeMLRelType']])

for node in seen_nodes:
    if node.startswith('t'):
        print """ {id} [shape=box label="{id} {value}"]""".format(
            id=node,  value=times_by_id[node]['value'])
    else:
        print """{id}""".format(id=node)


print "}"
