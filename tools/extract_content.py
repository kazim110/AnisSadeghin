#!/usr/bin/env python3
"""Re-extract the original text losslessly. Usage: python3 tools/extract_content.py original.apk"""
import sys,zipfile,tempfile,sqlite3,gzip,json,pathlib
with tempfile.TemporaryDirectory() as t:
 p=pathlib.Path(t)/'anis.db'; p.write_bytes(zipfile.ZipFile(sys.argv[1]).read('assets/data/anis.db')); c=sqlite3.connect(p)
 entries=[dict(id=r[0],title=gzip.decompress(r[1]).decode('cp1256'),parent=r[2],page=r[3],next=r[4],html=gzip.decompress(r[5]).decode('cp1256')) for r in c.execute('select ID,Title,ParID,PageNo,Next,Text from BookContent where BookID=1 order by ID')]
 data=dict(entries=entries)
 for i,k in [(16,'about'),(17,'bio')]: data[k]=gzip.decompress(c.execute('select Text from BookContent where BookID=2 and ID=?',(i,)).fetchone()[0]).decode('cp1256')
 dest=pathlib.Path(__file__).resolve().parents[1]/'app/src/main/assets/book.json';dest.write_text(json.dumps(data,ensure_ascii=False),encoding='utf-8')
 print(f'{len(entries)} entries extracted')
