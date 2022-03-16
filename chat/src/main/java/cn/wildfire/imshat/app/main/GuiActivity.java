package cn.wildfire.imshat.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.base.utils.ACacheUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.widget.indicator.CommonPagerAdapter;
import cn.wildfire.imshat.kit.widget.indicator.CustomIndicator;
import cn.wildfire.imshat.kit.widget.indicator.IndicatorItem;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;

import static cn.wildfire.imshat.kit.widget.indicator.CustomIndicator.INDICATOR_TYPE_SCALE_AND_GRADUAL;
import static cn.wildfire.imshat.kit.widget.indicator.CustomIndicator.INDICATOR_TYPE_SPLIT;

public class GuiActivity  extends AppCompatActivity {

    private ViewPager gui_vp;
    private CustomIndicator indicator;
    private int[] resIds;
    private int[] tips;
    private TextView tv_guide_skip;
    private Button btn_guide_enter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView( R.layout.activity_guide);
        gui_vp=findViewById(R.id.gui_vp);
        indicator=findViewById(R.id.indicator);
        tv_guide_skip=findViewById(R.id.tv_guide_skip);
        btn_guide_enter=findViewById(R.id.btn_guide_enter);
        
        resIds=new int[]{R.mipmap.uoko_guide_background_1,R.mipmap.uoko_guide_background_2,R.mipmap.uoko_guide_background_3};
        tips=new int[]{R.string.str_gui_1,R.string.str_gui_2,R.string.str_gui_3};
        initIndicator();
    }

    private void initIndicator(){

        List<IndicatorItem> datas=new ArrayList<>();
        for(int i=0;i<3;i++){

            datas.add(new IndicatorItem(resIds[i],tips[i]));
        }


        CommonPagerAdapter<IndicatorItem> adapter = new CommonPagerAdapter<IndicatorItem>(datas) {

            @Override
            public void renderItemView(@NonNull View itemView, int position) {
                Logger.e("---------getBindItemData(position)--->"+ JsonUtil.toJson(getBindItemData(position)));
                String str=getString(getBindItemData(position).getTip());
                Logger.e("-------------str------>"+str);
                ((TextView)itemView.findViewById(R.id.descrip)).setText(str);
                ((ImageView)itemView.findViewById(R.id.imgbg)).setBackgroundResource(getBindItemData(position).getResId());

            }

            @NonNull
            @Override
            public View getPageItemView(@NonNull ViewGroup container, int position) {
                View view=LayoutInflater.from(container.getContext()).inflate(R.layout.item_guide,null);
                view.setTag(String.valueOf(position));
                return view;
            }
        };
        adapter.setOnItemClickListener((view, position) -> {

        });
        gui_vp.setAdapter(adapter);
        gui_vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==adapter.getCount()-1){
                    btn_guide_enter.setVisibility(View.VISIBLE);
                    tv_guide_skip.setVisibility(View.GONE);

                }else{
                    btn_guide_enter.setVisibility(View.GONE);
                    tv_guide_skip.setVisibility(View.VISIBLE);


                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        indicator.bindViewPager(gui_vp);
        indicator.setIndicatorType(INDICATOR_TYPE_SCALE_AND_GRADUAL);

        initEvent();
    }


    private void initEvent(){
        tv_guide_skip.setOnClickListener(v->{
            ACacheUtil.get().put("frist",false);
            finish();
            startActivity(new Intent(this,SplashActivity.class));
        });
        btn_guide_enter.setOnClickListener(v->{
                    ACacheUtil.get().put("frist",false);
                    finish();
                    startActivity(new Intent(this,SplashActivity.class));
                }
             );
    }



    }



