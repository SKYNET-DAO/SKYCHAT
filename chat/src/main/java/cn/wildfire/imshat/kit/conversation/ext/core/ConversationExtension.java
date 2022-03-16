package cn.wildfire.imshat.kit.conversation.ext.core;

import android.content.Intent;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.imshat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ConversationViewModel;
import cn.wildfire.imshat.kit.widget.ViewPagerFixed;
import cn.wildfirechat.model.Conversation;

public class ConversationExtension {
    private FragmentActivity activity;
    private Conversation conversation;
    private FrameLayout containerLayout;
    private ViewPagerFixed extViewPager;
    private List<ConversationExt> exts;

    private boolean hideOnScroll = true;


    public ConversationExtension(FragmentActivity activity, FrameLayout inputContainerLayout, ViewPagerFixed extViewPager) {
        this.activity = activity;
        this.containerLayout = inputContainerLayout;
        this.extViewPager = extViewPager;
    }

    private void onConversationExtClick(ConversationExt ext) {
        List<ExtMenuItemWrapper> extMenuItems = new ArrayList<>();
        Method[] allMethods = ext.getClass().getDeclaredMethods();
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(ExtContextMenuItem.class)) {
                ExtContextMenuItem item = method.getAnnotation(ExtContextMenuItem.class);
                extMenuItems.add(new ExtMenuItemWrapper(item, method));
            }
        }
        if (extMenuItems.size() > 0) {
            if (extMenuItems.size() == 1) {
                try {
                    extMenuItems.get(0).method.invoke(ext, containerLayout, conversation);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                List<String> titles = new ArrayList<>(extMenuItems.size());
                for (ExtMenuItemWrapper itemWrapper : extMenuItems) {
                    if (itemWrapper.extContextMenuItem.titleResId() != 0) {
                        titles.add(activity.getString(itemWrapper.extContextMenuItem.titleResId()));
                    } else {
                        titles.add(itemWrapper.extContextMenuItem.title());
                    }
                }
                // TODO sort
                new MaterialDialog.Builder(activity).items(titles).itemsCallback((dialog, v, position, text) -> {
                    try {
                        extMenuItems.get(position).method.invoke(ext, containerLayout, conversation);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }).show();
            }
        }
    }


    public void bind(ConversationViewModel conversationViewModel, Conversation conversation) {
        this.conversation = conversation;
        setupExtViewPager(extViewPager);

        for (int i = 0; i < exts.size(); i++) {
            exts.get(i).onBind(activity, conversationViewModel, conversation, this, i);
        }
    }

    public void onDestroy() {
        for (int i = 0; i < exts.size(); i++) {
            exts.get(i).onDestroy();
        }
    }

    private void setupExtViewPager(ViewPagerFixed viewPager) {
        exts = ConversationExtManager.getInstance().getConversationExts(conversation);
        if (exts.isEmpty()) {
            return;
        }
        viewPager.setAdapter(new ConversationExtPagerAdapter(exts, index -> {
            onConversationExtClick(exts.get(index));
        }));
    }


    public void reset() {
        int childCount = containerLayout.getChildCount();
        
        while (--childCount > 0) {
            containerLayout.removeViewAt(childCount);
        }
        hideOnScroll = true;
    }


    public boolean canHideOnScroll() {
        return hideOnScroll;
    }

    public void disableHideOnScroll() {
        this.hideOnScroll = false;
    }

    public static final int REQUEST_CODE_MIN = 0x8000;



    public void startActivityForResult(Intent intent, int requestCode, int index) {
        int extRequestCode = (requestCode << 7) | 0x8000;
        extRequestCode += index;
        activity.startActivityForResult(intent, extRequestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int index = requestCode & 0x7F;
        exts.get(index).onActivityResult((requestCode >> 7) & 0xFF, resultCode, data);
    }

    private static class ExtMenuItemWrapper {
        ExtContextMenuItem extContextMenuItem;
        Method method;

        ExtMenuItemWrapper(ExtContextMenuItem extContextMenuItem, Method method) {
            this.extContextMenuItem = extContextMenuItem;
            this.method = method;
        }
    }
}
