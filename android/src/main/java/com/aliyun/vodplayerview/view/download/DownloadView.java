package com.aliyun.vodplayerview.view.download;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hyz.myaliplayer.R;
import com.aliyun.vodplayer.downloader.AliyunDownloadMediaInfo;
import com.aliyun.vodplayer.downloader.AliyunDownloadMediaInfo.Status;
import com.aliyun.vodplayerview.view.download.DownloadSection.OnSectionItemClickListener;
import com.aliyun.vodplayerview.view.sectionlist.SectionedRecyclerViewAdapter;

import static com.aliyun.vodplayerview.view.download.DownloadSection.DOWNLOADED_TAG;
import static com.aliyun.vodplayerview.view.download.DownloadSection.DOWNLOADING_TAG;

/**
 * 离线下载的界面 该界面中包含下载列表, 列表的item编辑(全选, 删除),  Empty空数据显示等操作
 *
 * @author Mulberry create on 2018/4/12.
 */
public class DownloadView extends FrameLayout implements OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView downloadListView;
    private LinearLayout downloadEmptyView;
    private ImageView ivDownloadDelete;
    private ImageView ivCloseEdit;
    private RelativeLayout rlDownloadManagerContent;
    private RelativeLayout rlDownloadManagerEdit;
    private RelativeLayout rlDownloadManagerEditDefault;

    private ArrayList<AlivcDownloadMediaInfo> alivcDownloadMediaInfos;
    private ArrayList<AlivcDownloadMediaInfo> alivcDownloadingMediaInfos;
    private WeakReference<Context> context;
    private CheckBox cbAllDownloadCheck;
    private LinearLayoutManager linearLayoutManager;
    private DownloadSection section;
    /**
     * 用于判断当前是否处于编辑状态
     */
    private boolean isEditeState = false;

    /**
     * 下载完成的选中状态
     */
    private boolean isDownloadedChecked = false;
    /**
     * 下载中的选中状态
     */
    private boolean isDownloadingChecked = false;

    public boolean isEditeState() {
        return isEditeState;
    }

    public void setEditeState(boolean editeState) {
        isEditeState = editeState;
    }

    public DownloadView(@NonNull Context context) {
        super(context);
        this.context = new WeakReference<Context>(context);

        initView();
    }

    public DownloadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = new WeakReference<Context>(context);
        initView();
    }

    public DownloadView(@NonNull Context context, @Nullable AttributeSet attrs,
                        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = new WeakReference<Context>(context);
        initView();
    }

    private void initView() {
        LayoutInflater.from(this.context.get()).inflate(R.layout.alivc_download_view_layout, this, true);
        downloadEmptyView = (LinearLayout)findViewById(R.id.alivc_layout_empty_view);
        downloadListView = (RecyclerView)findViewById(R.id.download_list_view);
        linearLayoutManager = new LinearLayoutManager(getContext());
        downloadListView.setLayoutManager(linearLayoutManager);

        rlDownloadManagerContent = (RelativeLayout)findViewById(R.id.rl_download_manager_content);
        rlDownloadManagerEdit = (RelativeLayout)findViewById(R.id.rl_download_manager_edit);
        rlDownloadManagerEditDefault = (RelativeLayout)findViewById(R.id.rl_download_manager_edit_default);
        ivDownloadDelete = (ImageView)findViewById(R.id.iv_download_delete);
        ivCloseEdit = (ImageView)findViewById(R.id.iv_close_edit);

        cbAllDownloadCheck = findViewById(R.id.checkbox_all_select);

        cbAllDownloadCheck.setOnCheckedChangeListener(this);

        ivDownloadDelete.setOnClickListener(this);
        rlDownloadManagerEditDefault.setOnClickListener(this);
        ivCloseEdit.setOnClickListener(this);
        sectionAdapter = new SectionedRecyclerViewAdapter();
        alivcDownloadMediaInfos = new ArrayList<>();
        alivcDownloadingMediaInfos = new ArrayList<>();
        downloadListView.setItemAnimator(null);
        downloadListView.setAdapter(sectionAdapter);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.rl_download_manager_edit_default) {
            changeDownloadEditState(true);
            isEditeState = true;
            cbAllDownloadCheck.setChecked(false);
        } else if (i == R.id.iv_download_delete) {



            ArrayList<AlivcDownloadMediaInfo> deleteDownloadMediaInfos = new ArrayList<>();
            for (Iterator<AlivcDownloadMediaInfo> it = alivcDownloadingMediaInfos.iterator(); it.hasNext(); ) {
                AlivcDownloadMediaInfo val = it.next();
                if (val.isCheckedState()) {

                    deleteDownloadMediaInfos.add(val);
                }
            }

            for (Iterator<AlivcDownloadMediaInfo> it = alivcDownloadMediaInfos.iterator(); it.hasNext(); ) {
                AlivcDownloadMediaInfo val = it.next();
                if (val.isCheckedState()) {
                    deleteDownloadMediaInfos.add(val);
                }
            }
            if (onDownloadViewListener != null) {
                onDownloadViewListener.onDeleteDownloadInfo(deleteDownloadMediaInfos);
            }

        } else if (i == R.id.iv_close_edit) {
            setEditeState(false);
            changeDownloadEditState(false);
            isEditeState = false;
        }
    }

    public void deleteDownloadInfo() {
        changeDownloadEditState(false);
        isEditeState = false;

        for (Iterator<AlivcDownloadMediaInfo> it = alivcDownloadingMediaInfos.iterator(); it.hasNext(); ) {
            AlivcDownloadMediaInfo val = it.next();
            if (val.isCheckedState()) {
                it.remove();
            }
        }

        for (Iterator<AlivcDownloadMediaInfo> it = alivcDownloadMediaInfos.iterator(); it.hasNext(); ) {
            AlivcDownloadMediaInfo val = it.next();
            if (val.isCheckedState()) {
                it.remove();
            }
        }
        if (alivcDownloadingMediaInfos.size() <= 0) {
            sectionAdapter.removeSection(DOWNLOADING_TAG);
        }

        if (alivcDownloadMediaInfos.size() <= 0) {
            sectionAdapter.removeSection(DOWNLOADED_TAG);
        }

        sectionAdapter.notifyDataSetChanged();
        showDownloadContentView();
    }

    /**
     * 添加现在所有的
     *
     * @param alldownloadMediaInfos
     */
    public void addAllDownloadMediaInfo(List<AliyunDownloadMediaInfo> alldownloadMediaInfos) {
        if (alldownloadMediaInfos == null) {
            return;
        }

        // TODO: 2018/4/18 这不是一个正确的做法，做排序没有多大意义 业务需要永远保证正在下载的section在前
        //Collections.sort(alldownloadMediaInfos, new Comparator<AliyunDownloadMediaInfo>() {
        //    @Override
        //    public int compare(AliyunDownloadMediaInfo o1, AliyunDownloadMediaInfo o2) {
        //        if (o1.getStatus() == Status.Complete) {
        //            return 1;
        //        } else if (o2.getStatus() == Status.Complete){
        //            return 1;
        //        } else {
        //            return 0;
        //        }
        //    }
        //});

        for (AliyunDownloadMediaInfo downloadMediaInfo : alldownloadMediaInfos) {
            String tag = downloadMediaInfo.getStatus() == Status.Complete ? DOWNLOADED_TAG : DOWNLOADING_TAG;
            String title = downloadMediaInfo.getStatus() == Status.Complete ? getResources().getString(
                R.string.already_downloaded) : getResources().getString(R.string.download_caching);
            if (downloadMediaInfo.getStatus() == Status.Complete) {
                AlivcDownloadMediaInfo alivcDownloadMediaInfo = new AlivcDownloadMediaInfo();
                alivcDownloadMediaInfo.setAliyunDownloadMediaInfo(downloadMediaInfo);
                alivcDownloadMediaInfos.add(0, alivcDownloadMediaInfo);
                addSection(tag, title, alivcDownloadMediaInfos);
            } else {
                AlivcDownloadMediaInfo alivcDownloadMediaInfo = new AlivcDownloadMediaInfo();
                alivcDownloadMediaInfo.setAliyunDownloadMediaInfo(downloadMediaInfo);
                alivcDownloadingMediaInfos.add(0, alivcDownloadMediaInfo);
                addSection(tag, title, alivcDownloadingMediaInfos);
            }
        }

        sectionAdapter.notifyDataSetChanged();

        showDownloadContentView();

    }

    /**
     * 添加一个item
     *
     * @param downloadMedia
     */
    public void addDownloadMediaInfo(AliyunDownloadMediaInfo downloadMedia) {
        if (hasAdded(downloadMedia)) {
            return;
        }
        String tag = downloadMedia.getStatus() == Status.Complete ? DOWNLOADED_TAG : DOWNLOADING_TAG;
        String title = downloadMedia.getStatus() == Status.Complete ? getResources().getString(
            R.string.already_downloaded) : getResources().getString(R.string.download_caching);

        if (downloadMedia.getStatus() == Status.Complete) {
            AlivcDownloadMediaInfo alivcDownloadMediaInfo = new AlivcDownloadMediaInfo();
            alivcDownloadMediaInfo.setAliyunDownloadMediaInfo(downloadMedia);
            alivcDownloadMediaInfos.add(0, alivcDownloadMediaInfo);
            addSection(tag, title, alivcDownloadMediaInfos);
        } else {
            AlivcDownloadMediaInfo alivcDownloadMediaInfo = new AlivcDownloadMediaInfo();
            alivcDownloadMediaInfo.setAliyunDownloadMediaInfo(downloadMedia);
            alivcDownloadingMediaInfos.add(0, alivcDownloadMediaInfo);
            addSection(tag, title, alivcDownloadingMediaInfos);
        }

        sectionAdapter.notifyDataSetChanged();
        //sectionAdapter.notifyItemInsertedInSection(tag, 0);
        showDownloadContentView();

    }

    private void addSection(String tag, String title, final ArrayList<AlivcDownloadMediaInfo> alivcDownloadMediaInfos) {
        if (sectionAdapter.getSection(tag) == null) {
            section = new DownloadSection(this.context.get(), tag, title,
                alivcDownloadMediaInfos);
            section.setOnSectionItemClickListener(new OnSectionItemClickListener() {
                @Override
                public void onItemClick(int posion, String tag) {
                    int positionInSection = sectionAdapter.getPositionInSection(posion);
                    if (tag.equals(DOWNLOADING_TAG)) {
                        if (alivcDownloadingMediaInfos.get(positionInSection).isEditState()) {

                            isDownloadingChecked = !isDownloadingChecked;
                            alivcDownloadingMediaInfos.get(positionInSection).setCheckedState(isDownloadingChecked);
                            sectionAdapter.notifyItemChangedInSection(tag, positionInSection);

                            return;
                        }
                    } else if (tag.equals(DOWNLOADED_TAG)) {
                        if (alivcDownloadMediaInfos.get(positionInSection).isEditState()) {
                            alivcDownloadMediaInfos.get(positionInSection).setCheckedState(isDownloadedChecked);
                            isDownloadedChecked = !isDownloadedChecked;
                            sectionAdapter.notifyItemChangedInSection(tag, positionInSection);
                            return;
                        }
                    }

                    if (tag.equals(DOWNLOADING_TAG)) {
                        AliyunDownloadMediaInfo aliyunDownloadMediaInfo = alivcDownloadingMediaInfos.get(
                            positionInSection).getAliyunDownloadMediaInfo();
                        if (aliyunDownloadMediaInfo.getStatus() == Status.Start) {
                            if (onDownloadViewListener != null) {
                                //aliyunDownloadMediaInfo.setStatus(Status.Stop);
                                onDownloadViewListener.onStop(aliyunDownloadMediaInfo);
                                sectionAdapter.notifyItemChangedInSection(tag, positionInSection);
                            }
                        } else if (aliyunDownloadMediaInfo.getStatus() == Status.Stop) {
                            if (aliyunDownloadMediaInfo.getStatus() != Status.Complete) {
                                //aliyunDownloadMediaInfo.setStatus(Status.Start);
                                onDownloadViewListener.onStart(aliyunDownloadMediaInfo);
                                sectionAdapter.notifyItemChangedInSection(tag, positionInSection);
                            }
                        }
                    }

                    if (tag.equals(DOWNLOADED_TAG)) {
                        // 点击播放下载完成的视频
                        if (onDownloadItemClickListener != null) {
                            onDownloadItemClickListener.onDownloadedItemClick(positionInSection);
                        }
                    }

                    if (tag.equals(DOWNLOADING_TAG)) {
                        // 点击下载中的视频
                        if (onDownloadItemClickListener != null) {
                            onDownloadItemClickListener.onDownloadingItemClick(alivcDownloadMediaInfos,
                                positionInSection);
                        }
                    }
                }
            });

            sectionAdapter.addSection(tag, section);
        }
    }

    private OnDownloadItemClickListener onDownloadItemClickListener;

    public void setOnDownloadedItemClickListener(OnDownloadItemClickListener listener) {
        this.onDownloadItemClickListener = listener;
    }

    /**
     * 下载完成的视频item点击事件
     */
    public interface OnDownloadItemClickListener {
        void onDownloadedItemClick(int positin);

        void onDownloadingItemClick(ArrayList<AlivcDownloadMediaInfo> alivcDownloadMediaInfos, int position);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkbox_all_select) {
            for (AlivcDownloadMediaInfo info : alivcDownloadMediaInfos) {
                if (info != null) {
                    info.setCheckedState(isChecked);
                }
            }

            for (AlivcDownloadMediaInfo info : alivcDownloadingMediaInfos) {
                if (info != null) {
                    info.setCheckedState(isChecked);
                }
            }
            sectionAdapter.notifyDataSetChanged();
        }
    }

    private OnNotifyItemCheckedListener onItemCheckAllListener;

    public void setOnItemCheckAllListener(OnNotifyItemCheckedListener listener) {
        this.onItemCheckAllListener = listener;
    }

    public interface OnNotifyItemCheckedListener {
        void onItemCheck(boolean isChecked);
    }

    public void removeDownloadingMeiaInfo(AliyunDownloadMediaInfo downloadingMedia) {
        for (AlivcDownloadMediaInfo info : alivcDownloadingMediaInfos) {
            if (info.getAliyunDownloadMediaInfo().getVid() == downloadingMedia.getVid()) {
                alivcDownloadingMediaInfos.remove(info);
                break;
            }
        }

        if (alivcDownloadingMediaInfos.size() <= 0) {
            sectionAdapter.removeSection(DOWNLOADING_TAG);
        }
        sectionAdapter.notifyDataSetChanged();
    }

    /**
     * 判断是否已经存在
     *
     * @param info
     * @return
     */
    private boolean hasAdded(AliyunDownloadMediaInfo info) {
        for (AlivcDownloadMediaInfo downloadMediaInfo : alivcDownloadingMediaInfos) {
            if (info.getFormat().equals(downloadMediaInfo.getAliyunDownloadMediaInfo().getFormat()) &&
                info.getQuality().equals(downloadMediaInfo.getAliyunDownloadMediaInfo().getQuality()) &&
                info.getVid().equals(downloadMediaInfo.getAliyunDownloadMediaInfo().getVid()) &&
                info.isEncripted() == downloadMediaInfo.getAliyunDownloadMediaInfo().isEncripted()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新item的值
     *
     * @param aliyunDownloadMediaInfo
     */
    public void updateInfo(AliyunDownloadMediaInfo aliyunDownloadMediaInfo) {
        AlivcDownloadMediaInfo tmpInfo = null;
        for (AlivcDownloadMediaInfo info : alivcDownloadingMediaInfos) {
            if (info.getAliyunDownloadMediaInfo().getVid().equals(aliyunDownloadMediaInfo.getVid()) &&
                info.getAliyunDownloadMediaInfo().getQuality().equals(aliyunDownloadMediaInfo.getQuality()) &&
                info.getAliyunDownloadMediaInfo().getFormat().equals(aliyunDownloadMediaInfo.getFormat())) {
                tmpInfo = info;
                break;
            }
        }

        if (tmpInfo != null) {
            //tmpInfo.getAliyunDownloadMediaInfo().setSavePath(aliyunDownloadMediaInfo.getSavePath());
            tmpInfo.getAliyunDownloadMediaInfo().setProgress(aliyunDownloadMediaInfo.getProgress());
            tmpInfo.getAliyunDownloadMediaInfo().setStatus(aliyunDownloadMediaInfo.getStatus());
        }

        //int sectionPosition = sectionAdapter.getSectionPosition(section);
        //sectionAdapter.notifyItemChanged(sectionPosition);
        sectionAdapter.notifyDataSetChanged();
    }

    public void updateProgress() {

    }

    /**
     * 切换为编辑状态
     */
    public void changeDownloadEditState(boolean isEdit) {
        for (AlivcDownloadMediaInfo info : alivcDownloadMediaInfos) {
            if (info != null) {
                info.setEditState(isEdit);
            }
        }

        for (AlivcDownloadMediaInfo info : alivcDownloadingMediaInfos) {
            if (info != null) {
                info.setEditState(isEdit);
            }
        }

        rlDownloadManagerEdit.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        rlDownloadManagerEditDefault.setVisibility(isEdit ? View.GONE : View.VISIBLE);
        sectionAdapter.notifyDataSetChanged();
        //showDownloadContentView();
    }

    /**
     * 下载完成更新
     *
     * @param aliyunDownloadMediaInfo
     */
    public void updateInfoByComplete(AliyunDownloadMediaInfo aliyunDownloadMediaInfo) {
        if (aliyunDownloadMediaInfo.getStatus() == Status.Complete) {
            removeDownloadingMeiaInfo(aliyunDownloadMediaInfo);
            addDownloadMediaInfo(aliyunDownloadMediaInfo);
        }
        showDownloadContentView();
        sectionAdapter.notifyDataSetChanged();

    }

    /**
     * 下载出错
     *
     * @param info
     */
    public void updateInfoByError(AliyunDownloadMediaInfo info) {
        if (info.getStatus() == Status.Error) {
            sectionAdapter.notifyDataSetChanged();
            showDownloadContentView();
        }

    }

    /**
     * 根据是否有数据判断是否显示downloadEmptyView
     */
    public void showDownloadContentView() {
        if (alivcDownloadMediaInfos.size() > 0 || alivcDownloadingMediaInfos.size() > 0) {

            downloadEmptyView.setVisibility(View.GONE);
            rlDownloadManagerContent.setVisibility(View.VISIBLE);
            rlDownloadManagerEdit.setVisibility(VISIBLE);
            rlDownloadManagerEditDefault.setVisibility(VISIBLE);
        } else if (alivcDownloadMediaInfos.size() <= 0 || alivcDownloadingMediaInfos.size() <= 0) {

            downloadEmptyView.setVisibility(View.VISIBLE);
            rlDownloadManagerContent.setVisibility(View.GONE);
            rlDownloadManagerEdit.setVisibility(GONE);
            rlDownloadManagerEditDefault.setVisibility(GONE);

        }
    }

    private OnDownloadViewListener onDownloadViewListener;

    public void setOnDownloadViewListener(
        OnDownloadViewListener onDownloadViewListener) {
        this.onDownloadViewListener = onDownloadViewListener;
    }

    public interface OnDownloadViewListener {
        void onStop(AliyunDownloadMediaInfo downloadMediaInfo);

        void onStart(AliyunDownloadMediaInfo downloadMediaInfo);

        void onDeleteDownloadInfo(ArrayList<AlivcDownloadMediaInfo> alivcDownloadMediaInfos);
    }


}
