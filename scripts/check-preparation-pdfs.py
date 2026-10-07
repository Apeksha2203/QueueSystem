"""Check extracted text and render contact sheets for manual visual QA."""
from pathlib import Path
import json
from pypdf import PdfReader
import pypdfium2 as pdfium
from PIL import Image, ImageDraw

root=Path(__file__).resolve().parents[1]
temporary=root/'tmp'/'pdfs';temporary.mkdir(parents=True,exist_ok=True)
report=[]
for filename in sorted((root/'output'/'pdf').glob('Campus-Queue-*.pdf')):
    reader=PdfReader(filename)
    texts=[page.extract_text() or '' for page in reader.pages]
    assert all(t.strip() for t in texts),'Empty page found'
    combined='\n'.join(texts)
    for required in ['Campus Queue','10','Netlify','SQL']:
        assert required.lower() in combined.lower(),required
    forbidden=['1campusqueue','campus2queue','campusqueue3','TestQueue8demo','QUEUE_DB_PASSWORD=']
    assert not any(x in combined for x in forbidden),'Credential value found'
    document=pdfium.PdfDocument(str(filename))
    if 'Master' in filename.name:
        indices=sorted(set([0,1,3,10,20,32,40,55,len(texts)//2,len(texts)-15,len(texts)-4,len(texts)-1]))
    else:indices=sorted(set([0,1,2,3,5,8,len(texts)//2,len(texts)-3,len(texts)-2,len(texts)-1]))
    indices=[i for i in indices if i<len(texts)]
    sheets=[]
    for start in range(0,len(indices),6):
        group=indices[start:start+6]
        sheet=Image.new('RGB',(1050,1060),'#dce2e8');draw=ImageDraw.Draw(sheet)
        for slot,index in enumerate(group):
            page=document[index];im=page.render(scale=0.7).to_pil().convert('RGB');im.thumbnail((330,485))
            x=15+(slot%3)*350;y=25+(slot//3)*530
            draw.text((x,y-18),f'Page {index+1}',fill='black');sheet.paste(im,(x,y))
        target=temporary/(filename.stem+f'-sheet-{start//6+1}.png');sheet.save(target);sheets.append(str(target))
    report.append({'file':filename.name,'pages':len(texts),'words':len(combined.split()),'bytes':filename.stat().st_size,'rendered_pages':[i+1 for i in indices],'contact_sheets':sheets})
(temporary/'pdf-quality-report.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
