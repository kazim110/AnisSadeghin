package ir.anis.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.*;
import android.widget.ImageView;

/** Fit the whole photo initially. Pinch/double tap zooms; dragging is bounded to the image. */
public final class ZoomPhotoView extends ImageView {
    final Matrix matrix=new Matrix();
    float base=1, zoom=1, lastX, lastY;
    int bitmapWidth,bitmapHeight;
    boolean dragging;
    final ScaleGestureDetector scale;
    final GestureDetector gestures;
    public ZoomPhotoView(Context context){
        super(context);setScaleType(ScaleType.MATRIX);setFocusable(true);
        scale=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector detector){
                float next=Math.max(1,Math.min(5,zoom*detector.getScaleFactor()));
                matrix.postScale(next/zoom,next/zoom,detector.getFocusX(),detector.getFocusY());zoom=next;constrain();return true;
            }
        });
        gestures=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDown(android.view.MotionEvent e){return true;}
            @Override public boolean onDoubleTap(android.view.MotionEvent e){
                if(zoom>1.05f)fit();else{matrix.postScale(2.5f,2.5f,e.getX(),e.getY());zoom=2.5f;constrain();}return true;
            }
            @Override public boolean onSingleTapConfirmed(android.view.MotionEvent e){performClick();return true;}
        });
    }
    @Override public void setImageBitmap(Bitmap bitmap){
        super.setImageBitmap(bitmap);bitmapWidth=bitmap.getWidth();bitmapHeight=bitmap.getHeight();fit();
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);fit();}
    void fit(){
        if(bitmapWidth==0||getWidth()==0||getHeight()==0)return;
        base=Math.min((float)getWidth()/bitmapWidth,(float)getHeight()/bitmapHeight);zoom=1;matrix.reset();
        matrix.postScale(base,base);matrix.postTranslate((getWidth()-bitmapWidth*base)/2,(getHeight()-bitmapHeight*base)/2);setImageMatrix(matrix);
    }
    void constrain(){
        RectF bounds=new RectF(0,0,bitmapWidth,bitmapHeight);matrix.mapRect(bounds);
        float dx=bounds.width()<=getWidth()?(getWidth()-bounds.width())/2-bounds.left:bounds.left>0?-bounds.left:bounds.right<getWidth()?getWidth()-bounds.right:0;
        float dy=bounds.height()<=getHeight()?(getHeight()-bounds.height())/2-bounds.top:bounds.top>0?-bounds.top:bounds.bottom<getHeight()?getHeight()-bounds.bottom:0;
        matrix.postTranslate(dx,dy);setImageMatrix(matrix);
    }
    @Override public boolean onTouchEvent(android.view.MotionEvent e){
        gestures.onTouchEvent(e);scale.onTouchEvent(e);
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:lastX=e.getX();lastY=e.getY();dragging=true;break;
            case MotionEvent.ACTION_MOVE:
                if(dragging&&!scale.isInProgress()&&e.getPointerCount()==1){matrix.postTranslate(e.getX()-lastX,e.getY()-lastY);constrain();}
                lastX=e.getX();lastY=e.getY();break;
            case MotionEvent.ACTION_POINTER_UP:dragging=false;break;
            case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:dragging=false;break;
        }
        return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
}
