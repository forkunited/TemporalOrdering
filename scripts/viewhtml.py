"""
Give one JSON dense-timebank file on stdin.
Output html on stdout.
"""

import sys,json
alldat = json.loads(sys.stdin.read())

def printheader():
    # print r"""<script src="file:///d/timebank/new/jquery-2.1.0.min.js""></script>"""
    print r"""<script src="https://code.jquery.com/jquery-2.1.0.min.js"></script>"""
    print r"""
    <script>

$(document).ready(function() {
    $("a.ref").hover(
        function(e) {
            var r = $(this).attr("ref");
            if (r) {
                $("a[ref='" + r + "']").css("background","yellow");
            }
        },
        function(e) {
            var r = $(this).attr("ref");
            if (r) {
                $("a[ref='" + r + "']").css("background","");
            }
        }
    );
});

    </script>
    <style>
.relname { font-family: helvetica; font-size: 9.5pt; }
td { vertical-align: top }
td { border-top: 1px solid gray; }
td.middle { min-width: 250px; padding-left:10px; }
td.fillertext { text-align:right; padding-top:1em; padding-right: 10px;}
td.links { min-width: 300px; }
.value { color:red; font-weight: bold; font-size:13pt; }
.moreinfo { font-family: helvetica; font-size: 9pt; }
.container { display:inline }
.senttext { display: inline; margin-left: 20px; }
.links { margin-left: 40px; }
a { text-decoration:none; }
a:hover { text-decoration:underline; }
.event { font-family: helvetica; font-size: 12pt; color:#558; }
.time { font-family: helvetica; font-size: 12pt; color:#855; }
.id { font-size:12pt; }
/* Dark2 from http://colorbrewer2.org/ */
.c1 { color: rgb(27,158,119); }
.c2 { color: rgb(217,95,2); }
.c3 { color: rgb(117,112,179); }
.c4 { color: rgb(231,41,138); }
.c5 { color: rgb(102,166,30); }
.c6 { color: rgb(230,171,2); }
.c7 { color: rgb(166,118,29); }
.c8 { color: rgb(102,102,102); }
    </style>
    """

def text_position(event_id):
    evt = events_by_id[event_id]
    return evt['tokenSpan']['startTokenIndex']

def get_links(evt):
    links = [t for t in alldat['tlinks'] if evt['id'] in (t['sourceId'], t['targetId'])]
    links.sort(key=lambda t: text_position( (set([t['sourceId'], t['targetId']]) - set([evt['id']])).pop() ))
    return links

def getphrase(tokens,span):
    return u' '.join(tokens[span['startTokenIndex']:span['endTokenIndex']])

def print_tlink(tlink):
    return "<span class=relname>{rel}</span>({srchtml}, {tgthtml})".format(
            rel=tlink['timeMLRelType'], 
            srchtml=hyperlink(events_by_id[tlink['sourceId']]),
            tgthtml=hyperlink(events_by_id[tlink['targetId']]),)
    # return "{timeMLRelType}({sourceId}, {targetId})".format(**tlink)

def getclass(evt):
    return "time" if evt['id'].startswith('t') else "event"

def hyperlink(evt):
    """event or timex"""
    klass = getclass(evt)
    return """<a href="#{evt[id]}" ref="{evt[id]}" class="ref {klass}">{evt[id]}</a>""".format(**locals())


printheader()

events_by_id = {}

events_by_id[alldat['creationTime']['id']] = alldat['creationTime']

for sent in alldat['sentences']:
    for evt in sent['events']:
        events_by_id[evt['id']] = evt
    for time in sent['times']:
        events_by_id[time['id']] = time


print """<table> <tr><td class=fillertext>"""

evt = alldat['creationTime']
klass = "time"
print """DOCUMENT CREATION TIME <td class=middle>"""
print "<span class='{klass}'><a class='ref' rel='{evt[id]}' name='{evt[id]}'>{evt[id]}</a></span>".format(**locals())
print "<br>"
print "<span class=value>{}</span>".format( evt['value'] )
d = {k:v for k,v in time.items() if k not in ('id','tokenSpan','value')}
print "<br><span class=moreinfo>{}</span>".format(json.dumps(d))
print "<td class=links>"
print "<div class=links>"
for tlink in get_links(evt):
    if tlink['timeMLRelType'] == 'VAGUE': continue
    print print_tlink(tlink),"<br>"
print "</div>"
print "<tr><td class=fillertext>"

for snum,sent in enumerate(alldat['sentences']):
    print """<span class=sentid>(S{})</span>""".format(snum)

    tokens = sent['tokens']
    N = len(tokens)

    chunks = []
    in_spans = [False for i in range(N)]

    for evt in (sent['events'] + sent['times']):
        span = (evt['tokenSpan']['startTokenIndex'], evt['tokenSpan']['endTokenIndex'])
        chunks.append( (span, evt) )
        for i in range(span[0],span[1]):
            in_spans[i] = True
    for i in range(N):
        if not in_spans[i]:
            chunks.append( ((i,i+1),None) )
    chunks.sort()

    for span,evt in chunks:
        if evt is not None:
            print "<td class=middle>"
            klass = "time" if evt['id'].startswith('t') else "event"
            print "<span class='{klass}'>[<a class='ref' ref='{evt[id]}' name='{evt[id]}'>{evt[id]}</a>]</span>".format(**locals())
            print "<br>"
        print u' '.join(tokens[span[0]:span[1]])
        if evt is not None:
            # print "<span class='{klass}'></span>"
            if klass=='time':
                print "<br>"
                print "<span class=value>{}</span>".format( evt['value'] )
                d = {k:v for k,v in time.items() if k not in ('id','tokenSpan','value')}
            elif klass=='event':
                d = {k:v for k,v in evt.items() if k not in ('id','sourceId','tokenSpan')}
            print "<br><span class=moreinfo>{}</span>".format(json.dumps(d))
            print "<td class=links>"
            print "<div class=links>"
            # for tlink in [t for t in alldat['tlinks'] if evt['id'] in (t['sourceId'], t['targetId'])]:
            for tlink in get_links(evt):
                if tlink['timeMLRelType'] == 'VAGUE': continue
                print print_tlink(tlink),"<br>"
            print "</div>"
            print "<tr><td class=fillertext>"
print "</table>"

