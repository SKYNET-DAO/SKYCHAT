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
import android.content.Intent;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.ipfsclient.IPFSManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import com.orhanobut.logger.Logger;
import com.vondear.rxtool.RxAppTool;
import com.vondear.rxtool.RxFileTool;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import cn.wildfire.imshat.discovery.download.Constants;
import cn.wildfire.imshat.discovery.download.IPFSModel;
import cn.wildfire.imshat.discovery.download.LogDownloadListener;
import cn.wildfire.imshat.discovery.download.ProgressButton;
import cn.wildfire.imshat.discovery.download.WebActivity;
import cn.wildfire.imshat.discovery.download.WfcPluginWebActivity;
import cn.wildfire.imshat.discovery.listener.UnzipCallback;
import cn.wildfire.imshat.discovery.util.PluginUtils;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.net.AppConst;
import cn.wildfire.imshat.wallet.utils.NetworkUtil;
import cn.wildfirechat.imshat.R;

import static cn.wildfire.imshat.discovery.download.Constants.PLUGIN_DIR;


public class VDownloadListAdapter extends RecyclerView.Adapter<VDownloadListAdapter.ViewHolder> {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_FINISH = 1;
    public static final int TYPE_ING = 2;

    private List<IPFSModel> datas;
    private List<DownloadTask> values;
    private NumberFormat numberFormat;
    private LayoutInflater inflater;
    private Context context;
    private int type = 1;

    public VDownloadListAdapter(Context context, List<IPFSModel> model) {
        this.context = context;
        this.datas = model;
        numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(2);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        updateData(1);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_home_loadmore, parent, false);
        return new ViewHolder(view);
    }

    public void addMoreData(List<IPFSModel> data) {
        if (datas != null) datas.addAll(data);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IPFSModel bean = datas.get(position);
        if (holder.getTag() == null) {
            DownloadTask task = OkDownload.getInstance().getTask(bean.getSign());
            if (task == null) {
                String tag = bean.getSign();
                holder.title.setText(datas.get(position).getTitle());
                holder.download.setText(R.string.str_install);
                Logger.e("----1111---->" + datas.get(position).getIconUrl());
                holder.download.setOnClickListener(v -> {
                    if(!NetworkUtil.isNetworkAvailable(context)){
                        Toast.makeText(context,R.string.str_net_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    StartDownload(holder, bean, position, tag);
                });
//                holder.bindDefault(datas.get(position));

            } else {
                String tag = createTag(task);
                task.register(new ListDownloadListener(tag, holder,bean))//
                        .register(new LogDownloadListener());
                holder.setTag(tag);
                holder.setTask(task);
                holder.bind();
                holder.refresh(task.progress,bean);
            }

        } else {
            holder.refresh(holder.getTask().progress,bean);

        }
        GlideApp.with(context).load(datas.get(position).getIconUrl()).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_def)).into(holder.icon);



    }


    public void unRegister() {
        Map<String, DownloadTask> taskMap = OkDownload.getInstance().getTaskMap();
        for (DownloadTask task : taskMap.values()) {
            task.unRegister(createTag(task));
        }
    }

    private String createTag(DownloadTask task) {
        return task.progress.tag;
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView subtitle2;
        ProgressButton download;
        private DownloadTask task;
        private String tag;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            subtitle2 = itemView.findViewById(R.id.subtitle2);
            download = itemView.findViewById(R.id.download);


        }

        public void setTask(DownloadTask task) {
            this.task = task;
        }

        public DownloadTask getTask() {
            return task;
        }


        public void bind() {
            Progress progress = task.progress;
            IPFSModel model = (IPFSModel) progress.extra1;
            Glide.with(context).load(model.getIconUrl()).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_def)).into(icon);
            title.setText(model.getTitle());
            subtitle2.setText(Formatter.formatFileSize(context, progress.currentSize));
        }


        public void refresh(Progress progress,IPFSModel newbean) {
            String currentSize = Formatter.formatFileSize(context, progress.currentSize);
            String totalSize = Formatter.formatFileSize(context, progress.totalSize);

            switch (progress.status) {
                case Progress.NONE:
                    Logger.e("----Progress.NONE---->");
                    download.setText(R.string.str_only_download);
                    download.setProgress((int) (progress.fraction * 100));
                    break;
                case Progress.PAUSE:
                    Logger.e("----Progress.PAUSE---->");
                    download.setText(R.string.str_download_continue);
                    download.setProgress((int) (progress.fraction * 100));
                    break;
                case Progress.ERROR:
                    Logger.e("----Progress.ERROR---->");

                    download.setText(R.string.str_download_error);
                    download.setOnClickListener(v -> {
                        if (task != null) task.restart();

                    });
                    break;
                case Progress.WAITING:
                    Logger.e("----Progress.WAITING---->");

                    download.setText(R.string.str_download_wait);
                    download.setEnabled(false);
                    download.setProgress((int) (progress.fraction * 100));
                    break;
                case Progress.FINISH:
                    download.setEnabled(true);
                    Logger.e("----Progress.FINISH---->");
                    if (!RxFileTool.isFileExists(task.progress.filePath)) {
                        
                        download.setText(R.string.str_install);
                        download.setOnClickListener(v -> {
                            task.remove(true);
                            StartDownload(this, newbean, this.getPosition(), newbean.getSign());

                        });

                    }
                    else if(RxFileTool.isFileExists(PLUGIN_DIR+"/"+progress.tag)){
                        IPFSModel item=(IPFSModel) progress.extra1;

                        if(Integer.parseInt(newbean.getVersion())>Integer.parseInt(item.getVersion())){
                            download.setText(R.string.str_only_update);
                            download.setOnClickListener(v -> {
                                task.remove(true);
                                StartDownload(this, newbean, this.getPosition(), newbean.getSign());


                            });

                        }else{
                            if(Integer.parseInt(newbean.getVersion())>Integer.parseInt(item.getVersion())){
                                Toast.makeText(context,R.string.str_need_update,Toast.LENGTH_SHORT).show();
                                return;
                            }

                            download.setText(R.string.str_open_app);
                            download.setOnClickListener(v -> {
                                WebActivity.Companion.loadUrl(context,item);

                            });

                        }

                    }

                    break;
                case Progress.LOADING:
                    download.setText(R.string.str_pause);
                    download.setEnabled(false);
                    Logger.e("---------Progress.LOADING------->" + numberFormat.format(progress.fraction));
                    download.setProgress((int) (progress.fraction * 100));
                    download.setText((int) (progress.fraction * 100) + "%");
                    break;
            }

        }

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
           // refresh(progress);
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
        private IPFSModel bean;



        ListDownloadListener(Object tag, ViewHolder holder,IPFSModel bean) {
            super(tag);
            this.holder = holder;
            this.bean=bean;
        }

        @Override
        public void onStart(Progress progress) {
        }

        @Override
        public void onProgress(Progress progress) {
            if (tag == holder.getTag()) {
                holder.refresh(progress,bean);
            }
        }

        @Override
        public void onError(Progress progress) {
            Throwable throwable = progress.exception;
            if (throwable != null) throwable.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {
           

            updateData(TYPE_FINISH);
            holder.download.setEnabled(false);
            holder.download.setText(R.string.str_ziping);
            
            PluginUtils.saveAndUnzip(progress.tag, progress.filePath, new UnzipCallback() {
                @Override
                public void zipSuccess() {
                    holder.download.setEnabled(true);
                    holder.download.setText(R.string.str_open_app);
                }

                @Override
                public void zipFail() {
                    holder.download.setText(R.string.str_zip_error);
                }
            });
        }

        @Override
        public void onRemove(Progress progress) {
        }
    }


    public void StartDownload(ViewHolder holder, IPFSModel model, int position, String tag) {

        
        String catStr = IPFSManager.getInstance().catUrl(model.getUrl());

        GetRequest<File> request = OkGo.get(catStr);//
//                .headers("aaa", "111")//
//                .params("bbb", "222");

        
        Logger.e("----model.getDownloadurl()---->" + catStr);
        DownloadTask task = OkDownload.request(tag, request)//
//                .priority(ipfsModel.getPriority())//
                .extra1(model)//
                .folder(PLUGIN_DIR)
                .fileName(tag + ".gz")
                .save()//
                .register(new ListDownloadListener(tag, holder,model));//

        task.start();
        holder.setTag(tag);
        holder.setTask(task);


        notifyItemChanged(position);
    }


    public void updateData(int type) {
        
        this.type = type;
        if (type == TYPE_ALL) values = OkDownload.restore(DownloadManager.getInstance().getAll());
        if (type == TYPE_FINISH)
            values = OkDownload.restore(DownloadManager.getInstance().getFinished());
        if (type == TYPE_ING)
            values = OkDownload.restore(DownloadManager.getInstance().getDownloading());
        notifyDataSetChanged();

    }

}
