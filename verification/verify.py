import re,sys,collections
cls={};meth=collections.defaultdict(list);fld={}
for l in open('/home/user/work/map/1.21.11.tiny'):
    p=l.rstrip('\n').split('\t')
    if p[0]=='CLASS': cls[p[2].split('/')[-1].split('$')[-1]]=p[1]; cls[p[2].split('/')[-1]]=p[1]
    elif p[0]=='METHOD': meth[p[4]].append((p[1],p[2]))
    elif p[0]=='FIELD': fld[p[4]]=(p[1],p[2])
obf2int={v:k for k,v in cls.items()}
def nargs(desc):
    a=desc[1:desc.index(')')];n=0;i=0
    while i<len(a):
        while a[i]=='[': i+=1
        if a[i]=='L': i=a.index(';',i)
        i+=1;n+=1
    return n
def callargs(s,i):
    depth=1;n=0;any_=False;j=i
    while depth:
        c=s[j]
        if c=='"':
            j+=1
            while s[j]!='"':
                if s[j]=='\\': j+=1
                j+=1
            any_=True
        elif c in '([{': depth+=1;any_=True
        elif c in ')]}':
            depth-=1
        elif c==',' and depth==1: n+=1
        elif c=='<' : pass
        elif not c.isspace(): any_=True
        j+=1
    return n+1 if any_ else 0
bad=0
for f in sys.argv[1:]:
    s=open(f).read(); s=re.sub(r'//[^\n]*','',s)
    for m in re.finditer(r'\b(class_\d+|method_\d+|field_\d+|comp_\d+)\b(\s*\()?',s):
        t=m.group(1);ln=s.count('\n',0,m.start())+1
        if t.startswith('class_'):
            if t not in cls: print(f.split('/')[-1],ln,'MISSING CLASS',t);bad+=1
        elif t.startswith('field_'):
            if t not in fld: print(f.split('/')[-1],ln,'MISSING FIELD',t);bad+=1
        else:
            if t not in meth and not (t.startswith('comp_') and t in fld): print(f.split('/')[-1],ln,'MISSING METHOD',t);bad+=1;continue
            if m.group(2) and s[m.start()-1]=='.':
                n=callargs(s,m.end())
                # generic commas inside lambdas may confuse; report only if no overload matches
                if all(nargs(d)!=n for _,d in meth[t]):
                    print(f.split('/')[-1],ln,'ARG COUNT',t,'call has',n,'mapping',[d for _,d in meth[t]]);bad+=1
print('files',len(sys.argv)-1,'problems',bad)
