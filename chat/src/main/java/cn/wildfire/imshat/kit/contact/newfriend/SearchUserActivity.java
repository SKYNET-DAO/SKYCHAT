package cn.wildfire.imshat.kit.contact.newfriend;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfirechat.imshat.R;

public class SearchUserActivity extends WfcBaseActivity {

    private SearchView searchView;
    private SearchUserFragment searchUserFragment;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected int menu() {
        return R.menu.search_user;
    }

    @Override
    protected void afterMenus(Menu menu) {
        super.afterMenus(menu);
        MenuItem searchItem = menu.findItem(R.id.search);
       
        searchView = (SearchView) searchItem.getActionView();
        initSearchView();
    }



    private void initSearchView() {
        searchView.onActionViewExpanded();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }
        });

    }

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_search_user));
        searchUserFragment = new SearchUserFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, searchUserFragment)
                .commit();
    }

    private void search(String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            searchUserFragment.showSearchPromptView(keyword);
        } else {
            searchUserFragment.hideSearchPromptView();
        }
    }

}
