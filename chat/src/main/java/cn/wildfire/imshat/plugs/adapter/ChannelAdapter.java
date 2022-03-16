package cn.wildfire.imshat.plugs.adapter;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.wildfire.imshat.plugs.beans.Channel;
import cn.wildfire.imshat.plugs.listener.OnChannelDragListener;
import cn.wildfirechat.imshat.R;




public class ChannelAdapter extends BaseMultiItemQuickAdapter<Channel, BaseViewHolder> {

    private BaseViewHolder mEditViewHolder;
    private boolean mIsEdit;
    private long startTime;
    
    private static final long SPACE_TIME = 100;
    private RecyclerView mRecyclerView;

    public ChannelAdapter(List<Channel> data) {
        super(data);
        
        mIsEdit = false;
        addItemType(Channel.TYPE_MY, R.layout.item_channel_title);
        addItemType(Channel.TYPE_MY_CHANNEL, R.layout.item_channel);
        addItemType(Channel.TYPE_OTHER, R.layout.item_channel_title);
        addItemType(Channel.TYPE_OTHER_CHANNEL, R.layout.item_channel);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mRecyclerView = (RecyclerView) parent;
        return super.onCreateViewHolder(parent, viewType);
    }

    private OnChannelDragListener onChannelDragListener;

    public void setOnChannelDragListener(OnChannelDragListener onChannelDragListener) {
        this.onChannelDragListener = onChannelDragListener;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final Channel channel) {
        switch (helper.getItemViewType()) {
            case Channel.TYPE_MY:
                
                mEditViewHolder = helper;
                helper.setText(R.id.tvTitle, channel.title)
                        .setOnClickListener(R.id.tvEdit, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!mIsEdit) {
                                    startEditMode(true);
                                    helper.setText(R.id.tvEdit, "OK");
                                } else {
                                    startEditMode(false);
                                    helper.setText(R.id.tvEdit, "Modify");
                                }
                            }
                        });
                break;
            case Channel.TYPE_OTHER:
                
                helper.setText(R.id.tvTitle, channel.title)
                        .setGone(R.id.tvEdit, false);
                break;
            case Channel.TYPE_MY_CHANNEL:
                
                helper
                        .setVisible(R.id.ivDelete, mIsEdit && !(channel.title.equals("Prefer")))
                        .setOnLongClickListener(R.id.rlItemView, new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (!mIsEdit) {
                                    
                                    startEditMode(true);
                                    mEditViewHolder.setText(R.id.tvEdit, "OK");
                                }
                                if (onChannelDragListener != null)
                                    onChannelDragListener.onStarDrag(helper);
                                return true;
                            }
                        }).setOnTouchListener(R.id.tvChannel, new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!mIsEdit) return false;
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startTime = System.currentTimeMillis();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                    
                                    if (onChannelDragListener != null)
                                        onChannelDragListener.onStarDrag(helper);
                                }
                                break;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                startTime = 0;
                                break;
                        }
                        return false;
                    }
                }).getView(R.id.ivDelete).setTag(true);
                helper.setText(R.id.tvChannel, channel.title).setOnClickListener(R.id.ivDelete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        
                        if (mIsEdit) {
                            int otherFirstPosition = getOtherFirstPosition();
                            int currentPosition = helper.getAdapterPosition() - getHeaderLayoutCount();
                            
                            View targetView = mRecyclerView.getLayoutManager().findViewByPosition(otherFirstPosition);
                            
                            View currentView = mRecyclerView.getLayoutManager().findViewByPosition(currentPosition);
                            
                            if (mRecyclerView.indexOfChild(targetView) >= 0 && otherFirstPosition != -1) {
                                RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
                                int spanCount = ((GridLayoutManager) manager).getSpanCount();
                                int targetX = targetView.getLeft();
                                int targetY = targetView.getTop();
                                int myChannelSize = getMyChannelSize();
                                if (myChannelSize % spanCount == 1) {
                                    
                                    targetY -= targetView.getHeight();
                                }

                                
                                channel.setItemType(Channel.TYPE_OTHER_CHANNEL);

                                if (onChannelDragListener != null)
                                    onChannelDragListener.onMoveToOtherChannel(currentPosition, otherFirstPosition - 1);
                                startAnimation(currentView, targetX, targetY);
                            } else {
                                channel.setItemType(Channel.TYPE_OTHER_CHANNEL);
                                if (otherFirstPosition == -1) otherFirstPosition = mData.size();
                                if (onChannelDragListener != null)
                                    onChannelDragListener.onMoveToOtherChannel(currentPosition, otherFirstPosition - 1);
                            }
//                            GlobalParams.mRemovedChannels.add(channel);
                        }
                    }


                });
                break;
            case Channel.TYPE_OTHER_CHANNEL:
                
                helper.setText(R.id.tvChannel, channel.title).setVisible(R.id.ivDelete, false)
                        .setOnClickListener(R.id.tvChannel, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int myLastPosition = getMyLastPosition();
                                int currentPosition = helper.getAdapterPosition() - getHeaderLayoutCount();
                                
                                View targetView = mRecyclerView.getLayoutManager().findViewByPosition(myLastPosition);
                                
                                View currentView = mRecyclerView.getLayoutManager().findViewByPosition(currentPosition);
                                
                                if (mRecyclerView.indexOfChild(targetView) >= 0 && myLastPosition != -1) {
                                    RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
                                    int spanCount = ((GridLayoutManager) manager).getSpanCount();
                                    int targetX = targetView.getLeft() + targetView.getWidth();
                                    int targetY = targetView.getTop();

                                    int myChannelSize = getMyChannelSize();
                                    if (myChannelSize % spanCount == 0) {
                                        

                                        View lastFourthView = mRecyclerView.getLayoutManager().findViewByPosition(getMyLastPosition() - 3);
//                                        View lastFourthView = mRecyclerView.getChildAt(getMyLastPosition() - 3);
                                        targetX = lastFourthView.getLeft();
                                        targetY = lastFourthView.getTop() + lastFourthView.getHeight();
                                    }


                                    
                                    channel.setItemType(Channel.TYPE_MY_CHANNEL);
                                    if (onChannelDragListener != null)
                                        onChannelDragListener.onMoveToMyChannel(currentPosition, myLastPosition + 1);
                                    startAnimation(currentView, targetX, targetY);
                                } else {
                                    channel.setItemType(Channel.TYPE_MY_CHANNEL);
                                    if (myLastPosition == -1) myLastPosition = 0;
                                    if (onChannelDragListener != null)
                                        onChannelDragListener.onMoveToMyChannel(currentPosition, myLastPosition + 1);
                                }
//                                GlobalParams.mRemovedChannels.remove(channel);

                            }
                        });
                break;
        }
    }

    public int getMyChannelSize() {
        int size = 0;
        for (int i = 0; i < mData.size(); i++) {
            Channel channel = (Channel) mData.get(i);
            if (channel.getItemType() == Channel.TYPE_MY_CHANNEL) {
                size++;
            }
        }
        return size;

    }

    private void startAnimation(final View currentView, int targetX, int targetY) {
        final ViewGroup parent = (ViewGroup) mRecyclerView.getParent();
        final ImageView mirrorView = addMirrorView(parent, currentView);
        TranslateAnimation animator = getTranslateAnimator(targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animator);
        animator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                parent.removeView(mirrorView);
                if (currentView.getVisibility() == View.INVISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    
    private ImageView addMirrorView(ViewGroup parent, View view) {
        view.destroyDrawingCache();
        
        view.setDrawingCacheEnabled(true);
        ImageView mirrorView = new ImageView(view.getContext());
        
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
       
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        mRecyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);
        return mirrorView;
    }

    private int ANIM_TIME = 360;

    
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    
    private int getOtherFirstPosition() {
        
        for (int i = 0; i < mData.size(); i++) {
            Channel channel = (Channel) mData.get(i);
            if (Channel.TYPE_OTHER_CHANNEL == channel.getItemType()) {
                
                return i;
            }
        }
        return -1;
    }

    
    private int getMyLastPosition() {
        for (int i = mData.size() - 1; i > -1; i--) {
            Channel channel = (Channel) mData.get(i);
            if (Channel.TYPE_MY_CHANNEL == channel.getItemType()) {
              
                return i;
            }
        }
        return -1;
    }

    
    private void startEditMode(boolean isEdit) {
        mIsEdit = isEdit;
        int visibleChildCount = mRecyclerView.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = mRecyclerView.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.ivDelete);
            if (imgEdit != null) {
                boolean isVis = imgEdit.getTag() == null ? false : (boolean) imgEdit.getTag();
                imgEdit.setVisibility(isVis && isEdit && !mData.get(i).title.equals("推荐") ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
}
