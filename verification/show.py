import re,sys,collections
cls={};o2i={};meth=collections.defaultdict(list);fld=collections.defaultdict(list)
for l in open('/home/user/work/map/1.21.11.tiny'):
    p=l.rstrip('\n').split('\t')
    if p[0]=='CLASS': o2i[p[1]]=p[2].split('/')[-1]
    elif p[0]=='METHOD': meth[p[4]].append((p[1],p[2]))
    elif p[0]=='FIELD': fld[p[4]].append((p[1],p[2]))
def tr(d): return re.sub(r'L([^;]+);',lambda m:'L'+o2i.get(m.group(1),m.group(1).split('/')[-1])+';',d)
for t in sys.argv[1:]:
    for o,d in meth.get(t,[]): print(t,'in',o2i.get(o,o),tr(d))
    for o,d in fld.get(t,[]): print(t,'FIELD in',o2i.get(o,o),tr(d))
    if t not in meth and t not in fld: print(t,'NOT FOUND')
