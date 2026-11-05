#!/usr/bin/env python3
"""Produce standard Android launcher icon densities from the app's existing portrait and palette."""
from pathlib import Path
from PIL import Image, ImageDraw
root=Path(__file__).resolve().parents[1]
res=root/'app/src/main/res'
photo=Image.open(res/'drawable-nodpi/author_portrait.jpg').convert('RGB')
# The source is preserved. Only the launcher frame selects the face from the wide photograph.
face=photo.crop((340,18,1050,728))
def foreground(size):
 image=Image.new('RGBA',(size,size),(0,0,0,0));draw=ImageDraw.Draw(image)
 outer=round(size/6);edge=round(size*2/3)
 draw.ellipse((outer-3,outer-3,outer+edge+3,outer+edge+3),fill='#D3BE85')
 avatar=face.resize((edge,edge),Image.Resampling.LANCZOS)
 mask=Image.new('L',(edge,edge));ImageDraw.Draw(mask).ellipse((0,0,edge-1,edge-1),fill=255)
 image.paste(avatar,(outer,outer),mask)
 return image
(res/'drawable-nodpi/launcher_portrait.png').write_bytes(b'')
foreground(432).save(res/'drawable-nodpi/launcher_portrait.png')
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
 p=res/('mipmap-'+density);p.mkdir(exist_ok=True)
 icon=Image.new('RGB',(size*4,size*4),'#155E50')
 icon.paste(foreground(size*4),(0,0),foreground(size*4))
 icon.resize((size,size),Image.Resampling.LANCZOS).save(p/'ic_launcher.png')
p=res/'mipmap-anydpi-v26';p.mkdir(exist_ok=True)
(p/'ic_launcher.xml').write_text('''<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@color/launcher_background"/><foreground android:drawable="@drawable/launcher_portrait"/></adaptive-icon>''')
(res/'values/colors.xml').write_text('<resources><color name="launcher_background">#155E50</color></resources>')
print('Launcher icons generated in all standard densities and adaptive format.')
