#!/usr/bin/env python3
"""Decode v1/v2 diagnostic captures. CLI prints aggregates only, never coordinates."""
import base64, collections, gzip, json, sys

def changes(row):
    if row.get('event')=='block_update':
        yield row['x'],row['y'],row['z'],row['block_id']
    elif row.get('event')=='section_update_batch':
        x,y,z=(int(row['section_'+axis])<<4 for axis in 'xyz')
        for group in row.get('palette',[]):
            data=base64.b64decode(group['cells_b64'],validate=True)
            if group['encoding']=='u16le':
                if len(data)%2:raise ValueError('Odd u16 cell data')
                cells=[data[i]|data[i+1]<<8 for i in range(0,len(data),2)]
            elif group['encoding']=='bitmap-le':
                if len(data)!=512:raise ValueError('Section bitmap must have 512 bytes')
                cells=[i for i in range(4096) if data[i>>3]&(1<<(i&7))]
            else:raise ValueError('Unknown cell encoding')
            if len(cells)!=group['count']:raise ValueError('Cell count mismatch')
            for cell in cells:
                if not 0<=cell<4096:raise ValueError('Invalid section cell')
                yield x+(cell>>8),y+(cell&15),z+((cell>>4)&15),group['block_id']

def summarize(path):
    events=collections.Counter();blocks=collections.Counter();footer=None
    with gzip.open(path,'rt',encoding='utf-8') as stream:
        for line in stream:
            row=json.loads(line);events[row.get('event')]+=1
            if row.get('event')=='summary':footer=row
            for _,_,_,block in changes(row):blocks[block]+=1
    return {'events':dict(events),'block_ids':dict(blocks),'summary':footer,'coordinates_exported':False}

if __name__=='__main__':
    if len(sys.argv)!=2:raise SystemExit('Usage: python3 tools/read_capture.py capture.jsonl.gz')
    print(json.dumps(summarize(sys.argv[1]),ensure_ascii=False,indent=2))
