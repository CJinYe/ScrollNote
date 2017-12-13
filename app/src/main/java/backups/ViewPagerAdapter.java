package backups;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-27 10:00
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
//public class ViewPagerAdapter extends PagerAdapter {
//    private static final String TAG = "PagerAdapter1";
//    private final Context mContext;
//    private final int mScreenWidth;
//    private final int mScreenHeight;
//    private NoteBean mNoteBean;
//    private String mPath;
//    public SparseArray<NoteFragment> mTuyaViewMap = new SparseArray<>();
//
//    private int size = 1;
//    public NoteFragment mNoteFragment;
//
//    public ViewPagerAdapter(String path, Context context, NoteBean noteBean, int screenWidth, int screenHeight) {
//        mContext = context;
//        mNoteBean = noteBean;
//        mPath = path;
//        mScreenWidth = screenWidth;
//        mScreenHeight = screenHeight;
//
//    }
//
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        container.removeView((View) object);
//    }
//
//    @Override
//    public boolean isViewFromObject(View view, Object object) {
//        return view == object;
//    }
//
//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        mNoteFragment = new NoteFragment();
//        mNoteFragment.setKey(position, null);
//        if (position == 0) {
//            if (mPath != null && !TextUtils.isEmpty(mPath)) {
//                Bitmap bitmap = BitmapFactory.decodeFile(mPath);
//                TuyaViewPage tuyaView = new TuyaViewPage(mContext, bitmap, mScreenWidth, mScreenHeight);
//                mNoteFragment.setTuyaView(tuyaView);
//
//                int pixelColor = bitmap.getPixel(0, 0);
//                int A = Color.alpha(pixelColor);
//                int R = Color.red(pixelColor);
//                int G = Color.green(pixelColor);
//                int B = Color.blue(pixelColor);
//                int backgroundColor = Color.argb(A, R, G, B);
//                tuyaView.backgroundColor = backgroundColor;
//
//                if (mNoteBean != null && !TextUtils.isEmpty(mNoteBean.text)) {
//                    mNoteFragment.setText(mNoteBean.text);
//                }
//                mTuyaViewMap.put(position, mNoteFragment);
//                bitmap.recycle();
//                container.addView(mNoteFragment.mView);
//                return mNoteFragment.mView;
//            } else {
//                TuyaViewPage tuyaView = new TuyaViewPage(mContext, mScreenWidth, mScreenHeight);
//                mNoteFragment.setTuyaView(tuyaView);
//                mTuyaViewMap.put(position, mNoteFragment);
//                container.addView(mNoteFragment.mView);
//                return mNoteFragment;
//            }
//        }
//        TuyaViewPage tuyaView = new TuyaViewPage(mContext, mScreenWidth, mScreenHeight);
//        mNoteFragment.setTuyaView(tuyaView);
//        mTuyaViewMap.put(position, mNoteFragment);
//        container.addView(mNoteFragment.mView);
//        return mNoteFragment.mView;
//    }
//
//    @Override
//    public int getCount() {
//        return size;
//    }
//
//    public int addCount() {
//        size++;
//        notifyDataSetChanged();
//        return size;
//    }
//
//    public void setPaintSize(int currentItem, int paintSize) {
//        mTuyaViewMap.get(currentItem).getTuyaView().selectPaintSize(paintSize);
//    }
//
//    public String saveNote(int currentItem, String time, String content) {
//
//        String path = null;
//        try {
//            path = mTuyaViewMap.get(0).getTuyaView().saveToSDCard(time, "第四张");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //        AddFilesWithAESEncryption(path,path1,path2);
//        return path;
//    }
//
//    public void clear(int currentItem) {
//        mTuyaViewMap.get(currentItem).getTuyaView().redo();
//    }
//
//    public void setPaintColor(int currentItem, int white) {
//        mTuyaViewMap.get(currentItem).getTuyaView().selectPaintColor(white);
//    }
//
//    public void undo(int currentItem) {
//        mTuyaViewMap.get(currentItem).getTuyaView().undo();
//    }
//
//    /**
//     * 点击文字
//     *
//     * @param currentItem
//     */
//    public void onClickType(int currentItem) {
//        //        mTuyaViewMap.get(currentItem).clickType();
//        mNoteFragment.clickType();
//    }
//
//
//    public void AddFilesWithAESEncryption(String path, String path1, String path2) {
//        try {
//            ZipFile zipFile = new ZipFile(Environment.getExternalStorageDirectory() + "/ScrawlNote/test.zip");
//
//            ArrayList<File> filesToAdd = new ArrayList<File>();
//            filesToAdd.add(new File(path));
//            filesToAdd.add(new File(path1));
//            filesToAdd.add(new File(path2));
//
//            ZipParameters parameters = new ZipParameters();
//            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
//
//            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
//            parameters.setEncryptFiles(true);
//
//            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
//
//
//            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
//            parameters.setPassword("123");
//
//            zipFile.addFiles(filesToAdd, parameters);
//        } catch (ZipException e) {
//            e.printStackTrace();
//            Log.i(TAG, "e = " + e);
//        }
//    }


//}
