package com.example.gxw.indoorlocation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class  PathView extends View {         //此类用于显示路径
    private Paint paint;

    public float mCurX;
    public float mCurY;

    private Bitmap mBitmap;
    private List<PointF> mPointList = new ArrayList<>();


    public PathView(Context context) {
        this(context, null);
    }

    public PathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.parseColor("#FF0000"));
        //初始化map
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.idoormap);
//        mPointList.get(0).x=0;   //初始化位点
//        mPointList.get(0).y=0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), new Rect(0, 0, getWidth(), getHeight()), null); // 将mBitmap绘到canLock

        canvas.drawCircle(mCurX,mCurY, 10, paint);    //定位阶段显示定位点
        Path path = new Path();
        path.moveTo(0,0);
        if(mPointList.size()!=0)
        {
            path.moveTo(mPointList.get(0).x,mPointList.get(0).y);
            for(int i=1;i<mPointList.size();i++)
            {
                path.lineTo(mPointList.get(i).x,mPointList.get(i).y);
            }

        }

        canvas.drawPath(path,paint);
        canvas.save(); // 保存画布
        canvas.restore(); // 恢复画布
    }

    /**
     * 自动增加点
     */
    public void autoAddPoint(ArrayList<Integer> list) {       //规划路径阶段，增加图中的必须通过的点集
        for(int i=0;i<list.size();i++)
        {
            PathPoint.getCoord(list.get(i));
            mCurX =PathPoint.Xcoord*80+225;
            mCurY =PathPoint.Ycoord*80+234;
            mPointList.add(new PointF(mCurX, mCurY));
        }
        postInvalidate();                //重新绘制UI
    }
    //得到X轴和Y轴坐标并刷新页面
    public void getCoord(float x,float y)                    //定位阶段或者位置
    {
        this.mCurX=x*80+225;
        this.mCurY=y*80+234;
        System.out.println(mCurX);
        System.out.println(mCurY);
        System.out.println("你好");
        postInvalidate();                //重新绘制UI
    }
}
