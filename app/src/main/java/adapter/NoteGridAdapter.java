package adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import constants.Constants;
import icox.com.scrawlnote.R;
import utils.DateTimeUtil;
import utils.SpUtils;

import static utils.SDcardUtil.getSavaPath;


/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-25 17:04
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteGridAdapter extends BaseAdapter {

    public List<File> mFiles = new ArrayList<>();
    private final Context mContext;
    private final String mLocation;

    public NoteGridAdapter(Context context) {
        mContext = context;
        SpUtils spUtils = new SpUtils(context);
        mLocation = spUtils.getString(Constants.MEETING_ADDR, Constants.MEETING_ADDR_NORMAL);
        String sdPath = null;
        try {
            String path = getSavaPath();
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getPath().contains(".zip"))
                        mFiles.add(files[i]);
                }
                Collections.sort(mFiles, new FileComparator());

            }
        } catch (Exception e) {
            Toast.makeText(context, "读取文件错误！" + e, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


    }

    /**
     * 将文件按时间降序排列
     */
    class FileComparator implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            if (file1.lastModified() < file2.lastModified()) {
                return 1;// 最后修改的文件在前
            } else {
                return -1;
            }
        }
    }

    @Override
    public int getCount() {
        if (mFiles != null) {
            return mFiles.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_main_grid_note, null);
            //            holder.iv = (ImageView) convertView.findViewById(R.id.item_iv_note);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.item_tv_title);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.item_tv_location);
            holder.tvTime = (TextView) convertView.findViewById(R.id.item_tv_time);
            holder.tvCompere = (TextView) convertView.findViewById(R.id.item_tv_compere);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String fileName = mFiles.get(position).getName().replaceAll(".zip", "");

        if (fileName.contains("_")) {
            String title = fileName.substring(0, fileName.indexOf("_"));
            String time = fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("_")+1);
            String compere = fileName.substring(fileName.lastIndexOf("_") + 1, fileName.length());

            if (TextUtils.isEmpty(time))
                time = compere;

            if (!time.equals(compere) && !compere.equals("null"))
                holder.tvCompere.setText(compere);

            long timeLong = DateTimeUtil.stringToLong(time);
            time = DateTimeUtil.longToString(timeLong);
            holder.tvTime.setText(time);
            holder.tvTitle.setText(title);
        } else {
            holder.tvTitle.setText(fileName);
        }
        holder.tvLocation.setText(mLocation);

        //        Picasso.with(mContext).load(mFi   les[position]).into(holder.iv);
        return convertView;
    }

    class ViewHolder {
        ImageView iv;
        TextView tvTitle;
        TextView tvLocation;
        TextView tvTime;
        TextView tvCompere;
    }
}
