package fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.lang.reflect.Field;

import bean.TuyaViewBean;
import constants.NoteBeanConf;
import icox.com.scrawlnote.R;
import service.PostilService;
import utils.TouchMeasureUtil;
import view.TuyaViews.TuyaViewPage;
import backups.TuyaViewThree;

import static constants.NoteBeanConf.saveNotePaintColor;
import static constants.NoteBeanConf.saveNotePaintSize;
import static constants.NoteBeanConf.saveNotePaintStyle;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-27 10:02
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteFragment extends Fragment {

    private final String TAG = "NoteFragment";
    private FrameLayout mFrameLayout;
    private TuyaViewPage mTuyaView;
    public TuyaViewThree mTuyaViewThree;
    private String mText;
    private boolean isType = false;
    public EditText mEditText;
    private ImageView mIvEraser;
    private int mKey;
    public View mView;
    private File mPath;
    private ImageView mIvBackground;
    private TuyaViewBean mTuyaViewBean;
    private boolean isPostil = false;

    private float mNewDist, mOldDist;
    private float mTouchSlop = 5;
    private float mScale;
    private float mOldScale = 0;
    private final float mMaxScale = 3.5f; // 最大缩放倍数
    private final float mMinScale = 1f; // 最小缩放倍数
    private float mTouchCentreX, mTouchCentreY, mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_note, container, false);
        mFrameLayout = (FrameLayout) mView.findViewById(R.id.fragment_note_tuya);
        mEditText = (EditText) mView.findViewById(R.id.fragment_edt_text);
        mIvEraser = (ImageView) mView.findViewById(R.id.fragment_note_iv);
        //        mIvBackground = (ImageView) mView.findViewById(R.id.fragment_iv_bg);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        if (!TextUtils.isEmpty(mText)) {
            //如果数据库有笔记文本就显示文本
            mEditText.setVisibility(View.VISIBLE);
            mEditText.setEnabled(false);
            mEditText.setText(mText);
        }
        if (mTuyaView != null) {
            mFrameLayout.addView(mTuyaView);
        }

        mTuyaViewBean = NoteBeanConf.tuyaBeanList.get(mKey);
//        if (mTuyaViewBean != null ) {
        if (mTuyaViewBean != null && !isPostil) {
            //如果缓存的画板信息不为空,则重新配置画板画笔
            mTuyaView.setConstants(mTuyaViewBean,metrics.widthPixels,metrics.heightPixels);

            if (!TextUtils.isEmpty(mTuyaViewBean.text)) {
                //如果数据库有笔记文本就显示文本
                mEditText.setVisibility(View.VISIBLE);
                mEditText.setEnabled(false);
                mEditText.setText(mTuyaViewBean.text);
            }
        } else {
//            if (!isPostil) {
                if (NoteBeanConf.saveNoteBgColor != -1 || saveNotePaintColor != -1
                        || saveNotePaintSize != -1 || saveNotePaintStyle != -1) {
                    mTuyaView.setConstants(NoteBeanConf.saveNoteBgColor,
                            saveNotePaintColor, saveNotePaintSize, saveNotePaintStyle);
                }
//            }
        }

        initListener2();
        return mView;
    }

    private int mTouchMode;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        //销毁的时候把当前的画板信息保存起来
        saveBitmap();
        mFrameLayout.removeView(mTuyaView);
        //        mTuyaView.destroyBitmap();
        mFrameLayout = null;
        mView = null;
        mTuyaView = null;
        isPostil = false;
        super.onDestroyView();
        System.gc();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isPostil && !isVisibleToUser && mTuyaView != null && (mTuyaView.backgroundColor != -1 || mTuyaView.currentColor != -16777216)) {
            if (mTuyaViewBean == null) {
                if (mTuyaView.backgroundColor != -1)
                    NoteBeanConf.saveNoteBgColor = mTuyaView.backgroundColor;
                if (mTuyaView.currentColor != -1)
                    saveNotePaintColor = mTuyaView.currentColor;
                saveNotePaintSize = mTuyaView.currentSize;
                saveNotePaintStyle = mTuyaView.currentStyle;
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void saveBitmap() {
        TuyaViewBean tuyaViewBean = new TuyaViewBean();
        tuyaViewBean.backgroundColor = mTuyaView.backgroundColor;
        tuyaViewBean.currentColor = mTuyaView.currentColor;
        tuyaViewBean.currentSize = mTuyaView.currentSize;
        tuyaViewBean.currentStyle = mTuyaView.currentStyle;
        tuyaViewBean.savePaths = mTuyaView.getSavePaths();
        tuyaViewBean.isChangerBackground = mTuyaView.mIsChangerBackground;
        tuyaViewBean.text = mEditText.getText().toString();
        tuyaViewBean.saveScreenWidth = mTuyaView.screenWidth;
        if (mPath != null) {
            tuyaViewBean.bitmapPath = mPath;
        }

        //如果是批注页面,则保存绘画的路径等信息
        if (mTuyaView.isPostil) {
            tuyaViewBean.currentStyle = 1;
            tuyaViewBean.currentColor = Color.BLACK;
            PostilService.postilList.clear();
            PostilService.postilList.put(0, tuyaViewBean);
        } else {
        }
            NoteBeanConf.tuyaBeanList.put(mKey, tuyaViewBean);

    }

    public void setTuyaView(TuyaViewPage tuyaView) {
        mTuyaView = tuyaView;
    }

    public TuyaViewPage getTuyaView() {
        return mTuyaView;
    }

    public void setTuyaViewThree(TuyaViewThree tuyaViewThree) {
        mTuyaViewThree = tuyaViewThree;
    }

    public void setText(String text) {
        mText = text;
    }

    public void clickType() {
        if (mEditText != null) {
            isType = !isType;
            if (isType) {
                mEditText.setVisibility(View.VISIBLE);
                mEditText.setEnabled(true);
            } else {
                if (!TextUtils.isEmpty(mEditText.getText().toString())) {
                    mEditText.setVisibility(View.VISIBLE);
                    mEditText.setEnabled(false);
                } else {
                    mEditText.setVisibility(View.GONE);
                }
            }
        }
    }


    public void setKey(int key, File path) {
        mKey = key;
        mPath = path;
    }

    public void setPostil(boolean postil) {
        isPostil = postil;
    }

    private void initListener() {
        mTuyaView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchMode = 1;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mTouchMode = 0;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mTouchMode -= 1;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mTouchMode += 1;
                        mOldDist = spacing(event);// 两点按下时的距离
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getRawX();
                        float moveY = event.getRawY();
                        if (mTouchMode > 2) {
                            return false;
                        } else if (mTouchMode == 2) {
                            float x1 = event.getX(0);
                            float x2 = event.getX(1);
                            float y1 = event.getY(0);
                            float y2 = event.getY(1);
                            double juli = Math.sqrt((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));

                            if (juli > 100) {
                                mNewDist = spacing(event);// 两点滑动时的距离
                                Log.i("toucha", "touchMode mNewDist= " + mNewDist);
                                if (Math.abs(mNewDist - mOldDist) >= mTouchSlop) {
                                    mScale = mNewDist / mOldDist;

                                    if (mScale > mMaxScale) {
                                        mScale = mMaxScale;
                                    }
                                    if (mScale < mMinScale) { // 最小倍数
                                        mScale = mMinScale;
                                    }

                                    // 围绕坐标(0,0)缩放图片
                                    if (mOldScale != 0 && (mScale > mOldScale + 0.2f || mScale + 0.2f < mOldScale)) {
                                        //                                        mTuyaView.setScaleX(mScale);
                                        //                                        mTuyaView.setScaleY(mScale);
                                        mTuyaView.setScale(mScale);
                                    }
                                    // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                                    //                                float transX = mGraffitiView.toTransX(mTouchCentreX, mToucheCentreXOnGraffiti);
                                    //                                float transY = mGraffitiView.toTransY(mTouchCentreY, mToucheCentreYOnGraffiti);
                                    //                                mGraffitiView.setTrans(transX, transY);

                                    Log.i("toucha", " mScale= " + mScale);

                                    mOldScale = mScale;
                                }

                                return true;
                            } else {

                            }

                        }
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }

    private int TOUCH_TYPE = 0;
    private final int TOUCH_TYPE_CLEAR = 3; //三点触摸,擦除
    private final int TOUCH_TYPE_ONE = 1; //单点触摸,画画
    private final int TOUCH_TYPE_ZOOM = 2; //两点触摸,放大缩小
    private long firstTouchTime = 0;
    private long firstTouchPointerTime = 0;

    private void initListener2() {
        mTuyaView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchMode = 1;
                        firstTouchTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        mTouchMode = 0;
                        TOUCH_TYPE = 0;
                        firstTouchTime = 0;
                        firstTouchPointerTime = 0;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mTouchMode -= 1;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (firstTouchPointerTime == 0)
                            firstTouchPointerTime = System.currentTimeMillis();

                        mTouchMode += 1;
                        mOldDist = spacing(event);// 两点按下时的距离
                        mOldScale = mTuyaView.getScale();
                        mTouchCentreX = (event.getX(0) + event.getX(1)) / 2;// 不用减trans
                        mTouchCentreY = (event.getY(0) + event.getY(1)) / 2;
                        mToucheCentreXOnGraffiti = mTuyaView.toX(mTouchCentreX);
                        mToucheCentreYOnGraffiti = mTuyaView.toY(mTouchCentreY);
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (mTouchMode != 2 || event.getPointerCount() != 2) {
                            if (TOUCH_TYPE == 0 && TouchMeasureUtil.ThreeTouchDistance(event)) {
                                TOUCH_TYPE = TOUCH_TYPE_CLEAR;
                            } else if (TOUCH_TYPE == 0 && mTouchMode < 2) {
                                if (firstTouchPointerTime == 0 && System.currentTimeMillis() - firstTouchTime > 1000) {
                                    TOUCH_TYPE = TOUCH_TYPE_ONE;
                                } else {
                                    if (firstTouchPointerTime - firstTouchTime > 1000) {
                                        TOUCH_TYPE = TOUCH_TYPE_ONE;
                                    }
                                }
                            } else if (TOUCH_TYPE == 0) {
                                TOUCH_TYPE = TOUCH_TYPE_ZOOM;
                            }
                        } else {
                            if (TOUCH_TYPE == 0)
                                TOUCH_TYPE = TOUCH_TYPE_ZOOM;

                            if (TOUCH_TYPE == TOUCH_TYPE_ZOOM) {
                                mNewDist = spacing(event);// 两点滑动时的距离
                                if (Math.abs(mNewDist - mOldDist) >= mTouchSlop) {
                                    float scale = mNewDist / mOldDist;
                                    mScale = mOldScale * scale;

                                    if (mScale > mMaxScale) {
                                        mScale = mMaxScale;
                                    }
                                    if (mScale < mMinScale) { // 最小倍数
                                        mScale = mMinScale;
                                    }
                                    // 围绕坐标(0,0)缩放图片
                                    mTuyaView.setScale(mScale);
                                    // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                                    float transX = mTuyaView.toTransX(mTouchCentreX, mToucheCentreXOnGraffiti);
                                    float transY = mTuyaView.toTransY(mTouchCentreY, mToucheCentreYOnGraffiti);
                                    mTuyaView.setTrans(transX, transY);
                                }
                            }
                        }

                        break;

                    default:
                        break;
                }

                return false;
            }
        });
    }

    /**
     * 计算两指间的距离
     *
     * @param event
     * @return
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
