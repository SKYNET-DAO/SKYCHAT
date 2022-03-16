package cn.wildfire.imshat.kit.search;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;


public abstract class SearchActivity extends WfcBaseActivity {
    private SearchFragment searchFragment;
    private List<SearchableModule> modules = new ArrayList<>();
    private SearchView searchView;

    @Override
    protected int contentLayout() {
        return R.layout.search_portal_activity;
    }

    @Override
    protected void afterViews() {
        initView();
    }

    @Override
    protected int menu() {
        return R.menu.search_portal;
    }

    @Override
    protected void afterMenus(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        
        searchView = (SearchView) searchItem.getActionView();
        initSearchView();
        String initialKeyword = getIntent().getStringExtra("keyword");
        if (!TextUtils.isEmpty(initialKeyword)) {
            searchView.setQuery(initialKeyword, true);
        }
    }

    private void initSearchView() {
        searchView.onActionViewExpanded();
        searchView.setQueryHint(UIUtils.getString(R.string.search));

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

    private void initView() {
        searchFragment = new SearchFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, searchFragment)
                .commit();
        initSearchModule(modules);
    }

    void search(String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            searchFragment.search(keyword, modules);
        } else {
            searchFragment.reset();
        }
    }

 
    protected abstract void initSearchModule(List<SearchableModule> modules);
}
