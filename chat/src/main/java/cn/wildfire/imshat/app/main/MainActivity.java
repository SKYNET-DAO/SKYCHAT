package cn.wildfire.imshat.app.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.base.utils.CompareVersion;
import com.android.ipfsclient.IPFSManager;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.king.zxing.Intents;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.simple.eventbus.Subscriber;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;

import cn.wildfire.imshat.app.MyApp;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcScheme;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.channel.ChannelInfoActivity;
import cn.wildfire.imshat.kit.contact.ContactFragment;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.contact.newfriend.SearchUserActivity;
import cn.wildfire.imshat.kit.conversation.CreateConversationActivity;
import cn.wildfire.imshat.kit.conversationlist.ConversationListFragment;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.imshat.kit.group.GroupInfoActivity;
import cn.wildfire.imshat.kit.qrcode.ScanQRCodeActivity;
import cn.wildfire.imshat.kit.search.SearchPortalActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.ChangeMyNameActivity;
import cn.wildfire.imshat.kit.user.UserInfoActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfire.imshat.kit.utils.ManifestUtil;
import cn.wildfire.imshat.kit.widget.ViewPagerFixed;
import cn.wildfire.imshat.net.AppConst;
import cn.wildfire.imshat.net.api.ImRetrofitService;
import cn.wildfire.imshat.net.bean.AppInfoBean;
import cn.wildfire.imshat.net.bean.RandomIp;
import cn.wildfire.imshat.net.helper.rxjavahelper.RxObserver;
import cn.wildfire.imshat.net.model.base.bean.ResponseData;
import cn.wildfire.imshat.net.model.base.bean.ResponseDataOther;
import cn.wildfire.imshat.wallet.BitUtil;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfire.imshat.wallet.services.WalletService;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import io.reactivex.functions.Consumer;
import me.leolin.shortcutbadger.ShortcutBadger;
import q.rorbin.badgeview.QBadgeView;

import static cn.wildfire.imshat.net.AppConst.BASE_URL;

public class MainActivity extends WfcBaseActivity implements ViewPager.OnPageChangeListener {

    private List<Fragment> mFragmentList = new ArrayList<>(4);

    @Bind(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;
    @Bind(R.id.vpContent)
    ViewPagerFixed mVpContent;

    private QBadgeView unreadMessageUnreadBadgeView;
    private QBadgeView unreadFriendRequestBadgeView;

    private static final int REQUEST_CODE_SCAN_QR_CODE = 100;
    private static final int REQUEST_IGNORE_BATTERY_CODE = 101;

    private ConversationListFragment conversationListFragment;
    private ConversationListViewModel conversationListViewModel;
    private ContactFragment contactFragment;
    private DiscoveryFragment discoveryFragment;
    private MeFragment meFragment;

    private ContactViewModel contactViewModel;



    Observer<UnreadCount> unreadCountObserver=unreadCount -> {

        Logger.e("------unreadCountObserver----->"+JsonUtil.toJson(unreadCount));
        if (unreadCount != null && unreadCount.unread > 0) {
            showUnreadMessageBadgeView(unreadCount.unread);
            ShortcutBadger.applyCount(this,unreadCount.unread);
        } else {
            hideUnreadMessageBadgeView();
            ShortcutBadger.applyCount(this,0);
        }
    };

    @Override
    protected int contentLayout() {
        return R.layout.main_activity;
    }

    @Override
    protected void afterViews() {
        initView();
         Logger.e("------onCreate------>");
         conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel), Arrays.asList(0)))
                .get(ConversationListViewModel.class);
        conversationListViewModel.unreadCountLiveData().observeForever(unreadCountObserver);

         contactViewModel = WfcUIKit.getAppScopeViewModel(ContactViewModel.class);
        contactViewModel.friendRequestUpdatedLiveData().observe(this, count -> {
            if (count == null || count == 0) {
                hideUnreadFriendRequestBadgeView();
            } else {
                showUnreadFriendRequestBadgeView(count);
            }
        });

        if (checkDisplayName()) {
            ignoreBatteryOption();
        }

        onUpdateunreadRequestNot();


    }


    @Subscriber(tag = "ACTION_UPDATE_REQUEST_FRIEND")
    public void onUpdateunreadRequest(String count){
        if (count == null || count.equals("0")) {
            hideUnreadFriendRequestBadgeView();
        } else {
            showUnreadFriendRequestBadgeView(Integer.valueOf(count));
        }
    }


    
    @Subscriber(tag = "ACTION_UPDATE_REQUEST_FRIEND_NOT")
    public void onUpdateunreadRequestNot(){
        List<FriendRequest>  requests=contactViewModel.getFriendRequest();
        Logger.e("---------requests---->"+JsonUtil.toJson(requests));
        if(requests==null||requests.size()==0)return;
        int unReadRequest=0;
        for (FriendRequest item:requests) {

            if(item.readStatus==0)unReadRequest++;
        }
        if ( unReadRequest==0) {
            hideUnreadFriendRequestBadgeView();
        } else {
            showUnreadFriendRequestBadgeView(Integer.valueOf(unReadRequest));
        }


    }

    private void showUnreadMessageBadgeView(int count) {
        if (unreadMessageUnreadBadgeView == null) {
            BottomNavigationMenuView bottomNavigationMenuView = ((BottomNavigationMenuView) bottomNavigationView.getChildAt(0));
            View view = bottomNavigationMenuView.getChildAt(0);
            unreadMessageUnreadBadgeView = new QBadgeView(MainActivity.this);
            unreadMessageUnreadBadgeView.setGravityOffset(UIUtils.dip2Px(20),0,false);
            unreadMessageUnreadBadgeView.bindTarget(view);
        }
        unreadMessageUnreadBadgeView.setBadgeNumber(count);
    }

    private void hideUnreadMessageBadgeView() {
        if (unreadMessageUnreadBadgeView != null) {
            unreadMessageUnreadBadgeView.hide(true);
        }
    }


    private void showUnreadFriendRequestBadgeView(int count) {
        if (unreadFriendRequestBadgeView == null) {
            BottomNavigationMenuView bottomNavigationMenuView = ((BottomNavigationMenuView) bottomNavigationView.getChildAt(0));
            View view = bottomNavigationMenuView.getChildAt(1);
            unreadFriendRequestBadgeView = new QBadgeView(MainActivity.this);
            unreadFriendRequestBadgeView.setGravityOffset(UIUtils.dip2Px(20),0,false);
            unreadFriendRequestBadgeView.bindTarget(view);
        }
        unreadFriendRequestBadgeView.setBadgeNumber(count);
    }

    public void hideUnreadFriendRequestBadgeView() {
        if (unreadFriendRequestBadgeView != null) {
            unreadFriendRequestBadgeView.hide(true);
            unreadFriendRequestBadgeView = null;
        }
    }

    @Override
    protected int menu() {
        return R.menu.main;
    }

    @Override
    protected boolean showHomeMenuItem() {
        return false;
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK&&event.getRepeatCount() == 0) {
            moveTaskToBack(true);
            
//            Intent intent= new Intent(Intent.ACTION_MAIN);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            startActivity(intent);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Logger.e("--------onBackPressed--->");
    }

    private void initView() {
        setTitle(UIUtils.getString(R.string.app_name_skychat));

        
        mVpContent.setOffscreenPageLimit(3);

        conversationListFragment = new ConversationListFragment();
        contactFragment = new ContactFragment();
        discoveryFragment = new DiscoveryFragment();
        meFragment = new MeFragment();
        mFragmentList.add(conversationListFragment);
        mFragmentList.add(contactFragment);
        mFragmentList.add(discoveryFragment);
        mFragmentList.add(meFragment);
        mVpContent.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
        mVpContent.setOnPageChangeListener(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.conversation_list:
                    mVpContent.setCurrentItem(0);
                    break;
                case R.id.contact:
                    mVpContent.setCurrentItem(1);
                    break;
                case R.id.discovery:
                    mVpContent.setCurrentItem(2);
                    break;
                case R.id.me:
                    mVpContent.setCurrentItem(3);
                    break;
                default:
                    break;
            }
            return true;
        });



        //checkupdate
        checkUpdateVersion();




    }





    @Override
    protected void onResume() {
        super.onResume();
        Logger.e("------onResume------>");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Logger.e("------onPause------>");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.e("------onStop------>");
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                showSearchPortal();
                break;
            case R.id.chat:
                createConversation();
                break;
            case R.id.add_contact:
                searchUser();
                break;
            case R.id.scan_qrcode:
                startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_SCAN_QR_CODE);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchPortal() {
        Intent intent = new Intent(this, SearchPortalActivity.class);
        startActivity(intent);
    }

    private void createConversation() {
        Intent intent = new Intent(this, CreateConversationActivity.class);
        startActivity(intent);
    }

    private void searchUser() {
        Intent intent = new Intent(this, SearchUserActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                bottomNavigationView.setSelectedItemId(R.id.conversation_list);
                break;
            case 1:
                bottomNavigationView.setSelectedItemId(R.id.contact);
                break;
            case 2:
                bottomNavigationView.setSelectedItemId(R.id.discovery);
                break;
            case 3:
                bottomNavigationView.setSelectedItemId(R.id.me);
                break;
            default:
                break;
        }
        contactFragment.showQuickIndexBar(position == 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            
            contactFragment.showQuickIndexBar(false);
        } else {
            contactFragment.showQuickIndexBar(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    onScanPcQrCode(result);
                }
                break;
            case REQUEST_IGNORE_BATTERY_CODE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, UIUtils.getString(R.string.str_skychat_onback), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void onScanPcQrCode(String qrcode) {
        String prefix = qrcode.substring(0, qrcode.lastIndexOf('/') + 1);
        String value = qrcode.substring(qrcode.lastIndexOf("/") + 1);
//        Uri uri = Uri.parse(value);
//        uri.getAuthority();
//        uri.getQuery()
        switch (prefix) {
            case WfcScheme.QR_CODE_PREFIX_PC_SESSION:
                pcLogin(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_USER:
                showUser(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_GROUP:
                joinGroup(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_CHANNEL:
                subscribeChannel(value);
                break;
            default:
                Toast.makeText(this, "qrcode: " + qrcode, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void pcLogin(String token) {
        Intent intent = new Intent(this, PCLoginActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    private void showUser(String uid) {

        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        UserInfo userInfo = userViewModel.getUserInfo(uid, true);
        if (userInfo == null) {
            return;
        }
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    private void joinGroup(String groupId) {
        Intent intent = new Intent(this, GroupInfoActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void subscribeChannel(String channelId) {
        Intent intent = new Intent(this, ChannelInfoActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }

    private boolean checkDisplayName() {
        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        UserInfo userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        if (userInfo != null && TextUtils.equals(userInfo.displayName, userInfo.mobile)) {
            if (!sp.getBoolean("updatedDisplayName", false)) {
                sp.edit().putBoolean("updatedDisplayName", true).apply();
                updateDisplayName();
                return false;
            }
        }
        return true;
    }




    public boolean checkDisplayName(UserInfo userInfo) {
        //  UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        // UserInfo userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);

        if (userInfo != null && TextUtils.equals(userInfo.displayName, userInfo.mobile)) {
            Logger.e("--------pop 1>"+ JsonUtil.toJson(userInfo));
            //  updateDisplayName();
            if (!sp.getBoolean("updatedDisplayName", false)) {
                sp.edit().putBoolean("updatedDisplayName", true).apply();
                updateDisplayName();
                return false;

            }
        }
        return true;
    }

    private void updateDisplayName() {

        Logger.e("--------POP 2>");
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.str_remeber_nickname))
                .positiveText(getString(R.string.str_modify))

                
                .cancelable(false)
                .onPositive((dialog1, which) -> {
                    Intent intent = new Intent(MainActivity.this, ChangeMyNameActivity.class);
                    startActivity(intent);
                }).build();
        dialog.show();
    }


    private void ignoreBatteryOption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivityForResult(intent, REQUEST_IGNORE_BATTERY_CODE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e("-------onDestroy----->");
        conversationListViewModel.unreadCountLiveData().removeObserver(unreadCountObserver);
        WalletService.stopSelf(this);

    }


    @SuppressLint("CheckResult")
    private void checkUpdateVersion(){
           String url=AppConst.BASE_URL + ImRetrofitService.version;
           Logger.e("------checkUpdateVersion------>"+url);
          ImRetrofitService.requestAppUpdate(url)
                .subscribe(bean -> {

                    if(bean==null||bean.getData()==null)return;
                    Logger.e("-----checkUpdateVersion_onNext------>"+JsonUtil.toJson(bean));

                    String currentversion = ManifestUtil.getVersionName(MyApp.getContext());
                    String remoteversion=bean.getData().getVersion();

                    Logger.e("-----currentversion and remoteversion------>"+currentversion+"  "+remoteversion);
                    Logger.e("-----currentversion and compareVersion------>"+CompareVersion.compareVersion(currentversion,remoteversion));

                    if(bean!=null&& CompareVersion.compareVersion(currentversion,remoteversion)==-1){
                        showDownloadDialog(BASE_URL+bean.getData().getDownloadurl());
                    }
                });

    }

    private void showDownloadDialog(String downloadUrl){

        MaterialDialog materialDialog=new  MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.str_update_version))
                .titleColor(getResources().getColor(R.color.colorPrimary))
                .content(getResources().getString(R.string.str_has_version_new))
                .progress(false,100,true)
                .positiveText(getResources().getString(R.string.str_download_update))
                .cancelable(false)
                .autoDismiss(false)
                .onPositive((d,w)->{
                    d.getActionButton(w).setVisibility(View.GONE);//gone the actionbtn
                    //star download
                    startDownload(d,downloadUrl);

                })
                .build();
        materialDialog.show();

    }

    private void startDownload(MaterialDialog materialDialog,String downloadUrl){
        OkGo.<File>get(downloadUrl)
                .tag(this)
                .execute(new FileCallback() {
                    @Override
                    public void onSuccess(Response<File> response) {
                        Logger.d("------Download successful------>"+response.body());
                        
                        startInstallActivity(response);

                    }

                    @Override
                    public void onError(Response<File> response) {

                        Logger.d("------download fail------>"+response.body());


                    }

                    @Override
                    public void downloadProgress(Progress progress) {


                        int value = (int) (100 * ((float)progress.currentSize / (float) progress.totalSize));
                        materialDialog.setProgress(value);
                    }

                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        Logger.d("------downloading------>");

                    }
                });

    }

    private void startInstallActivity(Response<File> response){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { 
                Uri apkUri = FileProvider.getUriForFile(this, "cn.wildfirechat.imshat.provider", response.body());  
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(response.body()), "application/vnd.android.package-archive");
            }
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }




}
