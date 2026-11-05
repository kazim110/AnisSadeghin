package ir.anis.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.*;
import java.io.*;
import java.util.*;

/** Curated photos are bundled offline; every photo retains its source attribution. */
public final class PhotoGallery {
    public static final class Photo {
        public final String file, title, source, sourceUrl, imageUrl;
        public final int width, height;
        Photo(JSONObject j) throws JSONException {
            file=j.getString("file"); title=j.getString("title"); source=j.getString("source");
            sourceUrl=j.getString("sourceUrl"); imageUrl=j.getString("imageUrl");
            width=j.getInt("width"); height=j.getInt("height");
        }
        public Bitmap bitmap(Context context,int maxEdge) {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=1;
            while(Math.max(width,height)/options.inSampleSize>maxEdge*1.5f) options.inSampleSize*=2;
            try(InputStream in=context.getAssets().open(file)) {
                Bitmap bitmap=BitmapFactory.decodeStream(in,null,options);
                if(bitmap==null)throw new IllegalStateException("Invalid gallery image: "+file);
                return bitmap;
            }catch(IOException e){throw new IllegalStateException(e);}
        }
    }
    public final List<Photo> photos;
    public PhotoGallery(Context context) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(InputStream in=context.getAssets().open("gallery.json")){
            byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);
        }
        JSONArray data=new JSONArray(bytes.toString("UTF-8"));
        List<Photo> result=new ArrayList<>();
        for(int i=0;i<data.length();i++)result.add(new Photo(data.getJSONObject(i)));
        photos=Collections.unmodifiableList(result);
    }
}
