package ir.anis.reader;

import android.content.Context;
import android.text.Html;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

/** Original HTML stays unchanged; normalization is only used for search and UI labels. */
public final class BookRepository {
    public static final class Entry {
        public final int id, parent;
        public final String title, html, page, plain, searchable;
        Entry(JSONObject j) throws JSONException {
            id=j.getInt("id"); parent=j.getInt("parent"); title=j.getString("title");
            html=j.getString("html"); page=j.getString("page");
            plain=Html.fromHtml(html).toString().trim();
            searchable=normalize(title+" "+plain);
        }
        public boolean isSection() { return html.trim().isEmpty(); }
    }
    public final List<Entry> entries=new ArrayList<>();
    public final Map<Integer,Entry> byId=new HashMap<>();
    public final String about, bio;
    public BookRepository(Context context) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(InputStream in=context.getAssets().open("book.json")) { byte[] b=new byte[8192]; int n; while((n=in.read(b))!=-1)bytes.write(b,0,n); }
        JSONObject data=new JSONObject(bytes.toString(StandardCharsets.UTF_8.name()));
        JSONArray list=data.getJSONArray("entries");
        for(int i=0;i<list.length();i++){ Entry e=new Entry(list.getJSONObject(i)); entries.add(e); byId.put(e.id,e); }
        about=data.getString("about"); bio=data.getString("bio");
    }
    public List<Entry> children(int parent) {
        List<Entry> out=new ArrayList<>(); for(Entry e:entries)if(e.parent==parent)out.add(e); return out;
    }
    public List<Entry> readable(){ List<Entry> out=new ArrayList<>();for(Entry e:entries)if(!e.isSection())out.add(e);return out; }
    public List<Entry> search(String query){
        String n=normalize(query).trim(); List<Entry> out=new ArrayList<>(); if(n.isEmpty())return out;
        String[] terms=n.split("\\s+");
        for(Entry e:entries){ boolean matches=true; for(String term:terms)if(!e.searchable.contains(term)){matches=false;break;} if(matches)out.add(e); }
        return out;
    }
    public static String label(String s){return s.replace('ي','ی').replace('ى','ی').replace('ك','ک');}
    public static String normalize(String s){ return Normalizer.normalize(label(s),Normalizer.Form.NFD).replaceAll("\\p{M}","").replace("ـ","").replace('\u200c',' ').toLowerCase(Locale.ROOT); }
}
