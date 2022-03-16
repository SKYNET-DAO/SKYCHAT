package cn.wildfire.imshat.discovery.download;

import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.recyclerview.LQRRecyclerView;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cn.wildfire.imshat.discovery.download.adapter.DownloadAdapter;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfirechat.imshat.R;

public class PlugTaskListActivity extends WfcBaseActivity {


    private LQRAdapterForRecyclerView<DownloadTask> adapter;
    private LQRRecyclerView exchangedatas;

    @Override
    protected int contentLayout() {

        return R.layout.activity_plug;

    }

    @Override
    protected void afterViews() {
        exchangedatas=findViewById(R.id.exchangedatas);
        adapter=new LQRAdapterForRecyclerView<DownloadTask>(this,new ArrayList<>(),R.layout.item_del) {
            @Override
            public void convert(LQRViewHolderForRecyclerView helper, DownloadTask item, int position) {
               ImageView imv= helper.getView(R.id.icon);
               TextView title= helper.getView(R.id.title);
               Button btn=helper.getView(R.id.del);
               IPFSModel value= (IPFSModel) item.progress.extra1;


                GlideApp.with(PlugTaskListActivity.this).load(value.getIconUrl()).into(imv);
                title.setText(value.getTitle());
                btn.setOnClickListener(v->{
                    item.remove(true);
                    removeItem(position);
                    adapter.notifyDataSetChanged();
                });

            }
        };
        exchangedatas.setAdapter(adapter);

        initData();

    }

    private void initData(){

       List<DownloadTask> finishtask= OkDownload.restore(DownloadManager.getInstance().getFinished());
       adapter.addNewData(finishtask);

    }




}
