package ir.anis.reader;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.res.ColorStateList;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    BookRepository book;
    android.content.SharedPreferences prefs;
    LinearLayout root, body, bar;
    ScrollView scroll;
    String screen="home", query="", returnScreen="home";
    int section=-1,current=-1, bg, surface, ink, muted, green, line;
    boolean dark;
    Set<String> marks;
    final Handler handler=new Handler(Looper.getMainLooper());
    Runnable pendingSearch;
    Typeface readingFont;
    AuthorMedia authorMedia;
    PhotoGallery photoGallery;
    boolean authorGallery=false;

    @Override public void onCreate(Bundle state){
        super.onCreate(state); prefs=getSharedPreferences("reader",MODE_PRIVATE);
        marks=new HashSet<>(prefs.getStringSet("bookmarks",Collections.emptySet()));
        try {book=new BookRepository(this);authorMedia=new AuthorMedia(book.bio);photoGallery=new PhotoGallery(this);readingFont=Typeface.createFromAsset(getAssets(),"fonts/BMitra.ttf");}
        catch(Exception e){ new AlertDialog.Builder(this,dark?android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK:android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setMessage("بارگذاری کتاب ممکن نشد").setPositiveButton("بستن",(d,w)->finish()).show();return; }
        if(state!=null){screen=state.getString("screen","home");section=state.getInt("section",-1);current=state.getInt("current",-1);query=state.getString("query","");returnScreen=state.getString("returnScreen","home");authorGallery=state.getBoolean("authorGallery",false);}
        render();
    }
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("screen",screen);out.putInt("section",section);out.putInt("current",current);out.putString("query",query);out.putString("returnScreen",returnScreen);out.putBoolean("authorGallery",authorGallery);}
    int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    String fa(int n){return fa(String.valueOf(n));}
    String fa(String s){for(int i=0;i<10;i++)s=s.replace((char)('0'+i),(char)('۰'+i));return s;}
    void palette(){dark=prefs.getBoolean("dark",false);bg=Color.parseColor(dark?"#111F1D":"#F8F7F2");surface=Color.parseColor(dark?"#1D302B":"#FFFFFF");ink=Color.parseColor(dark?"#F0F1E8":"#203B34");muted=Color.parseColor(dark?"#ACBDB4":"#667970");green=Color.parseColor(dark?"#91D4BC":"#17685A");line=Color.parseColor(dark?"#344A42":"#E4E8DF");}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    void rounded(View v,int color,int radius){v.setBackground(shape(color,radius));}
    TextView text(String value,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);v.setTextDirection(View.TEXT_DIRECTION_RTL);v.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));v.setIncludeFontPadding(true);return v;}
    LinearLayout col(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
    LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(0);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    void add(LinearLayout p,View v,int w,int h){p.addView(v,new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h)));}
    void gap(LinearLayout p,int h){add(p,new View(this),1,h);}
    void label(String s){TextView t=text(s,19,ink,true);t.setPadding(0,dp(8),0,dp(14));add(body,t,-1,-2);}
    void buttonEffect(View v){v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);v.setClickable(true);v.setFocusable(true);android.util.TypedValue out=new android.util.TypedValue();getTheme().resolveAttribute(android.R.attr.selectableItemBackground,out,true);v.setForeground(getDrawable(out.resourceId));}
    LinearLayout card(){LinearLayout c=col();c.setPadding(dp(18),dp(16),dp(18),dp(16));rounded(c,surface,22);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(12);body.addView(c,p);return c;}
    void navigate(String target){hideKeyboard();screen=target;section=-1;render();}
    void hideKeyboard(){View f=getCurrentFocus();if(f!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(f.getWindowToken(),0);}
    void render(){
        palette();root=col();root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(bg);root.setFitsSystemWindows(false);setContentView(root);
        getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(surface);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        root.setOnApplyWindowInsetsListener((v,insets)->{root.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});
        root.requestApplyInsets();
        if(screen.equals("about")){aboutApp();return;}
        if(screen.equals("author")){author();return;}
        if(screen.equals("reader")&&book.byId.containsKey(current)){reader();return;}
        String title=screen.equals("home")?"انیس الصادقین":screen.equals("library")?"فهرست کتاب":screen.equals("search")?"جست‌وجو":screen.equals("bookmarks")?"نشانک‌ها":"تنظیمات";
        header(title,null);
        scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);body=col();body.setPadding(dp(22),dp(4),dp(22),dp(20));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        switch(screen){case "home":home();break;case "library":library();break;case "search":search();break;case "bookmarks":bookmarks();break;default:settings();}
        bottom();
    }
    void header(String title,Runnable back){
        LinearLayout h=row();h.setPadding(dp(22),dp(8),dp(14),dp(8));
        if(back!=null){Icon v=icon("back","بازگشت",back);add(h,v,48,48);}
        TextView t=text(title,21,ink,true);h.addView(t,new LinearLayout.LayoutParams(0,dp(56),1));
        if(back==null)add(h,icon("settings","تنظیمات",()->navigate("settings")),48,48);
        add(root,h,-1,-2);
    }
    Icon icon(String type,String desc,Runnable click){Icon i=new Icon(type,green);i.setContentDescription(desc);buttonEffect(i);i.setOnClickListener(v->click.run());return i;}
    void home(){
        TextView greeting=text("لحظه‌ای آرام، در همراهی با دعا",14,muted,false);add(body,greeting,-1,-2);gap(body,18);
        LinearLayout hero=col();hero.setPadding(dp(24),dp(22),dp(24),dp(22));rounded(hero,Color.parseColor("#155E50"),26);
        LinearLayout top=row();TextView over=text("کتاب دعا و ذکر",12,Color.parseColor("#C7DDD1"),false);top.addView(over,new LinearLayout.LayoutParams(0,-2,1));Icon ornament=new Icon("spark",Color.parseColor("#DAC595"));add(top,ornament,30,30);add(hero,top,-1,-2);
        gap(hero,8);add(hero,text("انیس الصادقین",30,Color.parseColor("#FFF8E6"),true),-1,-2);gap(hero,6);
        add(hero,text("توصیه‌هایی در زمینهٔ ادعیه و اذکار\nاز سید حسین یعقوبی قائنی",14,Color.parseColor("#D8E8DD"),false),-1,-2);gap(hero,22);
        LinearLayout cta=row();TextView caption=text("ورود به فهرست کتاب",14,Color.WHITE,true);cta.addView(caption,new LinearLayout.LayoutParams(0,-2,1));add(cta,new Icon("chevron",Color.WHITE),24,24);add(hero,cta,-1,-2);
        buttonEffect(hero);hero.setOnClickListener(v->navigate("library"));add(body,hero,-1,-2);gap(body,20);
        int last=prefs.getInt("last",-1);
        if(book.byId.containsKey(last)){
            label("ادامهٔ مطالعه");BookRepository.Entry e=book.byId.get(last);LinearLayout c=card();entryInside(c,e,"از همان‌جا که خوانده بودید", "book");buttonEffect(c);c.setOnClickListener(v->open(e));
        }
        label("دسترسی سریع");
        LinearLayout quick=row();quick.setGravity(Gravity.TOP);quickTile(quick,"تعقیبات نماز","پس از نماز",5,"sun");quickTile(quick,"دعاها","همراه روزانه",158,"book");quickTile(quick,"زیارات","سلام و زیارت",197,"spark");add(body,quick,-1,-2);gap(body,20);
        label("در کتاب بگردید");LinearLayout search=card();entryInside(search,null,"جست‌وجو در عنوان و متن دعاها","search");buttonEffect(search);search.setOnClickListener(v->navigate("search"));
        TextView foot=text("آفلاین، همیشه در دسترس شما",12,muted,false);foot.setGravity(Gravity.CENTER);add(body,foot,-1,42);
    }
    void quickTile(LinearLayout parent,String title,String sub,int id,String glyph){
        LinearLayout c=col();c.setGravity(Gravity.CENTER);c.setPadding(dp(4),dp(17),dp(4),dp(15));rounded(c,surface,20);
        Icon i=new Icon(glyph,green);add(c,i,32,32);gap(c,10);TextView t=text(title,13,ink,true);t.setGravity(Gravity.CENTER);add(c,t,-1,-2);TextView s=text(sub,11,muted,false);s.setGravity(Gravity.CENTER);add(c,s,-1,-2);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(4),0,dp(4),0);parent.addView(c,p);buttonEffect(c);c.setOnClickListener(v->{screen="library";section=id;render();});
    }
    void entryInside(LinearLayout container,BookRepository.Entry e,String subtitle,String glyph){
        LinearLayout r=row();Icon icon=new Icon(glyph,green);rounded(icon,dark?Color.parseColor("#29443A"):Color.parseColor("#EDF3EC"),14);add(r,icon,48,48);
        LinearLayout words=col();words.setPadding(dp(14),0,dp(8),0);if(e!=null){TextView title=text(fa(BookRepository.label(e.title)),16,ink,true);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);add(words,title,-1,-2);gap(words,3);}
        add(words,text(subtitle,e==null?15:12,muted,false),-1,-2);r.addView(words,new LinearLayout.LayoutParams(0,-2,1));add(r,new Icon("chevron",muted),18,24);add(container,r,-1,-2);
    }
    void library(){
        if(section!=-1){BookRepository.Entry e=book.byId.get(section);if(e==null){section=-1;library();return;}
            TextView back=text("‹  همهٔ موضوع‌ها",14,green,true);add(body,back,-1,46);buttonEffect(back);back.setOnClickListener(v->{section=e.parent;render();});label(fa(BookRepository.label(e.title)));
        }else{add(body,text("دعا و ذکر موردنظر خود را پیدا کنید",14,muted,false),-1,-2);gap(body,16);}
        java.util.List<BookRepository.Entry> items=book.children(section);
        for(BookRepository.Entry e:items)entryCard(e,e.isSection()?fa(book.children(e.id).size())+" مطلب":"مطالعهٔ متن");
    }
    void entryCard(BookRepository.Entry e,String subtitle){LinearLayout c=card();entryInside(c,e,subtitle,e.isSection()?"folder":"book");buttonEffect(c);c.setOnClickListener(v->{if(e.isSection()){section=e.id;screen="library";render();}else open(e);});}
    void open(BookRepository.Entry e){if(e.isSection()){screen="library";section=e.id;render();return;}if(!screen.equals("reader"))returnScreen=screen;current=e.id;screen="reader";hideKeyboard();prefs.edit().putInt("last",current).apply();render();}
    void search(){
        EditText input=new EditText(this);input.setSingleLine(true);input.setTextSize(16);input.setTextColor(ink);input.setHintTextColor(muted);input.setHint("نام دعا یا بخشی از متن…");input.setTextDirection(View.TEXT_DIRECTION_RTL);input.setPadding(dp(18),dp(10),dp(18),dp(10));rounded(input,surface,18);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);input.setContentDescription("جست‌وجو در کتاب");add(body,input,-1,58);
        gap(body,12);TextView count=text("",13,muted,false);add(body,count,-1,32);LinearLayout result=col();add(body,result,-1,-2);
        Runnable update=()->{result.removeAllViews();if(query.trim().isEmpty()){count.setText("جست‌وجو در عنوان‌ها و متن کامل کتاب");empty(result,"چه می‌خواهید بخوانید؟","برای نمونه: استغفار، قنوت یا دعای صبح","search");return;}
            java.util.List<BookRepository.Entry> found=book.search(query);count.setText(fa(found.size())+" نتیجه");if(found.isEmpty())empty(result,"نتیجه‌ای پیدا نشد","واژه‌ای کوتاه‌تر یا عبارت دیگری بنویسید.","search");
            LinearLayout previous=body;body=result;for(BookRepository.Entry e:found){String plain=e.plain.replace('\n',' ');int at=BookRepository.normalize(plain).indexOf(BookRepository.normalize(query));String snippet=plain.substring(0,Math.min(plain.length(),95));if(at>0 && at<plain.length())snippet="… "+plain.substring(Math.max(0,at-20),Math.min(plain.length(),at+90));entryCard(e,e.isSection()?"موضوع":snippet);}body=previous;
        };
        input.setText(query);update.run();
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();if(pendingSearch!=null)handler.removeCallbacks(pendingSearch);pendingSearch=update;handler.postDelayed(update,180);}public void afterTextChanged(Editable e){}});
        input.setOnEditorActionListener((v,id,event)->{hideKeyboard();update.run();return true;});
    }
    void empty(LinearLayout into,String title,String desc,String glyph){LinearLayout c=col();c.setGravity(Gravity.CENTER);c.setPadding(dp(16),dp(50),dp(16),dp(30));add(c,new Icon(glyph,green),52,52);gap(c,18);TextView t=text(title,18,ink,true);t.setGravity(Gravity.CENTER);add(c,t,-1,-2);gap(c,8);TextView d=text(desc,14,muted,false);d.setGravity(Gravity.CENTER);add(c,d,-1,-2);add(into,c,-1,-2);}
    void bookmarks(){add(body,text("متن‌هایی که برای دوباره‌خواندن نگه داشته‌اید",14,muted,false),-1,-2);gap(body,16);int n=0;for(BookRepository.Entry e:book.entries)if(marks.contains(""+e.id)){entryCard(e,"ذخیره‌شده برای شما");n++;}if(n==0)empty(body,"هنوز نشانکی ندارید","هنگام مطالعه، نماد نشانک را لمس کنید.","bookmark");}
    void bottom(){
        bar=row();bar.setPadding(dp(10),dp(8),dp(10),dp(8));bar.setBackgroundColor(surface);
        String[] ids={"home","library","search","bookmarks"},names={"خانه","فهرست","جست‌وجو","نشانک‌ها"},icons={"home","book","search","bookmark"};
        for(int i=0;i<ids.length;i++){final String target=ids[i];boolean active=screen.equals(target);LinearLayout item=col();item.setGravity(Gravity.CENTER);Icon glyph=new Icon(icons[i],active?green:muted);add(item,glyph,26,26);gap(item,4);TextView t=text(names[i],11,active?green:muted,active);t.setGravity(Gravity.CENTER);add(item,t,-1,-2);if(active)rounded(item,dark?Color.parseColor("#29443A"):Color.parseColor("#EDF3EC"),16);buttonEffect(item);item.setContentDescription(names[i]);item.setOnClickListener(v->navigate(target));bar.addView(item,new LinearLayout.LayoutParams(0,dp(62),1));}
        add(root,bar,-1,-2);
    }
    String styled(String html){return html.replaceAll("(?i)color\\s*:\\s*red","color:"+(dark?"#E4BE88":"#986332")).replaceAll("(?i)color\\s*:\\s*blue","color:"+(dark?"#9EC9BE":"#34756A")).replaceAll("(?i)color=[\"']red[\"']","color=\""+(dark?"#E4BE88":"#986332")+"\"").replaceAll("(?i)color=[\"']blue[\"']","color=\""+(dark?"#9EC9BE":"#34756A")+"\"").replaceAll("(?i)#005A00",dark?"#B6C8AA":"#426D4B");}
    void reader(){
        BookRepository.Entry e=book.byId.get(current);
        LinearLayout tools=row();tools.setPadding(dp(12),dp(4),dp(12),dp(4));
        add(tools,icon("back","بازگشت",this::goBack),48,48);TextView name=text("مطالعه",17,ink,true);tools.addView(name,new LinearLayout.LayoutParams(0,dp(48),1));
        add(tools,icon(marks.contains(""+current)?"marked":"bookmark",marks.contains(""+current)?"حذف نشانک":"افزودن نشانک",()->{boolean exists=marks.remove(""+current);if(!exists)marks.add(""+current);prefs.edit().putStringSet("bookmarks",new HashSet<>(marks)).apply();render();Toast.makeText(this,exists?"نشانک حذف شد":"به نشانک‌ها اضافه شد",Toast.LENGTH_SHORT).show();}),48,48);
        add(tools,icon("type","تنظیمات مطالعه",this::readerOptions),48,48);add(tools,icon("share","اشتراک‌گذاری متن",()->{Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");share.putExtra(Intent.EXTRA_TEXT,e.title+"\n\n"+e.plain+"\n\nاز کتاب انیس الصادقین — سید حسین یعقوبی قائنی");startActivity(Intent.createChooser(share,"اشتراک‌گذاری متن"));}),48,48);add(root,tools,-1,-2);
        scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);body=col();body.setPadding(dp(26),dp(18),dp(26),dp(24));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        BookRepository.Entry parent=book.byId.get(e.parent);if(parent!=null){TextView trail=text(BookRepository.label(parent.title),13,green,false);add(body,trail,-1,-2);gap(body,8);}
        TextView title=text(fa(BookRepository.label(e.title)),24,ink,true);title.setLineSpacing(dp(3),1.05f);add(body,title,-1,-2);gap(body,12);
        LinearLayout meta=row();meta.addView(text("انیس الصادقین",12,muted,false),new LinearLayout.LayoutParams(0,-2,1));add(meta,text("صفحهٔ "+fa(BookRepository.label(e.page)),12,muted,false),-2,-2);add(body,meta,-1,32);gap(body,12);View divider=new View(this);divider.setBackgroundColor(line);add(body,divider,-1,1);gap(body,20);
        TextView content=text("",prefs.getInt("size",26),ink,false);String font=prefs.getString("font","mitra");if(!font.equals("system"))content.setTypeface(readingFont);content.setText(Html.fromHtml(styled(e.html)));content.setTextIsSelectable(true);content.setLineSpacing(dp(8),1.2f);content.setTextDirection(View.TEXT_DIRECTION_RTL);add(body,content,-1,-2);gap(body,24);
        int id=current;scroll.post(()->scroll.scrollTo(0,prefs.getInt("offset_"+id,0)));scroll.setOnScrollChangeListener((v,x,y,ox,oy)->prefs.edit().putInt("offset_"+id,y).apply());
        java.util.List<BookRepository.Entry> pages=book.readable();int index=pages.indexOf(e);LinearLayout pager=row();pager.setPadding(dp(16),dp(8),dp(16),dp(8));pager.setBackgroundColor(surface);
        TextView previous=text("→  قبلی",14,index>0?green:muted,true);previous.setGravity(Gravity.CENTER);pager.addView(previous,new LinearLayout.LayoutParams(0,dp(48),1));previous.setEnabled(index>0);buttonEffect(previous);previous.setOnClickListener(v->{if(index>0)open(pages.get(index-1));});
        TextView pos=text(fa(index+1)+" از "+fa(pages.size()),12,muted,false);pos.setGravity(Gravity.CENTER);add(pager,pos,-2,48);
        TextView next=text("بعدی  ←",14,index<pages.size()-1?green:muted,true);next.setGravity(Gravity.CENTER);pager.addView(next,new LinearLayout.LayoutParams(0,dp(48),1));next.setEnabled(index<pages.size()-1);buttonEffect(next);next.setOnClickListener(v->{if(index<pages.size()-1)open(pages.get(index+1));});add(root,pager,-1,-2);
    }
    void readerOptions(){
        LinearLayout box=col();box.setPadding(dp(24),dp(16),dp(24),dp(16));box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView size=text("اندازهٔ نوشته: "+fa(prefs.getInt("size",26)),16,ink,true);add(box,size,-1,-2);SeekBar slider=new SeekBar(this);slider.setMax(20);slider.setProgress(prefs.getInt("size",26)-20);add(box,slider,-1,52);slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean user){size.setText("اندازهٔ نوشته: "+fa(p+20));prefs.edit().putInt("size",p+20).apply();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        Switch mode=new Switch(this);mode.setText("حالت شب");mode.setTextColor(ink);mode.setTextSize(16);mode.setChecked(dark);add(box,mode,-1,52);mode.setOnCheckedChangeListener((v,on)->prefs.edit().putBoolean("dark",on).apply());
        Switch font=new Switch(this);font.setText("قلم کتابی میترا");font.setTextColor(ink);font.setChecked(!prefs.getString("font","mitra").equals("system"));add(box,font,-1,52);font.setOnCheckedChangeListener((v,on)->prefs.edit().putString("font",on?"mitra":"system").apply());
        AlertDialog dialog=new AlertDialog.Builder(this,dark?android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK:android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setTitle("تنظیمات مطالعه").setView(box).setPositiveButton("انجام شد",(d,w)->{}).create();dialog.setOnDismissListener(d->render());dialog.show();
    }
    void settings(){
        add(body,text("مطالعه را مطابق سلیقهٔ خود تنظیم کنید",14,muted,false),-1,-2);gap(body,20);
        LinearLayout c=card();entryInside(c,null,"قلم، اندازهٔ متن و حالت شب","type");buttonEffect(c);c.setOnClickListener(v->readerOptions());
        LinearLayout info=card();entryInside(info,null,"دربارهٔ برنامه و منابع","book");buttonEffect(info);info.setOnClickListener(v->navigate("about"));
        LinearLayout bio=card();entryInside(bio,null,"دربارهٔ مؤلف","spark");buttonEffect(bio);bio.setOnClickListener(v->navigate("author"));
        gap(body,20);TextView detail=text("انیس الصادقین • نسخهٔ بازطراحی‌شده\n۱.۲ — پیش‌نمایش\n\nمحتوای آفلاین برگرفته از نسخهٔ ۳ برنامهٔ اصلی\nسید حسین یعقوبی قائنی · سایت صادقین\n\nاین نسخه، بازطراحی مستقل برای استفادهٔ شخصی است.",13,muted,false);detail.setGravity(Gravity.CENTER);detail.setLineSpacing(dp(4),1.1f);add(body,detail,-1,-2);
    }
    void showDocument(String title,String html){ScrollView sc=new ScrollView(this);TextView t=text("",18,ink,false);t.setPadding(dp(24),dp(12),dp(24),dp(12));t.setText(Html.fromHtml(styled(html)));t.setTextIsSelectable(true);sc.addView(t);new AlertDialog.Builder(this,dark?android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK:android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setTitle(title).setView(sc).setPositiveButton("بستن",null).show();}
    void goBack(){hideKeyboard();if(screen.equals("author")||screen.equals("about")){navigate("settings");}else if(screen.equals("reader")){screen=returnScreen;render();}else if(screen.equals("library")&&section!=-1){section=book.byId.get(section).parent;render();}else if(!screen.equals("home")){navigate("home");}else super.onBackPressed();}
    @Override public void onBackPressed(){goBack();}

    void author(){
        header("دربارهٔ مؤلف",()->navigate("settings"));
        scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        body=col();body.setPadding(dp(22),dp(8),dp(22),dp(24));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout intro=card();intro.setGravity(Gravity.CENTER);
        ImageView portrait=new ImageView(this);portrait.setImageResource(R.drawable.author_portrait);
        portrait.setScaleType(ImageView.ScaleType.CENTER_CROP);rounded(portrait,line,20);portrait.setClipToOutline(true);
        portrait.setContentDescription("تصویر آیت‌الله سید حسین یعقوبی قائنی");
        LinearLayout.LayoutParams pic=new LinearLayout.LayoutParams(dp(112),dp(136));pic.gravity=Gravity.CENTER;intro.addView(portrait,pic);gap(intro,12);
        TextView name=text("سید حسین یعقوبی قائنی",23,ink,true);name.setGravity(Gravity.CENTER);add(intro,name,-1,-2);
        TextView subtitle=text("مؤلف انیس الصادقین",13,muted,false);subtitle.setGravity(Gravity.CENTER);add(intro,subtitle,-1,-2);
        gap(body,6);LinearLayout tabs=row();
        String[] names={"زندگی‌نامه","گالری ("+fa(authorMedia.images.size()+photoGallery.photos.size())+")"};
        for(int i=0;i<names.length;i++){
            final boolean gallery=i==1;
            TextView tab=text(names[i],15,authorGallery==gallery?surface:green,true);tab.setGravity(Gravity.CENTER);
            rounded(tab,authorGallery==gallery?green:surface,16);tab.setContentDescription(gallery?"گالری تصاویر مؤلف":"متن زندگی‌نامهٔ مؤلف");
            buttonEffect(tab);tab.setSelected(authorGallery==gallery);tab.setOnClickListener(v->{authorGallery=gallery;render();});
            LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(0,dp(50),1);layout.setMargins(dp(3),0,dp(3),0);tabs.addView(tab,layout);
        }
        add(body,tabs,-1,-2);gap(body,22);
        if(authorGallery){
            curatedPhotos();gap(body,10);label("تصاویر نسخهٔ اصلی");
            add(body,text("عکس‌های موجود در زندگی‌نامهٔ کتاب",12,muted,false),-1,-2);gap(body,16);
            for(int i=0;i<authorMedia.images.size();i++)addAuthorPhoto(body,authorMedia.images.get(i),i+1);
        }else{
            TextView source=text("گزیده‌ای از کتاب سفینة الصادقین",13,green,false);add(body,source,-1,-2);gap(body,12);
            for(AuthorMedia.Part part:authorMedia.parts){
                if(part.image!=null)addAuthorPhoto(body,part.image,part.imageNumber);
                else{
                    android.text.Spanned prose=Html.fromHtml(styled(part.html));
                    if(prose.toString().trim().isEmpty())continue;
                    TextView paragraph=text("",19,ink,false);paragraph.setText(prose);paragraph.setTextIsSelectable(true);
                    paragraph.setLineSpacing(dp(5),1.12f);add(body,paragraph,-1,-2);
                }
            }
        }
    }
    void addAuthorPhoto(LinearLayout into,Bitmap bitmap,int number){
        LinearLayout frame=col();frame.setGravity(Gravity.CENTER);frame.setPadding(dp(14),dp(14),dp(14),dp(12));rounded(frame,surface,20);
        int width=Math.min(dp(254),Math.max(dp(100),getResources().getDisplayMetrics().widthPixels-dp(72)));
        int height=Math.round(width*(float)bitmap.getHeight()/bitmap.getWidth());
        ImageView image=new ImageView(this);image.setImageBitmap(bitmap);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        String description="تصویر "+fa(number)+" از "+fa(authorMedia.images.size())+" · زندگی‌نامهٔ مؤلف";
        image.setContentDescription(description);image.setTag("author-photo-"+number);
        frame.addView(image,new LinearLayout.LayoutParams(width,height));gap(frame,9);
        TextView caption=text("تصویر "+fa(number)+" از "+fa(authorMedia.images.size())+"  ·  نمایش کامل",12,muted,false);caption.setGravity(Gravity.CENTER);add(frame,caption,-1,-2);
        buttonEffect(frame);frame.setContentDescription(description+"؛ نمایش کامل");frame.setOnClickListener(v->showAuthorPhoto(bitmap,number));
        LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(-1,-2);layout.topMargin=dp(8);layout.bottomMargin=dp(20);into.addView(frame,layout);
    }
    Dialog showAuthorPhoto(Bitmap bitmap,int number){
        final Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar_Fullscreen);
        LinearLayout page=col();page.setBackgroundColor(Color.parseColor("#101A17"));page.setPadding(dp(14),dp(20),dp(14),dp(20));
        TextView close=text("بستن  ×",16,Color.WHITE,true);close.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);close.setPadding(dp(12),0,dp(12),0);buttonEffect(close);close.setOnClickListener(v->dialog.dismiss());add(page,close,-1,52);
        ImageView image=new ImageView(this);image.setImageBitmap(bitmap);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setContentDescription("تصویر "+fa(number)+" از زندگی‌نامهٔ مؤلف");page.addView(image,new LinearLayout.LayoutParams(-1,0,1));
        TextView caption=text("تصویر "+fa(number)+" از "+fa(authorMedia.images.size()),13,Color.parseColor("#D4DFD5"),false);caption.setGravity(Gravity.CENTER);add(page,caption,-1,48);
        dialog.setContentView(page);dialog.show();dialog.getWindow().setLayout(-1,-1);return dialog;
    }

    void aboutApp(){
        header("دربارهٔ برنامه",()->navigate("settings"));
        scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        body=col();body.setPadding(dp(22),dp(10),dp(22),dp(28));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout intro=card();intro.setGravity(Gravity.CENTER);
        ImageView logo=new ImageView(this);logo.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));logo.setContentDescription("آیکون انیس الصادقین با تصویر مؤلف");
        LinearLayout.LayoutParams iconLayout=new LinearLayout.LayoutParams(dp(82),dp(82));iconLayout.gravity=Gravity.CENTER;intro.addView(logo,iconLayout);gap(intro,12);
        TextView name=text("انیس الصادقین",25,ink,true);name.setGravity(Gravity.CENTER);add(intro,name,-1,-2);
        TextView version=text("نسخهٔ بازطراحی‌شده · ۱.۲",13,muted,false);version.setGravity(Gravity.CENTER);add(intro,version,-1,-2);
        label("دربارهٔ این نسخه");
        TextView description=text("رابط این برنامه به‌صورت مستقل بازطراحی شده است تا مطالعهٔ کتاب، جست‌وجوی دعاها و دسترسی به تصاویر مؤلف آسان‌تر شود.",16,ink,false);description.setLineSpacing(dp(4),1.1f);add(body,description,-1,-2);gap(body,20);
        LinearLayout source=card();add(source,text("برنامهٔ مبنا",13,green,true),-1,-2);gap(source,6);
        add(source,text("انیس الصادقین، نسخهٔ ۳\nتهیه‌شده برای سایت صادقین",17,ink,true),-1,-2);gap(source,8);
        TextView attribution=text("محتوای کتاب و زندگی‌نامه از برنامهٔ اصلی استخراج شده‌اند. این بازطراحی به‌عنوان نسخهٔ رسمی سایت صادقین معرفی نمی‌شود.",14,muted,false);attribution.setLineSpacing(dp(3),1.1f);add(source,attribution,-1,-2);
        label("کتاب و تصاویر");
        TextView rights=text("مؤلف کتاب: سید حسین یعقوبی قائنی\nحقوق متن کتاب، تصاویر و سایر محتوای اصلی متعلق به صاحبان آن‌هاست. بازطراحی رابط به معنی انتقال مالکیت محتوای اصلی نیست.",15,ink,false);rights.setLineSpacing(dp(5),1.1f);add(body,rights,-1,-2);gap(body,16);
        TextView notice=text("مشاهدهٔ معرفی و اطلاعیهٔ حقوق نسخهٔ اصلی  ←",14,green,true);notice.setMinHeight(dp(52));buttonEffect(notice);notice.setOnClickListener(v->showDocument("معرفی نسخهٔ اصلی",book.about));add(body,notice,-1,-2);
        gap(body,10);add(body,text("منبع هر عکسِ افزوده‌شده در گالری ذکر شده است.",13,muted,false),-1,-2);
    }
    void curatedPhotos(){
        label("تصاویر منتخب");
        add(body,text("برای نمایش کامل و بزرگ‌نمایی، تصویر را لمس کنید.",13,muted,false),-1,-2);gap(body,14);
        for(int start=0;start<photoGallery.photos.size();start+=2){
            LinearLayout pair=row();pair.setGravity(Gravity.TOP);
            for(int index=start;index<Math.min(start+2,photoGallery.photos.size());index++){
                final int selected=index;PhotoGallery.Photo photo=photoGallery.photos.get(index);
                LinearLayout tile=col();tile.setPadding(dp(8),dp(8),dp(8),dp(12));rounded(tile,surface,20);
                ImageView image=new ImageView(this);image.setImageBitmap(photo.bitmap(this,720));image.setScaleType(ImageView.ScaleType.CENTER_CROP);rounded(image,line,14);image.setClipToOutline(true);image.setContentDescription(photo.title);image.setTag("curated-photo-"+index);add(tile,image,-1,144);gap(tile,10);
                TextView title=text(photo.title,14,ink,true);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);add(tile,title,-1,-2);gap(tile,4);add(tile,text(photo.source,11,muted,false),-1,-2);
                buttonEffect(tile);tile.setContentDescription(photo.title+"؛ نمایش کامل");tile.setOnClickListener(v->showCuratedPhoto(selected));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(4),0,dp(4),dp(14));pair.addView(tile,p);
            }
            if(pair.getChildCount()==1)pair.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
            add(body,pair,-1,-2);
        }
    }
    Dialog showCuratedPhoto(int initial){
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar_Fullscreen);
        LinearLayout page=col();page.setBackgroundColor(Color.parseColor("#101A17"));page.setPadding(dp(14),dp(18),dp(14),dp(18));page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView close=text("بستن  ×",16,Color.WHITE,true);close.setPadding(dp(12),0,dp(12),0);buttonEffect(close);close.setOnClickListener(v->dialog.dismiss());add(page,close,-1,52);
        ZoomPhotoView image=new ZoomPhotoView(this);page.addView(image,new LinearLayout.LayoutParams(-1,0,1));
        TextView title=text("",17,Color.WHITE,true);title.setGravity(Gravity.CENTER);add(page,title,-1,-2);
        TextView source=text("",12,Color.parseColor("#C3D7CA"),false);source.setGravity(Gravity.CENTER);add(page,source,-1,48);
        LinearLayout controls=row();TextView previous=text("→  قبلی",15,Color.WHITE,true),counter=text("",13,Color.WHITE,false),next=text("بعدی  ←",15,Color.WHITE,true);
        previous.setGravity(Gravity.CENTER);next.setGravity(Gravity.CENTER);counter.setGravity(Gravity.CENTER);buttonEffect(previous);buttonEffect(next);
        controls.addView(previous,new LinearLayout.LayoutParams(0,dp(52),1));add(controls,counter,-2,52);controls.addView(next,new LinearLayout.LayoutParams(0,dp(52),1));add(page,controls,-1,-2);
        int[] position={initial};Runnable update=()->{PhotoGallery.Photo photo=photoGallery.photos.get(position[0]);image.setImageBitmap(photo.bitmap(this,1920));image.setContentDescription(photo.title);title.setText(photo.title);source.setText("منبع: "+photo.source+"  ·  مشاهدهٔ صفحهٔ منبع");counter.setText(fa(position[0]+1)+" از "+fa(photoGallery.photos.size()));previous.setEnabled(position[0]>0);next.setEnabled(position[0]<photoGallery.photos.size()-1);previous.setAlpha(previous.isEnabled()?1:.35f);next.setAlpha(next.isEnabled()?1:.35f);};
        previous.setOnClickListener(v->{if(position[0]>0){position[0]--;update.run();}});next.setOnClickListener(v->{if(position[0]<photoGallery.photos.size()-1){position[0]++;update.run();}});
        buttonEffect(source);source.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(photoGallery.photos.get(position[0]).sourceUrl))));
        update.run();dialog.setContentView(page);dialog.show();dialog.getWindow().setLayout(-1,-1);return dialog;
    }

    /** Small vector icons drawn locally: no network, image fonts or external UI dependencies. */
    class Icon extends View {
        String kind; int color;Paint paint=new Paint(3);
        Icon(String kind,int color){super(MainActivity.this);this.kind=kind;this.color=color;setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
        void path(Canvas c,float... xy){Path p=new Path();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);c.drawPath(p,paint);}
        @Override protected void onDraw(Canvas raw){super.onDraw(raw);raw.save();float side=Math.min(getWidth(),getHeight())*(Math.min(getWidth(),getHeight())<=dp(32)?.86f:.53f);raw.translate((getWidth()-side)/2,(getHeight()-side)/2);raw.scale(side/24,side/24);paint.setColor(color);paint.setStrokeWidth(1.7f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStyle(Paint.Style.STROKE);
            switch(kind){
                case "home":path(raw,3,10,12,3,21,10,21,21,14,21,14,14,10,14,10,21,3,21,3,10);break;
                case "book":path(raw,12,5,12,21);path(raw,12,5,8,3,2,3,2,19,8,19,12,21,16,19,22,19,22,3,16,3,12,5);break;
                case "folder":path(raw,2,7,2,4,9,4,12,7,22,7,22,20,2,20,2,7);break;
                case "search":raw.drawCircle(10.5f,10.5f,7,paint);raw.drawLine(16,16,22,22,paint);break;
                case "marked":paint.setStyle(Paint.Style.FILL);case "bookmark":path(raw,5,3,19,3,19,22,12,17,5,22,5,3);break;
                case "chevron":path(raw,15,5,8,12,15,19);break;
                case "back":path(raw,9,5,16,12,9,19);break;
                case "share":raw.drawCircle(19,4,3,paint);raw.drawCircle(5,12,3,paint);raw.drawCircle(19,20,3,paint);path(raw,8,11,16,5);path(raw,8,13,16,19);break;
                case "sun":raw.drawCircle(12,12,4,paint);for(int a=0;a<8;a++){double r=a*Math.PI/4;raw.drawLine((float)(12+Math.cos(r)*8),(float)(12+Math.sin(r)*8),(float)(12+Math.cos(r)*10),(float)(12+Math.sin(r)*10),paint);}break;
                case "spark":path(raw,12,1,15,9,23,12,15,15,12,23,9,15,1,12,9,9,12,1);break;
                case "type":path(raw,2,6,15,6);path(raw,8,6,8,21);path(raw,16,12,12,21);path(raw,16,12,20,21);path(raw,14,18,18,18);break;
                default:raw.drawCircle(12,12,4,paint);raw.drawCircle(12,12,9,paint);for(int a=0;a<8;a++){double r=a*Math.PI/4;raw.drawLine((float)(12+Math.cos(r)*9),(float)(12+Math.sin(r)*9),(float)(12+Math.cos(r)*11),(float)(12+Math.sin(r)*11),paint);}
            }raw.restore();
        }
    }
}
