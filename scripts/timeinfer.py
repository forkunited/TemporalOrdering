import re,json,sys
from pprint import pprint
from copy import copy,deepcopy
from datetime import date,timedelta

########## Interval logic

class Interval:
    def __init__(self,s,e):
        self.start = s
        self.end = e
    def is_empty(self):
        return self.start==date(1,1,1)
    def __repr__(self):
        if self==EMPTY_INTERVAL: return "EmptyInterval"
        return "Interval[{},{}]".format(repr_dt(self.start), repr_dt(self.end))
    def __eq__(self, other):
        if other is None: return False
        return self.start==other.start and self.end==other.end

def repr_dt(dt):
    if dt==PAST_INF: return "-Inf"
    if dt==FUTURE_INF: return "+Inf"
    return dt.strftime("%Y-%m-%d")

EMPTY_INTERVAL = Interval(date(1,1,1),date(1,1,1))
PAST_INF = date(2,2,2)
FUTURE_INF = date(9999,12,31)

def next_month_ym(y,m):
    if m==12:
        return y+1, 1
    else:
        return y, m+1

def end_of_month(y,m):
    y2,m2 = next_month_ym(y,m)
    return date(y2,m2,1) - timedelta(days=1)

def delta_day(dt, delta):
    if delta>0 and dt==FUTURE_INF: return dt
    if delta<0 and dt==PAST_INF: return dt
    return dt + timedelta(days=delta)

def normalize_edge(edge):
    if edge['timeMLRelType']=='IS_INCLUDED':
        return {'timeMLRelType': 'INCLUDES', 'sourceId':edge['targetId'], 'targetId':edge['sourceId']}
    if edge['timeMLRelType']=='BEFORE':
        return {'timeMLRelType': 'AFTER', 'sourceId':edge['targetId'], 'targetId':edge['sourceId']}
    return edge

class CantResolveConstraint(Exception): pass

def apply_constraint(relation, leftarg=None, rightarg=None):
    """solve for X, for either:  
        relation(leftarg,X)
        relation(X,rightarg)
    depending which is passed in as None"""

    assert sum([leftarg is None, rightarg is None])==1
    assert relation in ('SIMULTANEOUS','AFTER','INCLUDES'), "only can do normalized relations"
    input_arg = leftarg if leftarg is not None else rightarg
    if input_arg==EMPTY_INTERVAL: return EMPTY_INTERVAL

    if relation=='SIMULTANEOUS':
        return deepcopy(input_arg)
    elif relation=='AFTER':
        if leftarg is None:
            return Interval(rightarg.start, FUTURE_INF)
        elif rightarg is None:
            return Interval(PAST_INF, leftarg.end)
    elif relation=='INCLUDES' and rightarg is None:
        return deepcopy(leftarg)
    elif relation=='INCLUDES' and leftarg is None:
        raise CantResolveConstraint()
    else:
        assert False

def apply_conjunction(int1, int2):
    """solve for X:  X = intersect(int1, int2)"""
    assert sum([int1 is None, int2 is None]) == 0

    if int1==EMPTY_INTERVAL or int2==EMPTY_INTERVAL:
        return EMPTY_INTERVAL
    if int1.end < int2.start or int2.end < int1.start:
        return EMPTY_INTERVAL
    s = max(int1.start, int2.start)
    e = min(int1.end, int2.end)
    return Interval(s,e)

def parse_value(value):
    if value is None: return None
    if re.search(r'^\d{4}$', value):
        year = int(value)
        start,end = date(year,1,1), date(year+1,1,1) - timedelta(days=1)
        return Interval(start,end)
    elif re.search(r'^\d{4}-\d{2}$', value):
        y1,m1 = value.split('-')
        y1=int(y1); m1=int(m1)
        return Interval(date(y1,m1,1), end_of_month(y1,m1))
    elif re.search(r'^\d{4}-\d{2}-\d{2}$', value):
        y,m,d = [int(x) for x in value.split('-')]
        return Interval(date(y,m,d), date(y,m,d))
    elif re.search(r'^\d{4}-\d{2}-\d{2}T', value):
        return parse_value(value.split('T')[0])
    else:
        return None
        
def repr_edge(edge):
    return "{timeMLRelType}({sourceId},{targetId})".format(**edge)


####  Graph inference system

class Node:
    def __init__(self):
        self.value = None
        self.incoming = []
        self.outgoing = []
    def __repr__(self):
        # return "{id}({value}, {n_in} incoming, {n_out} outgoing)".format(
        return "{id}({value})".format(
                n_in=len(self.incoming), n_out=len(self.outgoing), **self.__dict__)


def update_node(node, edge, is_incoming, nodes_by_id):
    if node.type=='time':
        return
    if is_incoming:
        other = nodes_by_id[edge['sourceId']]
        f = lambda oo: apply_constraint(edge['timeMLRelType'], oo, None)
    else:
        other = nodes_by_id[edge['targetId']]
        f = lambda oo: apply_constraint(edge['timeMLRelType'], None, oo)

    print "\n",node,"||",other,"||",repr_edge(edge)

    if other.value is None:
        print "no update"
        return

    try:
        value_from_constraint = f(other.value)
    except CantResolveConstraint:
        print "couldnt resolve constraint"
        return

    updated_value = apply_conjunction(node.value, value_from_constraint)
    # print "cur: {}  AND  constraint: {}  ==>".format(node.value, value_from_constraint, updated_value)
    print "constraint: {}  ==>".format(value_from_constraint)
    node.value = updated_value
    print "new:", node

def print_node_values(nodes_by_id, message):
    print "\n===", message
    for nodeid in sorted(nodes_by_id): 
        print "{}\t{}".format(nodeid, nodes_by_id[nodeid].value)
    print "=== end"

def runinfer(nodes_by_id, niter):
    print_node_values(nodes_by_id, "initial values")
    for itr in range(niter):
        saved_values = {n.id: n.value for n in nodes_by_id.values()}
        for node in sorted(nodes_by_id.values()):
            if node.type=='time': continue
            for edge in node.incoming:
                update_node(node,edge,True, nodes_by_id)
            for edge in node.outgoing:
                update_node(node,edge,False, nodes_by_id)
        new_values = {n.id:n.value for n in nodes_by_id.values()}
        if saved_values==new_values:
            print "CONVERGED in {} iters".format(itr)
            break
        print_node_values(nodes_by_id, "after iter %s" % itr)
    print_node_values(nodes_by_id, "final at iter %s" % (itr-1))

def read_graph(alldat, only_timeevent_links=False, exclude_after=False):
    edges = alldat['tlinks']
    print "{} edges in data".format(len(edges))
    relation_whitelist = ('BEFORE','AFTER','SIMULTANEOUS','INCLUDES','IS_INCLUDED') if not exclude_after else ('SIMULTANEOUS','INCLUDES','IS_INCLUDED')
    edges = [e for e in edges if e['timeMLRelType'] in relation_whitelist]
    edges = [normalize_edge(e) for e in edges]
    print "{} edges after reltype filter".format(len(edges))


    evts = [alldat['creationTime']]
    for sent in alldat['sentences']:
        evts += sent['events']
        evts += sent['times']
    # pprint(evts)

    nodes = []
    for evt in evts:
        if not evt['id']:
            print "SKIPPING",evt
            continue
        node = Node()
        node.id = evt['id']
        node.type = 'time' if node.id.startswith("t") else 'event' if node.id.startswith("ei") else None
        assert node.type is not None, repr(evt)
        if node.type=='time':
            node.value = parse_value(evt.get('value'))
            node.orig_value = node.value
        elif node.type=='event':
            node.value = Interval(PAST_INF, FUTURE_INF)

        nodes.append(node)

    # start with 'edges' set.
    nodes_by_id = {n.id:n for n in nodes if n.type=='event' or (n.type=='time' and n.orig_value is not None)}

    for edge in edges:
        if not (edge['sourceId'] in nodes_by_id and edge['targetId'] in nodes_by_id):
            continue
        if only_timeevent_links:
            type1 = nodes_by_id[edge['sourceId']].type
            type2 = nodes_by_id[edge['targetId']].type
            if (type1,type2) not in [('time','event'),('event','time')]:
                continue
        if edge['sourceId'] in nodes_by_id:
            nodes_by_id[edge['sourceId']].outgoing.append(edge)
        if edge['targetId'] in nodes_by_id:
            nodes_by_id[edge['targetId']].incoming.append(edge)

    # nodes_by_id = {n.id:n for n in nodes if n.incoming or n.outgoing}
    edges = [e for e in edges if e['sourceId'] in nodes_by_id and e['targetId'] in nodes_by_id]
    print "{} nodes, {} edges".format(len(nodes_by_id), len(edges))

    # print "Nodes:"
    # pprint(nodes_by_id)
    return nodes_by_id

def dump():
    alldat = json.loads(sys.stdin.read())
    v = alldat['creationTime']['value']
    print v, parse_value(v)

    for sent in alldat['sentences']:
        for evt in (sent['events'] + sent['times']):
            v = evt.get('value')
            if v:
                print evt['id'], "||", v, "||", parse_value(v)

def print_textview(nodes_by_id, alldat):
    out = sys.stdout
    dct = nodes_by_id.get('t0')
    value = dct.value if dct else None
    out.write("DCT: %s\n" % value)
    for snum,sent in enumerate(alldat['sentences']):
        out.write("(S%d)\n" % snum)
        tokens = sent['tokens']
        chunks = get_chunks(sent)
        for (start,end),evt in chunks:
            if evt is not None:
                node = nodes_by_id.get(evt['id'])
                value = node and node.value
                value = repr(value) if value is not None else ""
                info = "%-10s %s" % (evt['id'], value)
            else:
                info = ""
            out.write("%-45s " % info)
            out.write(" ".join(tokens[start:end]))
            out.write("\n")

def get_chunks(sent):
    tokens = sent['tokens']
    N = len(tokens)
    chunks = []
    in_spans = [False for i in range(N)]

    for evt in (sent['events'] + sent['times']):
        span = (evt['tokenSpan']['startTokenIndex'], evt['tokenSpan']['endTokenIndex'])
        chunks.append( (span, evt) )
        for i in range(span[0],span[1]):
            in_spans[i] = True
    i=0
    while i<N:
        if not in_spans[i]:
            for j in range(i,N):
                if in_spans[j]: break
            endpoint = N if j==N-1 else j
            chunks.append( ((i,endpoint),None) )
            i = endpoint
        i += 1
    chunks.sort()
    return chunks

def goinfer(niter, *args):
    alldat = json.loads(sys.stdin.read())
    niter=int(niter)
    nodes_by_id = read_graph(alldat, only_timeevent_links='-onehop' in args, exclude_after='-noafter' in args)
    runinfer(nodes_by_id, niter=niter)
    print "=== text view"
    print_textview(nodes_by_id, alldat)

eval(sys.argv[1])(*sys.argv[2:])



