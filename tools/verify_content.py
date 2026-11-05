#!/usr/bin/env python3
"""Compare every extracted title, body and relationship with a user-supplied original APK."""
import gzip,json,pathlib,sqlite3,sys,tempfile,zipfile
with tempfile.TemporaryDirectory() as t:
 p=pathlib.Path(t)/'anis.db';p.write_bytes(zipfile.ZipFile(sys.argv[1]).read('assets/data/anis.db'));c=sqlite3.connect(p)
 data=json.loads((pathlib.Path(__file__).resolve().parents[1]/'app/src/main/assets/book.json').read_text())
 rows=c.execute('select ID,Title,ParID,PageNo,Next,Text from BookContent where BookID=1 order by ID').fetchall()
 assert len(rows)==len(data['entries'])==366
 for a,b in zip(rows,data['entries']):
  assert a[0]==b['id'] and a[2]==b['parent'] and a[3]==b['page'] and a[4]==b['next']
  assert gzip.decompress(a[1]).decode('cp1256')==b['title']
  assert gzip.decompress(a[5]).decode('cp1256')==b['html']
 print('PASS: 366 entries match the original APK exactly.')
