exec(open('/tmp/swfx/t.py').read())
from vni import vni
parent,sl=pickle.load(open('/tmp/swfx/tree.pkl','rb'))
import collections
def chain(c,seen=None):
    """return list of paths (list of (owner,label,frame)) from c up to root"""
    if seen is None: seen=set()
    res=[]
    for (o,lab,fr) in parent.get(c,()):
        if o=='root': res.append([('root',lab,fr)]); continue
        key=o
        if isinstance(o,str): o=int(o[3:])
        if o in seen: continue
        seen.add(o)
        for p in chain(o,seen): res.append(p+[(key,lab,fr)])
    return res
