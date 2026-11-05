package ir.anis.reader;

import android.app.Instrumentation;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;
import android.widget.*;

/** Device checks for the extracted book and real reading/navigation actions. No test dependencies. */
public class ReaderChecks extends Instrumentation {
    int checks=0;
    void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
    View find(View v,String description){if(description.equals(v.getContentDescription()))return v;if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View f=find(g.getChildAt(i),description);if(f!=null)return f;}}return null;}
    boolean capture;
    @Override public void onCreate(Bundle args){super.onCreate(args);capture=args!=null && "true".equals(String.valueOf(args.get("capture")));start();}
    void preview(String name) throws Exception {waitForIdleSync();Thread.sleep(200);android.graphics.Bitmap b=getUiAutomation().takeScreenshot();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getCacheDir(),name))){b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}b.recycle();}
    @Override public void onStart(){Bundle result=new Bundle();try{
        BookRepository r=new BookRepository(getTargetContext());
        check(r.entries.size()==366,"All original entries");check(r.readable().size()==331,"All 331 reading pages");
        check(r.children(-1).size()==25,"25 top-level topics");
        check(BookRepository.normalize("يَكْى").equals(BookRepository.normalize("یکی")),"Arabic/Persian spelling and diacritics");
        check(!r.search("استغفر الله").isEmpty(),"Arabic text search without diacritics");
        check(r.search("قنوت").size()>0,"Persian title search");check(r.search("zyxnonexistent123").isEmpty(),"No-result search");check(r.search("").isEmpty(),"Empty search");
        Intent launch=new Intent();launch.setClassName(getTargetContext(),"ir.anis.reader.MainActivity");launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity a=(MainActivity)startActivitySync(launch);waitForIdleSync();
        runOnMainSync(()->{
            check(a.screen.equals("home"),"Home opens");
            check(a.authorMedia.images.size()==6,"All six embedded biography images decoded");
            check(a.photoGallery.photos.size()==9,"Nine curated online-source photographs bundled offline");
            for(PhotoGallery.Photo photo:a.photoGallery.photos){
                check(Math.max(photo.width,photo.height)>=700,"Curated source resolution");
                check(photo.sourceUrl.startsWith("https://") && photo.source.length()>0,"Photo source attribution");
                android.graphics.Bitmap decoded=photo.bitmap(a,1920);check(decoded.getWidth()>0 && decoded.getHeight()>0,"Curated photo decodes");
            }
            a.navigate("about");check(a.screen.equals("about"),"Independent app information opens");a.goBack();check(a.screen.equals("settings"),"About returns to settings");
            int[] widths={214,214,214,160,214,214}, heights={346,168,204,120,268,164};
            for(int i=0;i<6;i++)check(a.authorMedia.images.get(i).getWidth()==widths[i] && a.authorMedia.images.get(i).getHeight()==heights[i],"Original image dimensions "+i);
            check(a.authorMedia.parts.size()==13,"Six images remain between seven original HTML sections");
            a.navigate("author");check(find(a.root,"گالری تصاویر مؤلف")!=null,"Author gallery tab is accessible");
            check(a.root.findViewWithTag("author-photo-1")!=null && a.root.findViewWithTag("author-photo-6")!=null,"All inline photos rendered");
            find(a.root,"گالری تصاویر مؤلف").performClick();check(a.authorGallery,"Author gallery opens");
            check(a.root.findViewWithTag("author-photo-6")!=null,"Last gallery photo rendered");
            check(a.root.findViewWithTag("curated-photo-0")!=null && a.root.findViewWithTag("curated-photo-2")!=null,"Curated photo grid rendered");
            android.app.Dialog curated=a.showCuratedPhoto(0);check(curated.isShowing(),"Curated zoomable viewer opens");curated.dismiss();
            android.app.Dialog enlarged=a.showAuthorPhoto(a.authorMedia.images.get(0),1);check(enlarged.isShowing(),"Full-screen original photo opens");enlarged.dismiss();
            a.goBack();check(a.screen.equals("settings"),"Author returns to settings");
            a.navigate("library");check(a.book.children(a.section).size()==25,"Library root");
            a.open(a.book.byId.get(6));check(a.screen.equals("reader")&&a.current==6,"Open original page");
            check(a.prefs.getInt("last",-1)==6,"Last page saved");
            View save=find(a.root,"افزودن نشانک");check(save!=null,"Bookmark action accessible");save.performClick();check(a.marks.contains("6"),"Bookmark saved");
            View remove=find(a.root,"حذف نشانک");check(remove!=null,"Bookmark remove action");remove.performClick();check(!a.marks.contains("6"),"Bookmark removed");
            a.goBack();check(a.screen.equals("library"),"Back returns to source");
            a.prefs.edit().putBoolean("dark",true).commit();a.render();check(a.dark,"Night theme applied");
            a.prefs.edit().putBoolean("dark",false).commit();a.navigate("home");
        });
        if(capture){
            Thread.sleep(2500); // Let bookmark confirmation toasts expire before visual QA.
            runOnMainSync(()->{a.authorGallery=true;a.navigate("author");});preview("gallery-v12.png");
            runOnMainSync(()->a.navigate("about"));preview("about-v12.png");
            runOnMainSync(()->a.navigate("home"));
        }
        Intent splashIntent=new Intent();splashIntent.setClassName(getTargetContext(),"ir.anis.reader.SplashActivity");splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SplashActivity splash=(SplashActivity)startActivitySync(splashIntent);waitForIdleSync();
        check(splash.findViewById(android.R.id.content)!=null,"Photo splash opens");
        try {Thread.sleep(1800);} catch(InterruptedException ignored) {}
        waitForIdleSync();check(splash.isFinishing() || splash.isDestroyed(),"Splash transitions and finishes");
        result.putString("stream","PASS: "+checks+" checks\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable t){result.putString("stream","FAIL: "+t+"\n");finish(Activity.RESULT_CANCELED,result);}}
}
