/*
 * Copyright 2016 jeasonlzy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.wildfire.imshat.discovery.download.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.discovery.download.IPFSModel;
import cn.wildfire.imshat.discovery.download.LogDownloadListener;
import cn.wildfire.imshat.discovery.download.ProgressButton;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfirechat.imshat.R;


public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_FINISH = 1;
    public static final int TYPE_ING = 2;

    private List<DownloadTask> values;
    private NumberFormat numberFormat;
    private LayoutInflater inflater;
    private Context context;
    private int type;

    public DownloadAdapter(Context context) {
        this.context = context;
        numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(2);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateData(int type) {
        
        this.type = type;
        if (type == TYPE_ALL) values = OkDownload.restore(DownloadManager.getInstance().getAll());
        if (type == TYPE_FINISH) values = OkDownload.restore(DownloadManager.getInstance().getFinished());
        if (type == TYPE_ING) values = OkDownload.restore(DownloadManager.getInstance().getDownloading());
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_download_manager_plug, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownloadTask task = values.get(position);
        String tag = createTag(task);
        task.register(new ListDownloadListener(tag, holder))//
                .register(new LogDownloadListener());
        holder.setTag(tag);
        holder.setTask(task);
        holder.bind();
        holder.refresh(task.progress);
    }

    public void unRegister() {
        Map<String, DownloadTask> taskMap = OkDownload.getInstance().getTaskMap();
        for (DownloadTask task : taskMap.values()) {
            task.unRegister(createTag(task));
        }
    }

    private String createTag(DownloadTask task) {
        return type + "_" + task.progress.tag;
    }

    @Override
    public int getItemCount() {
        return values == null ? 0 : values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.icon)
        ImageView icon;
        @Bind(R.id.plug_name)
        TextView name;
        @Bind(R.id.start)
        ProgressButton download;
        private DownloadTask task;
        private String tag;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setTask(DownloadTask task) {
            this.task = task;
        }

        public void bind() {
            Progress progress = task.progress;
            GlideApp.with(context).load("xxxx").error(R.mipmap.icon_plug).into(icon);
            IPFSModel ipfsModel = (IPFSModel) progress.extra1;
            name.setText(ipfsModel.getTitle());
        }

        public void refresh(Progress progress) {
            String currentSize = Formatter.formatFileSize(context, progress.currentSize);
            String totalSize = Formatter.formatFileSize(context, progress.totalSize);

            switch (progress.status) {
                case Progress.NONE:

                    download.setText("Download");
                    break;
                case Progress.PAUSE:

                    download.setText("Continue");
                    break;
                case Progress.ERROR:

                    download.setText("Error");
                    break;
                case Progress.WAITING:

                    download.setText("Wait");
                    break;
                case Progress.FINISH:
                    download.setText("Success");
                    break;
                case Progress.LOADING:
                    download.setText("Pause");
                    Logger.e("---------Progress.LOADING------->"+numberFormat.format(progress.fraction));
                    download.setProgress((int) (progress.fraction * 100));
                    break;
            }

        }

        @OnClick(R.id.start)
        public void start() {
            Progress progress = task.progress;
            switch (progress.status) {
                case Progress.PAUSE:
                case Progress.NONE:
                case Progress.ERROR:
                    task.start();
                    break;
                case Progress.LOADING:
                    task.pause();
                    break;
                case Progress.FINISH:

                    break;
            }
            refresh(progress);
        }

//        @OnClick(R.id.remove)
//        public void remove() {
//            task.remove(true);
//            updateData(type);
//        }

//        @OnClick(R.id.restart)
//        public void restart() {
//            task.restart();
//        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    private class ListDownloadListener extends DownloadListener {

        private ViewHolder holder;

        ListDownloadListener(Object tag, ViewHolder holder) {
            super(tag);
            this.holder = holder;
        }

        @Override
        public void onStart(Progress progress) {
        }

        @Override
        public void onProgress(Progress progress) {
            if (tag == holder.getTag()) {
                holder.refresh(progress);
            }
        }

        @Override
        public void onError(Progress progress) {
            Throwable throwable = progress.exception;
            if (throwable != null) throwable.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {
            Toast.makeText(context, "Download success:" + progress.filePath, Toast.LENGTH_SHORT).show();
            updateData(type);
        }

        @Override
        public void onRemove(Progress progress) {
        }
    }
}
