import unicodedata
TONE={'ù':'́','ø':'̀','û':'̉','õ':'̃','ï':'̣','Ù':'́','Ø':'̀','Û':'̉','Õ':'̃','Ï':'̣'}
CIRC={'â':'','á':'́','à':'̀','å':'̉','ã':'̃','ä':'̣','Â':'','Á':'́','À':'̀','Å':'̉','Ã':'̃','Ä':'̣'}
BREV={'ê':'','é':'́','è':'̀','ú':'̉','ü':'̃','ë':'̣','Ê':'','É':'́','È':'̀','Ú':'̉','Ü':'̃','Ë':'̣'}
SINGLE={'ñ':'đ','Ñ':'Đ','ô':'ơ','Ô':'Ơ','ö':'ư','Ö':'Ư','æ':'ỉ','Æ':'Ỉ','ó':'ĩ','Ó':'Ĩ','ò':'ị','Ò':'Ị','î':'ỵ','Î':'Ỵ'}
def vni(s):
    out=[]
    for ch in s:
        prev=out[-1] if out else ''
        base=prev[-1:].lower() if prev else ''
        if ch in CIRC and base in ('a','e','o'):
            out[-1]=prev+'̂'+CIRC[ch]; continue
        if ch in BREV and base=='a':
            out[-1]=prev+'̆'+BREV[ch]; continue
        if ch in TONE and base in ('a','e','i','o','u','y','ơ','ư') or (ch in TONE and prev and unicodedata.normalize('NFD',prev[-1])[0].lower() in 'aeiouy'):
            out[-1]=prev+TONE[ch]; continue
        out.append(SINGLE.get(ch,ch))
    return unicodedata.normalize('NFC',''.join(out))
