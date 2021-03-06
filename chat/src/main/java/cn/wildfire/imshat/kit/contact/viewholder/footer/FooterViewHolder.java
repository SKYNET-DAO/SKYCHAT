package cn.wildfire.imshat.kit.contact.viewholder.footer;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.contact.model.FooterValue;

public abstract class FooterViewHolder<T extends FooterValue> extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected ContactAdapter adapter;
    protected ContactViewModel contactViewModel;

    public FooterViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        contactViewModel = WfcUIKit.getAppScopeViewModel(ContactViewModel.class);
    }


    public abstract void onBind(T t);

}
