package cn.wildfire.imshat.kit.contact.viewholder.footer;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.model.ContactCountFooterValue;
import cn.wildfirechat.imshat.R;

import static cn.wildfire.imshat.kit.third.utils.UIUtils.getString;

@LayoutRes(resId = R.layout.contact_item_footer)
public class ContactCountViewHolder extends FooterViewHolder<ContactCountFooterValue> {
    @Bind(R.id.countTextView)
    TextView countTextView;
    private ContactAdapter adapter;

    public ContactCountViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBind(ContactCountFooterValue contactCountFooterValue) {
        int count = adapter.getContactCount();
        if (count == 0) {
            countTextView.setText(getString(R.string.str_contact_number));
        } else {
            countTextView.setText(count+" "+getString(R.string.str_contact_wei));
        }
    }
}
